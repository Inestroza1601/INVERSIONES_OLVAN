package utilidades;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class EfectosUI {

    // --- PALETA OFICIAL: VERDE VINTAGE ELEGANTE Y BLANCO ---
    public static final Color COLOR_FONDO_PASTEL = new Color(232, 243, 236);    // Verde Pastel Suave para Fondos
    public static final Color COLOR_SIDEBAR_PASTEL = new Color(213, 233, 222);  // Verde Pastel Salvia para Sidebar
    public static final Color COLOR_FONDO_PANEL = new Color(232, 243, 236);      // Fondo uniforme de paneles
    public static final Color COLOR_CARD_BLANCO = new Color(255, 255, 255);      // Blanco puro para tablas y tarjetas
    public static final Color COLOR_TEXTO_TITULO = new Color(19, 58, 42);        // Verde Forestal Oscuro para t\u00EDtulos
    public static final Color COLOR_TEXTO_SUBTITULO = new Color(55, 100, 78);    // Verde Salvia medio
    public static final Color COLOR_TEXTO_OSCURO = new Color(30, 41, 59);        // Gris pizarra oscuro para tablas y campos
    public static final Color COLOR_BLANCO = new Color(255, 255, 255);
    public static final Color COLOR_BORDE = new Color(180, 208, 192);

    public static final Color COLOR_VERDE_PRIMARIO = new Color(45, 106, 79);     // Verde Bosque Vintage Elegante
    public static final Color COLOR_VERDE_HOVER = new Color(30, 77, 56);         // Verde Vintage Profundo
    public static final Color COLOR_VERDE_PRESIONADO = new Color(165, 205, 185); // Verde presionado
    public static final Color COLOR_VERDE_CLARO = new Color(225, 239, 230);      // Salvia claro suave

    public static final Color COLOR_ROJO_PRIMARIO = new Color(239, 68, 68);
    public static final Color COLOR_ROJO_HOVER = new Color(220, 38, 38);
    public static final Color COLOR_ROJO_CLARO = new Color(254, 242, 242);

    /**
     * Aplica animaci\u00F3n suave de color a cualquier JButton en 60 FPS
     * y cambia el texto a negro cuando se hace clic (mousePressed)
     */
    public static void aplicarEfectoHover(JButton boton, Color colorBase, Color colorHover, Color fgBase, Color fgHover) {
        boton.setFocusPainted(false);
        boton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        boton.setBackground(colorBase);
        boton.setForeground(fgBase);

        boton.addMouseListener(new MouseAdapter() {
            private float progreso = 0.0f;
            private Timer timer;
            private boolean isPressed = false;

            private void animar(boolean entrar) {
                if (!boton.isEnabled()) return;
                if (timer != null && timer.isRunning()) timer.stop();

                timer = new Timer(15, e -> {
                    if (entrar) {
                        progreso += 0.20f;
                        if (progreso >= 1.0f) {
                            progreso = 1.0f;
                            timer.stop();
                        }
                    } else {
                        progreso -= 0.20f;
                        if (progreso <= 0.0f) {
                            progreso = 0.0f;
                            timer.stop();
                        }
                    }

                    Color actualBg = mezclar(colorBase, colorHover, progreso);
                    Color actualFg = isPressed ? Color.BLACK : mezclar(fgBase, fgHover, progreso);
                    boton.setBackground(actualBg);
                    boton.setForeground(actualFg);
                    boton.repaint();
                });
                timer.start();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                animar(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                animar(false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                if (!boton.isEnabled()) return;
                isPressed = true;
                boton.setForeground(Color.BLACK); // Texto negro al dar clic
                boton.repaint();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!boton.isEnabled()) return;
                isPressed = false;
                if (boton.contains(e.getPoint())) {
                    boton.setForeground(fgHover);
                } else {
                    boton.setForeground(fgBase);
                }
                boton.repaint();
            }
        });
    }

    /**
     * Crea un bot\u00F3n primario verde moderno con animaci\u00F3n de texto negro en hover
     */
    public static JButton crearBotonVerde(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.putClientProperty("JButton.buttonType", "roundRect");
        boton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        aplicarEfectoHover(boton, COLOR_VERDE_PRIMARIO, COLOR_VERDE_HOVER, COLOR_BLANCO, Color.BLACK);
        return boton;
    }

    /**
     * Crea un bot\u00F3n secundario blanco con borde y animaci\u00F3n con texto negro en hover
     */
    public static JButton crearBotonBlanco(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.putClientProperty("JButton.buttonType", "roundRect");
        boton.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_BORDE, 1),
                BorderFactory.createEmptyBorder(7, 15, 7, 15)
        ));
        aplicarEfectoHover(boton, COLOR_BLANCO, COLOR_VERDE_CLARO, COLOR_TEXTO_OSCURO, Color.BLACK);
        return boton;
    }

    /**
     * Crea un bot\u00F3n de peligro/cancelar con animaci\u00F3n y texto negro en hover
     */
    public static JButton crearBotonPeligro(String texto) {
        JButton boton = new JButton(texto);
        boton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        boton.putClientProperty("JButton.buttonType", "roundRect");
        boton.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
        aplicarEfectoHover(boton, COLOR_ROJO_PRIMARIO, COLOR_ROJO_HOVER, COLOR_BLANCO, Color.BLACK);
        return boton;
    }

    private static Color mezclar(Color c1, Color c2, float t) {
        t = Math.max(0.0f, Math.min(1.0f, t));
        int r = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * t);
        int g = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * t);
        int b = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * t);
        return new Color(r, g, b);
    }
}
