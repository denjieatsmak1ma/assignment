import java.util.Random;

public class Tests {

    private static int passed = 0;
    private static int failed = 0;

    private static void check(String name, boolean condition) {
        if (condition) {
            passed++;
        } else {
            failed++;
            System.out.println("FAILED: " + name);
        }
    }

    public static void main(String[] args) {
        testDynamicArray();
        testLinkedList();
        testMinHeap();
        crossValidateWithJavaCollections();

        System.out.println("\nPassed: " + passed + ", Failed: " + failed);
    }

    private static void testDynamicArray() {
        DynamicArray a = new DynamicArray();
        check("DA empty size", a.size() == 0);
        check("DA empty isEmpty", a.isEmpty());
        check("DA empty contains", !a.contains(5));

        a.add(10);
        check("DA one element size", a.size() == 1);
        check("DA one element get", a.get(0) == 10);
        check("DA one element contains", a.contains(10));

        a.add(20);
        a.add(30);
        a.add(0, 5);
        check("DA insert at 0", a.get(0) == 5 && a.get(1) == 10);
        a.add(2, 15);
        check("DA insert middle", a.get(2) == 15);
        a.add(a.size(), 99);
        check("DA insert at end", a.get(a.size() - 1) == 99);

        a.add(10);
        check("DA duplicates", a.contains(10));

        int removed = a.remove(0);
        check("DA remove at 0", removed == 5);
        removed = a.remove(a.size() - 1);
        check("DA remove at end", removed == 10);

        boolean threw = false;
        try { a.get(-1); } catch (IndexOutOfBoundsException e) { threw = true; }
        check("DA invalid index get(-1)", threw);

        threw = false;
        try { a.get(a.size()); } catch (IndexOutOfBoundsException e) { threw = true; }
        check("DA invalid index get(size)", threw);

        threw = false;
        try { a.add(-1, 1); } catch (IndexOutOfBoundsException e) { threw = true; }
        check("DA invalid index add(-1,x)", threw);

        DynamicArray big = new DynamicArray(2);
        Random rnd = new Random(42);
        int n = 50_000;
        int[] mirror = new int[n];
        for (int i = 0; i < n; i++) {
            int v = rnd.nextInt();
            mirror[i] = v;
            big.add(v);
        }
        boolean ok = true;
        for (int i = 0; i < n; i++) if (big.get(i) != mirror[i]) { ok = false; break; }
        check("DA large input integrity", ok && big.size() == n);
    }

    private static void testLinkedList() {
        LinkedList l = new LinkedList();
        check("LL empty size", l.size() == 0);
        check("LL empty isEmpty", l.isEmpty());
        check("LL empty contains", !l.contains(1));

        l.add(1);
        check("LL one element get", l.get(0) == 1);

        l.add(2);
        l.add(3);
        l.add(0, 0);
        check("LL insert at head", l.get(0) == 0);
        l.add(2, 99);
        check("LL insert middle", l.get(2) == 99);

        l.add(1);
        check("LL duplicates", l.contains(1));

        l.add(l.size(), 100);
        check("LL insert at tail", l.get(l.size() - 1) == 100);

        int removed = l.remove(0);
        check("LL remove head", removed == 0);
        removed = l.remove(l.size() - 1);
        check("LL remove tail", removed == 100);

        boolean threw = false;
        try { l.get(-1); } catch (IndexOutOfBoundsException e) { threw = true; }
        check("LL invalid index get(-1)", threw);

        threw = false;
        try { l.remove(l.size()); } catch (IndexOutOfBoundsException e) { threw = true; }
        check("LL invalid index remove(size)", threw);

        LinkedList big = new LinkedList();
        Random rnd = new Random(42);
        int n = 50_000;
        int[] mirror = new int[n];
        for (int i = 0; i < n; i++) {
            int v = rnd.nextInt();
            mirror[i] = v;
            big.add(v);
        }
        boolean ok = true;
        for (int i = 0; i < n; i++) if (big.get(i) != mirror[i]) { ok = false; break; }
        check("LL large input integrity", ok && big.size() == n);
    }

    private static void testMinHeap() {
        MinHeap h = new MinHeap();
        check("Heap empty size", h.size() == 0);
        boolean threw = false;
        try { h.peekMin(); } catch (Exception e) { threw = true; }
        check("Heap empty peekMin throws", threw);
        threw = false;
        try { h.extractMin(); } catch (Exception e) { threw = true; }
        check("Heap empty extractMin throws", threw);

        h.insert(5);
        check("Heap one element peekMin", h.peekMin() == 5);
        check("Heap one element valid", h.isValidHeap());

        h.insert(3);
        h.insert(8);
        h.insert(1);
        h.insert(1);
        check("Heap duplicates allowed", h.peekMin() == 1);
        check("Heap valid after inserts", h.isValidHeap());

        int[] extracted = new int[h.size()];
        for (int i = 0; i < extracted.length; i++) extracted[i] = h.extractMin();
        boolean sorted = true;
        for (int i = 1; i < extracted.length; i++) if (extracted[i] < extracted[i - 1]) sorted = false;
        check("Heap extractMin non-decreasing order", sorted);
        check("Heap empty after all extracted", h.isEmpty());

        MinHeap big = new MinHeap();
        Random rnd = new Random(42);
        int n = 20_000;
        int[] values = new int[n];
        for (int i = 0; i < n; i++) {
            values[i] = rnd.nextInt(1_000_000);
            big.insert(values[i]);
        }
        check("Heap valid after large inserts", big.isValidHeap());
        int[] expected = values.clone();
        java.util.Arrays.sort(expected);
        boolean allMatch = true;
        for (int i = 0; i < n; i++) {
            if (big.extractMin() != expected[i]) { allMatch = false; break; }
        }
        check("Heap large-input extraction matches sorted order", allMatch);
    }

    private static void crossValidateWithJavaCollections() {
        Random rnd = new Random(42);
        int n = 5_000;
        DynamicArray da = new DynamicArray();
        LinkedList ll = new LinkedList();
        java.util.ArrayList<Integer> ref = new java.util.ArrayList<>();

        for (int i = 0; i < n; i++) {
            int v = rnd.nextInt(10_000);
            da.add(v); ll.add(v); ref.add(v);
        }
        for (int i = 0; i < 1000; i++) {
            int idx = rnd.nextInt(ref.size());
            int v = rnd.nextInt(10_000);
            da.add(idx, v); ll.add(idx, v); ref.add(idx, v);
        }
        for (int i = 0; i < 500; i++) {
            int idx = rnd.nextInt(ref.size());
            int a = da.remove(idx);
            int b = ll.remove(idx);
            int c = ref.remove(idx);
            if (a != b || b != c) { check("cross-validate remove mismatch at op " + i, false); return; }
        }
        boolean allEqual = true;
        for (int i = 0; i < ref.size(); i++) {
            if (da.get(i) != ref.get(i) || ll.get(i) != ref.get(i)) { allEqual = false; break; }
        }
        check("cross-validate DynamicArray/LinkedList vs ArrayList", allEqual && da.size() == ref.size() && ll.size() == ref.size());

        boolean containsOk = true;
        for (int i = 0; i < 200; i++) {
            int v = rnd.nextInt(20_000);
            boolean expected = ref.contains(v);
            if (da.contains(v) != expected || ll.contains(v) != expected) { containsOk = false; break; }
        }
        check("cross-validate contains() vs ArrayList.contains()", containsOk);

        MinHeap heap = new MinHeap();
        java.util.PriorityQueue<Integer> pq = new java.util.PriorityQueue<>();
        for (int i = 0; i < 5000; i++) {
            int v = rnd.nextInt(100_000);
            heap.insert(v); pq.add(v);
        }
        boolean pqOk = true;
        while (!pq.isEmpty()) {
            if (heap.extractMin() != pq.poll()) { pqOk = false; break; }
        }
        check("cross-validate MinHeap vs java.util.PriorityQueue", pqOk);
    }
}
