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

    public DialogoBuscarProductoSustituto(Window parent) {
        super(parent, "Catálogo Rápido - Selección de Sustituto", Dialog.ModalityType.APPLICATION_MODAL);
        setSize(600, 500);
        setLocationRelativeTo(parent);
        getContentPane().setBackground(new Color(240, 242, 245));
        setLayout(new BorderLayout(10, 10));

        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setBackground(new Color(240, 242, 245));
        pnlTop.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JTextField txtB = new JTextField();
        txtB.setBackground(new Color(255, 255, 255));
        txtB.setForeground(new Color(45, 45, 45));
        txtB.setCaretColor(new Color(45, 45, 45));
        txtB.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)),
                BorderFactory.createEmptyBorder(0, 5, 0, 5)));
        txtB.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtB.putClientProperty("JTextField.placeholderText", "Buscar por Nombre o Código...");
        pnlTop.add(txtB, BorderLayout.CENTER);
        add(pnlTop, BorderLayout.NORTH);

        String[] cols = { "ID", "Foto", "Código", "Producto", "Precio", "Stock" };
        DefaultTableModel mod = new DefaultTableModel(null, cols) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        JTable tab = new JTable(mod) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row)) {
                    Object valorStock = getValueAt(row, 5);
                    int stock = 1;
                    if (valorStock != null) {
                        try {
                            stock = Integer.parseInt(valorStock.toString());
                        } catch (NumberFormatException e) {
                        }
                    }
                    if (stock <= 0)
                        c.setForeground(new Color(227, 0, 15));
                    else
                        c.setForeground(new Color(45, 45, 45));
                    c.setBackground(new Color(255, 255, 255));
                }
                return c;
            }
        };

        tab.setBackground(new Color(255, 255, 255));
        tab.setForeground(new Color(45, 45, 45));
        tab.setRowHeight(60);
        tab.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        tab.setSelectionBackground(new Color(230, 235, 240));
        tab.setSelectionForeground(new Color(45, 45, 45));
        tab.getTableHeader().setBackground(new Color(240, 242, 245));
        tab.getTableHeader().setForeground(new Color(100, 100, 100));
        tab.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 222, 225)));
        tab.getTableHeader().setPreferredSize(new Dimension(0, 35));

        tab.getColumnModel().getColumn(0).setMinWidth(0);
        tab.getColumnModel().getColumn(0).setMaxWidth(0);
        tab.getColumnModel().getColumn(1).setPreferredWidth(70);
        tab.getColumnModel().getColumn(1).setMaxWidth(70);
        tab.getColumnModel().getColumn(1).setCellRenderer(new ImagenMiniaturaRenderer());

        TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(mod);
        tab.setRowSorter(sorter);
        txtB.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                s();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                s();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                s();
            }

            private void s() {
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + txtB.getText(), 2, 3));
            }
        });

        List<Producto> lista = new InventarioDAO().listarProductosActivos();
        for (Producto p : lista) {
            mod.addRow(new Object[] { p.getIdProducto(), p.getRutaImagen(), p.getCodigoBarras(), p.getNombreProducto(),
                    String.format("L %,.2f", p.getPrecioVenta()), p.getStockProducto() });
        }

        tab.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int filaModelo = tab.convertRowIndexToModel(tab.getSelectedRow());
                    int stockActual = (int) mod.getValueAt(filaModelo, 5);
                    if (stockActual <= 0) {
                        JOptionPane.showMessageDialog(DialogoBuscarProductoSustituto.this,
                                "No puede seleccionar este artículo porque no cuenta con existencias en el inventario.",
                                "Falta de Stock", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    int idSelec = (int) mod.getValueAt(filaModelo, 0);
                    productoSeleccionado = lista.stream().filter(p -> p.getIdProducto() == idSelec).findFirst()
                            .orElse(null);
                    dispose();
                }
            }
        });

        JScrollPane sc = new JScrollPane(tab);
        sc.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        sc.getViewport().setBackground(new Color(255, 255, 255));
        add(sc, BorderLayout.CENTER);
    }

    public Producto getProductoSeleccionado() {
        return productoSeleccionado;
    }

    private class ImagenMiniaturaRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            JLabel label = new JLabel();
            label.setHorizontalAlignment(SwingConstants.CENTER);
            label.setOpaque(true);
            label.setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());

            String imgVal = (value != null) ? value.toString() : null;

            if (imgVal == null || imgVal.trim().isEmpty()) {
                if (utilidades.SesionGlobal.getEmpresaActual() != null
                        && utilidades.SesionGlobal.getEmpresaActual().getLogoEmpresaRuta() != null) {
                    imgVal = utilidades.SesionGlobal.getEmpresaActual().getLogoEmpresaRuta();
                }
            }

            ImageIcon icon = utilidades.ImagenHelper.obtenerIcono(imgVal, 50, 50);
            if (icon != null) {
                label.setIcon(icon);
            } else {
                label.setText("No Img");
                label.setForeground(new Color(140, 145, 150));
            }
            return label;
        }
    }
}