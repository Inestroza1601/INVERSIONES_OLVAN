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
        super(parent, "Cambiar Contrase\u00F1a", true);
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
        
        JLabel lblTitulo = new JLabel("Ingresa tu nueva contrase\u00F1a");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        
        JLabel lblSub = new JLabel("<html><body>Hemos enviado un c\u00F3digo de 6 d\u00EDgitos a su correo.<br>Ingrese el c\u00F3digo y su nueva contrase\u00F1a.</body></html>");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblSub.setForeground(utilidades.EfectosUI.COLOR_TEXTO_SUBTITULO);
        
        gbc.gridy = 0; pnlFondo.add(lblTitulo, gbc);
        gbc.gridy = 1; pnlFondo.add(lblSub, gbc);
        
        // Campos
        txtToken = new JTextField(25);
        prepararCampo(txtToken, "C\u00F3digo de 6 d\u00EDgitos");
        // Permitir solo n\u00FAmeros y m\u00E1x 6
        txtToken.addKeyListener(new KeyAdapter() {
            public void keyTyped(KeyEvent e) {
                if(txtToken.getText().length() >= 6 || !Character.isDigit(e.getKeyChar())) e.consume();
            }
        });
        
        txtNuevaClave = new JPasswordField(25);
        prepararCampo(txtNuevaClave, "Nueva contrase\u00F1a");
        txtNuevaClave.putClientProperty("JTextField.showRevealButton", true);
        
        txtConfirmarClave = new JPasswordField(25);
        prepararCampo(txtConfirmarClave, "Confirmar contrase\u00F1a");
        txtConfirmarClave.putClientProperty("JTextField.showRevealButton", true);
        
        gbc.gridy = 2; pnlFondo.add(new JLabel("<html>C\u00F3digo de Seguridad: <font color='red'>*</font></html>"), gbc);
        gbc.insets = new Insets(0, 5, 10, 5);
        gbc.gridy = 3; pnlFondo.add(txtToken, gbc);
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 4; pnlFondo.add(new JLabel("<html>Nueva Contrase\u00F1a: <font color='red'>*</font></html>"), gbc);
        gbc.insets = new Insets(0, 5, 10, 5);
        gbc.gridy = 5; pnlFondo.add(txtNuevaClave, gbc);
        
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridy = 6; pnlFondo.add(new JLabel("<html>Confirmar Contrase\u00F1a: <font color='red'>*</font></html>"), gbc);
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
        
        btnGuardar = utilidades.EfectosUI.crearBotonVerde("Cambiar Contrase\u00F1a");
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
            utilidades.Mensajes.showMessageDialog(this, "Todos los campos son obligatorios.", "Campos vac\u00EDos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        if (!pass.equals(conf)) {
            utilidades.Mensajes.showMessageDialog(this, "Las contrase\u00F1as no coinciden.", "Error de validaci\u00F3n", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        UsuarioDAO dao = new UsuarioDAO();
        
        if (!dao.validarTokenRecuperacion(usuario.getIdUsuario(), token)) {
            utilidades.Mensajes.showMessageDialog(this, "El c\u00F3digo de seguridad es inv\u00E1lido o ha expirado.", "C\u00F3digo Incorrecto", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (dao.actualizarPassword(usuario.getIdUsuario(), pass)) {
            utilidades.Mensajes.showMessageDialog(this, "\u00A1Su contrase\u00F1a se ha actualizado correctamente!\nPuede iniciar sesi\u00F3n con su nueva clave.", "\u00C9xito", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            utilidades.Mensajes.showMessageDialog(this, "Ocurri\u00F3 un error al actualizar la contrase\u00F1a.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}

