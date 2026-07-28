package gui;

import dao.UsuarioDAO;
import modelo.Usuario;
import utilidades.SesionGlobal;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class PanelLogin extends JPanel {
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnEntrar;
    private MenuPrincipal menuPrincipal;

    public PanelLogin(MenuPrincipal menuPrincipal) {
        this.menuPrincipal = menuPrincipal;
        iniciarDiseno();
    }

    private void iniciarDiseno() {
        // El panel principal ocupa toda la ventana y mantiene el fondo unificado
        setLayout(new GridBagLayout());
        setBackground(new Color(240, 242, 245)); // Gris nube suave, sin imagen de fondo

        // --- TARJETA FLOTANTE CENTRAL ---
        JPanel pnlTarjeta = new JPanel(new GridLayout(1, 2)) {
            @Override
            public void paint(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Máscara de recorte redondeada
                g2.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 35, 35));
                super.paint(g2);
                g2.dispose();

                // Dibujar borde
                Graphics2D g2Border = (Graphics2D) g.create();
                g2Border.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2Border.setColor(new Color(200, 205, 210));
                g2Border.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 35, 35);
                g2Border.dispose();
            }
        };
        pnlTarjeta.setPreferredSize(new Dimension(900, 520));
        pnlTarjeta.setOpaque(false);

        // --- MITAD IZQUIERDA (BRANDING) ---
        JPanel pnlIzquierda = new JPanel(new GridBagLayout());
        pnlIzquierda.setBackground(new Color(39, 174, 96));

        JLabel lblBranding = new JLabel("", SwingConstants.CENTER);
        try {
            java.net.URL imgURL = getClass().getResource("/image/logo_inversionesOlvan_sinFondo.png");
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image img = icon.getImage().getScaledInstance(260, -1, Image.SCALE_SMOOTH);
                lblBranding.setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
        }

        JLabel lblBienvenida = new JLabel(
                "<html><center>Bienvenido al Sistema<br>Integral de Ventas e Inventario</center></html>",
                SwingConstants.CENTER);
        lblBienvenida.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblBienvenida.setForeground(new Color(255, 255, 255, 230));

        GridBagConstraints gbcIzq = new GridBagConstraints();
        gbcIzq.gridx = 0;
        gbcIzq.gridy = 0;
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

        JLabel lblTitulo = new JLabel("Iniciar Sesión", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(new Color(45, 45, 45));

        JLabel lblSub = new JLabel("Ingresa tus credenciales para continuar", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub.setForeground(new Color(140, 145, 150));

        gbcDer.gridy = 0;
        gbcDer.insets = new Insets(20, 45, 5, 45);
        pnlDerecha.add(lblTitulo, gbcDer);
        gbcDer.gridy = 1;
        gbcDer.insets = new Insets(0, 45, 35, 45);
        pnlDerecha.add(lblSub, gbcDer);

        // --- CAMPOS ---
        gbcDer.insets = new Insets(5, 45, 5, 45);

        JLabel lblUsr = new JLabel("Usuario");
        lblUsr.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblUsr.setForeground(new Color(100, 100, 100));

        txtUsuario = new JTextField();
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.setPreferredSize(new Dimension(0, 45));
        txtUsuario.putClientProperty("JTextField.placeholderText", "Ingresa tu usuario");
        txtUsuario.putClientProperty("JTextField.showClearButton", true);
        txtUsuario.putClientProperty("JComponent.roundRect", true);

        gbcDer.gridy = 2;
        gbcDer.insets = new Insets(5, 45, 4, 45);
        pnlDerecha.add(lblUsr, gbcDer);
        gbcDer.gridy = 3;
        gbcDer.insets = new Insets(0, 45, 15, 45);
        pnlDerecha.add(txtUsuario, gbcDer);

        JLabel lblPass = new JLabel("Contraseña");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblPass.setForeground(new Color(100, 100, 100));

        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(0, 45));
        txtPassword.putClientProperty("JTextField.placeholderText", "Ingresa tu contraseña");
        txtPassword.putClientProperty("JTextField.showClearButton", true);
        txtPassword.putClientProperty("JTextField.showRevealButton", true);
        txtPassword.putClientProperty("JComponent.roundRect", true);
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    procesarLogin();
                }
            }
        });

        gbcDer.gridy = 4;
        gbcDer.insets = new Insets(5, 45, 4, 45);
        pnlDerecha.add(lblPass, gbcDer);
        gbcDer.gridy = 5;
        gbcDer.insets = new Insets(0, 45, 35, 45);
        pnlDerecha.add(txtPassword, gbcDer);

        // Listener para limpiar el borde rojo cuando el usuario empiece a escribir de
        // nuevo
        DocumentListener resetErrorListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                resetError();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                resetError();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                resetError();
            }

            private void resetError() {
                txtUsuario.putClientProperty("JComponent.outline", null);
                txtPassword.putClientProperty("JComponent.outline", null);
            }
        };
        txtUsuario.getDocument().addDocumentListener(resetErrorListener);
        txtPassword.getDocument().addDocumentListener(resetErrorListener);

        // --- ENLACE OLVIDE MI CONTRASEÑA ---
        JLabel lblRecuperar = new JLabel("¿Olvidaste tu contraseña?");
        lblRecuperar.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblRecuperar.setForeground(new Color(41, 128, 185)); // Azul tipo enlace
        lblRecuperar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblRecuperar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                Frame parent = (Frame) SwingUtilities.getWindowAncestor(PanelLogin.this);
                DialogoRecuperarPassword dialog = new DialogoRecuperarPassword(parent);
                dialog.setVisible(true);
            }
        });

        gbcDer.gridy = 6;
        gbcDer.insets = new Insets(0, 45, 15, 45);
        pnlDerecha.add(lblRecuperar, gbcDer);

        btnEntrar = new JButton("Ingresar al Sistema");
        btnEntrar.setBackground(new Color(39, 174, 96));
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 15));
        btnEntrar.setFocusPainted(false);
        btnEntrar.setPreferredSize(new Dimension(0, 48));
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.putClientProperty("JButton.buttonType", "roundRect");
        btnEntrar.addActionListener(e -> procesarLogin());

        gbcDer.gridy = 7;
        gbcDer.insets = new Insets(0, 45, 15, 45);
        pnlDerecha.add(btnEntrar, gbcDer);

        // Ensamblar tarjeta
        pnlTarjeta.add(pnlIzquierda);
        pnlTarjeta.add(pnlDerecha);

        // Añadir la tarjeta centrada al fondo principal
        this.add(pnlTarjeta);
    }

    private void procesarLogin() {
        String usr = txtUsuario.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (usr.isEmpty() || pass.isEmpty()) {
            if (usr.isEmpty())
                txtUsuario.putClientProperty("JComponent.outline", "error");
            if (pass.isEmpty())
                txtPassword.putClientProperty("JComponent.outline", "error");
            JOptionPane.showMessageDialog(this, "Debe ingresar usuario y contraseña.", "Campos Incompletos",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        UsuarioDAO uDAO = new UsuarioDAO();
        Usuario logged = uDAO.autenticarUsuario(usr, pass);

        if (logged != null) {
            // Mostrar mensaje de bienvenida exitoso
            JOptionPane.showMessageDialog(this, "¡Bienvenido al sistema, " + logged.getNombreUsuario() + "!",
                    "Inicio de Sesión Exitoso", JOptionPane.INFORMATION_MESSAGE);

            SesionGlobal.setUsuarioActual(logged);

            if (SesionGlobal.getEmpresaActual() == null) {
                try {
                    modelo.Empresa emp = new dao.EmpresaDAO().obtenerDatos();
                    if (emp != null) {
                        SesionGlobal.setEmpresaActual(emp);
                    }
                } catch (Exception ex) {
                }
            }

            menuPrincipal.iniciarEntornoApp();
        } else {
            // Pintar los campos de rojo si las credenciales son incorrectas
            txtUsuario.putClientProperty("JComponent.outline", "error");
            txtPassword.putClientProperty("JComponent.outline", "error");
            JOptionPane.showMessageDialog(this, "El nombre de usuario o la contraseña son incorrectos.",
                    "Credenciales Inválidas", JOptionPane.ERROR_MESSAGE);
        }
    }

    public void limpiarCampos() {
        txtUsuario.setText("");
        txtPassword.setText("");
        txtUsuario.putClientProperty("JComponent.outline", null);
        txtPassword.putClientProperty("JComponent.outline", null);
        txtUsuario.requestFocus();
    }
}
