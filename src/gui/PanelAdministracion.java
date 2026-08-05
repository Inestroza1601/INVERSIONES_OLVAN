package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PanelAdministracion extends JPanel {

    private JPanel panelSubMenu;
    private JButton btnDatosEmpresa;
    private JButton btnUsuarios;
    private JButton btnReportes;
    
    // Contenedor dinámico donde se mostrarán los sub-paneles
    private JPanel panelContenedorAdmon;

    public PanelAdministracion() {
        initComponents(); // Deja que NetBeans cargue su panel vacío
        iniciarDiseno();  // Ejecuta nuestro código manual para sobreescribirlo
    }

    /**
     * Este es NUESTRO método para armar la pantalla, esquivando el bloqueo de NetBeans.
     */
    private void iniciarDiseno() {
        this.removeAll();
        this.setLayout(new BorderLayout());
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);

        // 1. Contenedor central (arriba)
        panelContenedorAdmon = new JPanel();
        panelContenedorAdmon.setLayout(new BorderLayout());
        panelContenedorAdmon.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);

        // 2. Barra de botones en la parte inferior
        panelSubMenu = new JPanel();
        panelSubMenu.setLayout(new FlowLayout(FlowLayout.RIGHT, 12, 10));
        panelSubMenu.setBackground(new Color(213, 233, 222));
        panelSubMenu.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(190, 215, 200)));

        // 3. Botones del sub-menú (mismo tamaño)
        btnDatosEmpresa = utilidades.EfectosUI.crearBotonVerde("Datos de Empresa");
        btnUsuarios = utilidades.EfectosUI.crearBotonVerde("Gestion de Usuarios");
        btnReportes = utilidades.EfectosUI.crearBotonVerde("Generación de Reportes");

        Dimension tamBoton = new Dimension(250, 50);
        btnDatosEmpresa.setPreferredSize(tamBoton);
        btnUsuarios.setPreferredSize(tamBoton);
        btnReportes.setPreferredSize(tamBoton);
        btnDatosEmpresa.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnUsuarios.setFont(new Font("Segoe UI", Font.BOLD, 16));
        btnReportes.setFont(new Font("Segoe UI", Font.BOLD, 16));

        panelSubMenu.add(btnDatosEmpresa);
        panelSubMenu.add(btnUsuarios);
        panelSubMenu.add(btnReportes);

        this.add(panelContenedorAdmon, BorderLayout.CENTER);
        this.add(panelSubMenu, BorderLayout.SOUTH);

        // 4. Configurar los Eventos
        btnDatosEmpresa.addActionListener(e -> {
            mostrarSubPanel(new PanelDatosEmpresa());
        });

        btnUsuarios.addActionListener(e -> {
            mostrarSubPanel(new PanelGestionUsuarios());
        });

        btnReportes.addActionListener(e -> {
            mostrarSubPanel(new PanelReportes());
        });

        mostrarSubPanel(new PanelDatosEmpresa());
    }

    /**
     * Método interno para cambiar el panel central de la administración
     */
    private void mostrarSubPanel(JPanel nuevoPanel) {
        panelContenedorAdmon.removeAll();
        panelContenedorAdmon.add(nuevoPanel, BorderLayout.CENTER);
        panelContenedorAdmon.revalidate();
        panelContenedorAdmon.repaint();
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
