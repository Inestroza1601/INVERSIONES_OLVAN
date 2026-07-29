package gui;

import dao.InventarioDefectuosoDAO;
import utilidades.GeneradorTickets;
import utilidades.SesionGlobal;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class DialogoEntregarDefectuoso extends JDialog {

    private int idProducto;
    private String nombreProducto;
    private String estadoActual;
    private String cliente;
    private InventarioDefectuosoDAO dao;
    private boolean exito = false;

    private JTextArea txtObservaciones;
    private JButton btnConfirmar;
    private JButton btnCancelar;

    public DialogoEntregarDefectuoso(Window owner, int idProducto, String nombreProducto, String estadoActual, String cliente) {
        super(owner, "Entregar Producto Reparado", ModalityType.APPLICATION_MODAL);
        this.idProducto = idProducto;
        this.nombreProducto = nombreProducto;
        this.estadoActual = estadoActual;
        this.cliente = cliente;
        this.dao = new InventarioDefectuosoDAO();

        iniciarDiseno();
    }

    private void iniciarDiseno() {
        this.setSize(500, 500);
        this.setLocationRelativeTo(getOwner());
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(Color.WHITE);

        // Header Panel
        JPanel pnlHead = new JPanel(new BorderLayout());
        pnlHead.setBackground(new Color(245, 247, 250));
        pnlHead.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel lblTitle = new JLabel("Entrega al Cliente");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(45, 45, 45));

        JLabel lblSub = new JLabel("Producto: " + nombreProducto + " | Propietario: " + cliente);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(100, 100, 100));

        pnlHead.add(lblTitle, BorderLayout.NORTH);
        pnlHead.add(lblSub, BorderLayout.SOUTH);

        // Body Panel
        JPanel pnlBody = new JPanel();
        pnlBody.setLayout(new BoxLayout(pnlBody, BoxLayout.Y_AXIS));
        pnlBody.setBackground(Color.WHITE);
        pnlBody.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));
        
        // Historial Timeline
        JLabel lblHistorial = new JLabel("Historial de Movimientos:");
        lblHistorial.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblHistorial.setForeground(new Color(60, 60, 60));
        lblHistorial.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        java.util.List<java.util.Map<String, Object>> detalles = dao.obtenerDetallesPorProductoYEstado(idProducto, estadoActual);
        StringBuilder timeline = new StringBuilder("<html><div style='font-size:12px; color:#444; margin-bottom:10px;'>");
        if (!detalles.isEmpty()) {
            java.util.Map<String, Object> d = detalles.get(0);
            timeline.append("• <b>Ingreso:</b> ").append(d.get("fecha")).append("<br>");
            if (d.get("fecha_envio") != null) {
                timeline.append("• <b>Envío a Proveedor:</b> ").append(d.get("fecha_envio")).append("<br>");
            }
            if (d.get("fecha_recibido") != null) {
                timeline.append("• <b>Recepción de Proveedor:</b> ").append(d.get("fecha_recibido")).append("<br>");
            }
        } else {
            timeline.append("No hay registros de historial disponibles.");
        }
        timeline.append("</div></html>");
        
        JLabel lblTimeline = new JLabel(timeline.toString());
        lblTimeline.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblObs = new JLabel("Observaciones de la Reparación / Notas del Proveedor:");
        lblObs.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblObs.setForeground(new Color(60, 60, 60));
        lblObs.setAlignmentX(Component.LEFT_ALIGNMENT);

        txtObservaciones = new JTextArea(3, 20);
        txtObservaciones.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtObservaciones.setLineWrap(true);
        txtObservaciones.setWrapStyleWord(true);
        txtObservaciones.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200)),
                BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JScrollPane scrollObs = new JScrollPane(txtObservaciones);
        scrollObs.setAlignmentX(Component.LEFT_ALIGNMENT);

        JLabel lblInfo = new JLabel("<html><i style='color:#7f8c8d; font-size:11px'>Al confirmar, se registrará la entrega y se imprimirá un comprobante.</i></html>");
        lblInfo.setAlignmentX(Component.LEFT_ALIGNMENT);

        pnlBody.add(lblHistorial);
        pnlBody.add(Box.createVerticalStrut(5));
        pnlBody.add(lblTimeline);
        pnlBody.add(Box.createVerticalStrut(15));
        pnlBody.add(lblObs);
        pnlBody.add(Box.createVerticalStrut(5));
        pnlBody.add(scrollObs);
        pnlBody.add(Box.createVerticalStrut(15));
        pnlBody.add(lblInfo);

        // Footer Panel
        JPanel pnlFoot = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlFoot.setBackground(new Color(245, 247, 250));
        pnlFoot.setBorder(new EmptyBorder(5, 10, 5, 10));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnCancelar.setBackground(Color.WHITE);
        btnCancelar.setForeground(new Color(80, 80, 80));
        btnCancelar.setFocusPainted(false);
        btnCancelar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnCancelar.addActionListener(e -> dispose());

        btnConfirmar = new JButton("Confirmar e Imprimir");
        btnConfirmar.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnConfirmar.setBackground(new Color(39, 174, 96));
        btnConfirmar.setForeground(Color.WHITE);
        btnConfirmar.setFocusPainted(false);
        btnConfirmar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnConfirmar.addActionListener(e -> procesarEntrega());

        pnlFoot.add(btnCancelar);
        pnlFoot.add(btnConfirmar);

        this.add(pnlHead, BorderLayout.NORTH);
        this.add(pnlBody, BorderLayout.CENTER);
        this.add(pnlFoot, BorderLayout.SOUTH);
    }

    private void procesarEntrega() {
        String obs = txtObservaciones.getText().trim();
        if (obs.isEmpty()) {
            obs = "Ninguna observación adicional.";
        }

        if (dao.entregarCliente(idProducto, estadoActual)) {
            this.exito = true;
            GeneradorTickets.imprimirTicketEntregaReparacion(nombreProducto, obs);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Ocurrió un error al procesar la entrega.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean isExito() {
        return exito;
    }
}
