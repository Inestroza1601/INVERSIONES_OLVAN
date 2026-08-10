package gui;

import dao.InventarioDAO;
import modelo.Producto;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.util.List;

public class DialogoBuscarProductoSustituto extends JDialog {
    private Producto productoSeleccionado = null;
    
    // Caché y estado para Lazy Loading con Shimmer
    private java.util.Map<Integer, ImageIcon> cacheImagenes = new java.util.concurrent.ConcurrentHashMap<>();
    private java.util.Set<Integer> imagenesEnProceso = java.util.Collections.synchronizedSet(new java.util.HashSet<>());
    private float animacionShimmerPhase = 0f;
    private Timer timerShimmer;

    public DialogoBuscarProductoSustituto(Window parent) {
        super(parent, "Catálogo Rápido - Selección de Sustituto", Dialog.ModalityType.APPLICATION_MODAL);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);
        setLayout(new BorderLayout(10, 10));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField txtB = new JTextField();
        txtB.setBackground(new Color(255, 255, 255));
        txtB.setForeground(new Color(45, 45, 45));
        txtB.setCaretColor(new Color(45, 45, 45));
        txtB.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(180, 208, 192)), BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        txtB.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtB.putClientProperty("JTextField.placeholderText", "Buscar por Nombre o Código...");
        pnlTop.add(txtB, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);

        String[] cols = {"ID", "Foto", "Código", "Producto", "Precio", "Stock"};
        DefaultTableModel mod = new DefaultTableModel(null, cols) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        JTable tab = new JTable(mod) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    Object valorStock = getValueAt(row, 5);
                    int stock = 1;
                    if (valorStock != null) {
                        try { stock = Integer.parseInt(valorStock.toString()); } catch (NumberFormatException e) {}
                    }
                    if (stock <= 0) c.setForeground(new Color(227, 0, 15));
                    else c.setForeground(new Color(45, 45, 45));
                    c.setBackground(new Color(255, 255, 255));
                }
                return c;
            }
        };

        tab.setBackground(new Color(255, 255, 255));
        tab.setForeground(new Color(45, 45, 45));
        tab.setRowHeight(60);
        tab.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tab.setSelectionBackground(new Color(205, 235, 218));
        tab.setSelectionForeground(Color.BLACK);
        tab.getTableHeader().setBackground(utilidades.EfectosUI.COLOR_SIDEBAR_PASTEL);
        tab.getTableHeader().setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        tab.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, utilidades.EfectosUI.COLOR_BORDE));
        tab.getTableHeader().setPreferredSize(new Dimension(0, 35));

        tab.getColumnModel().getColumn(0).setMinWidth(0);
        tab.getColumnModel().getColumn(0).setMaxWidth(0);
        tab.getColumnModel().getColumn(1).setPreferredWidth(70);
        tab.getColumnModel().getColumn(1).setMaxWidth(70);
        tab.getColumnModel().getColumn(1).setCellRenderer(new ImagenMiniaturaRenderer());

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(mod);
        tab.setRowSorter(sorter);
        txtB.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { s(); }
            @Override public void removeUpdate(DocumentEvent e) { s(); }
            @Override public void changedUpdate(DocumentEvent e) { s(); }
            private void s() { sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtB.getText(), 2, 3)); }
        });

        List<Producto> lista = new InventarioDAO().listarProductosActivos();
        for (Producto p : lista) {
            mod.addRow(new Object[]{p.getIdProducto(), p.getImagen_producto(), p.getCodigoBarras(), p.getNombreProducto(), String.format("L %,.2f", p.getPrecioVenta()), p.getStockProducto()});
        }

        tab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int filaModelo = tab.convertRowIndexToModel(tab.getSelectedRow());
                    int stockActual = (int) mod.getValueAt(filaModelo, 5);
                    if (stockActual <= 0) {
                        utilidades.Mensajes.showMessageDialog(DialogoBuscarProductoSustituto.this, "No puede seleccionar este artículo porque no cuenta con existencias en el inventario.", "Falta de Stock", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int idSelec = (int) mod.getValueAt(filaModelo, 0);
                    productoSeleccionado = lista.stream().filter(p -> p.getIdProducto() == idSelec).findFirst().orElse(null);
                    dispose();
                }
            }
        });

        JScrollPane sc = new JScrollPane(tab);
        sc.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        sc.getViewport().setBackground(new Color(255, 255, 255));
        add(sc, BorderLayout.CENTER);
        
        timerShimmer = new Timer(30, e -> {
            if (!imagenesEnProceso.isEmpty()) {
                animacionShimmerPhase += 0.05f;
                if (animacionShimmerPhase > 1f) animacionShimmerPhase = 0f;
                tab.repaint();
            }
        });
        timerShimmer.start();
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    private class ImagenMiniaturaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            
            int filaModelo = table.convertRowIndexToModel(row);
            int idProducto = (int) table.getModel().getValueAt(filaModelo, 0);
            
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
                    label.setForeground(new Color(140, 145, 150)); 
                }
                return label;
            }
            
            if (!imagenesEnProceso.contains(idProducto)) {
                imagenesEnProceso.add(idProducto);
                
                SwingWorker<ImageIcon, Void> worker = new SwingWorker<>() {
                    @Override
                    protected ImageIcon doInBackground() throws Exception {
                        String imgVal = new InventarioDAO().obtenerRutaImagenBase64(idProducto);
                        if (imgVal == null || imgVal.trim().isEmpty()) {
                            if (utilidades.SesionGlobal.getEmpresaActual() != null && utilidades.SesionGlobal.getEmpresaActual().getImagen_logo() != null) {
                                imgVal = utilidades.SesionGlobal.getEmpresaActual().getImagen_logo();
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
                        return utilidades.ImagenHelper.obtenerIcono(valProcesar, 50, 50);
                    }
                    @Override
                    protected void done() {
                        try {
                            ImageIcon icon = get();
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

            JPanel panelShimmer = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    g2.setColor(new Color(235, 237, 240));
                    g2.fillRoundRect(getWidth()/2 - 25, getHeight()/2 - 25, 50, 50, 10, 10);
                    
                    int gradientWidth = 50;
                    int startX = (getWidth()/2 - 25) - gradientWidth + (int)(animacionShimmerPhase * (50 + gradientWidth * 2));
                    
                    Color c1 = new Color(255, 255, 255, 0);
                    Color c2 = new Color(255, 255, 255, 200);
                    
                    LinearGradientPaint paint = new LinearGradientPaint(
                            startX, 0, startX + gradientWidth, 0,
                            new float[]{0.0f, 0.5f, 1.0f},
                            new Color[]{c1, c2, c1}
                    );
                    
                    g2.setPaint(paint);
                    g2.fillRoundRect(getWidth()/2 - 25, getHeight()/2 - 25, 50, 50, 10, 10);
                    g2.dispose();
                }
            };
            panelShimmer.setOpaque(true);
            panelShimmer.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            return panelShimmer;
        }
    }
}

