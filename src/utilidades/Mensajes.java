package utilidades;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class Mensajes {

    public static void showMessageDialog(Component parentComponent, Object message) {
        showMessageDialog(parentComponent, message, "Mensaje", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void showMessageDialog(Component parentComponent, Object message, String title, int messageType) {
        mostrarDialogo(parentComponent, message, title, messageType, false, JOptionPane.DEFAULT_OPTION);
    }
    
    public static int showConfirmDialog(Component parentComponent, Object message) {
        return mostrarDialogo(parentComponent, message, "Confirmar", JOptionPane.QUESTION_MESSAGE, true, JOptionPane.YES_NO_OPTION);
    }
    
    public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType) {
        return mostrarDialogo(parentComponent, message, title, JOptionPane.QUESTION_MESSAGE, true, optionType);
    }

    public static int showConfirmDialog(Component parentComponent, Object message, String title, int optionType, int messageType) {
        return mostrarDialogo(parentComponent, message, title, messageType, true, optionType);
    }

    private static Component crearTexto(String texto) {
        JTextArea txt = new JTextArea(texto);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        txt.setForeground(utilidades.EfectosUI.COLOR_TEXTO_OSCURO);
        txt.setOpaque(false);
        txt.setEditable(false);
        txt.setLineWrap(true);
        txt.setWrapStyleWord(true);
        txt.setFocusable(false);
        return txt;
    }

    private static int mostrarDialogo(Component parentComponent, Object message, String title, int messageType, boolean isConfirm, int optionType) {
        Window window = null;
        if (parentComponent != null) {
            if (parentComponent instanceof Window) {
                window = (Window) parentComponent;
            } else {
                window = SwingUtilities.getWindowAncestor(parentComponent);
            }
        }
        
        JDialog dialog = new JDialog(window, title, Dialog.ModalityType.APPLICATION_MODAL);
        dialog.setUndecorated(true);
        dialog.setBackground(new Color(0, 0, 0, 0));
        
        JPanel pnlContent = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(255, 255, 255)); // Fondo Blanco
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 20, 20));
                
                // Borde verde primario
                g2.setColor(utilidades.EfectosUI.COLOR_VERDE_PRIMARIO);
                g2.setStroke(new BasicStroke(3f));
                g2.draw(new RoundRectangle2D.Double(1, 1, getWidth() - 2, getHeight() - 2, 20, 20));
            }
        };
        pnlContent.setOpaque(false);
        pnlContent.setLayout(new BorderLayout());
        pnlContent.setBorder(new EmptyBorder(25, 25, 20, 25));
        
        // Titulo
        JLabel lblTitulo = new JLabel(title != null ? title : "");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        lblTitulo.setHorizontalAlignment(SwingConstants.CENTER);
        
        // Icono
        Icon icon = null;
        if (messageType == JOptionPane.ERROR_MESSAGE) icon = UIManager.getIcon("OptionPane.errorIcon");
        else if (messageType == JOptionPane.WARNING_MESSAGE) icon = UIManager.getIcon("OptionPane.warningIcon");
        else if (messageType == JOptionPane.QUESTION_MESSAGE) icon = UIManager.getIcon("OptionPane.questionIcon");
        else if (messageType == JOptionPane.INFORMATION_MESSAGE) icon = UIManager.getIcon("OptionPane.informationIcon");
        
        JLabel lblIcon = new JLabel();
        if (icon != null) {
            lblIcon.setIcon(icon);
        } else {
            lblIcon.setVisible(false);
        }
        lblIcon.setHorizontalAlignment(SwingConstants.CENTER);
        
        JPanel pnlHeader = new JPanel(new BorderLayout(15, 0));
        pnlHeader.setOpaque(false);
        pnlHeader.add(lblIcon, BorderLayout.WEST);
        pnlHeader.add(lblTitulo, BorderLayout.CENTER);
        
        // Contenido del mensaje (Manejo de arreglos de componentes/texto)
        JPanel pnlMsg = new JPanel();
        pnlMsg.setLayout(new BoxLayout(pnlMsg, BoxLayout.Y_AXIS));
        pnlMsg.setOpaque(false);
        pnlMsg.setBorder(new EmptyBorder(20, 0, 25, 0));
        
        if (message instanceof Object[]) {
            Object[] arr = (Object[]) message;
            for (Object obj : arr) {
                if (obj instanceof Component) {
                    pnlMsg.add((Component) obj);
                } else if (obj != null) {
                    pnlMsg.add(crearTexto(obj.toString()));
                }
                pnlMsg.add(Box.createVerticalStrut(10));
            }
        } else if (message instanceof Component) {
            pnlMsg.add((Component) message);
        } else if (message != null) {
            pnlMsg.add(crearTexto(message.toString()));
        }
        
        pnlContent.add(pnlHeader, BorderLayout.NORTH);
        pnlContent.add(pnlMsg, BorderLayout.CENTER);
        
        // Botones
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        pnlBotones.setOpaque(false);
        
        final int[] resultado = { JOptionPane.CLOSED_OPTION };
        
        if (isConfirm) {
            if (optionType == JOptionPane.OK_CANCEL_OPTION) {
                JButton btnOk = utilidades.EfectosUI.crearBotonVerde("Aceptar");
                JButton btnCancel = utilidades.EfectosUI.crearBotonPeligro("Cancelar");
                btnOk.addActionListener(e -> { resultado[0] = JOptionPane.OK_OPTION; animarCierre(dialog); });
                btnCancel.addActionListener(e -> { resultado[0] = JOptionPane.CANCEL_OPTION; animarCierre(dialog); });
                pnlBotones.add(btnCancel);
                pnlBotones.add(btnOk);
            } else {
                JButton btnSi = utilidades.EfectosUI.crearBotonVerde("Sí, Continuar");
                JButton btnNo = utilidades.EfectosUI.crearBotonPeligro("No, Cancelar");
                btnSi.addActionListener(e -> { resultado[0] = JOptionPane.YES_OPTION; animarCierre(dialog); });
                btnNo.addActionListener(e -> { resultado[0] = JOptionPane.NO_OPTION; animarCierre(dialog); });
                pnlBotones.add(btnNo);
                pnlBotones.add(btnSi);
            }
        } else {
            JButton btnOk = utilidades.EfectosUI.crearBotonVerde("Aceptar");
            btnOk.addActionListener(e -> { resultado[0] = JOptionPane.OK_OPTION; animarCierre(dialog); });
            pnlBotones.add(btnOk);
        }
        
        pnlContent.add(pnlBotones, BorderLayout.SOUTH);
        
        dialog.setContentPane(pnlContent);
        dialog.pack();
        int width = Math.max(380, dialog.getWidth() + 40);
        int height = dialog.getHeight();
        dialog.setSize(width, height);
        dialog.setLocationRelativeTo(window);
        
        // Animacion entrada (Fade in)
        dialog.setOpacity(0f);
        Timer anim = new Timer(15, new ActionListener() {
            float op = 0f;
            public void actionPerformed(ActionEvent e) {
                op += 0.1f;
                if (op >= 1f) {
                    op = 1f;
                    ((Timer)e.getSource()).stop();
                }
                dialog.setOpacity(op);
            }
        });
        anim.start();
        
        dialog.setVisible(true);
        return resultado[0];
    }
    
    private static void animarCierre(JDialog dialog) {
        Timer anim = new Timer(10, new ActionListener() {
            float op = 1f;
            public void actionPerformed(ActionEvent e) {
                op -= 0.15f;
                if (op <= 0f) {
                    ((Timer)e.getSource()).stop();
                    dialog.dispose();
                } else {
                    dialog.setOpacity(op);
                }
            }
        });
        anim.start();
    }
}
