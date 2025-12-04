package heheheha;

import java.util.*;

public class HashTable<K, V>
{

    public enum Mode { CHAINING, OPEN_ADDRESSING } // What in the world is this?

    private enum SlotState { EMPTY_SINCE_START, OCCUPIED, EMPTY_AFTER_DELETE }

    private static class Entry<K, V>
    {
        K key;
        V value;
        Entry(K k, V v) { key = k; value = v; }
        @Override public String toString() { return key + "=" + value; }
    }

    private Mode mode;
    private int capacity;
    private double loadFactorThreshold;

    private LinkedList<Entry<K, V>>[] chains;       // For chaining (I guess)
    private Entry<K, V>[] table;                    // For open addressing
    private SlotState[] states;

    private int size;

    public HashTable(int initialCapacity, double loadFactorThreshold, Mode mode) // Needed help here
    {
        if (initialCapacity <= 0) initialCapacity = 16;
        this.capacity = initialCapacity;
        this.loadFactorThreshold = loadFactorThreshold;
        this.mode = mode;
        initiate();
    }

    public HashTable() // Here is the hash table
    {
        this(16, 0.7, Mode.CHAINING);
    }

    public int getHashing(K key)
    {
        return Math.floorMod(key.hashCode(), capacity);
    }

    @SuppressWarnings("unchecked")
    private void initiate()
    {
        size = 0;

        if (mode == Mode.CHAINING)
        {
            chains = (LinkedList<Entry<K,V>>[]) new LinkedList[capacity];
            for (int i = 0; i < capacity; i++) chains[i] = null;
            table = null;
            states = null;
        }
        else
        {
            table = (Entry<K,V>[]) new Entry[capacity];
            states = new SlotState[capacity];
            for (int i = 0; i < capacity; i++) states[i] = SlotState.EMPTY_SINCE_START;
            chains = null;
        }
    }

    public void add(K key, V value)
    {
        if (key == null) throw new IllegalArgumentException("Null keys not allowed.");
        if (mode == Mode.CHAINING) addChaining(key, value);
        else addOpenAddressing(key, value);

        if ((double) size / capacity > loadFactorThreshold)
        {
            resize(capacity * 2);
            System.out.println("[resize] New capacity = " + capacity);
        }
    }

    private void addChaining(K key, V value)
    {
        int idx = getHashing(key);
        if (chains[idx] == null) chains[idx] = new LinkedList<>(); // Here is the linked list!
        for (Entry<K,V> e : chains[idx])
        {
            if (e.key.equals(key))
            {
                e.value = value;
                return;
            }
        }
        chains[idx].add(new Entry<>(key, value));
        size++;
    }

    private void addOpenAddressing(K key, V value) // Needed help here
    {
        int idx = getHashing(key);
        int start = idx;
        int firstDeleted = -1;

        while (true)
        {
            if (states[idx] == SlotState.EMPTY_SINCE_START)
            {
                if (firstDeleted != -1) idx = firstDeleted;
                table[idx] = new Entry<>(key, value);
                states[idx] = SlotState.OCCUPIED;
                size++;
                return;
            } 
            else if (states[idx] == SlotState.EMPTY_AFTER_DELETE)
            {
                if (firstDeleted == -1) firstDeleted = idx;
            } 
            else if (states[idx] == SlotState.OCCUPIED)
            {
                if (table[idx].key.equals(key))
                {
                    table[idx].value = value;
                    return;
                }
            }

            idx = probing(idx);
            if (idx == start) throw new RuntimeException("Table unexpectedly full.");
        }
    }

    public boolean remove(K key) // Needed help here. I think I'm doing this right!
    {
        if (key == null) return false;
        return (mode == Mode.CHAINING)
                ? removeChaining(key)
                : removeOpenAddressing(key);
    }

    private boolean removeChaining(K key)
    {
        int idx = getHashing(key);
        if (chains[idx] == null) return false;

        Iterator<Entry<K,V>> it = chains[idx].iterator();
        while (it.hasNext())
        {
            Entry<K,V> e = it.next();
            if (e.key.equals(key))
            {
                it.remove();
                size--;
                return true;
            }
        }
        return false;
    }

    private boolean removeOpenAddressing(K key)
    {
        int idx = getHashing(key);
        int start = idx;

        while (states[idx] != SlotState.EMPTY_SINCE_START)
        {
            if (states[idx] == SlotState.OCCUPIED && table[idx].key.equals(key))
            {
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

    public V get(K key)
    {
        if (key == null) return null;
        return (mode == Mode.CHAINING)
                ? getChaining(key)
                : getOpenAddressing(key);
    }

    private V getChaining(K key)
    {
        int idx = getHashing(key);
        if (chains[idx] == null) return null;
        for (Entry<K,V> e : chains[idx])
        {
            if (e.key.equals(key)) return e.value;
        }
        return null;
    }

    private V getOpenAddressing(K key)
    {
        int idx = getHashing(key);
        int start = idx;

        while (states[idx] != SlotState.EMPTY_SINCE_START)
        {
            if (states[idx] == SlotState.OCCUPIED && table[idx].key.equals(key))
                return table[idx].value;

            idx = probing(idx);
            if (idx == start) break;
        }
        return null;
    }

    private int probing(int idx)
    {
        return (idx + 1) % capacity;
    }

    private void resize(int newCapacity)
    {
        List<Entry<K,V>> items = new ArrayList<>();

        if (mode == Mode.CHAINING)
        {
            for (int i = 0; i < capacity; i++)
                if (chains[i] != null)
                    items.addAll(chains[i]);
        } else {
            for (int i = 0; i < capacity; i++)
                if (states[i] == SlotState.OCCUPIED)
                    items.add(table[i]);
        }

        this.capacity = newCapacity;

        if (mode == Mode.CHAINING)
        {
            @SuppressWarnings("unchecked")
            LinkedList<Entry<K,V>>[] newChains =
                    (LinkedList<Entry<K,V>>[]) new LinkedList[newCapacity];
            chains = newChains;
            table = null;
            states = null;
        }
        else
        {
            @SuppressWarnings("unchecked") // weird warnings here and somewhere up
            Entry<K,V>[] newTable = (Entry<K,V>[]) new Entry[newCapacity];
            table = newTable;
            states = new SlotState[newCapacity];
            for (int i = 0; i < newCapacity; i++)
                states[i] = SlotState.EMPTY_SINCE_START;
            chains = null;
        }

        size = 0;
        for (Entry<K,V> e : items) add(e.key, e.value);
    }

    public void printState() // Here's the printed version!
    {
        System.out.println("=== HashTable (" + mode + ") capacity=" + capacity + " size=" + size + " ===");

        if (mode == Mode.CHAINING) {
            for (int i = 0; i < capacity; i++)
                System.out.println(i + ": " + (chains[i] == null ? "null" : chains[i]));
        } else {
            for (int i = 0; i < capacity; i++) {
                System.out.print(i + ": ");
                switch (states[i]) {
                    case EMPTY_SINCE_START -> System.out.println("EMPTY_SINCE_START");
                    case EMPTY_AFTER_DELETE -> System.out.println("EMPTY_AFTER_DELETE");
                    case OCCUPIED -> System.out.println("OCCUPIED(" + table[i] + ")");
                }
            }
        }

        System.out.println("========================================\n");
    }

    public int size() { return size; }
    public int capacity() { return capacity; }
}