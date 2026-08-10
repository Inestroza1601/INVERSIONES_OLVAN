package gui;

import modelo.Empresa;
import dao.EmpresaDAO;
import utilidades.SesionGlobal;

import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.File;

public class PanelConfiguracionImpresion extends JPanel {

    // --- Componentes de Impresión ---
    private JTextArea txtMensajeFactura;
    private JTextArea txtMensajeRecibo;
    private JTextArea txtMensajeEntrega;
    private JTextArea txtMensajeCotizacion;
    private JTextArea txtMensajeGarantia;
    private JTextArea txtMensajeCambio;
    private JTextArea txtMensajeReclamo;
    private String imagenLogoBase64 = null;
    private Empresa empresaActiva;
    private JPanel panelTarjetas;

    public PanelConfiguracionImpresion() {
        iniciarDiseno();
        cargarDatosActuales();
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(new Color(240, 242, 245)); // Gris Nube
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        // --- PANEL SUPERIOR (Título y Botón Guardar) ---
        JPanel panelSuperior = new JPanel(new BorderLayout());
        panelSuperior.setOpaque(false);

        JLabel lblTitulo = new JLabel("Diseño de Documentos");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        panelSuperior.add(lblTitulo, BorderLayout.WEST);

        JButton btnGuardarDiseno = new JButton("Guardar Textos y Logo");
        btnGuardarDiseno.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnGuardarDiseno.setForeground(Color.WHITE);
        btnGuardarDiseno.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardarDiseno.setFocusPainted(false);
        btnGuardarDiseno.putClientProperty("JButton.buttonType", "roundRect");
        btnGuardarDiseno.addActionListener(e -> guardarDiseno());
        panelSuperior.add(btnGuardarDiseno, BorderLayout.EAST);

        this.add(panelSuperior, BorderLayout.NORTH);

        // --- PANEL CENTRAL (Simulador de Tickets) ---
        txtMensajeFactura = crearTextAreaEditable("¡Gracias por su compra!");
        txtMensajeRecibo = crearTextAreaEditable("Este es un comprobante de pago.");
        txtMensajeEntrega = crearTextAreaEditable("Revise su equipo antes de salir.");
        txtMensajeCotizacion = crearTextAreaEditable("Cotización válida por 15 días.");
        txtMensajeGarantia = crearTextAreaEditable("Conserve este documento. La garantía no aplica por daños físicos, humedad, exposición a líquidos o manipulación por terceros.");
        txtMensajeCambio = crearTextAreaEditable("Este comprobante avala el cambio de su producto por garantía.");
        txtMensajeReclamo = crearTextAreaEditable("Su reclamo de garantía ha sido recibido y será procesado. Gracias.");
        CardLayout cardLayout = new CardLayout();
        panelTarjetas = new JPanel(cardLayout);
        panelTarjetas.setBackground(new Color(255, 255, 255)); // Blanco Puro
        panelTarjetas.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true)); // Gris muy claro

        panelTarjetas.add(utilidades.GeneradorTickets.crearTicketVistaPrevia("DOCUMENTO: FACTURA", txtMensajeFactura), "Factura");
        panelTarjetas.add(utilidades.GeneradorTickets.crearTicketVistaPrevia("DOCUMENTO: RECIBO", txtMensajeRecibo), "Recibo");
        panelTarjetas.add(utilidades.GeneradorTickets.crearTicketVistaPrevia("DOCUMENTO: ENTREGA", txtMensajeEntrega), "Entrega");
        panelTarjetas.add(utilidades.GeneradorTickets.crearTicketVistaPrevia("DOCUMENTO: COTIZACIÓN", txtMensajeCotizacion), "Cotizacion");
        panelTarjetas.add(utilidades.GeneradorTickets.crearTicketVistaPrevia("POLÍTICAS DE GARANTÍA", txtMensajeGarantia), "Garantia");
        panelTarjetas.add(utilidades.GeneradorTickets.crearTicketVistaPrevia("COMPROBANTE DE CAMBIO", txtMensajeCambio), "Cambio");
        panelTarjetas.add(utilidades.GeneradorTickets.crearTicketVistaPrevia("RECIBO DE RECLAMO", txtMensajeReclamo), "Reclamo");

        // --- PANEL INFERIOR (Botones de Control) ---
        JPanel panelBotonesControl = new JPanel(new java.awt.GridLayout(2, 1));
        
        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        fila1.setBackground(new Color(240, 242, 245));
        
        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        fila2.setBackground(new Color(240, 242, 245));

        JButton btnFactura = crearBotonEstiloPestana("Factura");
        JButton btnRecibo = crearBotonEstiloPestana("Recibo");
        JButton btnEntrega = crearBotonEstiloPestana("Entrega");
        JButton btnCotizacion = crearBotonEstiloPestana("Cotización");
        JButton btnGarantia = crearBotonEstiloPestana("Garantías");
        JButton btnCambio = crearBotonEstiloPestana("Cambio");
        JButton btnReclamo = crearBotonEstiloPestana("Reclamo");

        btnFactura.addActionListener(e -> cardLayout.show(panelTarjetas, "Factura"));
        btnRecibo.addActionListener(e -> cardLayout.show(panelTarjetas, "Recibo"));
        btnEntrega.addActionListener(e -> cardLayout.show(panelTarjetas, "Entrega"));
        btnCotizacion.addActionListener(e -> cardLayout.show(panelTarjetas, "Cotizacion"));
        btnGarantia.addActionListener(e -> cardLayout.show(panelTarjetas, "Garantia")); 
        btnCambio.addActionListener(e -> cardLayout.show(panelTarjetas, "Cambio")); 
        btnReclamo.addActionListener(e -> cardLayout.show(panelTarjetas, "Reclamo")); 

        JButton btnLogo = crearBotonEstiloPestana("Cargar Logo");
        btnLogo.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnLogo.setForeground(Color.WHITE);
        btnLogo.addActionListener(e -> cargarLogoDesdePC());

        // Separador visual
        JLabel lblSeparador = new JLabel(" | ");
        lblSeparador.setForeground(new Color(140, 145, 150)); 
        lblSeparador.setFont(new Font("Segoe UI", Font.BOLD, 16));

        JButton btnImpresoras = crearBotonEstiloPestana("Configurar Impresoras");
        btnImpresoras.setBackground(new Color(108, 117, 125)); 
        btnImpresoras.setForeground(Color.WHITE);
        btnImpresoras.addActionListener(e -> abrirDialogoImpresoras());

        fila1.add(btnFactura);
        fila1.add(btnRecibo);
        fila1.add(btnEntrega);
        fila1.add(btnCotizacion);
        fila1.add(btnGarantia);
        fila1.add(btnCambio);
        fila1.add(btnReclamo);
        
        fila2.add(btnLogo);
        fila2.add(lblSeparador);
        fila2.add(btnImpresoras);
        
        panelBotonesControl.add(fila1);
        panelBotonesControl.add(fila2);
        
        this.add(panelTarjetas, BorderLayout.CENTER);
        this.add(panelBotonesControl, BorderLayout.SOUTH);
    }

    // =====================================================================
    // MÉTODOS DE BASE DE DATOS Y LÓGICA
    // =====================================================================

    private void cargarDatosActuales() {
        Empresa emp = SesionGlobal.getEmpresaActual();
        if (emp != null) {
            if (emp.getMensajeTicketPieFactura() != null) txtMensajeFactura.setText(emp.getMensajeTicketPieFactura());
            if (emp.getMensajeTicketPieRecibo() != null) txtMensajeRecibo.setText(emp.getMensajeTicketPieRecibo());
            if (emp.getMensajeTicketEntrega() != null) txtMensajeEntrega.setText(emp.getMensajeTicketEntrega());
            if (emp.getMensajeTicketPieCotizacion() != null) txtMensajeCotizacion.setText(emp.getMensajeTicketPieCotizacion());
            if (emp.getPoliticasGarantia() != null) txtMensajeGarantia.setText(emp.getPoliticasGarantia());
            if (emp.getMensajeTicketCambio() != null) txtMensajeCambio.setText(emp.getMensajeTicketCambio());
            if (emp.getMensajeTicketReclamo() != null) txtMensajeReclamo.setText(emp.getMensajeTicketReclamo());
            
            if (emp.getImagen_logo() != null) imagenLogoBase64 = emp.getImagen_logo();
        }
    }

    private void guardarDiseno() {
        // Usamos la empresa que nos pasó el otro panel. Si es nula, intentamos con SesionGlobal.
        Empresa emp = (this.empresaActiva != null) ? this.empresaActiva : SesionGlobal.getEmpresaActual();
        
        if (emp == null) {
            utilidades.Mensajes.showMessageDialog(this, "No se encontró una empresa activa.\nPor favor, guarde primero los Datos Generales.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // ... EL RESTO DEL CÓDIGO QUEDA IGUAL (emp.setMensajeTicketPieFactura... etc)

       // Actualizamos los campos en el objeto
        emp.setMensajeTicketPieFactura(txtMensajeFactura.getText().trim());
        emp.setMensajeTicketPieRecibo(txtMensajeRecibo.getText().trim());
        emp.setMensajeTicketEntrega(txtMensajeEntrega.getText().trim());
        emp.setMensajeTicketPieCotizacion(txtMensajeCotizacion.getText().trim());
        emp.setPoliticasGarantia(txtMensajeGarantia.getText().trim());
        emp.setMensajeTicketCambio(txtMensajeCambio.getText().trim());
        emp.setMensajeTicketReclamo(txtMensajeReclamo.getText().trim());
        
        emp.setImagen_logo(imagenLogoBase64);

        EmpresaDAO dao = new EmpresaDAO();
        if (dao.guardarOActualizar(emp)) {
            utilidades.Mensajes.showMessageDialog(this, "Diseño y textos guardados correctamente.");
        } else {
            utilidades.Mensajes.showMessageDialog(this, "Error al guardar en la base de datos.");
        }
    }

    private void cargarLogoDesdePC() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleccionar Logo de la Empresa");
        FileNameExtensionFilter filtro = new FileNameExtensionFilter("Imágenes (PNG, JPG, JPEG)", "png", "jpg", "jpeg");
        chooser.setFileFilter(filtro);
        
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            imagenLogoBase64 = utilidades.ImagenHelper.comprimirYConvertirABase64(archivo);
            
            // USAR LA NUEVA VARIABLE AQUÍ TAMBIÉN
            Empresa emp = (this.empresaActiva != null) ? this.empresaActiva : SesionGlobal.getEmpresaActual();
            
            if (emp != null) {
                emp.setImagen_logo(imagenLogoBase64);
                EmpresaDAO dao = new EmpresaDAO();
                
                if (dao.guardarOActualizar(emp)) {
                    utilidades.Mensajes.showMessageDialog(this, "Logo cargado y guardado en la base de datos exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    recargarVistaPrevia(); 
                } else {
                    utilidades.Mensajes.showMessageDialog(this, "Error al guardar el logo en la base de datos.", "Error de Base de Datos", JOptionPane.ERROR_MESSAGE);
                }
            } else {
                utilidades.Mensajes.showMessageDialog(this, "No se encontró una empresa activa.\nPor favor, guarde primero los Datos Generales.", "Aviso", JOptionPane.WARNING_MESSAGE);
            }
        }
    }

    // =====================================================================
    // MÉTODOS DE DISEÑO Y COMPONENTES VISUALES
    // =====================================================================

    private JTextArea crearTextAreaEditable(String textoDefecto) {
        Border bordeEditable = BorderFactory.createDashedBorder(Color.LIGHT_GRAY, 3, 2);
        JTextArea txt = new JTextArea(textoDefecto);
        txt.setFont(new Font("Courier New", Font.PLAIN, 12));
        txt.setForeground(new Color(45, 45, 45)); 
        txt.setBackground(new Color(250, 250, 250)); 
        txt.setBorder(BorderFactory.createCompoundBorder(bordeEditable, BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setToolTipText("Haz clic aquí para editar el mensaje final");
        return txt;
    }

    private JButton crearBotonEstiloPestana(String texto) {
        JButton btn = new JButton(texto);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btn.setBackground(new Color(255, 255, 255)); 
        btn.setForeground(new Color(45, 45, 45)); 
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 222, 225)),
            BorderFactory.createEmptyBorder(14, 22, 14, 22)
        ));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }

    // =====================================================================
    // VENTANA EMERGENTE PARA SELECCIONAR IMPRESORAS
    // =====================================================================

    private void abrirDialogoImpresoras() {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) ventanaPadre, "Configuración de Impresoras", true);
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel pnlContenido = new JPanel(new GridBagLayout());
        pnlContenido.setBackground(new Color(255, 255, 255)); 
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JComboBox<String> cmbImpresoraTickets = new JComboBox<>();
        JComboBox<String> cmbImpresoraFacturasA4 = new JComboBox<>();
        
        PrintService[] printServices = PrintServiceLookup.lookupPrintServices(null, null);
        cmbImpresoraTickets.addItem("Seleccione una impresora...");
        cmbImpresoraFacturasA4.addItem("Seleccione una impresora...");
        for (PrintService printer : printServices) {
            cmbImpresoraTickets.addItem(printer.getName());
            cmbImpresoraFacturasA4.addItem(printer.getName());
        }

        gbc.gridy = 0;
        JLabel lblT = new JLabel("Impresora Térmica (Tickets):");
        lblT.setForeground(new Color(45, 45, 45)); 
        pnlContenido.add(lblT, gbc);
        
        gbc.gridy = 1;
        pnlContenido.add(cmbImpresoraTickets, gbc);

        gbc.gridy = 2;
        JLabel lblA4 = new JLabel("Impresora A4 (Documentos):");
        lblA4.setForeground(new Color(45, 45, 45)); 
        pnlContenido.add(lblA4, gbc);

        gbc.gridy = 3;
        pnlContenido.add(cmbImpresoraFacturasA4, gbc);

        // Pre-seleccionar impresoras guardadas
        String ticketGuardada = utilidades.GestorImpresion.obtenerImpresoraTicket();
        String a4Guardada = utilidades.GestorImpresion.obtenerImpresoraA4();
        
        if (!ticketGuardada.isEmpty()) {
            cmbImpresoraTickets.setSelectedItem(ticketGuardada);
        }
        if (!a4Guardada.isEmpty()) {
            cmbImpresoraFacturasA4.setSelectedItem(a4Guardada);
        }

        dialog.add(pnlContenido, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlBotones.setBackground(new Color(240, 242, 245)); 
        
        JButton btnGuardar = new JButton("Guardar Impresoras");
        btnGuardar.setBackground(new Color(39, 174, 96)); 
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.addActionListener(e -> {
            utilidades.GestorImpresion.guardarImpresoraTicket(cmbImpresoraTickets.getSelectedItem().toString());
            utilidades.GestorImpresion.guardarImpresoraA4(cmbImpresoraFacturasA4.getSelectedItem().toString());
            utilidades.Mensajes.showMessageDialog(dialog, "Impresoras seleccionadas guardadas localmente.");
            dialog.dispose();
        });

        pnlBotones.add(btnGuardar);
        dialog.add(pnlBotones, BorderLayout.SOUTH);

        dialog.setVisible(true);
    }
    
    public void recargarVistaPrevia() {
        panelTarjetas.removeAll(); 
        
        panelTarjetas.add(utilidades.GeneradorTickets.crearTicketVistaPrevia("DOCUMENTO: FACTURA", txtMensajeFactura), "Factura");
        panelTarjetas.add(utilidades.GeneradorTickets.crearTicketVistaPrevia("DOCUMENTO: RECIBO", txtMensajeRecibo), "Recibo");
        panelTarjetas.add(utilidades.GeneradorTickets.crearTicketVistaPrevia("DOCUMENTO: ENTREGA", txtMensajeEntrega), "Entrega");
        panelTarjetas.add(utilidades.GeneradorTickets.crearTicketVistaPrevia("DOCUMENTO: COTIZACIÓN", txtMensajeCotizacion), "Cotizacion");
        panelTarjetas.add(utilidades.GeneradorTickets.crearTicketVistaPrevia("POLÍTICAS DE GARANTÍA", txtMensajeGarantia), "Garantia");
        
        panelTarjetas.revalidate(); 
        panelTarjetas.repaint();    
    }
    
    // =====================================================================
    // MÉTODO DE CONEXIÓN CON PANEL DATOS EMPRESA
    // =====================================================================
    
    public void setEmpresaEnEdicion(Empresa emp) {
        this.empresaActiva = emp;

        if (emp != null) {
            txtMensajeFactura.setText(emp.getMensajeTicketPieFactura() != null ? emp.getMensajeTicketPieFactura() : "");
            txtMensajeRecibo.setText(emp.getMensajeTicketPieRecibo() != null ? emp.getMensajeTicketPieRecibo() : "");
            txtMensajeEntrega.setText(emp.getMensajeTicketEntrega() != null ? emp.getMensajeTicketEntrega() : "");
            txtMensajeCotizacion.setText(emp.getMensajeTicketPieCotizacion() != null ? emp.getMensajeTicketPieCotizacion() : "");
            txtMensajeGarantia.setText(emp.getPoliticasGarantia() != null ? emp.getPoliticasGarantia() : "");
            
            if (emp.getImagen_logo() != null) imagenLogoBase64 = emp.getImagen_logo();
            
            recargarVistaPrevia();
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

