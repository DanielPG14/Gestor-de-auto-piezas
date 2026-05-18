package com.autopartes.Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PiezaDAO {

    public List<Pieza> obtenerTodas() {
        List<Pieza> lista = new ArrayList<>();

        String sql = "SELECT p.IDpieza, p.nombre, prov.nombre_razon_social, pp.codigo_proveedor, "
                + "hp.precio_compra, p.imagen, p.IDestante, p.nivelAsigned, p.stock, e.capMax "
                + "FROM piezas p "
                + "LEFT JOIN estantes e ON p.IDestante = e.IDestante "
                + "LEFT JOIN producto_proveedor pp ON p.IDpieza = pp.id_pieza "
                + "LEFT JOIN proveedor prov ON pp.id_proveedor = prov.id_proveedor "
                + "LEFT JOIN historial_precio hp ON pp.id_prod_prov = hp.id_prod_prov "
                + "AND hp.fecha_fin IS NULL";

        try (Connection db = Conexion.getInstancia();
                PreparedStatement ps = db.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                String razonSocial = rs.getString("nombre_razon_social");
                if (razonSocial == null)
                    razonSocial = "Sin Proveedor";

                String codigoProv = rs.getString("codigo_proveedor");
                if (codigoProv == null)
                    codigoProv = "S/C";

                double precio = rs.getDouble("precio_compra");
                int idEstante = rs.getInt("IDestante");
                int capMax = rs.getInt("capMax");

                lista.add(new Pieza(
                        rs.getInt("IDpieza"),
                        rs.getString("nombre"),
                        razonSocial,
                        codigoProv,
                        precio,
                        rs.getString("imagen"),
                        String.valueOf(idEstante),
                        rs.getInt("nivelAsigned"),
                        rs.getInt("stock"),
                        capMax));
            }
            System.out.println("-> [DAO] Registros extraídos de la BD: " + lista.size());
        } catch (SQLException e) {
            System.err.println("Error en PiezaDAO.obtenerTodas: " + e.getMessage());
        }
        return lista;
    }

    // NUEVO MÉTODO: Trae piezas de forma paginada y permite filtrar por estante
    // específico
    public List<Pieza> obtenerPiezas(int pagina, int tamanoPagina, int idEstante) {
        List<Pieza> lista = new ArrayList<>();

        // Base de la consulta idéntica a la tuva
        String sql = "SELECT p.IDpieza, p.nombre, prov.nombre_razon_social, pp.codigo_proveedor, "
                + "hp.precio_compra, p.imagen, p.IDestante, p.nivelAsigned, p.stock, e.capMax "
                + "FROM piezas p "
                + "LEFT JOIN estantes e ON p.IDestante = e.IDestante "
                + "LEFT JOIN producto_proveedor pp ON p.IDpieza = pp.id_pieza "
                + "LEFT JOIN proveedor prov ON pp.id_proveedor = prov.id_proveedor "
                + "LEFT JOIN historial_precio hp ON pp.id_prod_prov = hp.id_prod_prov "
                + "AND hp.fecha_fin IS NULL ";

        // Agregar filtro condicional si no se seleccionó "Todos (-1)"
        if (idEstante != -1) {
            sql += "WHERE p.IDestante = ? ";
        }

        // Añadir ordenamiento lógico y límites para la paginación SQL
        sql += "ORDER BY p.IDpieza ASC LIMIT ? OFFSET ?";

        try (Connection db = Conexion.getInstancia();
                PreparedStatement ps = db.prepareStatement(sql)) {

            int paramIdx = 1;
            if (idEstante != -1) {
                ps.setInt(paramIdx++, idEstante);
            }

            // Configurar tamaño del bloque y salto de registros (OFFSET)
            ps.setInt(paramIdx++, tamanoPagina);
            ps.setInt(paramIdx++, (pagina - 1) * tamanoPagina);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String razonSocial = rs.getString("nombre_razon_social");
                    if (razonSocial == null)
                        razonSocial = "Sin Proveedor";

                    String codigoProv = rs.getString("codigo_proveedor");
                    if (codigoProv == null)
                        codigoProv = "S/C";

                    lista.add(new Pieza(
                            rs.getInt("IDpieza"),
                            rs.getString("nombre"),
                            razonSocial,
                            codigoProv,
                            rs.getDouble("precio_compra"),
                            rs.getString("imagen"),
                            String.valueOf(rs.getInt("IDestante")),
                            rs.getInt("nivelAsigned"),
                            rs.getInt("stock"),
                            rs.getInt("capMax")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en PiezaDAO.obtenerPiezas: " + e.getMessage());
        }
        return lista;
    }

    // NUEVO MÉTODO: Cuenta cuántos registros totales cumplen el filtro para
    // calcular las páginas reales
    public int contarTotalPiezas(int idEstante) {
        String sql = "SELECT COUNT(*) FROM piezas";
        if (idEstante != -1) {
            sql += " WHERE IDestante = ?";
        }

        try (Connection db = Conexion.getInstancia();
                PreparedStatement ps = db.prepareStatement(sql)) {

            if (idEstante != -1) {
                ps.setInt(1, idEstante);
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error en PiezaDAO.contarTotalPiezas: " + e.getMessage());
        }
        return 0;
    }

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

    public boolean registrarNuevaPieza(Pieza pieza) {
        String sqlPieza = "INSERT INTO piezas (nombre, IDestante, nivelAsigned, stock, imagen) VALUES (?, ?, ?, ?, ?)";
        String sqlBuscarProv = "SELECT id_proveedor FROM proveedor WHERE nombre_razon_social = ?";
        String sqlIntermedia = "INSERT INTO producto_proveedor (id_proveedor, id_pieza, codigo_proveedor, activo) VALUES (?, ?, ?, 1)";
        String sqlHistorial = "INSERT INTO historial_precio (id_prod_prov, precio_compra, fecha_inicio) VALUES (?, ?, NOW())";

        Connection db = null;
        try {
            db = Conexion.getInstancia();
            db.setAutoCommit(false); // Iniciamos transacción manual

            int idPiezaGenerado = 0;
            try (PreparedStatement psPieza = db.prepareStatement(sqlPieza, PreparedStatement.RETURN_GENERATED_KEYS)) {
                psPieza.setString(1, pieza.getNombre());
                psPieza.setInt(2, Integer.parseInt(pieza.getIdEstante()));
                psPieza.setInt(3, pieza.getNivelAsigned());
                psPieza.setInt(4, pieza.getStock());
                psPieza.setString(5, pieza.getImagen() != null ? pieza.getImagen() : "default.jpg");
                psPieza.executeUpdate();

                try (ResultSet rsKeys = psPieza.getGeneratedKeys()) {
                    if (rsKeys.next())
                        idPiezaGenerado = rsKeys.getInt(1);
                }
            }

            if (idPiezaGenerado == 0)
                throw new SQLException("Fallo al obtener el ID de la pieza generada.");

            int idProveedor = 0;
            try (PreparedStatement psProv = db.prepareStatement(sqlBuscarProv)) {
                psProv.setString(1, pieza.getRazonSocialProveedor());
                try (ResultSet rsProv = psProv.executeQuery()) {
                    if (rsProv.next())
                        idProveedor = rsProv.getInt("id_proveedor");
                }
            }

            if (idProveedor == 0)
                throw new SQLException("Proveedor no encontrado en el sistema.");

            int idProdProvGenerado = 0;
            try (PreparedStatement psInter = db.prepareStatement(sqlIntermedia,
                    PreparedStatement.RETURN_GENERATED_KEYS)) {
                psInter.setInt(1, idProveedor);
                psInter.setInt(2, idPiezaGenerado);
                psInter.setString(3, pieza.getCodigoProveedor());
                psInter.executeUpdate();

                try (ResultSet rsInterKeys = psInter.getGeneratedKeys()) {
                    if (rsInterKeys.next())
                        idProdProvGenerado = rsInterKeys.getInt(1);
                }
            }

            try (PreparedStatement psHist = db.prepareStatement(sqlHistorial)) {
                psHist.setInt(1, idProdProvGenerado);
                psHist.setDouble(2, pieza.getPrecioCompra());
                psHist.executeUpdate();
            }

            db.commit(); // Si todo salió bien, guardamos cambios permanentemente
            return true;

        } catch (Exception e) {
            // El rollback SOLO debe ejecutarse si ocurrió un error real
            if (db != null) {
                try {
                    db.rollback();
                    System.err.println("-> [BD] Transacción revertida debido a un error: " + e.getMessage());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            e.printStackTrace();
            return false;
        } finally {
            // El finally únicamente se encarga de limpiar el estado y CERRAR la conexión
            if (db != null) {
                try {
                    db.close(); // Directo al cierre para liberar los sockets y liberar bloqueos
                    System.out.println("-> [BD] Conexión de registro cerrada correctamente.");
                } catch (SQLException e) {
                    System.err.println("-> [ERROR] No se pudo cerrar la conexión en el finally: " + e.getMessage());
                }
            }
        }
    }

    public boolean registrarListaDePiezas(List<Pieza> lista) {
        if (lista == null || lista.isEmpty()) {
            return false;
        }

        String sql = "INSERT INTO piezas (nombre, IDestante, nivelAsigned, stock, imagen) VALUES (?, ?, ?, ?, ?)";
        Connection db = null;
        try {
            db = Conexion.getInstancia();
            db.setAutoCommit(false);

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                for (Pieza pieza : lista) {
                    ps.setString(1, pieza.getNombre());
                    ps.setInt(2, Integer.parseInt(pieza.getIdEstante()));
                    ps.setInt(3, pieza.getNivelAsigned());
                    ps.setInt(4, pieza.getStock());
                    ps.setString(5, pieza.getImagen() != null ? pieza.getImagen() : "default.jpg");
                    ps.addBatch();
                }

                ps.executeBatch();
            }

            db.commit();
            return true;
        } catch (SQLException e) {
            if (db != null) {
                try {
                    db.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("Error al revertir la carga masiva: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error en registrarListaDePiezas: " + e.getMessage());
            return false;
        } finally {
            if (db != null) {
                try {
                    db.setAutoCommit(true);
                    db.close();
                } catch (SQLException e) {
                    System.err.println("Error cerrando conexión tras carga masiva: " + e.getMessage());
                }
            }
        }
    }

    public boolean actualizarPieza(Pieza p) {
        String sql = "UPDATE piezas SET nombre = ?, stock = ?, IDestante = ?, nivelAsigned = ? WHERE IDpieza = ?";
        try (Connection db = Conexion.getInstancia();
                PreparedStatement ps = db.prepareStatement(sql)) {

            ps.setString(1, p.getNombre());
            ps.setInt(2, p.getStock());
            ps.setInt(3, Integer.parseInt(p.getIdEstante()));
            ps.setInt(4, p.getNivelAsigned());
            ps.setInt(5, p.getIdPieza());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error en actualizarPieza: " + e.getMessage());
            return false;
        }
    }
}