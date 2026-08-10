package gui;

import modelo.Usuario;
import dao.UsuarioDAO;
import utilidades.Seguridad;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class PanelGestionUsuarios extends JPanel {

    // --- Componentes del Formulario ---
    private JPanel panelForm; 
    private JTextField txtIdentidad;
    private JTextField txtNombre;
    private JTextField txtEmail;
    private JComboBox<String> cmbRol;
    private JCheckBox chkAccesoSistema;
    private JPasswordField txtPassword;
    private JLabel lblPassword; 
    
    // --- Botones ---
    private JButton btnGuardar;
    private JButton btnEliminar;
    private JButton btnLimpiar;
    private JButton btnNuevoRol;
    
    // --- Tabla ---
    private JTable tablaUsuarios;
    private DefaultTableModel modeloTabla;

    public PanelGestionUsuarios() {
        initComponents();
        iniciarDiseno();
    }

    private void iniciarDiseno() {
        this.removeAll();
        this.setLayout(new BorderLayout(15, 15));
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); // Verde Vintage
        this.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Título Superior
        JLabel lblTitulo = new JLabel("Gestión y Roles de Usuarios");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        this.add(lblTitulo, BorderLayout.NORTH);

        this.add(crearPanelFormulario(), BorderLayout.WEST);
        this.add(crearPanelTabla(), BorderLayout.CENTER);
        
        // --- EVENTOS DE BOTONES ---
        btnGuardar.addActionListener(e -> guardarOActualizarUsuario());
        btnLimpiar.addActionListener(e -> limpiarFormulario());
        btnEliminar.addActionListener(e -> desactivarUsuarioSeleccionado());

        tablaUsuarios.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && tablaUsuarios.getSelectedRow() != -1) {
                cargarDatosEnFormulario();
            }
        });
        
        cargarRolesEnCombo();
        cargarTabla();
        limpiarFormulario();
    }

    private JPanel crearPanelFormulario() {
        panelForm = new JPanel(new GridBagLayout());
        panelForm.setBackground(new Color(255, 255, 255)); // Blanco puro
        panelForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 222, 225)), // Gris muy claro para borde
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Inicializar campos básicos
        txtIdentidad = new JTextField(15);
        txtIdentidad.setEditable(false); 
        txtIdentidad.setBackground(new Color(240, 242, 245)); // Gris Nube para campo inactivo
        txtIdentidad.setForeground(new Color(140, 145, 150)); // Gris suave
        txtIdentidad.setFont(new Font("Segoe UI", Font.BOLD, 12));
        txtIdentidad.setHorizontalAlignment(JTextField.CENTER);
        
        txtNombre = new JTextField(20);
        
        txtEmail = new JTextField(20);
        
        // --- 🚀 COMBOBOX Y BOTÓN NUEVO ROL (UNIFICADO) ---
        cmbRol = new JComboBox<>();
        // cmbRol.setEditable(true); // Deshabilitado para que solo se pueda seleccionar

        JPanel panelContenedorRol = new JPanel(new BorderLayout(5, 0));
        panelContenedorRol.setOpaque(false);

        btnNuevoRol = new JButton("+");
        btnNuevoRol.setBackground(Color.WHITE);
        btnNuevoRol.setForeground(new Color(227, 0, 15)); // Rojo Logo
        btnNuevoRol.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnNuevoRol.setFocusPainted(false);
        btnNuevoRol.setPreferredSize(new Dimension(40, 30));
        btnNuevoRol.addActionListener(e -> crearNuevoRol());

        modelo.Usuario uAct = utilidades.SesionGlobal.getUsuarioActual();
        if (uAct != null && !uAct.tienePermiso("CREAR_ADMINISTRACION")) {
            btnNuevoRol.setEnabled(false);
            btnNuevoRol.setToolTipText("No tienes permiso para crear roles.");
        }

        panelContenedorRol.add(cmbRol, BorderLayout.CENTER);
        panelContenedorRol.add(btnNuevoRol, BorderLayout.EAST);

        // --- CHECKBOX Y CONTRASEÑA ---
        chkAccesoSistema = new JCheckBox("Usuario con acceso al sistema");
        chkAccesoSistema.setBackground(new Color(255, 255, 255)); // Blanco puro
        chkAccesoSistema.setForeground(new Color(45, 45, 45)); // Gris oscuro
        chkAccesoSistema.setSelected(true); 

        txtPassword = new JPasswordField(20);
        lblPassword = new JLabel("Contraseña:");
        lblPassword.setForeground(new Color(45, 45, 45)); // Gris oscuro

        // --- 🔒 LÓGICA DE SEGURIDAD PARA ADMINISTRADOR ---
        cmbRol.addActionListener(e -> {
            if (cmbRol.getSelectedItem() != null) {
                String rolSeleccionado = cmbRol.getSelectedItem().toString().toLowerCase();
                if (rolSeleccionado.contains("admin")) {
                    chkAccesoSistema.setSelected(true);
                    chkAccesoSistema.setEnabled(false); // Bloqueado
                    lblPassword.setVisible(true);
                    txtPassword.setVisible(true);
                } else {
                    chkAccesoSistema.setEnabled(true); // Desbloqueado para otros roles
                    boolean tieneAcceso = chkAccesoSistema.isSelected();
                    lblPassword.setVisible(tieneAcceso);
                    txtPassword.setVisible(tieneAcceso);
                }
                panelForm.revalidate();
                panelForm.repaint();
            }
        });

        // --- LÓGICA DINÁMICA DEL CHECKBOX ---
        chkAccesoSistema.addActionListener(e -> {
            boolean tieneAcceso = chkAccesoSistema.isSelected();
            lblPassword.setVisible(tieneAcceso);
            txtPassword.setVisible(tieneAcceso);
            if (!tieneAcceso) {
                txtPassword.setText(""); 
            }
            panelForm.revalidate(); 
            panelForm.repaint();
        });

        // Agregar al layout (MIRA CÓMO AHORA SOLO AGREGAMOS panelContenedorRol)
        int fila = 0;
        agregarFilaFormulario(panelForm, gbc, fila++, "ID Usuario:", txtIdentidad);
        agregarFilaFormulario(panelForm, gbc, fila++, "Nombre Completo:", txtNombre);
        agregarFilaFormulario(panelForm, gbc, fila++, "Correo Electrónico:", txtEmail);
        agregarFilaFormulario(panelForm, gbc, fila++, "Rol del Usuario:", panelContenedorRol);
        
        gbc.gridy = fila++; gbc.gridx = 0; gbc.gridwidth = 2;
        panelForm.add(chkAccesoSistema, gbc);
        
        gbc.gridwidth = 1;
        gbc.gridy = fila++;
        gbc.gridx = 0;
        panelForm.add(lblPassword, gbc);
        gbc.gridx = 1;
        panelForm.add(txtPassword, gbc);

        // Panel de botones internos
        JPanel pnlBotones = new JPanel(new GridLayout(1, 3, 10, 0));
        pnlBotones.setBackground(new Color(255, 255, 255)); // Blanco puro
        
        btnGuardar = crearBotonFormulario("Guardar", new Color(39, 174, 96)); // Verde Menta
        btnLimpiar = crearBotonFormulario("Limpiar", new Color(140, 145, 150)); // Gris suave
        btnEliminar = crearBotonFormulario("Desactivar", new Color(227, 0, 15)); // Rojo Logo
        
        if (uAct != null && !uAct.tienePermiso("EDITAR_ADMINISTRACION") && !uAct.tienePermiso("CREAR_ADMINISTRACION")) {
            btnGuardar.setEnabled(false);
            btnGuardar.setToolTipText("No tienes permiso para gestionar usuarios.");
        }
        
        if (uAct != null && !uAct.tienePermiso("ELIMINAR_ADMINISTRACION")) {
            btnEliminar.setEnabled(false);
            btnEliminar.setToolTipText("No tienes permiso para desactivar usuarios.");
        }
        
        pnlBotones.add(btnGuardar);
        pnlBotones.add(btnLimpiar);
        pnlBotones.add(btnEliminar);

        gbc.gridy = fila++; gbc.gridx = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        panelForm.add(pnlBotones, gbc);
        
        gbc.gridy = fila++; gbc.weighty = 1.0;
        panelForm.add(new JLabel(""), gbc);

        return panelForm;
    }

    private JPanel crearPanelTabla() {
        JPanel panelTabla = new JPanel(new BorderLayout());
        panelTabla.setBackground(new Color(240, 242, 245)); // Gris Nube

        String[] columnas = {"ID", "Nombre", "Rol", "Correo", "Acceso", "Estado"};
        modeloTabla = new DefaultTableModel(null, columnas) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; } 
        };
        
        tablaUsuarios = new JTable(modeloTabla);
        tablaUsuarios.setRowHeight(30);
        tablaUsuarios.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 14));
        tablaUsuarios.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(tablaUsuarios);
        panelTabla.add(scrollPane, BorderLayout.CENTER);

        return panelTabla;
    }

    private void agregarFilaFormulario(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComponent campo) {
        gbc.gridy = fila;
        gbc.gridx = 0;
        if (!etiqueta.equals("Contraseña:")) {
            JLabel lbl = new JLabel(etiqueta);
            lbl.setForeground(new Color(45, 45, 45)); // Gris oscuro
            panel.add(lbl, gbc);
        }
        gbc.gridx = 1;
        panel.add(campo, gbc);
    }

    private JButton crearBotonFormulario(String texto, Color colorFondo) {
        JButton btn = new JButton(texto);
        btn.setForeground(Color.WHITE);
        btn.setBackground(colorFondo);
        btn.setFocusPainted(false);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setPreferredSize(new Dimension(0, 35));
        btn.putClientProperty("JButton.buttonType", "roundRect");
        return btn;
    }
    
    // =========================================================
    // LÓGICA DE NEGOCIO Y BASE DE DATOS
    // =========================================================

    private void cargarTabla() {
        modeloTabla.setRowCount(0);
        UsuarioDAO dao = new UsuarioDAO();
        for (Usuario u : dao.listarUsuarios()) {
            Object[] fila = new Object[6];
            fila[0] = u.getIdUsuario();
            fila[1] = u.getNombreUsuario();
            fila[2] = u.getNombreRol() != null ? u.getNombreRol().toUpperCase() : "DESCONOCIDO"; 
            fila[3] = u.getEmailUsuario() != null ? u.getEmailUsuario() : "";
            
            String hashRespaldo = Seguridad.encriptarSHA256(u.getNombreUsuario());
            
            if (u.getPasswordHash() != null && u.getPasswordHash().equals(hashRespaldo)) {
                // Si el hash coincide con el de su nombre, es la clave de respaldo (NO tiene acceso)
                fila[4] = "No";
            } else if (u.getPasswordHash() != null && !u.getPasswordHash().isEmpty()) {
                // Si tiene otra clave distinta, SÍ tiene acceso
                fila[4] = "Sí";
            } else {
                fila[4] = "No";
            }
            
            fila[5] = u.isEstadoUsuario() ? "Activo" : "Inactivo";
            modeloTabla.addRow(fila);
        }
    }
    private void cargarDatosEnFormulario() {
        int fila = tablaUsuarios.getSelectedRow();
        
        txtIdentidad.setText(tablaUsuarios.getValueAt(fila, 0).toString());
        txtNombre.setText(tablaUsuarios.getValueAt(fila, 1).toString());
        
        String rolT = tablaUsuarios.getValueAt(fila, 2).toString().toLowerCase();
        cmbRol.setSelectedItem(rolT);
        
        txtEmail.setText(tablaUsuarios.getValueAt(fila, 3).toString());
        
        boolean tieneAcceso = tablaUsuarios.getValueAt(fila, 4).toString().equals("Sí");
        
        // Reforzamos la regla al cargar datos
        if(rolT.contains("admin")){
            chkAccesoSistema.setSelected(true);
            chkAccesoSistema.setEnabled(false);
            lblPassword.setVisible(true);
            txtPassword.setVisible(true);
        } else {
            chkAccesoSistema.setSelected(tieneAcceso);
            chkAccesoSistema.setEnabled(true);
            lblPassword.setVisible(tieneAcceso);
            txtPassword.setVisible(tieneAcceso);
        }
        
        txtPassword.setText(""); // Dejamos en blanco por seguridad
        
        btnGuardar.setText("Actualizar");
        btnGuardar.setBackground(new Color(39, 174, 96)); // Verde Menta 
        
        String estado = tablaUsuarios.getValueAt(fila, 5).toString();
        if (estado.equals("Activo")) {
            btnEliminar.setText("Desactivar");
            btnEliminar.setBackground(new Color(227, 0, 15)); // Rojo
        } else {
            btnEliminar.setText("Activar");
            btnEliminar.setBackground(new Color(39, 174, 96)); // Verde
        }
        btnEliminar.setVisible(true);
        
        panelForm.revalidate();
        panelForm.repaint();
    }

    private void limpiarFormulario() {
        tablaUsuarios.clearSelection();
        txtIdentidad.setText("AUTOGENERADO");
        txtNombre.setText("");
        txtEmail.setText("");
        
        if (cmbRol.getItemCount() > 0) {
            cmbRol.setSelectedIndex(0);
        }
        
        txtPassword.setText("");
        
        btnGuardar.setText("Guardar");
        btnGuardar.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnEliminar.setVisible(false); 
        
        panelForm.revalidate();
        panelForm.repaint();
    }

    private void guardarOActualizarUsuario() {
        if (txtNombre.getText().trim().isEmpty()) {
            utilidades.Mensajes.showMessageDialog(this, "El nombre del usuario es obligatorio.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (chkAccesoSistema.isSelected() && txtEmail.getText().trim().isEmpty()) {
            utilidades.Mensajes.showMessageDialog(this, "El correo electrónico es obligatorio para usuarios con acceso al sistema.", "Advertencia", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Usuario u = new Usuario();
        
        if (btnGuardar.getText().equals("Actualizar")) {
            try {
                String idTexto = txtIdentidad.getText().trim();
                u.setIdUsuario(Integer.parseInt(idTexto)); 
            } catch (NumberFormatException e) {
                utilidades.Mensajes.showMessageDialog(this, "El ID del usuario no es válido.");
                return;
            }
        }

        u.setNombreUsuario(txtNombre.getText().trim());
        u.setEmailUsuario(txtEmail.getText().trim());

        UsuarioDAO dao = new UsuarioDAO();
        String rolSeleccionado = cmbRol.getSelectedItem().toString();
        int idRol = dao.obtenerOCrearRol(rolSeleccionado);
        u.setIdRol(idRol);

        String passEscrita = new String(txtPassword.getPassword());
        
        // --- 🚀 CORRECCIÓN: VALIDACIÓN MEJORADA AL ACTUALIZAR ACCESOS ---
        if (chkAccesoSistema.isSelected()) {
            
            boolean teniaAccesoAntes = false;
            if (btnGuardar.getText().equals("Actualizar")) {
                int filaSelec = tablaUsuarios.getSelectedRow();
                if (filaSelec != -1) {
                    teniaAccesoAntes = tablaUsuarios.getValueAt(filaSelec, 4).toString().equals("Sí");
                }
            }

            if (passEscrita.isEmpty()) {
                // Si lo estamos creando nuevo, O si antes NO tenía acceso y ahora se lo dimos:
                if (btnGuardar.getText().equals("Guardar") || !teniaAccesoAntes) {
                    utilidades.Mensajes.showMessageDialog(this, "Debe ingresar una contraseña para habilitar el acceso al sistema.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                } 
                // Si ya tenía acceso y deja la caja vacía, el DAO conserva la clave anterior
                u.setPasswordHash(""); 
            } else {
                // Validar que la contraseña no se repita con la de otro usuario
                if (dao.existePassword(passEscrita)) {
                    utilidades.Mensajes.showMessageDialog(this, "Contraseña no admitida por falta de seguridad. Ingrese una distinta.", "Advertencia", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                // Si escribió una clave nueva y es válida, la encriptamos
                u.setPasswordHash(Seguridad.encriptarSHA256(passEscrita));
            }
        } else {
            // Si NO tiene acceso, guardamos su nombre encriptado como respaldo
            u.setPasswordHash(Seguridad.encriptarSHA256(u.getNombreUsuario()));
        }
        
        boolean exito;
        if (btnGuardar.getText().equals("Guardar")) {
            exito = dao.registrarUsuario(u);
        } else {
            exito = dao.actualizarUsuario(u);
        }

        if (exito) {
            utilidades.Mensajes.showMessageDialog(this, "Operación realizada con éxito.");
            limpiarFormulario();
            cargarTabla(); 
        } else {
            utilidades.Mensajes.showMessageDialog(this, "Error al guardar en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void desactivarUsuarioSeleccionado() {
        int filaSelec = tablaUsuarios.getSelectedRow();
        if (filaSelec == -1) return;

        String idString = tablaUsuarios.getValueAt(filaSelec, 0).toString();
        
        if (idString.equals("AUTOGENERADO")) return; 

        try {
            int id = Integer.parseInt(idString);
            String estado = tablaUsuarios.getValueAt(filaSelec, 5).toString();
            UsuarioDAO dao = new UsuarioDAO();
            
            if (estado.equals("Activo")) {
                if (utilidades.Mensajes.showConfirmDialog(this, "¿Está seguro de desactivar ID " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (dao.desactivarUsuario(id)) {
                        utilidades.Mensajes.showMessageDialog(this, "Usuario desactivado.");
                        limpiarFormulario();
                        cargarTabla(); 
                    }
                }
            } else {
                if (utilidades.Mensajes.showConfirmDialog(this, "¿Está seguro de activar ID " + id + "?", "Confirmar", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    if (dao.activarUsuario(id)) {
                        utilidades.Mensajes.showMessageDialog(this, "Usuario activado.");
                        limpiarFormulario();
                        cargarTabla(); 
                    }
                }
            }
        } catch (NumberFormatException e) {
            utilidades.Mensajes.showMessageDialog(this, "Error de formato de ID.");
        }
    }
    
    private void cargarRolesEnCombo() {
        cmbRol.removeAllItems();
        UsuarioDAO dao = new UsuarioDAO();
        java.util.List<String> rolesBD = dao.listarNombresDeRoles();
        
        if (rolesBD.isEmpty()) {
            cmbRol.addItem("administrador");
        } else {
            for (String rol : rolesBD) {
                cmbRol.addItem(rol);
            }
        }
    }
    
    private void crearNuevoRol() {
        String nuevoRol = JOptionPane.showInputDialog(this, "Ingrese el nombre del nuevo rol:", "Nuevo Rol", JOptionPane.QUESTION_MESSAGE);
        
        if (nuevoRol != null && !nuevoRol.trim().isEmpty()) {
            UsuarioDAO dao = new UsuarioDAO();
            int idCreado = dao.obtenerOCrearRol(nuevoRol); 
            
            if (idCreado != -1) {
                cargarRolesEnCombo(); 
                cmbRol.setSelectedItem(nuevoRol.trim().toLowerCase()); 
                utilidades.Mensajes.showMessageDialog(this, "Rol creado y seleccionado.");
            } else {
                utilidades.Mensajes.showMessageDialog(this, "Error al crear el rol.");
            }
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

