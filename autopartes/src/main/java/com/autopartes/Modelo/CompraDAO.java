//DAO: data access object para la entidad "Compra".
//Encapsula toda la lógica de acceso a datos relacionada con las compras,
package com.autopartes.Modelo;

import com.autopartes.Controlador.RegistroCompraC;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
/*
Metodos:
- registrarCompra: Registra una nueva compra, actualizando el stock y el historial de precios.
- obtenerUltimasCompras: Devuelve una lista de las últimas compras realizadas, con detalles
- verificarOInsertarProductoProveedor: Verifica si existe la relación producto-proveedor y la inserta si no existe.
- cerrarHistorialPrecioAnterior: Cierra el registro de precio anterior para un producto-proveedor.
- insertarHistorialPrecio: Inserta un nuevo registro de precio para un producto-proveedor.
*/
public class CompraDAO {
    //Referencia a la conexión de base de datos
    private Connection db;

    public CompraDAO() {
        this.db = Conexion.getInstancia();
    }

    public int registrarCompra(int idProducto, int idProveedor, String codigoProveedor,
                               double precioCompra, int cantidad) {
        int idCompra = -1;

        try {
            db.setAutoCommit(false);

            int idProdProv = verificarOInsertarProductoProveedor(idProducto, idProveedor, codigoProveedor);
            if (idProdProv <= 0) {
                throw new SQLException("No se pudo establecer relación producto-proveedor.");
            }

            cerrarHistorialPrecioAnterior(idProdProv);

            boolean precioInsertado = insertarHistorialPrecio(idProdProv, precioCompra);
            if (!precioInsertado) {
                throw new SQLException("No se pudo registrar el nuevo precio.");
            }

            boolean stockActualizado = incrementarStock(idProducto, cantidad);
            if (!stockActualizado) {
                throw new SQLException("No se pudo actualizar el stock.");
            }

            idCompra = insertarCompra(idProducto, idProveedor, precioCompra, cantidad);
            if (idCompra <= 0) {
                throw new SQLException("No se pudo registrar la compra.");
            }

            db.commit();
            System.out.println("Compra " + idCompra + " registrada exitosamente.");

        } catch (SQLException e) {
            System.err.println("Error durante transacción de compra: " + e.getMessage());
            e.printStackTrace();

            try {
                if (db != null && !db.isClosed()) {
                    db.rollback();
                    System.out.println("Transacción de compra revertida (ROLLBACK).");
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

    private int verificarOInsertarProductoProveedor(int idProducto, int idProveedor, String codigoProveedor) {
        int idProdProv = -1;

        try {
            //SELECT para verificar si ya existe la relación producto-proveedor
            String sqlBuscar = "SELECT id_prod_prov FROM producto_proveedor " +
                    "WHERE id_producto = ? AND id_proveedor = ? LIMIT 1";

            //Busca la relación producto-proveedor existente y obtiene su ID de proveedor
            try (PreparedStatement psBuscar = db.prepareStatement(sqlBuscar)) {
                psBuscar.setInt(1, idProducto); // Establece el ID del producto
                psBuscar.setInt(2, idProveedor); // Establece el ID del proveedor

                try (ResultSet rs = psBuscar.executeQuery()) {
                    if (rs.next()) {
                        idProdProv = rs.getInt("id_prod_prov");
                        return idProdProv;
                    }
                }
            }

            // Si no existe, inserta la nueva relación producto-proveedor y obtiene su ID generado
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
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en verificarOInsertarProductoProveedor: " + e.getMessage());
            e.printStackTrace();
        }

        return idProdProv;
    }

    private void cerrarHistorialPrecioAnterior(int idProdProv) {
        try {
            //UPDATE para cerrar el historial de precio anterior estableciendo la fecha de fin a NOW()
            String sql = "UPDATE historial_precio SET fecha_fin = NOW() " +
                    "WHERE id_prod_prov = ? AND fecha_fin IS NULL";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, idProdProv);
                ps.executeUpdate();
            }

        } catch (SQLException e) {
            System.err.println("Error en cerrarHistorialPrecioAnterior: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean insertarHistorialPrecio(int idProdProv, double precioCompra) {
        try {
            //INSERT para registrar el nuevo precio de compra en el historial de precios
            String sql = "INSERT INTO historial_precio (id_prod_prov, precio_compra, fecha_inicio, fecha_fin) " +
                    "VALUES (?, ?, NOW(), NULL)";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, idProdProv);
                ps.setDouble(2, precioCompra);
                ps.executeUpdate();

                return true;
            }

        } catch (SQLException e) {
            System.err.println("Error en insertarHistorialPrecio: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private boolean incrementarStock(int idPieza, int cantidad) {
        try {
            //UPDATE para incrementar el stock de la pieza comprada sumando la cantidad adquirida
            String sql = "UPDATE piezas SET stock = stock + ? WHERE IDpieza = ?";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, cantidad);
                ps.setInt(2, idPieza);

                int filasActualizadas = ps.executeUpdate();
                if (filasActualizadas > 0) {
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en incrementarStock: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    private int insertarCompra(int idProducto, int idProveedor, double precioCompra, int cantidad) {
        int idCompra = -1;

        try {
            //INSERT para registrar la compra en la tabla de compras, incluyendo el cálculo del total (precio * cantidad)
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
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("Error en insertarCompra: " + e.getMessage());
            e.printStackTrace();
        }

        return idCompra;
    }

    public List<RegistroCompraC.CompraItem> obtenerUltimasCompras(int limite) {
        List<RegistroCompraC.CompraItem> compras = new ArrayList<>();

        try {
            //SELECT para obtener las últimas compras realizadas, incluyendo detalles como el nombre de la pieza, 
            //el proveedor, el código del proveedor, el precio de compra, la cantidad y la fecha de compra
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
                    while (rs.next()) { // Extrae los datos de cada compra y los agrega a la lista de compras
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

            System.out.println("" + compras.size() + " compras cargadas.");

        } catch (SQLException e) {
            System.err.println("Error en obtenerUltimasCompras: " + e.getMessage());
            e.printStackTrace();
        }

        return compras;
    }
}
