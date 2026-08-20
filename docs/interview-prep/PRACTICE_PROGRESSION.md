# Practice Progression

Work through these levels in order. Within a level, do P0 files before P1/P2/P3. Every file has a runnable `main()` — run it, then re-derive the solution without looking, then explain your approach out loud in under 2 minutes before checking the code.

## Level 1 — Easy fundamentals
Arrays → Strings → HashMap → Sorting → Basic Java 8
- `p0mustknow/`: TwoSum, MaxSubArrayKadane, ProductOfArrayExceptSelf, ValidParentheses, GroupAnagrams, TopKFrequentElements
- `arrays/`: RotateArray, MoveZeroes, MergeSortedArrayInPlace
- `strings/`: ValidAnagram, LongestPalindromicSubstring, StringCompression, ReverseWordsInString
- `hashmap/`: SubarraySumEqualsK, LongestConsecutiveSequence, FindAllDuplicatesInArray, IsomorphicStrings
- `sorting/`: MergeSortImplementation, QuickSortImplementation
- `java8/`: StreamsBasicsMapFilterReduce, MapVsFlatMap, GroupingByExample, FindDuplicatesUsingStreams, FrequencyCountUsingStreams, SecondHighestLowestUsingStreams

## Level 2 — Core DSA
Two Pointers → Sliding Window → Linked List → Stack/Queue → Binary Search → Trees
- `p0mustknow/`: ContainerWithMostWater, TrappingRainWater, MinimumWindowSubstring, ReverseLinkedList, DetectCycleLinkedList, MergeTwoSortedLists, MinStack, NextGreaterElement, BinarySearch, SearchInRotatedSortedArray, LevelOrderTraversal, BinaryTreeTraversals, MaxDepthOfBinaryTree, ValidateBST, LowestCommonAncestor, TopViewOfBinaryTree, BottomViewOfBinaryTree, LeftViewOfBinaryTree, RightViewOfBinaryTree
- `twopointers/`: ThreeSumClosest, SortColorsDutchFlag
- `slidingwindow/`: LongestSubstringKDistinct, FruitIntoBaskets
- `prefixsum/`: RangeSumQueryImmutable, SubarraySumDivisibleByK
- `linkedlist/`: ReverseLinkedListII, RemoveNthNodeFromEnd, AddTwoNumbers, LinkedListPalindromeCheck
- `stack/`, `queue/`, `deque/`: EvaluateReversePolishNotation, LargestRectangleInHistogram, DailyTemperatures, ImplementQueueUsingStacksAndStackUsingQueues, SlidingWindowMaximum
- `binarysearch/`: SearchInsertPosition, FindPeakElement
- `trees/`, `bst/`: DiameterOfBinaryTree, PathSumII, ZigzagLevelOrderTraversal, ConvertSortedArrayToBST, KthSmallestInBST

## Level 3 — Senior-level
Heap → Graphs → Backtracking → Dynamic Programming → Trie → Advanced Trees
- `heap/`: KClosestPointsToOrigin, MedianFromDataStream
- `sorting/`: MergeKSortedLists, KthSmallestInSortedMatrix
- `p0mustknow/`: NumberOfIslands, CloneGraph, CourseScheduleTopoSort, Permutations, Subsets, GenerateParentheses, ClimbingStairs, CoinChange, LongestCommonSubsequence, WordBreak
- `graphs/`: RottingOranges, NumberOfProvincesUnionFind, DijkstraShortestPath, WordLadder, AlienDictionary, RedundantConnectionUnionFind, MinimumSpanningTreeKruskal, GraphCycleDetection, BellmanFordShortestPath
- `backtracking/`: CombinationSum, NQueens, WordSearch, CombinationSumII, PalindromePartitioning, SubsetsII, SudokuSolver
- `dynamicprogramming/`: HouseRobber, LongestIncreasingSubsequence, ZeroOneKnapsack, EditDistance, UniquePaths, PartitionEqualSubsetSum, DecodeWays, MaximalSquare
- `trie/`: ImplementTrie, WordSearchII
- `bst/`: BSTIterator, RecoverBST, PopulateNextRightPointersInEachNode
- `trees/`: SerializeDeserializeBinaryTree, VerticalOrderTraversal
- `matrix/`, `bitmanipulation/`, `intervals/`, `mathpuzzles/`, `greedy/`: round these out once the above feel solid

## Level 4 — Backend coding
LRU Cache → LFU Cache → Rate Limiter → Concurrency → Producer-Consumer → Thread-safe components
- `p0mustknow/LRUCache` — do this one until you can write it in 15 minutes without looking, including the "why HashMap+DLL" explanation
- `cache/`: LFUCache, InMemoryTTLCache, RateLimiterTokenBucket, RateLimiterSlidingWindowCounter, ConcurrentLRUCache
- `concurrency/` P0 set first: ThreadSafeSingleton, SynchronizedVsReentrantLock, ProducerConsumerWaitNotify, ProducerConsumerBlockingQueue, AtomicVsSynchronizedCounter, ExecutorServicePatterns, CompletableFutureChaining, DeadlockDemo, RaceConditionDemo
- `concurrency/` P1 set: CustomBlockingQueue, ReadWriteLockDemo, ConcurrentHashMapVsSynchronizedMap, CountDownLatchAndCyclicBarrierDemo, SemaphoreConnectionPool, ThreadLocalDemo

## Level 5 — Senior/Staff thinking
LLD coding → Design patterns → Scalability → Concurrency design → Performance trade-offs
- `lld/BookingReservationSystem` first — most relevant to a hospitality company like Marriott
- `lld/`: ParkingLotSystem (State + Strategy), ElevatorSystem (State pattern), PaymentProcessingStrategy (Strategy/OCP), UrlShortener (encoding + distributed ID discussion), LogFileProcessor (streaming vs load-all), CoffeeVendingMachine, InventoryManagementSystem
- `concurrency/ForkJoinTaskDemo`, `matrix/TrappingRainWater2D` — staff-level stretch problems
- For every LLD file: be ready to answer "how would you make this thread-safe?" and "how would this scale to millions of users/entries?" — both are addressed in each file's INTERVIEW FOLLOW-UPS section

---

## How to practice each file
1. Read only the `PROBLEM` section — restate it in your own words out loud.
2. Sketch your approach before scrolling further — brute force first, then think about what's slow about it.
3. Compare against `SIMPLE APPROACH` / `OPTIMIZED APPROACH` in the comments.
4. Cover the code, re-implement the optimized method from scratch.
5. Run `main()` and check output against the `// Expected:` comments.
6. Say the `COMPLEXITY` and 2-3 `INTERVIEW FOLLOW-UPS` out loud before moving to the next file.

See `00_MASTER_LIST.md` for the full file inventory and `CHEATSHEET.md` for the day-before-interview skim.
