package gui;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class PanelLogin extends JPanel {
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnEntrar;
    private MenuPrincipal menuPrincipal;
    private JPanel pnlTarjeta;

    private float tarjetaAlpha = 0.0f;
    private int tarjetaOffsetY = 15;
    private int tarjetaOffsetX = 0;
    private Timer animTimer;

    public PanelLogin(MenuPrincipal menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        iniciarDiseno();
        iniciarAnimacionEntrada();
    }

    private void iniciarDiseno() {
        setLayout(new GridBagLayout());
        setBackground(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO); // Verde Vintage de los botones

        // --- TARJETA FLOTANTE CENTRAL ---
        pnlTarjeta = new JPanel(new GridLayout(1, 2)) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                if (tarjetaAlpha < 1.0f || tarjetaOffsetY != 0 || tarjetaOffsetX != 0) {
                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Math.max(0.0f, Math.min(1.0f, tarjetaAlpha))));
                    g2.translate(tarjetaOffsetX, tarjetaOffsetY);
                }

                // Sombra suave posterior a la tarjeta
                g2.setColor(new Color(15, 45, 30, 90));
                g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, 35, 35);

                // M\u00E1scara de recorte redondeada
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 35, 35));
                super.paint(g2);

                // Borde redondeado elegante
                g2.setClip(null);
                g2.setColor(new Color(25, 65, 48, 140));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 35, 35);
                g2.dispose();
            }
        };
        pnlTarjeta.setPreferredSize(new Dimension(900, 520));
        pnlTarjeta.setOpaque(false);

        // --- MITAD IZQUIERDA (BRANDING) ---
        JPanel pnlIzquierda = new JPanel(new GridBagLayout());
        pnlIzquierda.setBackground(new Color(236, 246, 240)); // Salvia claro muy suave y limpio
        
        JLabel lblBranding = new JLabel("", SwingConstants.CENTER);
        try {
            modelo.Empresa emp = utilidades.SesionGlobal.getEmpresaActual();
            if (emp == null) {
                dao.EmpresaDAO empDao = new dao.EmpresaDAO();
                emp = empDao.obtenerDatos(1);
            }
            if (emp != null && emp.getImagen_logo() != null && !emp.getImagen_logo().isEmpty()) {
                ImageIcon icon = utilidades.ImagenHelper.obtenerIcono(emp.getImagen_logo(), 260, 260);
                if (icon != null) {
                    Image img = icon.getImage().getScaledInstance(260, -1, Image.SCALE_SMOOTH);
                    lblBranding.setIcon(new ImageIcon(img));
                }
            } else {
                java.net.URL imgURL = getClass().getResource("/image/logo_inversionesOlvan_sinFondo.png");
                if (imgURL != null) {
                    ImageIcon icon = new ImageIcon(imgURL);
                    Image img = icon.getImage().getScaledInstance(260, -1, Image.SCALE_SMOOTH);
                    lblBranding.setIcon(new ImageIcon(img));
                }
            }
        } catch (Exception e) {}
        
        JLabel lblBienvenida = new JLabel("<html><center>Bienvenido al Sistema<br>Integral de Ventas e Inventario</center></html>", SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblBienvenida.setForeground(new Color(19, 58, 42));

        GridBagConstraints gbcIzq = new GridBagConstraints();
        gbcIzq.gridx = 0; gbcIzq.gridy = 0;
        gbcIzq.insets = new Insets(0, 0, 25, 0);
        pnlIzquierda.add(lblBranding, gbcIzq);
        gbcIzq.gridy = 1;
        pnlIzquierda.add(lblBienvenida, gbcIzq);

        // --- MITAD DERECHA (FORMULARIO) ---
        JPanel pnlDerecha = new JPanel(new GridBagLayout());
        pnlDerecha.setBackground(Color.WHITE);

        GridBagConstraints gbcDer = new GridBagConstraints();
        gbcDer.insets = new Insets(5, 45, 5, 45); 
        gbcDer.fill = GridBagConstraints.HORIZONTAL;
        gbcDer.gridx = 0;
        gbcDer.weightx = 1.0;

        JLabel lblTitulo = new JLabel("Iniciar Sesi\u00F3n", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28)); 
        lblTitulo.setForeground(new Color(45, 45, 45));
        
        JLabel lblSub = new JLabel("Ingresa tus credenciales para continuar", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(140, 140, 140));

        gbcDer.gridy = 0; pnlDerecha.add(lblTitulo, gbcDer);
        gbcDer.gridy = 1; gbcDer.insets = new Insets(0, 45, 25, 45); pnlDerecha.add(lblSub, gbcDer);

        // --- CAMPOS ---
        JLabel lblUser = new JLabel("Usuario");
        lblUser.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUser.setForeground(new Color(70, 70, 70));

        txtUsuario = new JTextField();
        txtUsuario.setPreferredSize(new Dimension(0, 42));
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.putClientProperty("JTextField.placeholderText", "Ingresa tu usuario");
        txtUsuario.putClientProperty("JTextField.showClearButton", true);
        
        gbcDer.gridy = 2; gbcDer.insets = new Insets(5, 45, 4, 45); pnlDerecha.add(lblUser, gbcDer);
        gbcDer.gridy = 3; gbcDer.insets = new Insets(0, 45, 15, 45); pnlDerecha.add(txtUsuario, gbcDer);

        JLabel lblPass = new JLabel("Contrase\u00F1a");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(new Color(70, 70, 70));

        txtPassword = new JPasswordField();
        txtPassword.setPreferredSize(new Dimension(0, 42));
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.putClientProperty("JTextField.placeholderText", "Ingresa tu contrase\u00F1a");
        txtPassword.putClientProperty("JTextField.showRevealButton", true);
        
        txtPassword.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    btnEntrar.doClick();
                }
            }
        });

        gbcDer.gridy = 4; gbcDer.insets = new Insets(5, 45, 4, 45); pnlDerecha.add(lblPass, gbcDer);
        gbcDer.gridy = 5; gbcDer.insets = new Insets(0, 45, 35, 45); pnlDerecha.add(txtPassword, gbcDer);

        // Listener para limpiar el borde rojo al escribir
        DocumentListener resetErrorListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { resetError(); }
            @Override public void removeUpdate(DocumentEvent e) { resetError(); }
            @Override public void changedUpdate(DocumentEvent e) { resetError(); }
            private void resetError() {
                txtUsuario.putClientProperty("JComponent.outline", null);
                txtPassword.putClientProperty("JComponent.outline", null);
            }
        };
        txtUsuario.getDocument().addDocumentListener(resetErrorListener);
        txtPassword.getDocument().addDocumentListener(resetErrorListener);

        // --- ENLACE OLVIDE MI CONTRASE\u00D1A ---
        JLabel lblRecuperar = new JLabel("\u00BFOlvidaste tu contrase\u00F1a?");
        lblRecuperar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRecuperar.setForeground(new Color(45, 106, 79)); // Verde Vintage
        lblRecuperar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRecuperar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Frame parent = (Frame) SwingUtilities.getWindowAncestor(PanelLogin.this);
                DialogoRecuperarPassword dialog = new DialogoRecuperarPassword(parent);
                dialog.setVisible(true);
            }
        });
        
        gbcDer.gridy = 6; gbcDer.insets = new Insets(0, 45, 15, 45); pnlDerecha.add(lblRecuperar, gbcDer);

        btnEntrar = new JButton("Ingresar al Sistema") {
            private float hoverProg = 0.0f;
            private Timer tmr;
            private boolean isPressed = false;
            {
                addMouseListener(new java.awt.event.MouseAdapter() {
                    @Override
                    public void mouseEntered(java.awt.event.MouseEvent e) {
                        if (tmr != null && tmr.isRunning()) tmr.stop();
                        tmr = new Timer(15, ae -> {
                            hoverProg += 0.15f;
                            if (hoverProg >= 1.0f) { hoverProg = 1.0f; tmr.stop(); }
                            repaint();
                        });
                        tmr.start();
                    }
                    @Override
                    public void mouseExited(java.awt.event.MouseEvent e) {
                        if (tmr != null && tmr.isRunning()) tmr.stop();
                        tmr = new Timer(15, ae -> {
                            hoverProg -= 0.15f;
                            if (hoverProg <= 0.0f) { hoverProg = 0.0f; tmr.stop(); }
                            repaint();
                        });
                        tmr.start();
                    }
                    @Override
                    public void mousePressed(java.awt.event.MouseEvent e) {
                        isPressed = true;
                        repaint();
                    }
                    @Override
                    public void mouseReleased(java.awt.event.MouseEvent e) {
                        isPressed = false;
                        repaint();
                    }
                });
            }
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                Color c1 = new Color(45, 106, 79); // Verde Bosque Vintage
                Color c2 = new Color(30, 77, 56);  // Verde Vintage Profundo
                if (hoverProg > 0.01f) {
                    c1 = new Color(55, 126, 95);
                    c2 = new Color(45, 106, 79);
                }
                
                GradientPaint gp = new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                
                // Texto negro en hover o al presionar / blanco cuando el cursor se retira
                if (isPressed || hoverProg > 0.05f) {
                    int r = (int) (255 - 255 * hoverProg);
                    int gVal = (int) (255 - 255 * hoverProg);
                    int b = (int) (255 - 255 * hoverProg);
                    g2.setColor(isPressed ? Color.BLACK : new Color(Math.max(0, r), Math.max(0, gVal), Math.max(0, b)));
                } else {
                    g2.setColor(Color.WHITE);
                }
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int tx = (getWidth() - fm.stringWidth(getText())) / 2;
                int ty = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2.drawString(getText(), tx, ty);
                g2.dispose();
            }
        };
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnEntrar.setFocusPainted(false);
        btnEntrar.setBorderPainted(false);
        btnEntrar.setContentAreaFilled(false);
        btnEntrar.setPreferredSize(new Dimension(0, 48)); 
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.addActionListener(e -> procesarLogin());
        
        gbcDer.gridy = 7; gbcDer.insets = new Insets(0, 45, 15, 45); pnlDerecha.add(btnEntrar, gbcDer);

        // Ensamblar tarjeta
        pnlTarjeta.add(pnlIzquierda);
        pnlTarjeta.add(pnlDerecha);

        // A\u00F1adir la tarjeta centrada al fondo principal
        this.add(pnlTarjeta);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        // Fondo Verde Vintage Elegante del color de los botones
        GradientPaint gp = new GradientPaint(0, 0, new Color(34, 82, 60), getWidth(), getHeight(), new Color(45, 106, 79));
        g2.setPaint(gp);
        g2.fillRect(0, 0, getWidth(), getHeight());
        g2.dispose();
    }

    public void iniciarAnimacionEntrada() {
        tarjetaAlpha = 0.0f;
        tarjetaOffsetY = 15;
        tarjetaOffsetX = 0;
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();

        animTimer = new Timer(15, e -> {
            tarjetaAlpha += 0.08f;
            tarjetaOffsetY = Math.max(0, (int) (15 * (1.0f - tarjetaAlpha)));
            if (tarjetaAlpha >= 1.0f) {
                tarjetaAlpha = 1.0f;
                tarjetaOffsetY = 0;
                animTimer.stop();
            }
            repaint();
        });
        animTimer.start();
    }

    private void ejecutarShakeError() {
        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
        int[] frames = {-12, 12, -9, 9, -5, 5, -2, 2, 0};
        final int[] index = {0};

        animTimer = new Timer(22, e -> {
            if (index[0] < frames.length) {
                tarjetaOffsetX = frames[index[0]];
                index[0]++;
                repaint();
            } else {
                tarjetaOffsetX = 0;
                animTimer.stop();
                repaint();
            }
        });
        animTimer.start();
    }

    private void procesarLogin() {
        String usr = txtUsuario.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (usr.isEmpty() || pass.isEmpty()) {
            if (usr.isEmpty()) txtUsuario.putClientProperty("JComponent.outline", "error");
            if (pass.isEmpty()) txtPassword.putClientProperty("JComponent.outline", "error");
            ejecutarShakeError();
            utilidades.Mensajes.showAutoCloseMessageDialog(this, "Debe ingresar usuario y contrase\u00F1a.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE, 2000);
            return;
        }

        // Estado visual de carga
        btnEntrar.setEnabled(false);
        txtUsuario.setEnabled(false);
        txtPassword.setEnabled(false);
        btnEntrar.setText("Conectando al servidor...");
        btnEntrar.repaint();
        this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        SwingWorker<modelo.Usuario, Void> loginWorker = new SwingWorker<modelo.Usuario, Void>() {
            @Override
            protected modelo.Usuario doInBackground() throws Exception {
                // Consultas a BD en segundo plano
                dao.UsuarioDAO uDAO = new dao.UsuarioDAO();
                modelo.Usuario logged = uDAO.autenticarUsuario(usr, pass);

                if (logged != null) {
                    if (utilidades.SesionGlobal.getEmpresaActual() == null) {
                        try {
                            modelo.Empresa emp = new dao.EmpresaDAO().obtenerDatos();
                            if (emp != null) {
                                utilidades.SesionGlobal.setEmpresaActual(emp);
                            }
                        } catch(Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
                return logged;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    modelo.Usuario logged = get();
                    if (logged != null) {
                        btnEntrar.setText("\u00A1Acceso Correcto! Entrando...");
                        btnEntrar.repaint();
                        utilidades.SesionGlobal.setUsuarioActual(logged);

                        // Animaci\u00F3n suave de salida (Fade-Out + Elevaci\u00F3n)
                        if (animTimer != null && animTimer.isRunning()) animTimer.stop();
                        animTimer = new Timer(15, e -> {
                            tarjetaAlpha -= 0.08f;
                            tarjetaOffsetY -= 2;
                            if (tarjetaAlpha <= 0.0f) {
                                tarjetaAlpha = 0.0f;
                                animTimer.stop();
                                menuPrincipal.iniciarEntornoApp();

                                // Restaurar controles para cuando se cierre sesi\u00F3n
                                btnEntrar.setEnabled(true);
                                txtUsuario.setEnabled(true);
                                txtPassword.setEnabled(true);
                                btnEntrar.setText("Ingresar al Sistema");
                            }
                            repaint();
                        });
                        animTimer.start();
                    } else {
                        btnEntrar.setEnabled(true);
                        txtUsuario.setEnabled(true);
                        txtPassword.setEnabled(true);
                        btnEntrar.setText("Ingresar al Sistema");
                        txtUsuario.putClientProperty("JComponent.outline", "error");
                        txtPassword.putClientProperty("JComponent.outline", "error");
                        ejecutarShakeError();
                        utilidades.Mensajes.showAutoCloseMessageDialog(PanelLogin.this, "El nombre de usuario o la contrase\u00F1a son incorrectos.", "Credenciales Inv\u00E1lidas", JOptionPane.ERROR_MESSAGE, 2000);
                    }
                } catch (Exception ex) {
                    btnEntrar.setEnabled(true);
                    txtUsuario.setEnabled(true);
                    txtPassword.setEnabled(true);
                    btnEntrar.setText("Ingresar al Sistema");
                    ex.printStackTrace();
                    utilidades.Mensajes.showMessageDialog(PanelLogin.this, "Error de conexi\u00F3n con la base de datos.", "Error de Red", JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        loginWorker.execute();
    }
    
    public void limpiarCampos() {
        txtUsuario.setText("");
        txtPassword.setText("");
        txtUsuario.putClientProperty("JComponent.outline", null);
        txtPassword.putClientProperty("JComponent.outline", null);
        txtUsuario.requestFocus();
        iniciarAnimacionEntrada();
    }
}

