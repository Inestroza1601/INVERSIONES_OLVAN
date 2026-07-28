package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class DialogoVisorImagen extends JDialog {

    private JLabel lblImagen;

    public DialogoVisorImagen(Frame parent, String titulo, String base64) {
        super(parent, titulo, true);
        iniciarDiseno(base64);
    }

    public DialogoVisorImagen(JDialog parent, String titulo, String base64) {
        super(parent, titulo, true);
        iniciarDiseno(base64);
    }

    private void iniciarDiseno(String base64) {
        this.setSize(800, 600);
        this.setLocationRelativeTo(getParent());
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(Color.BLACK);

        lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);

        ImageIcon icono = obtenerImagenDesdeBase64(base64);
        if (icono != null) {
            // Escalar para que quepa en la ventana sin deformarse
            Image img = icono.getImage();

            // Calculo de aspecto
            int imgW = img.getWidth(null);
            int imgH = img.getHeight(null);
            if (imgW > 0 && imgH > 0) {
                double scale = Math.min((double) 780 / imgW, (double) 560 / imgH);
                int scaledW = (int) (imgW * scale);
                int scaledH = (int) (imgH * scale);
                lblImagen.setIcon(new ImageIcon(img.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH)));
            } else {
                lblImagen.setIcon(icono);
            }
        } else {
            lblImagen.setText("Imagen no disponible");
            lblImagen.setForeground(Color.WHITE);
        }

        // Click para cerrar
        lblImagen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
        });

        this.add(new JScrollPane(lblImagen), BorderLayout.CENTER);

        // Instrucción
        JLabel lblCerrar = new JLabel("Haz clic en la imagen o presiona ESC para cerrar");
        lblCerrar.setForeground(Color.LIGHT_GRAY);
        lblCerrar.setHorizontalAlignment(SwingConstants.CENTER);
        lblCerrar.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        this.add(lblCerrar, BorderLayout.SOUTH);

        // Atajo teclado ESC
        this.getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private ImageIcon obtenerImagenDesdeBase64(String base64) {
        if (base64 == null || base64.isEmpty())
            return null;
        try {
            if (base64.contains(",")) {
                base64 = base64.split(",")[1];
            }
            byte[] bytes = java.util.Base64.getDecoder().decode(base64);
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
            Image img = javax.imageio.ImageIO.read(bais);
            if (img != null) {
                return new ImageIcon(img);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
