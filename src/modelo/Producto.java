package modelo;

public class Producto {
    private int idProducto;
    private String codigoBarras;
    private String nombreProducto;
    private int idCategoria;
    private int idProveedor;
    private int idUbicacion;
    private double precioCompra;
    private double precioVenta;
    private double precioMayorista; // Ser\u00E1 0 si no se aplica
    private int stockMinimo;
    private int stockProducto;
    private String imagen_producto;
    private boolean eliminado; // 0 = Activo, 1 = Eliminado (Soft Delete)
    private int diasGarantia;
    private boolean requiereSerie;
    private boolean incluyeImpuesto;

    public Producto() {}

    // --- GETTERS Y SETTERS ---
    public int getIdProducto() { return idProducto; }
    public void setIdProducto(int idProducto) { this.idProducto = idProducto; }

    public String getCodigoBarras() { return codigoBarras; }
    public void setCodigoBarras(String codigoBarras) { this.codigoBarras = codigoBarras; }

    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }

    public int getIdCategoria() { return idCategoria; }
    public void setIdCategoria(int idCategoria) { this.idCategoria = idCategoria; }

    public int getIdProveedor() { return idProveedor; }
    public void setIdProveedor(int idProveedor) { this.idProveedor = idProveedor; }

    public int getIdUbicacion() { return idUbicacion; }
    public void setIdUbicacion(int idUbicacion) { this.idUbicacion = idUbicacion; }

    public double getPrecioCompra() { return precioCompra; }
    public void setPrecioCompra(double precioCompra) { this.precioCompra = precioCompra; }

    public double getPrecioVenta() { return precioVenta; }
    public void setPrecioVenta(double precioVenta) { this.precioVenta = precioVenta; }

    public double getPrecioMayorista() { return precioMayorista; }
    public void setPrecioMayorista(double precioMayorista) { this.precioMayorista = precioMayorista; }

    public int getStockMinimo() { return stockMinimo; }
    public void setStockMinimo(int stockMinimo) { this.stockMinimo = stockMinimo; }

    public int getStockProducto() { return stockProducto; }
    public void setStockProducto(int stockProducto) { this.stockProducto = stockProducto; }

    public String getImagen_producto() { return imagen_producto; }
    public void setImagen_producto(String imagen_producto) { this.imagen_producto = imagen_producto; }

    public boolean isEliminado() { return eliminado; }
    public void setEliminado(boolean eliminado) { this.eliminado = eliminado; }
    
    public int getDiasGarantia() { return diasGarantia; }
    public void setDiasGarantia(int diasGarantia) { this.diasGarantia = diasGarantia; }

    public boolean isRequiereSerie() { return requiereSerie; }
    public void setRequiereSerie(boolean requiereSerie) { this.requiereSerie = requiereSerie; }

    public boolean isIncluyeImpuesto() { return incluyeImpuesto; }
    public void setIncluyeImpuesto(boolean incluyeImpuesto) { this.incluyeImpuesto = incluyeImpuesto; }
}
