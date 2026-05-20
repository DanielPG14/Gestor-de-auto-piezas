//Clase para manejar ops de piezas
package com.autopartes.Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PiezaDAO {
    /* 
    Metodos:
    - obtenerTodas(): Trae todas las piezas con su información completa, incluyendo proveedor y precio actual.
    - obtenerPiezas(pagina, tamanoPagina, idEstante, criterioBusqueda):
    - Trae piezas de forma paginada y permite filtrar por estante específico y criterio de búsqueda.
    - contarTotalPiezas(idEstante, criterioBusqueda): Cuenta cuántos registros totales cumplen el filtro para calcular las páginas reales.
    - obtenerRazonSocialProveedores(): Trae una lista de las razones sociales de los proveedores para llenar el ComboBox al agregar piezas.
    - registrarNuevaPieza(pieza): Inserta una nueva pieza en la base de datos, incluyendo su relación con el proveedor y el precio inicial.
    - registrarListaDePiezas(lista): Permite registrar una lista completa de piezas (usado para carga masiva desde Excel).
    - actualizarPieza(pieza): Actualiza la información de una pieza existente, incluyendo su precio (creando un nuevo registro en el historial de precios).
    */

    public List<Pieza> obtenerTodas() {
        List<Pieza> lista = new ArrayList<>();
        //SELECT con LEFT JOIN en tablas piezas, producto_proveedor, proveedor, historial_precio y estantes
        //Se uso left join para asegurar que se traigan todas las piezas incluso si no tienen proveedor o precio registrado
        //Inner join dio problemas al traer piezas sin proveedor asignado, lo que causaba que no se mostraran en la UI
        String sql = "SELECT p.*, pp.codigo_proveedor, prov.nombre_razon_social, "
                + "COALESCE(hp.precio_compra, 0.00) AS precio_compra, e.capMax "
                + "FROM piezas p "
                + "LEFT JOIN producto_proveedor pp ON p.IDpieza = pp.id_pieza "
                + "LEFT JOIN proveedor prov ON pp.id_proveedor = prov.id_proveedor "
                + "LEFT JOIN historial_precio hp ON pp.id_prod_prov = hp.id_prod_prov "
                + "AND hp.fecha_fin IS NULL "
                + "LEFT JOIN estantes e ON p.IDestante = e.IDestante";

        try (Connection db = Conexion.getInstancia();
                PreparedStatement ps = db.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            
            //while con rs.next() para iteraciones
            while (rs.next()) {
                // Manejo de posibles valores nulos para proveedor y código de proveedor
                String razonSocial = rs.getString("nombre_razon_social");
                if (razonSocial == null)
                    razonSocial = "Sin Proveedor";

                String codigoProv = rs.getString("codigo_proveedor");
                if (codigoProv == null)
                    codigoProv = "S/C";

                double precio = rs.getDouble("precio_compra");
                int idEstante = rs.getInt("IDestante");
                int capMax = rs.getInt("capMax");

                //Nueva pieza usando CC
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
    // específico y criterio de búsqueda.
    public List<Pieza> obtenerPiezas(int pagina, int tamanoPagina, int idEstante, String criterioBusqueda) {
        List<Pieza> lista = new ArrayList<>();
        //SELECT para filtrar por estante específico
        //Se diferencia con el anterior porque se agregan condiciones dinámicas para el filtro de búsqueda y estante, además de la paginación con LIMIT y OFFSET
        String sql = "SELECT p.*, pp.codigo_proveedor, prov.nombre_razon_social, "
                + "COALESCE(hp.precio_compra, 0.00) AS precio_compra, e.capMax "
                + "FROM piezas p "
                + "LEFT JOIN producto_proveedor pp ON p.IDpieza = pp.id_pieza "
                + "LEFT JOIN proveedor prov ON pp.id_proveedor = prov.id_proveedor "
                + "LEFT JOIN historial_precio hp ON pp.id_prod_prov = hp.id_prod_prov "
                + "AND hp.fecha_fin IS NULL "
                + "LEFT JOIN estantes e ON p.IDestante = e.IDestante ";
        //Condiciones dinámicas para el filtro de búsqueda y estante
        boolean tieneCriterio = criterioBusqueda != null && !criterioBusqueda.trim().isEmpty();
        if (tieneCriterio) {
            //Si hay criterio de búsqueda, se agrega una cláusula WHERE que filtra por nombre de pieza, código de proveedor o razón social del proveedor utilizando LIKE para coincidencias parciales
            sql += "WHERE (p.nombre LIKE ? OR pp.codigo_proveedor LIKE ? OR prov.nombre_razon_social LIKE ?) ";
            if (idEstante != -1) {
                //Si también se seleccionó un estante específico, se agrega una condición adicional para filtrar por el ID del estante
                sql += "AND p.IDestante = ? ";
            }
        } else if (idEstante != -1) {
            //Si no hay criterio de búsqueda pero sí se seleccionó un estante específico, se agrega una cláusula WHERE para filtrar solo por el ID del estante
            sql += "WHERE p.IDestante = ? ";
        }
        //Order by para ordenadar por ID de forma ascendente
        //limit para definir tamaño de paginacion
        //ofset para definir el punto de inicio de la paginación, calculado como (pagina - 1) * tamanoPagina para obtener el rango correcto de registros según la página actual
        sql += "ORDER BY p.IDpieza ASC LIMIT ? OFFSET ?";

        //Se ejecuta la consulta anterior
        try (Connection db = Conexion.getInstancia();
                PreparedStatement ps = db.prepareStatement(sql)) {

            int paramIdx = 1;
            if (tieneCriterio) {
                String buscado = "%" + criterioBusqueda.trim() + "%";
                ps.setString(paramIdx++, buscado);
                ps.setString(paramIdx++, buscado);
                ps.setString(paramIdx++, buscado);
                if (idEstante != -1) {
                    ps.setInt(paramIdx++, idEstante);
                }
            } else if (idEstante != -1) {
                ps.setInt(paramIdx++, idEstante);
            }

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
    
    //Metodos de sobrecarga para facilitar llamadas sin necesidad de pasar todos los parámetros si no se requieren filtros específicos
    //Sobrecarga: Si no se pasa un criterio de búsqueda, se llama al método principal con criterioBusqueda como null, lo que hace que se ignore el filtro de búsqueda y se traigan todas las piezas del estante seleccionado (o todas si no se seleccionó estante)
    // Sobrecarga para obtener piezas sin filtro de búsqueda (solo por estante)
    public List<Pieza> obtenerPiezas(int pagina, int tamanoPagina, int idEstante) {
        return obtenerPiezas(pagina, tamanoPagina, idEstante, null);
    }

    // Sobrecarga para obtener piezas sin filtro de búsqueda ni estante (todas las piezas paginadas)
    public int contarTotalPiezas(int idEstante) {
        return contarTotalPiezas(idEstante, null);
    }

    // NUEVO MÉTODO: Cuenta cuántos registros totales cumplen el filtro para
    // calcular las páginas reales
    public int contarTotalPiezas(int idEstante, String criterioBusqueda) {
        //COUNT para contar total de registros con ondiciones 
        //left joins para asegurar que se cuenten todas las piezas incluso si no tienen proveedor o precio registrado
        String sql = "SELECT COUNT(*) FROM piezas p "
                + "LEFT JOIN producto_proveedor pp ON p.IDpieza = pp.id_pieza "
                + "LEFT JOIN proveedor prov ON pp.id_proveedor = prov.id_proveedor ";

        boolean tieneCriterio = criterioBusqueda != null && !criterioBusqueda.trim().isEmpty();
        if (tieneCriterio) {
            sql += "WHERE (p.nombre LIKE ? OR pp.codigo_proveedor LIKE ? OR prov.nombre_razon_social LIKE ?) ";
            if (idEstante != -1) {
                sql += "AND p.IDestante = ? ";
            }
        } else if (idEstante != -1) {
            sql += "WHERE p.IDestante = ? ";
        }

        try (Connection db = Conexion.getInstancia();
                PreparedStatement ps = db.prepareStatement(sql)) {

            int paramIdx = 1;
            if (tieneCriterio) {
                String buscado = "%" + criterioBusqueda.trim() + "%";
                ps.setString(paramIdx++, buscado);
                ps.setString(paramIdx++, buscado);
                ps.setString(paramIdx++, buscado);
                if (idEstante != -1) {
                    ps.setInt(paramIdx++, idEstante);
                }
            } else if (idEstante != -1) {
                ps.setInt(paramIdx++, idEstante);
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

    //Lista de razones sociales de proveedores para llenar el ComboBox al agregar piezas
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

    //Registra una nueva pieza en la base de datos, incluyendo su relación con el proveedor y el precio inicial
    public boolean registrarNuevaPieza(Pieza pieza) {
        //INSET de la nueva pieza en la tabla piezas, obteniendo el ID generado automáticamente
        String sqlPieza = "INSERT INTO piezas (nombre, IDestante, nivelAsigned, stock, imagen) VALUES (?, ?, ?, ?, ?)";
        //SELECT para obtener el ID del proveedor a partir de la razón social seleccionada en el formulario
        String sqlBuscarProv = "SELECT id_proveedor FROM proveedor WHERE nombre_razon_social = ?";
        //INSERT para registrar la relación entre la pieza y el proveedor en la tabla intermedia producto_proveedor, obteniendo el ID generado automáticamente para luego registrar el precio inicial en el historial de precios
        String sqlIntermedia = "INSERT INTO producto_proveedor (id_proveedor, id_pieza, codigo_proveedor, activo) VALUES (?, ?, ?, 1)";
        //INSERT para registrar el precio inicial de la pieza en el historial de precios, vinculándolo al ID generado en la tabla intermedia producto_proveedor
        String sqlHistorial = "INSERT INTO historial_precio (id_prod_prov, precio_compra, fecha_inicio) VALUES (?, ?, NOW())";

        Connection db = null;
        //try para manejar la transaccion manuil y siendo atomica
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

    //Registra una lista completa de piezas 
    public boolean registrarListaDePiezas(List<Pieza> lista) {
        if (lista == null || lista.isEmpty()) {
            return false;
        }
        //INSERT para registrar cada pieza de la lista, utilizando batch para optimizar la inserción masiva
        //batch permite agrupar múltiples operaciones de inserción en una sola llamada a la base de datos, lo que mejora el rendimiento significativamente al reducir la sobrecarga de comunicación entre la aplicación y la base de datos
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

    //Actualiza la información de una pieza existente, incluyendo su precio (creando un nuevo registro en el historial de precios)
    //Lo usa el almacenista
    public boolean actualizarPieza(Pieza p) {
        //UPDATE para modificar la información de la pieza en la tabla piezas, utilizando el ID de la pieza para identificar el registro a actualizar
        String sqlPiezas = "UPDATE piezas SET nombre = ?, stock = ?, IDestante = ?, nivelAsigned = ? WHERE IDpieza = ?";
        //UPDATE para modificar el precio de compra en el historial de precios, creando un nuevo registro con la fecha de inicio actual y cerrando el registro anterior (si existe) con la fecha de fin actual
        String sqlHistorial = "UPDATE historial_precio hp " +
                "INNER JOIN producto_proveedor pp ON hp.id_prod_prov = pp.id_prod_prov " +
                "SET hp.precio_compra = ? " +
                "WHERE pp.id_pieza = ? AND hp.fecha_fin IS NULL";

        Connection db = null;
        try {
            db = Conexion.getInstancia();
            db.setAutoCommit(false);

            try (PreparedStatement psPiezas = db.prepareStatement(sqlPiezas);
                 PreparedStatement psHistorial = db.prepareStatement(sqlHistorial)) {

                psPiezas.setString(1, p.getNombre());
                psPiezas.setInt(2, p.getStock());
                psPiezas.setInt(3, Integer.parseInt(p.getIdEstante()));
                psPiezas.setInt(4, p.getNivelAsigned());
                psPiezas.setInt(5, p.getIdPieza());

                psHistorial.setDouble(1, p.getPrecioCompra());
                psHistorial.setInt(2, p.getIdPieza());

                int affectedPiezas = psPiezas.executeUpdate();
                int affectedHistorial = psHistorial.executeUpdate();

                if (affectedPiezas > 0 && affectedHistorial > 0) {
                    db.commit();
                    System.out.println("PiezaDAO.actualizarPieza -> commit exitoso. filasPiezas=" + affectedPiezas + ", filasHistorial=" + affectedHistorial);
                    return true;
                }

                db.rollback();
                System.err.println("PiezaDAO.actualizarPieza -> rollback porque al menos una actualización no afectó filas. filasPiezas=" + affectedPiezas + ", filasHistorial=" + affectedHistorial);
                return false;
            }
        } catch (SQLException e) {
            if (db != null) {
                try {
                    db.rollback();
                } catch (SQLException rollbackEx) {
                    System.err.println("PiezaDAO.actualizarPieza -> error en rollback: " + rollbackEx.getMessage());
                }
            }
            System.err.println("Error en actualizarPieza: " + e.getMessage());
            return false;
        } finally {
            if (db != null) {
                try {
                    db.setAutoCommit(true);
                    db.close();
                } catch (SQLException e) {
                    System.err.println("Error cerrando conexión en actualizarPieza: " + e.getMessage());
                }
            }
        }
    }
}