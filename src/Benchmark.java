import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class Benchmark {

    static final int[] SIZES = {100, 1_000, 10_000, 100_000};
    static final int REPEATS = 5;
    static final long SEED = 42L;
    static final String OUT_DIR = "results/tables";

    public static void main(String[] args) throws IOException {
        File outDir = new File(OUT_DIR);
        if (!outDir.exists() && !outDir.mkdirs()) {
            throw new IOException("Could not create output directory: " + outDir.getAbsolutePath());
        }
        workload1RandomAccess();
        workload2Search();
        workload3InsertRemove();
        workload4PriorityProcessing();
        System.out.println("Benchmark complete. See results/tables/*.csv");
    }

    static void workload1RandomAccess() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(OUT_DIR + "/workload1_random_access.csv"));
        out.println("structure,n,avg_time_ns,accesses");
        for (int n : SIZES) {
            long[] daTimes = new long[REPEATS];
            long[] llTimes = new long[REPEATS];
            long daAcc = 0, llAcc = 0;
            for (int rep = 0; rep < REPEATS; rep++) {
                Random gen = new Random(SEED);
                DynamicArray da = new DynamicArray();
                LinkedList ll = new LinkedList();
                for (int i = 0; i < n; i++) { int v = gen.nextInt(); da.add(v); ll.add(v); }

                int[] indices = new int[10_000];
                for (int i = 0; i < indices.length; i++) indices[i] = gen.nextInt(n);

                da.resetCounters();
                long t0 = System.nanoTime();
                for (int idx : indices) da.get(idx);
                daTimes[rep] = System.nanoTime() - t0;
                daAcc = da.accesses;

                ll.resetCounters();
                t0 = System.nanoTime();
                for (int idx : indices) ll.get(idx);
                llTimes[rep] = System.nanoTime() - t0;
                llAcc = ll.accesses;
            }
            out.println("DynamicArray," + n + "," + avg(daTimes) + "," + daAcc);
            out.println("LinkedList," + n + "," + avg(llTimes) + "," + llAcc);
        }
        out.close();
    }

    static void workload2Search() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(OUT_DIR + "/workload2_search.csv"));
        out.println("structure,n,avg_time_ns,comparisons");
        for (int n : SIZES) {
            long[] daTimes = new long[REPEATS];
            long[] llTimes = new long[REPEATS];
            long daCmp = 0, llCmp = 0;
            for (int rep = 0; rep < REPEATS; rep++) {
                Random gen = new Random(SEED);
                DynamicArray da = new DynamicArray();
                LinkedList ll = new LinkedList();
                for (int i = 0; i < n; i++) { int v = gen.nextInt(1_000_000); da.add(v); ll.add(v); }

                int[] queries = new int[1_000];
                for (int i = 0; i < queries.length; i++) queries[i] = gen.nextInt(1_000_000);

                da.resetCounters();
                long t0 = System.nanoTime();
                for (int q : queries) da.contains(q);
                daTimes[rep] = System.nanoTime() - t0;
                daCmp = da.comparisons;

                ll.resetCounters();
                t0 = System.nanoTime();
                for (int q : queries) ll.contains(q);
                llTimes[rep] = System.nanoTime() - t0;
                llCmp = ll.comparisons;
            }
            out.println("DynamicArray," + n + "," + avg(daTimes) + "," + daCmp);
            out.println("LinkedList," + n + "," + avg(llTimes) + "," + llCmp);
        }
        out.close();
    }

    static void workload3InsertRemove() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(OUT_DIR + "/workload3_insert_remove.csv"));
        out.println("structure,n,position,operation,avg_time_ns,movements");
        for (int n : SIZES) {
            runInsertRemoveAt(out, n, 0, "begin");
            runInsertRemoveAt(out, n, n / 2, "middle");
        }
        out.close();
    }

    static void runInsertRemoveAt(PrintWriter out, int n, int index, String label) {
        long[] daInsT = new long[REPEATS], llInsT = new long[REPEATS];
        long daInsMv = 0, llInsMv = 0;
        for (int rep = 0; rep < REPEATS; rep++) {
            Random gen = new Random(SEED);
            DynamicArray da = new DynamicArray();
            LinkedList ll = new LinkedList();
            for (int i = 0; i < n; i++) { int v = gen.nextInt(); da.add(v); ll.add(v); }
            int idx = Math.min(index, da.size());

            da.resetCounters();
            long t0 = System.nanoTime();
            for (int i = 0; i < 1000; i++) da.add(idx, i);
            daInsT[rep] = System.nanoTime() - t0;
            daInsMv = da.accesses;

            ll.resetCounters();
            t0 = System.nanoTime();
            for (int i = 0; i < 1000; i++) ll.add(idx, i);
            llInsT[rep] = System.nanoTime() - t0;
            llInsMv = ll.accesses;
        }
        out.println("DynamicArray," + n + "," + label + ",insert," + avg(daInsT) + "," + daInsMv);
        out.println("LinkedList," + n + "," + label + ",insert," + avg(llInsT) + "," + llInsMv);

        long[] daRemT = new long[REPEATS], llRemT = new long[REPEATS];
        long daRemMv = 0, llRemMv = 0;
        for (int rep = 0; rep < REPEATS; rep++) {
            Random gen = new Random(SEED);
            DynamicArray da = new DynamicArray();
            LinkedList ll = new LinkedList();
            for (int i = 0; i < n; i++) { int v = gen.nextInt(); da.add(v); ll.add(v); }
            int reps = Math.min(1000, n);

            da.resetCounters();
            long t0 = System.nanoTime();
            for (int i = 0; i < reps; i++) da.remove(Math.min(index, da.size() - 1));
            daRemT[rep] = System.nanoTime() - t0;
            daRemMv = da.accesses;

            ll.resetCounters();
            t0 = System.nanoTime();
            for (int i = 0; i < reps; i++) ll.remove(Math.min(index, ll.size() - 1));
            llRemT[rep] = System.nanoTime() - t0;
            llRemMv = ll.accesses;
        }
        out.println("DynamicArray," + n + "," + label + ",remove," + avg(daRemT) + "," + daRemMv);
        out.println("LinkedList," + n + "," + label + ",remove," + avg(llRemT) + "," + llRemMv);
    }

    static void workload4PriorityProcessing() throws IOException {
        PrintWriter out = new PrintWriter(new FileWriter(OUT_DIR + "/workload4_priority.csv"));
        out.println("n,avg_insert_time_ns,avg_extract_time_ns,comparisons,sorted_order_ok");
        for (int n : SIZES) {
            long[] insT = new long[REPEATS], extT = new long[REPEATS];
            long cmp = 0;
            boolean sortedOk = true;
            for (int rep = 0; rep < REPEATS; rep++) {
                Random gen = new Random(SEED);
                int[] values = new int[n];
                for (int i = 0; i < n; i++) values[i] = gen.nextInt();

                MinHeap heap = new MinHeap();
                heap.resetCounters();
                long t0 = System.nanoTime();
                for (int v : values) heap.insert(v);
                insT[rep] = System.nanoTime() - t0;
                long insertCmp = heap.comparisons;

                heap.resetCounters();
                int prev = Integer.MIN_VALUE;
                t0 = System.nanoTime();
                for (int i = 0; i < n; i++) {
                    int m = heap.extractMin();
                    if (m < prev) sortedOk = false;
                    prev = m;
                }
                extT[rep] = System.nanoTime() - t0;
                cmp = insertCmp + heap.comparisons;
            }
            out.println(n + "," + avg(insT) + "," + avg(extT) + "," + cmp + "," + sortedOk);
        }
        out.close();
    }

    static long avg(long[] xs) {
        long sum = 0;
        for (long x : xs) sum += x;
        return sum / xs.length;
    }
}
