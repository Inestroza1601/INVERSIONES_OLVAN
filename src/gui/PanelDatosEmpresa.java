package gui;

import modelo.Empresa;
import dao.EmpresaDAO;
import utilidades.SesionGlobal;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PanelDatosEmpresa extends JPanel {

    // --- Componentes de Control Superior ---
    private JComboBox<EmpresaComboItem> cmbEmpresas;
    private JTextField txtIdEmpresa;
    private JButton btnNuevaEmpresa;

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

    // --- Componentes para Impresion
    private PanelConfiguracionImpresion panelImpresion;

    // --- Botón de Acción ---
    private JButton btnGuardar;

    // --- VARIABLE CRÍTICA PARA ACTUALIZAR ---
    private int idEmpresaActual = 0;
    private boolean isCargandoCombo = false; // Evita disparos accidentales al llenar el combo

    public PanelDatosEmpresa() {
        iniciarDiseno();
    }

    private void iniciarDiseno() {
        this.removeAll();
        this.setLayout(new BorderLayout());
        this.setBackground(new Color(240, 242, 245)); // Gris Nube

        // =========================================================
        // 1. PANEL SUPERIOR (TÍTULO Y SELECTOR DE EMPRESA)
        // =========================================================
        JPanel pnlTop = new JPanel(new BorderLayout());
        pnlTop.setOpaque(false);

        JLabel lblTitulo = new JLabel("Configuración de Empresas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        pnlTop.add(lblTitulo, BorderLayout.NORTH);

        // Barra de control (Combo, ID, Botón Nuevo)
        JPanel pnlControl = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        pnlControl.setOpaque(false);
        pnlControl.setBorder(BorderFactory.createEmptyBorder(0, 15, 10, 20));

        JLabel lblSeleccion = new JLabel("Seleccionar Empresa:");
        lblSeleccion.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblSeleccion.setForeground(new Color(100, 100, 100));

        cmbEmpresas = new JComboBox<>();
        cmbEmpresas.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cmbEmpresas.setPreferredSize(new Dimension(300, 35));
        cmbEmpresas.setBackground(Color.WHITE);

        // Evento del ComboBox
        cmbEmpresas.addActionListener(e -> {
            if (!isCargandoCombo && cmbEmpresas.getSelectedItem() != null) {
                EmpresaComboItem item = (EmpresaComboItem) cmbEmpresas.getSelectedItem();
                cargarDatosEnFormulario(item.getEmpresa());
            }
        });

        JLabel lblId = new JLabel("ID:");
        lblId.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblId.setForeground(new Color(100, 100, 100));

        txtIdEmpresa = new JTextField(5);
        txtIdEmpresa.setFont(new Font("Segoe UI", Font.BOLD, 14));
        txtIdEmpresa.setHorizontalAlignment(SwingConstants.CENTER);
        txtIdEmpresa.setEditable(false); // INMODIFICABLE
        txtIdEmpresa.setBackground(new Color(230, 235, 240));
        txtIdEmpresa.setForeground(new Color(227, 0, 15)); // Resaltado en rojo
        txtIdEmpresa.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        txtIdEmpresa.setPreferredSize(new Dimension(50, 35));

        btnNuevaEmpresa = new JButton("+ Crear Nueva");
        btnNuevaEmpresa.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnNuevaEmpresa.setBackground(new Color(13, 110, 253)); // Azul
        btnNuevaEmpresa.setForeground(Color.WHITE);
        btnNuevaEmpresa.setFocusPainted(false);
        btnNuevaEmpresa.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        btnNuevaEmpresa.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNuevaEmpresa.addActionListener(e -> prepararNuevaEmpresa());

        pnlControl.add(lblSeleccion);
        pnlControl.add(cmbEmpresas);
        pnlControl.add(lblId);
        pnlControl.add(txtIdEmpresa);
        pnlControl.add(btnNuevaEmpresa);

        pnlTop.add(pnlControl, BorderLayout.CENTER);
        this.add(pnlTop, BorderLayout.NORTH);

        // =========================================================
        // 2. PESTAÑAS CENTRALES
        // =========================================================
        JTabbedPane pestanas = new JTabbedPane();
        pestanas.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        pestanas.setBackground(new Color(255, 255, 255));
        pestanas.setForeground(new Color(45, 45, 45));

        pestanas.addTab("Datos Generales", crearPanelGenerales());
        pestanas.addTab("Contacto y Redes", crearPanelContacto());
        panelImpresion = new PanelConfiguracionImpresion();
        pestanas.addTab("Impresión y Logo", panelImpresion);

        this.add(pestanas, BorderLayout.CENTER);

        // =========================================================
        // 3. PANEL INFERIOR (BOTÓN GUARDAR)
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

        panelInferior.add(btnGuardar);
        this.add(panelInferior, BorderLayout.SOUTH);

        // Cargar lista inicial
        cargarListaEmpresas(true);
    }

    // =========================================================
    // LÓGICA DE DATOS Y MULTI-EMPRESA
    // =========================================================

    private void cargarListaEmpresas(boolean seleccionarSesionActual) {
        isCargandoCombo = true;
        cmbEmpresas.removeAllItems();

        EmpresaDAO dao = new EmpresaDAO();
        // NOTA: Debes crear este método en tu EmpresaDAO para que devuelva un
        // List<Empresa>
        List<Empresa> lista = dao.listarTodas();

        for (Empresa emp : lista) {
            cmbEmpresas.addItem(new EmpresaComboItem(emp));
        }

        isCargandoCombo = false;

        // Seleccionar la empresa de la sesión actual (o la primera si no hay)
        if (cmbEmpresas.getItemCount() > 0 && seleccionarSesionActual) {
            int idSesion = (SesionGlobal.getEmpresaActual() != null) ? SesionGlobal.getEmpresaActual().getIdEmpresa()
                    : -1;
            boolean seleccionada = false;

            for (int i = 0; i < cmbEmpresas.getItemCount(); i++) {
                if (cmbEmpresas.getItemAt(i).getEmpresa().getIdEmpresa() == idSesion) {
                    cmbEmpresas.setSelectedIndex(i);
                    seleccionada = true;
                    break;
                }
            }
            if (!seleccionada)
                cmbEmpresas.setSelectedIndex(0); // Por defecto la primera
        }
    }

    private void cargarDatosEnFormulario(Empresa emp) {
        if (emp == null)
            return;

        this.idEmpresaActual = emp.getIdEmpresa();
        txtIdEmpresa.setText(String.valueOf(emp.getIdEmpresa()));

        txtNombreEmpresa.setText(emp.getNombreEmpresa());
        // ... (resto de tus textfields) ...
        txtFacebook.setText(emp.getFacebook());

        // --- ¡LA MAGIA OCURRE AQUÍ! ---
        // Le pasamos la empresa al panel de impresión en tiempo real
        panelImpresion.setEmpresaEnEdicion(emp);

        btnGuardar.setText("Actualizar Empresa");
        btnGuardar.setBackground(new Color(39, 174, 96)); // Verde Menta
    }

    private void prepararNuevaEmpresa() {
        // Deseleccionamos el combo temporalmente
        isCargandoCombo = true;
        cmbEmpresas.setSelectedIndex(-1);
        isCargandoCombo = false;

        this.idEmpresaActual = 0;
        txtIdEmpresa.setText("NUEVO");

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

        // Limpiamos también el panel de impresión/logo
        // panelImpresion.limpiarFormulario();

        btnGuardar.setText("Guardar Nueva Empresa");
        btnGuardar.setBackground(new Color(13, 110, 253)); // Azul
        txtNombreEmpresa.requestFocus();
    }

    private void guardarDatos() {
        if (txtNombreEmpresa.getText().trim().isEmpty() || txtRtnEmpresa.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "El Nombre de la Empresa y el RTN son obligatorios.", "Advertencia",
                    JOptionPane.WARNING_MESSAGE);
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

        EmpresaDAO dao = new EmpresaDAO();
        if (dao.guardarOActualizar(emp)) {
            JOptionPane.showMessageDialog(this, "Empresa guardada correctamente.", "Éxito",
                    JOptionPane.INFORMATION_MESSAGE);

            // Refrescar la lista del ComboBox
            cargarListaEmpresas(false);

            // Seleccionar automáticamente la empresa recién guardada/actualizada en el
            // ComboBox
            for (int i = 0; i < cmbEmpresas.getItemCount(); i++) {
                if (cmbEmpresas.getItemAt(i).getEmpresa().getNombreEmpresa().equalsIgnoreCase(emp.getNombreEmpresa())) {
                    cmbEmpresas.setSelectedIndex(i);
                    break;
                }
            }

            // Actualizamos la sesión global SÓLO si era la empresa que está usando el
            // usuario activo
            if (SesionGlobal.getEmpresaActual() != null
                    && SesionGlobal.getEmpresaActual().getIdEmpresa() == this.idEmpresaActual) {
                SesionGlobal.setEmpresaActual(emp);
            }

            // Mandar a guardar panel impresión (LOGO y tickets)
            // panelImpresion.guardarConfiguracion();
            // panelImpresion.recargarVistaPrevia();
        } else {
            JOptionPane.showMessageDialog(this, "Error al guardar la empresa en la base de datos.", "Error",
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

        chkHabilitarFacturacion = new JCheckBox("Habilitar Facturación (SAR)");
        chkHabilitarFacturacion.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        chkHabilitarFacturacion.setBackground(new Color(255, 255, 255));
        chkHabilitarFacturacion.setForeground(new Color(45, 45, 45));

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

        agregarFila(panel, gbc, 0, "Teléfono Principal:", txtNumeroTelefono);
        agregarFila(panel, gbc, 1, "Teléfono Secundario:", txtTelefonoSecundario);
        agregarFila(panel, gbc, 2, "WhatsApp:", txtWhatsapp);
        agregarFila(panel, gbc, 3, "Correo:", txtEmail);
        agregarFila(panel, gbc, 4, "Web:", txtWeb);
        agregarFila(panel, gbc, 5, "Facebook:", txtFacebook);

        gbc.gridy = 6;
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

    // =========================================================
    // CLASE WRAPPER PARA EL COMBOBOX
    // =========================================================
    private class EmpresaComboItem {
        private Empresa emp;

        public EmpresaComboItem(Empresa emp) {
            this.emp = emp;
        }

        public Empresa getEmpresa() {
            return emp;
        }

        @Override
        public String toString() {
            return emp.getNombreEmpresa();
        } // Esto es lo que mostrará el ComboBox
    }

    @SuppressWarnings("unchecked")
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
