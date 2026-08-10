package gui;

import utilidades.QRHelper;
import utilidades.ServidorFotos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.function.Consumer;

public class DialogoEscanearQR extends JDialog {

    private ServidorFotos servidor;
    private Consumer<String> onFotoRecibida;
    private JLabel lblQR;
    private JLabel lblInstrucciones;

    public DialogoEscanearQR(Frame parent, boolean mostrarGaleria, int maxFotos, Consumer<String> onFotoRecibida) {
        super(parent, "Escanear C\u00F3digo QR", true);
        this.onFotoRecibida = onFotoRecibida;
        iniciarServidor(mostrarGaleria, maxFotos);
        iniciarDiseno();
    }

    private void iniciarServidor(boolean mostrarGaleria, int maxFotos) {
        try {
            int puerto = 8080; // Podr\u00EDamos usar un puerto aleatorio tambi\u00E9n
            servidor = new ServidorFotos(puerto, mostrarGaleria, maxFotos, base64 -> {
                // Al recibir la foto, la pasamos al Consumer y cerramos el di\u00E1logo
                SwingUtilities.invokeLater(() -> {
                    onFotoRecibida.accept(base64);
                    cerrarServidor();
                    dispose();
                });
            });
            servidor.start(); // Inicia el servidor como daemon thread
        } catch (IOException e) {
            e.printStackTrace();
            utilidades.Mensajes.showMessageDialog(this, "No se pudo iniciar el servidor local.", "Error", JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }

    private void iniciarDiseno() {
        this.setSize(400, 450);
        this.setLocationRelativeTo(getParent());
        this.setLayout(new BorderLayout());
        this.getContentPane().setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL);

        JPanel panelSuperior = new JPanel();
        panelSuperior.setBackground(utilidades.EfectosUI.COLOR_SIDEBAR_PASTEL);
        panelSuperior.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 1, 0, utilidades.EfectosUI.COLOR_BORDE),
            BorderFactory.createEmptyBorder(20, 20, 15, 20)
        ));
        
        lblInstrucciones = new JLabel("<html><center>Escanea este c\u00F3digo con tu celular<br>para tomar y subir la foto.</center></html>", SwingConstants.CENTER);
        lblInstrucciones.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblInstrucciones.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        panelSuperior.add(lblInstrucciones);
        
        this.add(panelSuperior, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(new GridBagLayout());
        panelCentral.setOpaque(false);
        
        String ip = QRHelper.obtenerIPLocal();
        String url = "http://" + ip + ":8080/";
        Image qrImage = QRHelper.generarQR(url, 250, 250);
        
        lblQR = new JLabel();
        if (qrImage != null) {
            lblQR.setIcon(new ImageIcon(qrImage));
        } else {
            lblQR.setText("Error al generar QR");
        }
        
        panelCentral.add(lblQR);
        this.add(panelCentral, BorderLayout.CENTER);

        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER));
        panelInferior.setOpaque(false);
        panelInferior.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));
        
        JLabel lblUrl = new JLabel("O visita: " + url);
        lblUrl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUrl.setForeground(utilidades.EfectosUI.COLOR_TEXTO_SUBTITULO);
        panelInferior.add(lblUrl);
        
        this.add(panelInferior, BorderLayout.SOUTH);

        // Si el usuario cierra la ventana manualmente, detener el servidor
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cerrarServidor();
            }
        });
    }

    private void cerrarServidor() {
        if (servidor != null && servidor.isAlive()) {
            servidor.stop();
        }
    }
}

