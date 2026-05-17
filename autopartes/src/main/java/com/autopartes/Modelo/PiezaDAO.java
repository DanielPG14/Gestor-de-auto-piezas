package com.autopartes.Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
// Importa tu clase de conexión correspondiente (Ajusta el paquete si cambia)
// import com.autopartes.Utilidades.Conexion;

public class PiezaDAO {

    /**
     * TAREA 3: Obtiene todas las piezas mapeadas mediante un INNER JOIN multifactorial
     * que une los datos de la pieza, el proveedor y su precio histórico vigente.
     */
    public List<Pieza> obtenerTodas() {
        List<Pieza> lista = new ArrayList<>();
        // Query adaptado a la arquitectura de precios históricos dependientes de proveedor y fecha
        String sql = "SELECT p.IDpieza, p.nombre, prov.nombre_razon_social, pp.codigo_proveedor, "
                   + "hp.precio_compra, p.imagen, p.IDestante, p.nivelAsigned, p.stock, e.capMax "
                   + "FROM piezas p "
                   + "INNER JOIN estantes e ON p.IDestante = e.IDestante "
                   + "INNER JOIN producto_proveedor pp ON p.IDpieza = pp.id_producto "
                   + "INNER JOIN proveedor prov ON pp.id_proveedor = prov.id_proveedor "
                   + "INNER JOIN historial_precio hp ON pp.id_prod_prov = hp.id_prod_prov "
                   + "WHERE hp.fecha_fin IS NULL"; // Extrae solo el precio vigente actual

        try (Connection db = Conexion.getInstancia(); // Cambiar por tu método de conexión (ej. Conexion.getConexion())
             PreparedStatement ps = db.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Pieza(
                        rs.getInt("IDpieza"),
                        rs.getString("nombre"),
                        rs.getString("nombre_razon_social"),
                        rs.getString("codigo_proveedor"),
                        rs.getDouble("precio_compra"),
                        rs.getString("imagen"),
                        String.valueOf(rs.getInt("IDestante")),
                        rs.getInt("nivelAsigned"),
                        rs.getInt("stock"),
                        rs.getInt("capMax")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error en PiezaDAO.obtenerTodas: " + e.getMessage());
        }
        return lista;
    }

    /**
     * TAREA 4 (Paso A): Recupera los nombres de los proveedores para rellenar el ComboBox del formulario.
     */
    public List<String> obtenerRazonSocialProveedores() {
        List<String> proveedores = new ArrayList<>();
        String sql = "SELECT nombre_razon_social FROM proveedor";

        try (Connection db = Conexion.getInstancia();
             PreparedStatement ps = db.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                proveedores.add(rs.getString("nombre_razon_social"));
            }
        } catch (SQLException e) {
            System.err.println("Error en PiezaDAO.obtenerRazonSocialProveedores: " + e.getMessage());
        }
        return proveedores;
    }

    /**
     * TAREA 4 (Paso B): Registra una nueva pieza de forma transaccional.
     * Inserta la pieza, asocia el proveedor intermedio y genera el registro en el historial de precios.
     */
    public boolean registrarNuevaPieza(Pieza pieza) {
        String sqlPieza = "INSERT INTO piezas (nombre, IDestante, nivelAsigned, stock, imagen) VALUES (?, ?, ?, ?, ?)";
        String sqlBuscarProv = "SELECT id_proveedor FROM proveedor WHERE nombre_razon_social = ?";
        String sqlIntermedia = "INSERT INTO producto_proveedor (id_proveedor, id_producto, codigo_proveedor, activo) VALUES (?, ?, ?, 1)";
        String sqlHistorial = "INSERT INTO historial_precio (id_prod_prov, precio_compra, fecha_inicio) VALUES (?, ?, NOW())";

        Connection db = null;
        try {
            db = Conexion.getInstancia();
            db.setAutoCommit(false); // 👈 INICIA LA TRANSACCIÓN CONTROLADA

            // 1. Insertar la pieza base
            int idPiezaGenerado = 0;
            try (PreparedStatement psPieza = db.prepareStatement(sqlPieza, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psPieza.setString(1, pieza.getNombre());
                psPieza.setInt(2, Integer.parseInt(pieza.getIdEstante()));
                psPieza.setInt(3, pieza.getNivelAsigned());
                psPieza.setInt(4, pieza.getStock());
                psPieza.setString(5, pieza.getImagen() != null ? pieza.getImagen() : "default.jpg");
                psPieza.executeUpdate();

                try (ResultSet rsKeys = psPieza.getGeneratedKeys()) {
                    if (rsKeys.next()) idPiezaGenerado = rsKeys.getInt(1);
                }
            }

            if (idPiezaGenerado == 0) throw new SQLException("Fallo al obtener el ID de la pieza generada.");

            // 2. Buscar el ID del proveedor según la Razón Social seleccionada
            int idProveedor = 0;
            try (PreparedStatement psProv = db.prepareStatement(sqlBuscarProv)) {
                psProv.setString(1, pieza.getRazonSocialProveedor());
                try (ResultSet rsProv = psProv.executeQuery()) {
                    if (rsProv.next()) idProveedor = rsProv.getInt("id_proveedor");
                }
            }

            if (idProveedor == 0) throw new SQLException("Proveedor no encontrado en el sistema.");

            // 3. Crear el vínculo en la entidad intermedia 'producto_proveedor'
            int idProdProvGenerado = 0;
            try (PreparedStatement psInter = db.prepareStatement(sqlIntermedia, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psInter.setInt(1, idProveedor);
                psInter.setInt(2, idPiezaGenerado);
                psInter.setString(3, pieza.getCodigoProveedor());
                psInter.executeUpdate();

                try (ResultSet rsInterKeys = psInter.getGeneratedKeys()) {
                    if (rsInterKeys.next()) idProdProvGenerado = rsInterKeys.getInt(1);
                }
            }

            //Registrar el precio de costo inicial en el historial dinámico
            try (PreparedStatement psHist = db.prepareStatement(sqlHistorial)) {
                psHist.setInt(1, idProdProvGenerado);
                psHist.setDouble(2, pieza.getPrecioCompra());
                psHist.executeUpdate();
            }

            db.commit(); 
            return true;

        } catch (Exception e) {
            if (db != null) {
                try {
                    db.rollback(); 
                    System.err.println("Transacción revertida debido a un error: " + e.getMessage());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (db != null) {
                try {
                    db.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}