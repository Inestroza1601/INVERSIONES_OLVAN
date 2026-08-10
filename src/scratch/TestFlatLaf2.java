package scratch;
import gui.PanelInventario;
import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;

public class TestFlatLaf2 {
    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel(new FlatLightLaf());
        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setVisible(true);
        SwingWorker<JPanel, Void> worker = new SwingWorker<>() {
            protected JPanel doInBackground() {
                return new PanelInventario();
            }
            protected void done() {
                try {
                    frame.add(get());
                    frame.revalidate();
                    frame.repaint();
                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        };
        worker.execute();
        Thread.sleep(10000);
        System.exit(0);
    }
}
