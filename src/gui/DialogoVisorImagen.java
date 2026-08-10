package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.ArrayList;

public class DialogoVisorImagen extends JDialog {

    private JLabel lblImagen;
    private List<String> listaImagenes;
    private int indiceActual;
    private JLabel lblStatus;
    private JPanel pnlThumbnails;

    public DialogoVisorImagen(Frame parent, String titulo, String base64) {
        super(parent, titulo, true);
        this.listaImagenes = new ArrayList<>();
        if (base64 != null) this.listaImagenes.add(base64);
        this.indiceActual = 0;
        iniciarDiseno();
    }
    
    public DialogoVisorImagen(JDialog parent, String titulo, String base64) {
        super(parent, titulo, true);
        this.listaImagenes = new ArrayList<>();
        if (base64 != null) this.listaImagenes.add(base64);
        this.indiceActual = 0;
        iniciarDiseno();
    }

    public DialogoVisorImagen(Frame parent, String titulo, List<String> imagenes, int indexInicial) {
        super(parent, titulo, true);
        this.listaImagenes = imagenes != null ? new ArrayList<>(imagenes) : new ArrayList<>();
        this.indiceActual = indexInicial;
        iniciarDiseno();
    }

    public DialogoVisorImagen(JDialog parent, String titulo, List<String> imagenes, int indexInicial) {
        super(parent, titulo, true);
        this.listaImagenes = imagenes != null ? new ArrayList<>(imagenes) : new ArrayList<>();
        this.indiceActual = indexInicial;
        iniciarDiseno();
    }

    private void iniciarDiseno() {
        this.setSize(800, 680);
        this.setLocationRelativeTo(getParent());
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(Color.BLACK);

        lblImagen = new JLabel();
        lblImagen.setHorizontalAlignment(SwingConstants.CENTER);

        this.add(new JScrollPane(lblImagen), BorderLayout.CENTER);
        
        // Panel inferior para controles y atajos
        JPanel pnlFooter = new JPanel(new BorderLayout(10, 5));
        pnlFooter.setBackground(Color.BLACK);
        pnlFooter.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Status de paginación / Atajo
        JLabel lblCerrar = new JLabel("Haz clic para cerrar (Esc) | Flechas, botones o miniaturas para navegar");
        lblCerrar.setForeground(Color.LIGHT_GRAY);
        lblCerrar.setHorizontalAlignment(SwingConstants.CENTER);
        pnlFooter.add(lblCerrar, BorderLayout.NORTH);

        if (listaImagenes.size() > 1) {
            // Panel de Miniaturas
            pnlThumbnails = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
            pnlThumbnails.setOpaque(false);
            pnlFooter.add(pnlThumbnails, BorderLayout.CENTER);

            // Controles de Navegación
            JPanel pnlNavegacion = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
            pnlNavegacion.setOpaque(false);

            JButton btnPrev = new JButton("Anterior", new IconoFlechaIzquierda());
            btnPrev.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnPrev.setBackground(new Color(60, 60, 60));
            btnPrev.setForeground(Color.WHITE);
            btnPrev.setFocusPainted(false);
            btnPrev.addActionListener(e -> {
                if (indiceActual > 0) {
                    indiceActual--;
                    actualizarImagen();
                } else {
                    indiceActual = listaImagenes.size() - 1;
                    actualizarImagen();
                }
            });

            lblStatus = new JLabel("1 / 1", SwingConstants.CENTER);
            lblStatus.setForeground(Color.WHITE);
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 14));
            lblStatus.setPreferredSize(new Dimension(80, 30));

            JButton btnNext = new JButton("Siguiente", new IconoFlechaDerecha());
            btnNext.setHorizontalTextPosition(SwingConstants.LEFT);
            btnNext.setFont(new Font("Segoe UI", Font.BOLD, 12));
            btnNext.setBackground(new Color(60, 60, 60));
            btnNext.setForeground(Color.WHITE);
            btnNext.setFocusPainted(false);
            btnNext.addActionListener(e -> {
                if (indiceActual < listaImagenes.size() - 1) {
                    indiceActual++;
                    actualizarImagen();
                } else {
                    indiceActual = 0;
                    actualizarImagen();
                }
            });

            pnlNavegacion.add(btnPrev);
            pnlNavegacion.add(lblStatus);
            pnlNavegacion.add(btnNext);
            pnlFooter.add(pnlNavegacion, BorderLayout.SOUTH);
            
            // Atajos de flecha izquierda/derecha para navegar
            this.getRootPane().registerKeyboardAction(e -> btnPrev.doClick(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_LEFT, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
            this.getRootPane().registerKeyboardAction(e -> btnNext.doClick(),
                KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_RIGHT, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
        }

        this.add(pnlFooter, BorderLayout.SOUTH);
        
        actualizarImagen();

        // Click en imagen para cerrar si es la única, o cambiar si se prefiere
        lblImagen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (listaImagenes.size() <= 1) {
                    dispose();
                }
            }
        });

        // Atajo teclado ESC
        this.getRootPane().registerKeyboardAction(e -> dispose(),
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0),
            JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    private void actualizarImagen() {
        if (listaImagenes.isEmpty() || indiceActual < 0 || indiceActual >= listaImagenes.size()) {
            lblImagen.setIcon(null);
            lblImagen.setText("Imagen no disponible");
            lblImagen.setForeground(Color.WHITE);
            if (lblStatus != null) lblStatus.setText("0 / 0");
            return;
        }

        String base64 = listaImagenes.get(indiceActual);
        ImageIcon icono = obtenerImagenDesdeBase64(base64);
        if (icono != null) {
            Image img = icono.getImage();
            int imgW = img.getWidth(null);
            int imgH = img.getHeight(null);
            if (imgW > 0 && imgH > 0) {
                // Preservar la relación de aspecto y adaptarlo al tamaño de la pantalla
                double scale = Math.min((double) 780 / imgW, (double) 490 / imgH);
                int scaledW = (int) (imgW * scale);
                int scaledH = (int) (imgH * scale);
                if (scaledW <= 0) scaledW = 1;
                if (scaledH <= 0) scaledH = 1;
                lblImagen.setIcon(new ImageIcon(img.getScaledInstance(scaledW, scaledH, Image.SCALE_SMOOTH)));
            } else {
                lblImagen.setIcon(icono);
            }
            lblImagen.setText("");
        } else {
            lblImagen.setIcon(null);
            lblImagen.setText("Error al cargar la imagen");
            lblImagen.setForeground(Color.WHITE);
        }

        if (lblStatus != null) {
            lblStatus.setText((indiceActual + 1) + " / " + listaImagenes.size());
        }

        // Renderizar miniaturas actualizadas
        if (pnlThumbnails != null) {
            pnlThumbnails.removeAll();
            for (int i = 0; i < listaImagenes.size(); i++) {
                final int idx = i;
                String b64 = listaImagenes.get(i);
                ImageIcon thumbIcon = utilidades.ImagenHelper.obtenerIcono(b64, 40, 40);
                
                JButton btnThumb = new JButton(thumbIcon);
                btnThumb.setPreferredSize(new Dimension(44, 44));
                btnThumb.setFocusPainted(false);
                btnThumb.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
                
                if (i == indiceActual) {
                    btnThumb.setBorder(BorderFactory.createLineBorder(new Color(39, 174, 96), 2));
                } else {
                    btnThumb.setBorder(BorderFactory.createLineBorder(new Color(100, 100, 100), 1));
                }
                
                btnThumb.addActionListener(e -> {
                    indiceActual = idx;
                    actualizarImagen();
                });
                
                pnlThumbnails.add(btnThumb);
            }
            pnlThumbnails.revalidate();
            pnlThumbnails.repaint();
        }
    }

    private ImageIcon obtenerImagenDesdeBase64(String base64) {
        if (base64 == null || base64.isEmpty()) return null;
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

    // --- CLASES PARA ICONOS ---
    
    private class IconoFlechaIzquierda implements Icon {
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 10, y + 4, x + 4, y + 8);
            g2.drawLine(x + 4, y + 8, x + 10, y + 12);
            g2.dispose();
        }
    }

    private class IconoFlechaDerecha implements Icon {
        @Override public int getIconWidth() { return 16; }
        @Override public int getIconHeight() { return 16; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 6, y + 4, x + 12, y + 8);
            g2.drawLine(x + 12, y + 8, x + 6, y + 12);
            g2.dispose();
        }
    }
}
