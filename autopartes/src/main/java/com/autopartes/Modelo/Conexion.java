package com.autopartes.Modelo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {
    private static final String DB = "DBA";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB + "?serverTimezone=UTC";
    private static final String USER = "root"; 
    private static final String PASSWORD = "";

    private static Connection conexion = null;

    private Conexion() {}

    public static Connection getInstancia() {
        try {
            if (conexion == null || conexion.isClosed()) {
                Class.forName("com.mysql.cj.jdbc.Driver");
                conexion = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("LOG: Conexión establecida con éxito.");
            }
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("ERROR: No se pudo conectar a la base de datos: " + e.getMessage());
        }
        return conexion;
    }

    public static void cerrarConexion() {
        if (conexion != null) {
            try {
                conexion.close();
                System.out.println("LOG: Conexión cerrada.");
            } catch (SQLException e) {
                System.err.println("ERROR: Error al cerrar: " + e.getMessage());
            }
        }
    }
}