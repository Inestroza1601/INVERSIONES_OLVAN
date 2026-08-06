package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class PanelAdministracion extends JPanel {

    private JPanel panelSubMenu;
    private JButton btnDatosEmpresa;
    private JButton btnUsuarios;
<<<<<<< HEAD

=======
    private JButton btnReportes;
    
>>>>>>> origin/parte-muoz
    // Contenedor dinámico donde se mostrarán los sub-paneles
    private JPanel panelContenedorAdmon;

    public PanelAdministracion() {
        initComponents(); // Deja que NetBeans cargue su panel vacío
        iniciarDiseno(); // Ejecuta nuestro código manual para sobreescribirlo
    }

    /**
     * Este es NUESTRO método para armar la pantalla, esquivando el bloqueo de
     * NetBeans.
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
<<<<<<< HEAD
        panelSubMenu.setLayout(new FlowLayout(FlowLayout.LEFT, 15, 10));
        panelSubMenu.setBackground(new Color(255, 255, 255)); // Blanco puro
        panelSubMenu.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(220, 222, 225))); // Gris muy claro
                                                                                                       // para el borde

        // 3. Crear y estilizar los botones del sub-menú
        btnDatosEmpresa = crearBotonSubMenu("Datos de Empresa");
        btnUsuarios = crearBotonSubMenu("Gestión de Usuarios");
=======
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
>>>>>>> origin/parte-muoz

        panelSubMenu.add(btnDatosEmpresa);
        panelSubMenu.add(btnUsuarios);
        panelSubMenu.add(btnReportes);

<<<<<<< HEAD
        // 4. Crear el contenedor central (Aquí cargará PanelDatosEmpresa)
        panelContenedorAdmon = new JPanel();
        panelContenedorAdmon.setLayout(new BorderLayout());
        panelContenedorAdmon.setBackground(new Color(240, 242, 245)); // Gris Nube

        JLabel lblAdmon = new JLabel(
                "Gracias por confiar en ORION SYSTEMS. \nPor favor, seleccione una opción del menú superior",
                SwingConstants.CENTER);
        lblAdmon.setForeground(new Color(140, 145, 150)); // Gris suave
        lblAdmon.setFont(new Font("Segoe UI", Font.ITALIC, 18));
        panelContenedorAdmon.add(lblAdmon, BorderLayout.CENTER);

        // 5. Agregar paneles a este módulo
        this.add(panelSubMenu, BorderLayout.NORTH);
        this.add(panelContenedorAdmon, BorderLayout.CENTER);

        // 6. Configurar los Eventos
=======
        this.add(panelContenedorAdmon, BorderLayout.CENTER);
        this.add(panelSubMenu, BorderLayout.SOUTH);

        // 4. Configurar los Eventos con Carga Asíncrona
>>>>>>> origin/parte-muoz
        btnDatosEmpresa.addActionListener(e -> {
            abrirSubPanelAsync(() -> new PanelDatosEmpresa());
        });

        btnUsuarios.addActionListener(e -> {
            abrirSubPanelAsync(() -> new PanelGestionUsuarios());
        });

        btnReportes.addActionListener(e -> {
            abrirSubPanelAsync(() -> new PanelReportes());
        });

        abrirSubPanelAsync(() -> new PanelDatosEmpresa());
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
<<<<<<< HEAD
    }

    /**
     * Diseño elegante para los botones del sub-menú
     */
    private JButton crearBotonSubMenu(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        boton.setBackground(new Color(39, 174, 96)); // Verde Menta
        boton.setForeground(new Color(255, 255, 255)); // Blanco
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setPreferredSize(new Dimension(180, 35));
        boton.putClientProperty("JButton.buttonType", "roundRect");
        return boton;
    }

    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated
    // Code">//GEN-BEGIN:initComponents
=======
    }                       
   @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
>>>>>>> origin/parte-muoz
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
