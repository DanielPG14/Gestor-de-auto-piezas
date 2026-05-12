package com.autopartes.Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstanteDAO {
    
    // Para ver todo el mapa del almacén
    public List<Estante> obtenerMapaAlmacen() {
        List<Estante> mapa = new ArrayList<>();
        String sql = "SELECT e.IDestante, p.nombre, e.pisos, a.ubicacion " +
                     "FROM Estantes e " +
                     "JOIN Piezas p ON e.IDpieza = p.IDpieza " +
                     "JOIN Almacen a ON e.IDalmacen = a.IDalmacen";
        
        try (Connection db = Conexion.getInstancia();
             PreparedStatement ps = db.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                mapa.add(new Estante(
                    rs.getInt("IDestante"),
                    0, 
                    rs.getString("nombre"),
                    rs.getInt("pisos"),
                    rs.getString("ubicacion")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return mapa;
    }

    // NUEVO: Para que el almacenista busque dónde guardar una pieza específica
    public Estante buscarUbicacionPieza(int idPieza) {
        String sql = "SELECT e.IDestante, p.nombre, e.pisos, a.ubicacion " +
                     "FROM Estantes e " +
                     "JOIN Piezas p ON e.IDpieza = p.IDpieza " +
                     "JOIN Almacen a ON e.IDalmacen = a.IDalmacen " +
                     "WHERE p.IDpieza = ?";
        try (Connection db = Conexion.getInstancia();
             PreparedStatement ps = db.prepareStatement(sql)) {
            ps.setInt(1, idPieza);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Estante(rs.getInt("IDestante"), idPieza, rs.getString("nombre"), rs.getInt("pisos"), rs.getString("ubicacion"));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }
}