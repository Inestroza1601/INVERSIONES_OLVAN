package dao;

import factory.ConexionFactory;
import modelo.Usuario;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class UsuarioDAO {

    private ConexionFactory factory;

    public UsuarioDAO() {
        this.factory = new ConexionFactory();
    }

    // 1. CREAR NUEVO USUARIO
    public boolean registrarUsuario(Usuario u) {
        // SQL sin id_usuario porque es IDENTITY
        String sql = "INSERT INTO USUARIOS (id_rol, nombre_usuario, password_hash, estado_usuario, email_usuario) VALUES (?, ?, ?, 1, ?)";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, u.getIdRol());
            ps.setString(2, u.getNombreUsuario());
            ps.setString(3, u.getPasswordHash()); 
            ps.setString(4, u.getEmailUsuario());
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al registrar usuario: " + e.getMessage());
            return false;
        }
    }

    // 2. SOFT DELETE (Recibe int, no String)
    // 2. SOFT DELETE - F\u00EDjate que el par\u00E1metro ahora es (int idUsuario)
    public boolean desactivarUsuario(int idUsuario) {
        String sql = "UPDATE USUARIOS SET estado_usuario = 0 WHERE id_usuario = ?";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, idUsuario); // Ahora esto no dar\u00E1 error porque idUsuario es int
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al desactivar usuario: " + e.getMessage());
            return false;
        }
    }

    public boolean activarUsuario(int idUsuario) {
        String sql = "UPDATE USUARIOS SET estado_usuario = 1 WHERE id_usuario = ?";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, idUsuario); 
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al activar usuario: " + e.getMessage());
            return false;
        }
    }

    // 3. OBTENER TODOS PARA LA TABLA
    public List<Usuario> listarUsuarios() {
        List<Usuario> lista = new ArrayList<>();
        String sql = "SELECT u.*, r.nombre_rol FROM USUARIOS u INNER JOIN ROLES_USUARIO r ON u.id_rol = r.id_rol ORDER BY u.estado_usuario DESC, u.nombre_usuario ASC";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                Usuario u = new Usuario();
                u.setIdUsuario(rs.getInt("id_usuario"));
                u.setIdRol(rs.getInt("id_rol"));
                u.setNombreRol(rs.getString("nombre_rol")); // <--- LEEMOS EL NOMBRE DE LA BD
                u.setNombreUsuario(rs.getString("nombre_usuario"));
                u.setPasswordHash(rs.getString("password_hash"));
                u.setEstadoUsuario(rs.getBoolean("estado_usuario"));
                u.setEmailUsuario(rs.getString("email_usuario"));
                lista.add(u);
            }
        } catch (SQLException e) {
            System.err.println("Error al listar usuarios: " + e.getMessage());
        }
        return lista;
    }
    
    // 4. ACTUALIZAR USUARIO EXISTENTE
    public boolean actualizarUsuario(Usuario u) {
        String sql;
        boolean actualizaClave = u.getPasswordHash() != null && !u.getPasswordHash().isEmpty();
        
        if (actualizaClave) {
            sql = "UPDATE USUARIOS SET id_rol = ?, nombre_usuario = ?, password_hash = ?, email_usuario = ? WHERE id_usuario = ?";
        } else {
            sql = "UPDATE USUARIOS SET id_rol = ?, nombre_usuario = ?, email_usuario = ? WHERE id_usuario = ?";
        }
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, u.getIdRol());
            ps.setString(2, u.getNombreUsuario());
            
            if (actualizaClave) {
                ps.setString(3, u.getPasswordHash());
                ps.setString(4, u.getEmailUsuario());
                ps.setInt(5, u.getIdUsuario()); // Correcci\u00F3n a setInt
            } else {
                ps.setString(3, u.getEmailUsuario());
                ps.setInt(4, u.getIdUsuario()); // Correcci\u00F3n a setInt
            }
            
            return ps.executeUpdate() > 0;
            
        } catch (SQLException e) {
            System.err.println("Error al actualizar usuario: " + e.getMessage());
            return false;
        }
    }

    // 5. AUTENTICAR USUARIO PARA SESI\u00D3N
    public Usuario autenticarUsuario(String nombreUsuario, String passwordPlana) throws SQLException {
        String hash = utilidades.Seguridad.encriptarSHA256(passwordPlana);
        String sql = "SELECT u.*, r.nombre_rol FROM USUARIOS u INNER JOIN ROLES_USUARIO r ON u.id_rol = r.id_rol WHERE LOWER(u.nombre_usuario) = LOWER(?) AND u.password_hash = ? AND u.estado_usuario = 1";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, nombreUsuario.trim());
            ps.setString(2, hash);
            
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setIdRol(rs.getInt("id_rol"));
                    u.setNombreRol(rs.getString("nombre_rol"));
                    u.setNombreUsuario(rs.getString("nombre_usuario"));
                    u.setPasswordHash(rs.getString("password_hash"));
                    u.setEstadoUsuario(rs.getBoolean("estado_usuario"));
                    u.setEmailUsuario(rs.getString("email_usuario"));
                    
                    // Cargar permisos RBAC
                    u.setPermisos(cargarPermisosRol(u.getIdRol()));
                    
                    return u;
                }
            }
        }
        return null;
    }
    
    // M\u00E9todo auxiliar para cargar permisos de un rol
    public List<String> cargarPermisosRol(int idRol) {
        List<String> permisos = new ArrayList<>();
        // Si el esquema de RBAC a\u00FAn no existe en la BD (ej. script no ejecutado), atrapamos el error y devolvemos lista vac\u00EDa (o acceso total si es admin).
        String sql = "SELECT p.nombre_permiso FROM PERMISOS p " +
                     "INNER JOIN ROL_PERMISOS rp ON p.id_permiso = rp.id_permiso " +
                     "WHERE rp.id_rol = ?";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idRol);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    permisos.add(rs.getString("nombre_permiso").toUpperCase());
                }
            }
        } catch (SQLException e) {
            System.err.println("Aviso: No se pudieron cargar permisos (\u00BFTablas RBAC creadas?): " + e.getMessage());
        }
        return permisos;
    }
    
    // =========================================================
    // METODOS DE RECUPERACION DE CONTRASE\u00D1A
    // =========================================================
    
    public Usuario obtenerUsuarioPorNombre(String nombreUsuario) {
        String sql = "SELECT * FROM USUARIOS WHERE LOWER(nombre_usuario) = LOWER(?) AND estado_usuario = 1";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, nombreUsuario.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setIdRol(rs.getInt("id_rol"));
                    u.setNombreUsuario(rs.getString("nombre_usuario"));
                    u.setEmailUsuario(rs.getString("email_usuario"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por nombre: " + e.getMessage());
        }
        return null;
    }
    
    public Usuario obtenerUsuarioPorId(int idUsuario) {
        String sql = "SELECT u.*, r.nombre_rol FROM USUARIOS u LEFT JOIN ROLES_USUARIO r ON u.id_rol = r.id_rol WHERE u.id_usuario = ?";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setInt(1, idUsuario);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setIdRol(rs.getInt("id_rol"));
                    u.setNombreRol(rs.getString("nombre_rol"));
                    u.setNombreUsuario(rs.getString("nombre_usuario"));
                    u.setEmailUsuario(rs.getString("email_usuario"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por ID: " + e.getMessage());
        }
        return null;
    }
    
    public Usuario obtenerUsuarioPorEmail(String email) {
        String sql = "SELECT * FROM USUARIOS WHERE LOWER(email_usuario) = LOWER(?) AND estado_usuario = 1";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
             
            ps.setString(1, email.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Usuario u = new Usuario();
                    u.setIdUsuario(rs.getInt("id_usuario"));
                    u.setIdRol(rs.getInt("id_rol"));
                    u.setNombreUsuario(rs.getString("nombre_usuario"));
                    u.setEmailUsuario(rs.getString("email_usuario"));
                    return u;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar usuario por email: " + e.getMessage());
        }
        return null;
    }
    
    public boolean guardarTokenRecuperacion(int idUsuario, String token) {
        // Token expira en 15 minutos
        String sql = "UPDATE USUARIOS SET token_recuperacion = ?, expiracion_token = DATEADD(minute, 15, GETDATE()) WHERE id_usuario = ?";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, token);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al guardar token: " + e.getMessage());
            return false;
        }
    }
    
    public boolean validarTokenRecuperacion(int idUsuario, String token) {
        String sql = "SELECT * FROM USUARIOS WHERE id_usuario = ? AND token_recuperacion = ? AND expiracion_token > GETDATE()";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, idUsuario);
            ps.setString(2, token);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Si hay resultado, el token es v\u00E1lido
            }
        } catch (SQLException e) {
            System.err.println("Error al validar token: " + e.getMessage());
            return false;
        }
    }
    
    public boolean actualizarPassword(int idUsuario, String passwordPlana) {
        String hash = utilidades.Seguridad.encriptarSHA256(passwordPlana);
        // Limpiamos el token al actualizar la contrase\u00F1a
        String sql = "UPDATE USUARIOS SET password_hash = ?, token_recuperacion = NULL, expiracion_token = NULL WHERE id_usuario = ?";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash);
            ps.setInt(2, idUsuario);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al actualizar password: " + e.getMessage());
            return false;
        }
    }

    // =========================================================
    // VALIDACION DE SEGURIDAD
    // =========================================================

    public boolean existePassword(String passwordPlana) {
        String hash = utilidades.Seguridad.encriptarSHA256(passwordPlana);
        String sql = "SELECT 1 FROM USUARIOS WHERE password_hash = ?";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, hash);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next(); // Retorna true si hay un hash igual
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar password: " + e.getMessage());
        }
        return false;
    }

    // =========================================================
    // GESTI\u00D3N DIN\u00C1MICA DE ROLES
    // =========================================================

    public List<String> listarNombresDeRoles() {
        List<String> roles = new ArrayList<>();
        String sql = "SELECT nombre_rol FROM ROLES_USUARIO ORDER BY id_rol ASC";
        
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
             
            while (rs.next()) {
                roles.add(rs.getString("nombre_rol"));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar roles: " + e.getMessage());
        }
        return roles;
    }

    public int obtenerOCrearRol(String nombreRol) {
        nombreRol = nombreRol.trim().toLowerCase();
        int idRol = -1;

        String sqlSelect = "SELECT id_rol FROM ROLES_USUARIO WHERE LOWER(nombre_rol) = ?";
        try (Connection con = factory.getConexion();
             PreparedStatement psSelect = con.prepareStatement(sqlSelect)) {
             
            psSelect.setString(1, nombreRol);
            try (ResultSet rs = psSelect.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("id_rol");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al buscar rol: " + e.getMessage());
        }

        String sqlInsert = "INSERT INTO ROLES_USUARIO (nombre_rol) VALUES (?)";
        try (Connection con = factory.getConexion();
             PreparedStatement psInsert = con.prepareStatement(sqlInsert, PreparedStatement.RETURN_GENERATED_KEYS)) {
             
            psInsert.setString(1, nombreRol);
            psInsert.executeUpdate();
            
            try (ResultSet rsKeys = psInsert.getGeneratedKeys()) {
                if (rsKeys.next()) {
                    idRol = rsKeys.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error al crear nuevo rol: " + e.getMessage());
        }

        return idRol;
    }
    
    // Obtener lista completa de objetos Rol
    public List<modelo.Rol> obtenerTodosLosRoles() {
        List<modelo.Rol> roles = new ArrayList<>();
        String sql = "SELECT id_rol, nombre_rol FROM ROLES_USUARIO ORDER BY id_rol ASC";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                roles.add(new modelo.Rol(rs.getInt("id_rol"), rs.getString("nombre_rol")));
            }
        } catch (SQLException e) {
            System.err.println("Error al listar roles completos: " + e.getMessage());
        }
        return roles;
    }
    
    // Guardar permisos seleccionados para un rol
    public boolean guardarPermisosRol(int idRol, List<String> permisos) {
        Connection con = null;
        PreparedStatement psDelete = null;
        PreparedStatement psInsert = null;
        
        try {
            con = factory.getConexion();
            con.setAutoCommit(false); // Transacci\u00F3n
            
            // 1. Eliminar todos los permisos actuales
            String sqlDelete = "DELETE FROM ROL_PERMISOS WHERE id_rol = ?";
            psDelete = con.prepareStatement(sqlDelete);
            psDelete.setInt(1, idRol);
            psDelete.executeUpdate();
            
            // 2. Insertar los nuevos permisos
            if (permisos != null && !permisos.isEmpty()) {
                String sqlInsert = "INSERT INTO ROL_PERMISOS (id_rol, id_permiso) " +
                                   "VALUES (?, (SELECT id_permiso FROM PERMISOS WHERE UPPER(nombre_permiso) = UPPER(?)))";
                psInsert = con.prepareStatement(sqlInsert);
                
                for (String p : permisos) {
                    psInsert.setInt(1, idRol);
                    psInsert.setString(2, p);
                    psInsert.addBatch();
                }
                psInsert.executeBatch();
            }
            
            con.commit();
            return true;
            
        } catch (SQLException e) {
            System.err.println("Error al guardar permisos de rol: " + e.getMessage());
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { }
            }
            return false;
        } finally {
            try { if (psDelete != null) psDelete.close(); } catch (Exception e) {}
            try { if (psInsert != null) psInsert.close(); } catch (Exception e) {}
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (Exception e) {}
        }
    }
    
    // Editar el nombre de un rol
    public boolean editarRol(int idRol, String nuevoNombre) {
        String sql = "UPDATE ROLES_USUARIO SET nombre_rol = ? WHERE id_rol = ?";
        try (Connection con = factory.getConexion();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, nuevoNombre.trim());
            ps.setInt(2, idRol);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error al editar rol: " + e.getMessage());
            return false;
        }
    }

    // Eliminar un rol (fallar\u00E1 si hay usuarios con este rol asignado por FK)
    public boolean eliminarRol(int idRol) {
        Connection con = null;
        PreparedStatement psDeletePermisos = null;
        PreparedStatement psDeleteRol = null;
        try {
            con = factory.getConexion();
            con.setAutoCommit(false);
            
            // Primero eliminar sus permisos asignados
            String sqlPermisos = "DELETE FROM ROL_PERMISOS WHERE id_rol = ?";
            psDeletePermisos = con.prepareStatement(sqlPermisos);
            psDeletePermisos.setInt(1, idRol);
            psDeletePermisos.executeUpdate();
            
            // Luego eliminar el rol
            String sqlRol = "DELETE FROM ROLES_USUARIO WHERE id_rol = ?";
            psDeleteRol = con.prepareStatement(sqlRol);
            psDeleteRol.setInt(1, idRol);
            int filas = psDeleteRol.executeUpdate();
            
            con.commit();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error al eliminar rol (verificar constraint FK con usuarios): " + e.getMessage());
            if (con != null) {
                try { con.rollback(); } catch (SQLException ex) { }
            }
            return false;
        } finally {
            try { if (psDeletePermisos != null) psDeletePermisos.close(); } catch (Exception e) {}
            try { if (psDeleteRol != null) psDeleteRol.close(); } catch (Exception e) {}
            try { if (con != null) { con.setAutoCommit(true); con.close(); } } catch (Exception e) {}
        }
    }
}
