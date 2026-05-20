//DAO para estantes, maneja consultas relacionadas con la ubicación de piezas en el almacén
package com.autopartes.Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class EstanteDAO {
    
    public List<Estante> obtenerMapaAlmacen() { // Método para obtener un mapa completo del almacén, incluyendo la ubicación de cada pieza en los estantes
    List<Estante> mapa = new ArrayList<>(); //Se genera una lista de estantes vacía para almacenar los resultados de la consulta SQL
    
    //SELECT con JOIN para obtener la información de los estantes junto con las piezas y su ubicación en el almacén
    String sql = "SELECT e.IDestante, p.nombre, e.pisos, a.ubicacion " +
                 "FROM Estantes e " +
                 "JOIN Piezas p ON e.IDestante = p.IDestante " + 
                 "JOIN Almacen a ON e.IDalmacen = a.IDalmacen";

    System.out.println("-> [DAO] PASO 1: Solicitando conexión a Conexion.getInstancia()...");
    
    try (Connection db = Conexion.getInstancia()) {
        System.out.println("-> [DAO] PASO 2: Conexión obtenida con éxito. Preparando la consulta SQL...");
        
        try (PreparedStatement ps = db.prepareStatement(sql)) { //PreparedStatement es una clase que permite ejecutar consultas SQL de manera segura, evitando inyecciones SQL
            System.out.println("-> [DAO] PASO 3: Consulta preparada. Enviando ejecución a MySQL (Si se congela aquí, es un bloqueo de tablas)...");
            
            try (ResultSet rs = ps.executeQuery()) {
                System.out.println("-> [DAO] PASO 4: Base de datos respondió. Extrayendo filas...");
            
                while (rs.next()) {
                    mapa.add(new Estante( //Se crea un nuevo objeto Estante con los datos obtenidos de la consulta SQL y se agrega a la lista mapa
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
    // Método para buscar la ubicación de una pieza específica por su ID, devuelve un objeto Estante con la información de la ubicación
    public Estante buscarUbicacionPieza(int idPieza) {
        //SELECT con JOIN para obtener la información del estante donde se encuentra la pieza específica, junto con su ubicación en el almacén
        //where con el parametro pasado a la clase (idPieza) para filtrar por la pieza específica
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
        //SELECT con ORDER BY para obtener los IDs de los estantes ordenados de forma ascendente, lo que facilita su visualización en la barra lateral de la UI
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