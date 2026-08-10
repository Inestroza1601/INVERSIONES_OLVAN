package gui;

import javax.swing.*;
import java.awt.*;

public class PanelInventario extends JPanel {

    private JPanel panelSubMenu;
    private JButton btnCrearProducto;
    private JButton btnBuscarProducto;
    private JButton btnInventarioDefectuoso;
    private JPanel panelContenedorInventario;

    private JButton botonActivo = null;

    private void cambiarBotonActivo(JButton nuevoBoton) {
        if (botonActivo != null) {
            botonActivo.setEnabled(true);
            botonActivo.setBackground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO); // Restaurar original
        }
        botonActivo = nuevoBoton;
        if (botonActivo != null) {
            botonActivo.setEnabled(false);
        }
    }

    public PanelInventario() {
        iniciarDiseno();
    }

    private void iniciarDiseno() {
        this.removeAll();
        this.setLayout(new BorderLayout());
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);

        // 1. Crear el contenedor central (la tabla/sub-panel sube arriba del todo)
        panelContenedorInventario = new JPanel();
        panelContenedorInventario.setLayout(new BorderLayout());
        panelContenedorInventario.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);

        // 2. Barra de botones en la parte inferior (elevados)
        panelSubMenu = new JPanel();
        panelSubMenu.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        panelSubMenu.setBackground(new Color(213, 233, 222));
        panelSubMenu.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(190, 215, 200)),
                BorderFactory.createEmptyBorder(10, 12, 20, 12)
        ));

        // 3. Botones del sub-menú (verdes vintage, mismo tamaño)
        btnBuscarProducto = utilidades.EfectosUI.crearBotonVerde("Buscar Producto / Inventario");
        btnCrearProducto = utilidades.EfectosUI.crearBotonVerde("Crear Producto");
        btnInventarioDefectuoso = utilidades.EfectosUI.crearBotonVerde("Inventario Defectuoso");

        Dimension tamBoton = new Dimension(260, 50);
        btnBuscarProducto.setPreferredSize(tamBoton);
        btnCrearProducto.setPreferredSize(tamBoton);
        btnInventarioDefectuoso.setPreferredSize(tamBoton);
        btnBuscarProducto.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnCrearProducto.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnInventarioDefectuoso.setFont(new Font("Segoe UI", Font.BOLD, 16));

        panelSubMenu.add(btnBuscarProducto);
        modelo.Usuario uAct = utilidades.SesionGlobal.getUsuarioActual();
        // Control RBAC: Solo usuarios con permiso CREAR_INVENTARIO pueden crear
        if (uAct == null || uAct.tienePermiso("CREAR_INVENTARIO")) {
            panelSubMenu.add(btnCrearProducto);
        }
        // Inventario defectuoso puede asociarse a ELIMINAR_INVENTARIO (baja de producto)
        if (uAct == null || uAct.tienePermiso("ELIMINAR_INVENTARIO")) {
            panelSubMenu.add(btnInventarioDefectuoso);
        }

        this.add(panelContenedorInventario, BorderLayout.CENTER);
        this.add(panelSubMenu, BorderLayout.SOUTH);  // Botones abajo (con elevación)

        // 4. Configurar los Eventos con Carga Asíncrona
        btnBuscarProducto.addActionListener(e -> {
            cambiarBotonActivo(btnBuscarProducto);
            abrirSubPanelAsync(() -> new PanelBuscarProducto());
        });

        btnCrearProducto.addActionListener(e -> {
            cambiarBotonActivo(btnCrearProducto);
            abrirSubPanelAsync(() -> new PanelCrearProducto());
        });

        btnInventarioDefectuoso.addActionListener(e -> {
            cambiarBotonActivo(btnInventarioDefectuoso);
            abrirSubPanelAsync(() -> new PanelInventarioDefectuoso());
        });

        cambiarBotonActivo(btnBuscarProducto);
        abrirSubPanelAsync(() -> new PanelBuscarProducto());
    }

    public void abrirSubPanelAsync(java.util.function.Supplier<JPanel> panelSupplier) {
        panelContenedorInventario.removeAll();
        
        PanelCargaOverlay loader = new PanelCargaOverlay("Cargando inventario...");
        panelContenedorInventario.add(loader, BorderLayout.CENTER);
        panelContenedorInventario.revalidate();
        panelContenedorInventario.repaint();
        loader.iniciarAnimacion();

        SwingWorker<JPanel, Void> worker = new SwingWorker<JPanel, Void>() {
            @Override
            protected JPanel doInBackground() throws Exception {
                return panelSupplier.get();
            }

            @Override
            protected void done() {
                try {
                    JPanel nuevoPanel = get();
                    loader.detenerAnimacion();
                    mostrarSubPanel(nuevoPanel);
                } catch (Exception e) {
                    e.printStackTrace();
                    loader.detenerAnimacion();
                    panelContenedorInventario.removeAll();
                    JLabel lblError = new JLabel("Error al cargar el módulo: " + e.getMessage());
                    lblError.setHorizontalAlignment(SwingConstants.CENTER);
                    lblError.setForeground(Color.RED);
                    panelContenedorInventario.add(lblError, BorderLayout.CENTER);
                    panelContenedorInventario.revalidate();
                    panelContenedorInventario.repaint();
                }
            }
        };
        worker.execute();
    }

    public void mostrarSubPanel(JPanel nuevoPanel) {
        panelContenedorInventario.removeAll();
        panelContenedorInventario.add(nuevoPanel, BorderLayout.CENTER);
        panelContenedorInventario.revalidate();
        panelContenedorInventario.repaint();
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
