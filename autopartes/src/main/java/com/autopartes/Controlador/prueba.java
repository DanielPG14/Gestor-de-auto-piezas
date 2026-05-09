package com.autopartes.Controlador;

import com.autopartes.Modelo.Conexion;
import com.autopartes.Modelo.Pieza;
import com.autopartes.Modelo.PiezaDAO;

import javafx.fxml.FXML;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class prueba {
    @FXML
    private void manejarBotonPrueba() {
        System.out.println("LOG: Consultando tabla 'piezas' en DBA...");

        PiezaDAO dao = new PiezaDAO();
        List<Pieza> lista = dao.obtenerTodas();

        if (lista.isEmpty()) {
            System.out.println("ALERTA: No se trajeron datos. ¿La tabla piezas tiene registros?");
        } else {
            for (Pieza p : lista) {
                System.out.println("Pieza encontrada: " + p.getNombre() + " - $" + p.getPrecioActual());
            }
        }
    }
}