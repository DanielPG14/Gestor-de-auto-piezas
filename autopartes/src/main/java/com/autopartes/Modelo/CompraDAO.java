package com.autopartes.Modelo;

import com.autopartes.Controlador.RegistroCompraC;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * CompraDAO: Data Access Object para gestionar transacciones de compra (abastecimiento).
 * Maneja:
 * - Inserción de compras (cabecera)
 * - Gestión de relaciones producto-proveedor
 * - Actualización de historial de precios (3FN)
 * - Incremento de stock
 * - Control transaccional (COMMIT/ROLLBACK)
 */
public class CompraDAO {

    private Connection db;

    /**
     * Constructor: Obtiene la instancia de conexión a BD.
     */
    public CompraDAO() {
        this.db = Conexion.getInstancia();
    }

    /**
     * Registra una compra completa en una transacción.
     * Pasos:
     * A - Verificar/crear relación en tabla producto_proveedor
     * B - Actualizar historial_precio (cierra vigencia anterior)
     * C - Insertar nueva entrada de precio
     * D - Incrementar stock de la pieza
     * E - Registrar cabecera de compra
     *
     * @param idProducto   ID de la pieza
     * @param idProveedor  ID del proveedor
     * @param codigoProveedor Código asignado por el proveedor
     * @param precioCompra Costo de adquisición
     * @param cantidad     Cantidad de unidades
     * @return ID de compra si es exitoso, -1 si falla
     */
    public int registrarCompra(int idProducto, int idProveedor, String codigoProveedor,
                               double precioCompra, int cantidad) {
        int idCompra = -1;

        try {
            db.setAutoCommit(false);

            // === PASO A: Verificar/crear relación producto-proveedor ===
            int idProdProv = verificarOInsertarProductoProveedor(idProducto, idProveedor, codigoProveedor);
            if (idProdProv <= 0) {
                throw new SQLException("No se pudo establecer relación producto-proveedor.");
            }

            // === PASO B: Cerrar vigencia del precio anterior ===
            cerrarHistorialPrecioAnterior(idProdProv);

            // === PASO C: Insertar nuevo precio en historial ===
            boolean precioInsertado = insertarHistorialPrecio(idProdProv, precioCompra);
            if (!precioInsertado) {
                throw new SQLException("No se pudo registrar el nuevo precio.");
            }

            // === PASO D: Incrementar stock ===
            boolean stockActualizado = incrementarStock(idProducto, cantidad);
            if (!stockActualizado) {
                throw new SQLException("No se pudo actualizar el stock.");
            }

            // === PASO E: Registrar compra (cabecera) ===
            idCompra = insertarCompra(idProducto, idProveedor, precioCompra, cantidad);
            if (idCompra <= 0) {
                throw new SQLException("No se pudo registrar la compra.");
            }

            // COMMIT exitoso
            db.commit();
            System.out.println("✓ Compra " + idCompra + " registrada exitosamente.");

        } catch (SQLException e) {
            System.err.println("✗ Error durante transacción de compra: " + e.getMessage());
            e.printStackTrace();

            try {
                if (db != null && !db.isClosed()) {
                    db.rollback();
                    System.out.println("✓ Transacción de compra revertida (ROLLBACK).");
                }
            } catch (SQLException ex) {
                System.err.println("Error al hacer rollback: " + ex.getMessage());
            }

            idCompra = -1;

        } finally {
            try {
                if (db != null && !db.isClosed()) {
                    db.setAutoCommit(true);
                }
            } catch (SQLException e) {
                System.err.println("Error al restaurar auto-commit: " + e.getMessage());
            }
        }

        return idCompra;
    }

    /**
     * PASO A: Verifica si existe relación producto-proveedor.
     * Si no existe, la crea e inserta el código específico.
     *
     * @return ID de la relación (id_prod_prov), -1 si falla
     */
    private int verificarOInsertarProductoProveedor(int idProducto, int idProveedor, String codigoProveedor) {
        int idProdProv = -1;

        try {
            // Buscar relación existente
            String sqlBuscar = "SELECT id_prod_prov FROM producto_proveedor " +
                    "WHERE id_producto = ? AND id_proveedor = ? LIMIT 1";

            try (PreparedStatement psBuscar = db.prepareStatement(sqlBuscar)) {
                psBuscar.setInt(1, idProducto);
                psBuscar.setInt(2, idProveedor);

                try (ResultSet rs = psBuscar.executeQuery()) {
                    if (rs.next()) {
                        idProdProv = rs.getInt("id_prod_prov");
                        System.out.println("Relación encontrada: id_prod_prov=" + idProdProv);
                        return idProdProv;
                    }
                }
            }

            // Si no existe, crear nueva relación
            String sqlInsertar = "INSERT INTO producto_proveedor (id_producto, id_proveedor, codigo_proveedor) " +
                    "VALUES (?, ?, ?)";

            try (PreparedStatement psInsertar = db.prepareStatement(sqlInsertar, Statement.RETURN_GENERATED_KEYS)) {
                psInsertar.setInt(1, idProducto);
                psInsertar.setInt(2, idProveedor);
                psInsertar.setString(3, codigoProveedor);
                psInsertar.executeUpdate();

                try (ResultSet rsGenerated = psInsertar.getGeneratedKeys()) {
                    if (rsGenerated.next()) {
                        idProdProv = rsGenerated.getInt(1);
                        System.out.println("Relación creada: id_prod_prov=" + idProdProv);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en verificarOInsertarProductoProveedor: " + e.getMessage());
            e.printStackTrace();
        }

        return idProdProv;
    }

    /**
     * PASO B: Cierra la vigencia del precio anterior en historial_precio.
     * Actualiza fecha_fin = NOW() para el registro activo.
     */
    private void cerrarHistorialPrecioAnterior(int idProdProv) {
        try {
            String sql = "UPDATE historial_precio SET fecha_fin = NOW() " +
                    "WHERE id_prod_prov = ? AND fecha_fin IS NULL";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, idProdProv);
                ps.executeUpdate();
                System.out.println("Precio anterior cerrado para id_prod_prov=" + idProdProv);
            }

        } catch (SQLException e) {
            System.err.println("Error en cerrarHistorialPrecioAnterior: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * PASO C: Inserta nueva entrada en historial_precio.
     * Registra el precio vigente con fecha_inicio = NOW() y fecha_fin = NULL.
     *
     * @return true si es exitoso, false si falla
     */
    private boolean insertarHistorialPrecio(int idProdProv, double precioCompra) {
        try {
            String sql = "INSERT INTO historial_precio (id_prod_prov, precio_compra, fecha_inicio, fecha_fin) " +
                    "VALUES (?, ?, NOW(), NULL)";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, idProdProv);
                ps.setDouble(2, precioCompra);
                ps.executeUpdate();

                System.out.println("Precio registrado en historial: $" + String.format("%.2f", precioCompra));
                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error en insertarHistorialPrecio: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * PASO D: Incrementa el stock de una pieza.
     * UPDATE piezas SET stock = stock + cantidad WHERE IDpieza = ?
     *
     * @return true si es exitoso, false si falla
     */
    private boolean incrementarStock(int idPieza, int cantidad) {
        try {
            String sql = "UPDATE piezas SET stock = stock + ? WHERE IDpieza = ?";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, cantidad);
                ps.setInt(2, idPieza);

                int filasActualizadas = ps.executeUpdate();
                if (filasActualizadas > 0) {
                    System.out.println("Stock incrementado: +" + cantidad + " unidades para IDpieza=" + idPieza);
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en incrementarStock: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * PASO E: Inserta la cabecera de la compra en tabla compra.
     *
     * @return ID de la compra generada, -1 si falla
     */
    private int insertarCompra(int idProducto, int idProveedor, double precioCompra, int cantidad) {
        int idCompra = -1;

        try {
            String sql = "INSERT INTO compra (id_producto, id_proveedor, fecha, precio_compra, cantidad, total) " +
                    "VALUES (?, ?, NOW(), ?, ?, ?)";

            try (PreparedStatement ps = db.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setInt(1, idProducto);
                ps.setInt(2, idProveedor);
                ps.setDouble(3, precioCompra);
                ps.setInt(4, cantidad);
                ps.setDouble(5, precioCompra * cantidad);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idCompra = rs.getInt(1);
                        System.out.println("Compra insertada: ID=" + idCompra);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en insertarCompra: " + e.getMessage());
            e.printStackTrace();
        }

        return idCompra;
    }

    /**
     * Obtiene las últimas compras registradas (límite configurable).
     * Útil para el historial en RegistroCompraC.
     *
     * @param limite Número máximo de registros a traer
     * @return Lista de CompraItem
     */
    public List<RegistroCompraC.CompraItem> obtenerUltimasCompras(int limite) {
        List<RegistroCompraC.CompraItem> compras = new ArrayList<>();

        try {
            String sql = "SELECT p.nombre, prov.razon_social, pp.codigo_proveedor, " +
                    "c.precio_compra, c.cantidad, c.fecha " +
                    "FROM compra c " +
                    "INNER JOIN piezas p ON c.id_producto = p.IDpieza " +
                    "INNER JOIN proveedores prov ON c.id_proveedor = prov.id_proveedor " +
                    "INNER JOIN producto_proveedor pp ON c.id_producto = pp.id_producto " +
                    "AND c.id_proveedor = pp.id_proveedor " +
                    "ORDER BY c.fecha DESC " +
                    "LIMIT ?";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, limite);

                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String pieza = rs.getString("nombre");
                        String proveedor = rs.getString("razon_social");
                        String codigo = rs.getString("codigo_proveedor");
                        double precio = rs.getDouble("precio_compra");
                        int cantidad = rs.getInt("cantidad");
                        String fecha = rs.getString("fecha");

                        compras.add(new RegistroCompraC.CompraItem(pieza, proveedor, codigo, precio, cantidad, fecha));
                    }
                }
            }

            System.out.println("✓ " + compras.size() + " compras cargadas.");

        } catch (SQLException e) {
            System.err.println("✗ Error en obtenerUltimasCompras: " + e.getMessage());
            e.printStackTrace();
        }

        return compras;
    }
}
