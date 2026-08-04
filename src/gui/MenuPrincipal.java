package gui;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
    private JButton btnEstadisticas;
    private JButton btnCerrarSesion;
    private JButton botonActivo = null;

    // --- PALETA CROMÁTICA OFICIAL: VERDE Y BLANCO ---
    private final Color COLOR_FONDO_SIDEBAR = new Color(255, 255, 255); // Blanco Puro
    private final Color COLOR_FONDO_APP = new Color(248, 250, 252);     // Fondo suave
    private final Color COLOR_TEXTO_MENU = new Color(71, 85, 105);      // Gris pizarra legible
    private final Color COLOR_VERDE_EMERALD = new Color(16, 185, 129);  // Verde Esmeralda
    private final Color COLOR_VERDE_OSCURO = new Color(5, 150, 105);    // Verde Esmeralda oscuro
    private final Color COLOR_VERDE_HOVER = new Color(236, 253, 245);   // Menta suave
    private final Color COLOR_ROJO_PELIGRO = new Color(239, 68, 68);
    private final Color COLOR_ROJO_HOVER = new Color(254, 242, 242);
    private final Color COLOR_ROJO_TEXTO = new Color(220, 38, 38);
    private final Color COLOR_BORDE_LATERAL = new Color(226, 232, 240);

    public MenuPrincipal() {
        setTitle("INVERSIONES OLVAN - SISTEMA INTEGRAL");
        try {
            java.net.URL imgURL = getClass().getResource("/image/logo.png");
            if (imgURL != null) {
                setIconImage(new javax.swing.ImageIcon(imgURL).getImage());
            }
        } catch (Exception e) {
            System.err.println("No se pudo cargar el logo: " + e.getMessage());
        }
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setSize(1200, 800);
        setMinimumSize(new Dimension(1024, 700));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(COLOR_FONDO_APP);

        cardLayout = new CardLayout();
        panelContenedor = new JPanel(cardLayout);
        add(panelContenedor, BorderLayout.CENTER);

        // Iniciar con la pantalla de Login
        panelLogin = new PanelLogin(this);
        panelContenedor.add(panelLogin, "LOGIN");
        cardLayout.show(panelContenedor, "LOGIN");
    }

    public void iniciarEntornoApp() {
        botonActivo = null;

        JPanel panelApp = new JPanel(new BorderLayout());
        panelApp.setBackground(COLOR_FONDO_APP);

        // Panel Lateral Izquierdo (Blanco y Verde)
        panelLateralIzquierdo = new JPanel();
        panelLateralIzquierdo.setLayout(new BoxLayout(panelLateralIzquierdo, BoxLayout.Y_AXIS));
        panelLateralIzquierdo.setPreferredSize(new Dimension(250, 0));
        panelLateralIzquierdo.setBackground(COLOR_FONDO_SIDEBAR);
        panelLateralIzquierdo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 0, 1, COLOR_BORDE_LATERAL),
                BorderFactory.createEmptyBorder(25, 0, 20, 0)));

        // Cabecera con Marca
        JLabel lblEmpresa = new JLabel("INVERSIONES OLVAN");
        lblEmpresa.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblEmpresa.setForeground(COLOR_VERDE_OSCURO);
        lblEmpresa.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblEmpresa.setBorder(BorderFactory.createEmptyBorder(0, 25, 2, 0));

        JLabel lblOnline = new JLabel("● SISTEMA ONLINE");
        lblOnline.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblOnline.setForeground(COLOR_VERDE_EMERALD);
        lblOnline.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblOnline.setBorder(BorderFactory.createEmptyBorder(0, 25, 15, 0));

        JLabel lblMenu = new JLabel("MENÚ PRINCIPAL");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMenu.setForeground(new Color(148, 163, 184));
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMenu.setBorder(BorderFactory.createEmptyBorder(0, 25, 12, 0));

        // Creación de botones con micro-animaciones fluidas
        btnAdministracion = crearBotonWebAnimado("Administración", new IconoMenu(1), false);
        btnClientes = crearBotonWebAnimado("Clientes", new IconoMenu(2), false);
        btnInventario = crearBotonWebAnimado("Inventario", new IconoMenu(3), false);
        btnPuntoVenta = crearBotonWebAnimado("Punto de Venta", new IconoMenu(4), false);
        btnControlCaja = crearBotonWebAnimado("Control de Caja", new IconoMenu(8), false);
        btnApartados = crearBotonWebAnimado("Apartados", new IconoMenu(9), false);
        btnHistorialVentas = crearBotonWebAnimado("Historial de Ventas", new IconoMenu(9), false);
        btnGarantias = crearBotonWebAnimado("Garantías", new IconoMenu(5), false);
        btnEstadisticas = crearBotonWebAnimado("Estadísticas", new IconoMenu(7), false);
        btnCerrarSesion = crearBotonWebAnimado("Cerrar Sesión", new IconoMenu(6), true);

        int rolId = (utilidades.SesionGlobal.getUsuarioActual() != null)
                ? utilidades.SesionGlobal.getUsuarioActual().getIdRol()
                : 1;

        panelLateralIzquierdo.add(lblEmpresa);
        panelLateralIzquierdo.add(lblOnline);
        panelLateralIzquierdo.add(lblMenu);
        panelLateralIzquierdo.add(Box.createVerticalStrut(4));

        if (rolId != 3) {
            panelLateralIzquierdo.add(btnAdministracion);
            panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        }
        panelLateralIzquierdo.add(btnClientes);
        panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        panelLateralIzquierdo.add(btnInventario);
        panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        panelLateralIzquierdo.add(btnPuntoVenta);
        panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        panelLateralIzquierdo.add(btnControlCaja);
        panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        panelLateralIzquierdo.add(btnApartados);
        panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        panelLateralIzquierdo.add(btnHistorialVentas);
        panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        panelLateralIzquierdo.add(btnGarantias);

        if (rolId != 3) {
            panelLateralIzquierdo.add(Box.createVerticalStrut(4));
            panelLateralIzquierdo.add(btnEstadisticas);
        }

        panelLateralIzquierdo.add(Box.createVerticalGlue());
        panelLateralIzquierdo.add(btnCerrarSesion);

        // Panel Central
        panelCentral = new JPanel();
        panelCentral.setLayout(new BorderLayout());
        panelCentral.setBackground(COLOR_FONDO_APP);

        if (rolId == 3) {
            mostrarPanelHijo(new PanelPuntoVenta());
            marcarBotonActivo(btnPuntoVenta, false);
        } else {
            mostrarPanelHijo(new PanelEstadisticas());
            marcarBotonActivo(btnEstadisticas, false);
        }

        panelApp.add(panelLateralIzquierdo, BorderLayout.WEST);
        panelApp.add(panelCentral, BorderLayout.CENTER);

        panelContenedor.add(panelApp, "APP");
        cardLayout.show(panelContenedor, "APP");

        // Eventos de apertura de módulos con animación
        btnAdministracion.addActionListener(e -> mostrarPanelHijo(new PanelAdministracion()));
        btnClientes.addActionListener(e -> mostrarPanelHijo(new PanelGestionClientes()));
        btnInventario.addActionListener(e -> mostrarPanelHijo(new PanelInventario()));
        btnPuntoVenta.addActionListener(e -> mostrarPanelHijo(new PanelPuntoVenta()));
        btnControlCaja.addActionListener(e -> mostrarPanelHijo(new PanelControlCaja()));
        btnApartados.addActionListener(e -> mostrarPanelHijo(new PanelApartados()));
        btnHistorialVentas.addActionListener(e -> mostrarPanelHijo(new PanelHistorialVentas()));
        btnGarantias.addActionListener(e -> mostrarPanelHijo(new PanelGestionGarantias()));
        btnEstadisticas.addActionListener(e -> mostrarPanelHijo(new PanelEstadisticas()));

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
    }

    // =========================================================================
    // ANIMACIÓN DE TRANSICIÓN AL ABRIR VENTANAS (FADE-IN + SLIDE SUAVE)
    // =========================================================================
    public void mostrarPanelHijo(JPanel nuevoPanel) {
        panelCentral.removeAll();
        nuevoPanel.setBackground(COLOR_FONDO_APP);

        PanelTransicionSuave contenedorAnimado = new PanelTransicionSuave(nuevoPanel);
        panelCentral.add(contenedorAnimado, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
    }

    // =========================================================================
    // CREACIÓN DE BOTONES CON ANIMACIÓN SUAVE DE HOVER (INTERPOLACIÓN 60 FPS)
    // =========================================================================
    private JButton crearBotonWebAnimado(String texto, Icon icono, boolean esPeligro) {
        JButton boton = new JButton(texto);
        boton.setIcon(icono);
        boton.setIconTextGap(14);
        boton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 46));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));

        boton.setBackground(COLOR_FONDO_SIDEBAR);
        boton.setForeground(COLOR_TEXTO_MENU);
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setHorizontalAlignment(SwingConstants.LEFT);
        boton.setAlignmentX(Component.LEFT_ALIGNMENT);

        javax.swing.border.Border bordeNormal = BorderFactory.createEmptyBorder(0, 25, 0, 0);
        boton.setBorder(bordeNormal);
        boton.putClientProperty("JButton.buttonType", "borderless");

        Color targetColorFondo = esPeligro ? COLOR_ROJO_HOVER : COLOR_VERDE_HOVER;
        Color targetColorBorde = esPeligro ? COLOR_ROJO_PELIGRO : COLOR_VERDE_EMERALD;
        Color targetColorTexto = esPeligro ? COLOR_ROJO_TEXTO : COLOR_VERDE_OSCURO;

        // Controlador de micro-animación fluida de color y barra indicadora
        class AnimadorHover {
            private float progreso = 0.0f;
            private Timer timer;

            void animar(boolean entrar) {
                if (boton == botonActivo) return;
                if (timer != null && timer.isRunning()) timer.stop();

                timer = new Timer(15, e -> {
                    if (entrar) {
                        progreso += 0.20f;
                        if (progreso >= 1.0f) {
                            progreso = 1.0f;
                            timer.stop();
                        }
                    } else {
                        progreso -= 0.20f;
                        if (progreso <= 0.0f) {
                            progreso = 0.0f;
                            timer.stop();
                        }
                    }

                    if (boton != botonActivo) {
                        Color bg = mezclarColores(COLOR_FONDO_SIDEBAR, targetColorFondo, progreso);
                        Color fg = mezclarColores(COLOR_TEXTO_MENU, targetColorTexto, progreso);
                        int anchoBorde = (int) (5 * progreso);

                        boton.setBackground(bg);
                        boton.setForeground(fg);
                        if (anchoBorde > 0) {
                            boton.setBorder(BorderFactory.createCompoundBorder(
                                    BorderFactory.createMatteBorder(0, anchoBorde, 0, 0, targetColorBorde),
                                    BorderFactory.createEmptyBorder(0, 25 - anchoBorde, 0, 0)));
                        } else {
                            boton.setBorder(bordeNormal);
                        }
                        boton.repaint();
                    }
                });
                timer.start();
            }
        }

        AnimadorHover animador = new AnimadorHover();

        boton.addActionListener(e -> marcarBotonActivo(boton, esPeligro));

        boton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                animador.animar(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animador.animar(false);
            }
        });

        return boton;
    }

    private void marcarBotonActivo(JButton botonSeleccionado, boolean esPeligro) {
        if (botonActivo != null && botonActivo != botonSeleccionado) {
            botonActivo.setBackground(COLOR_FONDO_SIDEBAR);
            botonActivo.setForeground(COLOR_TEXTO_MENU);
            botonActivo.setBorder(BorderFactory.createEmptyBorder(0, 25, 0, 0));
        }

        botonActivo = botonSeleccionado;

        Color colorFondoActivo = esPeligro ? COLOR_ROJO_HOVER : COLOR_VERDE_HOVER;
        Color colorLineaLateral = esPeligro ? COLOR_ROJO_PELIGRO : COLOR_VERDE_EMERALD;
        Color colorTextoActivo = esPeligro ? COLOR_ROJO_TEXTO : COLOR_VERDE_OSCURO;

        botonActivo.setBackground(colorFondoActivo);
        botonActivo.setForeground(colorTextoActivo);
        botonActivo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 5, 0, 0, colorLineaLateral),
                BorderFactory.createEmptyBorder(0, 20, 0, 0)));
    }

    private static Color mezclarColores(Color c1, Color c2, float fraccion) {
        fraccion = Math.max(0.0f, Math.min(1.0f, fraccion));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * fraccion);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * fraccion);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * fraccion);
        return new Color(r, g, b);
    }

    // =========================================================================
    // PANEL CONTENEDOR CON ANIMACIÓN SUAVE DE ENTRADA (FADE-IN Y SLIDE)
    // =========================================================================
    private class PanelTransicionSuave extends JPanel {
        private float opacidad = 0.05f;
        private int desplazamientoY = 10;
        private Timer timerTransicion;

        public PanelTransicionSuave(JPanel hijo) {
            setLayout(new BorderLayout());
            setBackground(COLOR_FONDO_APP);
            add(hijo, BorderLayout.CENTER);

            timerTransicion = new Timer(14, e -> {
                opacidad += 0.15f;
                desplazamientoY = Math.max(0, (int) (10 * (1.0f - opacidad)));
                if (opacidad >= 1.0f) {
                    opacidad = 1.0f;
                    desplazamientoY = 0;
                    timerTransicion.stop();
                }
                repaint();
            });
            timerTransicion.start();
        }

        @Override
        public void paint(Graphics g) {
            if (opacidad >= 1.0f) {
                super.paint(g);
                return;
            }
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Fondo limpio
            g2.setColor(getBackground());
            g2.fillRect(0, 0, getWidth(), getHeight());

            // Efecto de aparición con transparencia gradual y deslizamiento suave
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.min(1.0f, Math.max(0.0f, opacidad))));
            g2.translate(0, desplazamientoY);
            super.paint(g2);
            g2.dispose();
        }
    }

    // =========================================================================
    // ÍCONOS VECTORIALES NÍPIDOS DEL MENÚ
    // =========================================================================
    private class IconoMenu implements Icon {
        private int tipo;

        public IconoMenu(int tipo) {
            this.tipo = tipo;
        }

        @Override
        public int getIconWidth() {
            return 20;
        }

        @Override
        public int getIconHeight() {
            return 20;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            g2.setColor(c.getForeground());
            g2.setStroke(new BasicStroke(1.9f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            switch (tipo) {
                case 1: // Administración
                    g2.drawLine(x + 2, y + 6, x + 18, y + 6);
                    g2.drawOval(x + 12, y + 3, 4, 5);
                    g2.drawLine(x + 2, y + 14, x + 18, y + 14);
                    g2.drawOval(x + 4, y + 11, 4, 5);
                    break;
                case 2: // Clientes
                    g2.drawOval(x + 6, y + 1, 8, 8);
                    g2.drawArc(x + 1, y + 9, 18, 12, 0, 180);
                    break;
                case 3: // Inventario
                    g2.drawRect(x + 2, y + 4, 16, 13);
                    g2.drawLine(x + 2, y + 9, x + 18, y + 9);
                    g2.drawLine(x + 10, y + 9, x + 10, y + 17);
                    break;
                case 4: // Punto de Venta
                    g2.drawPolygon(new int[] { x + 2, x + 10, x + 18, x + 10 },
                            new int[] { y + 10, y + 2, y + 10, y + 18 }, 4);
                    g2.drawOval(x + 6, y + 9, 2, 2);
                    break;
                case 5: // Garantías
                    g2.drawPolygon(new int[] { x + 2, x + 18, x + 18, x + 10, x + 2 },
                            new int[] { y + 2, y + 2, y + 10, y + 18, y + 10 }, 5);
                    g2.drawLine(x + 7, y + 9, x + 10, y + 12);
                    g2.drawLine(x + 10, y + 12, x + 14, y + 6);
                    break;
                case 6: // Cerrar Sesión
                    g2.drawArc(x + 3, y + 3, 14, 14, -240, 300);
                    g2.drawLine(x + 10, y + 1, x + 10, y + 9);
                    break;
                case 7: // Estadísticas
                    g2.drawLine(x + 2, y + 18, x + 18, y + 18);
                    g2.fillRect(x + 3, y + 9, 3, 9);
                    g2.fillRect(x + 8, y + 4, 3, 14);
                    g2.fillRect(x + 13, y + 12, 3, 6);
                    break;
                case 8: // Control de Caja
                    g2.drawRect(x + 2, y + 4, 16, 12);
                    g2.drawLine(x + 2, y + 10, x + 18, y + 10);
                    g2.drawOval(x + 9, y + 6, 2, 2);
                    g2.drawLine(x + 7, y + 13, x + 13, y + 13);
                    break;
                case 9: // Historial / Apartados
                    g2.drawRect(x + 3, y + 2, 14, 16);
                    g2.drawLine(x + 6, y + 6, x + 14, y + 6);
                    g2.drawLine(x + 6, y + 10, x + 14, y + 10);
                    g2.drawLine(x + 6, y + 14, x + 11, y + 14);
                    break;
            }
            g2.dispose();
        }
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
}
