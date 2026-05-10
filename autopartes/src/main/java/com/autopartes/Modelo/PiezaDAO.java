// clase para hacer consultas sobre las piezas
package com.autopartes.Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PiezaDAO {

    public List<Pieza> obtenerTodas() {
        List<Pieza> lista = new ArrayList<>();
        // Usamos los nombres exactos: IDpieza, nombre, PrecioActual, imagen
        String sql = "SELECT IDpieza, nombre, PrecioActual, imagen FROM piezas";

        try (Connection db = Conexion.getInstancia(); // Aquí definiste 'db'
                PreparedStatement ps = db.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Asegúrate de NO usar la palabra 'conexion' aquí adentro
                lista.add(new Pieza(
                        rs.getInt("IDpieza"),
                        rs.getString("nombre"),
                        rs.getDouble("PrecioActual"),
                        rs.getString("imagen")));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL en PiezaDAO: " + e.getMessage());
        }
        return lista;
    }
}