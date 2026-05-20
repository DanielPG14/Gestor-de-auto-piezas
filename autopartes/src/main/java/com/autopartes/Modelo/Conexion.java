//Clase para conexion con DB, usa singleton
package com.autopartes.Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String DB = "DBA";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB + "?serverTimezone=UTC";
    private static final String USER = "root"; 
    private static final String PASSWORD = "";

    private Conexion() {}

    public static Connection getInstancia() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            // Retorna una conexión directa que el try-with-resources del DAO se encargará de cerrar
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("ERROR: No se pudo conectar a la base de datos: " + e.getMessage());
            return null;
        }
    }
}