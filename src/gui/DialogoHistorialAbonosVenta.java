package gui;

import dao.ApartadoDAO;
import modelo.AbonoApartado;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.List;

public class DialogoHistorialAbonosVenta extends JDialog {
    private JTable tablaAbonos;
    private DefaultTableModel modeloAbonos;
    private ApartadoDAO dao;

    public DialogoHistorialAbonosVenta(Frame parent, int idApartado) {
        super(parent, "Historial de Abonos - Apartado #" + idApartado, true);
        dao = new ApartadoDAO();
        initUI(idApartado);
    }

    private void initUI(int idApartado) {
        setSize(700, 400);
        setLocationRelativeTo(getParent());
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(245, 247, 250));

        JPanel pnlHeader = new JPanel(new BorderLayout());
        pnlHeader.setBackground(new Color(245, 247, 250));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(15, 20, 5, 20));

        JLabel lblTitulo = new JLabel("Detalle de Pagos / Abonos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitulo.setForeground(new Color(44, 62, 80));

        JLabel lblSub = new JLabel("Abonos recibidos para el Apartado #" + idApartado);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(Color.GRAY);

        pnlHeader.add(lblTitulo, BorderLayout.NORTH);
        pnlHeader.add(lblSub, BorderLayout.CENTER);
        add(pnlHeader, BorderLayout.NORTH);

        String[] cols = { "N°", "Fecha y Hora", "Método", "Cajero", "Monto Abonado" };
        modeloAbonos = new DefaultTableModel(null, cols) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        tablaAbonos = new JTable(modeloAbonos);
        tablaAbonos.setRowHeight(28);
        tablaAbonos.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        tablaAbonos.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        tablaAbonos.getTableHeader().setBackground(new Color(236, 240, 241));
        tablaAbonos.getTableHeader().setForeground(new Color(44, 62, 80));
        tablaAbonos.setShowGrid(false);
        tablaAbonos.setIntercellSpacing(new Dimension(0, 0));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        tablaAbonos.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tablaAbonos.getColumnModel().getColumn(0).setPreferredWidth(50);
        tablaAbonos.getColumnModel().getColumn(1).setPreferredWidth(130);

        JScrollPane scroll = new JScrollPane(tablaAbonos);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Color.WHITE);

        JPanel pnlTabla = new JPanel(new BorderLayout());
        pnlTabla.setBackground(Color.WHITE);
        pnlTabla.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(0, 20, 0, 20),
                BorderFactory.createLineBorder(new Color(220, 222, 225))));
        pnlTabla.add(scroll, BorderLayout.CENTER);
        add(pnlTabla, BorderLayout.CENTER);

        List<AbonoApartado> abonos = dao.listarAbonos(idApartado);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm a");
        double totalAbonos = 0;

        for (int i = 0; i < abonos.size(); i++) {
            AbonoApartado ab = abonos.get(i);
            totalAbonos += ab.getMontoAbono();
            modeloAbonos.addRow(new Object[] {
                    (i + 1), // Correlative N° instead of DB ID for cleaner look
                    sdf.format(ab.getFechaAbono()),
                    ab.getNombreMetodo(),
                    ab.getNombreUsuario(),
                    String.format("L %,.2f", ab.getMontoAbono())
            });
        }

        JPanel pnlFooter = new JPanel(new BorderLayout());
        pnlFooter.setBackground(new Color(245, 247, 250));
        pnlFooter.setBorder(BorderFactory.createEmptyBorder(10, 20, 15, 20));

        JLabel lblTotal = new JLabel("Total Abonado: L " + String.format("%,.2f", totalAbonos));
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTotal.setForeground(new Color(39, 174, 96));
        pnlFooter.add(lblTotal, BorderLayout.WEST);

        JButton btnCerrar = new JButton("Cerrar Historial");
        btnCerrar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCerrar.setFocusPainted(false);
        btnCerrar.addActionListener(e -> dispose());
        pnlFooter.add(btnCerrar, BorderLayout.EAST);

        add(pnlFooter, BorderLayout.SOUTH);
    }
}