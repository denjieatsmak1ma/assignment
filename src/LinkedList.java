public class LinkedList {

    private static class Node {
        int value;
        Node next;
        Node(int value) { this.value = value; }
    }

    private Node head;
    private Node tail;
    private int size;

    public long comparisons = 0;
    public long accesses = 0;

    public int size() { return size; }
    public boolean isEmpty() { return size == 0; }

    public void resetCounters() {
        comparisons = 0;
        accesses = 0;
    }

    public void add(int x) {
        Node node = new Node(x);
        accesses++;
        if (tail == null) {
            head = tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    public void add(int index, int x) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        if (index == 0) {
            Node node = new Node(x);
            node.next = head;
            head = node;
            if (tail == null) tail = node;
            size++;
            accesses++;
            return;
        }
        if (index == size) {
            add(x);
            return;
        }
        Node prev = head;
        accesses++;
        int steps = 1;
        while (steps < index) {
            prev = prev.next;
            accesses++;
            steps++;
        }
        Node node = new Node(x);
        node.next = prev.next;
        prev.next = node;
        accesses += 2;
        size++;
    }

    public int remove(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        int removed;
        if (index == 0) {
            removed = head.value;
            head = head.next;
            if (head == null) tail = null;
            accesses++;
        } else {
            Node prev = head;
            accesses++;
            int steps = 0;
            while (steps < index - 1) {
                prev = prev.next;
                accesses++;
                steps++;
            }
            Node target = prev.next;
            removed = target.value;
            prev.next = target.next;
            if (target == tail) tail = prev;
            accesses += 2;
        }
        size--;
        return removed;
    }

    public int get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node cur = head;
        accesses++;
        for (int i = 0; i < index; i++) {
            cur = cur.next;
            accesses++;
        }
        return cur.value;
    }

    public boolean contains(int x) {
        Node cur = head;
        while (cur != null) {
            comparisons++;
            accesses++;
            if (cur.value == x) return true;
            cur = cur.next;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node cur = head;
        while (cur != null) {
            sb.append(cur.value);
            if (cur.next != null) sb.append(", ");
            cur = cur.next;
        }
        return sb.append("]").toString();
    }
}

