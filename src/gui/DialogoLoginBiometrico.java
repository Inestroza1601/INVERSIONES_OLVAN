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
import java.io.File;

import org.bytedeco.opencv.opencv_core.Mat;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;

public class DialogoLoginBiometrico extends JDialog {
    private Webcam webcam;
    private FaceIdPanel animPanel;
    private ReconocimientoFacial reconocedor;
    private BiometriaDAO biometriaDAO;
    
    private boolean loginExitoso = false;
    private int idUsuarioAutenticado = -1;
    private Thread hiloCamara;
    private volatile boolean corriendo = false;
    private int fallosConsecutivos = 0;

    public DialogoLoginBiometrico(Frame parent) {
        super(parent, "Reconocimiento Facial", true);
        setSize(400, 450); // Ajustar tamaño para que el Face ID se vea más cuadrado y natural
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
        if (!webcam.isOpen()) {
            webcam.open();
        }

        animPanel = new FaceIdPanel();
        add(animPanel, BorderLayout.CENTER);

        JButton btnCancelar = new JButton("Cancelar");
        btnCancelar.addActionListener(e -> cerrarCamaraYSalir());
        add(btnCancelar, BorderLayout.SOUTH);

        // Hilo en segundo plano para procesar cámara y reconocimiento sin congelar la UI
        corriendo = true;
        hiloCamara = new Thread(this::bucleCamara);
        hiloCamara.start();
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                cerrarCamaraYSalir();
            }
        });
    }

    private void bucleCamara() {
        long ultimoReconocimiento = 0;
        
        while (corriendo && webcam.isOpen()) {
            BufferedImage image = webcam.getImage();
            if (image == null) continue;
            
            // 1. Actualizar previsualización a máxima velocidad (aprox 30 FPS)
            SwingUtilities.invokeLater(() -> animPanel.updateImage(image));
            
            // 2. Reconocimiento facial cada 500ms
            long ahora = System.currentTimeMillis();
            if (ahora - ultimoReconocimiento > 500) {
                ultimoReconocimiento = ahora;
                procesarReconocimiento(image);
            }
            
            // Pequeña pausa para no saturar CPU
            try { Thread.sleep(30); } catch (Exception e) {}
        }
    }

    private void procesarReconocimiento(BufferedImage image) {

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
                        corriendo = false; // Detener bucle
                        this.loginExitoso = true;
                        this.idUsuarioAutenticado = idUsuario;
                        
                        // Iniciar la animación de éxito y luego cerrar en la UI
                        SwingUtilities.invokeLater(() -> {
                            animPanel.setSuccess(() -> cerrarCamaraYSalir());
                        });
                    }
                } else {
                    // Rostro detectado pero no reconocido o confianza muy pobre
                    fallosConsecutivos++;
                    if (fallosConsecutivos >= 5) {
                        corriendo = false; // Detener bucle
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
        corriendo = false;
        if (hiloCamara != null && hiloCamara.isAlive()) {
            hiloCamara.interrupt();
        }
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
