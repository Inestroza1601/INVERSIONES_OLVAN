package modelo;

public class DetalleApartado {
    private int idDetalleApartado;
    private int idApartado;
    private int idProducto;
    private String descripcionApartado;
    private int cantidadApartado;
    private double precioUnitarioApartado;
    private double subtotalApartado;
    private String identificadorSerie;
    private String codigoBarras; // For joins
    private String nombreProducto;

    public DetalleApartado() {
    }

    // Getters and Setters
    public int getIdDetalleApartado() {
        return idDetalleApartado;
    }

    public void setIdDetalleApartado(int idDetalleApartado) {
        this.idDetalleApartado = idDetalleApartado;
    }

    public int getIdApartado() {
        return idApartado;
    }

    public void setIdApartado(int idApartado) {
        this.idApartado = idApartado;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public void setIdProducto(int idProducto) {
        this.idProducto = idProducto;
    }

    public String getDescripcionApartado() {
        return descripcionApartado;
    }

    public void setDescripcionApartado(String descripcionApartado) {
        this.descripcionApartado = descripcionApartado;
    }

    public int getCantidadApartado() {
        return cantidadApartado;
    }

    public void setCantidadApartado(int cantidadApartado) {
        this.cantidadApartado = cantidadApartado;
    }

    public double getPrecioUnitarioApartado() {
        return precioUnitarioApartado;
    }

    public void setPrecioUnitarioApartado(double precioUnitarioApartado) {
        this.precioUnitarioApartado = precioUnitarioApartado;
    }

    public double getSubtotalApartado() {
        return subtotalApartado;
    }

    public void setSubtotalApartado(double subtotalApartado) {
        this.subtotalApartado = subtotalApartado;
    }

    public String getIdentificadorSerie() {
        return identificadorSerie;
    }

    public void setIdentificadorSerie(String identificadorSerie) {
        this.identificadorSerie = identificadorSerie;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public void setNombreProducto(String nombreProducto) {
        this.nombreProducto = nombreProducto;
    }
}
