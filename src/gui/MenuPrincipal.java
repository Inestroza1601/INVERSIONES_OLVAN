package gui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;

public class MenuPrincipal extends JFrame {

    private JPanel panelContenedor;
    private CardLayout cardLayout;
    private PanelLogin panelLogin;

    private JPanel panelCentral;
    private JPanel panelLateralIzquierdo;
    private JButton btnAdministracion;
    private JButton btnInventario;
    private JButton btnClientes;
    private JButton btnPuntoVenta;
    private JButton btnControlCaja;
    private JButton btnApartados;
    private JButton btnHistorialVentas;
    private JButton btnGarantias;
    private JButton btnCerrarSesion;
    private JButton botonActivo = null;
    private JButton btnEstadisticas;

    // --- PALETA DE COLORES "SOFT LIGHT" INVERSIONES OLVAN ---
    private final Color COLOR_FONDO_PRINCIPAL = new Color(240, 242, 245);
    private final Color COLOR_TEXTO_MENU = new Color(85, 85, 85);
    private final Color COLOR_VERDE_MENTA = new Color(39, 174, 96);
    private final Color COLOR_VERDE_CLARO = new Color(46, 204, 113, 25);
    private final Color COLOR_ROJO_LOGO = new Color(227, 0, 15);
    private final Color COLOR_ROJO_CLARO = new Color(227, 0, 15, 20);
    private final Color COLOR_BORDE_LATERAL = new Color(220, 222, 225);

    public MenuPrincipal() {
        setTitle("ORION SYSTEMS - ORGANIZACIÓN DE RECURSOS, INVENTARIO, OPERACIONES Y NEGOCIOS");
        try {
            java.net.URL imgURL = getClass().getResource("/image/logo.png");
            if (imgURL != null) {
                setIconImage(new javax.swing.ImageIcon(imgURL).getImage());
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo: " + e.getMessage());
        }
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO_PRINCIPAL);

        cardLayout = new CardLayout();
        panelContenedor = new JPanel(cardLayout);
        add(panelContenedor, BorderLayout.CENTER);

        // Iniciar con la pantalla de Login
        panelLogin = new PanelLogin(this);
        panelContenedor.add(panelLogin, "LOGIN");
        cardLayout.show(panelContenedor, "LOGIN");
    }

    public void iniciarEntornoApp() {
        JPanel panelApp = new JPanel(new BorderLayout());
        panelApp.setBackground(COLOR_FONDO_PRINCIPAL);

        // Panel Lateral
        panelLateralIzquierdo = new JPanel();
        panelLateralIzquierdo.setLayout(new BoxLayout(panelLateralIzquierdo, BoxLayout.Y_AXIS));
        panelLateralIzquierdo.setPreferredSize(new Dimension(240, 0));
        panelLateralIzquierdo.setBackground(COLOR_FONDO_PRINCIPAL);

        panelLateralIzquierdo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDE_LATERAL),
                BorderFactory.createEmptyBorder(40, 0, 40, 0)));

        JLabel lblMenu = new JLabel("MENÚ PRINCIPAL");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblMenu.setForeground(new Color(140, 145, 150));
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMenu.setBorder(BorderFactory.createEmptyBorder(0, 25, 20, 0));

        // Botones
        btnAdministracion = crearBotonWeb("Administración", new IconoMenu(1), false);
        btnClientes = crearBotonWeb("Clientes", new IconoMenu(2), false);
        btnInventario = crearBotonWeb("Inventario", new IconoMenu(3), false);
        btnPuntoVenta = crearBotonWeb("Punto de Venta", new IconoMenu(4), false);
        btnControlCaja = crearBotonWeb("Control de Caja", new IconoMenu(8), false);
        btnApartados = crearBotonWeb("Apartados", new IconoMenu(9), false);
        btnHistorialVentas = crearBotonWeb("Historial de Ventas", new IconoMenu(9), false);
        btnGarantias = crearBotonWeb("Garantías", new IconoMenu(5), false);
        btnEstadisticas = crearBotonWeb("Estadísticas", new IconoMenu(7), false);
        btnCerrarSesion = crearBotonWeb("Cerrar Sesión", new IconoMenu(6), true);

        int rolId = (utilidades.SesionGlobal.getUsuarioActual() != null)
                ? utilidades.SesionGlobal.getUsuarioActual().getIdRol()
                : 1;

        panelLateralIzquierdo.add(lblMenu);
        panelLateralIzquierdo.add(Box.createVerticalStrut(20));

        if (rolId != 3) {
            panelLateralIzquierdo.add(btnAdministracion);
            panelLateralIzquierdo.add(Box.createVerticalStrut(12));
        }
        panelLateralIzquierdo.add(btnClientes);
        panelLateralIzquierdo.add(Box.createVerticalStrut(12));
        panelLateralIzquierdo.add(btnInventario);
        panelLateralIzquierdo.add(Box.createVerticalStrut(12));
        panelLateralIzquierdo.add(btnPuntoVenta);
        panelLateralIzquierdo.add(Box.createVerticalStrut(12));
        panelLateralIzquierdo.add(btnControlCaja);
        panelLateralIzquierdo.add(Box.createVerticalStrut(12));
        panelLateralIzquierdo.add(btnApartados);
        panelLateralIzquierdo.add(Box.createVerticalStrut(12));
        panelLateralIzquierdo.add(btnHistorialVentas);
        panelLateralIzquierdo.add(Box.createVerticalStrut(12));
        panelLateralIzquierdo.add(btnGarantias);

        if (rolId != 3) {
            panelLateralIzquierdo.add(Box.createVerticalStrut(12));
            panelLateralIzquierdo.add(btnEstadisticas);
        }

        panelLateralIzquierdo.add(Box.createVerticalGlue());
        panelLateralIzquierdo.add(btnCerrarSesion);

        // Panel Central
        panelCentral = new JPanel();
        panelCentral.setLayout(new BorderLayout());
        panelCentral.setBackground(COLOR_FONDO_PRINCIPAL);
        if (rolId == 3) {
            panelCentral.add(new PanelPuntoVenta(), BorderLayout.CENTER);
            marcarBotonActivo(btnPuntoVenta, false);
        } else {
            panelCentral.add(new PanelEstadisticas(), BorderLayout.CENTER);
            marcarBotonActivo(btnEstadisticas, false);
        }

        panelApp.add(panelLateralIzquierdo, BorderLayout.WEST);
        panelApp.add(panelCentral, BorderLayout.CENTER);

        panelContenedor.add(panelApp, "APP");
        cardLayout.show(panelContenedor, "APP");

        // Eventos
        btnAdministracion.addActionListener(e -> mostrarPanelHijo(new PanelAdministracion()));
        btnClientes.addActionListener(e -> mostrarPanelHijo(new PanelGestionClientes()));
        btnInventario.addActionListener(e -> mostrarPanelHijo(new PanelInventario()));
        btnPuntoVenta.addActionListener(e -> mostrarPanelHijo(new PanelPuntoVenta()));
        btnControlCaja.addActionListener(e -> mostrarPanelHijo(new PanelControlCaja()));
        btnApartados.addActionListener(e -> mostrarPanelHijo(new PanelApartados()));
        btnHistorialVentas.addActionListener(e -> mostrarPanelHijo(new PanelHistorialVentas()));
        btnGarantias.addActionListener(e -> mostrarPanelHijo(new PanelGestionGarantias()));
        btnCerrarSesion.addActionListener(e -> {
            Object[] opciones = { "Sí, cerrar sesión", "Cancelar" };
            int opcion = JOptionPane.showOptionDialog(this,
                    "¿Estás seguro de que deseas cerrar tu sesión actual?",
                    "Confirmar Cierre de Sesión",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    opciones,
                    opciones[1]);

            if (opcion == JOptionPane.YES_OPTION) {
                utilidades.SesionGlobal.setUsuarioActual(null);
                utilidades.SesionGlobal.setEmpresaActual(null);
                botonActivo = null;
                panelLogin.limpiarCampos();
                cardLayout.show(panelContenedor, "LOGIN");
            }
        });
        btnEstadisticas.addActionListener(e -> mostrarPanelHijo(new PanelEstadisticas()));
    }

    public void mostrarPanelHijo(JPanel nuevoPanel) {
        panelCentral.removeAll();
        nuevoPanel.setBackground(COLOR_FONDO_PRINCIPAL);
        panelCentral.add(nuevoPanel, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Error al inicializar FlatLaf: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            new MenuPrincipal().setVisible(true);
        });
    }

    private JButton crearBotonWeb(String texto, Icon icono, boolean esPeligro) {
        JButton boton = new JButton(texto);

        boton.setIcon(icono);
        boton.setIconTextGap(15);

        boton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 55));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        boton.setBackground(COLOR_FONDO_PRINCIPAL);
        boton.setForeground(COLOR_TEXTO_MENU);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));

        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);

        javax.swing.border.Border bordeNormal = BorderFactory.createEmptyBorder(0, 25, 0, 0);
        boton.setBorder(bordeNormal);
        boton.putClientProperty("JButton.buttonType", "borderless");

        Color colorFondoHover = esPeligro ? COLOR_ROJO_CLARO : COLOR_VERDE_CLARO;
        Color colorLineaLateral = esPeligro ? COLOR_ROJO_LOGO : COLOR_VERDE_MENTA;
        Color colorTextoHover = esPeligro ? COLOR_ROJO_LOGO : COLOR_VERDE_MENTA;

        javax.swing.border.Border bordeHover = BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, colorLineaLateral),
                BorderFactory.createEmptyBorder(0, 20, 0, 0));

        boton.addActionListener(e -> marcarBotonActivo(boton, esPeligro));

        boton.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (boton != botonActivo) {
                    boton.setBackground(colorFondoHover);
                    boton.setForeground(colorTextoHover);
                    boton.setBorder(bordeHover);
                }
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (boton != botonActivo) {
                    boton.setBackground(COLOR_FONDO_PRINCIPAL);
                    boton.setForeground(COLOR_TEXTO_MENU);
                    boton.setBorder(bordeNormal);
                }
            }
        });

        return boton;
    }

    private class IconoMenu implements Icon {
        private int tipo;

        public IconoMenu(int tipo) {
            this.tipo = tipo;
        }

        @Override
        public int getIconWidth() {
            return 22;
        }

        @Override
        public int getIconHeight() {
            return 22;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (tipo) {
                case 1:
                    g2.drawLine(x + 2, y + 7, x + 20, y + 7);
                    g2.drawOval(x + 14, y + 4, 4, 5);
                    g2.drawLine(x + 2, y + 15, x + 20, y + 15);
                    g2.drawOval(x + 4, y + 12, 4, 5);
                    break;
                case 2:
                    g2.drawOval(x + 7, y + 2, 8, 8);
                    g2.drawArc(x + 2, y + 10, 18, 14, 0, 180);
                    break;
                case 3:
                    g2.drawRect(x + 3, y + 5, 16, 13);
                    g2.drawLine(x + 3, y + 10, x + 19, y + 10);
                    g2.drawLine(x + 11, y + 10, x + 11, y + 18);
                    break;
                case 4:
                    g2.drawPolygon(new int[] { x + 3, x + 11, x + 19, x + 11 },
                            new int[] { y + 11, y + 3, y + 11, y + 19 }, 4);
                    g2.drawOval(x + 7, y + 10, 2, 2);
                    break;
                case 5:
                    g2.drawPolygon(new int[] { x + 3, x + 19, x + 19, x + 11, x + 3 },
                            new int[] { y + 3, y + 3, y + 11, y + 19, y + 11 }, 5);
                    g2.drawLine(x + 8, y + 10, x + 11, y + 13);
                    g2.drawLine(x + 11, y + 13, x + 15, y + 7);
                    break;
                case 6:
                    g2.drawArc(x + 4, y + 4, 14, 14, -240, 300);
                    g2.drawLine(x + 11, y + 2, x + 11, y + 10);
                    break;
                case 7:
                    g2.drawLine(x + 2, y + 19, x + 20, y + 19);
                    g2.fillRect(x + 4, y + 10, 3, 9);
                    g2.fillRect(x + 10, y + 4, 3, 15);
                    g2.fillRect(x + 16, y + 13, 3, 6);
                    break;
                case 8:
                    g2.drawRect(x + 3, y + 5, 16, 12);
                    g2.drawLine(x + 3, y + 11, x + 19, y + 11);
                    g2.drawOval(x + 10, y + 7, 2, 2);
                    g2.drawLine(x + 8, y + 14, x + 14, y + 14);
                    break;
                case 9:
                    g2.drawRect(x + 4, y + 3, 14, 16);
                    g2.drawLine(x + 7, y + 7, x + 15, y + 7);
                    g2.drawLine(x + 7, y + 11, x + 15, y + 11);
                    g2.drawLine(x + 7, y + 15, x + 12, y + 15);
                    break;
            }
            g2.dispose();
        }
    }

    private void marcarBotonActivo(JButton botonSeleccionado, boolean esPeligro) {
        if (botonActivo != null && botonActivo != botonSeleccionado) {
            botonActivo.setBackground(COLOR_FONDO_PRINCIPAL);
            botonActivo.setForeground(COLOR_TEXTO_MENU);
            botonActivo.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        }

        botonActivo = botonSeleccionado;

        Color colorFondoHover = esPeligro ? COLOR_ROJO_CLARO : COLOR_VERDE_CLARO;
        Color colorLineaLateral = esPeligro ? COLOR_ROJO_LOGO : COLOR_VERDE_MENTA;
        Color colorTextoActivo = esPeligro ? COLOR_ROJO_LOGO : COLOR_VERDE_MENTA;

        botonActivo.setBackground(colorFondoHover);
        botonActivo.setForeground(colorTextoActivo);
        botonActivo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, colorLineaLateral),
                BorderFactory.createEmptyBorder(0, 20, 0, 0)));
    }
}
