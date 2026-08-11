import java.nio.file.*;
import java.nio.charset.Charset;

public class AddFechasValidas {
    public static void main(String[] args) throws Exception {
        Path p = Paths.get("src/gui/PanelReportes.java");
        byte[] bytes = Files.readAllBytes(p);
        
        // Remove BOM if exists
        int start = 0;
        if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF) {
            start = 3;
        }
        
        byte[] utf8Bytes = new byte[bytes.length - start];
        System.arraycopy(bytes, start, utf8Bytes, 0, utf8Bytes.length);
        
        // Read as Windows-1252
        String content = new String(utf8Bytes, Charset.forName("windows-1252"));
        
        String replaceCambiarFiltros = 
            "    private void cambiarFiltros() {\r\n" +
            "        panelFiltros.removeAll();\r\n" +
            "        int index = cmbTipoReporte.getSelectedIndex();\r\n" +
            "        \r\n" +
            "        if (index == 0) { // Caja Diario\r\n" +
            "            panelFiltros.add(new JLabel(\"Fecha:\"));\r\n" +
            "            dpFechaDiaria = new JDateChooser(new Date());\r\n" +
            "            dpFechaDiaria.setMaxSelectableDate(new Date());\r\n" +
            "            dpFechaDiaria.getJCalendar().getDayChooser().addDateEvaluator(new FechasValidasEvaluator(reportesDAO.obtenerFechasConCaja()));\r\n" +
            "            dpFechaDiaria.setDateFormatString(\"yyyy-MM-dd\");\r\n" +
            "            aplicarEstiloDateChooser(dpFechaDiaria);\r\n" +
            "            panelFiltros.add(dpFechaDiaria);\r\n" +
            "        } \r\n" +
            "        else if (index == 1) { // Detallado Ventas\r\n" +
            "            java.util.List<Date> fechasVentas = reportesDAO.obtenerFechasConVentas();\r\n" +
            "            \r\n" +
            "            panelFiltros.add(new JLabel(\"Desde:\"));\r\n" +
            "            dpFechaDesde = new JDateChooser(new Date());\r\n" +
            "            dpFechaDesde.setMaxSelectableDate(new Date());\r\n" +
            "            dpFechaDesde.getJCalendar().getDayChooser().addDateEvaluator(new FechasValidasEvaluator(fechasVentas));\r\n" +
            "            dpFechaDesde.setDateFormatString(\"yyyy-MM-dd\");\r\n" +
            "            aplicarEstiloDateChooser(dpFechaDesde);\r\n" +
            "            panelFiltros.add(dpFechaDesde);\r\n" +
            "            \r\n" +
            "            panelFiltros.add(new JLabel(\"Hasta:\"));\r\n" +
            "            dpFechaHasta = new JDateChooser(new Date());\r\n" +
            "            dpFechaHasta.setMaxSelectableDate(new Date());\r\n" +
            "            dpFechaHasta.getJCalendar().getDayChooser().addDateEvaluator(new FechasValidasEvaluator(fechasVentas));\r\n" +
            "            dpFechaHasta.setDateFormatString(\"yyyy-MM-dd\");\r\n" +
            "            aplicarEstiloDateChooser(dpFechaHasta);\r\n" +
            "            panelFiltros.add(dpFechaHasta);\r\n" +
            "\r\n" +
            "            configurarRestriccionFechas();\r\n" +
            "        }";
            
        String targetCambiarFiltros = 
            "    private void cambiarFiltros() {\r\n" +
            "        panelFiltros.removeAll();\r\n" +
            "        int index = cmbTipoReporte.getSelectedIndex();\r\n" +
            "        \r\n" +
            "        if (index == 0) { // Caja Diario\r\n" +
            "            panelFiltros.add(new JLabel(\"Fecha:\"));\r\n" +
            "            dpFechaDiaria = new JDateChooser(new Date());\r\n" +
            "            dpFechaDiaria.setDateFormatString(\"yyyy-MM-dd\");\r\n" +
            "            aplicarEstiloDateChooser(dpFechaDiaria);\r\n" +
            "            panelFiltros.add(dpFechaDiaria);\r\n" +
            "        } \r\n" +
            "        else if (index == 1) { // Detallado Ventas\r\n" +
            "            panelFiltros.add(new JLabel(\"Desde:\"));\r\n" +
            "            dpFechaDesde = new JDateChooser(new Date());\r\n" +
            "            dpFechaDesde.setDateFormatString(\"yyyy-MM-dd\");\r\n" +
            "            aplicarEstiloDateChooser(dpFechaDesde);\r\n" +
            "            panelFiltros.add(dpFechaDesde);\r\n" +
            "            \r\n" +
            "            panelFiltros.add(new JLabel(\"Hasta:\"));\r\n" +
            "            dpFechaHasta = new JDateChooser(new Date());\r\n" +
            "            dpFechaHasta.setDateFormatString(\"yyyy-MM-dd\");\r\n" +
            "            aplicarEstiloDateChooser(dpFechaHasta);\r\n" +
            "            panelFiltros.add(dpFechaHasta);\r\n" +
            "\r\n" +
            "            configurarRestriccionFechas();\r\n" +
            "        }";
            
        String targetEstilo = "    private void aplicarEstiloDateChooser(JDateChooser dateChooser) {\r\n" +
                              "        // Estilo general\r\n" +
                              "        dateChooser.setFont(new Font(\"Segoe UI\", Font.PLAIN, 14));";
        String replaceEstilo = "    private void aplicarEstiloDateChooser(JDateChooser dateChooser) {\r\n" +
                               "        dateChooser.setPreferredSize(new Dimension(140, 30));\r\n" +
                               "        // Estilo general\r\n" +
                               "        dateChooser.setFont(new Font(\"Segoe UI\", Font.PLAIN, 14));";

        String evalClass = "\r\n" +
            "    private class FechasValidasEvaluator implements com.toedter.calendar.IDateEvaluator {\r\n" +
            "        private java.util.Set<String> fechasValidasSet = new java.util.HashSet<>();\r\n" +
            "        private SimpleDateFormat sdf = new SimpleDateFormat(\"yyyy-MM-dd\");\r\n" +
            "\r\n" +
            "        public FechasValidasEvaluator(java.util.List<Date> fechasValidas) {\r\n" +
            "            for (Date d : fechasValidas) {\r\n" +
            "                fechasValidasSet.add(sdf.format(d));\r\n" +
            "            }\r\n" +
            "        }\r\n" +
            "\r\n" +
            "        @Override\r\n" +
            "        public boolean isSpecial(Date date) { return false; }\r\n" +
            "        @Override\r\n" +
            "        public Color getSpecialForegroundColor() { return null; }\r\n" +
            "        @Override\r\n" +
            "        public Color getSpecialBackroundColor() { return null; }\r\n" +
            "        @Override\r\n" +
            "        public String getSpecialTooltip() { return null; }\r\n" +
            "        @Override\r\n" +
            "        public boolean isInvalid(Date date) { return !fechasValidasSet.contains(sdf.format(date)); }\r\n" +
            "        @Override\r\n" +
            "        public Color getInvalidForegroundColor() { return Color.LIGHT_GRAY; }\r\n" +
            "        @Override\r\n" +
            "        public Color getInvalidBackroundColor() { return null; }\r\n" +
            "        @Override\r\n" +
            "        public String getInvalidTooltip() { return \"Sin movimientos en esta fecha\"; }\r\n" +
            "    }\r\n" +
            "}";
            
        content = content.replace(targetCambiarFiltros, replaceCambiarFiltros);
        content = content.replace(targetEstilo, replaceEstilo);
        content = content.substring(0, content.lastIndexOf("}")) + evalClass;

        Files.write(p, content.getBytes(Charset.forName("windows-1252")));
        System.out.println("Modified and written to ANSI successfully.");
    }
}
