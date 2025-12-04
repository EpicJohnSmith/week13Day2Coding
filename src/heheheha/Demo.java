package heheheha;

public class Demo {
    public static void main(String[] args) // Sort of needed help here...heh, AI documentation
    {

        System.out.println("=== DEMO: Chaining Mode ===");
        HashTable<String, Integer> htChain =
                new HashTable<>(8, 0.7, HashTable.Mode.CHAINING);

        htChain.add("A", 1);
        htChain.add("B", 2);
        htChain.add("C", 3);

        htChain.printState();
        System.out.println("Get B = " + htChain.get("B"));
        htChain.remove("B");
        htChain.printState();

        System.out.println("\n=== DEMO: Linear Probing Mode ===");
        HashTable<String, Integer> htOpen =
                new HashTable<>(8, 0.7, HashTable.Mode.OPEN_ADDRESSING);

        htOpen.add("A", 1);
        htOpen.add("B", 2);
        htOpen.add("C", 3);
        htOpen.add("D", 4);

        htOpen.printState();
        System.out.println("Get C = " + htOpen.get("C"));
        htOpen.remove("B");
        htOpen.printState();

        htOpen.add("E", 5);
        htOpen.add("F", 6);
        htOpen.add("G", 7);
        htOpen.add("H", 8);
        htOpen.add("I", 9);

        htOpen.printState();
    }
}
