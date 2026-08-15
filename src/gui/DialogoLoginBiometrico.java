package gui;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import dao.BiometriaDAO;
import utilidades.ReconocimientoFacial;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;


import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

public class DialogoLoginBiometrico extends JDialog {
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private ReconocimientoFacial reconocedor;
    private BiometriaDAO biometriaDAO;
    
    private boolean loginExitoso = false;
    private int idUsuarioAutenticado = -1;
    private Timer timerDeteccion;
    private int fallosConsecutivos = 0;

    public DialogoLoginBiometrico(Frame parent) {
        super(parent, "Reconocimiento Facial", true);
        setSize(640, 520);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        reconocedor = new ReconocimientoFacial();
        biometriaDAO = new BiometriaDAO();

        initCamara();
    }

    private void initCamara() {
        webcam = Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(this, "No se encontr\u00F3 ninguna c\u00E1mara conectada.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setFPSDisplayed(true);
        webcamPanel.setImageSizeDisplayed(true);
        webcamPanel.setMirrored(true);

        add(webcamPanel, BorderLayout.CENTER);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> cerrarCamaraYSalir());
        add(btnCancelar, BorderLayout.SOUTH);

        // Timer para leer fotogramas y detectar rostros cada 500ms
        timerDeteccion = new Timer(500, e -> procesarFrame());
        timerDeteccion.start();
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                cerrarCamaraYSalir();
            }
        });
    }

    private void procesarFrame() {
        if (!webcam.isOpen()) return;

        BufferedImage image = webcam.getImage();
        if (image == null) return;

        try {
            // Convert BufferedImage to byte array, then to OpenCV Mat
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            
            Mat matImage = imdecode(new Mat(bytes), IMREAD_COLOR);
            
            Mat rostro = reconocedor.detectarRostro(matImage);
            if (rostro != null) {
                double[] resultado = reconocedor.predecir(rostro);
                int label = (int) resultado[0];
                double confianza = resultado[1];

                // LBPH: menor confianza es mejor. Umbral más estricto para evitar falsos positivos (ideal 50-60)
                if (label != -1 && confianza < 55.0) {
                    int idUsuario = biometriaDAO.obtenerUsuarioPorLabelId(label);
                    if (idUsuario != -1) {
                        System.out.println("Usuario reconocido: ID=" + idUsuario + " (Confianza: " + confianza + ")");
                        timerDeteccion.stop();
                        this.loginExitoso = true;
                        this.idUsuarioAutenticado = idUsuario;
                        cerrarCamaraYSalir();
                    }
                } else {
                    // Rostro detectado pero no reconocido o confianza muy pobre
                    fallosConsecutivos++;
                    if (fallosConsecutivos >= 5) {
                        timerDeteccion.stop();
                        // Mostrar el mensaje en el hilo de la UI de forma segura
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(DialogoLoginBiometrico.this, "Acceso Denegado. Rostro no reconocido o no autorizado.", "Seguridad", JOptionPane.ERROR_MESSAGE);
                            cerrarCamaraYSalir();
                        });
                    }
                }
            } else {
                // Opcional: si deja de ver un rostro, podríamos reiniciar el contador para evitar bloqueos por falsos positivos aislados
                // fallosConsecutivos = 0; 
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void cerrarCamaraYSalir() {
        if (timerDeteccion != null) timerDeteccion.stop();
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        dispose();
    }

    public boolean isLoginExitoso() {
        return loginExitoso;
    }

    public int getIdUsuarioAutenticado() {
        return idUsuarioAutenticado;
    }
}
