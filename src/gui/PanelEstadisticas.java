package gui;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.text.DecimalFormat;

public class PanelEstadisticas extends JPanel {

    private final Color COLOR_FONDO = utilidades.EfectosUI.COLOR_FONDO_PANEL; // Verde Pastel Suave Fondo
    private final Color COLOR_TARJETA = new Color(255, 255, 255); // Blanco puro para tarjetas
    private final Color COLOR_TEXTO = new Color(30, 41, 59);      // Gris pizarra oscuro
    private final Color COLOR_ACENTO = new Color(42, 157, 114);   // Verde Esmeralda Pastel
    private final Color COLOR_EXITO = new Color(42, 157, 114);    // Verde Esmeralda Pastel
    private final Color COLOR_ALERTA = new Color(239, 68, 68);    // Rojo Coral

    private JComboBox<String> cmbFiltro;
    private TacometroPanel tacometro;
    private JLabel lblMensajeComparacion;
    private JLabel lblDiferenciaPorcentaje;
    private JLabel lblTicketPromedio;
    private JLabel lblTopProducto;

    public PanelEstadisticas() {
        iniciarDiseno();
        cargarDatosDesdeBD("D\u00EDa");
    }

    private void iniciarDiseno() {
        this.removeAll();
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(COLOR_FONDO);
        this.setBorder(BorderFactory.createEmptyBorder(25, 35, 25, 35));

        // --- 1. CABECERA (T\u00CDTULO Y FILTRO) ---
        JPanel panelCabecera = new JPanel(new BorderLayout());
        panelCabecera.setOpaque(false);

        // Contenedor apilado para la Marca y el T\u00EDtulo
        JPanel panelTextos = new JPanel();
        panelTextos.setLayout(new BoxLayout(panelTextos, BoxLayout.Y_AXIS));
        panelTextos.setOpaque(false);

        // El toque corporativo minimalista
        JLabel lblMarca = new JLabel("I N V E R S I O N E S   O L V A N");
        lblMarca.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblMarca.setForeground(COLOR_ACENTO); 
        
        JLabel lblTitulo = new JLabel("Dashboard de Estad\u00EDsticas");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 28));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);

        panelTextos.add(lblMarca);
        panelTextos.add(Box.createVerticalStrut(2)); // Un mini respiro entre textos
        panelTextos.add(lblTitulo);

        String[] opcionesFiltro = {"D\u00EDa", "Semana", "Mes", "A\u00F1o"};
        cmbFiltro = new JComboBox<>(opcionesFiltro);
        cmbFiltro.setFont(new Font("Segoe UI", Font.BOLD, 14));
        cmbFiltro.setPreferredSize(new Dimension(150, 35));
        cmbFiltro.addActionListener(e -> {
            cargarDatosDesdeBD(cmbFiltro.getSelectedItem().toString());
        });

        // Agregamos los textos a la izquierda y el filtro a la derecha
        panelCabecera.add(panelTextos, BorderLayout.WEST);
        panelCabecera.add(cmbFiltro, BorderLayout.EAST);
        this.add(panelCabecera, BorderLayout.NORTH);

        // --- 2. ZONA CENTRAL (CUADR\u00CDCULA 2x2 SIM\u00C9TRICA) ---
        JPanel panelCuadricula = new JPanel(new GridLayout(2, 2, 25, 25));
        panelCuadricula.setOpaque(false);

        // Tarjeta 1: Tac\u00F3metro
        JPanel tarjetaTacometro = crearTarjeta("Ventas Totales (L.)");
        tacometro = new TacometroPanel();
        tarjetaTacometro.add(tacometro, BorderLayout.CENTER);

        // Tarjeta 2: Comparativa
        JPanel tarjetaComparativa = crearTarjeta("Rendimiento vs Per\u00EDodo Anterior");
        tarjetaComparativa.setLayout(new BoxLayout(tarjetaComparativa, BoxLayout.Y_AXIS));
        
        lblDiferenciaPorcentaje = new JLabel("+0.00%", SwingConstants.CENTER);
        lblDiferenciaPorcentaje.setFont(new Font("Segoe UI", Font.BOLD, 55)); // Fuente ampliada
        lblDiferenciaPorcentaje.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        lblMensajeComparacion = new JLabel("<html><div style='text-align: center;'>Analizando datos comerciales...</div></html>");
        lblMensajeComparacion.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        lblMensajeComparacion.setForeground(new Color(100, 116, 139));
        lblMensajeComparacion.setAlignmentX(Component.CENTER_ALIGNMENT);

        tarjetaComparativa.add(Box.createVerticalGlue());
        tarjetaComparativa.add(lblDiferenciaPorcentaje);
        tarjetaComparativa.add(Box.createVerticalStrut(20));
        tarjetaComparativa.add(lblMensajeComparacion);
        tarjetaComparativa.add(Box.createVerticalGlue());

        // Tarjeta 3: Ticket Promedio
        JPanel tarjetaTicket = crearTarjeta("Ticket Promedio");
        lblTicketPromedio = new JLabel("L. 0.00", SwingConstants.CENTER);
        lblTicketPromedio.setFont(new Font("Segoe UI", Font.BOLD, 42)); 
        lblTicketPromedio.setForeground(COLOR_TEXTO);
        tarjetaTicket.add(lblTicketPromedio, BorderLayout.CENTER);

        // Tarjeta 4: Top Producto
        JPanel tarjetaTop = crearTarjeta("Producto M\u00E1s Vendido");
        lblTopProducto = new JLabel("<html><div style='text-align: center;'>Cargando...</div></html>", SwingConstants.CENTER);
        lblTopProducto.setFont(new Font("Segoe UI", Font.BOLD, 26)); 
        lblTopProducto.setForeground(COLOR_ACENTO);
        tarjetaTop.add(lblTopProducto, BorderLayout.CENTER);

        // Agregamos todo a la cuadr\u00EDcula en orden
        panelCuadricula.add(tarjetaTacometro);
        panelCuadricula.add(tarjetaComparativa);
        panelCuadricula.add(tarjetaTicket);
        panelCuadricula.add(tarjetaTop);

        this.add(panelCuadricula, BorderLayout.CENTER);
    }

    private JPanel crearTarjeta(String titulo) {
        JPanel tarjeta = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                // Fondo blanco redondeado con sombra suave y acento superior
                g2.setColor(COLOR_TARJETA);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), getHeight(), 16, 16));
                
                // L\u00EDnea superior decorativa verde esmeralda
                g2.setColor(COLOR_ACENTO);
                g2.fill(new RoundRectangle2D.Double(0, 0, getWidth(), 4, 16, 16));
                g2.dispose();
            }
        };
        tarjeta.setOpaque(false);
        tarjeta.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 15));
        lblTitulo.setForeground(new Color(100, 116, 139));
        tarjeta.add(lblTitulo, BorderLayout.NORTH);

        return tarjeta;
    }

    // =================================================================
    // L\u00D3GICA DE ACTUALIZACI\u00D3N DE DATOS Y ANIMACI\u00D3N
    // =================================================================
    private void cargarDatosDesdeBD(String filtro) {
        dao.EstadisticasDAO dao = new dao.EstadisticasDAO();
        Object[] datos = dao.obtenerDatos(filtro);

        double ventasActual = datos[0] != null ? (double) datos[0] : 0;
        double ventasAnterior = datos[1] != null ? (double) datos[1] : 0;
        double ticketProm = datos[2] != null ? (double) datos[2] : 0;
        String topProducto = datos[3] != null ? (String) datos[3] : "Sin movimientos";

        // Definimos metas estimadas para que el tac\u00F3metro se vea proporcional
        double metaEstimada = 10000;
        switch(filtro) {
            case "D\u00EDa": metaEstimada = 25000; break; // Ejemplo: Meta diaria de 25k
            case "Semana": metaEstimada = 150000; break;
            case "Mes": metaEstimada = 500000; break;
            case "A\u00F1o": metaEstimada = 2000000; break;
        }
        
        if (ventasActual >= metaEstimada) {
            metaEstimada = ventasActual * 1.2; // Expandimos un 20% m\u00E1s
        }

        actualizarUI(ventasActual, ventasAnterior, metaEstimada, topProducto, ticketProm, filtro);
    }

    private void actualizarUI(double totalActual, double totalAnterior, double metaMax, String topProd, double ticket, String tipoFiltro) {
        // 1. Animamos el Tac\u00F3metro
        tacometro.animarA(totalActual, metaMax);

        // 2. Preparamos el formato y textos
        DecimalFormat df = new DecimalFormat("#,##0.00");
        String periodoText = tipoFiltro.toLowerCase().equals("d\u00EDa") ? "este d\u00EDa y el anterior" : 
                             "est" + (tipoFiltro.equals("Semana") ? "a " : "e ") + tipoFiltro.toLowerCase() + " y el anterior";

        // Limpiamos el \u00EDcono por defecto
        lblMensajeComparacion.setIcon(null);

        // --- VALIDACI\u00D3N DE DATOS INEXISTENTES ---
        if (totalAnterior <= 0) {
            lblDiferenciaPorcentaje.setText("-- %");
            lblDiferenciaPorcentaje.setForeground(new Color(140, 145, 150)); // Gris neutro
            
            // Inyectamos nuestro \u00EDcono vectorial y el mensaje
            lblMensajeComparacion.setIcon(new IconoFaltaDatos());
            lblMensajeComparacion.setText("<html><div style='text-align: center; padding-left: 8px;'>No existen datos suficientes del per\u00EDodo<br>anterior para calcular una diferencia.</div></html>");
            
        } else {
            // L\u00F3gica normal de c\u00E1lculo
            double porcentaje = ((totalActual - totalAnterior) / totalAnterior) * 100;
            String percStr = df.format(Math.abs(porcentaje)) + "%";

            if (porcentaje >= 0) {
                lblDiferenciaPorcentaje.setText("\u25B2 " + percStr);
                lblDiferenciaPorcentaje.setForeground(COLOR_EXITO);
                lblMensajeComparacion.setText("<html><div style='text-align: center;'>Orion Systems detect\u00F3 un <b>incremento</b> de ventas<br>del " + percStr + " entre " + periodoText + ".</div></html>");
            } else {
                lblDiferenciaPorcentaje.setText("\u25BC " + percStr);
                lblDiferenciaPorcentaje.setForeground(COLOR_ALERTA);
                lblMensajeComparacion.setText("<html><div style='text-align: center;'>Orion Systems detect\u00F3 un <b>decremento</b> de ventas<br>del " + percStr + " entre " + periodoText + ".</div></html>");
            }
        }

        // 3. Actualizamos las m\u00E9tricas inferiores
        lblTicketPromedio.setText("L. " + df.format(ticket));
        lblTopProducto.setText(topProd);
    }

    // =================================================================
    // CLASE INTERNA: TAC\u00D3METRO ANIMADO CON JAVA 2D
    // =================================================================
    private class TacometroPanel extends JPanel {
        private double valorActual = 0;
        private double valorObjetivo = 0;
        private double maximo = 10000;
        private Timer timer;

        public TacometroPanel() {
            setOpaque(false);
            timer = new Timer(15, e -> {
                double paso = (valorObjetivo - valorActual) * 0.1; // Efecto de desaceleraci\u00F3n suave
                if (Math.abs(valorObjetivo - valorActual) < 1) {
                    valorActual = valorObjetivo;
                    timer.stop();
                } else {
                    valorActual += paso;
                }
                repaint();
            });
        }

        public void animarA(double nuevoValor, double nuevoMaximo) {
            this.valorObjetivo = nuevoValor;
            this.maximo = nuevoMaximo;
            this.valorActual = 0; // Siempre inicia desde 0 al actualizar
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int width = getWidth();
            int height = getHeight();
            int paddingTop = 20; // Espacio superior para el grosor de la l\u00EDnea
            int paddingBottom = 30;
            int paddingSides = 30;
            int diameter = Math.min(width - paddingSides * 2, (height - paddingBottom - paddingTop) * 2);
            int x = (width - diameter) / 2;
            int y = height - paddingBottom - (diameter / 2); // Centro en la parte inferior

            // 1. Dibujar el arco de fondo (Gris suave / Menta muy tenue)
            g2.setStroke(new BasicStroke(20, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(226, 232, 240));
            g2.draw(new Arc2D.Double(x, y, diameter, diameter, 0, 180, Arc2D.OPEN));

            // 2. Dibujar el arco de progreso (Degradado Esmeralda a Menta)
            double porcentaje = Math.min(valorActual / maximo, 1.0);
            double anguloProgreso = porcentaje * 180;
            g2.setPaint(new GradientPaint(x, y, new Color(52, 211, 153), x + diameter, y, new Color(5, 150, 105)));
            g2.draw(new Arc2D.Double(x, y, diameter, diameter, 180 - anguloProgreso, anguloProgreso, Arc2D.OPEN));

            // 3. Texto del total en el centro
            DecimalFormat df = new DecimalFormat("#,##0.00");
            String textoCentro = "L. " + df.format(valorActual);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 30));
            g2.setColor(COLOR_TEXTO);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (width - fm.stringWidth(textoCentro)) / 2;
            int ty = y + (diameter / 2) - 20;
            g2.drawString(textoCentro, tx, ty);
            
            // Texto de Meta
            String textoMeta = "Meta: L. " + df.format(maximo);
            g2.setFont(new Font("Segoe UI", Font.BOLD, 14));
            g2.setColor(COLOR_ACENTO);
            FontMetrics fmMeta = g2.getFontMetrics();
            g2.drawString(textoMeta, (width - fmMeta.stringWidth(textoMeta)) / 2, ty + 25);
        }
    }
    
    // =================================================================
    // CLASE INTERNA: \u00CDCONO VECTORIAL DE AVISO (FALTA DE DATOS)
    // =================================================================
    private class IconoFaltaDatos implements Icon {
        @Override public int getIconWidth() { return 24; }
        @Override public int getIconHeight() { return 24; }
        
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // Color gris corporativo
            g2.setColor(new Color(140, 145, 150)); 
            g2.setStroke(new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            // Dibujar el c\u00EDrculo exterior
            g2.drawOval(x + 2, y + 2, 20, 20);
            
            // Dibujar el signo de exclamaci\u00F3n (!)
            g2.drawLine(x + 12, y + 7, x + 12, y + 13); // L\u00EDnea superior
            g2.fillOval(x + 10, y + 16, 4, 4);          // Punto inferior
            
            g2.dispose();
        }
    }                 
    @SuppressWarnings("unused")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
