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

    private JButton botonActivo = null;

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
        JButton btnRoles = utilidades.EfectosUI.crearBotonVerde("Gestión de Permisos");
        btnReportes = utilidades.EfectosUI.crearBotonVerde("Generación de Reportes");

        Dimension tamBoton = new Dimension(220, 50); // Reducido un poco para caber los 4
        btnDatosEmpresa.setPreferredSize(tamBoton);
        btnUsuarios.setPreferredSize(tamBoton);
        btnRoles.setPreferredSize(tamBoton);
        btnReportes.setPreferredSize(tamBoton);
        
        btnDatosEmpresa.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnUsuarios.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnRoles.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnReportes.setFont(new Font("Segoe UI", Font.BOLD, 15));

        panelSubMenu.add(btnDatosEmpresa);
        panelSubMenu.add(btnUsuarios);
        panelSubMenu.add(btnRoles);
        panelSubMenu.add(btnReportes);

        this.add(panelContenedorAdmon, BorderLayout.CENTER);
        this.add(panelSubMenu, BorderLayout.SOUTH);

        // 4. Configurar los Eventos con Carga Asíncrona
        btnDatosEmpresa.addActionListener(e -> {
            cambiarBotonActivo(btnDatosEmpresa);
            abrirSubPanelAsync(() -> new PanelDatosEmpresa());
        });

        btnUsuarios.addActionListener(e -> {
            cambiarBotonActivo(btnUsuarios);
            abrirSubPanelAsync(() -> new PanelGestionUsuarios());
        });

        btnRoles.addActionListener(e -> {
            cambiarBotonActivo(btnRoles);
            abrirSubPanelAsync(() -> new PanelGestionRoles());
        });

        btnReportes.addActionListener(e -> {
            cambiarBotonActivo(btnReportes);
            abrirSubPanelAsync(() -> new PanelReportes());
        });

        // Configurar estado inicial
        cambiarBotonActivo(btnDatosEmpresa);
        abrirSubPanelAsync(() -> new PanelDatosEmpresa());
    }

    private void cambiarBotonActivo(JButton nuevoBoton) {
        if (botonActivo != null) {
            botonActivo.setEnabled(true);
            botonActivo.setBackground(new Color(46, 204, 113)); // Restaurar color original del botón verde
        }
        botonActivo = nuevoBoton;
        if (botonActivo != null) {
            botonActivo.setEnabled(false);
            botonActivo.setBackground(Color.GRAY); // Color bloqueado
        }
    }

    public void abrirSubPanelAsync(java.util.function.Supplier<JPanel> panelSupplier) {
        panelContenedorAdmon.removeAll();
        
        PanelCargaOverlay loader = new PanelCargaOverlay("Cargando módulo...");
        panelContenedorAdmon.add(loader, BorderLayout.CENTER);
        panelContenedorAdmon.revalidate();
        panelContenedorAdmon.repaint();
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
                    panelContenedorAdmon.removeAll();
                    JLabel lblError = new JLabel("Error al cargar el módulo: " + e.getMessage());
                    lblError.setHorizontalAlignment(SwingConstants.CENTER);
                    lblError.setForeground(Color.RED);
                    panelContenedorAdmon.add(lblError, BorderLayout.CENTER);
                    panelContenedorAdmon.revalidate();
                    panelContenedorAdmon.repaint();
                }
            }
        };
        worker.execute();
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
