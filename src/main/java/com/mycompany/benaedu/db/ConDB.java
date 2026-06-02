package com.mycompany.benaedu.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author b17za
 */
public class ConDB {
    
    protected Connection conexion;
    
    // Credenciales y rutas de la nueva base de datos benaedu
    private final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    private final String DB_URL = "jdbc:mysql://localhost:3306/benaedu";
    private final String USER = "benavente";
    private final String PASSWORD = "lf]]rV]kyfsxnetD";
    
    /**
     * Establece la conexión con la base de datos utilizando variables de instancia.
     */
    public Connection Conectar() {
        try {
            // Es buena práctica registrar el driver ANTES de la conexión
            Class.forName(JDBC_DRIVER);
            conexion = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            // Opcional: Mensaje de éxito en consola para depuración
            // System.out.println("Conexión exitosa a benaedu");
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(ConDB.class.getName()).log(Level.SEVERE, "Error al conectar a la base de datos", ex);
        }
        return conexion;
    }
    
    /**
     * Cierra la conexión activa de forma segura.
     */
    public void Cerrar() {
        try {
            if (conexion != null && !conexion.isClosed()) {
                conexion.close();
            }
        } catch (SQLException ex) {
            Logger.getLogger(ConDB.class.getName()).log(Level.SEVERE, "Error al cerrar la conexión", ex);
        }
    }
    
    /**
     * Método estático alternativo por si prefieres obtener la conexión directamente.
     */
    public static Connection getConnection() throws Exception {
        String driver = "com.mysql.cj.jdbc.Driver";
        String dbUrl = "jdbc:mysql://localhost:3306/benaedu";
        String user = "benavente";
        String password = "lf]]rV]kyfsxnetD";
        
        Class.forName(driver);
        return DriverManager.getConnection(dbUrl, user, password);
    }
}