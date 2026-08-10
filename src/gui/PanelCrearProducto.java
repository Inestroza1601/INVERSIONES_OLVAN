package gui;

import dao.InventarioDAO;
import dao.CatalogosDAO;
import modelo.Producto;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;

public class PanelCrearProducto extends JPanel {

    private JTextField txtCodigoBarras;
    private JLabel lblErrorCodigo; 
    private JTextField txtNombre;
    
    private JComboBox<ItemCatalogo> cmbCategoria;
    private JComboBox<ItemCatalogo> cmbProveedor;
    private JComboBox<ItemCatalogo> cmbUbicacion;
    
    private JTextField txtPrecioCompra;
    private JTextField txtPrecioVenta;
    private JCheckBox chkPrecioMayorista;
    private JTextField txtPrecioMayorista;
    
    private JTextField txtStockInicial;
    private JTextField txtStockMinimo;
    
    private JLabel lblVistaPreviaImagen;
    private String imagenSeleccionada = null; 
    private JButton btnGuardar;
    private java.util.List<String> imagenesSeleccionadas = new java.util.ArrayList<>();
    private int indiceImagenActual = -1;
    private JLabel lblNavegacionImg;
    private JButton btnAnteriorImg;
    private JButton btnSiguienteImg;
    private JButton btnEliminarImg;
    
    private JComboBox<String> cmbDiasGarantia;
    private final int[] valoresGarantia = {0, 3, 7, 15, 30, 60, 90,365}; // Array interno para guardar en BD
    private JCheckBox chkRequiereSerie;
    private JCheckBox chkIncluyeImpuesto;
    
    private Producto productoAEditar = null;
    private JButton btnKardex;
    
    private Set<String> codigosEnRam;

    public PanelCrearProducto() {
        this(null); // Llama al constructor de abajo en modo "Crear"
    }

    public PanelCrearProducto(Producto p) {
        this.productoAEditar = p;
        InventarioDAO dao = new InventarioDAO();
        codigosEnRam = dao.obtenerCodigosEnRam();
        if (codigosEnRam == null) codigosEnRam = new HashSet<>();
        
        if(productoAEditar != null) {
            codigosEnRam.remove(productoAEditar.getCodigoBarras()); // Evita que su propio código marque error
        }
        
        iniciarDiseno();
        configurarValidacionEnVivo();
        aplicarRestriccionesNumericas();
        cargarDatosCombos(); 
        
        if(productoAEditar != null) {
            cargarDatosEdicion();
        }
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout(20, 20));
        this.setBackground(utilidades.EfectosUI.COLOR_FONDO_PANEL); // Verde Vintage
        this.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        String textoTitulo = (productoAEditar == null) ? "Registrar Nuevo Producto" : "Edición de Producto";
        JLabel lblTitulo = new JLabel(textoTitulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 24));
        lblTitulo.setForeground(utilidades.EfectosUI.COLOR_TEXTO_TITULO);
        this.add(lblTitulo, BorderLayout.NORTH);

        JPanel panelCentral = new JPanel(new BorderLayout(30, 0));
        panelCentral.setOpaque(false);

        // --- IZQUIERDA: FORMULARIO ---
        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(new Color(255, 255, 255)); // Blanco Puro
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        txtCodigoBarras = new JTextField(20);
        txtCodigoBarras.putClientProperty("JTextField.placeholderText", "Dejar vacío para usar ID autogenerado");
        
        lblErrorCodigo = new JLabel(" ");
        lblErrorCodigo.setForeground(new Color(227, 0, 15)); // Rojo Logo
        lblErrorCodigo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        txtNombre = new JTextField(20);
        
        cmbCategoria = new JComboBox<>();
        cmbProveedor = new JComboBox<>();
        cmbUbicacion = new JComboBox<>();
        
        txtPrecioCompra = new JTextField(10);
        txtPrecioVenta = new JTextField(10);
        
        chkPrecioMayorista = new JCheckBox("Habilitar Precio Mayorista");
        chkPrecioMayorista.setBackground(new Color(255, 255, 255)); // Blanco Puro
        chkPrecioMayorista.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        chkPrecioMayorista.setFocusPainted(false);
        
        txtPrecioMayorista = new JTextField(10);
        txtPrecioMayorista.setEnabled(false); 
        
        chkPrecioMayorista.addActionListener(e -> {
            txtPrecioMayorista.setEnabled(chkPrecioMayorista.isSelected());
            if (!chkPrecioMayorista.isSelected()) txtPrecioMayorista.setText(""); 
        });

        txtStockInicial = new JTextField(10);
        txtStockMinimo = new JTextField("0", 10); 
        agregarFilaCorta(pnlForm, gbc, 0, "Código de Barras:", txtCodigoBarras);
        gbc.gridy = 1; gbc.gridx = 1; gbc.insets = new Insets(0, 10, 10, 10);
        pnlForm.add(lblErrorCodigo, gbc);
        gbc.insets = new Insets(10, 10, 10, 10); 
        
        agregarFila(pnlForm, gbc, 2, "Nombre del Producto:", txtNombre);
        agregarFila(pnlForm, gbc, 3, "Categoría:", crearPanelCatalogo(cmbCategoria, "Categoría"));
        agregarFila(pnlForm, gbc, 4, "Proveedor:", crearPanelCatalogo(cmbProveedor, "Proveedor"));
        agregarFila(pnlForm, gbc, 5, "Ubicación:", crearPanelCatalogo(cmbUbicacion, "Ubicación"));

        cmbDiasGarantia = new JComboBox<>(new String[]{"Sin garantía", "3 días", "7 días", "15 días", "30 días", "60 días", "90 días", "1 año"});
        cmbDiasGarantia.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        agregarFilaCorta(pnlForm, gbc, 6, "Garantía (Días):", cmbDiasGarantia);

        chkRequiereSerie = new JCheckBox("Solicitar Identificador al facturar");
        chkRequiereSerie.setBackground(new Color(255, 255, 255)); // Blanco Puro
        chkRequiereSerie.setForeground(new Color(39, 174, 96)); // Verde Menta en vez del azul original
        chkRequiereSerie.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkRequiereSerie.setFocusPainted(false);
        
        chkIncluyeImpuesto = new JCheckBox("Aplica Impuesto (ISV 15%)");
        chkIncluyeImpuesto.setBackground(new Color(255, 255, 255));
        chkIncluyeImpuesto.setForeground(new Color(39, 174, 96));
        chkIncluyeImpuesto.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkIncluyeImpuesto.setFocusPainted(false);
        
        JPanel pnlChecks = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 0));
        pnlChecks.setOpaque(false);
        pnlChecks.add(chkRequiereSerie);
        pnlChecks.add(chkIncluyeImpuesto);

        gbc.gridy = 7; gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        pnlForm.add(pnlChecks, gbc);
        agregarFilaCorta(pnlForm, gbc, 8, "Precio Compra (L):", txtPrecioCompra);
        agregarFilaCorta(pnlForm, gbc, 9, "Precio Venta (L):", txtPrecioVenta);
        
        gbc.gridy = 10; gbc.gridx = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        pnlForm.add(chkPrecioMayorista, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        txtPrecioMayorista.setPreferredSize(new Dimension(150, 32)); 
        txtPrecioMayorista.setBackground(new Color(250, 250, 250)); // Fondo claro input
        txtPrecioMayorista.setForeground(new Color(45, 45, 45)); // Texto Oscuro
        txtPrecioMayorista.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        pnlForm.add(txtPrecioMayorista, gbc);
        agregarFilaCorta(pnlForm, gbc, 11, "Stock Inicial:", txtStockInicial);
        agregarFilaCorta(pnlForm, gbc, 12, "Stock Mínimo:", txtStockMinimo);

        gbc.gridy = 13; gbc.weighty = 1.0;
        pnlForm.add(new JLabel(""), gbc);
        JScrollPane scrollForm = new JScrollPane(pnlForm);
        scrollForm.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 222, 225), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        scrollForm.getVerticalScrollBar().setUnitIncrement(16);

        // --- DERECHA: PANEL DE IMAGEN ---
        JPanel pnlImagen = new JPanel(); 
        pnlImagen.setLayout(new BoxLayout(pnlImagen, BoxLayout.Y_AXIS));
        pnlImagen.setBackground(new Color(255, 255, 255)); // Blanco Puro
        pnlImagen.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 222, 225), 1),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        pnlImagen.setPreferredSize(new Dimension(250, 0)); 

        JLabel lblTituloImg = new JLabel("Fotografías (Máx 7)");
        lblTituloImg.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        lblTituloImg.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTituloImg.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblVistaPreviaImagen = new JLabel("Sin Imagen", SwingConstants.CENTER);
        lblVistaPreviaImagen.setPreferredSize(new Dimension(150, 150));
        lblVistaPreviaImagen.setMaximumSize(new Dimension(150, 150));
        lblVistaPreviaImagen.setBorder(BorderFactory.createDashedBorder(Color.GRAY, 3, 2));
        lblVistaPreviaImagen.setForeground(Color.GRAY);
        lblVistaPreviaImagen.setAlignmentX(Component.CENTER_ALIGNMENT);

        lblVistaPreviaImagen.setCursor(new Cursor(Cursor.HAND_CURSOR));
        lblVistaPreviaImagen.setToolTipText("Clic para ver imagen en grande");
        lblVistaPreviaImagen.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (indiceImagenActual >= 0 && indiceImagenActual < imagenesSeleccionadas.size()) {
                    Window window = SwingUtilities.getWindowAncestor(PanelCrearProducto.this);
                    if (window instanceof Frame) {
                        new DialogoVisorImagen((Frame) window, "Visor de Fotografía", imagenesSeleccionadas, indiceImagenActual).setVisible(true);
                    } else if (window instanceof JDialog) {
                        new DialogoVisorImagen((JDialog) window, "Visor de Fotografía", imagenesSeleccionadas, indiceImagenActual).setVisible(true);
                    }
                }
            }
        });

        // Controles de Carrusel
        JPanel pnlCarrusel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pnlCarrusel.setOpaque(false);
        pnlCarrusel.setAlignmentX(Component.CENTER_ALIGNMENT);

        btnAnteriorImg = new JButton("<");
        btnAnteriorImg.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnAnteriorImg.setPreferredSize(new Dimension(40, 26));
        btnAnteriorImg.setFocusPainted(false);
        btnAnteriorImg.setEnabled(false);
        btnAnteriorImg.addActionListener(e -> {
            if (indiceImagenActual > 0) {
                indiceImagenActual--;
                actualizarVistaPreviaImagen();
            }
        });

        lblNavegacionImg = new JLabel("0 / 0", SwingConstants.CENTER);
        lblNavegacionImg.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblNavegacionImg.setPreferredSize(new Dimension(50, 26));

        btnSiguienteImg = new JButton(">");
        btnSiguienteImg.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnSiguienteImg.setPreferredSize(new Dimension(40, 26));
        btnSiguienteImg.setFocusPainted(false);
        btnSiguienteImg.setEnabled(false);
        btnSiguienteImg.addActionListener(e -> {
            if (indiceImagenActual < imagenesSeleccionadas.size() - 1) {
                indiceImagenActual++;
                actualizarVistaPreviaImagen();
            }
        });

        pnlCarrusel.add(btnAnteriorImg);
        pnlCarrusel.add(lblNavegacionImg);
        pnlCarrusel.add(btnSiguienteImg);

        btnEliminarImg = new JButton("Eliminar Foto");
        btnEliminarImg.setFont(new Font("Segoe UI", Font.BOLD, 11));
        btnEliminarImg.setBackground(new Color(220, 53, 69));
        btnEliminarImg.setForeground(Color.WHITE);
        btnEliminarImg.setFocusPainted(false);
        btnEliminarImg.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnEliminarImg.setMaximumSize(new Dimension(150, 26));
        btnEliminarImg.setEnabled(false);
        btnEliminarImg.addActionListener(e -> {
            if (indiceImagenActual >= 0 && indiceImagenActual < imagenesSeleccionadas.size()) {
                imagenesSeleccionadas.remove(indiceImagenActual);
                if (indiceImagenActual >= imagenesSeleccionadas.size()) {
                    indiceImagenActual = imagenesSeleccionadas.size() - 1;
                }
                actualizarVistaPreviaImagen();
            }
        });

        JButton btnTomarFoto = new JButton("Foto Celular");
        btnTomarFoto.setIcon(new IconoCamara());
        btnTomarFoto.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnTomarFoto.setBackground(new Color(39, 174, 96)); // Verde en lugar de rojo
        btnTomarFoto.setForeground(Color.WHITE);
        btnTomarFoto.setFocusPainted(false);
        btnTomarFoto.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnTomarFoto.setMaximumSize(new Dimension(150, 35));
        btnTomarFoto.addActionListener(e -> {
            if (imagenesSeleccionadas.size() >= 7) {
                JOptionPane.showMessageDialog(this, "Límite de 7 imágenes alcanzado.", "Límite Excedido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Window window = SwingUtilities.getWindowAncestor(PanelCrearProducto.this);
            Frame frame = (window instanceof Frame) ? (Frame) window : null;
            new DialogoEscanearQR(frame, true, b64 -> {
                imagenesSeleccionadas.add(b64);
                indiceImagenActual = imagenesSeleccionadas.size() - 1;
                actualizarVistaPreviaImagen();
                JOptionPane.showMessageDialog(this, "Foto capturada y adjuntada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
            }).setVisible(true);
        });

        JButton btnCargarImagen = new JButton("De la PC");
        btnCargarImagen.setIcon(new IconoCarpeta());
        btnCargarImagen.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCargarImagen.setBackground(new Color(240, 242, 245));
        btnCargarImagen.setForeground(new Color(45, 45, 45));
        btnCargarImagen.setFocusPainted(false);
        btnCargarImagen.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCargarImagen.setMaximumSize(new Dimension(150, 35));
        btnCargarImagen.addActionListener(e -> seleccionarImagen());

        JButton btnCargarURL = new JButton("De la Web");
        btnCargarURL.setIcon(new IconoNube());
        btnCargarURL.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnCargarURL.setBackground(new Color(240, 242, 245));
        btnCargarURL.setForeground(new Color(45, 45, 45));
        btnCargarURL.setFocusPainted(false);
        btnCargarURL.setAlignmentX(Component.CENTER_ALIGNMENT);
        btnCargarURL.setMaximumSize(new Dimension(150, 35));
        btnCargarURL.addActionListener(e -> cargarImagenDesdeURL());

        pnlImagen.add(lblTituloImg);
        pnlImagen.add(Box.createVerticalStrut(15));
        pnlImagen.add(lblVistaPreviaImagen);
        pnlImagen.add(Box.createVerticalStrut(10));
        pnlImagen.add(pnlCarrusel);
        pnlImagen.add(Box.createVerticalStrut(5));
        pnlImagen.add(btnEliminarImg);
        pnlImagen.add(Box.createVerticalStrut(15));
        pnlImagen.add(btnTomarFoto);
        pnlImagen.add(Box.createVerticalStrut(5));
        pnlImagen.add(btnCargarImagen);
        pnlImagen.add(Box.createVerticalStrut(5));
        pnlImagen.add(btnCargarURL);

        panelCentral.add(scrollForm, BorderLayout.CENTER);
        panelCentral.add(pnlImagen, BorderLayout.EAST);
        this.add(panelCentral, BorderLayout.CENTER);

        // --- PANEL INFERIOR: BOTONES ---
        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotones.setOpaque(false);
        
        btnKardex = new JButton("Ver Kardex");
        btnKardex.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnKardex.setForeground(Color.WHITE);
        btnKardex.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnKardex.setPreferredSize(new Dimension(140, 40));
        btnKardex.setFocusPainted(false);
        btnKardex.setVisible(productoAEditar != null);
        btnKardex.addActionListener(e -> {
            Window parentWindow = SwingUtilities.getWindowAncestor(this);
            DialogoKardex dialogo = new DialogoKardex(parentWindow, productoAEditar);
            dialogo.setVisible(true);
            InventarioDAO dao = new InventarioDAO();
            productoAEditar = dao.obtenerProductoPorId(productoAEditar.getIdProducto());
            cargarDatosEdicion(); // Refresca los textfields visualmente
        });
        String textoBtnGuardar = (productoAEditar == null) ? "Guardar Producto" : "Actualizar Producto";
        btnGuardar = new JButton(textoBtnGuardar);
        btnGuardar.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.setPreferredSize(new Dimension(180, 40));
        btnGuardar.setFocusPainted(false);
        btnGuardar.addActionListener(e -> guardarProducto());
        
        pnlBotones.add(btnKardex);
        pnlBotones.add(btnGuardar);
        this.add(pnlBotones, BorderLayout.SOUTH);
    }

    // =========================================================
    // LÓGICA: RESTRICCIONES DE TECLADO
    // =========================================================
    private void aplicarRestriccionesNumericas() {
        permitirSoloNumeros(txtPrecioCompra, true);
        permitirSoloNumeros(txtPrecioVenta, true);
        permitirSoloNumeros(txtPrecioMayorista, true);
        permitirSoloNumeros(txtStockInicial, false);
        permitirSoloNumeros(txtStockMinimo, false);
    }

    private void permitirSoloNumeros(JTextField campo, boolean permiteDecimales) {
        campo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                char c = e.getKeyChar();
                if (permiteDecimales) {
                    if (!Character.isDigit(c) && c != '.') e.consume();
                    if (c == '.' && campo.getText().contains(".")) e.consume();
                } else {
                    if (!Character.isDigit(c)) e.consume();
                }
            }
        });
    }

    // =========================================================
    // LÓGICA DE GUARDADO EN BASE DE DATOS
    // =========================================================
    private void cargarDatosCombos() {
        CatalogosDAO dao = new CatalogosDAO();
        
        cmbCategoria.removeAllItems();
        cmbCategoria.addItem(new ItemCatalogo(0, "Seleccione Categoría..."));
        for (Map.Entry<Integer, String> entry : dao.listarCategorias().entrySet()) {
            cmbCategoria.addItem(new ItemCatalogo(entry.getKey(), entry.getValue()));
        }
        
        cmbProveedor.removeAllItems();
        cmbProveedor.addItem(new ItemCatalogo(0, "Seleccione Proveedor..."));
        for (Map.Entry<Integer, String> entry : dao.listarProveedores().entrySet()) {
            cmbProveedor.addItem(new ItemCatalogo(entry.getKey(), entry.getValue()));
        }
        
        cmbUbicacion.removeAllItems();
        cmbUbicacion.addItem(new ItemCatalogo(0, "Seleccione Ubicación..."));
        for (Map.Entry<Integer, String> entry : dao.listarUbicaciones().entrySet()) {
            cmbUbicacion.addItem(new ItemCatalogo(entry.getKey(), entry.getValue()));
        }
    }
    
    private void guardarProducto() {
        if (txtNombre.getText().trim().isEmpty() || txtPrecioCompra.getText().trim().isEmpty() || txtPrecioVenta.getText().trim().isEmpty() || txtStockInicial.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Complete los campos obligatorios (Nombre, Precios y Stock).", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        ItemCatalogo cat = (ItemCatalogo) cmbCategoria.getSelectedItem();
        ItemCatalogo prov = (ItemCatalogo) cmbProveedor.getSelectedItem();
        ItemCatalogo ubi = (ItemCatalogo) cmbUbicacion.getSelectedItem();
        
        if (cat == null || cat.id == 0 || prov == null || prov.id == 0 || ubi == null || ubi.id == 0) {
            JOptionPane.showMessageDialog(this, "Debe seleccionar Categoría, Proveedor y Ubicación.", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Producto p = (productoAEditar == null) ? new Producto() : productoAEditar;
            p.setCodigoBarras(txtCodigoBarras.getText().trim());
            p.setNombreProducto(txtNombre.getText().trim());
            p.setIdCategoria(cat.id);
            p.setIdProveedor(prov.id);
            p.setIdUbicacion(ubi.id);
            p.setPrecioCompra(Double.parseDouble(txtPrecioCompra.getText().trim()));
            p.setPrecioVenta(Double.parseDouble(txtPrecioVenta.getText().trim()));
            p.setPrecioMayorista(chkPrecioMayorista.isSelected() && !txtPrecioMayorista.getText().trim().isEmpty() ? Double.parseDouble(txtPrecioMayorista.getText().trim()) : 0.0);
            p.setStockProducto(Integer.parseInt(txtStockInicial.getText().trim()));
            p.setStockMinimo(Integer.parseInt(txtStockMinimo.getText().trim()));
            if (imagenesSeleccionadas.isEmpty()) {
                p.setImagen_producto(null);
            } else {
                p.setImagen_producto(String.join("|", imagenesSeleccionadas));
            }
            p.setDiasGarantia(valoresGarantia[cmbDiasGarantia.getSelectedIndex()]);
            p.setRequiereSerie(chkRequiereSerie.isSelected());
            p.setIncluyeImpuesto(chkIncluyeImpuesto.isSelected());
            // -----------------------------------------

            InventarioDAO dao = new InventarioDAO();
            boolean exito;
            if (productoAEditar == null) {
                exito = dao.registrarProducto(p);
            } else {
                exito = dao.actualizarProducto(p);
            }

            if (exito) {
                JOptionPane.showMessageDialog(this, "¡Producto " + ((productoAEditar == null)? "guardado" : "actualizado") + " exitosamente!", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                if (productoAEditar == null) limpiarFormulario();
            } else {
                // AQUÍ FALTABA ESTE BLOQUE
                JOptionPane.showMessageDialog(this, "Error al guardar el producto en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Verifique que los valores numéricos sean válidos.", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiarFormulario() {
        txtCodigoBarras.setText(""); txtNombre.setText(""); txtPrecioCompra.setText("");
        txtPrecioVenta.setText(""); txtPrecioMayorista.setText(""); chkPrecioMayorista.setSelected(false);
        txtPrecioMayorista.setEnabled(false); txtStockInicial.setText(""); txtStockMinimo.setText("0");
        imagenesSeleccionadas.clear();
        indiceImagenActual = -1;
        actualizarVistaPreviaImagen();
        cmbCategoria.setSelectedIndex(0); cmbProveedor.setSelectedIndex(0); cmbUbicacion.setSelectedIndex(0);
        codigosEnRam = new InventarioDAO().obtenerCodigosEnRam();
        cmbDiasGarantia.setSelectedIndex(0); chkRequiereSerie.setSelected(false); chkIncluyeImpuesto.setSelected(false);
    }
    
    private void cargarDatosEdicion() {
        txtCodigoBarras.setText(productoAEditar.getCodigoBarras());
        txtNombre.setText(productoAEditar.getNombreProducto());
        txtPrecioCompra.setText(String.valueOf(productoAEditar.getPrecioCompra()));
        txtPrecioVenta.setText(String.valueOf(productoAEditar.getPrecioVenta()));
        
        if(productoAEditar.getPrecioMayorista() > 0) {
            chkPrecioMayorista.setSelected(true);
            txtPrecioMayorista.setEnabled(true);
            txtPrecioMayorista.setText(String.valueOf(productoAEditar.getPrecioMayorista()));
        }
        
        txtStockInicial.setText(String.valueOf(productoAEditar.getStockProducto()));
        txtStockInicial.setEnabled(false);
        txtStockInicial.setToolTipText("El stock solo puede modificarse a través del Kardex.");
        
        txtStockMinimo.setText(String.valueOf(productoAEditar.getStockMinimo()));
        
        seleccionarComboPorId(cmbCategoria, productoAEditar.getIdCategoria());
        seleccionarComboPorId(cmbProveedor, productoAEditar.getIdProveedor());
        seleccionarComboPorId(cmbUbicacion, productoAEditar.getIdUbicacion());
        
        chkRequiereSerie.setSelected(productoAEditar.isRequiereSerie());
        chkIncluyeImpuesto.setSelected(productoAEditar.isIncluyeImpuesto());
        int dias = productoAEditar.getDiasGarantia();
        int index = 0;
        for (int i = 0; i < valoresGarantia.length; i++) { if (valoresGarantia[i] == dias) index = i; }
        cmbDiasGarantia.setSelectedIndex(index);
        
        imagenesSeleccionadas.clear();
        if(productoAEditar.getImagen_producto() != null && !productoAEditar.getImagen_producto().trim().isEmpty()) {
            String rawImg = productoAEditar.getImagen_producto();
            if (rawImg.contains("|")) {
                String[] parts = rawImg.split("\\|");
                for (String part : parts) {
                    if (!part.trim().isEmpty()) {
                        imagenesSeleccionadas.add(part);
                    }
                }
            } else {
                imagenesSeleccionadas.add(rawImg);
            }
            indiceImagenActual = 0;
        } else {
            indiceImagenActual = -1;
        }
        actualizarVistaPreviaImagen();
    }
    
    private void seleccionarComboPorId(JComboBox<ItemCatalogo> combo, int id) {
        for(int i = 0; i < combo.getItemCount(); i++) {
            ItemCatalogo item = combo.getItemAt(i);
            if(item.id == id) { combo.setSelectedIndex(i); break; }
        }
    }

    // =========================================================
    // VENTANAS PARA CREAR/EDITAR CATÁLOGOS (FUNCIONALES)
    // =========================================================

    private void abrirDialogoMantenimiento(String tipoCatalogo) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) ventanaPadre, "Nuevo/a " + tipoCatalogo, true);
        dialog.setSize(400, 450); dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout()); dialog.getContentPane().setBackground(new Color(240, 242, 245)); // Gris Nube

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.CENTER)); pnlTop.setBackground(new Color(240, 242, 245)); // Gris Nube
        JLabel lblTop = new JLabel("Registrar " + tipoCatalogo); lblTop.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblTop.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        pnlTop.add(lblTop); dialog.add(pnlTop, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout()); pnlForm.setBackground(new Color(255, 255, 255)); // Blanco Puro
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10, 5, 10, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0; gbc.weightx = 1.0;

        JTextField txtNombreCat = crearInputOscuro();
        agregarFilaDialog(pnlForm, gbc, 0, "Nombre:", txtNombreCat);
        
        JTextField txtDesc = null, txtGarantia = null, txtEncargado = null, txtTel = null, txtDir = null, txtRepuestos = null;

        if (tipoCatalogo.equals("Categoría")) {
            txtGarantia = crearInputOscuro();
            permitirSoloNumeros(txtGarantia, false); // Solo números para los días
            agregarFilaDialog(pnlForm, gbc, 2, "Días de Garantía:", txtGarantia);
        } else if (tipoCatalogo.equals("Proveedor")) {
            txtEncargado = crearInputOscuro(); txtTel = crearInputOscuro();
            txtDir = crearInputOscuro(); txtRepuestos = crearInputOscuro();
            permitirSoloNumeros(txtTel, false); // Solo números para teléfono
            agregarFilaDialog(pnlForm, gbc, 1, "Encargado:", txtEncargado);
            agregarFilaDialog(pnlForm, gbc, 2, "Teléfono:", txtTel);
            agregarFilaDialog(pnlForm, gbc, 3, "Dirección:", txtDir);
            agregarFilaDialog(pnlForm, gbc, 4, "Tipo Repuestos:", txtRepuestos);
        }

        gbc.gridy = 10; gbc.weighty = 1.0; pnlForm.add(new JLabel(""), gbc);
        dialog.add(pnlForm, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT)); pnlBotones.setBackground(new Color(240, 242, 245)); // Gris Nube
        JButton btnCancelar = new JButton("Cancelar"); btnCancelar.setBackground(new Color(140, 145, 150)); btnCancelar.setForeground(Color.WHITE); btnCancelar.addActionListener(e -> dialog.dispose());
        JButton btnGuardarCat = new JButton("Guardar"); btnGuardarCat.setBackground(new Color(39, 174, 96)); btnGuardarCat.setForeground(Color.WHITE); // Verde Menta
        
        // Variables finales para usar dentro del listener
        final JTextField fTxtDesc = txtDesc, fTxtGarantia = txtGarantia, fTxtEncargado = txtEncargado, fTxtTel = txtTel, fTxtDir = txtDir, fTxtRepuestos = txtRepuestos;
        
        btnGuardarCat.addActionListener(e -> {
            if (txtNombreCat.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(dialog, "El nombre es obligatorio."); return; }
            CatalogosDAO dao = new CatalogosDAO();
            boolean exito = false;
            
            if (tipoCatalogo.equals("Categoría")) {
                int garantias = fTxtGarantia.getText().trim().isEmpty() ? 0 : Integer.parseInt(fTxtGarantia.getText().trim());
                exito = dao.registrarCategoria(txtNombreCat.getText().trim(), fTxtDesc != null ? fTxtDesc.getText().trim() : "", garantias);
            } else if (tipoCatalogo.equals("Proveedor")) {
                exito = dao.registrarProveedor(txtNombreCat.getText().trim(), fTxtEncargado.getText().trim(), fTxtTel.getText().trim(), fTxtDir.getText().trim(), fTxtRepuestos.getText().trim());
            } else {
                exito = dao.registrarUbicacion(txtNombreCat.getText().trim());
            }

            if (exito) {
                JOptionPane.showMessageDialog(dialog, tipoCatalogo + " registrada exitosamente.");
                cargarDatosCombos(); // Recarga los combos inmediatamente
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al guardar en la base de datos.");
            }
        });
        
        pnlBotones.add(btnCancelar); pnlBotones.add(btnGuardarCat);
        dialog.add(pnlBotones, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    // =========================================================
    // UTILIDADES RESTANTES (Sin cambios estructurales)
    // =========================================================
    private JTextField crearInputOscuro() {
        JTextField txt = new JTextField(20); txt.setFont(new Font("Segoe UI", Font.PLAIN, 14)); txt.setBackground(new Color(250, 250, 250)); // Fondo claro input
        txt.setForeground(new Color(45, 45, 45)); // Texto oscuro
        txt.setCaretColor(new Color(45, 45, 45)); txt.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        return txt;
    }

    private void agregarFilaDialog(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JTextField campo) {
        gbc.gridy = fila * 2; JLabel lbl = new JLabel(etiqueta); lbl.setForeground(new Color(45, 45, 45)); lbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); // Texto Oscuro
        panel.add(lbl, gbc); gbc.gridy = (fila * 2) + 1; panel.add(campo, gbc);
    }

    private void configurarValidacionEnVivo() {
        txtCodigoBarras.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validarCodigo(); }
            @Override public void removeUpdate(DocumentEvent e) { validarCodigo(); }
            @Override public void changedUpdate(DocumentEvent e) { validarCodigo(); }
        });
    }

    private void validarCodigo() {
        String codigoStr = txtCodigoBarras.getText().trim();
        if (codigoStr.isEmpty()) { restaurarEstiloCodigo(); btnGuardar.setEnabled(true); return; }
        if (codigosEnRam.contains(codigoStr)) {
            txtCodigoBarras.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(227, 0, 15), 2), BorderFactory.createEmptyBorder(4, 7, 4, 7))); // Rojo Logo
            lblErrorCodigo.setText("Este código ya está registrado en el inventario."); btnGuardar.setEnabled(false); 
        } else { restaurarEstiloCodigo(); btnGuardar.setEnabled(true); }
    }

    private void restaurarEstiloCodigo() {
        txtCodigoBarras.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(5, 8, 5, 8))); lblErrorCodigo.setText(" ");
    }

    private void agregarFila(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComponent campo) {
        gbc.gridy = fila; gbc.gridx = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.anchor = GridBagConstraints.EAST;
        JLabel label = new JLabel(etiqueta); label.setForeground(new Color(45, 45, 45)); label.setFont(new Font("Segoe UI", Font.BOLD, 13)); panel.add(label, gbc); // Gris Oscuro
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        if (campo instanceof JTextField) {
            campo.setFont(new Font("Segoe UI", Font.PLAIN, 14)); campo.setBackground(new Color(250, 250, 250)); campo.setForeground(new Color(45, 45, 45)); // Fondo claro, texto oscuro
            ((JTextField)campo).setCaretColor(new Color(45, 45, 45)); campo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        } panel.add(campo, gbc);
    }

    private void agregarFilaCorta(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JComponent campo) {
        gbc.gridy = fila; gbc.gridx = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        JLabel label = new JLabel(etiqueta); label.setForeground(new Color(45, 45, 45)); label.setFont(new Font("Segoe UI", Font.BOLD, 13)); panel.add(label, gbc); // Gris Oscuro
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        if (campo instanceof JTextField) {
            campo.setPreferredSize(new Dimension(200, 32)); campo.setFont(new Font("Segoe UI", Font.PLAIN, 14)); campo.setBackground(new Color(250, 250, 250)); // Fondo claro input
            campo.setForeground(new Color(45, 45, 45)); ((JTextField)campo).setCaretColor(new Color(45, 45, 45)); campo.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(5, 8, 5, 8))); // Texto oscuro
        } panel.add(campo, gbc); gbc.fill = GridBagConstraints.HORIZONTAL; 
    }

    private JPanel crearPanelCatalogo(JComboBox<ItemCatalogo> combo, String tipoCatalogo) {
        JPanel pnl = new JPanel(new BorderLayout(5, 0)); 
        pnl.setOpaque(false); 
        combo.setFont(new Font("Segoe UI", Font.PLAIN, 14)); 
        pnl.add(combo, BorderLayout.CENTER);
        
        JPanel pnlBotones = new JPanel(new GridLayout(1, 2, 5, 0)); 
        pnlBotones.setOpaque(false);
        
        JButton btnNuevo = new JButton("+"); 
        btnNuevo.setBackground(new Color(39, 174, 96)); // Verde Menta
        btnNuevo.setForeground(Color.WHITE); 
        btnNuevo.setFocusPainted(false); 
        btnNuevo.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNuevo.addActionListener(e -> abrirDialogoMantenimiento(tipoCatalogo));
        
        JButton btnEditar = new JButton("Editar"); 
        btnEditar.setFont(new Font("Segoe UI", Font.BOLD, 10));
        btnEditar.setBackground(new Color(140, 145, 150)); // Gris suave en vez del azul para editar catálogos
        btnEditar.setForeground(Color.WHITE); 
        btnEditar.setFocusPainted(false); 
        btnEditar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEditar.addActionListener(e -> {
            ItemCatalogo item = (ItemCatalogo) combo.getSelectedItem();
            if (item == null || item.id == 0) {
                JOptionPane.showMessageDialog(this, "Seleccione un(a) " + tipoCatalogo + " válido de la lista para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            } else {
                abrirDialogoEdicion(tipoCatalogo, item);
            }
        });
        
        // --- ESTO ERA LO QUE FALTABA ---
        pnlBotones.add(btnNuevo); 
        pnlBotones.add(btnEditar); 
        pnl.add(pnlBotones, BorderLayout.EAST); 
        return pnl; // Devolvemos el panel construido en lugar de null
    }

    private void seleccionarImagen() {
        if (imagenesSeleccionadas.size() >= 7) {
            JOptionPane.showMessageDialog(this, "Límite de 7 imágenes alcanzado.", "Límite Excedido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Imágenes (JPG, PNG, GIF, JPEG)", "jpg", "png", "gif", "jpeg");
        fileChooser.setFileFilter(filter);

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                String base64Image = utilidades.ImagenHelper.comprimirYConvertirABase64(selectedFile);
                if (base64Image != null) {
                    imagenesSeleccionadas.add(base64Image);
                    indiceImagenActual = imagenesSeleccionadas.size() - 1;
                    actualizarVistaPreviaImagen();
                } else {
                    JOptionPane.showMessageDialog(this, "Error al procesar la imagen.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al leer el archivo de imagen.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cargarImagenDesdeURL() {
        if (imagenesSeleccionadas.size() >= 7) {
            JOptionPane.showMessageDialog(this, "Límite de 7 imágenes alcanzado.", "Límite Excedido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        String urlStr = JOptionPane.showInputDialog(this, "Ingrese la URL de la imagen:", "Cargar desde la Web", JOptionPane.QUESTION_MESSAGE);
        if (urlStr == null || urlStr.trim().isEmpty()) {
            return;
        }
        
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                java.net.URL url = new java.net.URL(urlStr.trim());
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(url);
                if (img == null) throw new Exception("La URL no contiene una imagen válida.");
                return utilidades.ImagenHelper.convertirImagenABase64(img);
            }
            @Override
            protected void done() {
                setCursor(Cursor.getDefaultCursor());
                try {
                    String b64 = get();
                    imagenesSeleccionadas.add(b64);
                    indiceImagenActual = imagenesSeleccionadas.size() - 1;
                    actualizarVistaPreviaImagen();
                    JOptionPane.showMessageDialog(PanelCrearProducto.this, "Imagen descargada y adjuntada exitosamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(PanelCrearProducto.this, "⚠️ No se pudo descargar la imagen.\nVerifique que la URL sea válida o que tenga conexión a internet (No WiFi).", "Error de Conexión", JOptionPane.WARNING_MESSAGE);
                }
            }
        };
        worker.execute();
    }

    private void actualizarVistaPreviaImagen() {
        if (imagenesSeleccionadas.isEmpty() || indiceImagenActual < 0 || indiceImagenActual >= imagenesSeleccionadas.size()) {
            lblVistaPreviaImagen.setIcon(null);
            lblVistaPreviaImagen.setText("Sin Imagen");
            lblNavegacionImg.setText("0 / 0");
            btnAnteriorImg.setEnabled(false);
            btnSiguienteImg.setEnabled(false);
            btnEliminarImg.setEnabled(false);
            imagenSeleccionada = null;
        } else {
            String imgBase64 = imagenesSeleccionadas.get(indiceImagenActual);
            imagenSeleccionada = imgBase64;
            ImageIcon icon = utilidades.ImagenHelper.obtenerIcono(imgBase64, 150, 150);
            if (icon != null) {
                lblVistaPreviaImagen.setText("");
                lblVistaPreviaImagen.setIcon(icon);
            } else {
                lblVistaPreviaImagen.setText("Error al cargar");
                lblVistaPreviaImagen.setIcon(null);
            }
            lblNavegacionImg.setText((indiceImagenActual + 1) + " / " + imagenesSeleccionadas.size());
            btnAnteriorImg.setEnabled(indiceImagenActual > 0);
            btnSiguienteImg.setEnabled(indiceImagenActual < imagenesSeleccionadas.size() - 1);
            btnEliminarImg.setEnabled(true);
        }
    }

    private class ItemCatalogo {
        int id; String nombre;
        public ItemCatalogo(int id, String nombre) { this.id = id; this.nombre = nombre; }
        @Override public String toString() { return nombre; }
    }
    
    private void abrirDialogoEdicion(String tipoCatalogo, ItemCatalogo item) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) ventanaPadre, "Editar " + tipoCatalogo, true);
        dialog.setSize(400, 450); dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout()); dialog.getContentPane().setBackground(new Color(240, 242, 245)); // Gris Nube

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.CENTER)); pnlTop.setBackground(new Color(240, 242, 245)); // Gris Nube
        JLabel lblTop = new JLabel("Editar " + tipoCatalogo); lblTop.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblTop.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        pnlTop.add(lblTop); dialog.add(pnlTop, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout()); pnlForm.setBackground(new Color(255, 255, 255)); // Blanco Puro
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10, 5, 10, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0; gbc.weightx = 1.0;

        JTextField txtNombreCat = crearInputOscuro();
        txtNombreCat.setText(item.nombre); // Precargar nombre
        agregarFilaDialog(pnlForm, gbc, 0, "Nombre:", txtNombreCat);
        
        JTextField txtDesc = null, txtGarantia = null, txtEncargado = null, txtTel = null, txtDir = null, txtRepuestos = null;
        CatalogosDAO dao = new CatalogosDAO();

        if (tipoCatalogo.equals("Categoría")) {
            String[] datos = dao.obtenerDatosCategoria(item.id);
            txtDesc = crearInputOscuro(); txtDesc.setText(datos[0]);
            txtGarantia = crearInputOscuro(); txtGarantia.setText(datos[1]);
            permitirSoloNumeros(txtGarantia, false);
            agregarFilaDialog(pnlForm, gbc, 1, "Descripción:", txtDesc);
            agregarFilaDialog(pnlForm, gbc, 2, "Días de Garantía:", txtGarantia);
        } else if (tipoCatalogo.equals("Proveedor")) {
            String[] datos = dao.obtenerDatosProveedor(item.id);
            txtEncargado = crearInputOscuro(); txtEncargado.setText(datos[0]);
            txtTel = crearInputOscuro(); txtTel.setText(datos[1]);
            txtDir = crearInputOscuro(); txtDir.setText(datos[2]);
            txtRepuestos = crearInputOscuro(); txtRepuestos.setText(datos[3]);
            permitirSoloNumeros(txtTel, false);
            agregarFilaDialog(pnlForm, gbc, 1, "Encargado:", txtEncargado);
            agregarFilaDialog(pnlForm, gbc, 2, "Teléfono:", txtTel);
            agregarFilaDialog(pnlForm, gbc, 3, "Dirección:", txtDir);
            agregarFilaDialog(pnlForm, gbc, 4, "Tipo Repuestos:", txtRepuestos);
        }

        gbc.gridy = 10; gbc.weighty = 1.0; pnlForm.add(new JLabel(""), gbc);
        dialog.add(pnlForm, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT)); pnlBotones.setBackground(new Color(240, 242, 245)); // Gris Nube
        JButton btnCancelar = new JButton("Cancelar"); btnCancelar.setBackground(new Color(140, 145, 150)); btnCancelar.setForeground(Color.WHITE); btnCancelar.addActionListener(e -> dialog.dispose()); // Gris Suave
        JButton btnGuardarCat = new JButton("Actualizar"); btnGuardarCat.setBackground(new Color(39, 174, 96)); btnGuardarCat.setForeground(Color.WHITE); // Verde Menta
        
        final JTextField fTxtDesc = txtDesc, fTxtGarantia = txtGarantia, fTxtEncargado = txtEncargado, fTxtTel = txtTel, fTxtDir = txtDir, fTxtRepuestos = txtRepuestos;
        
        btnGuardarCat.addActionListener(e -> {
            if (txtNombreCat.getText().trim().isEmpty()) { JOptionPane.showMessageDialog(dialog, "El nombre es obligatorio."); return; }
            boolean exito = false;
            
            if (tipoCatalogo.equals("Categoría")) {
                int garantias = fTxtGarantia.getText().trim().isEmpty() ? 0 : Integer.parseInt(fTxtGarantia.getText().trim());
                exito = dao.actualizarCategoria(item.id, txtNombreCat.getText().trim(), fTxtDesc != null ? fTxtDesc.getText().trim() : "", garantias);
            } else if (tipoCatalogo.equals("Proveedor")) {
                exito = dao.actualizarProveedor(item.id, txtNombreCat.getText().trim(), fTxtEncargado.getText().trim(), fTxtTel.getText().trim(), fTxtDir.getText().trim(), fTxtRepuestos.getText().trim());
            } else {
                exito = dao.actualizarUbicacion(item.id, txtNombreCat.getText().trim());
            }

            if (exito) {
                JOptionPane.showMessageDialog(dialog, tipoCatalogo + " actualizada exitosamente.");
                cargarDatosCombos(); // Recarga los combos inmediatamente
                dialog.dispose();
            } else {
                JOptionPane.showMessageDialog(dialog, "Error al actualizar en la base de datos.");
            }
        });
        
        pnlBotones.add(btnCancelar); pnlBotones.add(btnGuardarCat);
        dialog.add(pnlBotones, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }
     @SuppressWarnings("unchecked")
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

    private class IconoCamara implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(Color.WHITE);
            g2.fillRoundRect(x + 2, y + 6, 16, 10, 4, 4);
            g2.fillRoundRect(x + 6, y + 3, 8, 4, 2, 2);
            g2.setColor(new Color(227, 0, 15));
            g2.fillOval(x + 6, y + 7, 8, 8);
            g2.setColor(Color.WHITE);
            g2.drawOval(x + 6, y + 7, 8, 8);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
    }

    private class IconoCarpeta implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(100, 100, 100));
            g2.fillRoundRect(x + 2, y + 4, 8, 4, 2, 2);
            g2.fillRoundRect(x + 2, y + 6, 16, 10, 2, 2);
            g2.setColor(new Color(130, 130, 130));
            g2.fillRoundRect(x + 2, y + 8, 16, 8, 2, 2);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
    }

    private class IconoNube implements Icon {
        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(new Color(13, 110, 253)); // Azul
            g2.fillOval(x + 2, y + 8, 6, 6);
            g2.fillOval(x + 5, y + 4, 8, 8);
            g2.fillOval(x + 10, y + 6, 7, 7);
            g2.fillRoundRect(x + 3, y + 9, 13, 5, 5, 5);
            g2.dispose();
        }
        @Override public int getIconWidth() { return 20; }
        @Override public int getIconHeight() { return 20; }
    }
}
