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

    // --- PALETA CROMÁTICA OFICIAL: VERDE VINTAGE ELEGANTE Y BLANCO ---
    private final Color COLOR_FONDO_SIDEBAR = new Color(213, 233, 222);     // Verde Pastel Salvia para Sidebar
    private final Color COLOR_FONDO_APP = new Color(232, 243, 236);         // Verde Pastel Suave para Fondos
    private final Color COLOR_TEXTO_MENU = new Color(28, 59, 45);          // Forest vintage suave para opciones
    private final Color COLOR_VERDE_EMERALD = new Color(45, 106, 79);      // Verde Bosque Vintage Elegante
    private final Color COLOR_VERDE_OSCURO = Color.BLACK;                  // Negro puro para opción activa
    private final Color COLOR_VERDE_HOVER = new Color(192, 218, 204);      // Verde vintage suave hover
    private final Color COLOR_ROJO_PELIGRO = new Color(239, 68, 68);
    private final Color COLOR_ROJO_HOVER = new Color(254, 226, 226);
    private final Color COLOR_ROJO_TEXTO = new Color(185, 28, 28);
    private final Color COLOR_BORDE_LATERAL = new Color(180, 208, 192);

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

        // Panel Lateral Izquierdo (Pastel y Blanco)
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
        lblEmpresa.setForeground(new Color(19, 58, 42));
        lblEmpresa.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblEmpresa.setBorder(BorderFactory.createEmptyBorder(0, 25, 2, 0));

        JLabel lblOnline = new JLabel("● SISTEMA ONLINE");
        lblOnline.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lblOnline.setForeground(new Color(22, 101, 52));
        lblOnline.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblOnline.setBorder(BorderFactory.createEmptyBorder(0, 25, 15, 0));

        JLabel lblMenu = new JLabel("MENÚ PRINCIPAL");
        lblMenu.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblMenu.setForeground(new Color(75, 115, 95));
        lblMenu.setAlignmentX(Component.LEFT_ALIGNMENT);
        lblMenu.setBorder(BorderFactory.createEmptyBorder(0, 25, 12, 0));

        // Creación de botones con micro-animaciones fluidas
        btnAdministracion = crearBotonWebAnimado("Administración", new IconoMenu(1), false);
        btnClientes = crearBotonWebAnimado("Clientes", new IconoMenu(2), false);
        btnInventario = crearBotonWebAnimado("Inventario", new IconoMenu(3), false);
        btnPuntoVenta = crearBotonWebAnimado("Punto de Venta", new IconoMenu(4), false);
        btnControlCaja = crearBotonWebAnimado("Control de Caja", new IconoMenu(8), false);
        btnApartados = crearBotonWebAnimado("Apartados", new IconoMenu(10), false);
        btnHistorialVentas = crearBotonWebAnimado("Historial de Ventas", new IconoMenu(9), false);
        btnGarantias = crearBotonWebAnimado("Garantías", new IconoMenu(5), false);
        btnEstadisticas = crearBotonWebAnimado("Estadísticas", new IconoMenu(7), false);
        btnCerrarSesion = crearBotonWebAnimado("Cerrar Sesión", new IconoMenu(6), true);

        modelo.Usuario uAct = utilidades.SesionGlobal.getUsuarioActual();
        
        boolean pAdmin       = (uAct == null) ? true : uAct.tienePermiso("VER_ADMINISTRACION");
        boolean pClientes    = (uAct == null) ? true : uAct.tienePermiso("VER_CLIENTES");
        boolean pInventario  = (uAct == null) ? true : uAct.tienePermiso("VER_INVENTARIO");
        boolean pPOS         = (uAct == null) ? true : uAct.tienePermiso("VER_POS");
        boolean pCaja        = (uAct == null) ? true : uAct.tienePermiso("VER_CAJA");
        boolean pApartados   = (uAct == null) ? true : uAct.tienePermiso("VER_APARTADOS");
        boolean pVentas      = (uAct == null) ? true : uAct.tienePermiso("VER_VENTAS");
        boolean pGarantias   = (uAct == null) ? true : uAct.tienePermiso("VER_GARANTIAS");
        boolean pEstadisticas= (uAct == null) ? true : uAct.tienePermiso("VER_ESTADISTICAS");

        panelLateralIzquierdo.add(lblEmpresa);
        panelLateralIzquierdo.add(lblOnline);
        panelLateralIzquierdo.add(lblMenu);
        panelLateralIzquierdo.add(Box.createVerticalStrut(4));

        if (pAdmin) {
            panelLateralIzquierdo.add(btnAdministracion);
            panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        }
        if (pClientes) {
            panelLateralIzquierdo.add(btnClientes);
            panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        }
        if (pInventario) {
            panelLateralIzquierdo.add(btnInventario);
            panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        }
        if (pPOS) {
            panelLateralIzquierdo.add(btnPuntoVenta);
            panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        }
        if (pCaja) {
            panelLateralIzquierdo.add(btnControlCaja);
            panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        }
        if (pApartados) {
            panelLateralIzquierdo.add(btnApartados);
            panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        }
        if (pVentas) {
            panelLateralIzquierdo.add(btnHistorialVentas);
            panelLateralIzquierdo.add(Box.createVerticalStrut(4));
        }
        if (pGarantias) {
            panelLateralIzquierdo.add(btnGarantias);
        }
        if (pEstadisticas) {
            panelLateralIzquierdo.add(Box.createVerticalStrut(4));
            panelLateralIzquierdo.add(btnEstadisticas);
        }

        panelLateralIzquierdo.add(Box.createVerticalGlue());
        panelLateralIzquierdo.add(btnCerrarSesion);

        // Panel Central
        panelCentral = new JPanel();
        panelCentral.setLayout(new BorderLayout());
        panelCentral.setBackground(COLOR_FONDO_APP);

        if (pEstadisticas) {
            abrirPanelAsync(() -> new PanelEstadisticas());
            marcarBotonActivo(btnEstadisticas, false);
        } else if (pPOS) {
            abrirPanelAsync(() -> new PanelPuntoVenta());
            marcarBotonActivo(btnPuntoVenta, false);
        } else if (pInventario) {
            abrirPanelAsync(() -> new PanelInventario());
            marcarBotonActivo(btnInventario, false);
        }

        panelApp.add(panelLateralIzquierdo, BorderLayout.WEST);
        panelApp.add(panelCentral, BorderLayout.CENTER);

        PanelTransicionSuave appAnimada = new PanelTransicionSuave(panelApp);
        panelContenedor.add(appAnimada, "APP");
        cardLayout.show(panelContenedor, "APP");

        // Eventos de apertura de módulos con animación y carga asíncrona
        btnAdministracion.addActionListener(e -> abrirPanelAsync(() -> new PanelAdministracion()));
        btnClientes.addActionListener(e -> abrirPanelAsync(() -> new PanelGestionClientes()));
        btnInventario.addActionListener(e -> abrirPanelAsync(() -> new PanelInventario()));
        btnPuntoVenta.addActionListener(e -> abrirPanelAsync(() -> new PanelPuntoVenta()));
        btnControlCaja.addActionListener(e -> abrirPanelAsync(() -> new PanelControlCaja()));
        btnApartados.addActionListener(e -> abrirPanelAsync(() -> new PanelApartados()));
        btnHistorialVentas.addActionListener(e -> abrirPanelAsync(() -> new PanelHistorialVentas()));
        btnGarantias.addActionListener(e -> abrirPanelAsync(() -> new PanelGestionGarantias()));
        btnEstadisticas.addActionListener(e -> abrirPanelAsync(() -> new PanelEstadisticas()));

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
    // CARGA ASÍNCRONA DE PANELES (NUEVO SISTEMA ANTI-FREEZE)
    // =========================================================================
    public void abrirPanelAsync(java.util.function.Supplier<JPanel> panelSupplier) {
        panelCentral.removeAll();
        
        PanelCargaOverlay loader = new PanelCargaOverlay("Cargando módulo...");
        panelCentral.add(loader, BorderLayout.CENTER);
        panelCentral.revalidate();
        panelCentral.repaint();
        loader.iniciarAnimacion();

        SwingWorker<JPanel, Void> worker = new SwingWorker<JPanel, Void>() {
            @Override
            protected JPanel doInBackground() throws Exception {
                // Instancia el panel en segundo plano (las DB queries del constructor corren aquí)
                return panelSupplier.get();
            }

            @Override
            protected void done() {
                try {
                    JPanel nuevoPanel = get();
                    loader.detenerAnimacion();
                    mostrarPanelHijo(nuevoPanel); // Reutilizamos mostrarPanelHijo para la transición de entrada
                } catch (Exception e) {
                    e.printStackTrace();
                    loader.detenerAnimacion();
                    panelCentral.removeAll();
                    JLabel lblError = new JLabel("Error al cargar el módulo: " + e.getMessage());
                    lblError.setForeground(COLOR_ROJO_TEXTO);
                    lblError.setHorizontalAlignment(SwingConstants.CENTER);
                    panelCentral.add(lblError, BorderLayout.CENTER);
                    panelCentral.revalidate();
                    panelCentral.repaint();
                }
            }
        };
        worker.execute();
    }

    // =========================================================================
    // CREACIÓN DE BOTONES CON ANIMACIÓN SUAVE DE HOVER (INTERPOLACIÓN 60 FPS)
    // =========================================================================
    private JButton crearBotonWebAnimado(String texto, Icon icono, boolean esPeligro) {
        JButton boton = new JButton(texto);
        boton.setIcon(icono);
        boton.setIconTextGap(16);
        boton.setPreferredSize(new Dimension(Integer.MAX_VALUE, 50));
        boton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        boton.setFont(new Font("Segoe UI", Font.BOLD, 16));

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
        Color targetColorTexto = Color.BLACK; // Texto negro al pasar el cursor (hover)

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

            @Override
            public void mousePressed(MouseEvent e) {
                boton.setForeground(Color.BLACK); // Texto negro al presionar / click
                boton.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (boton == botonActivo) {
                    boton.setForeground(Color.BLACK);
                }
                boton.repaint();
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

        Color colorFondoActivo = esPeligro ? COLOR_ROJO_HOVER : Color.WHITE;
        Color colorLineaLateral = esPeligro ? COLOR_ROJO_PELIGRO : COLOR_VERDE_EMERALD;
        Color colorTextoActivo = esPeligro ? COLOR_ROJO_TEXTO : Color.BLACK;

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
                case 9: // Historial de Ventas
                    g2.drawRect(x + 3, y + 2, 14, 16);
                    g2.drawLine(x + 6, y + 6, x + 14, y + 6);
                    g2.drawLine(x + 6, y + 10, x + 14, y + 10);
                    g2.drawLine(x + 6, y + 14, x + 11, y + 14);
                    break;
                case 10: // Apartados (Reloj de espera)
                    g2.drawOval(x + 3, y + 3, 14, 14);
                    g2.drawLine(x + 10, y + 5, x + 10, y + 10);
                    g2.drawLine(x + 10, y + 10, x + 14, y + 10);
                    break;
            }
            g2.dispose();
        }
    }

    public static void main(String[] args) {
        try {
            // --- CONFIGURACIÓN GLOBAL: TEMA VERDE PASTEL ELEGANTE Y BLANCO ---
            UIManager.put("Button.arc", 12);
            UIManager.put("Component.arc", 12);
            UIManager.put("ProgressBar.arc", 12);
            UIManager.put("TextComponent.arc", 10);
            UIManager.put("ScrollBar.showButtons", true);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.thumbInsets", new Insets(2, 2, 2, 2));
            UIManager.put("ScrollBar.thumb", new Color(185, 215, 198)); // Verde pastel medio
            UIManager.put("ScrollBar.hoverThumbColor", new Color(42, 157, 114)); // Esmeralda al pasar cursor

            // Fondo general de paneles y ventanas emergentes (diálogos, popups, alert boxes)
            UIManager.put("Panel.background", new Color(232, 243, 236));
            UIManager.put("ScrollPane.background", new Color(232, 243, 236));
            UIManager.put("Viewport.background", new Color(255, 255, 255));
            UIManager.put("Dialog.background", new Color(232, 243, 236));
            UIManager.put("OptionPane.background", new Color(232, 243, 236));
            UIManager.put("OptionPane.messageForeground", new Color(19, 58, 42));
            UIManager.put("PopupMenu.background", new Color(255, 255, 255));
            UIManager.put("PopupMenu.borderColor", new Color(180, 208, 192));

            // Estilos de botones interactivos y texto negro al pasar el cursor (hover) y presionar
            UIManager.put("Button.background", new Color(255, 255, 255));
            UIManager.put("Button.foreground", new Color(30, 41, 59));
            UIManager.put("Button.hoverBackground", new Color(215, 235, 225));
            UIManager.put("Button.hoverForeground", Color.BLACK);
            UIManager.put("Button.pressedBackground", new Color(185, 220, 200));
            UIManager.put("Button.pressedForeground", Color.BLACK);
            UIManager.put("Button.hoverBorderColor", new Color(45, 106, 79));
            UIManager.put("Button.focusedBorderColor", new Color(45, 106, 79));
            UIManager.put("Button.borderColor", new Color(180, 208, 192));

            UIManager.put("Button.default.background", new Color(45, 106, 79));
            UIManager.put("Button.default.hoverBackground", new Color(30, 77, 56));
            UIManager.put("Button.default.hoverForeground", Color.BLACK);
            UIManager.put("Button.default.pressedBackground", new Color(165, 205, 185));
            UIManager.put("Button.default.focusedBackground", new Color(45, 106, 79));
            UIManager.put("Button.default.foreground", Color.WHITE);
            UIManager.put("Button.default.pressedForeground", Color.BLACK);
            UIManager.put("Button.default.borderColor", new Color(30, 77, 56));

            // Componentes de entrada
            UIManager.put("Component.focusColor", new Color(45, 106, 79, 90));
            UIManager.put("Component.focusedBorderColor", new Color(45, 106, 79));
            UIManager.put("TextField.focusedBorderColor", new Color(45, 106, 79));
            UIManager.put("PasswordField.focusedBorderColor", new Color(45, 106, 79));
            UIManager.put("ComboBox.focusedBorderColor", new Color(45, 106, 79));
            UIManager.put("Spinner.focusedBorderColor", new Color(45, 106, 79));

            // --- TABLAS BLANCAS NÍTIDAS SOBRE FONDO VERDE PASTEL ---
            UIManager.put("TableHeader.background", new Color(240, 248, 244)); // Fondo claro nítido
            UIManager.put("TableHeader.foreground", new Color(19, 58, 42));     // Verde forestal elegante
            UIManager.put("TableHeader.separatorColor", new Color(210, 230, 220));
            UIManager.put("TableHeader.bottomSeparatorColor", new Color(42, 157, 114)); // Línea viva esmeralda
            UIManager.put("TableHeader.height", 42);

            UIManager.put("Table.background", new Color(255, 255, 255)); // Tablas 100% Blancas
            UIManager.put("Table.foreground", new Color(30, 41, 59));
            UIManager.put("Table.selectionBackground", new Color(205, 235, 218)); // Menta seleccionada luminosa
            UIManager.put("Table.selectionForeground", Color.BLACK);             // Negro texto activo
            UIManager.put("Table.alternateRowColor", new Color(246, 251, 248));   // Rayado blanco alterno muy suave
            UIManager.put("Table.gridColor", new Color(235, 242, 238));
            UIManager.put("Table.showHorizontalLines", true);
            UIManager.put("Table.showVerticalLines", false);

            UIManager.put("List.selectionBackground", new Color(205, 235, 218));
            UIManager.put("List.selectionForeground", Color.BLACK);

            UIManager.put("TabbedPane.selectedBackground", new Color(255, 255, 255));
            UIManager.put("TabbedPane.selectedForeground", Color.BLACK);
            UIManager.put("TabbedPane.underlineColor", new Color(42, 157, 114));

            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Error al inicializar FlatLaf: " + ex.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            new MenuPrincipal().setVisible(true);
        });
    }
}
