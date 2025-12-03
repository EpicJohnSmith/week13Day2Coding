package heheheha;

public class HashTable {
	import java.util.*;

	/**
	 * Generic HashTable supporting two collision strategies:
	 *  - CHAINING (separate chaining using LinkedList)
	 *  - OPEN_ADDRESSING (linear probing)
	 *
	 * Basic operations: add, get, remove, resize.
	 */
	public class HashTable<K, V> {

	    public enum Mode { CHAINING, OPEN_ADDRESSING }

	    // For open addressing slot state
	    private enum SlotState { EMPTY_SINCE_START, OCCUPIED, EMPTY_AFTER_DELETE }

	    private static class Entry<K, V> {
	        K key;
	        V value;
	        Entry(K k, V v) { key = k; value = v; }
	        @Override public String toString() { return key + "=" + value; }
	    }

	    private Mode mode;
	    private int capacity;
	    private double loadFactorThreshold;

	    // For chaining
	    private LinkedList<Entry<K, V>>[] chains;

	    // For open addressing
	    private Entry<K, V>[] table;
	    private SlotState[] states;

	    private int size; // number of stored key-value pairs

	    /**
	     * Constructor
	     * @param initialCapacity initial number of buckets / slots
	     * @param loadFactorThreshold threshold (e.g., 0.7) to trigger resize
	     * @param mode CHAINING or OPEN_ADDRESSING
	     */
	    @SuppressWarnings("unchecked")
	    public HashTable(int initialCapacity, double loadFactorThreshold, Mode mode) {
	        if (initialCapacity <= 0) initialCapacity = 16;
	        this.capacity = initialCapacity;
	        this.loadFactorThreshold = loadFactorThreshold;
	        this.mode = mode;
	        initiate();
	    }

	    /** Default constructor: capacity 16, load factor .7, chaining by default */
	    public HashTable() {
	        this(16, 0.7, Mode.CHAINING);
	    }

	    /** Expose hashing function */
	    public int getHashing(K key) {
	        // Use modulo (positive) of key.hashCode()
	        return Math.floorMod(key.hashCode(), capacity);
	    }

	    /** Initialize internal structures depending on mode */
	    @SuppressWarnings("unchecked")
	    private void initiate() {
	        size = 0;
	        if (mode == Mode.CHAINING) {
	            chains = (LinkedList<Entry<K, V>>[]) new LinkedList[capacity];
	            for (int i = 0; i < capacity; i++) chains[i] = null; // mark null = empty bucket
	            table = null;
	            states = null;
	        } else {
	            table = (Entry<K, V>[]) new Entry[capacity];
	            states = new SlotState[capacity];
	            for (int i = 0; i < capacity; i++) states[i] = SlotState.EMPTY_SINCE_START;
	            chains = null;
	        }
	    }

	    /** Add (insert or update) */
	    public void add(K key, V value) {
	        if (key == null) throw new IllegalArgumentException("Null keys not supported.");
	        if (mode == Mode.CHAINING) addChaining(key, value);
	        else addOpenAddressing(key, value);

	        // check load factor & resize if needed
	        if ((double) size / capacity > loadFactorThreshold) {
	            resize(capacity * 2);
	            System.out.println("[resize] capacity increased to " + capacity);
	        }
	    }

	    /** Add for chaining */
	    private void addChaining(K key, V value) {
	        int idx = getHashing(key);
	        if (chains[idx] == null) {
	            chains[idx] = new LinkedList<>();
	        }
	        // If key exists, update
	        for (Entry<K, V> e : chains[idx]) {
	            if (e.key.equals(key)) {
	                e.value = value;
	                return;
	            }
	        }
	        chains[idx].add(new Entry<>(key, value));
	        size++;
	    }

	    /** Add for open addressing (linear probing) */
	    private void addOpenAddressing(K key, V value) {
	        int idx = getHashing(key);
	        int start = idx;
	        int firstDeleted = -1;
	        while (true) {
	            if (states[idx] == SlotState.EMPTY_SINCE_START) {
	                if (firstDeleted != -1) idx = firstDeleted; // prefer to reuse deleted slot
	                table[idx] = new Entry<>(key, value);
	                states[idx] = SlotState.OCCUPIED;
	                size++;
	                return;
	            } else if (states[idx] == SlotState.EMPTY_AFTER_DELETE) {
	                // mark potential reuse but keep probing in case key already exists later
	                if (firstDeleted == -1) firstDeleted = idx;
	            } else if (states[idx] == SlotState.OCCUPIED) {
	                Entry<K,V> e = table[idx];
	                if (e.key.equals(key)) {
	                    e.value = value; // update
	                    return;
	                }
	            }
	            // linear probing
	            idx = probing(idx);
	            if (idx == start) throw new RuntimeException("Hash table full (should have resized earlier).");
	        }
	    }

	    /** Remove */
	    public boolean remove(K key) {
	        if (key == null) return false;
	        if (mode == Mode.CHAINING) return removeChaining(key);
	        else return removeOpenAddressing(key);
	    }

	    private boolean removeChaining(K key) {
	        int idx = getHashing(key);
	        LinkedList<Entry<K,V>> list = chains[idx];
	        if (list == null) return false;
	        Iterator<Entry<K,V>> it = list.iterator();
	        while (it.hasNext()) {
	            Entry<K,V> e = it.next();
	            if (e.key.equals(key)) {
	                it.remove();
	                size--;
	                return true;
	            }
	        }
	        return false;
	    }

	    private boolean removeOpenAddressing(K key) {
	        int idx = getHashing(key);
	        int start = idx;
	        while (states[idx] != SlotState.EMPTY_SINCE_START) {
	            if (states[idx] == SlotState.OCCUPIED && table[idx].key.equals(key)) {
	                table[idx] = null;
	                states[idx] = SlotState.EMPTY_AFTER_DELETE;
	                size--;
	                return true;
	            }
	            idx = probing(idx);
	            if (idx == start) break;
	        }
	        return false;
	    }

	    /** Get (search) */
	    public V get(K key) {
	        if (key == null) return null;
	        if (mode == Mode.CHAINING) return getChaining(key);
	        else return getOpenAddressing(key);
	    }

	    private V getChaining(K key) {
	        int idx = getHashing(key);
	        LinkedList<Entry<K,V>> list = chains[idx];
	        if (list == null) return null;
	        for (Entry<K,V> e : list) if (e.key.equals(key)) return e.value;
	        return null;
	    }

	    private V getOpenAddressing(K key) {
	        int idx = getHashing(key);
	        int start = idx;
	        while (states[idx] != SlotState.EMPTY_SINCE_START) {
	            if (states[idx] == SlotState.OCCUPIED && table[idx].key.equals(key)) return table[idx].value;
	            idx = probing(idx);
	            if (idx == start) break;
	        }
	        return null;
	    }

	    /** Linear probing: move to next index (wrap around) */
	    private int probing(int idx) {
	        return (idx + 1) % capacity;
	    }

	    /** Resize to newCapacity and rehash all elements */
	    @SuppressWarnings("unchecked")
	    private void resize(int newCapacity) {
	        // Save old items
	        List<Entry<K,V>> items = new ArrayList<>();
	        if (mode == Mode.CHAINING) {
	            for (int i = 0; i < capacity; i++) {
	                if (chains[i] != null) {
	                    items.addAll(chains[i]);
	                }
	            }
	        } else {
	            for (int i = 0; i < capacity; i++) {
	                if (states[i] == SlotState.OCCUPIED) {
	                    items.add(table[i]);
	                }
	            }
	        }

	        // Set new capacity and reinit
	        this.capacity = newCapacity;
	        initiate();

	        // Reinsert
	        for (Entry<K,V> e : items) add(e.key, e.value);
	    }

	    /** Utility: print internal state (useful for demo) */
	    public void printState() {
	        System.out.println("=== HashTable State (" + mode + ") capacity=" + capacity + " size=" + size + ") ===");
	        if (mode == Mode.CHAINING) {
	            for (int i = 0; i < capacity; i++) {
	                System.out.print(i + ": ");
	                if (chains[i] == null) System.out.println("null");
	                else {
	                    System.out.println(chains[i]);
	                }
	            }
	        } else {
	            for (int i = 0; i < capacity; i++) {
	                System.out.print(i + ": ");
	                if (states[i] == SlotState.EMPTY_SINCE_START) System.out.println("EMPTY_SINCE_START");
	                else if (states[i] == SlotState.EMPTY_AFTER_DELETE) System.out.println("EMPTY_AFTER_DELETE");
	                else System.out.println("OCCUPIED(" + table[i] + ")");
	            }
	        }
	        System.out.println("========================================\n");
	    }

	    public int size() { return size; }
	    public int capacity() { return capacity; }
	}
}
