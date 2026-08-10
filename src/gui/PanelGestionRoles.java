package gui;

import dao.UsuarioDAO;
import modelo.Rol;
import utilidades.EfectosUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PanelGestionRoles extends JPanel {

    private UsuarioDAO usuarioDAO;
    private JComboBox<Rol> cbRoles;
    private JPanel panelPermisos;
    private JButton btnGuardar;
    
    // Almacena los checkboxes activos en memoria (NombrePermiso -> JCheckBox)
    private Map<String, JCheckBox> checkboxesPermisos = new HashMap<>();
    
    // Lista de permisos cargados para el rol seleccionado desde BD
    private List<String> permisosRolSeleccionado = new ArrayList<>();
    
    // Definición estática de permisos por módulo (CRUD)
    private final Map<String, String[]> MAPA_PERMISOS = new HashMap<>() {{
        put("Administración", new String[]{"VER_ADMINISTRACION", "CREAR_ADMINISTRACION", "EDITAR_ADMINISTRACION", "ELIMINAR_ADMINISTRACION"});
        put("Punto de Venta", new String[]{"VER_POS", "CREAR_POS", "EDITAR_POS", "ELIMINAR_POS"});
        put("Inventario", new String[]{"VER_INVENTARIO", "CREAR_INVENTARIO", "EDITAR_INVENTARIO", "ELIMINAR_INVENTARIO"});
        put("Caja", new String[]{"VER_CAJA", "CREAR_CAJA", "EDITAR_CAJA", "ELIMINAR_CAJA"});
        put("Clientes", new String[]{"VER_CLIENTES", "CREAR_CLIENTES", "EDITAR_CLIENTES", "ELIMINAR_CLIENTES"});
        put("Apartados", new String[]{"VER_APARTADOS", "CREAR_APARTADOS", "EDITAR_APARTADOS", "ELIMINAR_APARTADOS"});
        put("Ventas", new String[]{"VER_VENTAS", "CREAR_VENTAS", "EDITAR_VENTAS", "ELIMINAR_VENTAS"});
        put("Garantías", new String[]{"VER_GARANTIAS", "CREAR_GARANTIAS", "EDITAR_GARANTIAS", "ELIMINAR_GARANTIAS"});
        put("Estadísticas", new String[]{"VER_ESTADISTICAS"}); // Solo ver
    }};
    
    private final Map<String, String> DESCRIPCIONES = new HashMap<>() {{
        // Administración
        put("VER_ADMINISTRACION", "Ver módulo de Administración");
        put("CREAR_ADMINISTRACION", "Crear Datos/Usuarios/Roles");
        put("EDITAR_ADMINISTRACION", "Editar Datos/Usuarios/Roles");
        put("ELIMINAR_ADMINISTRACION", "Eliminar Usuarios/Roles");
        
        // Punto de Venta
        put("VER_POS", "Acceder al Punto de Venta");
        put("CREAR_POS", "Generar Factura (Crear Venta)");
        put("EDITAR_POS", "Aplicar Descuentos/Modificar Precios");
        put("ELIMINAR_POS", "Anular Venta en Curso"); // Excepción solicitada
        
        // Inventario
        put("VER_INVENTARIO", "Ver Inventario de Productos");
        put("CREAR_INVENTARIO", "Adicionar Nuevos Productos");
        put("EDITAR_INVENTARIO", "Editar Productos");
        put("ELIMINAR_INVENTARIO", "Eliminar Productos");
        
        // Caja
        put("VER_CAJA", "Ver Estado de Caja");
        put("CREAR_CAJA", "Abrir Caja / Generar Egresos");
        put("EDITAR_CAJA", "Modificar Movimientos (Avanzado)");
        put("ELIMINAR_CAJA", "Cerrar Caja");
        
        // Clientes
        put("VER_CLIENTES", "Ver Directorio de Clientes");
        put("CREAR_CLIENTES", "Adicionar Nuevos Clientes");
        put("EDITAR_CLIENTES", "Editar Clientes Existentes");
        put("ELIMINAR_CLIENTES", "Eliminar Clientes");
        
        // Apartados
        put("VER_APARTADOS", "Ver Apartados");
        put("CREAR_APARTADOS", "Crear Nuevos Apartados / Abonos");
        put("EDITAR_APARTADOS", "Editar Apartados");
        put("ELIMINAR_APARTADOS", "Cancelar / Anular Apartados");
        
        // Ventas
        put("VER_VENTAS", "Ver Historial de Ventas");
        put("CREAR_VENTAS", "Adicionar Documentos Anexos");
        put("EDITAR_VENTAS", "Editar Historial (Avanzado)");
        put("ELIMINAR_VENTAS", "Anular Facturas / Devoluciones");
        
        // Garantías
        put("VER_GARANTIAS", "Ver Módulo de Garantías");
        put("CREAR_GARANTIAS", "Registrar Nuevos Reclamos");
        put("EDITAR_GARANTIAS", "Editar Estado de Reclamos");
        put("ELIMINAR_GARANTIAS", "Eliminar Reclamos");
        
        // Estadísticas
        put("VER_ESTADISTICAS", "Ver Gráficos y Reportes Financieros");
    }};

    public PanelGestionRoles() {
        usuarioDAO = new UsuarioDAO();
        iniciarDiseno();
        cargarRoles();
    }

    private JButton btnEditarRol;
    private JButton btnEliminarRol;

    private void iniciarDiseno() {
        setLayout(new BorderLayout());
        setBackground(EfectosUI.COLOR_FONDO_PANEL);

        // --- PANEL SUPERIOR: SELECCIÓN DE ROL ---
        JPanel panelSuperior = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 15));
        panelSuperior.setBackground(new Color(232, 243, 236));
        panelSuperior.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(190, 215, 200)));
        
        JLabel lblRol = new JLabel("Seleccione el Rol a configurar:");
        lblRol.setFont(new Font("Segoe UI", Font.BOLD, 14));
        
        cbRoles = new JComboBox<>();
        cbRoles.setPreferredSize(new Dimension(250, 35));
        cbRoles.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        cbRoles.addActionListener(e -> alSeleccionarRol());
        
        JButton btnNuevoRol = EfectosUI.crearBotonVerde("Crear Nuevo Rol");
        btnNuevoRol.setPreferredSize(new Dimension(150, 35));
        btnNuevoRol.addActionListener(e -> crearNuevoRol(btnNuevoRol));

        btnEditarRol = new JButton("Editar Rol");
        btnEditarRol.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEditarRol.setBackground(new Color(241, 196, 15));
        btnEditarRol.setForeground(Color.WHITE);
        btnEditarRol.setFocusPainted(false);
        btnEditarRol.setPreferredSize(new Dimension(100, 35));
        btnEditarRol.addActionListener(e -> editarRol(btnEditarRol));

        btnEliminarRol = new JButton("Eliminar Rol");
        btnEliminarRol.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnEliminarRol.setBackground(new Color(231, 76, 60));
        btnEliminarRol.setForeground(Color.WHITE);
        btnEliminarRol.setFocusPainted(false);
        btnEliminarRol.setPreferredSize(new Dimension(110, 35));
        btnEliminarRol.addActionListener(e -> eliminarRol(btnEliminarRol));
        
        modelo.Usuario uAct = utilidades.SesionGlobal.getUsuarioActual();
        if (uAct != null && !uAct.tienePermiso("CREAR_ADMINISTRACION")) {
            btnNuevoRol.setEnabled(false);
            btnNuevoRol.setToolTipText("No tienes permiso para crear roles.");
        }
        if (uAct != null && !uAct.tienePermiso("EDITAR_ADMINISTRACION")) {
            btnEditarRol.setEnabled(false);
            btnEditarRol.setToolTipText("No tienes permiso para editar roles.");
        }
        if (uAct != null && !uAct.tienePermiso("ELIMINAR_ADMINISTRACION")) {
            btnEliminarRol.setEnabled(false);
            btnEliminarRol.setToolTipText("No tienes permiso para eliminar roles.");
        }

        panelSuperior.add(lblRol);
        panelSuperior.add(cbRoles);
        panelSuperior.add(btnNuevoRol);
        panelSuperior.add(btnEditarRol);
        panelSuperior.add(btnEliminarRol);

        // --- PANEL IZQUIERDO: MÓDULOS ---
        JPanel panelIzquierdo = new JPanel();
        panelIzquierdo.setLayout(new BoxLayout(panelIzquierdo, BoxLayout.Y_AXIS));
        panelIzquierdo.setPreferredSize(new Dimension(220, 0));
        panelIzquierdo.setBackground(new Color(213, 233, 222));
        panelIzquierdo.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        
        JLabel lblModulos = new JLabel("MÓDULOS");
        lblModulos.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblModulos.setForeground(new Color(75, 115, 95));
        lblModulos.setAlignmentX(Component.CENTER_ALIGNMENT);
        panelIzquierdo.add(lblModulos);
        panelIzquierdo.add(Box.createVerticalStrut(15));
        
        for (String modulo : MAPA_PERMISOS.keySet()) {
            JButton btnModulo = new JButton(modulo);
            btnModulo.setFont(new Font("Segoe UI", Font.BOLD, 14));
            btnModulo.setForeground(new Color(28, 59, 45));
            btnModulo.setBackground(new Color(232, 243, 236));
            btnModulo.setFocusPainted(false);
            btnModulo.setAlignmentX(Component.CENTER_ALIGNMENT);
            btnModulo.setMaximumSize(new Dimension(200, 40));
            btnModulo.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnModulo.addActionListener(e -> mostrarPermisosModulo(modulo));
            
            panelIzquierdo.add(btnModulo);
            panelIzquierdo.add(Box.createVerticalStrut(8));
        }

        // --- PANEL CENTRAL: CHECKBOXES DE PERMISOS ---
        panelPermisos = new JPanel();
        panelPermisos.setLayout(new BoxLayout(panelPermisos, BoxLayout.Y_AXIS));
        panelPermisos.setBackground(Color.WHITE);
        panelPermisos.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        
        JScrollPane scrollCentral = new JScrollPane(panelPermisos);
        scrollCentral.setBorder(null);

        // --- PANEL INFERIOR: GUARDAR ---
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        panelInferior.setBackground(EfectosUI.COLOR_FONDO_PANEL);
        
        btnGuardar = EfectosUI.crearBotonVerde("Guardar Permisos del Rol");
        btnGuardar.setPreferredSize(new Dimension(250, 45));
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnGuardar.addActionListener(e -> guardarPermisos(btnGuardar));
        
        if (uAct != null && !uAct.tienePermiso("EDITAR_ADMINISTRACION") && !uAct.tienePermiso("CREAR_ADMINISTRACION")) {
            btnGuardar.setEnabled(false);
            btnGuardar.setToolTipText("No tienes permiso para modificar permisos de roles.");
        }
        
        panelInferior.add(btnGuardar);

        // Armar la vista final
        add(panelSuperior, BorderLayout.NORTH);
        add(panelIzquierdo, BorderLayout.WEST);
        add(scrollCentral, BorderLayout.CENTER);
        add(panelInferior, BorderLayout.SOUTH);
    }

    private void cargarRoles() {
        cbRoles.removeAllItems();
        List<Rol> roles = usuarioDAO.obtenerTodosLosRoles();
        for (Rol r : roles) {
            cbRoles.addItem(r);
        }
    }

    private void alSeleccionarRol() {
        Rol rolSeleccionado = (Rol) cbRoles.getSelectedItem();
        if (rolSeleccionado != null) {
            // Si es rol 1 (Administrador), bloqueamos los cambios
            if (rolSeleccionado.getIdRol() == 1 || rolSeleccionado.getNombreRol().equalsIgnoreCase("Administrador")) {
                btnGuardar.setEnabled(false);
                if (btnEditarRol != null) btnEditarRol.setEnabled(false);
                if (btnEliminarRol != null) btnEliminarRol.setEnabled(false);
                JOptionPane.showMessageDialog(this, "El Rol Maestro/Administrador tiene acceso total por defecto y no puede ser modificado.", "Rol Protegido", JOptionPane.INFORMATION_MESSAGE);
                panelPermisos.removeAll();
                panelPermisos.revalidate();
                panelPermisos.repaint();
                return;
            } else {
                btnGuardar.setEnabled(true);
                if (btnEditarRol != null) btnEditarRol.setEnabled(true);
                if (btnEliminarRol != null) btnEliminarRol.setEnabled(true);
            }
            
            // Cargar de base de datos a memoria
            permisosRolSeleccionado = usuarioDAO.cargarPermisosRol(rolSeleccionado.getIdRol());
            
            // Actualizar visualmente si hay algún panel abierto
            panelPermisos.removeAll();
            JLabel lblSelect = new JLabel("Seleccione un módulo a la izquierda para configurar sus permisos.");
            lblSelect.setFont(new Font("Segoe UI", Font.ITALIC, 14));
            lblSelect.setForeground(Color.GRAY);
            panelPermisos.add(lblSelect);
            panelPermisos.revalidate();
            panelPermisos.repaint();
        }
    }

    private void mostrarPermisosModulo(String modulo) {
        Rol rolSeleccionado = (Rol) cbRoles.getSelectedItem();
        if (rolSeleccionado == null || !btnGuardar.isEnabled()) return;
        
        // Antes de cambiar de módulo, guardamos en memoria RAM lo que el usuario haya cliqueado en el módulo anterior
        sincronizarCheckboxesAMemoria();
        
        panelPermisos.removeAll();
        checkboxesPermisos.clear();
        
        JLabel lblTitulo = new JLabel("Permisos para: " + modulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(45, 106, 79));
        panelPermisos.add(lblTitulo);
        panelPermisos.add(Box.createVerticalStrut(20));
        
        String[] permisosDelModulo = MAPA_PERMISOS.get(modulo);
        
        for (String p : permisosDelModulo) {
            String desc = DESCRIPCIONES.getOrDefault(p, p);
            JCheckBox chk = new JCheckBox(desc);
            chk.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            chk.setBackground(Color.WHITE);
            chk.setFocusPainted(false);
            chk.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            // Si el permiso está en la lista de memoria, lo marcamos
            if (permisosRolSeleccionado.contains(p)) {
                chk.setSelected(true);
            }
            
            checkboxesPermisos.put(p, chk);
            panelPermisos.add(chk);
            panelPermisos.add(Box.createVerticalStrut(10));
        }
        
        panelPermisos.revalidate();
        panelPermisos.repaint();
    }
    
    private void sincronizarCheckboxesAMemoria() {
        for (Map.Entry<String, JCheckBox> entry : checkboxesPermisos.entrySet()) {
            String permiso = entry.getKey();
            boolean estaMarcado = entry.getValue().isSelected();
            
            if (estaMarcado && !permisosRolSeleccionado.contains(permiso)) {
                permisosRolSeleccionado.add(permiso);
            } else if (!estaMarcado && permisosRolSeleccionado.contains(permiso)) {
                permisosRolSeleccionado.remove(permiso);
            }
        }
    }

    private void guardarPermisos(JButton boton) {
        Rol rolSeleccionado = (Rol) cbRoles.getSelectedItem();
        if (rolSeleccionado == null) return;
        
        // Sincronizar el panel activo antes de guardar
        sincronizarCheckboxesAMemoria();
        
        boton.setEnabled(false);
        boton.setText("Guardando...");
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                return usuarioDAO.guardarPermisosRol(rolSeleccionado.getIdRol(), permisosRolSeleccionado);
            }

            @Override
            protected void done() {
                try {
                    boolean exito = get();
                    if (exito) {
                        JOptionPane.showMessageDialog(PanelGestionRoles.this, "¡Permisos guardados correctamente para el rol: " + rolSeleccionado.getNombreRol() + "!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(PanelGestionRoles.this, "Ocurrió un error al guardar los permisos.", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(PanelGestionRoles.this, "Error: " + ex.getMessage());
                } finally {
                    boton.setEnabled(true);
                    boton.setText("Guardar Permisos del Rol");
                }
            }
        };
        worker.execute();
    }
    
    private void crearNuevoRol(JButton boton) {
        String nombreRol = JOptionPane.showInputDialog(this, "Ingrese el nombre para el nuevo Rol (Ej. Cajero, Vendedor):", "Nuevo Rol", JOptionPane.QUESTION_MESSAGE);
        if (nombreRol != null && !nombreRol.trim().isEmpty()) {
            boton.setEnabled(false);
            SwingWorker<Integer, Void> worker = new SwingWorker<Integer, Void>() {
                @Override
                protected Integer doInBackground() throws Exception {
                    return usuarioDAO.obtenerOCrearRol(nombreRol);
                }

                @Override
                protected void done() {
                    try {
                        int idGenerado = get();
                        if (idGenerado > 0) {
                            JOptionPane.showMessageDialog(PanelGestionRoles.this, "Rol creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                            cargarRoles();
                        } else {
                            JOptionPane.showMessageDialog(PanelGestionRoles.this, "Error al crear el rol o el rol ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(PanelGestionRoles.this, "Error: " + ex.getMessage());
                    } finally {
                        boton.setEnabled(true);
                    }
                }
            };
            worker.execute();
        }
    }

    private void editarRol(JButton boton) {
        Rol rolSeleccionado = (Rol) cbRoles.getSelectedItem();
        if (rolSeleccionado == null) return;

        String nuevoNombre = JOptionPane.showInputDialog(this, "Ingrese el nuevo nombre para el rol '" + rolSeleccionado.getNombreRol() + "':", "Editar Rol", JOptionPane.QUESTION_MESSAGE);
        if (nuevoNombre != null && !nuevoNombre.trim().isEmpty() && !nuevoNombre.equalsIgnoreCase(rolSeleccionado.getNombreRol())) {
            boton.setEnabled(false);
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return usuarioDAO.editarRol(rolSeleccionado.getIdRol(), nuevoNombre);
                }

                @Override
                protected void done() {
                    try {
                        boolean exito = get();
                        if (exito) {
                            JOptionPane.showMessageDialog(PanelGestionRoles.this, "Rol editado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                            cargarRoles();
                        } else {
                            JOptionPane.showMessageDialog(PanelGestionRoles.this, "Ocurrió un error al editar el rol.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(PanelGestionRoles.this, "Error: " + ex.getMessage());
                    } finally {
                        boton.setEnabled(true);
                    }
                }
            };
            worker.execute();
        }
    }

    private void eliminarRol(JButton boton) {
        Rol rolSeleccionado = (Rol) cbRoles.getSelectedItem();
        if (rolSeleccionado == null) return;

        int confirmacion = JOptionPane.showConfirmDialog(this, "¿Está seguro que desea eliminar el rol '" + rolSeleccionado.getNombreRol() + "'? Esta acción eliminará los permisos asociados.\nNota: Fallará si existen usuarios asignados a este rol.", "Eliminar Rol", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirmacion == JOptionPane.YES_OPTION) {
            boton.setEnabled(false);
            SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return usuarioDAO.eliminarRol(rolSeleccionado.getIdRol());
                }

                @Override
                protected void done() {
                    try {
                        boolean exito = get();
                        if (exito) {
                            JOptionPane.showMessageDialog(PanelGestionRoles.this, "Rol eliminado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                            cargarRoles();
                        } else {
                            JOptionPane.showMessageDialog(PanelGestionRoles.this, "No se pudo eliminar el rol. Verifique que no haya usuarios asignados a él.", "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(PanelGestionRoles.this, "Error: " + ex.getMessage());
                    } finally {
                        boton.setEnabled(true);
                    }
                }
            };
            worker.execute();
        }
    }
}
