package gui;

import modelo.Empresa;
import dao.EmpresaDAO;
import utilidades.SesionGlobal;
import javax.swing.*;
import java.awt.*;


public class PanelDatosEmpresa extends JPanel {

    // --- Componentes - Datos Generales ---
    private JTextField txtNombreEmpresa;
    private JTextField txtRtnEmpresa;
    private JTextField txtDuenoEmpresa;
    private JTextArea txtDireccionEmpresa;
    private JCheckBox chkEstadoEmpresa;
    private JCheckBox chkHabilitarFacturacion;

    // --- Componentes - Contacto y Redes ---
    private JTextField txtNumeroTelefono;
    private JTextField txtTelefonoSecundario;
    private JTextField txtWhatsapp;
    private JTextField txtEmail;
    private JTextField txtWeb;
    private JTextField txtFacebook;
    private JPasswordField txtApiKeyGemini;

    // --- Componentes para Impresion
    private PanelConfiguracionImpresion panelImpresion;

    // --- Bot\u00F3n de Acci\u00F3n ---
    private JButton btnGuardar;

    // --- VARIABLE CR\u00CDTICA PARA ACTUALIZAR ---
    private int idEmpresaActual = 0;

    public PanelDatosEmpresa() {
        iniciarDiseno();
    }

    private void iniciarDiseno() {
        this.removeAll();
        this.setLayout(new BorderLayout());
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); // Verde Vintage

        // =========================================================
        // 1. PANEL SUPERIOR (T\u00CDTULO Y SELECTOR DE EMPRESA)
        // =========================================================
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTitulo = new JLabel("Configuraci\u00F3n de Empresa");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(40, 20, 10, 20));
        pnlTop.add(lblTitulo, BorderLayout.NORTH);

        this.add(pnlTop, BorderLayout.NORTH);

        // =========================================================
        // 2. PESTA\u00D1AS CENTRALES
        // =========================================================
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pestanas.setBackground(new Color(255, 255, 255));
        pestanas.setForeground(new Color(45, 45, 45));

        pestanas.addTab("Datos Generales", crearPanelGenerales());
        pestanas.addTab("Contacto y Redes", crearPanelContacto());
        panelImpresion = new PanelConfiguracionImpresion();
        pestanas.addTab("Impresi\u00F3n y Logo", panelImpresion);

        this.add(pestanas, BorderLayout.CENTER);

        // =========================================================
        // 3. PANEL INFERIOR (BOT\u00D3N GUARDAR)
        // =========================================================
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panelInferior.setBackground(new Color(240, 242, 245));
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        btnGuardar = new JButton("Guardar Cambios");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setBackground(new Color(227, 0, 15));
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setPreferredSize(new Dimension(180, 40));
        btnGuardar.setBorder(BorderFactory.createEmptyBorder());
        btnGuardar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnGuardar.addActionListener(e -> guardarDatos());
        
        modelo.Usuario uAct = utilidades.SesionGlobal.getUsuarioActual();
        if (uAct != null && !uAct.tienePermiso("EDITAR_ADMINISTRACION")) {
            btnGuardar.setEnabled(false);
            btnGuardar.setToolTipText("No tienes permiso para editar los datos de la empresa.");
        }

        panelInferior.add(btnGuardar);
        this.add(panelInferior, BorderLayout.SOUTH);

        // Cargar datos de la única empresa
        cargarDatosEmpresa();
        
        agregarListenersRealTime();
    }

    private void agregarListenersRealTime() {
        javax.swing.event.DocumentListener dl = new javax.swing.event.DocumentListener() {
            public void insertUpdate(javax.swing.event.DocumentEvent e) { actualizarPreview(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { actualizarPreview(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { actualizarPreview(); }
        };
        txtNombreEmpresa.getDocument().addDocumentListener(dl);
        txtRtnEmpresa.getDocument().addDocumentListener(dl);
        txtDuenoEmpresa.getDocument().addDocumentListener(dl);
        txtDireccionEmpresa.getDocument().addDocumentListener(dl);
        txtNumeroTelefono.getDocument().addDocumentListener(dl);
        txtTelefonoSecundario.getDocument().addDocumentListener(dl);
        txtWhatsapp.getDocument().addDocumentListener(dl);
        txtEmail.getDocument().addDocumentListener(dl);
        txtWeb.getDocument().addDocumentListener(dl);
        txtFacebook.getDocument().addDocumentListener(dl);
    }

    private void actualizarPreview() {
        Empresa emp = new Empresa();
        emp.setIdEmpresa(this.idEmpresaActual);
        emp.setNombreEmpresa(txtNombreEmpresa.getText().trim());
        emp.setRtnEmpresa(txtRtnEmpresa.getText().trim());
        emp.setDuenoEmpresa(txtDuenoEmpresa.getText().trim());
        emp.setDireccionEmpresa(txtDireccionEmpresa.getText().trim());
        emp.setNumeroTelefono(txtNumeroTelefono.getText().trim());
        emp.setTelefonoSecundario(txtTelefonoSecundario.getText().trim());
        emp.setWhatsapp(txtWhatsapp.getText().trim());
        emp.setEmail(txtEmail.getText().trim());
        emp.setWeb(txtWeb.getText().trim());
        emp.setFacebook(txtFacebook.getText().trim());

        panelImpresion.actualizarSoloDatosEmpresa(emp);
    }

    // =========================================================
    // L\u00D3GICA DE DATOS
    // =========================================================

    private void cargarDatosEmpresa() {
        EmpresaDAO dao = new EmpresaDAO();
        Empresa emp = dao.obtenerDatos();

        if (emp != null) {
            cargarDatosEnFormulario(emp);
        } else {
            // No existe empresa registrada, preparamos formulario limpio
            prepararNuevaEmpresa();
        }
    }

    private void cargarDatosEnFormulario(Empresa emp) {
        if (emp == null)
            return;

        this.idEmpresaActual = emp.getIdEmpresa();

        txtNombreEmpresa.setText(emp.getNombreEmpresa());
        txtRtnEmpresa.setText(emp.getRtnEmpresa());
        txtDuenoEmpresa.setText(emp.getDuenoEmpresa());
        txtDireccionEmpresa.setText(emp.getDireccionEmpresa());
        chkEstadoEmpresa.setSelected(emp.isEstadoEmpresa());
        chkHabilitarFacturacion.setSelected(emp.isHabilitarFacturacion());

        txtNumeroTelefono.setText(emp.getNumeroTelefono());
        txtTelefonoSecundario.setText(emp.getTelefonoSecundario());
        txtWhatsapp.setText(emp.getWhatsapp());
        txtEmail.setText(emp.getEmail());
        txtWeb.setText(emp.getWeb());
        txtFacebook.setText(emp.getFacebook());
        txtApiKeyGemini.setText(emp.getApiKeyGemini() != null ? emp.getApiKeyGemini() : "");

        // --- ¡LA MAGIA OCURRE AQUÍ! ---
        // Le pasamos la empresa al panel de impresión en tiempo real
        panelImpresion.setEmpresaEnEdicion(emp);

        btnGuardar.setText("Actualizar Datos");
        btnGuardar.setBackground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO);
    }

    private void prepararNuevaEmpresa() {
        this.idEmpresaActual = 0;

        txtNombreEmpresa.setText("");
        txtRtnEmpresa.setText("");
        txtDuenoEmpresa.setText("");
        txtDireccionEmpresa.setText("");
        chkEstadoEmpresa.setSelected(true); // Activa por defecto
        chkHabilitarFacturacion.setSelected(false);

        txtNumeroTelefono.setText("");
        txtTelefonoSecundario.setText("");
        txtWhatsapp.setText("");
        txtEmail.setText("");
        txtWeb.setText("");
        txtFacebook.setText("");
        txtApiKeyGemini.setText("");

        btnGuardar.setText("Guardar Nueva Empresa");
        btnGuardar.setBackground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO);
        txtNombreEmpresa.requestFocus();
    }

    private void guardarDatos() {
        if (txtNombreEmpresa.getText().trim().isEmpty() || txtRtnEmpresa.getText().trim().isEmpty()) {
            utilidades.Mensajes.showMessageDialog(this, "El Nombre de la Empresa y el RTN son obligatorios.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int opcion = utilidades.Mensajes.showConfirmDialog(this,
                "¿Está seguro de que desea guardar/actualizar los datos de la empresa?",
                "Confirmar Actualización", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        Empresa emp = new Empresa();
        emp.setIdEmpresa(this.idEmpresaActual);
        emp.setNombreEmpresa(txtNombreEmpresa.getText().trim());
        emp.setRtnEmpresa(txtRtnEmpresa.getText().trim());
        emp.setDuenoEmpresa(txtDuenoEmpresa.getText().trim());
        emp.setDireccionEmpresa(txtDireccionEmpresa.getText().trim());
        emp.setEstadoEmpresa(chkEstadoEmpresa.isSelected());
        emp.setHabilitarFacturacion(chkHabilitarFacturacion.isSelected());

        emp.setNumeroTelefono(txtNumeroTelefono.getText().trim());
        emp.setTelefonoSecundario(txtTelefonoSecundario.getText().trim());
        emp.setWhatsapp(txtWhatsapp.getText().trim());
        emp.setEmail(txtEmail.getText().trim());
        emp.setWeb(txtWeb.getText().trim());
        emp.setFacebook(txtFacebook.getText().trim());
        emp.setApiKeyGemini(new String(txtApiKeyGemini.getPassword()).trim());

        EmpresaDAO dao = new EmpresaDAO();
        if (dao.guardarOActualizar(emp)) {
            utilidades.Mensajes.showMessageDialog(this, "Empresa guardada correctamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

            // Refrescar los datos cargados en memoria tras guardar
            cargarDatosEmpresa();

            // Actualizamos la sesión global
            SesionGlobal.setEmpresaActual(emp);
        } else {
            utilidades.Mensajes.showMessageDialog(this, "Error al guardar la empresa en la base de datos.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // =========================================================
    // CREACIÓN DE PANELES Y ESTILOS
    // =========================================================

    private JTextField crearTextField(int columnas) {
        JTextField txt = new JTextField(columnas);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txt.setBackground(new Color(255, 255, 255));
        txt.setForeground(new Color(45, 45, 45));
        txt.setCaretColor(new Color(45, 45, 45));
        txt.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 222, 225)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return txt;
    }

    private JPanel crearPanelGenerales() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);

        txtNombreEmpresa = crearTextField(35);
        txtRtnEmpresa = crearTextField(15);
        txtRtnEmpresa.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyTyped(java.awt.event.KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar())) {
                    e.consume();
                }
            }
        });
        txtDuenoEmpresa = crearTextField(35);

        txtDireccionEmpresa = new JTextArea(4, 35);
        txtDireccionEmpresa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtDireccionEmpresa.setBackground(new Color(255, 255, 255));
        txtDireccionEmpresa.setForeground(new Color(45, 45, 45));
        txtDireccionEmpresa.setCaretColor(new Color(45, 45, 45));
        txtDireccionEmpresa.setLineWrap(true);
        txtDireccionEmpresa.setWrapStyleWord(true);

        JScrollPane scrollDir = new JScrollPane(txtDireccionEmpresa);
        scrollDir.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));

        chkEstadoEmpresa = new JCheckBox("Empresa Activa");
        chkEstadoEmpresa.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkEstadoEmpresa.setBackground(new Color(255, 255, 255));
        chkEstadoEmpresa.setForeground(new Color(45, 45, 45));
        chkEstadoEmpresa.setVisible(false);

        chkHabilitarFacturacion = new JCheckBox("Habilitar Facturación (SAR)");
        chkHabilitarFacturacion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkHabilitarFacturacion.setBackground(new Color(255, 255, 255));
        chkHabilitarFacturacion.setForeground(new Color(45, 45, 45));
        chkHabilitarFacturacion.setVisible(false);

        agregarFila(panel, gbc, 0, "Nombre Empresa:", txtNombreEmpresa);
        agregarFila(panel, gbc, 1, "RTN:", txtRtnEmpresa);
        agregarFila(panel, gbc, 2, "Propietario:", txtDuenoEmpresa);

        gbc.gridy = 3;
        gbc.gridx = 0;
        gbc.anchor = GridBagConstraints.NORTHEAST;
        JLabel lblDir = new JLabel("Dirección:");
        lblDir.setForeground(new Color(100, 100, 100));
        lblDir.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(lblDir, gbc);

        gbc.gridx = 1;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(scrollDir, gbc);

        gbc.gridx = 1;
        gbc.gridy = 4;
        panel.add(chkEstadoEmpresa, gbc);
        gbc.gridy = 5;
        panel.add(chkHabilitarFacturacion, gbc);
        gbc.gridy = 6;
        gbc.weighty = 1.0;
        panel.add(new JLabel(""), gbc);

        return panel;
    }

    private JPanel crearPanelContacto() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(new Color(255, 255, 255));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 15, 10, 15);

        txtNumeroTelefono = crearTextField(15);
        txtTelefonoSecundario = crearTextField(15);
        txtWhatsapp = crearTextField(15);
        txtEmail = crearTextField(30);
        txtWeb = crearTextField(30);
        txtFacebook = crearTextField(30);
        
        txtApiKeyGemini = new JPasswordField(30);
        txtApiKeyGemini.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtApiKeyGemini.setBackground(new Color(255, 255, 255));
        txtApiKeyGemini.setForeground(new Color(45, 45, 45));
        txtApiKeyGemini.setCaretColor(new Color(45, 45, 45));
        txtApiKeyGemini.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 222, 225)),
                BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        txtApiKeyGemini.putClientProperty("JPasswordField.cutCopyAllowed", false);

        agregarFila(panel, gbc, 0, "Teléfono Principal:", txtNumeroTelefono);
        agregarFila(panel, gbc, 1, "Teléfono Secundario:", txtTelefonoSecundario);
        agregarFila(panel, gbc, 2, "WhatsApp:", txtWhatsapp);
        agregarFila(panel, gbc, 3, "Correo:", txtEmail);
        agregarFila(panel, gbc, 4, "Web:", txtWeb);
        agregarFila(panel, gbc, 5, "Facebook:", txtFacebook);
        agregarFila(panel, gbc, 6, "API Key (Gemini):", txtApiKeyGemini);

        gbc.gridy = 7;
        gbc.weighty = 1.0;
        gbc.gridx = 0;
        panel.add(new JLabel(""), gbc);

        return panel;
    }

    private void agregarFila(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComponent campo) {
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.EAST;
        JLabel label = new JLabel(etiqueta);
        label.setForeground(new Color(100, 100, 100));
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(campo, gbc);
    }

    @SuppressWarnings("unused")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 400, Short.MAX_VALUE));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGap(0, 300, Short.MAX_VALUE));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}

