import java.util.NoSuchElementException;

public class MinHeap {
    private int[] data;
    private int size;

    public long comparisons = 0;
    public long accesses = 0;

    public MinHeap() {
        this(16);
    }

    public MinHeap(int initialCapacity) {
        if (initialCapacity <= 0) initialCapacity = 1;
        data = new int[initialCapacity];
        size = 0;
    }

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void resetCounters() {
        comparisons = 0;
        accesses = 0;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= data.length) return;
        int newCapacity = Math.max(data.length * 2, minCapacity);
        int[] newData = new int[newCapacity];
        for (int i = 0; i < size; i++) { newData[i] = data[i]; accesses++; }
        data = newData;
    }

    private static int parent(int i) { return (i - 1) / 2; }
    private static int left(int i) { return 2 * i + 1; }
    private static int right(int i) { return 2 * i + 2; }

    private void swap(int i, int j) {
        int t = data[i];
        data[i] = data[j];
        data[j] = t;
        accesses += 4;
    }

    public void insert(int x) {
        ensureCapacity(size + 1);
        data[size] = x;
        accesses++;
        int i = size;
        size++;
        while (i > 0) {
            int p = parent(i);
            comparisons++;
            if (data[p] <= data[i]) break;
            swap(i, p);
            i = p;
        }
    }

    public int peekMin() {
        if (size == 0) throw new NoSuchElementException("Heap is empty");
        accesses++;
        return data[0];
    }

    public int extractMin() {
        if (size == 0) throw new NoSuchElementException("Heap is empty");
        int min = data[0];
        accesses++;
        size--;
        data[0] = data[size];
        accesses += 2;
        int i = 0;
        while (true) {
            int l = left(i);
            int r = right(i);
            int smallest = i;
            if (l < size) {
                comparisons++;
                if (data[l] < data[smallest]) smallest = l;
            }
            if (r < size) {
                comparisons++;
                if (data[r] < data[smallest]) smallest = r;
            }
            if (smallest == i) break;
            swap(i, smallest);
            i = smallest;
        }
        return min;
    }

    public boolean isValidHeap() {
        for (int i = 1; i < size; i++) {
            if (data[parent(i)] > data[i]) return false;
        }
        return true;
    }
}
