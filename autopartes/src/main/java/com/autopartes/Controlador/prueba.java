package com.autopartes.Controlador;

import com.autopartes.Modelo.Conexion;
import javafx.fxml.FXML;
import java.sql.Connection;
import java.sql.SQLException;

public class prueba {

    @FXML
    private void manejarBotonPrueba() {
        System.out.println("LOG: Intentando conectar a DBA...");
        
        try (Connection db = Conexion.getInstancia()) {
            if (db != null && !db.isClosed()) {
                System.out.println("¡ÉXITO!: La conexión a DBA está activa y lista.");
            } else {
                System.out.println("ERROR: La conexión regresó nula.");
            }
        } catch (SQLException e) {
            System.err.println("ERROR de SQL: " + e.getMessage());
        }
    }
}