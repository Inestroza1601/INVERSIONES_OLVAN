package gui;

import dao.InventarioDAO;
import dao.CatalogosDAO;
import modelo.Producto;
import javax.swing.*;
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
    private JLabel lblErrorPrecio;
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
    @SuppressWarnings("unused")
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
    private JCheckBox chkImpuesto15;
    private JCheckBox chkExento;
    
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
            codigosEnRam.remove(productoAEditar.getCodigoBarras()); // Evita que su propio c\u00F3digo marque error
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

        String textoTitulo = (productoAEditar == null) ? "Registrar Nuevo Producto" : "Edici\u00F3n de Producto";
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
        txtCodigoBarras.putClientProperty("JTextField.placeholderText", "Dejar vac\u00EDo para usar ID autogenerado");
        
        lblErrorCodigo = new JLabel(" ");
        lblErrorCodigo.setForeground(new Color(227, 0, 15)); // Rojo Logo
        lblErrorCodigo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
        txtNombre = new JTextField(20);
        
        cmbCategoria = new JComboBox<>();
        cmbProveedor = new JComboBox<>();
        cmbUbicacion = new JComboBox<>();
        
        txtPrecioCompra = new JTextField(10);
        txtPrecioVenta = new JTextField(10);
        
        lblErrorPrecio = new JLabel(" ");
        lblErrorPrecio.setForeground(new Color(227, 0, 15)); // Rojo Logo
        lblErrorPrecio.setFont(new Font("Segoe UI", Font.BOLD, 12));
        
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
        agregarFilaCorta(pnlForm, gbc, 0, "C\u00F3digo de Barras:", txtCodigoBarras);
        gbc.gridy = 1; gbc.gridx = 1; gbc.insets = new Insets(0, 10, 10, 10);
        pnlForm.add(lblErrorCodigo, gbc);
        gbc.insets = new Insets(10, 10, 10, 10); 
        
        agregarFila(pnlForm, gbc, 2, "Nombre del Producto:", txtNombre);
        agregarFila(pnlForm, gbc, 3, "Categor\u00EDa:", crearPanelCatalogo(cmbCategoria, "Categor\u00EDa"));
        agregarFila(pnlForm, gbc, 4, "Proveedor:", crearPanelCatalogo(cmbProveedor, "Proveedor"));
        agregarFila(pnlForm, gbc, 5, "Ubicaci\u00F3n:", crearPanelCatalogo(cmbUbicacion, "Ubicaci\u00F3n"));

        cmbDiasGarantia = new JComboBox<>(new String[]{"Sin garant\u00EDa", "3 d\u00EDas", "7 d\u00EDas", "15 d\u00EDas", "30 d\u00EDas", "60 d\u00EDas", "90 d\u00EDas", "1 a\u00F1o"});
        cmbDiasGarantia.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        agregarFilaCorta(pnlForm, gbc, 6, "Garant\u00EDa (D\u00EDas):", cmbDiasGarantia);

        chkRequiereSerie = new JCheckBox("Solicitar Identificador al facturar");
        chkRequiereSerie.setBackground(new Color(255, 255, 255)); // Blanco Puro
        chkRequiereSerie.setForeground(new Color(39, 174, 96)); // Verde Menta en vez del azul original
        chkRequiereSerie.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkRequiereSerie.setFocusPainted(false);
        
        chkImpuesto15 = new JCheckBox("15% (ISV)");
        chkImpuesto15.setBackground(new Color(255, 255, 255));
        chkImpuesto15.setForeground(new Color(39, 174, 96));
        chkImpuesto15.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkImpuesto15.setFocusPainted(false);
        
        chkExento = new JCheckBox("Exento");
        chkExento.setBackground(new Color(255, 255, 255));
        chkExento.setForeground(new Color(39, 174, 96));
        chkExento.setFont(new Font("Segoe UI", Font.BOLD, 13));
        chkExento.setFocusPainted(false);
        
        chkImpuesto15.addActionListener(e -> {
            if (chkImpuesto15.isSelected()) chkExento.setSelected(false);
        });
        
        chkExento.addActionListener(e -> {
            if (chkExento.isSelected()) chkImpuesto15.setSelected(false);
        });
        
        JPanel pnlChecks = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 15, 0));
        pnlChecks.setOpaque(false);
        pnlChecks.add(chkRequiereSerie);
        pnlChecks.add(chkImpuesto15);
        pnlChecks.add(chkExento);

        gbc.gridy = 7; gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        pnlForm.add(pnlChecks, gbc);
        agregarFilaCorta(pnlForm, gbc, 8, "Precio Compra (L):", txtPrecioCompra);
        agregarFilaCorta(pnlForm, gbc, 9, "Precio Venta (L):", txtPrecioVenta);
        
        gbc.gridy = 10; gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(0, 10, 10, 10);
        pnlForm.add(lblErrorPrecio, gbc);
        gbc.insets = new Insets(10, 10, 10, 10);
        
        gbc.gridy = 11; gbc.gridx = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.EAST;
        pnlForm.add(chkPrecioMayorista, gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; gbc.anchor = GridBagConstraints.WEST;
        txtPrecioMayorista.setPreferredSize(new Dimension(150, 32)); 
        txtPrecioMayorista.setBackground(new Color(250, 250, 250)); // Fondo claro input
        txtPrecioMayorista.setForeground(new Color(45, 45, 45)); // Texto Oscuro
        txtPrecioMayorista.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(5, 8, 5, 8)));
        pnlForm.add(txtPrecioMayorista, gbc);
        agregarFilaCorta(pnlForm, gbc, 12, "Stock Inicial:", txtStockInicial);
        agregarFilaCorta(pnlForm, gbc, 13, "Stock M\u00EDnimo:", txtStockMinimo);

        gbc.gridy = 14; gbc.weighty = 1.0;
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

        JLabel lblTituloImg = new JLabel("Fotograf\u00EDas (M\u00E1x 4)");
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
                        new DialogoVisorImagen((Frame) window, "Visor de Fotograf\u00EDa", imagenesSeleccionadas, indiceImagenActual).setVisible(true);
                    } else if (window instanceof JDialog) {
                        new DialogoVisorImagen((JDialog) window, "Visor de Fotograf\u00EDa", imagenesSeleccionadas, indiceImagenActual).setVisible(true);
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
            if (imagenesSeleccionadas.size() >= 4) {
                utilidades.Mensajes.showMessageDialog(this, "L\u00EDmite de 4 im\u00E1genes alcanzado.", "L\u00EDmite Excedido", JOptionPane.WARNING_MESSAGE);
                return;
            }
            Window window = SwingUtilities.getWindowAncestor(PanelCrearProducto.this);
            Frame frame = (window instanceof Frame) ? (Frame) window : null;
            new DialogoEscanearQR(frame, true, 4, b64Str -> {
                if (b64Str != null && !b64Str.isEmpty()) {
                    String[] parts = b64Str.split("\\|");
                    for (String p : parts) {
                        if (imagenesSeleccionadas.size() < 4) {
                            imagenesSeleccionadas.add(p);
                        }
                    }
                    indiceImagenActual = imagenesSeleccionadas.size() - 1;
                    actualizarVistaPreviaImagen();
                    utilidades.Mensajes.showMessageDialog(this, "Foto(s) capturada(s) exitosamente.", "\u00C9xito", JOptionPane.INFORMATION_MESSAGE);
                }
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
    // L\u00C3\u201CGICA: RESTRICCIONES DE TECLADO
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
    // L\u00C3\u201CGICA DE GUARDADO EN BASE DE DATOS
    // =========================================================
    private void cargarDatosCombos() {
        CatalogosDAO dao = new CatalogosDAO();
        
        cmbCategoria.removeAllItems();
        cmbCategoria.addItem(new ItemCatalogo(0, "Seleccione Categor\u00EDa..."));
        for (Map.Entry<Integer, String> entry : dao.listarCategorias().entrySet()) {
            cmbCategoria.addItem(new ItemCatalogo(entry.getKey(), entry.getValue()));
        }
        
        cmbProveedor.removeAllItems();
        cmbProveedor.addItem(new ItemCatalogo(0, "Seleccione Proveedor..."));
        for (Map.Entry<Integer, String> entry : dao.listarProveedores().entrySet()) {
            cmbProveedor.addItem(new ItemCatalogo(entry.getKey(), entry.getValue()));
        }
        
        cmbUbicacion.removeAllItems();
        cmbUbicacion.addItem(new ItemCatalogo(0, "Seleccione Ubicaci\u00F3n..."));
        for (Map.Entry<Integer, String> entry : dao.listarUbicaciones().entrySet()) {
            cmbUbicacion.addItem(new ItemCatalogo(entry.getKey(), entry.getValue()));
        }
    }
    
    private void guardarProducto() {
        if (txtNombre.getText().trim().isEmpty() || txtPrecioCompra.getText().trim().isEmpty() || txtPrecioVenta.getText().trim().isEmpty() || txtStockInicial.getText().trim().isEmpty()) {
            utilidades.Mensajes.showMessageDialog(this, "Complete los campos obligatorios (Nombre, Precios y Stock).", "Error", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        ItemCatalogo cat = (ItemCatalogo) cmbCategoria.getSelectedItem();
        ItemCatalogo prov = (ItemCatalogo) cmbProveedor.getSelectedItem();
        ItemCatalogo ubi = (ItemCatalogo) cmbUbicacion.getSelectedItem();
        
        if (cat == null || cat.id == 0 || prov == null || prov.id == 0 || ubi == null || ubi.id == 0) {
            utilidades.Mensajes.showMessageDialog(this, "Debe seleccionar Categor\u00EDa, Proveedor y Ubicaci\u00F3n.", "Error", JOptionPane.WARNING_MESSAGE);
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
            p.setIncluyeImpuesto(chkExento.isSelected() ? 2 : (chkImpuesto15.isSelected() ? 1 : 0));
            // -----------------------------------------

            InventarioDAO dao = new InventarioDAO();
            boolean exito;
            if (productoAEditar == null) {
                exito = dao.registrarProducto(p);
            } else {
                exito = dao.actualizarProducto(p);
            }

            if (exito) {
                utilidades.Mensajes.showMessageDialog(this, "\u00A1Producto " + ((productoAEditar == null)? "guardado" : "actualizado") + " exitosamente!", "\u00C9xito", JOptionPane.INFORMATION_MESSAGE);
                if (productoAEditar == null) limpiarFormulario();
            } else {
                // AQU\u00CD FALTABA ESTE BLOQUE
                utilidades.Mensajes.showMessageDialog(this, "Error al guardar el producto en la base de datos.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            utilidades.Mensajes.showMessageDialog(this, "Verifique que los valores num\u00E9ricos sean v\u00E1lidos.", "Error", JOptionPane.ERROR_MESSAGE);
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
        cmbDiasGarantia.setSelectedIndex(0); chkRequiereSerie.setSelected(false); chkImpuesto15.setSelected(false); chkExento.setSelected(false);
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
        txtStockInicial.setToolTipText("El stock solo puede modificarse a trav\u00E9s del Kardex.");
        
        txtStockMinimo.setText(String.valueOf(productoAEditar.getStockMinimo()));
        
        seleccionarComboPorId(cmbCategoria, productoAEditar.getIdCategoria());
        seleccionarComboPorId(cmbProveedor, productoAEditar.getIdProveedor());
        seleccionarComboPorId(cmbUbicacion, productoAEditar.getIdUbicacion());
        
        chkRequiereSerie.setSelected(productoAEditar.isRequiereSerie());
        int tipoImpuesto = productoAEditar.getIncluyeImpuesto();
        chkImpuesto15.setSelected(tipoImpuesto == 1);
        chkExento.setSelected(tipoImpuesto == 2);
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
    // VENTANAS PARA CREAR/EDITAR CAT\u00C1LOGOS (FUNCIONALES)
    // =========================================================

    private void abrirDialogoMantenimiento(String tipoCatalogo) {
        Window ventanaPadre = SwingUtilities.getWindowAncestor(this);
        JDialog dialog = new JDialog((Frame) ventanaPadre, "Nuevo/a " + tipoCatalogo, true);
        if (tipoCatalogo.equals("Proveedor")) {
            dialog.setSize(550, 300);
        } else {
            dialog.setSize(400, 250); // Categoría / Ubicación
        }
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout()); dialog.getContentPane().setBackground(new Color(240, 242, 245)); // Gris Nube

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.CENTER)); pnlTop.setBackground(new Color(240, 242, 245)); // Gris Nube
        JLabel lblTop = new JLabel("Registrar " + tipoCatalogo); lblTop.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblTop.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        pnlTop.add(lblTop); dialog.add(pnlTop, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout()); pnlForm.setBackground(new Color(255, 255, 255)); // Blanco Puro
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10, 5, 10, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0; gbc.weightx = 1.0;

        JTextField txtNombreCat = crearInputOscuro();
        agregarFilaDialog(pnlForm, gbc, 0, "Nombre:", txtNombreCat);
        
        JTextField txtDesc = null, txtEncargado = null, txtTel = null, txtDir = null, txtRepuestos = null;

        if (tipoCatalogo.equals("Categoría")) {
            // Ya no se piden días de garantía aquí
        } else if (tipoCatalogo.equals("Proveedor")) {
            txtEncargado = crearInputOscuro(); txtTel = crearInputOscuro();
            txtDir = crearInputOscuro(); txtRepuestos = crearInputOscuro();
            permitirSoloNumeros(txtTel, false); // Solo números para teléfono
            gbc.gridx = 0; agregarFilaDialog(pnlForm, gbc, 1, "Encargado:", txtEncargado);
            gbc.gridx = 1; agregarFilaDialog(pnlForm, gbc, 1, "Teléfono:", txtTel);
            gbc.gridx = 0; agregarFilaDialog(pnlForm, gbc, 2, "Dirección:", txtDir);
            gbc.gridx = 1; agregarFilaDialog(pnlForm, gbc, 2, "Tipos de Productos:", txtRepuestos);
            gbc.gridx = 0; // reset
        }

        gbc.gridy = 10; gbc.weighty = 1.0; pnlForm.add(new JLabel(""), gbc);
        dialog.add(pnlForm, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT)); pnlBotones.setBackground(new Color(240, 242, 245)); // Gris Nube
        JButton btnCancelar = new JButton("Cancelar"); btnCancelar.setBackground(new Color(140, 145, 150)); btnCancelar.setForeground(Color.WHITE); btnCancelar.addActionListener(e -> dialog.dispose());
        JButton btnGuardarCat = new JButton("Guardar"); btnGuardarCat.setBackground(new Color(39, 174, 96)); btnGuardarCat.setForeground(Color.WHITE); // Verde Menta
        
        // Variables finales para usar dentro del listener
        final JTextField fTxtDesc = txtDesc, fTxtEncargado = txtEncargado, fTxtTel = txtTel, fTxtDir = txtDir, fTxtRepuestos = txtRepuestos;
        
        btnGuardarCat.addActionListener(e -> {
            if (txtNombreCat.getText().trim().isEmpty()) { utilidades.Mensajes.showMessageDialog(dialog, "El nombre es obligatorio."); return; }
            CatalogosDAO dao = new CatalogosDAO();
            boolean exito = false;
            
            if (tipoCatalogo.equals("Categoría")) {
                exito = dao.registrarCategoria(txtNombreCat.getText().trim(), fTxtDesc != null ? fTxtDesc.getText().trim() : "", 0);
            } else if (tipoCatalogo.equals("Proveedor")) {
                exito = dao.registrarProveedor(txtNombreCat.getText().trim(), fTxtEncargado.getText().trim(), fTxtTel.getText().trim(), fTxtDir.getText().trim(), fTxtRepuestos.getText().trim());
            } else {
                exito = dao.registrarUbicacion(txtNombreCat.getText().trim());
            }

            if (exito) {
                utilidades.Mensajes.showMessageDialog(dialog, tipoCatalogo + " registrada exitosamente.");
                cargarDatosCombos(); // Recarga los combos inmediatamente
                dialog.dispose();
            } else {
                utilidades.Mensajes.showMessageDialog(dialog, "Error al guardar en la base de datos.");
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
        DocumentListener priceListener = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validarPrecios(); }
            @Override public void removeUpdate(DocumentEvent e) { validarPrecios(); }
            @Override public void changedUpdate(DocumentEvent e) { validarPrecios(); }
        };
        txtPrecioCompra.getDocument().addDocumentListener(priceListener);
        txtPrecioVenta.getDocument().addDocumentListener(priceListener);
    }

    private void validarPrecios() {
        try {
            String strCompra = txtPrecioCompra.getText().trim();
            String strVenta = txtPrecioVenta.getText().trim();
            if (strCompra.isEmpty() || strVenta.isEmpty()) {
                restaurarEstiloPrecio();
                btnGuardar.setEnabled(true);
                return;
            }
            double compra = Double.parseDouble(strCompra);
            double venta = Double.parseDouble(strVenta);
            if (venta < compra) {
                txtPrecioVenta.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(227, 0, 15), 2), BorderFactory.createEmptyBorder(4, 7, 4, 7))); // Rojo Logo
                lblErrorPrecio.setText("El precio de venta no puede ser menor al precio de compra.");
                btnGuardar.setEnabled(false);
            } else {
                restaurarEstiloPrecio();
                btnGuardar.setEnabled(true);
            }
        } catch (NumberFormatException e) {
            restaurarEstiloPrecio();
            btnGuardar.setEnabled(true);
        }
    }

    private void restaurarEstiloPrecio() {
        txtPrecioVenta.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(220, 222, 225)), BorderFactory.createEmptyBorder(5, 8, 5, 8))); 
        lblErrorPrecio.setText(" ");
    }

    private void validarCodigo() {
        String codigoStr = txtCodigoBarras.getText().trim();
        if (codigoStr.isEmpty()) { restaurarEstiloCodigo(); btnGuardar.setEnabled(true); return; }
        if (codigosEnRam.contains(codigoStr)) {
            txtCodigoBarras.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(new Color(227, 0, 15), 2), BorderFactory.createEmptyBorder(4, 7, 4, 7))); // Rojo Logo
            lblErrorCodigo.setText("Este c\u00F3digo ya est\u00E1 registrado en el inventario."); btnGuardar.setEnabled(false); 
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
        btnEditar.setBackground(new Color(140, 145, 150)); // Gris suave en vez del azul para editar cat\u00E1logos
        btnEditar.setForeground(Color.WHITE); 
        btnEditar.setFocusPainted(false); 
        btnEditar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEditar.addActionListener(e -> {
            ItemCatalogo item = (ItemCatalogo) combo.getSelectedItem();
            if (item == null || item.id == 0) {
                utilidades.Mensajes.showMessageDialog(this, "Seleccione un(a) " + tipoCatalogo + " v\u00E1lido de la lista para editar.", "Aviso", JOptionPane.WARNING_MESSAGE);
            } else {
                abrirDialogoEdicion(tipoCatalogo, item);
            }
        });
        
        JButton btnEliminar = new JButton("X"); 
        btnEliminar.setBackground(new Color(227, 0, 15)); // Rojo para eliminar
        btnEliminar.setForeground(Color.WHITE);
        btnEliminar.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEliminar.setFocusPainted(false);
        btnEliminar.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnEliminar.setPreferredSize(new Dimension(35, btnEditar.getPreferredSize().height));
        btnEliminar.addActionListener(e -> {
            ItemCatalogo item = (ItemCatalogo) combo.getSelectedItem();
            if (item == null || item.id == 0) {
                utilidades.Mensajes.showMessageDialog(this, "Seleccione un(a) " + tipoCatalogo + " v\u00E1lido de la lista para eliminar.", "Aviso", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            CatalogosDAO dao = new CatalogosDAO();
            dao.InventarioDAO invDao = new dao.InventarioDAO();
            
            String columna = "";
            if (tipoCatalogo.equals("Categor\u00EDa")) columna = "id_categoria";
            else if (tipoCatalogo.equals("Proveedor")) columna = "id_proveedor";
            else if (tipoCatalogo.equals("Ubicaci\u00F3n")) columna = "id_ubicacion";

            int asociados = dao.contarProductosAsociados(columna, item.id);
            if (asociados > 0) {
                String[] opciones = {"Reasignar a otro/a", "Eliminar productos", "Cancelar"};
                int seleccion = JOptionPane.showOptionDialog(this,
                    "Se encuentran " + asociados + " producto(s) asociado(s) a este/a " + tipoCatalogo + ".\n" +
                    "\u00BFDesea a\u00F1adirlos a uno nuevo o desea eliminarlos?",
                    "Productos Asociados",
                    JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, opciones, opciones[0]);

                if (seleccion == 0) { // Reasignar
                    java.util.List<ItemCatalogo> disponibles = new java.util.ArrayList<>();
                    for (int i = 0; i < combo.getItemCount(); i++) {
                        ItemCatalogo cItem = combo.getItemAt(i);
                        if (cItem.id != 0 && cItem.id != item.id) disponibles.add(cItem);
                    }
                    if (disponibles.isEmpty()) {
                        utilidades.Mensajes.showMessageDialog(this, "No hay otro/a " + tipoCatalogo + " disponible para reasignar. Crea uno nuevo primero.", "Aviso", JOptionPane.WARNING_MESSAGE);
                        return;
                    }
                    ItemCatalogo seleccionado = (ItemCatalogo) JOptionPane.showInputDialog(this,
                        "Seleccione el nuevo destino:", "Reasignar " + tipoCatalogo,
                        JOptionPane.QUESTION_MESSAGE, null, disponibles.toArray(), disponibles.get(0));
                    
                    if (seleccionado != null) {
                        boolean reasignado = invDao.reasignarProductos(columna, item.id, seleccionado.id);
                        if (!reasignado) {
                            utilidades.Mensajes.showMessageDialog(this, "Error al reasignar productos.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        return; // Cancel\u00F3 la reasignaci\u00F3n
                    }
                } else if (seleccion == 1) { // Eliminar productos
                    int confirm2 = JOptionPane.showConfirmDialog(this, "\u00BFEst\u00E1 COMPLETAMENTE SEGURO de eliminar los " + asociados + " productos asociados?", "Confirmaci\u00F3n cr\u00EDtica", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                    if (confirm2 == JOptionPane.YES_OPTION) {
                        boolean eliminados = invDao.eliminarProductosPorCatalogo(columna, item.id);
                        if (!eliminados) {
                            utilidades.Mensajes.showMessageDialog(this, "Error al eliminar productos asociados.", "Error", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                    } else {
                        return; // Cancel\u00F3
                    }
                } else {
                    return; // Cancel\u00F3
                }
            } else {
                int resp = JOptionPane.showConfirmDialog(this, "\u00BFEst\u00E1 seguro que desea eliminar este/a " + tipoCatalogo + "?", "Confirmar Eliminaci\u00F3n", JOptionPane.YES_NO_OPTION);
                if (resp != JOptionPane.YES_OPTION) return;
            }

            // Proceder a eliminar la categor\u00EDa
            boolean exito = false;
            if (tipoCatalogo.equals("Categor\u00EDa")) {
                exito = dao.eliminarCategoria(item.id);
            } else if (tipoCatalogo.equals("Proveedor")) {
                exito = dao.eliminarProveedor(item.id);
            } else if (tipoCatalogo.equals("Ubicaci\u00F3n")) {
                exito = dao.eliminarUbicacion(item.id);
            }
            
            if (exito) {
                utilidades.Mensajes.showMessageDialog(this, tipoCatalogo + " eliminado/a correctamente.", "\u00C9xito", JOptionPane.INFORMATION_MESSAGE);
                cargarDatosCombos();
            } else {
                utilidades.Mensajes.showMessageDialog(this, "Error al eliminar " + tipoCatalogo + ".", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        // --- ESTO ERA LO QUE FALTABA ---
        pnlBotones.add(btnNuevo); 
        pnlBotones.add(btnEditar); 
        pnlBotones.add(btnEliminar);
        pnl.add(pnlBotones, BorderLayout.EAST); 
        return pnl; // Devolvemos el panel construido en lugar de null
    }

    private void seleccionarImagen() {
        if (imagenesSeleccionadas.size() >= 4) {
            utilidades.Mensajes.showMessageDialog(this, "L\u00EDmite de 4 im\u00E1genes alcanzado.", "L\u00EDmite Excedido", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("Im\u00E1genes (JPG, PNG, GIF, JPEG)", "jpg", "png", "gif", "jpeg");
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
                    utilidades.Mensajes.showMessageDialog(this, "Error al procesar la imagen.", "Error", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                utilidades.Mensajes.showMessageDialog(this, "Error al leer el archivo de imagen.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void cargarImagenDesdeURL() {
        if (imagenesSeleccionadas.size() >= 4) {
            utilidades.Mensajes.showMessageDialog(this, "L\u00EDmite de 4 im\u00E1genes alcanzado.", "L\u00EDmite Excedido", JOptionPane.WARNING_MESSAGE);
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
                java.net.URL url = java.net.URI.create(urlStr.trim()).toURL();
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(url);
                if (img == null) throw new Exception("La URL no contiene una imagen v\u00E1lida.");
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
                    utilidades.Mensajes.showMessageDialog(PanelCrearProducto.this, "Imagen descargada y adjuntada exitosamente.", "\u00C9xito", JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    utilidades.Mensajes.showMessageDialog(PanelCrearProducto.this, "\u00E2\u0161\u00A0\u00EF\u00B8\uFFFD No se pudo descargar la imagen.\nVerifique que la URL sea v\u00E1lida o que tenga conexi\u00F3n a internet (No WiFi).", "Error de Conexi\u00F3n", JOptionPane.WARNING_MESSAGE);
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
        if (tipoCatalogo.equals("Proveedor")) {
            dialog.setSize(550, 300);
        } else {
            dialog.setSize(400, 250);
        }
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout()); dialog.getContentPane().setBackground(new Color(240, 242, 245)); // Gris Nube

        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.CENTER)); pnlTop.setBackground(new Color(240, 242, 245)); // Gris Nube
        JLabel lblTop = new JLabel("Editar " + tipoCatalogo); lblTop.setFont(new Font("Segoe UI", Font.BOLD, 18)); lblTop.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        pnlTop.add(lblTop); dialog.add(pnlTop, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout()); pnlForm.setBackground(new Color(255, 255, 255)); // Blanco Puro
        pnlForm.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints(); gbc.insets = new Insets(10, 5, 10, 5); gbc.fill = GridBagConstraints.HORIZONTAL; gbc.gridx = 0; gbc.weightx = 1.0;

        JTextField txtNombreCat = crearInputOscuro();
        txtNombreCat.setText(item.nombre); // Precargar nombre
        gbc.gridwidth = 2; // Ocupar ambas columnas
        agregarFilaDialog(pnlForm, gbc, 0, "Nombre:", txtNombreCat);
        gbc.gridwidth = 1; // Restaurar
        
        JTextField txtDesc = null, txtEncargado = null, txtTel = null, txtDir = null, txtRepuestos = null;
        CatalogosDAO dao = new CatalogosDAO();

        if (tipoCatalogo.equals("Categoría")) {
            String[] datos = dao.obtenerDatosCategoria(item.id);
            txtDesc = crearInputOscuro(); txtDesc.setText(datos[0]);
            agregarFilaDialog(pnlForm, gbc, 1, "Descripción:", txtDesc);
        } else if (tipoCatalogo.equals("Proveedor")) {
            String[] datos = dao.obtenerDatosProveedor(item.id);
            txtEncargado = crearInputOscuro(); txtEncargado.setText(datos[0]);
            txtTel = crearInputOscuro(); txtTel.setText(datos[1]);
            txtDir = crearInputOscuro(); txtDir.setText(datos[2]);
            txtRepuestos = crearInputOscuro(); txtRepuestos.setText(datos[3]);
            permitirSoloNumeros(txtTel, false);
            gbc.gridx = 0; agregarFilaDialog(pnlForm, gbc, 1, "Encargado:", txtEncargado);
            gbc.gridx = 1; agregarFilaDialog(pnlForm, gbc, 1, "Teléfono:", txtTel);
            gbc.gridx = 0; agregarFilaDialog(pnlForm, gbc, 2, "Dirección:", txtDir);
            gbc.gridx = 1; agregarFilaDialog(pnlForm, gbc, 2, "Tipos de Productos:", txtRepuestos);
            gbc.gridx = 0;
        }

        gbc.gridy = 10; gbc.weighty = 1.0; pnlForm.add(new JLabel(""), gbc);
        dialog.add(pnlForm, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT)); pnlBotones.setBackground(new Color(240, 242, 245)); // Gris Nube
        JButton btnCancelar = new JButton("Cancelar"); btnCancelar.setBackground(new Color(140, 145, 150)); btnCancelar.setForeground(Color.WHITE); btnCancelar.addActionListener(e -> dialog.dispose()); // Gris Suave
        JButton btnGuardarCat = new JButton("Actualizar"); btnGuardarCat.setBackground(new Color(39, 174, 96)); btnGuardarCat.setForeground(Color.WHITE); // Verde Menta
        
        final JTextField fTxtDesc = txtDesc, fTxtEncargado = txtEncargado, fTxtTel = txtTel, fTxtDir = txtDir, fTxtRepuestos = txtRepuestos;
        
        btnGuardarCat.addActionListener(e -> {
            if (txtNombreCat.getText().trim().isEmpty()) { utilidades.Mensajes.showMessageDialog(dialog, "El nombre es obligatorio."); return; }
            boolean exito = false;
            
            if (tipoCatalogo.equals("Categoría")) {
                exito = dao.actualizarCategoria(item.id, txtNombreCat.getText().trim(), fTxtDesc != null ? fTxtDesc.getText().trim() : "", 0);
            } else if (tipoCatalogo.equals("Proveedor")) {
                exito = dao.actualizarProveedor(item.id, txtNombreCat.getText().trim(), fTxtEncargado.getText().trim(), fTxtTel.getText().trim(), fTxtDir.getText().trim(), fTxtRepuestos.getText().trim());
            } else {
                exito = dao.actualizarUbicacion(item.id, txtNombreCat.getText().trim());
            }

            if (exito) {
                utilidades.Mensajes.showMessageDialog(dialog, tipoCatalogo + " actualizada exitosamente.");
                cargarDatosCombos(); // Recarga los combos inmediatamente
                dialog.dispose();
            } else {
                utilidades.Mensajes.showMessageDialog(dialog, "Error al actualizar en la base de datos.");
            }
        });
        
        pnlBotones.add(btnCancelar); pnlBotones.add(btnGuardarCat);
        dialog.add(pnlBotones, BorderLayout.SOUTH);
        dialog.setVisible(true);
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




