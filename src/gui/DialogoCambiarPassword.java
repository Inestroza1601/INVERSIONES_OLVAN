package gui;

import dao.UsuarioDAO;
import modelo.Usuario;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class DialogoCambiarPassword extends JDialog {
    
    private JTextField txtToken;
    private JPasswordField txtNuevaClave;
    private JPasswordField txtConfirmarClave;
    private JButton btnGuardar;
    private JButton btnCancelar;
    private Usuario usuario;
    
    public DialogoCambiarPassword(Frame parent, Usuario usuario) {
        super(parent, "Cambiar Contraseña", true);
        this.usuario = usuario;
        iniciarDiseno();
        pack();
        setLocationRelativeTo(parent);
    }
    
    private void iniciarDiseno() {
        JPanel pnlFondo = new JPanel(new GridBagLayout());
        pnlFondo.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);
        pnlFondo.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(10, 5, 5, 5);
        gbc.gridx = 0;
        
        JLabel lblTitulo = new JLabel("Ingresa tu nueva contraseña");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        
        JLabel lblSub = new JLabel("<html><body>Hemos enviado un código de 6 dígitos a su correo.<br>Ingrese el código y su nueva contraseña.</body></html>");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(utilidades.EfectosUI.COLOR_TEXTO_SUBTITULO);
        
        gbc.gridy = 0; pnlFondo.add(lblTitulo, gbc);
        gbc.gridy = 1; pnlFondo.add(lblSub, gbc);
        
        // Campos
        txtToken = new JTextField(25);
        prepararCampo(txtToken, "Código de 6 dígitos");
        // Permitir solo números y máx 6
        txtToken.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if(txtToken.getText().length() >= 6 || !Character.isDigit(e.getKeyChar())) e.consume();
            }
        });
        
        txtNuevaClave = new JPasswordField(25);
        prepararCampo(txtNuevaClave, "Nueva contraseña");
        txtNuevaClave.putClientProperty("JTextField.showRevealButton", true);
        
        txtConfirmarClave = new JPasswordField(25);
        prepararCampo(txtConfirmarClave, "Confirmar contraseña");
        txtConfirmarClave.putClientProperty("JTextField.showRevealButton", true);
        
        gbc.gridy = 2; pnlFondo.add(new JLabel("Código de Seguridad:"), gbc);
        gbc.insets = new Insets(0, 5, 10, 5);
        gbc.gridy = 3; pnlFondo.add(txtToken, gbc);
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 4; pnlFondo.add(new JLabel("Nueva Contraseña:"), gbc);
        gbc.insets = new Insets(0, 5, 10, 5);
        gbc.gridy = 5; pnlFondo.add(txtNuevaClave, gbc);
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 6; pnlFondo.add(new JLabel("Confirmar Contraseña:"), gbc);
        gbc.insets = new Insets(0, 5, 15, 5);
        gbc.gridy = 7; pnlFondo.add(txtConfirmarClave, gbc);
        
        // Botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotones.setOpaque(false);
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelar.setPreferredSize(new Dimension(110, 40));
        btnCancelar.putClientProperty("JButton.buttonType", "roundRect");
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(e -> dispose());
        
        btnGuardar = utilidades.EfectosUI.crearBotonVerde("Cambiar Contraseña");
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setPreferredSize(new Dimension(180, 40));
        btnGuardar.putClientProperty("JButton.buttonType", "roundRect");
        btnGuardar.addActionListener(e -> guardarPassword());
        
        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnGuardar);
        
        gbc.gridy = 8;
        gbc.insets = new Insets(10, 5, 0, 5);
        pnlFondo.add(pnlBotones, gbc);
        
        add(pnlFondo);
    }
    
    private void prepararCampo(JTextField campo, String placeholder) {
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setPreferredSize(new Dimension(0, 40));
        campo.putClientProperty("JTextField.placeholderText", placeholder);
        campo.putClientProperty("JComponent.roundRect", true);
    }
    
    private void guardarPassword() {
        String token = txtToken.getText().trim();
        String pass = new String(txtNuevaClave.getPassword());
        String conf = new String(txtConfirmarClave.getPassword());
        
        if (token.isEmpty() || pass.isEmpty() || conf.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Todos los campos son obligatorios.", "Campos vacíos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!pass.equals(conf)) {
            JOptionPane.showMessageDialog(this, "Las contraseñas no coinciden.", "Error de validación", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        UsuarioDAO dao = new UsuarioDAO();
        
        if (!dao.validarTokenRecuperacion(usuario.getIdUsuario(), token)) {
            JOptionPane.showMessageDialog(this, "El código de seguridad es inválido o ha expirado.", "Código Incorrecto", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (dao.actualizarPassword(usuario.getIdUsuario(), pass)) {
            JOptionPane.showMessageDialog(this, "¡Su contraseña se ha actualizado correctamente!\nPuede iniciar sesión con su nueva clave.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Ocurrió un error al actualizar la contraseña.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}
