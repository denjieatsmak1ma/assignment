# Assignment 2 — Dynamic Array, Linked List, Min-Heap: Analysis & Benchmarks

## 1. Overview

Three structures implemented from scratch in Java: **Dynamic Array**, **Linked List**, **Min-Heap** (`src/`). `Tests.java` validates correctness (edge cases + cross-check vs `ArrayList`/`PriorityQueue`, 43 checks, 0 failed). `Benchmark.java` runs Workloads 1–4 on OpenJDK 21, `Random(42)`, 5 repeats averaged — all numbers below are **real measured data**, in `results/tables/*.csv`, plotted in `results/plots/*.png`.

## 2. Complexity Analysis

| Structure | Operation | Best | Avg | Worst | Space |
|---|---|---|---|---|---|
| Dynamic Array | `add(x)` append | Θ(1) | Θ(1)* | O(n) resize | O(1)* |
| Dynamic Array | `add(index,x)` / `remove(index)` | Ω(1) (end) | Θ(n) | O(n) (index 0) | O(1) |
| Dynamic Array | `get(index)` | Θ(1) | Θ(1) | Θ(1) | O(1) |
| Dynamic Array | `contains(x)` | Ω(1) | Θ(n) | O(n) | O(1) |
| Linked List | `add(x)` append (tail ptr) | Θ(1) | Θ(1) | Θ(1) | O(1) |
| Linked List | `add(index,x)` / `remove(index)` | Ω(1) (head) | Θ(n) | O(n) (tail/middle) | O(1) |
| Linked List | `get(index)` / `contains(x)` | Ω(1) | Θ(n) | O(n) | O(1) |
| Min-Heap | `insert(x)` | Ω(1) | O(log n) | O(log n) | O(1) |
| Min-Heap | `peekMin()` | Θ(1) | Θ(1) | Θ(1) | O(1) |
| Min-Heap | `extractMin()` | Ω(1) | O(log n) | O(log n) | O(1) |

*amortized.

**Why:** array `get` is one pointer-arithmetic dereference — no loop, independent of `n`. List `get`/`contains` must walk `next` pointers from `head` — Θ(n). Array `add/remove(index)` shift `n − index` elements (worst at index 0). List `add/remove(index)` costs Θ(index) to *find* the node (splice itself is O(1)) — so head ops are O(1), tail/middle are O(n). Heap height is Θ(log n) (complete binary tree), so sift-up/sift-down touch at most that many levels.

**Look-alikes, different cost:** `get(i)` — same signature, Θ(1) vs Θ(n). `add(0,x)` — O(n) on the array (shift everything) vs O(1) on the list (relink head) — same logical op, opposite cost, purely from memory layout.

## 3. Correctness — Loop Invariant Proofs

### 3.1 `DynamicArray.add(index, x)` (shift loop)
```java
for (int j = size; j > index; j--) data[j] = data[j - 1];
data[index] = x;
```
**Invariant:** at the start of the iteration with counter `j`, for every `k` in `[j, size]`: `data[k] = old[k-1]` (the suffix from `j` on has already been shifted right; `[0, j-1]` is untouched).
**Init:** `j = size` — range `[size, size]` is vacuous, nothing shifted yet — holds trivially.
**Maintenance:** given the invariant for `j`, the body sets `data[j] = data[j-1]`; since no earlier iteration wrote position `j-1`, it still equals `old[j-1]`, so now `data[j] = old[j-1]` — exactly the pattern extended to `k = j-1` after `j` decrements. Positions `> j` are untouched and still satisfy it by hypothesis.
**Termination:** `j` strictly decreases and the loop stops at `j = index`, after exactly `size − index` steps.
**Conclusion:** at exit, `data[k] = old[k-1]` for all `k ∈ [index, size]` — every element from `index` onward has moved one slot right, nothing before `index` was touched. `data[index] = x` fills the vacated slot. Result = `old[0..index-1], x, old[index..size-1]` — exactly "insert x at index". ∎

### 3.2 `MinHeap.extractMin()` (sift-down loop)
```java
while (true) {
    smallest = argmin(data[i], data[left], data[right]) among existing children;
    if (smallest == i) break;
    swap(i, smallest); i = smallest;
}
```
**Invariant:** every subtree is a valid heap *except possibly* the one rooted at the current `i`.
**Init:** only the new root (just moved from the last slot) can violate the heap property — holds.
**Maintenance:** if `smallest == i`, `i`'s subtree is now valid too (loop breaks — invariant holds with no exceptions). Otherwise, swapping puts the smaller child at `i` (local property restored at `i`), and the only remaining possible violation moves down to `i = smallest` — exactly the invariant's claim for the next iteration.
**Termination:** each step moves `i` one level deeper in a tree of height `⌊log₂ n⌋`, or breaks; a leaf always breaks (no children ⇒ `smallest == i`). So it halts in ≤ `⌊log₂ n⌋` steps.
**Conclusion:** the loop only exits when the current subtree is valid, and every other subtree was valid throughout ⇒ the whole array is a valid heap. `min = data[0]` was saved before any change, and was the true minimum on entry (heap property) — so the return value and the restored heap are both correct. ∎

## 4. Experimental Setup

- **n:** 100, 1,000, 10,000, 100,000 — same across all workloads.
- **m:** 10,000 `get()` (W1), 1,000 `contains()` (W2), 1,000 insert + up to 1,000 remove at index 0 and n/2 (W3), n inserts + n extracts (W4).
- **Repeats:** 5, average reported. **Timer:** `System.nanoTime()`, excludes data generation. **Seed:** single `Random(42)` instance — data drawn first, then the query/operation stream (a *second* `Random(42)` would replay the same sequence and under-count comparisons — avoided).
- **Metrics:** accesses/movements (W1, W3), comparisons (W2, W4), `sorted_order_ok` (W4).

**Reproduce:**
```bash
javac src/*.java -d out && java -cp out Tests && java -cp out Benchmark
python3 make_plots.py   # regenerate plots from fresh CSVs
```

## 5. Results

### Workload 1 — Random Access (10,000 `get()`)
| n | Array ms | List ms | Array accesses | List accesses |
|---|---|---|---|---|
| 100 | 0.25 | 0.91 | 10,000 | 511,327 |
| 1,000 | 0.04 | 5.79 | 10,000 | 5,021,262 |
| 10,000 | 0.01 | 60.6 | 10,000 | 50,180,278 |
| 100,000 | 0.12 | 630.8 | 10,000 | 505,028,648 |

Array flat (Θ(1)); List time and hop-count both scale ≈10× per 10× n (Θ(n)), matching theory almost exactly — a 5,000×+ gap by n=100,000.

### Workload 2 — Search (1,000 `contains()`)
| n | Array ms | List ms | Comparisons (both, equal) |
|---|---|---|---|
| 100 | 0.31 | 0.32 | 99,996 |
| 1,000 | 0.93 | 1.19 | 998,832 |
| 10,000 | 3.30 | 11.6 | 9,948,508 |
| 100,000 | 36.9 | 122.7 | 95,047,696 |

Comparison counts are **identical** between structures at every n (same Θ(n) work) — yet List takes up to 3.5× longer, purely from cache-unfriendly pointer chasing vs contiguous scanning.

### Workload 3 — Insert/Remove, 1,000 ops
**At index 0:** List stays O(1) (≤0.06 ms at every n, exactly 1,000 relinks); Array grows with n (1.1 ms → 14.9 ms, tracking Θ(n) shifts, ~201M movements at n=100,000).
**At index n/2:** both are Θ(n) — but constants flip: at n=100 Array is slower (1.76 vs 0.10 ms); by n=100,000 List is **23× slower** (59.1 vs 2.58 ms) despite doing *fewer* counted movements (50M vs 101M) — pointer-chasing to the midpoint costs far more per step than shifting packed ints.
*(full table: `results/tables/workload3_insert_remove.csv`; some small-n array timings are non-monotonic — JIT warm-up/GC noise on the very first calls, not a complexity issue.)*

### Workload 4 — Min-Heap (n inserts + n extracts)
| n | Insert ms | Extract ms | Comparisons | Sorted? |
|---|---|---|---|---|
| 100 | 0.025 | 0.071 | 1,069 | ✔ |
| 1,000 | 0.052 | 0.117 | 17,322 | ✔ |
| 10,000 | 0.447 | 0.775 | 239,284 | ✔ |
| 100,000 | 1.919 | 7.683 | 3,059,283 | ✔ |

Comparisons grow ×16.2/×13.8/×12.8 per decade, closely tracking the theoretical n·log₂n ratios (×15.0/×13.3/×12.5). `extractMin` always costs more than `insert` (full sift-down vs early-terminating sift-up). Output was non-decreasing in every trial — correctness confirmed alongside complexity.

## 6. Discussion

1–3. **n's effect / agreement with theory:** every workload's growth *class* (flat / linear / n log n) matches theory closely; only fine constants deviate (W2's comparison count is a touch below `1000·n` from early hits; a few W3 array timings are non-monotonic at small n from JIT warm-up/GC jitter — not complexity disagreements).
4–5. **Same Big-O, different speed — why:** constant factors from memory layout. W2's `contains()` is the cleanest proof: identical comparison counts, up to 3.5× time gap, purely cache locality (contiguous array vs scattered `Node`s). W3's middle case is starker: List does *half* the movements of the Array yet is 23× slower.
6. **Array wins when:** random access dominates (W1: 5,000×+ advantage) or ops happen near the end.
7. **List wins when:** inserts/removes happen at a known end, especially the head (W3 begin: List ≤0.06 ms vs Array's 14.9 ms at n=100,000).
8. **Heap wins when:** you always need the current min/max next — O(log n) insert *and* extract, vs O(n) naive scanning.
9. **Workload → structure:** random access → array; head-only insert/remove → list; repeated "give me the smallest" → heap; mixed/middle access → array, for its smaller constant, since both are O(n) there anyway.

## 7. Design Recommendations

- Random-access-heavy → **Dynamic Array**.
- Head-only insert/remove (queue/stack) → **Linked List**.
- Priority/"next smallest" workloads → **Min-Heap**.
- Arbitrary-position insert/remove at scale → **Dynamic Array** (same Θ(n) as list, but ~23× smaller constant, per W3).

## 8. Conclusion

Measured behavior matches theory closely across all four workloads and all n ∈ {100, 1e3, 1e4, 1e5}: Array = Θ(1) access / Θ(n) front-insert; List = the inverse; Heap = Θ(1) `peekMin` with O(log n) insert/extract, its comparison counts tracking n·log n almost exactly. Both required loop invariants (array shift-insert, heap sift-down) were proved via initialization/maintenance/termination. The few visible deviations (JIT warm-up, GC-pause noise at small n) have identifiable causes and don't contradict the complexity analysis; 43/43 correctness tests pass, including cross-validation against Java's standard collections.
