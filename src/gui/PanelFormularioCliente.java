package gui;

import modelo.Cliente;
import dao.ClienteDAO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.MaskFormatter;
import java.awt.*;
import java.text.ParseException;
import java.util.Set;
import java.util.HashSet;

public class PanelFormularioCliente extends JPanel {

    private JDialog dialogPadre;
    private PanelGestionClientes panelPadre; 
    private Cliente clienteAEditar; 
    
    // --- RAM CACHE ---
    private Set<String> identidadesEnRam;
    
    private JFormattedTextField txtIdentidad; // Ahora es FormattedTextField
    private JLabel lblErrorIdentidad;
    private JTextField txtNombre;
    private JTextField txtApellido;
    private JFormattedTextField txtTelefono;  // Ahora es FormattedTextField
    private JTextField txtCorreo;
    
    private JButton btnGuardar;
    private JButton btnCancelar;

    public JDialog getDialogoPadre() {
        return dialogPadre;
    }

    public PanelFormularioCliente(JDialog dialogPadre, PanelGestionClientes panelPadre, Cliente cliente) {
        this.dialogPadre = dialogPadre;
        this.panelPadre = panelPadre;
        this.clienteAEditar = cliente;
        
        // 1. CARGAMOS LA RAM ANTES DE INICIAR EL DISE\u00D1O
        ClienteDAO dao = new ClienteDAO();
        identidadesEnRam = dao.obtenerIdentidadesEnRam();
        
        // Si estamos editando, sacamos SU PROPIA identidad de la RAM para que no choque consigo mismo
        if (clienteAEditar != null) {
            identidadesEnRam.remove(clienteAEditar.getIdentidadCliente());
        }

        iniciarDiseno();
        cargarDatosSiEsEdicion();
        configurarValidacionEnVivo();
    }

    private void iniciarDiseno() {
        this.setLayout(new BorderLayout());
        this.setBackground(new Color(240, 242, 245)); // Gris Nube 
        this.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 222, 225), 2), // Borde gris claro
            BorderFactory.createEmptyBorder(25, 30, 25, 30)
        ));

        String textoTitulo = (clienteAEditar == null) ? "Registrar Nuevo Cliente" : "Editar Cliente";
        JLabel lblTitulo = new JLabel(textoTitulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitulo.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        this.add(lblTitulo, BorderLayout.NORTH);

        JPanel pnlForm = new JPanel(new GridBagLayout());
        pnlForm.setBackground(new Color(255, 255, 255)); // Blanco Puro
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 5, 5, 5); 
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // 2. CONFIGURACI\u00D3N DE LAS M\u00C1SCARAS
        try {
            MaskFormatter maskDNI = new MaskFormatter("####-####-#####");
            maskDNI.setPlaceholderCharacter('_');
            txtIdentidad = new JFormattedTextField(maskDNI);
            
            MaskFormatter maskTel = new MaskFormatter("####-####");
            maskTel.setPlaceholderCharacter('_');
            txtTelefono = new JFormattedTextField(maskTel);
        } catch (ParseException e) {
            // Fallback por si hay error en la m\u00E1scara
            txtIdentidad = new JFormattedTextField();
            txtTelefono = new JFormattedTextField();
        }

        txtNombre = new JTextField(20);
        txtApellido = new JTextField(20);
        txtCorreo = new JTextField(20);

        agregarFila(pnlForm, gbc, 0, "Identidad:", txtIdentidad);
        
        gbc.gridy = 1;
        gbc.gridx = 1;
        gbc.insets = new Insets(0, 5, 10, 5); 
        lblErrorIdentidad = new JLabel(" ");
        lblErrorIdentidad.setForeground(new Color(227, 0, 15)); // Rojo Logo
        lblErrorIdentidad.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlForm.add(lblErrorIdentidad, gbc);

        gbc.insets = new Insets(10, 5, 10, 5);
        
        agregarFila(pnlForm, gbc, 2, "Nombre:", txtNombre);
        agregarFila(pnlForm, gbc, 3, "Apellido:", txtApellido);
        agregarFila(pnlForm, gbc, 4, "Tel\u00E9fono:", txtTelefono);
        agregarFila(pnlForm, gbc, 5, "Correo Electr\u00F3nico:", txtCorreo);

        this.add(pnlForm, BorderLayout.CENTER);

        JPanel pnlBotones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        pnlBotones.setBackground(new Color(240, 242, 245)); // Gris Nube
        pnlBotones.setBorder(BorderFactory.createEmptyBorder(20, 0, 0, 0));

        btnCancelar = new JButton("Cancelar");
        btnCancelar.setBackground(new Color(140, 145, 150)); // Gris suave
        btnCancelar.setForeground(Color.WHITE);
        btnCancelar.setFocusPainted(false);
        btnCancelar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnCancelar.addActionListener(e -> dialogPadre.dispose());

        String textoBoton = (clienteAEditar == null) ? "Guardar Cliente" : "Actualizar Cliente";
        Color colorBoton = new Color(39, 174, 96); // Verde Menta unificado
        
        btnGuardar = new JButton(textoBoton);
        btnGuardar.setBackground(colorBoton);
        btnGuardar.setForeground(Color.WHITE);
        btnGuardar.setFocusPainted(false);
        btnGuardar.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnGuardar.addActionListener(e -> guardarOActualizar());

        pnlBotones.add(btnCancelar);
        pnlBotones.add(btnGuardar);

        this.add(pnlBotones, BorderLayout.SOUTH);
    }

    private void cargarDatosSiEsEdicion() {
        if (clienteAEditar != null) {
            txtIdentidad.setText(clienteAEditar.getIdentidadCliente());
            txtNombre.setText(clienteAEditar.getNombreCliente());
            txtApellido.setText(clienteAEditar.getApellidoCliente());
            txtTelefono.setText(clienteAEditar.getTelefonoCliente());
            txtCorreo.setText(clienteAEditar.getCorreoCliente());
        }
    }

    private void configurarValidacionEnVivo() {
        txtIdentidad.getDocument().addDocumentListener(new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { validarIdentidad(); }
            @Override public void removeUpdate(DocumentEvent e) { validarIdentidad(); }
            @Override public void changedUpdate(DocumentEvent e) { validarIdentidad(); }
        });
    }

    private void validarIdentidad() {
        // 3. LIMPIAMOS EL TEXTO PARA LA VALIDACI\u00D3N (Quitamos guiones y guiones bajos)
        String identidadRaw = txtIdentidad.getText().replace("-", "").replace("_", "").trim();
        
        // Si no ha escrito los 13 d\u00EDgitos, restauramos visualmente
        if (identidadRaw.isEmpty() || identidadRaw.length() < 13) {
            restaurarEstiloIdentidad();
            btnGuardar.setEnabled(true);
            return;
        }

        //  VERIFICACI\u00D3N EN RAM
        if (identidadesEnRam.contains(identidadRaw)) {
            txtIdentidad.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(227, 0, 15), 2), // Rojo Logo
                BorderFactory.createEmptyBorder(4, 7, 4, 7)
            ));
            lblErrorIdentidad.setText("Esta identidad ya est\u00E1 registrada.");
            btnGuardar.setEnabled(false); 
        } else {
            restaurarEstiloIdentidad();
            btnGuardar.setEnabled(true); 
        }
    }

    private void restaurarEstiloIdentidad() {
        txtIdentidad.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 222, 225)), // Gris claro
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        lblErrorIdentidad.setText(" ");
    }

    private void guardarOActualizar() {
        // Obtenemos los valores "limpios" sin guiones
        String identidadLimpia = txtIdentidad.getText().replace("-", "").replace("_", "").trim();
        String telefonoLimpio = txtTelefono.getText().replace("-", "").replace("_", "").trim();
        
        if (txtNombre.getText().trim().isEmpty() || identidadLimpia.isEmpty()) {
            utilidades.Mensajes.showMessageDialog(this, "El nombre y la identidad son obligatorios.", "Campos Incompletos", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // Validamos que haya escrito los 13 n\u00FAmeros completos
        if (identidadLimpia.length() != 13) {
            utilidades.Mensajes.showMessageDialog(this, "La identidad debe tener exactamente 13 d\u00EDgitos.", "Formato Inv\u00E1lido", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Cliente c = new Cliente();
        c.setIdentidadCliente(identidadLimpia);
        c.setNombreCliente(txtNombre.getText().trim());
        c.setApellidoCliente(txtApellido.getText().trim());
        // Solo guardamos el tel\u00E9fono si escribi\u00F3 algo
        c.setTelefonoCliente(telefonoLimpio.isEmpty() ? "" : telefonoLimpio);
        c.setCorreoCliente(txtCorreo.getText().trim());

        ClienteDAO dao = new ClienteDAO();
        boolean exito;

        if (clienteAEditar == null) {
            exito = dao.registrarCliente(c);
        } else {
            c.setIdCliente(clienteAEditar.getIdCliente());
            exito = dao.actualizarCliente(c);
        }

        if (exito) {
            utilidades.Mensajes.showMessageDialog(this, "Cliente guardado exitosamente.");
            panelPadre.cargarDatosDesdeBD(); 
            dialogPadre.dispose(); 
        } else {
            utilidades.Mensajes.showMessageDialog(this, "Error al guardar en la base de datos.");
        }
    }

    private void agregarFila(JPanel panel, GridBagConstraints gbc, int fila, String etiqueta, JTextField campo) {
        gbc.gridy = fila;
        gbc.gridx = 0;
        gbc.weightx = 0.0;
        JLabel label = new JLabel(etiqueta);
        label.setForeground(new Color(45, 45, 45)); // Gris Oscuro
        label.setFont(new Font("Segoe UI", Font.BOLD, 13));
        panel.add(label, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        campo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        campo.setBackground(new Color(250, 250, 250)); // Fondo claro input
        campo.setForeground(new Color(45, 45, 45)); // Texto oscuro
        campo.setCaretColor(new Color(45, 45, 45));
        
        campo.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 222, 225)), // Gris claro
            BorderFactory.createEmptyBorder(5, 8, 5, 8)
        ));
        panel.add(campo, gbc);
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
}

