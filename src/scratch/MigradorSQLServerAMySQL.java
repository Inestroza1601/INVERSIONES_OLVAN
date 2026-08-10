package scratch;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class MigradorSQLServerAMySQL {

    public static void main(String[] args) {
        String url = "jdbc:sqlserver://170.80.140.2:6161;databaseName=NexarBD;encrypt=true;trustServerCertificate=true;";
        String outputFilePath = "C:\\#INVERSIONES OLVAN\\INVERSIONES_OLVAN\\para_so\\NexarBD_MySQL.sql";

        try (Connection conn = DriverManager.getConnection(url, "orionsys", "123");
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFilePath))) {

            writer.write("-- Script de migracion generado de SQL Server a MySQL\n");
            writer.write("SET FOREIGN_KEY_CHECKS=0;\n\n");

            DatabaseMetaData metaData = conn.getMetaData();
            
            // 1. Obtener todas las tablas
            List<String> tables = new ArrayList<>();
            try (ResultSet rsTables = metaData.getTables(null, "dbo", "%", new String[]{"TABLE"})) {
                while (rsTables.next()) {
                    String tableName = rsTables.getString("TABLE_NAME");
                    // Excluir tablas de sistema
                    if (!tableName.startsWith("sys") && !tableName.startsWith("sync")) {
                        tables.add(tableName);
                    }
                }
            }

            // 2. Iterar sobre las tablas para generar CREATE y datos
            for (String tableName : tables) {
                System.out.println("Procesando tabla: " + tableName);
                
                // Generar CREATE TABLE
                writer.write("DROP TABLE IF EXISTS `" + tableName + "`;\n");
                writer.write("CREATE TABLE `" + tableName + "` (\n");
                
                List<String> columnsInfo = new ArrayList<>();
                List<String> columnNames = new ArrayList<>();
                
                try (ResultSet rsCols = metaData.getColumns(null, "dbo", tableName, "%")) {
                    while (rsCols.next()) {
                        String colName = rsCols.getString("COLUMN_NAME");
                        int type = rsCols.getInt("DATA_TYPE");
                        String typeName = rsCols.getString("TYPE_NAME");
                        int size = rsCols.getInt("COLUMN_SIZE");
                        int decimalDigits = rsCols.getInt("DECIMAL_DIGITS");
                        String isNullable = rsCols.getString("IS_NULLABLE"); // "YES" or "NO"
                        boolean isAutoIncrement = rsCols.getString("IS_AUTOINCREMENT").equals("YES");

                        columnNames.add(colName);
                        
                        String mySqlType = mapToMySQLType(typeName, size, decimalDigits);
                        
                        String colDef = "  `" + colName + "` " + mySqlType;
                        if (isNullable.equals("NO")) {
                            colDef += " NOT NULL";
                        }
                        if (isAutoIncrement) {
                            colDef += " AUTO_INCREMENT";
                        }
                        columnsInfo.add(colDef);
                    }
                }
                
                // Tratar llaves primarias
                try (ResultSet rsKeys = metaData.getPrimaryKeys(null, "dbo", tableName)) {
                    List<String> pks = new ArrayList<>();
                    while (rsKeys.next()) {
                        pks.add(rsKeys.getString("COLUMN_NAME"));
                    }
                    if (!pks.isEmpty()) {
                        StringBuilder pkStr = new StringBuilder("  PRIMARY KEY (");
                        for (int i = 0; i < pks.size(); i++) {
                            pkStr.append("`").append(pks.get(i)).append("`");
                            if (i < pks.size() - 1) pkStr.append(", ");
                        }
                        pkStr.append(")");
                        columnsInfo.add(pkStr.toString());
                    }
                }

                writer.write(String.join(",\n", columnsInfo));
                writer.write("\n) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;\n\n");
                
                // Generar INSERTS
                try (Statement stmt = conn.createStatement();
                     ResultSet rsData = stmt.executeQuery("SELECT * FROM [" + tableName + "]")) {
                     
                    int colCount = columnNames.size();
                    while (rsData.next()) {
                        StringBuilder insert = new StringBuilder();
                        insert.append("INSERT INTO `").append(tableName).append("` (");
                        for (int i = 0; i < colCount; i++) {
                            insert.append("`").append(columnNames.get(i)).append("`");
                            if (i < colCount - 1) insert.append(", ");
                        }
                        insert.append(") VALUES (");
                        
                        for (int i = 1; i <= colCount; i++) {
                            Object val = rsData.getObject(i);
                            if (val == null) {
                                insert.append("NULL");
                            } else {
                                String valStr = val.toString().replace("'", "''").replace("\\", "\\\\");
                                insert.append("'").append(valStr).append("'");
                            }
                            if (i < colCount) insert.append(", ");
                        }
                        insert.append(");\n");
                        writer.write(insert.toString());
                    }
                }
                writer.write("\n");
            }
            
            writer.write("SET FOREIGN_KEY_CHECKS=1;\n");
            System.out.println("Migracion completada en: " + outputFilePath);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String mapToMySQLType(String sqlServerType, int size, int decimalDigits) {
        sqlServerType = sqlServerType.toUpperCase();
        if (sqlServerType.contains("IDENTITY")) {
            sqlServerType = sqlServerType.replace(" IDENTITY", "");
        }

        switch (sqlServerType) {
            case "INT":
                return "INT";
            case "BIGINT":
                return "BIGINT";
            case "SMALLINT":
                return "SMALLINT";
            case "TINYINT":
                return "TINYINT";
            case "BIT":
                return "TINYINT(1)";
            case "DECIMAL":
            case "NUMERIC":
                return "DECIMAL(" + size + "," + decimalDigits + ")";
            case "MONEY":
            case "SMALLMONEY":
                return "DECIMAL(19,4)";
            case "FLOAT":
            case "REAL":
                return "DOUBLE";
            case "VARCHAR":
            case "NVARCHAR":
                if (size > 65535 || size <= 0) return "LONGTEXT";
                return "VARCHAR(" + size + ")";
            case "CHAR":
            case "NCHAR":
                return "CHAR(" + size + ")";
            case "TEXT":
            case "NTEXT":
                return "LONGTEXT";
            case "DATE":
                return "DATE";
            case "DATETIME":
            case "DATETIME2":
            case "SMALLDATETIME":
                return "DATETIME";
            case "TIME":
                return "TIME";
            case "UNIQUEIDENTIFIER":
                return "VARCHAR(36)";
            case "IMAGE":
            case "VARBINARY":
                return "LONGBLOB";
            default:
                return "LONGTEXT"; // Fallback
        }
    }
}
