package com.autopartes.Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class EstanteDAO {
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
                0, // IDpieza opcional aquí
                rs.getString("nombre"),
                rs.getInt("pisos"),
                rs.getString("ubicacion")
            ));
        }
    } catch (SQLException e) { e.printStackTrace(); }
    return mapa;
}
}
