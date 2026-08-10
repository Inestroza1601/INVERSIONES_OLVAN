package gui;

import dao.InventarioDAO;
import modelo.Producto;
import utilidades.SesionGlobal;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.io.File;
import java.util.List;

public class PanelBuscarProducto extends JPanel {

    private JTable tablaInventario;
    private DefaultTableModel modeloTabla;
    private JTextField txtBusqueda;
    private TableRowSorter<DefaultTableModel> sorter;
    
    // Cach\u00E9 y estado para Lazy Loading con Shimmer
    private java.util.Map<Integer, ImageIcon> cacheImagenes = new java.util.concurrent.ConcurrentHashMap<>();
    private java.util.Set<Integer> imagenesEnProceso = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    private float animacionShimmerPhase = 0f;
    private Timer timerShimmer;

    public PanelBuscarProducto() {
        iniciarDiseno();
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); // Verde Vintage
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // --- PANEL SUPERIOR ---
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setOpaque(false);

        JLabel lblTitulo = new JLabel("Cat\u00E1logo de Inventario");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);

        txtBusqueda = new JTextField(20);
        txtBusqueda.putClientProperty("JTextField.placeholderText", "Buscar por nombre o c\u00F3digo...");
        txtBusqueda.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtBusqueda.setPreferredSize(new Dimension(300, 35));

        panelSuperior.add(lblTitulo, BorderLayout.WEST);
        panelSuperior.add(txtBusqueda, BorderLayout.EAST);
        this.add(panelSuperior, BorderLayout.NORTH);

        // --- TABLA DE INVENTARIO (Sin columna de acciones) ---
        String[] columnas = {"ID", "Foto", "C\u00F3digo", "Producto", "P. Compra", "P. Venta", "P. T\u00E9cnico", "Stock"};
        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } // Nada es editable directamente
        };

        // --- CREACI\u00D3N DE LA TABLA CON FORMATO CONDICIONAL ROJO ---
        tablaInventario = new JTable(modeloTabla) {
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    Object valorStock = getValueAt(row, 7);
                    int stock = 1; 
                    if (valorStock != null) {
                        try { stock = Integer.parseInt(valorStock.toString()); } catch (NumberFormatException e) {}
                    }
                    if (stock <= 0) c.setForeground(new Color(227, 0, 15)); // Rojo Logo para sin stock
                    else c.setForeground(new Color(45, 45, 45)); // Gris Oscuro para texto normal
                    c.setBackground(new Color(255, 255, 255)); // Blanco Puro
                }
                return c;
            }
        };

        tablaInventario.setShowGrid(false);
        tablaInventario.setRowHeight(70); 
        tablaInventario.setBackground(new Color(255, 255, 255)); // Blanco Puro
        tablaInventario.setForeground(new Color(30, 41, 59));
        tablaInventario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tablaInventario.setSelectionBackground(new Color(209, 250, 229)); // Selecci\u00F3n menta luminosa
        tablaInventario.setSelectionForeground(new Color(6, 95, 70));
        tablaInventario.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaInventario.getTableHeader().setBackground(new Color(236, 253, 245)); // Verde Menta fresca
        tablaInventario.getTableHeader().setForeground(new Color(6, 95, 70));     // Verde Esmeralda Oscuro
        tablaInventario.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(16, 185, 129)));
        tablaInventario.getTableHeader().setPreferredSize(new Dimension(0, 45));

        tablaInventario.getColumnModel().getColumn(0).setMinWidth(0);
        tablaInventario.getColumnModel().getColumn(0).setMaxWidth(0); 

        tablaInventario.getColumnModel().getColumn(1).setPreferredWidth(80); 
        tablaInventario.getColumnModel().getColumn(1).setMaxWidth(80);
        tablaInventario.getColumnModel().getColumn(1).setCellRenderer(new ImagenProductoRenderer());

        // Centrar el texto en todas las dem\u00E1s columnas para que se vea ordenado y alineado con el encabezado
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        for (int i = 2; i < tablaInventario.getColumnCount(); i++) {
            tablaInventario.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        sorter = new TableRowSorter<>(modeloTabla);
        sorter.setSortKeys(java.util.List.of(new RowSorter.SortKey(0, SortOrder.DESCENDING)));
        
        tablaInventario.setRowSorter(sorter);
        txtBusqueda.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { filtrar(); }
            @Override public void removeUpdate(DocumentEvent e) { filtrar(); }
            @Override public void changedUpdate(DocumentEvent e) { filtrar(); }
            private void filtrar() {
                String texto = txtBusqueda.getText();
                if (texto.trim().length() == 0) sorter.setRowFilter(null);
                else sorter.setRowFilter(RowFilter.regexFilter("(?i)" + texto, 2, 3)); 
            }
        });

       // --- L\u00D3GICA DE CURSORES Y CLICS EN TABLA ---
        tablaInventario.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                int columna = tablaInventario.columnAtPoint(e.getPoint());
                if (columna == 1) tablaInventario.setCursor(new Cursor(Cursor.HAND_CURSOR));
                else tablaInventario.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });

        tablaInventario.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int fila = tablaInventario.rowAtPoint(e.getPoint());
                int columna = tablaInventario.columnAtPoint(e.getPoint());
                
                if (fila >= 0) {
                    // Selecciona autom\u00E1ticamente la fila al hacer clic (\u00FAtil para el clic derecho)
                    tablaInventario.setRowSelectionInterval(fila, fila);

                    // 1. L\u00F3gica para hacer ZOOM a la foto (Clic Izquierdo en Columna 1)
                    if (columna == 1 && SwingUtilities.isLeftMouseButton(e)) {
                        int filaModelo = tablaInventario.convertRowIndexToModel(fila);
                        int idProducto = (int) modeloTabla.getValueAt(filaModelo, 0);
                        String nombre = modeloTabla.getValueAt(filaModelo, 3).toString();
                        int stock = 0;
                        try {
                            stock = Integer.parseInt(modeloTabla.getValueAt(filaModelo, 7).toString());
                        } catch(Exception ex) {}
                        mostrarZoomImagen(idProducto, nombre, stock);
                    }
                    
                    // 2. L\u00F3gica del Men\u00FA Estilo Windows (Doble Clic Izquierdo O Clic Derecho)
                    if ((SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) || SwingUtilities.isRightMouseButton(e)) {
                        mostrarMenuOpciones(e.getComponent(), e.getX(), e.getY(), fila);
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tablaInventario);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true));
        scrollPane.getViewport().setBackground(new Color(255, 255, 255)); // Fondo blanco
        this.add(scrollPane, BorderLayout.CENTER);

        cargarDatosDesdeBD();

        // Timer global para la animaci\u00F3n fluida del Shimmer (brillo)
        timerShimmer = new Timer(30, e -> {
            if (!imagenesEnProceso.isEmpty()) {
                animacionShimmerPhase += 0.05f;
                if (animacionShimmerPhase > 1f) animacionShimmerPhase = 0f;
                tablaInventario.repaint(); // Repinta para animar el skeleton
            }
        });
        timerShimmer.start();
    }

    public void cargarDatosDesdeBD() {
        modeloTabla.setRowCount(0); // Limpiar tabla
        InventarioDAO dao = new InventarioDAO();
        List<Producto> productos = dao.listarProductosActivos();

        for (Producto p : productos) {
            String pCompra = String.format("L %,.2f", p.getPrecioCompra());
            String pVenta = String.format("L %,.2f", p.getPrecioVenta());
            String pMayorista = p.getPrecioMayorista() > 0 ? String.format("L %,.2f", p.getPrecioMayorista()) : "N/A";

            modeloTabla.addRow(new Object[]{
                p.getIdProducto(),       // 0: ID
                p.getImagen_producto(),       // 1: Ruta Foto
                p.getCodigoBarras(),     // 2: C\u00F3digo
                p.getNombreProducto(),   // 3: Nombre
                pCompra,                 // 4: Precio Compra
                pVenta,                  // 5: Precio Venta
                pMayorista,              // 6: Precio T\u00E9cnico/Mayorista
                p.getStockProducto(),    // 7: Stock
            });
        }
    }

    // =========================================================
    // RENDERIZADOR DE IMAGEN CON LAZY LOADING Y SHIMMER EFFECT
    // =========================================================
    private class ImagenProductoRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            int filaModelo = table.convertRowIndexToModel(row);
            int idProducto = (int) table.getModel().getValueAt(filaModelo, 0);
            
            // Si ya est\u00E1 en cach\u00E9, retornamos el JLabel normal
            if (cacheImagenes.containsKey(idProducto)) {
                JLabel label = new JLabel();
                label.setHorizontalAlignment(SwingConstants.CENTER);
                label.setOpaque(true);
                label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
                
                ImageIcon icon = cacheImagenes.get(idProducto);
                if (icon != null && icon.getIconWidth() > 0) {
                    label.setIcon(icon);
                } else {
                    label.setText("No Img");
                    label.setForeground(Color.GRAY);
                }
                return label;
            }

            // Si NO est\u00E1 en cach\u00E9, iniciamos SwingWorker y retornamos panel animado
            if (!imagenesEnProceso.contains(idProducto)) {
                imagenesEnProceso.add(idProducto);
                
                SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
                    @Override
                    protected ImageIcon doInBackground() throws Exception {
                        String imgVal = new InventarioDAO().obtenerRutaImagenBase64(idProducto);
                        if (imgVal == null || imgVal.trim().isEmpty()) {
                            if (SesionGlobal.getEmpresaActual() != null && SesionGlobal.getEmpresaActual().getImagen_logo() != null) {
                                imgVal = SesionGlobal.getEmpresaActual().getImagen_logo();
                            }
                        }
                        if (imgVal == null || imgVal.trim().isEmpty()) {
                              java.net.URL defaultLogo = getClass().getResource("/image/logo.png");
                              if (defaultLogo != null) {
                                  return new ImageIcon(new ImageIcon(defaultLogo).getImage().getScaledInstance(60, 60, Image.SCALE_SMOOTH));
                              }
                              return null;
                        }
                        
                        String valProcesar = imgVal;
                        if (valProcesar.contains("|")) {
                            valProcesar = valProcesar.split("\\|")[0];
                        }
                        return utilidades.ImagenHelper.obtenerIcono(valProcesar, 60, 60);
                    }
                    @Override
                    protected void done() {
                        try {
                            ImageIcon icon = get();
                            // Guardamos null-safe
                            cacheImagenes.put(idProducto, icon != null ? icon : new ImageIcon()); 
                        } catch (Exception ex) {
                            cacheImagenes.put(idProducto, new ImageIcon());
                        } finally {
                            imagenesEnProceso.remove(idProducto);
                            table.repaint();
                        }
                    }
                };
                worker.execute();
            }

            // --- RENDERIZADO DEL SKELETON ANIMADO (BR\u00CDLLO) ---
            JPanel panelShimmer = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    // Fondo gris base
                    g2.setColor(new Color(235, 237, 240));
                    g2.fillRoundRect(getWidth()/2 - 30, getHeight()/2 - 30, 60, 60, 10, 10);
                    
                    // Gradiente de brillo que se mueve
                    int gradientWidth = 60;
                    int startX = (getWidth()/2 - 30) - gradientWidth + (int)(animacionShimmerPhase * (60 + gradientWidth * 2));
                    
                    Color c1 = new Color(255, 255, 255, 0);
                    Color c2 = new Color(255, 255, 255, 200);
                    
                    LinearGradientPaint paint = new LinearGradientPaint(
                            startX, 0, startX + gradientWidth, 0,
                            new float[]{0.0f, 0.5f, 1.0f},
                            new Color[]{c1, c2, c1}
                    );
                    
                    g2.setPaint(paint);
                    g2.fillRoundRect(getWidth()/2 - 30, getHeight()/2 - 30, 60, 60, 10, 10);
                    g2.dispose();
                }
            };
            panelShimmer.setOpaque(true);
            panelShimmer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return panelShimmer;
        }
    }
    
    private void mostrarZoomImagen(int idProducto, String nombreProducto, int stock) {
        String imgVal = new InventarioDAO().obtenerRutaImagenBase64(idProducto);
        if (imgVal == null || imgVal.trim().isEmpty()) {
            if (SesionGlobal.getEmpresaActual() != null && SesionGlobal.getEmpresaActual().getImagen_logo() != null) {
                imgVal = SesionGlobal.getEmpresaActual().getImagen_logo();
            }
        }
        if (imgVal == null || imgVal.trim().isEmpty()) return;
        
        java.util.List<String> list = new java.util.ArrayList<>();
        if (imgVal.contains("|")) {
            String[] parts = imgVal.split("\\|");
            for (String part : parts) {
                if (!part.trim().isEmpty()) {
                    list.add(part);
                }
            }
        } else {
            list.add(imgVal);
        }

        Window window = SwingUtilities.getWindowAncestor(this);
        DialogoVisorImagen visor = null;
        if (window instanceof Frame) {
            visor = new DialogoVisorImagen((Frame) window, "Previsualizaci\u00F3n del Producto", list, 0);
        } else if (window instanceof JDialog) {
            visor = new DialogoVisorImagen((JDialog) window, "Previsualizaci\u00F3n del Producto", list, 0);
        }
        
        if (visor != null) {
            visor.setInfoProducto(nombreProducto, stock);
            visor.setVisible(true);
        }
    }

    /**
     * Reduce las dimensiones de la imagen por pasos intermedios para evitar la p\u00E9rdida de definici\u00F3n.
     */
    private java.awt.image.BufferedImage escalarConMaximaNitidez(Image img, int targetWidth, int targetHeight) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        
        // Crear un lienzo inicial en RAM con la imagen original
        java.awt.image.BufferedImage scratch = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = scratch.createGraphics();
        g2.drawImage(img, 0, 0, null);
        g2.dispose();
        
        // Reducci\u00F3n progresiva dividiendo por 2 hasta acercarnos al tama\u00F1o final
        while (w > targetWidth * 2 || h > targetHeight * 2) {
            w = (w > targetWidth * 2) ? w / 2 : targetWidth;
            h = (h > targetHeight * 2) ? h / 2 : targetHeight;
            
            java.awt.image.BufferedImage temp = new java.awt.image.BufferedImage(w, h, java.awt.image.BufferedImage.TYPE_INT_ARGB);
            g2 = temp.createGraphics();
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.drawImage(scratch, 0, 0, w, h, null);
            g2.dispose();
            scratch = temp;
        }
        
        // Renderizado final con ajuste Bic\u00FAbico y Antialiasing para m\u00E1xima fidelidad
        java.awt.image.BufferedImage imgFinal = new java.awt.image.BufferedImage(targetWidth, targetHeight, java.awt.image.BufferedImage.TYPE_INT_ARGB);
        g2 = imgFinal.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.drawImage(scratch, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        
        return imgFinal;
    }
    
    // =========================================================
    // MEN\u00DA CONTEXTUAL (ESTILO WINDOWS) Y ACCIONES
    // =========================================================
    private void mostrarMenuOpciones(Component componente, int x, int y, int filaVista) {
        int rolId = (SesionGlobal.getUsuarioActual() != null) ? SesionGlobal.getUsuarioActual().getIdRol() : 1;
        if (rolId == 3) {
            return; // Cajeros no tienen acceso a editar/eliminar productos
        }

        JPopupMenu menu = new JPopupMenu();
        menu.setBackground(new Color(255, 255, 255)); // Blanco puro
        menu.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1));

        // Ahora inyectamos nuestros propios \u00EDconos dibujados en Java 2D
        JMenuItem itemEditar = crearMenuItem("Ver / Editar Producto", new Color(39, 174, 96), new IconoOjo()); // Verde Menta
        JMenuItem itemEliminar = crearMenuItem("Eliminar Producto", new Color(227, 0, 15), new IconoBasurero()); // Rojo Logo

        itemEditar.addActionListener(e -> editarProductoSeleccionado(filaVista));
        
        modelo.Usuario uAct = utilidades.SesionGlobal.getUsuarioActual();
        if (uAct != null && !uAct.tienePermiso("EDITAR_INVENTARIO")) {
            itemEditar.setEnabled(false);
            itemEditar.setToolTipText("No tienes permiso para editar productos.");
        }
        
        if (uAct != null && !uAct.tienePermiso("ELIMINAR_INVENTARIO")) {
            itemEliminar.setEnabled(false);
            itemEliminar.setToolTipText("No tienes permiso para eliminar productos.");
        } else {
            itemEliminar.addActionListener(e -> eliminarProductoSeleccionado(filaVista));
        }

        menu.add(itemEditar);
        menu.addSeparator(); 
        menu.add(itemEliminar);
        
        menu.show(componente, x, y);
    }

    private JMenuItem crearMenuItem(String texto, Color colorHover, Icon icono) {
        JMenuItem item = new JMenuItem(texto);
        item.setIcon(icono); // Asignamos el \u00EDcono detallado
        item.setIconTextGap(12); // Separaci\u00F3n elegante entre el dibujo y el texto
        item.setFont(new Font("Segoe UI", Font.BOLD, 14));
        item.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        item.setBackground(new Color(255, 255, 255)); // Blanco Puro
        item.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        item.setCursor(new Cursor(Cursor.HAND_CURSOR));
        item.setOpaque(true);
        
        item.addChangeListener(e -> {
            if (item.isArmed()) {
                item.setBackground(colorHover);
                item.setForeground(Color.WHITE);
            } else {
                item.setBackground(new Color(255, 255, 255));
                item.setForeground(new Color(45, 45, 45));
            }
        });
        return item;
    }

    private void editarProductoSeleccionado(int filaVista) {
        int filaModelo = tablaInventario.convertRowIndexToModel(filaVista);
        int idProducto = (int) modeloTabla.getValueAt(filaModelo, 0); 
        
        PanelInventario parent = (PanelInventario) SwingUtilities.getAncestorOfClass(PanelInventario.class, PanelBuscarProducto.this);
        if(parent != null) {
            parent.abrirSubPanelAsync(() -> {
                InventarioDAO dao = new InventarioDAO();
                Producto p = dao.obtenerProductoPorId(idProducto); 
                if (p != null) {
                    return new PanelCrearProducto(p);
                } else {
                    throw new RuntimeException("Error: No se encontraron los datos del producto.");
                }
            });
        }
    }

    private void eliminarProductoSeleccionado(int filaVista) {
        int filaModelo = tablaInventario.convertRowIndexToModel(filaVista);
        int idProducto = (int) modeloTabla.getValueAt(filaModelo, 0);
        String nombre = modeloTabla.getValueAt(filaModelo, 3).toString();
        
        int confirmacion = utilidades.Mensajes.showConfirmDialog(this, "\u00BFEst\u00E1 seguro de eliminar el producto:\n" + nombre + "?", "Confirmar Eliminaci\u00F3n", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacion == JOptionPane.YES_OPTION) {
            InventarioDAO dao = new InventarioDAO();
            try {
                if (dao.eliminarProductoLogico(idProducto)) {
                    utilidades.Mensajes.showMessageDialog(this, "Producto eliminado exitosamente.");
                    cargarDatosDesdeBD();
                }
            } catch (java.sql.SQLException ex) {
                utilidades.Mensajes.showMessageDialog(this, ex.getMessage(), "Acci\u00F3n Denegada", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    // =========================================================
    // DIBUJOS VECTORIALES PARA EL MEN\u00DA CONTEXTUAL
    // =========================================================
    private class IconoOjo implements Icon {
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground()); // Hereda din\u00E1micamente el color del texto (gris o blanco en hover)
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Forma de almendra (Curvas superior e inferior)
            g2.draw(new java.awt.geom.QuadCurve2D.Double(x + 1, y + 10, x + 10, y + 2, x + 19, y + 10));
            g2.draw(new java.awt.geom.QuadCurve2D.Double(x + 1, y + 10, x + 10, y + 18, x + 19, y + 10));
            
            // Iris (C\u00EDrculo central)
            g2.drawOval(x + 6, y + 6, 8, 8);
            
            // Pupila (Relleno) y un peque\u00F1o brillo (Blanco)
            g2.fillOval(x + 8, y + 8, 4, 4);
            g2.setColor(Color.WHITE);
            g2.fillOval(x + 9, y + 9, 1, 1); // Detalle de brillo en el ojo
            
            g2.dispose();
        }
    }

    private class IconoBasurero implements Icon {
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
        @Override public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(c.getForeground()); // Hereda din\u00E1micamente el color del texto (gris o blanco en hover)
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            // Agarradera y Tapa
            g2.drawRoundRect(x + 8, y + 2, 4, 3, 2, 2);
            g2.drawLine(x + 4, y + 5, x + 16, y + 5);
            
            // Cuerpo del basurero (Con bordes suavemente redondeados)
            g2.drawRoundRect(x + 5, y + 5, 10, 12, 3, 3);
            
            // Detalles internos (L\u00EDneas del basurero)
            g2.setStroke(new BasicStroke(1.5f));
            g2.drawLine(x + 8, y + 8, x + 8, y + 14);
            g2.drawLine(x + 12, y + 8, x + 12, y + 14);
            
            g2.dispose();
        }
    }
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

