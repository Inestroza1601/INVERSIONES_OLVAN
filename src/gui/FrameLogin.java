package gui;

import dao.UsuarioDAO;
import modelo.Usuario;
import utilidades.SesionGlobal;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class FrameLogin extends JFrame {
    private JTextField txtUsuario;
    private JPasswordField txtPassword;
    private JButton btnEntrar;
    private JButton btnSalir;

    public FrameLogin() {
        iniciarDiseno();
    }

    private void iniciarDiseno() {
        setTitle("Inversiones Olvan - Iniciar Sesión");
        setSize(400, 480);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(240, 242, 245)); // Gris Nube

        JPanel pnlCard = new JPanel(new GridBagLayout());
        pnlCard.setBackground(Color.WHITE);
        pnlCard.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 222, 225), 1, true),
            BorderFactory.createEmptyBorder(30, 30, 30, 30)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 0, 10, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.weightx = 1.0;

        // Logo / Icon
        JLabel lblLogo = new JLabel("🔐", SwingConstants.CENTER);
        lblLogo.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        gbc.gridy = 0; pnlCard.add(lblLogo, gbc);

        // Title
        JLabel lblTitulo = new JLabel("INVERSIONES OLVAN", SwingConstants.CENTER);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(new Color(45, 45, 45));
        gbc.gridy = 1; pnlCard.add(lblTitulo, gbc);

        JLabel lblSub = new JLabel("Acceso al Sistema POS & Inventario", SwingConstants.CENTER);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(new Color(140, 145, 150));
        gbc.gridy = 2; pnlCard.add(lblSub, gbc);

        gbc.insets = new Insets(8, 0, 4, 0);

        // Usuario
        JLabel lblUsr = new JLabel("Usuario:");
        lblUsr.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUsr.setForeground(new Color(85, 85, 85));
        txtUsuario = new JTextField();
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.setPreferredSize(new Dimension(0, 36));
        txtUsuario.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        gbc.gridy = 3; pnlCard.add(lblUsr, gbc); gbc.gridy = 4; pnlCard.add(txtUsuario, gbc);

        // Contraseña
        JLabel lblPass = new JLabel("Contraseña:");
        lblPass.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblPass.setForeground(new Color(85, 85, 85));
        txtPassword = new JPasswordField();
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtPassword.setPreferredSize(new Dimension(0, 36));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(0, 8, 0, 8)));
        txtPassword.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    procesarLogin();
                }
            }
        });
        gbc.gridy = 5; pnlCard.add(lblPass, gbc); gbc.gridy = 6; pnlCard.add(txtPassword, gbc);

        // Boton Entrar
        btnEntrar = new JButton("Iniciar Sesión");
        btnEntrar.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnEntrar.setForeground(Color.WHITE);
        btnEntrar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEntrar.setFocusPainted(false);
        btnEntrar.setPreferredSize(new Dimension(0, 42));
        btnEntrar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEntrar.addActionListener(e -> procesarLogin());
        gbc.insets = new Insets(15, 0, 5, 0);
        gbc.gridy = 7; pnlCard.add(btnEntrar, gbc);

        btnSalir = new JButton("Salir");
        btnSalir.setBackground(Color.WHITE);
        btnSalir.setForeground(new Color(140, 145, 150));
        btnSalir.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSalir.setFocusPainted(false);
        btnSalir.setPreferredSize(new Dimension(0, 32));
        btnSalir.setBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)));
        btnSalir.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnSalir.addActionListener(e -> System.exit(0));
        gbc.insets = new Insets(5, 0, 5, 0);
        gbc.gridy = 8; pnlCard.add(btnSalir, gbc);

        // Center card in frame
        JPanel pnlOuter = new JPanel(new GridBagLayout());
        pnlOuter.setOpaque(false);
        pnlOuter.add(pnlCard);
        this.add(pnlOuter, BorderLayout.CENTER);
    }

    private void procesarLogin() {
        String usr = txtUsuario.getText().trim();
        String pass = new String(txtPassword.getPassword());

        if (usr.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Debe ingresar usuario y contraseña.", "Aviso", JOptionPane.WARNING_MESSAGE);
            return;
        }

        UsuarioDAO uDAO = new UsuarioDAO();
        Usuario logged = uDAO.autenticarUsuario(usr, pass);

        if (logged != null) {
            SesionGlobal.setUsuarioActual(logged);
            
            // Si la empresa no está cargada, cargar la predeterminada (ID = 1)
            if (SesionGlobal.getEmpresaActual() == null) {
                try {
                    modelo.Empresa emp = new dao.EmpresaDAO().obtenerDatos();
                    if (emp != null) {
                        SesionGlobal.setEmpresaActual(emp);
                    }
                } catch(Exception ex) {
                    System.err.println("Error al cargar empresa predeterminada: " + ex.getMessage());
                }
            }

            // Abrir menú principal
            SwingUtilities.invokeLater(() -> {
                new MenuPrincipal().setVisible(true);
            });
            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Usuario o contraseña incorrectos.", "Error de Acceso", JOptionPane.ERROR_MESSAGE);
        }
    }
}
