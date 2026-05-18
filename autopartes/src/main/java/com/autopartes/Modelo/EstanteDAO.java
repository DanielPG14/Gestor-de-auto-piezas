package com.autopartes.Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstanteDAO {
    
    public List<Estante> obtenerMapaAlmacen() {
    List<Estante> mapa = new ArrayList<>();

    String sql = "SELECT e.IDestante, p.nombre, e.pisos, a.ubicacion " +
                 "FROM Estantes e " +
                 "JOIN Piezas p ON e.IDestante = p.IDestante " + 
                 "JOIN Almacen a ON e.IDalmacen = a.IDalmacen";

    System.out.println("-> [DAO] PASO 1: Solicitando conexión a Conexion.getInstancia()...");
    
    try (Connection db = Conexion.getInstancia()) {
        System.out.println("-> [DAO] PASO 2: Conexión obtenida con éxito. Preparando la consulta SQL...");
        
        try (PreparedStatement ps = db.prepareStatement(sql)) {
            System.out.println("-> [DAO] PASO 3: Consulta preparada. Enviando ejecución a MySQL (Si se congela aquí, es un bloqueo de tablas)...");
            
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("-> [DAO] PASO 4: Base de datos respondió. Extrayendo filas...");
            
                while (rs.next()) {
                    mapa.add(new Estante(
                        rs.getInt("IDestante"),
                        0, 
                        rs.getString("nombre"),
                        rs.getInt("pisos"),
                        rs.getString("ubicacion")
                    ));
                }
            }
        }
        System.out.println("-> [DAO] Total estantes cargados para el mapa: " + mapa.size());

    } catch (SQLException e) { 
        System.err.println("-> [ERROR] Error en EstanteDAO.obtenerMapaAlmacen: " + e.getMessage());
        e.printStackTrace(); 
    }
    return mapa;
}

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

    // NUEVO MÉTODO: Extrae los IDs numéricos únicos ordenados de tu tabla estantes para la barra de UI lateral
    public List<Integer> obtenerListaIds() {
        List<Integer> ids = new ArrayList<>();
        String sql = "SELECT IDestante FROM estantes ORDER BY IDestante ASC";

        try (Connection db = Conexion.getInstancia();
             PreparedStatement ps = db.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ids.add(rs.getInt("IDestante"));
            }
        } catch (SQLException e) {
            System.err.println("Error en EstanteDAO.obtenerListaIds: " + e.getMessage());
        }
        return ids;
    }
}