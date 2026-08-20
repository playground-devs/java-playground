# Master List — Senior Java Interview Prep Repo

All classes live under `src/main/java/com/playground/java/interview/<package>/`.
Full package name = `com.playground.java.interview.<package>`.

**Total: 179 classes across 29 packages.**

Priority legend: **P0** = must know, asked very frequently · **P1** = high priority, commonly asked for senior roles · **P2** = good to know / variations · **P3** = advanced / staff-lead level.

Start with `p0mustknow/` (below) before anything else — see `PRACTICE_PROGRESSION.md` for the full order.

---

## p0mustknow/ (41) — drill this package first, in any order
TwoSum, ThreeSum, MaxSubArrayKadane, ProductOfArrayExceptSelf, MergeIntervals, ValidParentheses, LongestSubstringWithoutRepeating, GroupAnagrams, TopKFrequentElements, FindSecondMostFrequentElement, ContainerWithMostWater, TrappingRainWater, MinimumWindowSubstring, BinarySearch, SearchInRotatedSortedArray, KthLargestElement, ReverseLinkedList, DetectCycleLinkedList, MergeTwoSortedLists, LRUCache, MinStack, NextGreaterElement, LevelOrderTraversal, BinaryTreeTraversals, MaxDepthOfBinaryTree, ValidateBST, LowestCommonAncestor, TopViewOfBinaryTree, BottomViewOfBinaryTree, LeftViewOfBinaryTree, RightViewOfBinaryTree, NumberOfIslands, CloneGraph, CourseScheduleTopoSort, Permutations, Subsets, GenerateParentheses, ClimbingStairs, CoinChange, LongestCommonSubsequence, WordBreak

*(all P0 — every one of these should be solvable from memory, explaining approach out loud, in under 15 minutes)*

## arrays/ (3, P1)
RotateArray, MoveZeroes, MergeSortedArrayInPlace

## strings/ (4, P1)
LongestPalindromicSubstring, ValidAnagram, StringCompression, ReverseWordsInString

## hashmap/ (4, P1)
SubarraySumEqualsK, LongestConsecutiveSequence, FindAllDuplicatesInArray, IsomorphicStrings

## twopointers/ (2, P1)
ThreeSumClosest, SortColorsDutchFlag

## slidingwindow/ (2, P1)
LongestSubstringKDistinct, FruitIntoBaskets

## prefixsum/ (2, P1)
RangeSumQueryImmutable, SubarraySumDivisibleByK

## sorting/ (4, P1)
MergeSortImplementation, QuickSortImplementation, MergeKSortedLists, KthSmallestInSortedMatrix

## binarysearch/ (4)
SearchInsertPosition (P1), FindPeakElement (P1), MedianOfTwoSortedArrays (P2), KokoEatingBananas (P2)

## recursion/
Not a separate package — recursion is the backbone of `backtracking/`, `dynamicprogramming/`, and the tree/graph packages (every recursive tree/graph/backtracking file explicitly discusses the recursion + call-stack cost). Treat those packages as your recursion practice too.

## linkedlist/ (5)
ReverseLinkedListII (P1), RemoveNthNodeFromEnd (P1), AddTwoNumbers (P1), LinkedListPalindromeCheck (P1), CopyListWithRandomPointer (P2)

## stack/ (3, P1)
EvaluateReversePolishNotation, LargestRectangleInHistogram, DailyTemperatures

## queue/ (1, P1)
ImplementQueueUsingStacksAndStackUsingQueues

## deque/ (1, P1)
SlidingWindowMaximum

## heap/ (2, P1)
KClosestPointsToOrigin, MedianFromDataStream

## trees/ (6)
DiameterOfBinaryTree (P1), PathSumII (P1), SerializeDeserializeBinaryTree (P1), ZigzagLevelOrderTraversal (P1), ConvertSortedArrayToBST (P1), VerticalOrderTraversal (P2)

## bst/ (4)
KthSmallestInBST (P1), BSTIterator (P2), RecoverBST (P2), PopulateNextRightPointersInEachNode (P2)

## graphs/ (9)
RottingOranges (P1), NumberOfProvincesUnionFind (P1), DijkstraShortestPath (P1), WordLadder (P2), AlienDictionary (P2), RedundantConnectionUnionFind (P2), MinimumSpanningTreeKruskal (P2), GraphCycleDetection (P2), BellmanFordShortestPath (P2)

## backtracking/ (7)
CombinationSum (P1), NQueens (P1), WordSearch (P1), CombinationSumII (P2), PalindromePartitioning (P2), SubsetsII (P2), SudokuSolver (P2)

## dynamicprogramming/ (8)
HouseRobber (P1), LongestIncreasingSubsequence (P1), ZeroOneKnapsack (P1), EditDistance (P1), UniquePaths (P1), PartitionEqualSubsetSum (P2), DecodeWays (P2), MaximalSquare (P2)

## intervals/ (2, P1)
InsertInterval, MeetingRoomsII

## matrix/ (4)
RotateImage (P1), SpiralMatrix (P1), SetMatrixZeroes (P1), TrappingRainWater2D (P3)

## bitmanipulation/ (4, P1)
SingleNumber, NumberOf1Bits, PowerOfTwo, CountingBits

## trie/ (2)
ImplementTrie (P1), WordSearchII (P2)

## mathpuzzles/ (5, P2)
BoyerMooreMajorityElement, NextPermutation, GcdLcmAndSieveOfEratosthenes, PowerFunctionFastExponentiation, ReservoirSampling

## greedy/ (2, P2)
GasStation, JumpGameOneAndTwo

## java8/ (19)
StreamsBasicsMapFilterReduce (P0), MapVsFlatMap (P0), GroupingByExample (P0), CollectorsToMapDuplicateKeys (P0), FindDuplicatesUsingStreams (P0), FrequencyCountUsingStreams (P0), SecondHighestLowestUsingStreams (P0), ListToMapConversion (P0), OptionalDeepDive (P0), CustomFunctionalInterface (P0), MethodReferencesAllFourKinds (P0), ComparableVsComparatorDemo (P0), ImmutableClassExample (P0), EqualsAndHashCodeContract (P0), PartitioningByExample (P1), SortingWithComparatorStreams (P1), CollectorsJoiningAndSummaryStatistics (P1), CustomCheckedUncheckedException (P1), GenericsBoundedTypesDemo (P1)

## concurrency/ (16)
ThreadSafeSingleton (P0), SynchronizedVsReentrantLock (P0), ProducerConsumerWaitNotify (P0), ProducerConsumerBlockingQueue (P0), AtomicVsSynchronizedCounter (P0), ExecutorServicePatterns (P0), CompletableFutureChaining (P0), DeadlockDemo (P0), RaceConditionDemo (P0), CustomBlockingQueue (P1), ReadWriteLockDemo (P1), ConcurrentHashMapVsSynchronizedMap (P1), CountDownLatchAndCyclicBarrierDemo (P1), SemaphoreConnectionPool (P1), ThreadLocalDemo (P1), ForkJoinTaskDemo (P2)

## cache/ (5)
LFUCache (P1), InMemoryTTLCache (P1), RateLimiterTokenBucket (P1), RateLimiterSlidingWindowCounter (P2), ConcurrentLRUCache (P2)
*(LRUCache itself lives in `p0mustknow/` — it's P0)*

## lld/ (8)
BookingReservationSystem (P0 — Marriott-relevant), ParkingLotSystem (P1), ElevatorSystem (P1), PaymentProcessingStrategy (P1), UrlShortener (P1), LogFileProcessor (P1), CoffeeVendingMachine (P2), InventoryManagementSystem (P2)

---

## Known gap / acknowledged limitation
During parallel batch generation, one agent briefly deleted and a follow-up recovery agent restored 12 files in `dynamicprogramming/`, `backtracking/`, and `graphs/` (HouseRobber, LongestIncreasingSubsequence, ZeroOneKnapsack, EditDistance, UniquePaths, CombinationSum, NQueens, WordSearch, RottingOranges, WordLadder, NumberOfProvincesUnionFind, DijkstraShortestPath). All 12 are confirmed present and the whole project compiles (`./gradlew compileJava` — BUILD SUCCESSFUL) as of this writing. Mentioned here only for transparency, not because anything is currently missing.
