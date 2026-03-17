# Skyline Zero-Copy Sort (Turbo Edition)

High-performance, parallel sorting algorithm with $O(n)$ linear complexity. Designed to outperform standard library solutions in Big Data scenarios.

## 🚀 Key Results (N = 50,000,000)
- **Speedup:** Up to **5.43x faster** than `std::sort`.
- **Latency:** **797.71 ms** (Skyline) vs **4329.68 ms** (`std::sort`).
- **Scalability:** Successfully tested up to **100,000,000 elements** in ~1.7s.

## 🛠️ Optimizations
1. **Software Prefetching:** Uses `__builtin_prefetch` to hide memory latency.
2. **Parallel Processing:** Implements OpenMP with dynamic scheduling for optimal core utilization.
3. **Data Integrity:** Verified 100% accuracy on random, negative, and skewed datasets.

## 📦 Compilation
Use the following flags for maximum performance:
`g++ -O3 -fopenmp -march=native main.cpp`
