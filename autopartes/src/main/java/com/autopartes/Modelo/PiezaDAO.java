package com.autopartes.Modelo;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PiezaDAO {

    public List<Pieza> obtenerTodas() {
        List<Pieza> lista = new ArrayList<>();
        
        String sql = "SELECT p.IDpieza, p.nombre, p.imagen, p.stock, p.nivelAsigned, p.IDestante, " +
                     "pr.nombre_razon_social, pp.codigo_proveedor, hp.precio_compra " +
                     "FROM piezas p " +
                     "INNER JOIN producto_proveedor pp ON p.IDpieza = pp.id_pieza " +
                     "INNER JOIN proveedor pr ON pp.id_proveedor = pr.id_proveedor " +
                     "INNER JOIN historial_precio hp ON pp.id_prod_prov = hp.id_prod_prov " +
                     "WHERE hp.fecha_fin IS NULL";

        try (Connection db = Conexion.getInstancia();
             PreparedStatement ps = db.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                // Intentamos obtener la capacidad máxima desde el query; si no existe en tu SELECT actual, ponemos 100 por defecto
                int capMax = 100; 
                try {
                    capMax = rs.getInt("capMax"); // Ajusta el nombre de la columna si en tu BD se llama diferente
                } catch (SQLException e) {
                    // Si no venía en el SELECT, no pasa nada, se queda con el valor por defecto
                }

                // CORRECCIÓN: Orden exacto del constructor de tu clase Pieza
                lista.add(new Pieza(
                        rs.getInt("IDpieza"),                     // 1. idPieza (int)
                        rs.getString("nombre"),                   // 2. nombre (String)
                        rs.getString("nombre_razon_social"),      // 3. razonSocialProveedor (String)
                        rs.getString("codigo_proveedor"),         // 4. codigoProveedor (String)
                        rs.getDouble("precio_compra"),            // 5. precioCompra (double)
                        rs.getString("imagen"),                   // 6. imagen (String)
                        String.valueOf(rs.getInt("IDestante")),   // 7. idEstante (String)
                        rs.getInt("nivelAsigned"),                // 8. nivelAsigned (int)
                        rs.getInt("stock"),                       // 9. stock (int)
                        capMax                                    // 10. capMax (int)
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL en PiezaDAO.obtenerTodas: " + e.getMessage());
        }
        return lista;
    }

    public boolean registrarNuevaPieza(Pieza pieza) {
        String sql = "INSERT INTO piezas (nombre, imagen, stock, IDestante, nivelAsigned) VALUES (?, ?, ?, ?, ?)";
        // Nota: Las inserciones a producto_proveedor e historial se hacen en la transacción aquí o mediante métodos complementarios

        try (Connection db = Conexion.getInstancia();
             PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, pieza.getNombre());
            ps.setString(2, pieza.getImagen() != null ? pieza.getImagen() : "default.jpg");
            ps.setInt(3, pieza.getStock());
            ps.setInt(4, Integer.parseInt(pieza.getIdEstante()));
            ps.setInt(5, pieza.getNivelAsigned());
            
            // CORRECCIÓN ERROR 2: Si necesitas enviar el precio a las tablas hijas en la transacción:
            // double costo = pieza.getPrecioCompra(); 

            int filas = ps.executeUpdate();
            return filas > 0;
        } catch (SQLException e) {
            System.err.println("Error SQL en PiezaDAO.registrarNuevaPieza: " + e.getMessage());
            return false;
        }
    }
    public List<String> obtenerRazonSocialProveedores() {
        List<String> proveedores = new ArrayList<>();
        String sql = "SELECT nombre_razon_social FROM proveedor";

        try (Connection db = Conexion.getInstancia(); // Ajusta a tu clase de conexión (ej. Conexion.getConexion() si se llama diferente)
             PreparedStatement ps = db.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                proveedores.add(rs.getString("nombre_razon_social"));
            }
        } catch (SQLException e) {
            System.err.println("Error SQL en PiezaDAO.obtenerRazonSocialProveedores: " + e.getMessage());
        }
        return proveedores;
    }
}