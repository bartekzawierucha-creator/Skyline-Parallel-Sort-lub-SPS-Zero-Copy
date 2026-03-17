/**
 * PROJECT: Skyline Parallel Sort (SPS) - Zero-Copy Edition
 * AUTHOR: bartekzawierucha-creator
 * LICENSE: MIT
 * * DESCRIPTION: 
 * A high-performance, non-comparative O(n) sorting algorithm.
 * Optimized for multi-core systems using OpenMP and SIMD-friendly 
 * Vertical Horizon counting engine.
 */

#include <iostream>
#include <vector>
#include <algorithm>
#include <omp.h>
#include <chrono>
#include <ctime>

// --- VERTICAL HORIZON ENGINE (Core Counting Processor) ---
void vertical_horizon_engine(int* data, int size, int min_lvl, int max_lvl) {
    if (size < 2) return;
    int range = max_lvl - min_lvl + 1;
    if (range <= 0) return;

    // Local histogram for cache efficiency
    std::vector<int> counts(range, 0);
    
    // Scan phase (SIMD-auto-vectorizable)
    for (int i = 0; i < size; ++i) {
        counts[data[i] - min_lvl]++;
    }
    
    // Reconstruction phase
    int pos = 0;
    for (int i = 0; i < range; ++i) {
        int c = counts[i];
        while (c-- > 0) data[pos++] = i + min_lvl;
    }
}

// --- SKYLINE ZERO-COPY ALGORITHM ---
void skyline_sort(std::vector<int>& data, int num_threads) {
    size_t n = data.size();
    if (n < 2) return;

    // 1. Initial Range Scan
    auto res = std::minmax_element(data.begin(), data.end());
    int min_val = *res.first;
    int max_val = *res.second;
    if (min_val == max_val) return;

    long long total_range = (long long)max_val - min_val + 1;
    std::vector<int> bucket_sizes(num_threads, 0);
    std::vector<int> bucket_offsets(num_threads + 1, 0);

    // 2. Histogram Generation
    for (int x : data) {
        int idx = (int)((long long)(x - min_val) * num_threads / total_range);
        if (idx >= num_threads) idx = num_threads - 1;
        bucket_sizes[idx]++;
    }

    // 3. Offset Calculation (Zero-Copy Preparation)
    for (int i = 0; i < num_threads; ++i) {
        bucket_offsets[i+1] = bucket_offsets[i] + bucket_sizes[i];
    }

    // 4. Distribution Phase (Temporary Buffer)
    std::vector<int> temp(n);
    std::vector<int> current_pos = bucket_offsets;
    for (int x : data) {
        int idx = (int)((long long)(x - min_val) * num_threads / total_range);
        if (idx >= num_threads) idx = num_threads - 1;
        temp[current_pos[idx]++] = x;
    }

    // 5. Parallel Processing Phase
    #pragma omp parallel for num_threads(num_threads) schedule(dynamic)
    for (int i = 0; i < num_threads; ++i) {
        int start = bucket_offsets[i];
        int size = bucket_sizes[i];
        if (size > 0) {
            auto b_res = std::minmax_element(temp.data() + start, temp.data() + start + size);
            vertical_horizon_engine(temp.data() + start, size, *b_res.first, *b_res.second);
        }
    }

    // 6. Swap and Release Memory
    data.swap(temp); 
}

int main() {
    // Optimized for OnlineGDB/Cloud environments (Limit: ~1GB RAM)
    const size_t SIZE = 60000000; 
    const int MAX_VAL = 1000000;
    const int THREADS = 4;

    std::cout << "--- SKYLINE PARALLEL SORT (SPS) ---" << std::endl;
    std::cout << "Preparing " << SIZE << " elements..." << std::endl;

    std::vector<int> data(SIZE);
    srand(static_cast<unsigned int>(time(0)));
    for(size_t i = 0; i < SIZE; ++i) data[i] = rand() % MAX_VAL;

    std::cout << "Sorting started (THREADS: " << THREADS << ")..." << std::endl;
    auto start = std::chrono::high_resolution_clock::now();
    
    skyline_sort(data, THREADS);
    
    auto end = std::chrono::high_resolution_clock::now();
    std::chrono::duration<double> diff = end - start;

    std::cout << "Sorting finished in: " << diff.count() << " seconds" << std::endl;

    // --- FULL VALIDATION ---
    std::cout << "Verifying sequence integrity..." << std::endl;
    bool sorted = true;
    for (size_t i = 1; i < SIZE; ++i) {
        if (data[i] < data[i-1]) {
            sorted = false;
            break;
        }
    }

    std::cout << "Verification: " << (sorted ? "PASSED (100%)" : "FAILED") << std::endl;

    return 0;
}
