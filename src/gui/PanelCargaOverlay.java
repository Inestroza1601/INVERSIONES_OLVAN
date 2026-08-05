package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;

public class PanelCargaOverlay extends JPanel {
    private Timer timer;
    private double angle = 0;
    private String mensaje;

    public PanelCargaOverlay(String mensaje) {
        this.mensaje = mensaje != null ? mensaje : "Cargando...";
        setOpaque(true);
        setBackground(new Color(232, 243, 236)); // Color fondo app (Verde Pastel Suave)

        timer = new Timer(15, e -> {
            angle += 5; // Velocidad de rotación
            if (angle >= 360) {
                angle -= 360;
            }
            repaint();
        });
    }

    public void setMensaje(String msg) {
        this.mensaje = msg;
        repaint();
    }

    public void iniciarAnimacion() {
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    public void detenerAnimacion() {
        if (timer.isRunning()) {
            timer.stop();
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2 - 30; // Un poco más arriba para centrar con el texto
        int size = 60; // Tamaño del spinner

        // Fondo del spinner (Círculo tenue)
        g2.setColor(new Color(180, 208, 192, 100));
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Ellipse2D.Double(centerX - size / 2.0, centerY - size / 2.0, size, size));

        // Arco animado (Verde Esmeralda Oscuro brillante)
        g2.setColor(new Color(6, 95, 70));
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        g2.draw(new Arc2D.Double(centerX - size / 2.0, centerY - size / 2.0, size, size, angle, 120, Arc2D.OPEN));

        // Texto
        g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
        g2.setColor(new Color(28, 59, 45)); // Texto oscuro vintage
        FontMetrics fm = g2.getFontMetrics();
        int textX = centerX - fm.stringWidth(mensaje) / 2;
        int textY = centerY + size / 2 + 35; // Debajo del spinner
        
        g2.drawString(mensaje, textX, textY);

        g2.dispose();
    }
}
