package gui;

import javax.swing.*;
import java.awt.*;

public class PanelInventario extends JPanel {

    private JPanel panelSubMenu;
    private JButton btnCrearProducto;
    private JButton btnBuscarProducto;
    private JButton btnInventarioDefectuoso;
    private JPanel panelContenedorInventario;

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
        int rolId = (utilidades.SesionGlobal.getUsuarioActual() != null) ? utilidades.SesionGlobal.getUsuarioActual().getIdRol() : 1;
        if (rolId != 3) {
            panelSubMenu.add(btnCrearProducto);
            panelSubMenu.add(btnInventarioDefectuoso);
        }

        this.add(panelContenedorInventario, BorderLayout.CENTER);
        this.add(panelSubMenu, BorderLayout.SOUTH);  // Botones abajo (con elevación)

        // 4. Configurar los Eventos
        btnBuscarProducto.addActionListener(e -> {
            mostrarSubPanel(new PanelBuscarProducto());
        });

        btnCrearProducto.addActionListener(e -> {
            mostrarSubPanel(new PanelCrearProducto());
        });

        btnInventarioDefectuoso.addActionListener(e -> {
            mostrarSubPanel(new PanelInventarioDefectuoso());
        });

        mostrarSubPanel(new PanelBuscarProducto());
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
