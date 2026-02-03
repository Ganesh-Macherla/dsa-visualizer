import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Visualizer extends JPanel {

    int[] data = new int[50];
    boolean sorting = false;
    String algorithm = "Bubble";

    public Visualizer() {
        generateData();
    }

    void generateData() {
        Random rand = new Random();
        for (int i = 0; i < data.length; i++) {
            data[i] = rand.nextInt(300) + 20;
        }
        repaint();
    }

    void sort() {
        if (sorting) return;
        sorting = true;

        new Thread(() -> {
            try {
                if (algorithm.equals("Bubble")) bubbleSort();
                if (algorithm.equals("Quick")) quickSort(0, data.length - 1);
            } catch (Exception ignored) {}

            sorting = false;
        }).start();
    }

    void bubbleSort() throws InterruptedException {
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data.length - i - 1; j++) {
                if (data[j] > data[j + 1]) {
                    int temp = data[j];
                    data[j] = data[j + 1];
                    data[j + 1] = temp;
                }
                repaint();
                Thread.sleep(20);
            }
        }
    }

    void quickSort(int low, int high) throws InterruptedException {
        if (low < high) {
            int pi = partition(low, high);
            quickSort(low, pi - 1);
            quickSort(pi + 1, high);
        }
    }

    int partition(int low, int high) throws InterruptedException {
        int pivot = data[high];
        int i = low - 1;

        for (int j = low; j < high; j++) {
            if (data[j] < pivot) {
                i++;
                int temp = data[i];
                data[i] = data[j];
                data[j] = temp;
                repaint();
                Thread.sleep(20);
            }
        }

        int temp = data[i + 1];
        data[i + 1] = data[high];
        data[high] = temp;
        repaint();
        Thread.sleep(20);

        return i + 1;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int width = getWidth() / data.length;

        for (int i = 0; i < data.length; i++) {
            g.setColor(Color.BLUE);
            g.fillRect(i * width, getHeight() - data[i], width, data[i]);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("DSA Visualizer");
        Visualizer panel = new Visualizer();

        JButton generateBtn = new JButton("Generate");
        JButton sortBtn = new JButton("Sort");

        String[] algos = {"Bubble", "Quick"};
        JComboBox<String> dropdown = new JComboBox<>(algos);

        dropdown.addActionListener(e ->
                panel.algorithm = (String) dropdown.getSelectedItem()
        );

        generateBtn.addActionListener(e -> {
            if (!panel.sorting) panel.generateData();
        });

        sortBtn.addActionListener(e -> panel.sort());

        JPanel controls = new JPanel();
        controls.add(generateBtn);
        controls.add(dropdown);
        controls.add(sortBtn);

        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.CENTER);
        frame.add(controls, BorderLayout.SOUTH);

        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
