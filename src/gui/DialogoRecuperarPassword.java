package gui;

import dao.UsuarioDAO;
import modelo.Usuario;
import utilidades.EmailSender;
import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class DialogoRecuperarPassword extends JDialog {
    
    private JTextField txtUsuario;
    private JButton btnEnviar;
    private JButton btnCancelar;
    
    public DialogoRecuperarPassword(Frame parent) {
        super(parent, "Recuperar Contrase\u00F1a", true);
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
        gbc.insets = new Insets(10, 5, 10, 5);
        gbc.gridx = 0;
        
        JLabel lblTitulo = new JLabel("Recuperaci\u00F3n de Contrase\u00F1a");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        
        JLabel lblInstrucciones = new JLabel("<html><body>Ingrese su nombre de usuario.<br>Verificaremos su cuenta y enviaremos el c\u00F3digo a su correo.</body></html>");
        lblInstrucciones.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        lblInstrucciones.setForeground(utilidades.EfectosUI.COLOR_TEXTO_SUBTITULO);
        
        txtUsuario = new JTextField(25);
        txtUsuario.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtUsuario.setPreferredSize(new Dimension(0, 40));
        txtUsuario.putClientProperty("JTextField.placeholderText", "ejemplo: admin");
        txtUsuario.putClientProperty("JComponent.roundRect", true);
        
        gbc.gridy = 0; pnlFondo.add(lblTitulo, gbc);
        gbc.gridy = 1; pnlFondo.add(lblInstrucciones, gbc);
        gbc.gridy = 2; pnlFondo.add(txtUsuario, gbc);
        
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotones.setOpaque(false);
        
        btnCancelar = new JButton("Cancelar");
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelar.setPreferredSize(new Dimension(110, 40));
        btnCancelar.putClientProperty("JButton.buttonType", "roundRect");
        btnCancelar.setFocusPainted(false);
        btnCancelar.addActionListener(e -> dispose());
        
        btnEnviar = utilidades.EfectosUI.crearBotonVerde("Enviar C\u00F3digo");
        btnEnviar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnEnviar.setPreferredSize(new Dimension(140, 40));
        btnEnviar.putClientProperty("JButton.buttonType", "roundRect");
        btnEnviar.addActionListener(e -> enviarCodigo());
        
        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnEnviar);
        
        gbc.gridy = 3; 
        gbc.insets = new Insets(20, 5, 0, 5);
        pnlFondo.add(pnlBotones, gbc);
        
        add(pnlFondo);
    }
    
    private void enviarCodigo() {
        String nombreUsuario = txtUsuario.getText().trim();
        if (nombreUsuario.isEmpty()) {
            utilidades.Mensajes.showMessageDialog(this, "Por favor ingrese su nombre de usuario.", "Atenci\u00F3n", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        btnEnviar.setEnabled(false);
        
        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            private Usuario usuarioEncontrado;
            private String tokenGenerado;
            private String correoUsuario;
            private boolean sinCorreo = false;
            
            @Override
            protected Boolean doInBackground() throws Exception {
                UsuarioDAO dao = new UsuarioDAO();
                usuarioEncontrado = dao.obtenerUsuarioPorNombre(nombreUsuario);
                
                if (usuarioEncontrado == null) {
                    return false; // No existe
                }
                
                correoUsuario = usuarioEncontrado.getEmailUsuario();
                if (correoUsuario == null || correoUsuario.trim().isEmpty()) {
                    sinCorreo = true;
                    return false;
                }
                
                // Generar token de 6 digitos
                tokenGenerado = String.format("%06d", new Random().nextInt(999999));
                
                if (dao.guardarTokenRecuperacion(usuarioEncontrado.getIdUsuario(), tokenGenerado)) {
                    return EmailSender.enviarCorreoRecuperacion(correoUsuario, tokenGenerado);
                }
                return false;
            }

            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                btnEnviar.setEnabled(true);
                
                try {
                    boolean exito = get();
                    if (exito) {
                        String ofuscado = correoUsuario.replaceAll("(^[^@]{2}|(?!^)\\G)[^@]", "$1*");
                        utilidades.Mensajes.showMessageDialog(DialogoRecuperarPassword.this, 
                            "Se ha enviado un c\u00F3digo de recuperaci\u00F3n a su correo: " + ofuscado, 
                            "Correo Enviado", JOptionPane.INFORMATION_MESSAGE);
                        dispose();
                        
                        // Abrir dialogo para cambiar clave
                        Frame parent = (Frame) getParent();
                        DialogoCambiarPassword dialog = new DialogoCambiarPassword(parent, usuarioEncontrado);
                        dialog.setVisible(true);
                        
                    } else {
                        if (usuarioEncontrado == null) {
                            utilidades.Mensajes.showMessageDialog(DialogoRecuperarPassword.this, 
                                "No se encontr\u00F3 ning\u00FAn usuario con ese nombre.", 
                                "Error", JOptionPane.ERROR_MESSAGE);
                        } else if (sinCorreo) {
                            utilidades.Mensajes.showMessageDialog(DialogoRecuperarPassword.this, 
                                "El usuario no tiene un correo electr\u00F3nico enlazado.\nPor favor contacte al administrador.", 
                                "Sin Correo Enlazado", JOptionPane.WARNING_MESSAGE);
                        } else {
                            utilidades.Mensajes.showMessageDialog(DialogoRecuperarPassword.this, 
                                "Hubo un error al enviar el correo. Por favor intente m\u00E1s tarde.", 
                                "Error de Env\u00EDo", JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        };
        worker.execute();
    }
}

