import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class Visualizer extends JPanel {

    int[] data = new int[50];

    public Visualizer() {
        generateData();
        new Thread(this::bubbleSort).start();
    }

    void generateData() {
        Random rand = new Random();
        for (int i = 0; i < data.length; i++) {
            data[i] = rand.nextInt(300) + 20;
        }
    }

    void bubbleSort() {
        try {
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
        } catch (InterruptedException ignored) {}
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

        frame.add(panel);
        frame.setSize(800, 400);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
