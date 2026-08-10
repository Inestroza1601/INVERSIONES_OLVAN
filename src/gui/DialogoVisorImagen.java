package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.ArrayList;

public class DialogoVisorImagen extends JDialog {

    private List<String> listaImagenes;
    private int indiceActual;
    private JLabel lblStatus;
    private JPanel pnlThumbnails;
    
    // Nuevos componentes para la info del producto
    private JPanel pnlTopInfo;
    private JLabel lblNombreProducto;
    private JLabel lblStockProducto;
    
    // Renderizador personalizado y Animación
    private JPanel pnlRender;
    private Image imgActual;
    private Image imgSiguiente;
    private Image imgOriginalActual;
    
    private float animacionSlide = 1.0f; // 1.0 = terminada
    private int animacionDireccion = 1; // 1 = der->izq (siguiente), -1 = izq->der (anterior)
    private Timer timerAnimacion;

    // Zoom y Pan
    private boolean isZoomed = false;
    private int panX = 0;
    private int panY = 0;
    private Point dragOrigin;
    
    // Tema Verde Oscuro Elegante
    private final Color COLOR_FONDO = new Color(11, 28, 20);
    private final Color COLOR_PANEL = new Color(16, 40, 28);
    private final Color COLOR_ACENTO = new Color(16, 185, 129); // Menta

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

    public void setInfoProducto(String nombre, int stock) {
        lblNombreProducto.setText(nombre);
        if (stock > 0) {
            lblStockProducto.setText("<html>Stock disponible: <font color='#2ecc71'><b>" + stock + "</b></font> unidades</html>");
        } else {
            lblStockProducto.setText("<html>Stock disponible: <font color='#e74c3c'><b>" + stock + "</b></font> unidades</html>");
        }
        pnlTopInfo.setVisible(true);
    }

    private void iniciarDiseno() {
        this.setSize(850, 720);
        this.setLocationRelativeTo(getParent());
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(COLOR_FONDO);

        // --- PANEL SUPERIOR (INFO PRODUCTO) ---
        pnlTopInfo = new JPanel(new BorderLayout());
        pnlTopInfo.setBackground(COLOR_PANEL);
        pnlTopInfo.setBorder(BorderFactory.createEmptyBorder(15, 25, 15, 25));
        pnlTopInfo.setVisible(false);

        lblNombreProducto = new JLabel("");
        lblNombreProducto.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblNombreProducto.setForeground(new Color(240, 240, 240));

        lblStockProducto = new JLabel("");
        lblStockProducto.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        lblStockProducto.setForeground(new Color(230, 240, 235)); // Color claro para el texto base
        
        pnlTopInfo.add(lblNombreProducto, BorderLayout.CENTER);
        pnlTopInfo.add(lblStockProducto, BorderLayout.EAST);
        
        this.add(pnlTopInfo, BorderLayout.NORTH);

        // --- PANEL CENTRAL (RENDERIZADO Y ANIMACIÓN) ---
        pnlRender = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                
                int panelW = getWidth();
                int panelH = getHeight();
                
                if (animacionSlide < 1.0f && imgSiguiente != null && imgActual != null) {
                    // Animación de deslizamiento suave (Ease Out)
                    float t = animacionSlide;
                    float easeOut = 1 - (float)Math.pow(1 - t, 3);
                    
                    int currentX = (panelW - imgActual.getWidth(null)) / 2;
                    int currentY = (panelH - imgActual.getHeight(null)) / 2;
                    
                    int nextX = (panelW - imgSiguiente.getWidth(null)) / 2;
                    int nextY = (panelH - imgSiguiente.getHeight(null)) / 2;
                    
                    int offset = (int)(panelW * easeOut);
                    
                    if (animacionDireccion == 1) { // Viene de la derecha
                        g2.drawImage(imgActual, currentX - offset, currentY, null);
                        g2.drawImage(imgSiguiente, nextX + panelW - offset, nextY, null);
                    } else { // Viene de la izquierda
                        g2.drawImage(imgActual, currentX + offset, currentY, null);
                        g2.drawImage(imgSiguiente, nextX - panelW + offset, nextY, null);
                    }
                } else if (imgActual != null) {
                    if (isZoomed && imgOriginalActual != null) {
                        int w = imgOriginalActual.getWidth(null);
                        int h = imgOriginalActual.getHeight(null);
                        
                        // Limitar Paneo
                        if (w > panelW) {
                            panX = Math.max(panelW - w, Math.min(0, panX));
                        } else {
                            panX = (panelW - w) / 2;
                        }
                        
                        if (h > panelH) {
                            panY = Math.max(panelH - h, Math.min(0, panY));
                        } else {
                            panY = (panelH - h) / 2;
                        }
                        
                        g2.drawImage(imgOriginalActual, panX, panY, null);
                    } else {
                        int currentX = (panelW - imgActual.getWidth(null)) / 2;
                        int currentY = (panelH - imgActual.getHeight(null)) / 2;
                        g2.drawImage(imgActual, currentX, currentY, null);
                    }
                } else {
                    // Sin imagen
                    g2.setColor(Color.WHITE);
                    g2.drawString("Imagen no disponible", panelW/2 - 50, panelH/2);
                }
            }
        };
        pnlRender.setBackground(COLOR_FONDO);
        pnlRender.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
        
        // --- INTERACCIONES DE RATÓN ---
        MouseAdapter mouseInteractions = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    dispose();
                } else if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 1) {
                    if (imgOriginalActual == null) return;
                    if (imgOriginalActual.getWidth(null) > pnlRender.getWidth() || imgOriginalActual.getHeight(null) > pnlRender.getHeight()) {
                        isZoomed = !isZoomed;
                        if (isZoomed) {
                            panX = (pnlRender.getWidth() - imgOriginalActual.getWidth(null)) / 2;
                            panY = (pnlRender.getHeight() - imgOriginalActual.getHeight(null)) / 2;
                            pnlRender.setCursor(new Cursor(Cursor.MOVE_CURSOR));
                        } else {
                            pnlRender.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                        }
                        pnlRender.repaint();
                    }
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (isZoomed) {
                    dragOrigin = e.getPoint();
                }
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                if (isZoomed && dragOrigin != null) {
                    int dx = e.getX() - dragOrigin.x;
                    int dy = e.getY() - dragOrigin.y;
                    panX += dx;
                    panY += dy;
                    dragOrigin = e.getPoint();
                    pnlRender.repaint();
                }
            }
        };
        pnlRender.addMouseListener(mouseInteractions);
        pnlRender.addMouseMotionListener(mouseInteractions);
        
        // Listener para redimensionar la ventana (re-escalar la imagen actual)
        pnlRender.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (imgOriginalActual != null && !isZoomed) {
                    imgActual = escalarImagen(imgOriginalActual, pnlRender.getWidth(), pnlRender.getHeight());
                    pnlRender.repaint();
                }
            }
        });

        this.add(pnlRender, BorderLayout.CENTER);
        
        // --- PANEL INFERIOR (CONTROLES) ---
        JPanel pnlFooter = new JPanel(new BorderLayout(10, 5));
        pnlFooter.setBackground(COLOR_PANEL);
        pnlFooter.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblCerrar = new JLabel("Haz doble clic en la imagen para cerrar (Esc) | Clic para Zoom | Arrastrar para mover");
        lblCerrar.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblCerrar.setForeground(new Color(170, 190, 180));
        lblCerrar.setHorizontalAlignment(SwingConstants.CENTER);
        pnlFooter.add(lblCerrar, BorderLayout.NORTH);

        if (listaImagenes.size() > 1) {
            pnlThumbnails = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 5));
            pnlThumbnails.setOpaque(false);
            pnlFooter.add(pnlThumbnails, BorderLayout.CENTER);

            JPanel pnlNavegacion = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
            pnlNavegacion.setOpaque(false);

            JButton btnPrev = crearBotonNavegacion("Anterior", new IconoFlechaIzquierda());
            btnPrev.addActionListener(e -> {
                if (animacionSlide < 1.0f) return; // Bloquear si está animando
                if (indiceActual > 0) { indiceActual--; actualizarImagen(true, -1); }
                else { indiceActual = listaImagenes.size() - 1; actualizarImagen(true, -1); }
            });

            lblStatus = new JLabel("1 / 1", SwingConstants.CENTER);
            lblStatus.setForeground(Color.WHITE);
            lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 15));
            lblStatus.setPreferredSize(new Dimension(80, 30));

            JButton btnNext = crearBotonNavegacion("Siguiente", new IconoFlechaDerecha());
            btnNext.setHorizontalTextPosition(SwingConstants.LEFT);
            btnNext.addActionListener(e -> {
                if (animacionSlide < 1.0f) return; // Bloquear si está animando
                if (indiceActual < listaImagenes.size() - 1) { indiceActual++; actualizarImagen(true, 1); }
                else { indiceActual = 0; actualizarImagen(true, 1); }
            });

            pnlNavegacion.add(btnPrev);
            pnlNavegacion.add(lblStatus);
            pnlNavegacion.add(btnNext);
            pnlFooter.add(pnlNavegacion, BorderLayout.SOUTH);
            
            this.getRootPane().registerKeyboardAction(e -> btnPrev.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
            this.getRootPane().registerKeyboardAction(e -> btnNext.doClick(), KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
        }

        this.add(pnlFooter, BorderLayout.SOUTH);
        
        // Cargar imagen inicial sin animación
        SwingUtilities.invokeLater(() -> actualizarImagen(false, 1));

        this.getRootPane().registerKeyboardAction(e -> dispose(), KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);
    }
    
    private JButton crearBotonNavegacion(String texto, Icon icono) {
        JButton btn = new JButton(texto, icono);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btn.setBackground(new Color(30, 80, 60)); // Verde más claro para que resalten más
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) { btn.setBackground(COLOR_ACENTO); }
            public void mouseExited(MouseEvent e) { btn.setBackground(new Color(30, 80, 60)); }
        });
        return btn;
    }

    private Image escalarImagen(Image img, int targetW, int targetH) {
        if (targetW <= 0) targetW = 800;
        if (targetH <= 0) targetH = 500;
        double scale = Math.min((double) targetW / img.getWidth(null), (double) targetH / img.getHeight(null));
        if (scale > 1.0) scale = 1.0; 
        int sw = (int) (img.getWidth(null) * scale);
        int sh = (int) (img.getHeight(null) * scale);
        if (sw <= 0) sw = 1;
        if (sh <= 0) sh = 1;
        return img.getScaledInstance(sw, sh, Image.SCALE_SMOOTH);
    }

    private void actualizarImagen(boolean animar, int direccion) {
        if (listaImagenes.isEmpty() || indiceActual < 0 || indiceActual >= listaImagenes.size()) {
            imgActual = null;
            imgOriginalActual = null;
            if (lblStatus != null) lblStatus.setText("0 / 0");
            pnlRender.repaint();
            return;
        }

        String base64 = listaImagenes.get(indiceActual);
        ImageIcon icono = obtenerImagenDesdeBase64(base64);
        
        if (icono != null && icono.getIconWidth() > 0) {
            Image nuevaOriginal = icono.getImage();
            Image nuevaEscalada = escalarImagen(nuevaOriginal, pnlRender.getWidth(), pnlRender.getHeight());
            
            if (animar && imgActual != null) {
                imgSiguiente = nuevaEscalada;
                imgOriginalActual = nuevaOriginal; // Guardar la original pero no permitir zoom hasta que acabe
                animacionDireccion = direccion;
                animacionSlide = 0.0f;
                isZoomed = false;
                panX = 0; panY = 0;
                pnlRender.setCursor(new Cursor(Cursor.CROSSHAIR_CURSOR));
                
                if (timerAnimacion != null && timerAnimacion.isRunning()) {
                    timerAnimacion.stop();
                }
                
                timerAnimacion = new Timer(15, e -> {
                    animacionSlide += 0.05f; 
                    if (animacionSlide >= 1.0f) {
                        animacionSlide = 1.0f;
                        imgActual = imgSiguiente;
                        imgSiguiente = null;
                        timerAnimacion.stop();
                    }
                    pnlRender.repaint();
                });
                timerAnimacion.start();
            } else {
                imgActual = nuevaEscalada;
                imgOriginalActual = nuevaOriginal;
                isZoomed = false;
                panX = 0; panY = 0;
                pnlRender.repaint();
            }
        } else {
            imgActual = null;
            imgOriginalActual = null;
            pnlRender.repaint();
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
                ImageIcon thumbIcon = utilidades.ImagenHelper.obtenerIcono(b64, 45, 45);
                
                JButton btnThumb = new JButton(thumbIcon);
                btnThumb.setPreferredSize(new Dimension(50, 50));
                btnThumb.setFocusPainted(false);
                btnThumb.setBackground(COLOR_FONDO);
                btnThumb.setCursor(new Cursor(Cursor.HAND_CURSOR));
                
                if (i == indiceActual) {
                    btnThumb.setBorder(BorderFactory.createLineBorder(COLOR_ACENTO, 2));
                } else {
                    btnThumb.setBorder(BorderFactory.createLineBorder(new Color(40, 80, 60), 1));
                }
                
                btnThumb.addActionListener(e -> {
                    if (animacionSlide < 1.0f || idx == indiceActual) return;
                    int dir = (idx > indiceActual) ? 1 : -1;
                    indiceActual = idx;
                    actualizarImagen(true, dir);
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
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 10, y + 3, x + 4, y + 8);
            g2.drawLine(x + 4, y + 8, x + 10, y + 13);
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
            g2.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.drawLine(x + 6, y + 3, x + 12, y + 8);
            g2.drawLine(x + 12, y + 8, x + 6, y + 13);
            g2.dispose();
        }
    }
}
