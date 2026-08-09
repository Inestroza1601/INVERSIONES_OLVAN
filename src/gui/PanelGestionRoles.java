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
    
    // Definición estática de permisos por módulo
    private final Map<String, String[]> MAPA_PERMISOS = new HashMap<>() {{
        put("Administración", new String[]{"ACCESO_ADMINISTRACION"});
        put("Punto de Venta", new String[]{"ACCESO_POS", "CANCELAR_VENTAS", "APLICAR_DESCUENTOS"});
        put("Inventario", new String[]{"ACCESO_INVENTARIO", "ELIMINAR_PRODUCTOS"});
        put("Caja", new String[]{"ACCESO_CAJA"});
        put("Clientes", new String[]{"ACCESO_CLIENTES", "ELIMINAR_CLIENTES"});
        put("Apartados", new String[]{"ACCESO_APARTADOS"});
        put("Ventas", new String[]{"ACCESO_VENTAS", "ANULAR_FACTURAS"});
        put("Garantías", new String[]{"ACCESO_GARANTIAS"});
        put("Estadísticas", new String[]{"ACCESO_ESTADISTICAS"});
    }};
    
    private final Map<String, String> DESCRIPCIONES = new HashMap<>() {{
        put("ACCESO_ADMINISTRACION", "Permitir acceso al panel de Administración (Gestión)");
        put("ACCESO_POS", "Permitir acceso al módulo de Facturación");
        put("CANCELAR_VENTAS", "Permitir cancelar una venta en curso");
        put("APLICAR_DESCUENTOS", "Permitir aplicar descuentos manuales en facturación");
        put("ACCESO_INVENTARIO", "Permitir ver el Inventario");
        put("ELIMINAR_PRODUCTOS", "Permitir eliminar y editar productos (Acceso Avanzado)");
        put("ACCESO_CAJA", "Permitir aperturas, cierres y egresos de caja");
        put("ACCESO_CLIENTES", "Permitir acceso al Directorio de Clientes");
        put("ELIMINAR_CLIENTES", "Permitir eliminar clientes del directorio");
        put("ACCESO_APARTADOS", "Permitir registrar y ver Apartados");
        put("ACCESO_VENTAS", "Permitir ver el Historial de Ventas");
        put("ANULAR_FACTURAS", "Permitir anular facturas y procesar devoluciones");
        put("ACCESO_GARANTIAS", "Permitir gestionar Garantías");
        put("ACCESO_ESTADISTICAS", "Permitir ver Gráficos y Reportes Financieros");
    }};

    public PanelGestionRoles() {
        usuarioDAO = new UsuarioDAO();
        iniciarDiseno();
        cargarRoles();
    }

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
        btnNuevoRol.addActionListener(e -> crearNuevoRol());

        panelSuperior.add(lblRol);
        panelSuperior.add(cbRoles);
        panelSuperior.add(btnNuevoRol);

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
        btnGuardar.addActionListener(e -> guardarPermisos());
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
                JOptionPane.showMessageDialog(this, "El Rol Maestro/Administrador tiene acceso total por defecto y no puede ser modificado.", "Rol Protegido", JOptionPane.INFORMATION_MESSAGE);
                panelPermisos.removeAll();
                panelPermisos.revalidate();
                panelPermisos.repaint();
                return;
            } else {
                btnGuardar.setEnabled(true);
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

    private void guardarPermisos() {
        Rol rolSeleccionado = (Rol) cbRoles.getSelectedItem();
        if (rolSeleccionado == null) return;
        
        // Sincronizar el panel activo antes de guardar
        sincronizarCheckboxesAMemoria();
        
        boolean exito = usuarioDAO.guardarPermisosRol(rolSeleccionado.getIdRol(), permisosRolSeleccionado);
        if (exito) {
            JOptionPane.showMessageDialog(this, "¡Permisos guardados correctamente para el rol: " + rolSeleccionado.getNombreRol() + "!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "Ocurrió un error al guardar los permisos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void crearNuevoRol() {
        String nombreRol = JOptionPane.showInputDialog(this, "Ingrese el nombre para el nuevo Rol (Ej. Cajero, Vendedor):", "Nuevo Rol", JOptionPane.QUESTION_MESSAGE);
        if (nombreRol != null && !nombreRol.trim().isEmpty()) {
            int idGenerado = usuarioDAO.obtenerOCrearRol(nombreRol);
            if (idGenerado > 0) {
                JOptionPane.showMessageDialog(this, "Rol creado exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                cargarRoles();
            } else {
                JOptionPane.showMessageDialog(this, "Error al crear el rol o el rol ya existe.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
