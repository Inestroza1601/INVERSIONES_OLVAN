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
        this.setBackground(new Color(240, 242, 245)); // Gris Nube (Fondo principal)

        // 1. Crear el Sub-Menú superior
        panelSubMenu = new JPanel();
        panelSubMenu.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 0)); // Quitamos los márgenes para que parezcan pestañas
        panelSubMenu.setBackground(new Color(255, 255, 255)); // Blanco puro para el menú
        panelSubMenu.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 222, 225))); // Borde Gris Claro

        // 2. Crear y estilizar los botones del sub-menú
        btnCrearProducto = crearBotonSubMenu("Crear Producto");
        btnInventarioDefectuoso = crearBotonSubMenu("Inventario Defectuoso");
        btnBuscarProducto = crearBotonSubMenu("Buscar Producto / Inventario"); 

        panelSubMenu.add(btnBuscarProducto); // Ponemos buscar primero, es la acción más común
        int rolId = (utilidades.SesionGlobal.getUsuarioActual() != null) ? utilidades.SesionGlobal.getUsuarioActual().getIdRol() : 1;
        if (rolId != 3) {
            panelSubMenu.add(btnCrearProducto);
            panelSubMenu.add(btnInventarioDefectuoso);
        }

        // 3. Crear el contenedor central (VACÍO, sin el JLabel)
        panelContenedorInventario = new JPanel();
        panelContenedorInventario.setLayout(new BorderLayout());
        panelContenedorInventario.setBackground(new Color(240, 242, 245)); // Gris Nube

        this.add(panelSubMenu, BorderLayout.NORTH); 
        this.add(panelContenedorInventario, BorderLayout.CENTER); 

        // 4. Configurar los Eventos
        btnCrearProducto.addActionListener(e -> {
            mostrarSubPanel(new PanelCrearProducto());
        });

        btnInventarioDefectuoso.addActionListener(e -> {
            mostrarSubPanel(new PanelInventarioDefectuoso());
        });

        btnBuscarProducto.addActionListener(e -> {
            mostrarSubPanel(new PanelBuscarProducto());
        });

        // 5. LA MAGIA: Hacemos clic automático en "Buscar" al abrir el módulo
        SwingUtilities.invokeLater(() -> {
            btnBuscarProducto.doClick();
        });
    }

    // Nuevo diseño de botones tipo "Tab" web adaptado a paleta clara
    private JButton crearBotonSubMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setBackground(new Color(255, 255, 255)); // Blanco Puro
        boton.setForeground(new Color(140, 145, 150)); // Gris Suave (Inactivo)
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(200, 45)); // Más altos y anchos
        boton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Sin borde
        boton.putClientProperty("JButton.buttonType", "borderless"); // Quita el diseño por defecto de FlatLaf

        // Efecto Hover (Se ilumina al pasar el mouse)
        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(240, 242, 245)); // Gris Nube para un hover sutil
                boton.setForeground(new Color(45, 45, 45)); // Gris Oscuro (Texto Activo)
                // Le agregamos una línea azul abajo al pasar el cursor (Color Azul de acciones informativas)
                boton.setBorder(BorderFactory.createMatteBorder(0, 0, 3, 0, new Color(13, 110, 253)));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                boton.setBackground(new Color(255, 255, 255)); // Vuelve al Blanco Puro
                boton.setForeground(new Color(140, 145, 150)); // Vuelve al Gris Suave
                boton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
            }
        });

        return boton;
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
