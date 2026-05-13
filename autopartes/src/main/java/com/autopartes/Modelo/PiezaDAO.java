package com.autopartes.Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PiezaDAO {

    public List<Pieza> obtenerTodas() {
        List<Pieza> lista = new ArrayList<>();

        // Consulta para unir Piezas, Estantes y Almacenes, para la gestión de stock
        String sql = "SELECT p.IDpieza, p.nombre, p.PrecioActual, p.imagen, p.stock, p.nivelAsigned, " +
                "e.IDestante, e.capMax " + //
                "FROM Piezas p " +
                "INNER JOIN Estantes e ON p.IDestante = e.IDestante " +
                "INNER JOIN Almacen a ON e.IDalmacen = a.IDalmacen";
        // Conexión y ejecución de la consulta
        try (Connection db = Conexion.getInstancia();
                PreparedStatement ps = db.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            // Mapeo de resultados a objetos Pieza, se guarda en la lista y se manda a la
            // tabla en StockC
            while (rs.next()) {
                lista.add(new Pieza(
                        rs.getInt("IDpieza"),
                        rs.getString("nombre"),
                        rs.getDouble("PrecioActual"),
                        rs.getString("imagen"),
                        String.valueOf(rs.getInt("IDestante")),
                        rs.getInt("nivelAsigned"),
                        rs.getInt("stock"),
                        rs.getInt("capMax")));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL en PiezaDAO: " + e.getMessage());
        }
        return lista;
    }

    public boolean registrarNuevaPieza(Pieza pieza) {
        // Define la consulta SQL para insertar una nueva pieza, con los campos necesarios
        String sql = "INSERT INTO Piezas (nombre, PrecioActual, imagen, stock, IDestante, nivelAsigned) " +
                "VALUES (?, ?, ?, ?, ?, ?)"; // "?" es un placeholder para los parámetros que se pasarán
        // Conexión a la base de datos y preparación de la consulta
        try (Connection db = Conexion.getInstancia();
                PreparedStatement ps = db.prepareStatement(sql)) { //preparar la consulta con los parámetros

            //Se pasan los parámetros desde el objeto definido en Pieza.java
            ps.setString(1, pieza.getNombre()); //nombre pieza
            ps.setDouble(2, pieza.getPrecioActual()); //precio pieza

            //manejador de la imagen, por defecto default
            if (pieza.getImagen() != null && !pieza.getImagen().isEmpty()) {
                ps.setString(3, pieza.getImagen()); //ruta
            } else {
                ps.setString(3, "default.jpg"); //default
            }

            ps.setInt(4, pieza.getStock()); // Stock inicial
            ps.setInt(5, Integer.parseInt(pieza.getIdEstante())); // ID de String a INT para la FK
            ps.setInt(6, pieza.getNivelAsigned()); // Piso estante

            // Ejecutamos la consulta. Retorna true si se insertó correctamente
            int filasAfectadas = ps.executeUpdate(); //retorna filas afectadas
            return filasAfectadas > 0; // Si se afectó al menos una fila, la inserción fue exitosa

        } catch (SQLException e) {
            System.err.println("Error SQL al registrar pieza en PiezaDAO: " + e.getMessage());
            return false;
        }
    }
}