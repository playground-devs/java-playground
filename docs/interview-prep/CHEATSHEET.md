# Interview Cheat Sheet — 10-Minute Pre-Interview Skim

All classes referenced below live in `src/main/java/com/playground/java/interview/`. Package shown as `pkg/ClassName`.

## Top 50 must-know problems (do these cold, no notes)

| # | Problem | Location | Pattern |
|---|---|---|---|
| 1 | Two Sum | p0mustknow/TwoSum | HashMap |
| 2 | Three Sum | p0mustknow/ThreeSum | Sort + Two Pointers |
| 3 | Max Subarray (Kadane) | p0mustknow/MaxSubArrayKadane | DP / Greedy |
| 4 | Product of Array Except Self | p0mustknow/ProductOfArrayExceptSelf | Prefix/Suffix product |
| 5 | Merge Intervals | p0mustknow/MergeIntervals | Sort + sweep |
| 6 | Valid Parentheses | p0mustknow/ValidParentheses | Stack |
| 7 | Longest Substring Without Repeating Chars | p0mustknow/LongestSubstringWithoutRepeating | Sliding Window |
| 8 | Group Anagrams | p0mustknow/GroupAnagrams | HashMap signature |
| 9 | Top K Frequent Elements | p0mustknow/TopKFrequentElements | Heap / Bucket sort |
| 10 | Container With Most Water | p0mustknow/ContainerWithMostWater | Two Pointers |
| 11 | Trapping Rain Water | p0mustknow/TrappingRainWater | Two Pointers |
| 12 | Minimum Window Substring | p0mustknow/MinimumWindowSubstring | Sliding Window |
| 13 | Binary Search | p0mustknow/BinarySearch | Binary Search |
| 14 | Search in Rotated Sorted Array | p0mustknow/SearchInRotatedSortedArray | Modified Binary Search |
| 15 | Kth Largest Element | p0mustknow/KthLargestElement | Heap / QuickSelect |
| 16 | Reverse Linked List | p0mustknow/ReverseLinkedList | Linked List |
| 17 | Detect Cycle in Linked List | p0mustknow/DetectCycleLinkedList | Floyd's Cycle |
| 18 | Merge Two Sorted Lists | p0mustknow/MergeTwoSortedLists | Linked List |
| 19 | **LRU Cache** | p0mustknow/LRUCache | HashMap + DLL |
| 20 | Min Stack | p0mustknow/MinStack | Stack |
| 21 | Next Greater Element | p0mustknow/NextGreaterElement | Monotonic Stack |
| 22 | Level Order Traversal | p0mustknow/LevelOrderTraversal | BFS |
| 23 | Tree Traversals (in/pre/post, iterative) | p0mustknow/BinaryTreeTraversals | Stack / Recursion |
| 24 | Max Depth of Binary Tree | p0mustknow/MaxDepthOfBinaryTree | Recursion / BFS |
| 25 | Validate BST | p0mustknow/ValidateBST | Range recursion |
| 26 | Lowest Common Ancestor | p0mustknow/LowestCommonAncestor | Recursion |
| 27 | Top/Bottom/Left/Right View | p0mustknow/*ViewOfBinaryTree | BFS + HashMap/TreeMap |
| 28 | Number of Islands | p0mustknow/NumberOfIslands | Grid BFS/DFS |
| 29 | Clone Graph | p0mustknow/CloneGraph | BFS/DFS + HashMap |
| 30 | Course Schedule (Topo Sort) | p0mustknow/CourseScheduleTopoSort | Kahn's Algorithm |
| 31 | Permutations | p0mustknow/Permutations | Backtracking |
| 32 | Subsets | p0mustknow/Subsets | Backtracking |
| 33 | Generate Parentheses | p0mustknow/GenerateParentheses | Backtracking |
| 34 | Climbing Stairs | p0mustknow/ClimbingStairs | DP (Fibonacci) |
| 35 | Coin Change | p0mustknow/CoinChange | DP |
| 36 | Longest Common Subsequence | p0mustknow/LongestCommonSubsequence | DP (2D) |
| 37 | Word Break | p0mustknow/WordBreak | DP |
| 38 | House Robber | dynamicprogramming/HouseRobber | DP |
| 39 | Longest Increasing Subsequence | dynamicprogramming/LongestIncreasingSubsequence | DP / Binary Search |
| 40 | 0/1 Knapsack | dynamicprogramming/ZeroOneKnapsack | DP |
| 41 | Edit Distance | dynamicprogramming/EditDistance | DP (2D) |
| 42 | N-Queens | backtracking/NQueens | Backtracking |
| 43 | Word Search | backtracking/WordSearch | DFS + Backtracking |
| 44 | Rotting Oranges | graphs/RottingOranges | Multi-source BFS |
| 45 | Sliding Window Maximum | deque/SlidingWindowMaximum | Monotonic Deque |
| 46 | Median from Data Stream | heap/MedianFromDataStream | Two Heaps |
| 47 | Merge K Sorted Lists | sorting/MergeKSortedLists | Heap |
| 48 | Implement Trie | trie/ImplementTrie | Trie |
| 49 | Second Most Frequent Element | p0mustknow/FindSecondMostFrequentElement | HashMap / Streams |
| 50 | Serialize/Deserialize Binary Tree | trees/SerializeDeserializeBinaryTree | Preorder + Queue |

## Top DSA patterns → when to reach for them

| Signal in the problem | Pattern | Example |
|---|---|---|
| "contiguous subarray/substring" | Sliding Window | LongestSubstringWithoutRepeating |
| "sorted array, find pair/triplet" | Two Pointers | ThreeSum, ContainerWithMostWater |
| "range sum queries, many times" | Prefix Sum | RangeSumQueryImmutable |
| "find target in sorted / minimize-maximize" | Binary Search (incl. on the answer) | SearchInRotatedSortedArray, KokoEatingBananas |
| "next greater/smaller, span" | Monotonic Stack | NextGreaterElement, DailyTemperatures |
| "max/min of every window" | Monotonic Deque | SlidingWindowMaximum |
| "top-K / kth something" | Heap | KthLargestElement, KClosestPointsToOrigin |
| "count ways / min-max over choices with overlap" | DP | CoinChange, EditDistance |
| "generate all combinations/arrangements" | Backtracking | Permutations, NQueens |
| "shortest path unweighted" | BFS | RottingOranges, WordLadder |
| "shortest path weighted, non-negative" | Dijkstra (heap) | DijkstraShortestPath |
| "shortest path, negative edges" | Bellman-Ford | BellmanFordShortestPath |
| "ordering with dependencies" | Topological Sort | CourseScheduleTopoSort |
| "connected components / cycle in undirected graph" | Union-Find | NumberOfProvincesUnionFind |
| "prefix matching many words" | Trie | ImplementTrie, WordSearchII |
| "recently used eviction" | HashMap + Doubly Linked List | LRUCache |

## Java 8 must-fire programs
map/filter/reduce (StreamsBasicsMapFilterReduce) · map vs flatMap (MapVsFlatMap) · groupingBy +counting/summing (GroupingByExample) · partitioningBy (PartitioningByExample) · toMap with duplicate-key merge function (CollectorsToMapDuplicateKeys) · Comparator chaining (SortingWithComparatorStreams) · find duplicates (FindDuplicatesUsingStreams) · frequency count (FrequencyCountUsingStreams) · 2nd highest/lowest (SecondHighestLowestUsingStreams) · List→Map (ListToMapConversion) · Optional pitfalls: orElse (eager) vs orElseGet (lazy) (OptionalDeepDive) · 4 kinds of method references (MethodReferencesAllFourKinds) · equals()/hashCode() contract break demo (EqualsAndHashCodeContract) · PECS wildcards (GenericsBoundedTypesDemo).

## Java concurrency must-fire programs
Double-checked locking + enum singleton (ThreadSafeSingleton) · synchronized vs ReentrantLock (SynchronizedVsReentrantLock) · wait/notify producer-consumer (ProducerConsumerWaitNotify) · BlockingQueue producer-consumer (ProducerConsumerBlockingQueue) · custom blocking queue with Condition (CustomBlockingQueue) · AtomicInteger vs synchronized (AtomicVsSynchronizedCounter) · ExecutorService shutdown lifecycle (ExecutorServicePatterns) · thenApply vs thenCompose (CompletableFutureChaining) · reproduce+fix a real deadlock (DeadlockDemo) · reproduce+fix a real race condition (RaceConditionDemo) · ConcurrentHashMap vs synchronizedMap iteration (ConcurrentHashMapVsSynchronizedMap).

## LRU / cache family — know the differences
- **LRUCache** (p0mustknow): HashMap<Key,Node> + Doubly Linked List, evict tail on overflow. O(1) get/put.
- **LFUCache** (cache): HashMap<Key,Node> + HashMap<freq, LinkedHashSet<Key>> + minFreq pointer. O(1) get/put, evicts least-frequent (LRU tie-break).
- **InMemoryTTLCache** (cache): ConcurrentHashMap + lazy expiry on read + background sweeper thread.
- **RateLimiterTokenBucket / RateLimiterSlidingWindowCounter** (cache): token bucket allows bursts up to capacity; sliding window counter fixes the fixed-window boundary problem.
- **ConcurrentLRUCache** (cache): the LRU design wrapped in one ReentrantLock (mention sharding by `hash(key)%N` as the scale-up answer).

### Why HashMap + Doubly Linked List for LRU (the answer the interviewer wants)
- HashMap alone: O(1) lookup, but no notion of "recency" — can't find the least-recently-used item without an O(n) scan.
- Doubly Linked List alone: O(1) to move an item to the front/back if you already have a reference to its node, but O(n) to *find* that node by key.
- Together: HashMap gives you the node reference in O(1); the DLL gives you O(1) removal/reinsertion at either end **because you have both `prev` and `next`** — a singly linked list can't unlink a node in O(1) without knowing its predecessor.
- Dummy head/tail sentinels remove all the null-check special cases at the boundaries.
- Net result: `get()` = HashMap lookup + move node to front = O(1). `put()` = HashMap lookup/insert + add-to-front (+ evict tail if over capacity) = O(1).

## Tree/graph traversal quick reference
- **BFS** (Queue) → level order, shortest path in unweighted graphs, top/left/right view (first node per level/column wins), rotting oranges.
- **DFS** (recursion or explicit Stack) → most tree problems, path-sum style backtracking, number of islands, cycle detection.
- **first-seen-at-column wins** during BFS → Top View; **last-seen wins** → Bottom View.
- Postorder iterative is the hardest traversal — needs two stacks, or one stack + a "last visited" marker.

## Common interview tricks / gotchas
- Validate BST: checking only `left.val < node.val < right.val` is **wrong** — a grandchild can violate an ancestor's bound. Pass a `(min, max)` range down instead.
- LRU: it must be a *doubly* linked list — O(1) removal needs the `prev` pointer.
- Negative mod in Java: `((x % k) + k) % k` to normalize (SubarraySumDivisibleByK).
- `orElse()` always evaluates its argument eagerly; `orElseGet()` is lazy — matters when the fallback is expensive (OptionalDeepDive).
- Override `equals()` without `hashCode()` and HashSet/HashMap silently break (EqualsAndHashCodeContract).
- `wait()` must be called in a `while` loop, never `if` — spurious wakeups (ProducerConsumerWaitNotify).
- Deadlock needs consistent lock ordering across all threads, not just two.

## Complexity cheat sheet
| Operation | Complexity |
|---|---|
| HashMap get/put | O(1) avg, O(n) worst (bad hash) |
| TreeMap get/put | O(log n) |
| ArrayList get / add at end | O(1) / O(1) amortized |
| ArrayList add/remove at front | O(n) |
| LinkedList add/remove at known node | O(1) |
| Binary Search | O(log n) |
| Heap insert/extract-min | O(log n) |
| Build heap from array | O(n) |
| Merge Sort / Heap Sort | O(n log n) always |
| Quick Sort | O(n log n) avg, O(n²) worst |
| BFS/DFS on graph | O(V + E) |
| Dijkstra (heap) | O((V+E) log V) |
| Union-Find (path compression + rank) | ~O(1) amortized |
| DP over 1D / 2D state | O(n) / O(n·m) |

## Which data structure for which problem
| Need | Structure |
|---|---|
| Fast existence/lookup, no order | HashSet / HashMap |
| Insertion order preserved | LinkedHashMap/Set |
| Sorted keys, range queries | TreeMap / TreeSet |
| Recency-based eviction | HashMap + Doubly Linked List (LRU) |
| Frequency-based eviction | HashMap + freq buckets (LFU) |
| Kth largest/smallest, streaming | Heap (PriorityQueue) |
| LIFO undo / matching brackets | Stack |
| FIFO processing / BFS | Queue (ArrayDeque) |
| Both-end access / sliding window max | Deque (ArrayDeque) |
| Prefix matching | Trie |
| Connectivity / cycle in undirected graph | Union-Find |
| Range sum, static array | Prefix sum array |
| Range sum, mutable array | Segment Tree / Fenwick Tree |
