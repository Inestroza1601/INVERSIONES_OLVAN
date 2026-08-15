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

public class DialogoRegistroBiometrico extends JDialog {
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private ReconocimientoFacial reconocedor;
    private BiometriaDAO biometriaDAO;
    
    private int labelId;
    private int fotosTomadas = 0;
    private final int MAX_FOTOS = 30;
    private Timer timerCaptura;
    private JLabel lblEstado;

    public DialogoRegistroBiometrico(Frame parent, int idUsuario) {
        super(parent, "Registro Biom\u00E9trico", true);
        setSize(640, 560);
        setLocationRelativeTo(parent);
        setLayout(new BorderLayout());

        reconocedor = new ReconocimientoFacial();
        biometriaDAO = new BiometriaDAO();
        
        // Registrar en BD para obtener el Label
        this.labelId = biometriaDAO.registrarUsuarioBiometria(idUsuario);

        initUI();
    }

    private void initUI() {
        webcam = Webcam.getDefault();
        if (webcam == null) {
            JOptionPane.showMessageDialog(this, "No se encontr\u00F3 ninguna c\u00E1mara conectada.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setMirrored(true);
        add(webcamPanel, BorderLayout.CENTER);

        JPanel pnlSur = new JPanel(new BorderLayout());
        lblEstado = new JLabel("Presiona 'Iniciar Captura' y mira fijamente a la c\u00E1mara.", SwingConstants.CENTER);
        lblEstado.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblEstado.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        pnlSur.add(lblEstado, BorderLayout.NORTH);

        JPanel pnlBotones = new JPanel();
        JButton btnIniciar = new JButton("Iniciar Captura");
        JButton btnCancelar = new JButton("Cancelar / Salir");
        
        btnIniciar.addActionListener(e -> {
            btnIniciar.setEnabled(false);
            iniciarCaptura();
        });
        
        btnCancelar.addActionListener(e -> cerrarCamaraYSalir());

        pnlBotones.add(btnIniciar);
        pnlBotones.add(btnCancelar);
        pnlSur.add(pnlBotones, BorderLayout.SOUTH);

        add(pnlSur, BorderLayout.SOUTH);

        timerCaptura = new Timer(200, e -> procesarCaptura());
        
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                cerrarCamaraYSalir();
            }
        });
    }

    private void iniciarCaptura() {
        fotosTomadas = 0;
        timerCaptura.start();
        lblEstado.setText("Capturando... 0/" + MAX_FOTOS);
    }

    private void procesarCaptura() {
        if (!webcam.isOpen()) return;

        BufferedImage image = webcam.getImage();
        if (image == null) return;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] bytes = baos.toByteArray();
            
            Mat matImage = imdecode(new Mat(bytes), IMREAD_COLOR);
            Mat rostro = reconocedor.detectarRostro(matImage);
            
            if (rostro != null) {
                fotosTomadas++;
                lblEstado.setText("Capturando rostro... " + fotosTomadas + "/" + MAX_FOTOS);
                
                // Guardar la imagen con el formato label_numero.jpg
                String filePath = reconocedor.getFacesDir() + "/" + labelId + "_" + fotosTomadas + ".jpg";
                imwrite(filePath, rostro);

                if (fotosTomadas >= MAX_FOTOS) {
                    timerCaptura.stop();
                    lblEstado.setText("\u00A1Captura completada! Entrenando modelo...");
                    
                    // Entrenar el modelo con las nuevas fotos
                    new SwingWorker<Boolean, Void>() {
                        @Override
                        protected Boolean doInBackground() {
                            return reconocedor.entrenarModelo();
                        }
                        @Override
                        protected void done() {
                            try {
                                if (get()) {
                                    JOptionPane.showMessageDialog(DialogoRegistroBiometrico.this, "\u00A1Modelo entrenado exitosamente!");
                                } else {
                                    JOptionPane.showMessageDialog(DialogoRegistroBiometrico.this, "Error al entrenar el modelo.", "Error", JOptionPane.ERROR_MESSAGE);
                                }
                            } catch (Exception ex) {}
                            cerrarCamaraYSalir();
                        }
                    }.execute();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void cerrarCamaraYSalir() {
        if (timerCaptura != null) timerCaptura.stop();
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
        dispose();
    }
}
