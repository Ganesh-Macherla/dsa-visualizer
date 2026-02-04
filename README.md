# 📊 Sorting Visualizer

A professional-grade **Java Swing** application that visualizes classic sorting algorithms step-by-step. Designed as a learning and debugging tool for DSA students, this project combines a modern dark-themed UI with a powerful **Snapshot Debugger** that lets you rewind algorithm execution in real time.

## 🚀 Key Features

- **Snapshot Debugger**  
  Records every array state. Pause the sort and scrub through history using **← Previous** and **Next →**.

- **Fluid Animation Engine**  
  High-performance `Graphics2D` rendering with anti-aliasing and rounded bar geometry.

- **Live Operation Tracking**  
  Real-time comparison + move counters.

- **Theory Panel**  
  Displays time complexity, recurrence equations, evaluated formulas based on array size, and color legend.

- **Deep Time Analysis**  
  Estimates actual CPU execution time vs. visualized delay scale.

- **Thread-safe Control System**  
  Monitor-based pause/resume/step execution.

## 🧠 Supported Algorithms

| Algorithm | Best Case | Average Case | Worst Case |
| :--- | :--- | :--- | :--- |
| Bubble Sort | O(n) | O(n²) | O(n²) |
| Selection Sort | O(n²) | O(n²) | O(n²) |
| Insertion Sort | O(n) | O(n²) | O(n²) |
| Merge Sort | O(n log n) | O(n log n) | O(n log n) |
| Quick Sort | O(n log n) | O(n log n) | O(n²) |
| Heap Sort | O(n log n) | O(n log n) | O(n log n) |
| Shell Sort | depends on gap | ~O(n^(3/2)) | O(n²) |

## 🎮 How to Use

1. Select an algorithm
2. Generate a dataset
3. Start the sort
4. Pause anytime to step backward/forward
5. Resume from any snapshot

## ⚙️ Installation

```bash
javac DSAVisualizer.java
java DSAVisualizer
```

Requires Java 11+

## Output

[Watch the 1-min video demo here](videos/output.mp4)


