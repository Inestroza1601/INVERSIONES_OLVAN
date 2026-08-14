package utilidades;

import org.bytedeco.javacpp.DoublePointer;
import org.bytedeco.javacpp.IntPointer;
import org.bytedeco.opencv.opencv_core.*;
import org.bytedeco.opencv.opencv_face.*;
import org.bytedeco.opencv.opencv_objdetect.*;
import static org.bytedeco.opencv.global.opencv_core.*;
import static org.bytedeco.opencv.global.opencv_face.*;
import static org.bytedeco.opencv.global.opencv_imgproc.*;
import static org.bytedeco.opencv.global.opencv_imgcodecs.*;
import static org.bytedeco.opencv.global.opencv_objdetect.*;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.IntBuffer;

public class ReconocimientoFacial {

    private LBPHFaceRecognizer recognizer;
    private CascadeClassifier faceDetector;
    private static final String CLASSIFIER_PATH = "haarcascade_frontalface_alt.xml";
    private static final String MODEL_PATH = "biometria_data/modelo_lbph.yml";
    private static final String FACES_DIR = "biometria_data/rostros";

    public ReconocimientoFacial() {
        // Asegurar que existan los directorios
        new File(FACES_DIR).mkdirs();

        faceDetector = new CascadeClassifier(CLASSIFIER_PATH);
        if (faceDetector.empty()) {
            System.err.println("Error: No se pudo cargar el clasificador Haar Cascade en: " + CLASSIFIER_PATH);
        }

        recognizer = LBPHFaceRecognizer.create();
        cargarModelo();
    }

    private void cargarModelo() {
        File f = new File(MODEL_PATH);
        if (f.exists() && f.length() > 0) {
            try {
                recognizer.read(MODEL_PATH);
                System.out.println("Modelo de reconocimiento facial cargado.");
            } catch (Exception e) {
                System.err.println("Error al leer el modelo LBPH: " + e.getMessage());
            }
        } else {
            System.out.println("No se encontr\u00F3 modelo pre-entrenado. Necesitas registrar usuarios primero.");
        }
    }

    /**
     * Detecta un rostro en la imagen dada y lo devuelve recortado y redimensionado a 160x160.
     * Retorna null si no detecta rostros.
     */
    public Mat detectarRostro(Mat imagen) {
        Mat imagenGris = new Mat();
        cvtColor(imagen, imagenGris, COLOR_BGR2GRAY);
        equalizeHist(imagenGris, imagenGris);

        RectVector rostros = new RectVector();
        faceDetector.detectMultiScale(imagenGris, rostros, 1.1, 3, 0, new Size(100, 100), new Size(500, 500));

        if (rostros.size() > 0) {
            Rect rostro = rostros.get(0); // Tomamos el primer rostro detectado
            Mat rostroRecortado = new Mat(imagenGris, rostro);
            Mat rostroRedimensionado = new Mat();
            resize(rostroRecortado, rostroRedimensionado, new Size(160, 160));
            return rostroRedimensionado;
        }
        return null;
    }

    /**
     * Entrena el algoritmo con todas las fotos almacenadas en la carpeta de rostros.
     */
    public boolean entrenarModelo() {
        File dir = new File(FACES_DIR);
        FilenameFilter imgFilter = (d, name) -> name.endsWith(".jpg") || name.endsWith(".png");
        File[] archivos = dir.listFiles(imgFilter);

        if (archivos == null || archivos.length == 0) {
            System.out.println("No hay im\u00E1genes para entrenar.");
            return false;
        }

        MatVector fotos = new MatVector(archivos.length);
        Mat etiquetas = new Mat(archivos.length, 1, CV_32SC1);
        IntBuffer bufferEtiquetas = etiquetas.createBuffer();

        int contador = 0;
        for (File imagenArchivo : archivos) {
            Mat foto = imread(imagenArchivo.getAbsolutePath(), IMREAD_GRAYSCALE);
            
            // El formato del nombre es: labelId_numero.jpg
            String nombre = imagenArchivo.getName();
            int label = Integer.parseInt(nombre.split("_")[0]);

            fotos.put(contador, foto);
            bufferEtiquetas.put(contador, label);
            contador++;
        }

        try {
            recognizer.train(fotos, etiquetas);
            recognizer.save(MODEL_PATH);
            System.out.println("Modelo entrenado y guardado exitosamente con " + contador + " im\u00E1genes.");
            return true;
        } catch (Exception e) {
            System.err.println("Error al entrenar el modelo: " + e.getMessage());
            return false;
        }
    }

    /**
     * Predice a qui\u00E9n pertenece el rostro en la foto.
     * Retorna un arreglo: [labelId, nivelConfianza]
     * Nivel de confianza m\u00E1s bajo = mayor precisi\u00F3n (idealmente < 60)
     */
    public double[] predecir(Mat rostroRedimensionado) {
        IntPointer label = new IntPointer(1);
        DoublePointer confianza = new DoublePointer(1);
        
        recognizer.predict(rostroRedimensionado, label, confianza);
        
        return new double[]{label.get(0), confianza.get(0)};
    }
    
    public String getFacesDir() {
        return FACES_DIR;
    }
}
