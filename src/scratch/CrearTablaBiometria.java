package scratch;

import factory.ConexionFactory;
import java.sql.Connection;
import java.sql.Statement;

public class CrearTablaBiometria {
    public static void main(String[] args) {
        System.out.println("Iniciando creación de tabla USUARIO_BIOMETRIA en SQL Server...");
        try {
            ConexionFactory factory = new ConexionFactory();
            Connection con = factory.getConexion();
            
            String sql = "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='USUARIO_BIOMETRIA' and xtype='U')\n" +
                         "BEGIN\n" +
                         "    CREATE TABLE USUARIO_BIOMETRIA (\n" +
                         "        id_usuario INT NOT NULL,\n" +
                         "        label_id INT IDENTITY(1,1) NOT NULL,\n" +
                         "        modelo_entrenado VARBINARY(MAX),\n" +
                         "        PRIMARY KEY (label_id),\n" +
                         "        CONSTRAINT FK_BIOMETRIA_USUARIO FOREIGN KEY (id_usuario) REFERENCES USUARIOS(id_usuario)\n" +
                         "    )\n" +
                         "    PRINT 'Tabla creada exitosamente.'\n" +
                         "END\n" +
                         "ELSE\n" +
                         "BEGIN\n" +
                         "    PRINT 'La tabla USUARIO_BIOMETRIA ya existe.'\n" +
                         "END";
                         
            Statement st = con.createStatement();
            st.execute(sql);
            st.close();
            con.close();
            System.out.println("Proceso finalizado con éxito.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
