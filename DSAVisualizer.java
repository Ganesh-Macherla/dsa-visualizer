import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class DSAVisualizer extends JFrame {

    private static final int N = 30;
    private static final int BAR_WIDTH = 20;
    private int visualDelay = 50; // visualization delay in ms
    private int REAL_OP_NS = 10; // approximate real CPU operation time in ns

    private Bar[] bars, originalBars;
    private long comparisons = 0;
    private long moves = 0;

    private JComboBox<String> algoBox;
    private JButton generateBtn, startBtn;
    private JTextArea info;
    private DrawPanel panel;

    private long visualStart, visualEnd;

    public DSAVisualizer() {
        setTitle("DSA Sorting Visualizer");
        setSize(1000, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        bars = new Bar[N];
        originalBars = new Bar[N];
        generateBars();

        algoBox = new JComboBox<>(new String[]{
                "Bubble Sort", "Selection Sort", "Insertion Sort",
                "Merge Sort", "Quick Sort", "Heap Sort", "Shell Sort"
        });

        generateBtn = new JButton("Generate");
        startBtn = new JButton("Start");

        JPanel top = new JPanel();
        top.add(new JLabel("Algorithm: "));
        top.add(algoBox);
        top.add(generateBtn);
        top.add(startBtn);

        panel = new DrawPanel();
        info = new JTextArea(25, 30);
        info.setEditable(false);
        info.setLineWrap(true);
        info.setWrapStyleWord(true);

        add(top, BorderLayout.NORTH);
        add(panel, BorderLayout.CENTER);
        add(new JScrollPane(info), BorderLayout.EAST);

        // Initial theory and pattern display
        showTheory((String) algoBox.getSelectedItem());

        generateBtn.addActionListener(e -> {
            generateBars();
            resetStats();
            showTheory((String) algoBox.getSelectedItem());
            panel.repaint();
        });

        algoBox.addActionListener(e -> {
            // Reset bars to original pattern
            for (int i = 0; i < N; i++) {
                bars[i].value = originalBars[i].value;
                bars[i].color = Color.BLUE;
            }
            resetStats();
            showTheory((String) algoBox.getSelectedItem());
            panel.repaint();
        });

        startBtn.addActionListener(e -> new Thread(this::startSort).start());
    }

    class Bar {
        int value;
        Color color;

        Bar(int value) {
            this.value = value;
            this.color = Color.BLUE;
        }
    }

    private void generateBars() {
        Random r = new Random();
        for (int i = 0; i < N; i++) {
            int val = r.nextInt(400) + 50;
            bars[i] = new Bar(val);
            originalBars[i] = new Bar(val);
        }
    }

    private void resetStats() {
        comparisons = moves = 0;
        for (Bar b : bars) b.color = Color.BLUE;
    }

    private void startSort() {
        resetStats();
        visualStart = System.currentTimeMillis();

        String algo = (String) algoBox.getSelectedItem();

        switch (algo) {
            case "Bubble Sort": bubbleSort(); break;
            case "Selection Sort": selectionSort(); break;
            case "Insertion Sort": insertionSort(); break;
            case "Merge Sort": mergeSort(0, N - 1); break;
            case "Quick Sort": quickSort(0, N - 1); break;
            case "Heap Sort": heapSort(); break;
            case "Shell Sort": shellSort(); break;
        }

        for (Bar b : bars) b.color = Color.GREEN;
        panel.repaint();

        visualEnd = System.currentTimeMillis();
        showTimeStats();
    }

    /* ======================= SORTS ======================= */
    private void bubbleSort() {
        try {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N - i - 1; j++) {
                    highlightCompare(j, j + 1);
                    comparisons++;
                    if (bars[j].value > bars[j + 1].value) swap(j, j + 1);
                    resetColor(j, j + 1);
                }
                bars[N - i - 1].color = Color.GREEN;
            }
        } catch (InterruptedException ignored) {}
    }

    private void selectionSort() {
        try {
            for (int i = 0; i < N - 1; i++) {
                int min = i;
                for (int j = i + 1; j < N; j++) {
                    highlightCompare(min, j);
                    comparisons++;
                    if (bars[j].value < bars[min].value) min = j;
                    resetColor(min, j);
                }
                swap(i, min);
                bars[i].color = Color.GREEN;
            }
            bars[N - 1].color = Color.GREEN;
        } catch (InterruptedException ignored) {}
    }

    private void insertionSort() {
        try {
            for (int i = 1; i < N; i++) {
                Bar key = bars[i];
                int j = i - 1;
                while (j >= 0 && bars[j].value > key.value) {
                    highlightCompare(j, j + 1);
                    comparisons++;
                    bars[j + 1] = bars[j];
                    moves++;
                    panel.repaint();
                    Thread.sleep(visualDelay);
                    resetColor(j, j + 1);
                    j--;
                }
                bars[j + 1] = key;
            }
        } catch (InterruptedException ignored) {}
    }

    private void mergeSort(int l, int r) {
        if (l < r) {
            int m = (l + r) / 2;
            mergeSort(l, m);
            mergeSort(m + 1, r);
            merge(l, m, r);
        }
    }

    private void merge(int l, int m, int r) {
        try {
            int[] temp = new int[r - l + 1];
            int i = l, j = m + 1, k = 0;
            while (i <= m && j <= r) {
                highlightCompare(i, j);
                comparisons++;
                if (bars[i].value <= bars[j].value) temp[k++] = bars[i++].value;
                else temp[k++] = bars[j++].value;
                panel.repaint();
                Thread.sleep(visualDelay);
                resetColor(i - 1, j - 1);
            }
            while (i <= m) temp[k++] = bars[i++].value;
            while (j <= r) temp[k++] = bars[j++].value;

            for (i = l; i <= r; i++) {
                bars[i].value = temp[i - l];
                panel.repaint();
                Thread.sleep(visualDelay);
            }
        } catch (InterruptedException ignored) {}
    }

    private void quickSort(int low, int high) {
        if (low < high) {
            int p = partition(low, high);
            quickSort(low, p - 1);
            quickSort(p + 1, high);
        }
    }

    private int partition(int low, int high) {
        Bar pivot = bars[high];
        pivot.color = Color.ORANGE; // Pivot is orange
        int i = low - 1;
        try {
            for (int j = low; j < high; j++) {
                highlightCompare(j, high);
                comparisons++;
                if (bars[j].value < pivot.value) swap(++i, j);
                resetColor(j, high); // keep pivot orange
            }
            swap(i + 1, high);
            pivot.color = Color.BLUE; // reset pivot color after placement
            panel.repaint();
        } catch (InterruptedException ignored) {}
        return i + 1;
    }

    private void heapSort() {
        try {
            int n = bars.length;
            for (int i = n / 2 - 1; i >= 0; i--) heapify(n, i);
            for (int i = n - 1; i > 0; i--) {
                swap(0, i);
                bars[i].color = Color.GREEN;
                heapify(i, 0);
            }
            bars[0].color = Color.GREEN;
        } catch (InterruptedException ignored) {}
    }

    private void heapify(int n, int i) throws InterruptedException {
        int largest = i;
        int l = 2 * i + 1;
        int r = 2 * i + 2;

        if (l < n) {
            highlightCompare(l, largest);
            comparisons++;
            if (bars[l].value > bars[largest].value) largest = l;
            resetColor(l, largest);
        }
        if (r < n) {
            highlightCompare(r, largest);
            comparisons++;
            if (bars[r].value > bars[largest].value) largest = r;
            resetColor(r, largest);
        }
        if (largest != i) {
            swap(i, largest);
            heapify(n, largest);
        }
    }

    private void shellSort() {
        try {
            for (int gap = N / 2; gap > 0; gap /= 2) {
                for (int i = gap; i < N; i++) {
                    Bar temp = bars[i];
                    int j = i;
                    while (j >= gap && bars[j - gap].value > temp.value) {
                        highlightCompare(j - gap, j);
                        comparisons++;
                        bars[j] = bars[j - gap];
                        moves++;
                        panel.repaint();
                        Thread.sleep(visualDelay);
                        resetColor(j - gap, j);
                        j -= gap;
                    }
                    bars[j] = temp;
                }
            }
            for (Bar b : bars) b.color = Color.GREEN;
            panel.repaint();
        } catch (InterruptedException ignored) {}
    }

    private void swap(int i, int j) throws InterruptedException {
        Bar temp = bars[i];
        bars[i] = bars[j];
        bars[j] = temp;
        moves++;
        panel.repaint();
        Thread.sleep(visualDelay);
    }

    private void highlightCompare(int i, int j) {
        if (i >= 0 && i < bars.length) bars[i].color = Color.RED;
        if (j >= 0 && j < bars.length && bars[j].color != Color.ORANGE) bars[j].color = Color.RED;
        panel.repaint();
    }

    private void resetColor(int i, int j) {
        if (i >= 0 && i < bars.length && bars[i].color != Color.ORANGE) bars[i].color = Color.BLUE;
        if (j >= 0 && j < bars.length && bars[j].color != Color.ORANGE) bars[j].color = Color.BLUE;
        panel.repaint();
    }

    /* ======================= THEORY + PATTERN EQUATION ======================= */
    private void showTheory(String algo) {
        info.setText("ALGORITHM: " + algo + "\n\nTIME COMPLEXITY:\n");

        String generalEquation = "";
        switch (algo) {
            case "Bubble Sort":
                info.append("Best: O(n)\nAverage: O(n²)\nWorst: O(n²)\n\n" +
                        "Idea: Repeatedly compares adjacent elements and swaps if out of order.\n");
                generalEquation = "Σ(n-i) = n(n-1)/2";
                break;
            case "Selection Sort":
                info.append("Best/Avg/Worst: O(n²)\n\n" +
                        "Idea: Selects the minimum element and places it at correct position.\n");
                generalEquation = "n(n-1)/2 comparisons";
                break;
            case "Insertion Sort":
                info.append("Best: O(n)\nAverage/Worst: O(n²)\n\n" +
                        "Idea: Builds sorted array one element at a time like playing cards.\n");
                generalEquation = "Σ(i) ≈ n²/2 comparisons";
                break;
            case "Merge Sort":
                info.append("All cases: O(n log n)\n\n" +
                        "Idea: Divide and conquer by recursively splitting and merging arrays.\n");
                generalEquation = "T(n) = 2T(n/2) + n";
                break;
            case "Quick Sort":
                info.append("Best/Average: O(n log n)\nWorst: O(n²)\n\n" +
                        "Idea: Partition array around a pivot recursively.\n");
                generalEquation = "T(n) = T(k) + T(n-k-1) + n";
                break;
            case "Heap Sort":
                info.append("All cases: O(n log n)\n\n" +
                        "Idea: Build a max heap and repeatedly move max to end.\n");
                generalEquation = "≈ 2(n log n) comparisons/swaps";
                break;
            case "Shell Sort":
                info.append("Best: O(n log n)?\nAverage/Worst: O(n^(3/2))\n\n" +
                        "Idea: Sorts elements far apart and reduces gap.\n");
                generalEquation = "Depends on gap sequence";
                break;
        }

        // Show general equation
        info.append("General Equation: " + generalEquation + "\n");

        // Pattern-specific equation
        String patternEquation = evaluatePatternEquation(algo);
        info.append("Pattern Equation for this array: " + patternEquation + "\n");

        // Time scale and legend
        info.append("\nTIME SCALE:\n1 operation ≈ " + REAL_OP_NS + " ns\n" +
                "Visualizer delay = " + visualDelay + " ms\n");

        info.append("\nColor Legend:\n" +
                "Blue = Unsorted\n" +
                "Red = Comparing\n" +
                "Orange = Pivot (Quick Sort)\n" +
                "Green = Sorted\n");
    }

    private String evaluatePatternEquation(String algo) {
        switch (algo) {
            case "Bubble Sort":
            case "Selection Sort":
                long val = (long) N * (N - 1) / 2;
                return "Σ(n-i) = " + val + " operations (for n=" + N + ")";
            case "Insertion Sort":
                long approx = (long) (N * N / 2.0);
                return "≈ " + approx + " operations (for n=" + N + ")";
            case "Merge Sort":
                return "T(n) = 2T(n/2) + n → ≈ " + (int)(N * Math.log(N)/Math.log(2)) + " steps";
            case "Quick Sort":
                return "T(n) ≈ n log n → ≈ " + (int)(N * Math.log(N)/Math.log(2)) + " steps";
            case "Heap Sort":
                return "≈ 2(n log n) → ≈ " + (int)(2*N*Math.log(N)/Math.log(2)) + " operations";
            case "Shell Sort":
                return "Depends on gap sequence, n=" + N;
            default:
                return "N/A";
        }
    }

    private void showTimeStats() {
        long visualTime = visualEnd - visualStart;
        double scaleFactor = (visualDelay * 1_000_000.0) / REAL_OP_NS;
        double realTimeMs = visualTime / scaleFactor;

        info.append("\n--- TIME ANALYSIS ---\n" +
                "Visual Time: " + visualTime + " ms\n" +
                "Estimated Real Time: " + String.format("%.6f", realTimeMs) + " ms\n" +
                "Scale Factor: " + (long) scaleFactor + "× slower\n\n" +
                "Operations:\nComparisons: " + comparisons + "\nMoves: " + moves + "\n");
    }

    /* ======================= DRAW PANEL ======================= */
    class DrawPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            for (int i = 0; i < bars.length; i++) {
                Bar b = bars[i];
                int x = i * BAR_WIDTH;
                int y = getHeight() - b.value;
                g.setColor(b.color);
                g.fillRect(x, y, BAR_WIDTH - 2, b.value);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DSAVisualizer().setVisible(true));
    }
}
