# 📊Sorting Visualizer

A professional-grade **Java Swing** application designed to visualize the step-by-step logic of classic sorting algorithms. This project combines a modern dark-themed UI with a robust **Snapshot Debugger**, allowing you to rewind and analyze data movements in real-time.

## 🚀 Key Features

- **Snapshot Debugger:** The visualizer records every array state. If you pause, you can "scrub" through history using **← Previous** and **Next →** buttons.
- **Fluid Animation:** Uses high-performance `Graphics2D` rendering with anti-aliasing and rounded bar geometry for smooth motion.
- **Deep Time Analysis:** Calculates the "Estimated Real Time" it would take a CPU to run the sort vs. the visual delay.
- **Mathematical Theory:** Displays general recurrence relations and specific pattern equations based on the current array size.



## 🛠 Tech Stack

- **Language:** Java 11+
- **Framework:** Java Swing / AWT
- **Logic:** Monitor-based concurrency for thread-safe pause/resume.

## 🧠 Supported Algorithms

| Algorithm | Best Case | Average Case | Worst Case |
| :--- | :--- | :--- | :--- |
| **Bubble Sort** | O(n) | O(n²) | O(n²) |
| **Selection Sort** | O(n²) | O(n²) | O(n²) |
| **Insertion Sort** | O(n) | O(n²) | O(n²) |
| **Merge Sort** | O(n log n) | O(n log n) | O(n log n) |
| **Quick Sort** | O(n log n) | O(n log n) | O(n²) |
| **Heap Sort** | O(n log n) | O(n log n) | O(n log n) |

## 🎮 How to Use

1. **Select Algorithm:** Choose a technique from the dropdown menu.
2. **Generate:** Click "Generate" to create a new random dataset.
3. **Start Sort:** Watch the fluid animation as it highlights comparisons (Red) and pivots (Yellow).
4. **Debug:**
   - Hit **Pause** at any time.
   - Click **← Previous** to go back to a previous comparison or move.
   - Click **Next →** to move forward through history or force the next step.
   - Hit **Resume** to continue the sort from your current point.

## ⚙️ Installation & Running

Ensure you have a Java Development Kit (JDK) installed.

```bash
# Compile the program
javac DSAVisualizer.java

# Run the application
java DSAVisualizer
