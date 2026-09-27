import java.util.NoSuchElementException;

public class DynamicArray {

    private int[] data;
    private int size;

    public long comparisons = 0;
    public long accesses = 0;

    public DynamicArray() {
        this(8);
    }

    public DynamicArray(int initialCapacity) {
        if (initialCapacity <= 0) initialCapacity = 1;
        data = new int[initialCapacity];
        size = 0;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void resetCounters() {
        comparisons = 0;
        accesses = 0;
    }

    private void ensureCapacity(int minCapacity) {
        if (minCapacity <= data.length) return;
        int newCapacity = data.length * 2;
        if (newCapacity < minCapacity) newCapacity = minCapacity;
        int[] newData = new int[newCapacity];
        for (int i = 0; i < size; i++) {
            newData[i] = data[i];
            accesses++;
        }
        data = newData;
    }

    public void add(int x) {
        ensureCapacity(size + 1);
        data[size] = x;
        accesses++;
        size++;
    }

    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        ensureCapacity(size + 1);
        for (int j = size; j > index; j--) {
            data[j] = data[j - 1];
            accesses += 2;
        }
        data[index] = x;
        accesses++;
        size++;
    }

    public int remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        int removed = data[index];
        accesses++;
        for (int j = index; j < size - 1; j++) {
            data[j] = data[j + 1];
            accesses += 2;
        }
        size--;
        return removed;
    }

    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        accesses++;
        return data[index];
    }

    public boolean contains(int x) {
        for (int i = 0; i < size; i++) {
            comparisons++;
            accesses++;
            if (data[i] == x) return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(data[i]);
            if (i < size - 1) sb.append(", ");
        }
        return sb.append("]").toString();
    }
}

