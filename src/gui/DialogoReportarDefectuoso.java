package gui;

import java.awt.*;
import java.awt.event.ActionEvent;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import dao.InventarioDefectuosoDAO;

public class DialogoReportarDefectuoso extends JDialog {

    private boolean exito = false;
    private int idProducto;
    private int maxStock;
    
    private JSpinner spinCantidad;
    private JTextArea txtObservacion;
    private JLabel lblFotoPlaceholder;
    private JButton btnProcesar;
    private JButton btnSeleccionarFoto;
    private JButton btnTomarFotoQR;
    private String fotoBase64 = null;

    public DialogoReportarDefectuoso(Window parent, int idProducto, String nombreProducto, int stockDisponible) {
        super(parent, "Reportar Producto Defectuoso (Almac\u00E9n)", Dialog.ModalityType.APPLICATION_MODAL);
        this.idProducto = idProducto;
        this.maxStock = stockDisponible;
        
        setSize(700, 500);
        setLocationRelativeTo(parent);
        setResizable(false);
        
        iniciarDiseno(nombreProducto, stockDisponible);
    }

    private void iniciarDiseno(String producto, int stockDisponible) {
        JPanel panelPrincipal = new JPanel(new BorderLayout(20, 20));
        panelPrincipal.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(20, 25, 20, 25));

        // --- 1. CABECERA: DATOS DEL PRODUCTO ---
        JPanel pnlInfo = new JPanel(new GridLayout(1, 2, 15, 10));
        pnlInfo.setOpaque(false);
        pnlInfo.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200)), 
            "Informaci\u00F3n del Producto", 
            TitledBorder.LEFT, TitledBorder.TOP, 
            new Font("Segoe UI", Font.BOLD, 14), new Color(45, 45, 45)
        ));

        pnlInfo.add(crearInfoLabel("Producto:", producto));
        pnlInfo.add(crearInfoLabel("Stock Disponible:", String.valueOf(stockDisponible) + " unidades"));

        panelPrincipal.add(pnlInfo, BorderLayout.NORTH);

        // --- 2. CENTRO: OBSERVACIONES Y FOTO ---
        JPanel pnlCentro = new JPanel(new BorderLayout(20, 0));
        pnlCentro.setOpaque(false);

        // Izquierda: Cantidad y Observaciones
        JPanel pnlIzquierda = new JPanel(new BorderLayout(0, 15));
        pnlIzquierda.setOpaque(false);
        
        JPanel pnlCantidad = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlCantidad.setOpaque(false);
        JLabel lblCantidad = new JLabel("Cantidad Defectuosa:");
        lblCantidad.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        SpinnerNumberModel model = new SpinnerNumberModel(1, 1, Math.max(1, stockDisponible), 1);
        spinCantidad = new JSpinner(model);
        spinCantidad.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        spinCantidad.setPreferredSize(new Dimension(80, 30));
        if(stockDisponible == 0) spinCantidad.setEnabled(false);
        
        pnlCantidad.add(lblCantidad);
        pnlCantidad.add(spinCantidad);

        JPanel pnlObs = new JPanel(new BorderLayout(0, 5));
        pnlObs.setOpaque(false);
        JLabel lblObs = new JLabel("Motivo del Da\u00F1o (Ej. Pantalla rota de f\u00E1brica):");
        lblObs.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        txtObservacion = new JTextArea();
        txtObservacion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtObservacion.setLineWrap(true);
        txtObservacion.setWrapStyleWord(true);
        JScrollPane scrollObs = new JScrollPane(txtObservacion);
        scrollObs.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        pnlObs.add(lblObs, BorderLayout.NORTH);
        pnlObs.add(scrollObs, BorderLayout.CENTER);
        
        pnlIzquierda.add(pnlCantidad, BorderLayout.NORTH);
        pnlIzquierda.add(pnlObs, BorderLayout.CENTER);

        // Derecha: Evidencia Fotogr\u00E1fica
        JPanel pnlFoto = new JPanel(new BorderLayout(0, 10));
        pnlFoto.setOpaque(false);
        pnlFoto.setPreferredSize(new Dimension(280, 0));
        
        JLabel lblTituloFoto = new JLabel("Fotograf\u00EDa de Evidencia (Max 4):");
        lblTituloFoto.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        lblFotoPlaceholder = new JLabel("Sin Imagen", SwingConstants.CENTER);
        lblFotoPlaceholder.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblFotoPlaceholder.setForeground(new Color(150, 150, 150));
        lblFotoPlaceholder.setBorder(BorderFactory.createDashedBorder(new Color(180, 180, 180), 2, 5, 2, true));
        lblFotoPlaceholder.setOpaque(true);
        lblFotoPlaceholder.setBackground(new Color(250, 250, 250));
        lblFotoPlaceholder.setToolTipText("Clic para ver imagen en grande");
        
        btnSeleccionarFoto = new JButton("Seleccionar desde PC...");
        btnSeleccionarFoto.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        btnSeleccionarFoto.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSeleccionarFoto.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            chooser.setDialogTitle("Seleccionar Evidencia del Da\u00F1o");
            javax.swing.filechooser.FileNameExtensionFilter filtro = new javax.swing.filechooser.FileNameExtensionFilter("Im\u00E1genes", "png", "jpg", "jpeg");
            chooser.setFileFilter(filtro);
            
            if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                try {
                    java.awt.image.BufferedImage imgOriginal = javax.imageio.ImageIO.read(chooser.getSelectedFile());
                    if (imgOriginal == null) return;
                    
                    int maxW = 800, maxH = 600;
                    int orgW = imgOriginal.getWidth(), orgH = imgOriginal.getHeight();
                    double scale = Math.min((double) maxW / orgW, (double) maxH / orgH);
                    int newW = (int) (orgW * scale), newH = (int) (orgH * scale);
                    
                    java.awt.image.BufferedImage imgRedimensionada = new java.awt.image.BufferedImage(newW, newH, java.awt.image.BufferedImage.TYPE_INT_RGB);
                    Graphics2D g2d = imgRedimensionada.createGraphics();
                    g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                    g2d.drawImage(imgOriginal, 0, 0, newW, newH, null);
                    g2d.dispose();
                    
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    javax.imageio.ImageIO.write(imgRedimensionada, "jpg", baos);
                    fotoBase64 = "data:image/jpeg;base64," + java.util.Base64.getEncoder().encodeToString(baos.toByteArray());
                    setPreviewBase64(fotoBase64);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        btnTomarFotoQR = new JButton("Tomar Foto con Celular (QR)");
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

        JPanel pnlBotonesFoto = new JPanel(new GridLayout(2, 1, 0, 5));
        pnlBotonesFoto.setOpaque(false);
        pnlBotonesFoto.add(btnTomarFotoQR);
        pnlBotonesFoto.add(btnSeleccionarFoto);

        pnlFoto.add(lblTituloFoto, BorderLayout.NORTH);
        pnlFoto.add(lblFotoPlaceholder, BorderLayout.CENTER);
        pnlFoto.add(pnlBotonesFoto, BorderLayout.SOUTH);

        pnlCentro.add(pnlIzquierda, BorderLayout.CENTER);
        pnlCentro.add(pnlFoto, BorderLayout.EAST);
        
        panelPrincipal.add(pnlCentro, BorderLayout.CENTER);

        // --- 3. PIE: BOTONES DE ACCI\u00D3N ---
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotones.setOpaque(false);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> this.dispose());

        btnProcesar = new JButton("Mandar a Defectuosos");
        btnProcesar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnProcesar.setBackground(new Color(243, 156, 18)); 
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
            Image img = javax.imageio.ImageIO.read(new java.io.ByteArrayInputStream(bytes));
            if (img != null) {
                lblFotoPlaceholder.setIcon(new ImageIcon(img.getScaledInstance(260, 200, Image.SCALE_SMOOTH)));
                if (count > 1) {
                    lblFotoPlaceholder.setText(count + " fotos (Clic para ver)");
                    lblFotoPlaceholder.setVerticalTextPosition(SwingConstants.BOTTOM);
                    lblFotoPlaceholder.setHorizontalTextPosition(SwingConstants.CENTER);
                    lblFotoPlaceholder.setForeground(new Color(41, 128, 185));
                    lblFotoPlaceholder.setFont(new Font("Segoe UI", Font.BOLD, 12));
                } else {
                    lblFotoPlaceholder.setText("");
                }
            }
        } catch(Exception e) {}
        
        for (java.awt.event.MouseListener ml : lblFotoPlaceholder.getMouseListeners()) {
            lblFotoPlaceholder.removeMouseListener(ml);
        }
        lblFotoPlaceholder.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblFotoPlaceholder.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                    java.util.List<String> list = new java.util.ArrayList<>();
                    if (fotoBase64.contains("|")) list.addAll(java.util.Arrays.asList(fotoBase64.split("\\|")));
                    else list.add(fotoBase64);
                    new DialogoVisorImagen(DialogoReportarDefectuoso.this, "Visor de Fotograf\u00EDa", list, 0).setVisible(true);
                }
            }
        });
    }

    private void procesarReclamo(ActionEvent evt) {
        if(maxStock <= 0) {
            utilidades.Mensajes.showMessageDialog(this, "No hay stock disponible para mandar a defectuosos.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (txtObservacion.getText().trim().isEmpty()) {
            utilidades.Mensajes.showMessageDialog(this, "Por favor, ingrese el motivo del da\u00F1o.", "Faltan Datos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int cantidad = (int) spinCantidad.getValue();
        
        int confirmacion = utilidades.Mensajes.showConfirmDialog(this, 
            "\u00BFEst\u00E1 seguro que desea mover " + cantidad + " unidades al inventario defectuoso?\n\nEsto descontar\u00E1 el stock de su inventario principal.", 
            "Confirmar Movimiento", 
            JOptionPane.YES_NO_OPTION, 
            JOptionPane.WARNING_MESSAGE);

        if (confirmacion == JOptionPane.YES_OPTION) {
            try {
                int idUsuario = utilidades.SesionGlobal.getUsuarioActual() != null ? utilidades.SesionGlobal.getUsuarioActual().getIdUsuario() : 1;
                InventarioDefectuosoDAO dao = new InventarioDefectuosoDAO();
                
                int idGenerado = dao.reportarDefectuosoAlmacen(idProducto, cantidad, txtObservacion.getText().trim(), idUsuario, fotoBase64);
                
                if (idGenerado != -1) {
                    if (fotoBase64 != null && !fotoBase64.isEmpty()) {
                        String[] fotos = fotoBase64.split("\\|");
                        String urlGuardada = null;
                        for (int i = 0; i < fotos.length; i++) {
                            String urlUnica = utilidades.ServidorFotos.guardarImagenDefectuosoDirecto(idGenerado, fotos[i], i+1);
                            if (i == 0) urlGuardada = urlUnica;
                            else if(urlGuardada != null) urlGuardada += "|" + urlUnica;
                        }
                        
                        // Si hay que guardar el path en DB, se puede hacer, pero normalmente GarantiaDAO lo guarda en DETALLE_VENTA
                        // Sin embargo, para defectuosos sin venta, deberiamos añadir una columna `foto_defectuoso` a INVENTARIO_DEFECTUOSO,
                        // o simplemente se leen de la carpeta basandonos en el ID de defectuoso como ya hace `ServidorFotos`.
                        // Como ServidorFotos busca por nombre de archivo, no necesitamos actualizar la DB, ServidorFotos se encarga
                    }
                    
                    this.exito = true;
                    utilidades.Mensajes.showMessageDialog(this, "Movimiento realizado con \u00E9xito.", "\u00C9xito", JOptionPane.INFORMATION_MESSAGE);
                    this.dispose();
                } else {
                    utilidades.Mensajes.showMessageDialog(this, "Hubo un error al guardar.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
                utilidades.Mensajes.showMessageDialog(this, "Error al guardar: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public boolean isExito() {
        return exito;
    }
}
