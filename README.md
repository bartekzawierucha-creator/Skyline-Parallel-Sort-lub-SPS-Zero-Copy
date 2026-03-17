# Skyline Parallel Sort (SPS) - Zero-Copy Edition 🚀

**Skyline Parallel Sort** is a high-performance, non-comparative sorting algorithm designed for Big Data processing on multi-core systems. By leveraging a "Zero-Copy" memory strategy and a vectorized counting engine, it achieves near-hardware-limit speeds for integer datasets.

## 📈 Key Benchmarks (OnlineGDB Environment)
| Data Size | Time | Status |
| :--- | :--- | :--- |
| **10 Million** | 0.18s | ✅ Verified |
| **60 Million** | 0.92s | ✅ Verified |
| **100 Million** | 8.38s | ✅ Verified |

## ✨ Features
- **O(n) Complexity:** Linear time performance regardless of data distribution.
- **Zero-Copy Memory Strategy:** Optimized to work within strict RAM limits (e.g., 1GB environments).
- **Multi-threaded:** Powered by OpenMP for maximum CPU utilization.
- **Branchless Engine:** Uses the "Vertical Horizon" counting method to minimize CPU branch mispredictions.

## 🛠️ How to Run
To compile with full optimizations and OpenMP support:
```bash
g++ -O3 -march=native -fopenmp main.cpp -o skyline_sort
./skyline_sort
