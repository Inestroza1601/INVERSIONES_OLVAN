package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.border.TitledBorder;

public class DialogoReclamarGarantia extends JDialog {

    private boolean exito = false;
    private int idDetalle;
    
    private JTextArea txtObservacion;
    private JComboBox<String> cmbResolucion;
    private JCheckBox chkReintegro;
    private JPanel pnlDiferencia;
    private JLabel lblProductoNuevo;
    private JLabel lblDiferencia;
    private JButton btnElegirSustituto;
    private modelo.Producto productoSustituto = null;
    private JLabel lblFotoPlaceholder;
    private JButton btnProcesar;
    private JButton btnSeleccionarFoto;
    private JButton btnTomarFotoQR;
    private String fotoBase64 = null;

    public DialogoReclamarGarantia(Window parent, String cliente, String producto, String serie, String fechaCompra, int idDetalle) {
        super(parent, "Procesar Reclamo de Garant\u00EDa", Dialog.ModalityType.APPLICATION_MODAL);
        this.idDetalle = idDetalle;
        
        setSize(700, 550);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        iniciarDiseno(cliente, producto, serie, fechaCompra);
    }

    private void iniciarDiseno(String cliente, String producto, String serie, String fechaCompra) {
        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));
        panelPrincipal.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); // Fondo verde pastel
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // --- 1. CABECERA: DATOS DEL PRODUCTO ---
        JPanel pnlInfo = new JPanel(new GridLayout(2, 2, 15, 10));
        pnlInfo.setOpaque(false);
        pnlInfo.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Informaci\u00F3n del Producto", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), new Color(45, 45, 45)
        ));

        pnlInfo.add(crearInfoLabel("Cliente:", cliente));
        pnlInfo.add(crearInfoLabel("Producto:", producto));
        pnlInfo.add(crearInfoLabel("Serie / IMEI:", serie));
        pnlInfo.add(crearInfoLabel("Fecha Compra:", fechaCompra));

        panelPrincipal.add(pnlInfo, BorderLayout.NORTH);

        // --- 2. CENTRO: OBSERVACIONES Y FOTO ---
        JPanel pnlCentro = new JPanel(new BorderLayout(20, 0));
        pnlCentro.setOpaque(false);

        // Izquierda: Observaciones y Resoluci\u00F3n
        JPanel pnlObs = new JPanel(new BorderLayout(0, 10));
        pnlObs.setOpaque(false);
        JLabel lblObs = new JLabel("Observaciones / Motivo del Da\u00F1o:");
        lblObs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtObservacion = new JTextArea();
        txtObservacion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtObservacion.setLineWrap(true);
        txtObservacion.setWrapStyleWord(true);
        JScrollPane scrollObs = new JScrollPane(txtObservacion);
        scrollObs.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        // Novedades: Combo y Checkbox
        // Novedades: Combo y Checkbox y Diferencia
        JPanel pnlResolucion = new JPanel();
        pnlResolucion.setLayout(new BoxLayout(pnlResolucion, BoxLayout.Y_AXIS));
        pnlResolucion.setOpaque(false);
        pnlResolucion.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));
        
        JLabel lblRes = new JLabel("Resoluci\u00F3n del Reclamo:");
        lblRes.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblRes.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        cmbResolucion = new JComboBox<>(new String[]{"Reparaci\u00F3n T\u00E9cnica con Proveedor", "Cambio por Producto Nuevo", "Cambio por Otro Producto Diferente", "Sin Soluci\u00F3n"});
        cmbResolucion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbResolucion.setAlignmentX(Component.LEFT_ALIGNMENT);
        cmbResolucion.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        
        chkReintegro = new JCheckBox("<html>Descontar reemplazo del inv.<br>y enviar producto a bodega</html>");
        chkReintegro.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        chkReintegro.setForeground(new Color(100, 100, 100));
        chkReintegro.setOpaque(false);
        chkReintegro.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // --- Panel Din\u00E1mico de Diferencia ---
        pnlDiferencia = new JPanel();
        pnlDiferencia.setLayout(new BoxLayout(pnlDiferencia, BoxLayout.Y_AXIS));
        pnlDiferencia.setOpaque(false);
        pnlDiferencia.setVisible(false); // Oculto por defecto
        pnlDiferencia.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlDiferencia.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
        
        btnElegirSustituto = new JButton("Buscar Producto Sustituto...");
        btnElegirSustituto.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblProductoNuevo = new JLabel("Producto: Ninguno");
        lblProductoNuevo.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        lblProductoNuevo.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblDiferencia = new JLabel("Diferencia: L 0.00");
        lblDiferencia.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblDiferencia.setForeground(new Color(13, 110, 253));
        lblDiferencia.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        pnlDiferencia.add(btnElegirSustituto);
        pnlDiferencia.add(Box.createVerticalStrut(3));
        pnlDiferencia.add(lblProductoNuevo);
        pnlDiferencia.add(Box.createVerticalStrut(3));
        pnlDiferencia.add(lblDiferencia);
        
        cmbResolucion.addActionListener(e -> {
            boolean esSustituto = cmbResolucion.getSelectedIndex() == 2;
            pnlDiferencia.setVisible(esSustituto);
            chkReintegro.setVisible(!esSustituto); // Si es sustituto, la l\u00F3gica de inv. es diferente
            revalidate();
            repaint();
        });
        
        btnElegirSustituto.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            DialogoBuscarProductoSustituto dialog = new DialogoBuscarProductoSustituto(parentWindow);
            dialog.setVisible(true);
            productoSustituto = dialog.getProductoSeleccionado();
            if (productoSustituto != null) {
                lblProductoNuevo.setText("Producto: " + productoSustituto.getNombreProducto());
                double precioOriginal = new dao.GarantiaDAO().obtenerPrecioOriginal(idDetalle);
                double diferencia = productoSustituto.getPrecioVenta() - precioOriginal;
                
                if (diferencia > 0) {
                    lblDiferencia.setText(String.format("Diferencia a Cobrar: L %,.2f", diferencia));
                    lblDiferencia.setForeground(new Color(227, 0, 15)); // Rojo (Debe dinero)
                } else if (diferencia < 0) {
                    lblDiferencia.setText(String.format("Diferencia a Devolver: L %,.2f", Math.abs(diferencia)));
                    lblDiferencia.setForeground(new Color(39, 174, 96)); // Verde (Dar vuelto al cliente)
                } else {
                    lblDiferencia.setText("Diferencia: L 0.00 (Cambio Directo)");
                    lblDiferencia.setForeground(new Color(45, 45, 45));
                }
            }
        });
        
        pnlResolucion.add(lblRes);
        pnlResolucion.add(Box.createVerticalStrut(5));
        pnlResolucion.add(cmbResolucion);
        pnlResolucion.add(Box.createVerticalStrut(5));
        pnlResolucion.add(chkReintegro);
        pnlResolucion.add(pnlDiferencia);

        JPanel pnlIzquierda = new JPanel(new BorderLayout(0, 10));
        pnlIzquierda.setOpaque(false);
        pnlIzquierda.add(lblObs, BorderLayout.NORTH);
        pnlIzquierda.add(scrollObs, BorderLayout.CENTER);
        pnlIzquierda.add(pnlResolucion, BorderLayout.SOUTH);

        // Derecha: Evidencia Fotogr\u00E1fica (Maqueta)
        JPanel pnlFoto = new JPanel(new BorderLayout(0, 10));
        pnlFoto.setOpaque(false);
        pnlFoto.setPreferredSize(new Dimension(280, 0));
        
        JLabel lblTituloFoto = new JLabel("Fotograf\u00EDa de Evidencia:");
        lblTituloFoto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        lblFotoPlaceholder = new JLabel("Sin Imagen", SwingConstants.CENTER);
        lblFotoPlaceholder.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblFotoPlaceholder.setForeground(new Color(150, 150, 150));
        lblFotoPlaceholder.setBorder(BorderFactory.createDashedBorder(new Color(180, 180, 180), 2, 5, 2, true));
        lblFotoPlaceholder.setOpaque(true);
        lblFotoPlaceholder.setBackground(new Color(250, 250, 250));
        lblFotoPlaceholder.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblFotoPlaceholder.setToolTipText("Clic para ver imagen en grande");
        
        lblFotoPlaceholder.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (fotoBase64 != null) {
                    new DialogoVisorImagen((Frame) SwingUtilities.getWindowAncestor(DialogoReclamarGarantia.this), "Visor de Fotograf\u00EDa", fotoBase64).setVisible(true);
                }
            }
        });

        btnSeleccionarFoto = new JButton("Seleccionar desde PC...");
        btnSeleccionarFoto.setIcon(new IconoCarpeta());
        btnSeleccionarFoto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnSeleccionarFoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSeleccionarFoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleccionar Evidencia del Da\u00F1o");
            javax.swing.filechooser.FileNameExtensionFilter filtro = new javax.swing.filechooser.FileNameExtensionFilter("Im\u00E1genes (PNG, JPG, JPEG)", "png", "jpg", "jpeg");
            chooser.setFileFilter(filtro);
            
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                java.io.File archivo = chooser.getSelectedFile();
                try {
                    // Cargar imagen original
                    java.awt.image.BufferedImage imgOriginal = javax.imageio.ImageIO.read(archivo);
                    if (imgOriginal == null) {
                        utilidades.Mensajes.showMessageDialog(this, "El archivo seleccionado no es una imagen v\u00E1lida.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    // Redimensionar para no saturar la BD (ej. max 800x600)
                    int maxW = 800, maxH = 600;
                    int orgW = imgOriginal.getWidth(), orgH = imgOriginal.getHeight();
                    double scale = Math.min((double) maxW / orgW, (double) maxH / orgH);
                    
                    int newW = (int) (orgW * scale);
                    int newH = (int) (orgH * scale);
                    
                    java.awt.image.BufferedImage imgRedimensionada = new java.awt.image.BufferedImage(newW, newH, java.awt.image.BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = imgRedimensionada.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(imgOriginal, 0, 0, newW, newH, null);
                    g2d.dispose();
                    
                    // Convertir a Base64
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(imgRedimensionada, "jpg", baos);
                    byte[] imageBytes = baos.toByteArray();
                    fotoBase64 = "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(imageBytes);
                    
                    // Mostrar preview (150x150)
                    Image imgPreview = imgRedimensionada.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    lblFotoPlaceholder.setIcon(new ImageIcon(imgPreview));
                    lblFotoPlaceholder.setText("");
                    
                } catch (Exception ex) {
                    ex.printStackTrace();
                    utilidades.Mensajes.showMessageDialog(this, "Error al procesar la imagen.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        pnlCentro.add(pnlIzquierda, BorderLayout.CENTER);
        construirPanelFoto(pnlFoto, lblTituloFoto, pnlCentro);
        panelPrincipal.add(pnlCentro, BorderLayout.CENTER);

        // --- 3. PIE: BOTONES DE ACCI\u00D3N ---
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotones.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> this.dispose());

        btnProcesar = new JButton("Procesar Reclamo");
        btnProcesar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnProcesar.setBackground(new Color(227, 0, 15)); // Rojo
        btnProcesar.setForeground(Color.WHITE);
        btnProcesar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnProcesar.addActionListener(this::procesarReclamo);

        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnProcesar);

        panelPrincipal.add(pnlBotones, BorderLayout.SOUTH);

        this.add(panelPrincipal);
    }

    private JPanel crearInfoLabel(String titulo, String valor) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        p.setOpaque(false);
        JLabel lblT = new JLabel(titulo);
        lblT.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblT.setForeground(new Color(100, 100, 100));
        
        JLabel lblV = new JLabel(valor);
        lblV.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblV.setForeground(new Color(45, 45, 45));
        
        p.add(lblT);
        p.add(lblV);
        return p;
    }

    private void procesarReclamo(ActionEvent evt) {
        if (txtObservacion.getText().trim().isEmpty()) {
            utilidades.Mensajes.showMessageDialog(this, 
                "Por favor, ingrese un motivo u observaci\u00F3n sobre el da\u00F1o del producto.", 
                "Faltan Datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirmacion = utilidades.Mensajes.showConfirmDialog(this, 
            "\u00BFEst\u00E1 seguro de procesar el reclamo de garant\u00EDa para este producto?", 
            "Confirmar Reclamo", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.QUESTION_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            dao.GarantiaDAO dao = new dao.GarantiaDAO();
            if (cmbResolucion.getSelectedIndex() == 2) {
                // Cambio por Otro Producto Diferente
                if (productoSustituto == null) {
                    utilidades.Mensajes.showMessageDialog(this, "Debe seleccionar un producto sustituto.", "Faltan Datos", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                int idUsuario = utilidades.SesionGlobal.getUsuarioActual() != null ? utilidades.SesionGlobal.getUsuarioActual().getIdUsuario() : 1;
                
                if (dao.procesarCambioGarantia(idDetalle, productoSustituto.getIdProducto(), productoSustituto.getPrecioVenta(), idUsuario, txtObservacion.getText(), fotoBase64, cmbResolucion.getSelectedItem().toString(), chkReintegro.isSelected())) {
                    this.exito = true;
                    // Generar Ticket
                    utilidades.GeneradorTickets.generarTicketCambioPDF(idDetalle, productoSustituto, dao.obtenerPrecioOriginal(idDetalle));
                    this.dispose();
                } else {
                    utilidades.Mensajes.showMessageDialog(this, "Hubo un error al aplicar el reclamo.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                // Resoluci\u00F3n Normal
                int idUsuario = utilidades.SesionGlobal.getUsuarioActual() != null ? utilidades.SesionGlobal.getUsuarioActual().getIdUsuario() : 1;
                if (dao.aplicarReclamo(idDetalle, txtObservacion.getText(), fotoBase64, cmbResolucion.getSelectedItem().toString(), chkReintegro.isSelected(), idUsuario)) {
                    this.exito = true;
                    // Generar Ticket de Garantia
                    utilidades.GeneradorTickets.generarTicketGarantiaPDF(idDetalle, cmbResolucion.getSelectedItem().toString(), txtObservacion.getText());
                    this.dispose();
                } else {
                    utilidades.Mensajes.showMessageDialog(this, "Hubo un error al aplicar el reclamo.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    public boolean isExito() {
        return exito;
    }

    private void setPreviewBase64(String base64String) {
        this.fotoBase64 = base64String;
        try {
            String b64 = base64String;
            int count = 1;
            if (b64.contains("|")) {
                String[] parts = b64.split("\\|");
                b64 = parts[0];
                count = parts.length;
            }
            if (b64.contains(",")) b64 = b64.split(",")[1];
            byte[] bytes = java.util.Base64.getDecoder().decode(b64);
            java.io.ByteArrayInputStream bais = new java.io.ByteArrayInputStream(bytes);
            Image img = javax.imageio.ImageIO.read(bais);
            if (img != null) {
                Image imgPreview = img.getScaledInstance(260, 200, Image.SCALE_SMOOTH);
                lblFotoPlaceholder.setIcon(new ImageIcon(imgPreview));
                if (count > 1) {
                    lblFotoPlaceholder.setText(count + " fotos (Clic para ver)");
                    lblFotoPlaceholder.setVerticalTextPosition(SwingConstants.BOTTOM);
                    lblFotoPlaceholder.setHorizontalTextPosition(SwingConstants.CENTER);
                    lblFotoPlaceholder.setForeground(new Color(39, 174, 96));
                    lblFotoPlaceholder.setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    lblFotoPlaceholder.setText("");
                }
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
        
        for (java.awt.event.MouseListener ml : lblFotoPlaceholder.getMouseListeners()) {
            lblFotoPlaceholder.removeMouseListener(ml);
        }
        lblFotoPlaceholder.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblFotoPlaceholder.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                    java.util.List<String> list = new java.util.ArrayList<>();
                    if (fotoBase64.contains("|")) {
                        list.addAll(java.util.Arrays.asList(fotoBase64.split("\\|")));
                    } else {
                        list.add(fotoBase64);
                    }
                    new DialogoVisorImagen(DialogoReclamarGarantia.this, "Visor de Garant\u00EDa", list, 0).setVisible(true);
                }
            }
        });
    }

    private void construirPanelFoto(JPanel pnlFoto, JLabel lblTituloFoto, JPanel pnlCentro) {
        btnTomarFotoQR = new JButton("Tomar Foto con Celular");
        btnTomarFotoQR.setIcon(new IconoCamara());
        btnTomarFotoQR.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnTomarFotoQR.setBackground(new Color(227, 0, 15));
        btnTomarFotoQR.setForeground(Color.WHITE);
        btnTomarFotoQR.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnTomarFotoQR.addActionListener(e -> {
            new DialogoEscanearQR((Frame) SwingUtilities.getWindowAncestor(this), false, 4, b64 -> {
                setPreviewBase64(b64);
                utilidades.Mensajes.showMessageDialog(this, "Foto capturada y adjuntada exitosamente.", "\u00C9xito", JOptionPane.INFORMATION_MESSAGE);
            }).setVisible(true);
        });

        JPanel pnlBotones = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlBotones.setOpaque(false);
        pnlBotones.add(btnTomarFotoQR);
        pnlBotones.add(btnSeleccionarFoto);

        pnlFoto.add(lblTituloFoto, BorderLayout.NORTH);
        pnlFoto.add(lblFotoPlaceholder, BorderLayout.CENTER);
        pnlFoto.add(pnlBotones, BorderLayout.SOUTH);

        pnlCentro.add(pnlFoto, BorderLayout.EAST);
    }

    private class IconoCamara implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            // Cuerpo de la c\u00E1mara
            g2.fillRoundRect(x + 2, y + 6, 16, 10, 4, 4);
            // Flash/visor arriba
            g2.fillRoundRect(x + 6, y + 3, 8, 4, 2, 2);
            // Lente
            g2.setColor(new Color(227, 0, 15)); // Rojo para hacer contraste
            g2.fillOval(x + 6, y + 7, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawOval(x + 6, y + 7, 8, 8);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
    }

    private class IconoCarpeta implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(100, 100, 100));
            // Parte trasera de la carpeta
            g2.fillRoundRect(x + 2, y + 4, 8, 4, 2, 2);
            g2.fillRoundRect(x + 2, y + 6, 16, 10, 2, 2);
            // Solapa frontal m\u00E1s clara
            g2.setColor(new Color(130, 130, 130));
            g2.fillRoundRect(x + 2, y + 8, 16, 8, 2, 2);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
    }
}

