package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

public class FaceIdPanel extends JPanel {
    private double progress = 0.0; // 0.0 (Scanning) to 1.0 (Success Circle)
    private boolean isSuccess = false;
    private Timer animTimer;
    private double scanY = 0;
    private boolean scanDown = true;
    private Runnable onComplete;
    private java.awt.image.BufferedImage cameraImage;

    public FaceIdPanel() {
        setBackground(new Color(20, 20, 20)); // Fondo oscuro estilo Apple
        
        animTimer = new Timer(16, e -> {
            if (isSuccess) {
                if (progress < 1.0) {
                    progress += 0.04; // Velocidad de la transición
                    if (progress >= 1.0) {
                        progress = 1.0;
                        animTimer.stop();
                        // Esperar un poquito antes de cerrar para que se vea el check
                        Timer delay = new Timer(500, evt -> {
                            if (onComplete != null) onComplete.run();
                        });
                        delay.setRepeats(false);
                        delay.start();
                    }
                }
            } else {
                // Animación del escáner
                if (scanDown) {
                    scanY += 0.03;
                    if (scanY >= 1.0) scanDown = false;
                } else {
                    scanY -= 0.03;
                    if (scanY <= 0.0) scanDown = true;
                }
            }
            repaint();
        });
        animTimer.start();
    }

    public void setSuccess(Runnable onComplete) {
        this.isSuccess = true;
        this.onComplete = onComplete;
    }

    public void updateImage(java.awt.image.BufferedImage img) {
        this.cameraImage = img;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        
        int w = getWidth();
        int h = getHeight();

        // Dibujar la imagen de la cámara si existe
        if (cameraImage != null) {
            // Escalar la imagen para que llene el panel manteniendo la proporción
            double scaleX = (double) w / cameraImage.getWidth();
            double scaleY = (double) h / cameraImage.getHeight();
            double scale = Math.max(scaleX, scaleY);
            
            int imgW = (int) (cameraImage.getWidth() * scale);
            int imgH = (int) (cameraImage.getHeight() * scale);
            int imgX = (w - imgW) / 2;
            int imgY = (h - imgH) / 2;
            
            // Dibujar la imagen espejada (como en WebcamPanel.setMirrored(true))
            g2.drawImage(cameraImage, imgX + imgW, imgY, -imgW, imgH, null);
            
            // Capa oscura encima para que resalte la animación
            g2.setColor(new Color(0, 0, 0, 150));
            g2.fillRect(0, 0, w, h);
        }

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        int size = Math.min(w, h) - 100;
        if (size < 100) size = 100;
        
        int cx = w / 2;
        int cy = h / 2;
        
        double H = size / 2.0;
        double R = 30 + (H - 30) * progress; // Radio de las esquinas
        double L = 60 + (H - 60) * progress; // Longitud de los lados visibles
        
        // Dibujar el icono de la cara mientras no sea éxito total
        if (progress < 1.0) {
            float faceAlpha = (float)(1.0 - progress);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, faceAlpha));
            drawFaceIcon(g2, cx, cy, (int)(size * 0.5));
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
            
            // Dibujar el láser de escaneo si aún está escaneando
            if (progress == 0) {
                int laserY = (int)(cy - H + 20 + (2*H - 40) * scanY);
                GradientPaint gp = new GradientPaint((float)(cx - H), laserY, new Color(76, 217, 100, 0), 
                                                     cx, laserY, new Color(76, 217, 100, 200), true);
                g2.setPaint(gp);
                g2.setStroke(new BasicStroke(3));
                g2.drawLine((int)(cx - H + 20), laserY, (int)(cx + H - 20), laserY);
            }
        }
        
        // Configurar pincel para el marco / círculo
        g2.setColor(Color.WHITE);
        if (progress > 0.5) {
            // Cambiar a verde progresivamente
            double colorProgress = (progress - 0.5) * 2.0;
            int r = (int)(255 - (255 - 76) * colorProgress);
            int gr = (int)(255 - (255 - 217) * colorProgress);
            int b = (int)(255 - (255 - 100) * colorProgress);
            g2.setColor(new Color(r, gr, b));
        }
        g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

        // Esquina Superior Izquierda
        Path2D.Double tl = new Path2D.Double();
        tl.moveTo(cx - H, cy - H + L);
        tl.lineTo(cx - H, cy - H + R);
        tl.append(new Arc2D.Double(cx - H, cy - H, 2*R, 2*R, 180, -90, Arc2D.OPEN), true);
        tl.lineTo(cx - H + L, cy - H);
        g2.draw(tl);

        // Esquina Superior Derecha
        Path2D.Double tr = new Path2D.Double();
        tr.moveTo(cx + H - L, cy - H);
        tr.lineTo(cx + H - R, cy - H);
        tr.append(new Arc2D.Double(cx + H - 2*R, cy - H, 2*R, 2*R, 90, -90, Arc2D.OPEN), true);
        tr.lineTo(cx + H, cy - H + L);
        g2.draw(tr);

        // Esquina Inferior Derecha
        Path2D.Double br = new Path2D.Double();
        br.moveTo(cx + H, cy + H - L);
        br.lineTo(cx + H, cy + H - R);
        br.append(new Arc2D.Double(cx + H - 2*R, cy + H - 2*R, 2*R, 2*R, 0, -90, Arc2D.OPEN), true);
        br.lineTo(cx + H - L, cy + H);
        g2.draw(br);

        // Esquina Inferior Izquierda
        Path2D.Double bl = new Path2D.Double();
        bl.moveTo(cx - H + L, cy + H);
        bl.lineTo(cx - H + R, cy + H);
        bl.append(new Arc2D.Double(cx - H, cy + H - 2*R, 2*R, 2*R, 270, -90, Arc2D.OPEN), true);
        bl.lineTo(cx - H, cy + H - L);
        g2.draw(bl);
        
        // Dibujar el check de éxito
        if (progress > 0.5) {
            double checkProgress = (progress - 0.5) * 2.0;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, (float)checkProgress));
            g2.setColor(new Color(76, 217, 100)); // Verde iOS
            
            Path2D.Double check = new Path2D.Double();
            check.moveTo(cx - size*0.15, cy + size*0.02);
            check.lineTo(cx - size*0.03, cy + size*0.15);
            check.lineTo(cx + size*0.2, cy - size*0.15);
            g2.draw(check);
        }
        
        g2.dispose();
    }
    
    private void drawFaceIcon(Graphics2D g2, int cx, int cy, int size) {
        g2.setColor(new Color(150, 150, 150));
        g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int r = size / 2;
        
        // Ojos
        g2.drawLine(cx - r/3, cy - r/4, cx - r/3, cy - r/4 + r/5);
        g2.drawLine(cx + r/3, cy - r/4, cx + r/3, cy - r/4 + r/5);
        
        // Nariz
        Path2D.Double nose = new Path2D.Double();
        nose.moveTo(cx, cy - r/10);
        nose.lineTo(cx, cy + r/5);
        nose.lineTo(cx + r/6, cy + r/5);
        g2.draw(nose);
        
        // Sonrisa
        Path2D.Double smile = new Path2D.Double();
        smile.moveTo(cx - r/3, cy + r/2);
        smile.curveTo(cx - r/6, cy + r/1.5, cx + r/6, cy + r/1.5, cx + r/3, cy + r/2);
        g2.draw(smile);
    }
}
