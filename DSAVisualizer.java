import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DSAVisualizer extends JFrame {

    private static final int N = 30;
    private static final int BAR_WIDTH = 25;
    private int visualDelay = 50; 
    private int REAL_OP_NS = 10; 

    // theme
    private final Color BG_DARK = new Color(25, 25, 30);
    private final Color SIDEBAR_BG = new Color(35, 35, 40);
    private final Color ACCENT_BLUE = new Color(80, 150, 240);
    private final Color BAR_DEFAULT = new Color(60, 120, 200);
    private final Color BAR_COMPARE = new Color(255, 60, 80);
    private final Color BAR_SORTED = new Color(80, 220, 120);
    private final Color BAR_PIVOT = new Color(255, 180, 50);

    private Bar[] bars, originalBars;
    private long comparisons = 0;
    private long moves = 0;

    private JComboBox<String> algoBox;
    private JButton generateBtn, startBtn, pauseBtn, prevBtn, nextBtn;
    private JTextArea info;
    private DrawPanel panel;

    private long visualStart, visualEnd;

    // snapshot debugger
    private boolean isPaused = false;
    private boolean stepRequested = false; 
    private List<Snapshot> history = new ArrayList<>();
    private int currentSnapshotIndex = -1;
    private final Object pauseLock = new Object();

    public DSAVisualizer() {
        setTitle("Fluid DSA Sorting Visualizer");
        setSize(1150, 750);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        bars = new Bar[N];
        originalBars = new Bar[N];
        generateBars();

        initUI();
        showTheory((String) algoBox.getSelectedItem());
    }

    // ui
    private void initUI() {
        JPanel mainContent = new JPanel(new BorderLayout());
        mainContent.setBackground(BG_DARK);

        // header
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 15));
        topBar.setBackground(SIDEBAR_BG);
        
        algoBox = new JComboBox<>(new String[]{
                "Bubble Sort", "Selection Sort", "Insertion Sort",
                "Merge Sort", "Quick Sort", "Heap Sort", "Shell Sort"
        });
        
        generateBtn = createStyledButton("Generate", new Color(100, 100, 110));
        startBtn = createStyledButton("Start Sort", ACCENT_BLUE);
        pauseBtn = createStyledButton("Pause", new Color(150, 150, 160));
        prevBtn = createStyledButton("← Previous", new Color(70, 70, 80));
        nextBtn = createStyledButton("Next →", new Color(70, 70, 80));

        prevBtn.setVisible(false);
        nextBtn.setVisible(false);

        topBar.add(algoBox);
        topBar.add(generateBtn);
        topBar.add(startBtn);
        topBar.add(pauseBtn);
        topBar.add(prevBtn);
        topBar.add(nextBtn);

        panel = new DrawPanel();
        
        info = new JTextArea(25, 32);
        info.setEditable(false);
        info.setBackground(SIDEBAR_BG);
        info.setForeground(Color.WHITE);
        info.setMargin(new Insets(15, 15, 15, 15));
        info.setFont(new Font("Monospaced", Font.PLAIN, 12));
        info.setLineWrap(true);
        info.setWrapStyleWord(true);
        JScrollPane scroll = new JScrollPane(info);
        scroll.setBorder(null);

        mainContent.add(topBar, BorderLayout.NORTH);
        mainContent.add(panel, BorderLayout.CENTER);
        mainContent.add(scroll, BorderLayout.EAST);

        add(mainContent);

        generateBtn.addActionListener(e -> {
            generateBars();
            resetStats();
            showTheory((String) algoBox.getSelectedItem());
            panel.repaint();
        });

        algoBox.addActionListener(e -> {
            for (int i = 0; i < N; i++) {
                bars[i].value = originalBars[i].value;
                bars[i].color = BAR_DEFAULT;
            }
            resetStats();
            showTheory((String) algoBox.getSelectedItem());
            panel.repaint();
        });

        startBtn.addActionListener(e -> new Thread(this::startSort).start());

        pauseBtn.addActionListener(e -> {
            synchronized (pauseLock) {
                isPaused = !isPaused;
                pauseBtn.setText(isPaused ? "Resume" : "Pause");
                prevBtn.setVisible(isPaused);
                nextBtn.setVisible(isPaused);
                if (!isPaused) pauseLock.notifyAll();
            }
        });

        prevBtn.addActionListener(e -> {
            if (currentSnapshotIndex > 0) {
                currentSnapshotIndex--;
                loadSnapshot(history.get(currentSnapshotIndex));
            }
        });

        nextBtn.addActionListener(e -> {
            synchronized (pauseLock) {
                if (currentSnapshotIndex < history.size() - 1) {
                    currentSnapshotIndex++;
                    loadSnapshot(history.get(currentSnapshotIndex));
                } else {
                    stepRequested = true;
                    pauseLock.notifyAll();
                }
            }
        });
    }

    private JButton createStyledButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(10, 20, 10, 20));
        return btn;
    }

    /* snapshot system */
    class Snapshot {
        int[] values;
        Color[] colors;
        long comparisons, moves;

        Snapshot(Bar[] currentBars, long comp, long mov) {
            this.values = new int[currentBars.length];
            this.colors = new Color[currentBars.length];
            for (int i = 0; i < currentBars.length; i++) {
                this.values[i] = currentBars[i].value;
                this.colors[i] = currentBars[i].color;
            }
            this.comparisons = comp;
            this.moves = mov;
        }
    }

    private void saveSnapshot() {
        history.add(new Snapshot(bars, comparisons, moves));
        currentSnapshotIndex = history.size() - 1;
    }

    private void loadSnapshot(Snapshot s) {
        for (int i = 0; i < bars.length; i++) {
            bars[i].value = s.values[i];
            bars[i].color = s.colors[i];
        }
        this.comparisons = s.comparisons;
        this.moves = s.moves;
        panel.repaint();
    }

    private void emulatorWait() throws InterruptedException {
        saveSnapshot();
        panel.repaint();
        synchronized (pauseLock) {
            while (isPaused && !stepRequested) {
                pauseLock.wait();
            }
            if (!isPaused && currentSnapshotIndex != history.size() - 1) {
                currentSnapshotIndex = history.size() - 1;
                loadSnapshot(history.get(currentSnapshotIndex));
            }
            stepRequested = false; 
        }
        Thread.sleep(visualDelay);
    }

    class Bar {
        int value;
        Color color;
        Bar(int value) { this.value = value; this.color = BAR_DEFAULT; }
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
        history.clear();
        currentSnapshotIndex = -1;
        isPaused = false;
        stepRequested = false;
        pauseBtn.setText("Pause");
        prevBtn.setVisible(false);
        nextBtn.setVisible(false);
        for (Bar b : bars) b.color = BAR_DEFAULT;
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
        for (Bar b : bars) b.color = BAR_SORTED;
        panel.repaint();
        visualEnd = System.currentTimeMillis();
        showTimeStats();
    }

    /* sorting */
    private void bubbleSort() {
        try {
            for (int i = 0; i < N; i++) {
                for (int j = 0; j < N - i - 1; j++) {
                    highlightCompare(j, j + 1);
                    comparisons++;
                    if (bars[j].value > bars[j + 1].value) swap(j, j + 1);
                    resetColor(j, j + 1);
                }
                bars[N - i - 1].color = BAR_SORTED;
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
                bars[i].color = BAR_SORTED;
            }
            bars[N - 1].color = BAR_SORTED;
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
                    emulatorWait(); 
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
                emulatorWait(); 
                resetColor(i - 1, j - 1);
            }
            while (i <= m) temp[k++] = bars[i++].value;
            while (j <= r) temp[k++] = bars[j++].value;
            for (i = l; i <= r; i++) {
                bars[i].value = temp[i - l];
                emulatorWait(); 
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
        pivot.color = BAR_PIVOT; 
        int i = low - 1;
        try {
            for (int j = low; j < high; j++) {
                highlightCompare(j, high);
                comparisons++;
                if (bars[j].value < pivot.value) swap(++i, j);
                resetColor(j, high); 
            }
            swap(i + 1, high);
            pivot.color = BAR_DEFAULT; 
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
                bars[i].color = BAR_SORTED;
                heapify(i, 0);
            }
            bars[0].color = BAR_SORTED;
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
                        emulatorWait(); 
                        resetColor(j - gap, j);
                        j -= gap;
                    }
                    bars[j] = temp;
                }
            }
            for (Bar b : bars) b.color = BAR_SORTED;
            panel.repaint();
        } catch (InterruptedException ignored) {}
    }

    private void swap(int i, int j) throws InterruptedException {
        Bar temp = bars[i];
        bars[i] = bars[j];
        bars[j] = temp;
        moves++;
        emulatorWait(); 
    }

    private void highlightCompare(int i, int j) {
        if (i >= 0 && i < bars.length) bars[i].color = BAR_COMPARE;
        if (j >= 0 && j < bars.length && bars[j].color != BAR_PIVOT) bars[j].color = BAR_COMPARE;
        panel.repaint();
    }

    private void resetColor(int i, int j) {
        if (i >= 0 && i < bars.length && bars[i].color != BAR_PIVOT) bars[i].color = BAR_DEFAULT;
        if (j >= 0 && j < bars.length && bars[j].color != BAR_PIVOT) bars[j].color = BAR_DEFAULT;
        panel.repaint();
    }

    /* theory page (vibe code) */
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

        info.append("General Equation: " + generalEquation + "\n");
        info.append("Pattern Equation: " + evaluatePatternEquation(algo) + "\n");

        info.append("\nTIME SCALE:\n1 operation ≈ " + REAL_OP_NS + " ns\n" +
                "Visualizer delay = " + visualDelay + " ms\n");

        info.append("\nCOLOR LEGEND:\n" +
                "Blue   = Unsorted\n" +
                "Red    = Comparing\n" +
                "Yellow = Pivot (Quick Sort)\n" +
                "Green  = Sorted\n");
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
            case "Quick Sort":
                return "≈ " + (int)(N * Math.log(N)/Math.log(2)) + " steps";
            case "Heap Sort":
                return "≈ " + (int)(2*N*Math.log(N)/Math.log(2)) + " operations";
            default: return "N/A";
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

    class DrawPanel extends JPanel {
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            int startX = (getWidth() - (bars.length * BAR_WIDTH)) / 2;
            for (int i = 0; i < bars.length; i++) {
                Bar b = bars[i];
                int x = startX + (i * BAR_WIDTH);
                int y = getHeight() - b.value - 20;
                
                g2.setColor(b.color);
                g2.fillRoundRect(x, y, BAR_WIDTH - 5, b.value, 10, 10);
                g2.setColor(new Color(255,255,255, 30));
                g2.drawRoundRect(x, y, BAR_WIDTH - 5, b.value, 10, 10);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new DSAVisualizer().setVisible(true));
    }
}
