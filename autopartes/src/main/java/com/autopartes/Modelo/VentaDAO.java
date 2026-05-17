package com.autopartes.Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DAO para la gestión transaccional de ventas (Tickets, ListaTicket, Caja).
 * 
 * RESPONSABILIDADES:
 * - Procesar ventas de manera atómica (ACID)
 * - Insertar tickets con detalles
 * - Actualizar inventario
 * - Registrar movimientos de caja
 * 
 * ESQUEMA REAL DE BD:
 * - ticket: Cabecera de venta (IDticket, Fecha, montoTotal, metodoPago, estado)
 * - lista_ticket: Detalles de venta (IDlista, IDpieza, id_prod_prov, IDticket, ...)
 * - piezas: Inventario (IDpieza, ..., stock, ...)
 * - caja: Movimientos de caja (IDcaja, IDticket, Presupuesto, Ingresos, Fecha, turno, IDusuario)
 */
public class VentaDAO {

    private static final Logger LOGGER = Logger.getLogger(VentaDAO.class.getName());

    /**
     * MÉTODO MAESTRO: Procesa una venta de manera atómica (ACID) incluyendo:
     * 1. Inserción de cabecera (ticket)
     * 2. Inserción de detalles (lista_ticket)
     * 3. Actualización de inventario (piezas)
     * 4. Registro de caja (si está pagado)
     *
     * @param ticket              Objeto con datos de fecha, monto, método de pago y estado
     * @param items               Lista de ItemCarrito con detalles de cada producto vendido
     * @param montoTotal          Monto total de la venta
     * @param turno               Turno del vendedor (Matutino/Vespertino)
     * @param IDusuarioOperador   ID del usuario que realiza la venta (Cajero/Vendedor)
     * @return true si la venta se procesó exitosamente, false si hubo error
     */
    public static boolean procesarVenta(Ticket ticket, List<ItemCarrito> items, 
                                       double montoTotal, String turno, int IDusuarioOperador) {
        
        Connection conexion = null;
        
        try {
            // PASO 0: Obtener conexión y desactivar auto-commit (inicia transacción)
            conexion = Conexion.getInstancia();
            conexion.setAutoCommit(false);
            
            LOGGER.log(Level.INFO, "Transacción iniciada. Auto-commit desactivado.");

            // ========================================================================
            // PASO 1: Insertar cabecera (TICKET)
            // ========================================================================
            int IDticketGenerado = insertarTicket(conexion, ticket, montoTotal);
            if (IDticketGenerado <= 0) {
                throw new SQLException("No se pudo generar el ID del ticket.");
            }
            LOGGER.log(Level.INFO, "Ticket insertado con ID: " + IDticketGenerado);

            // ========================================================================
            // PASO 2: Iterar e insertar detalles (LISTA_TICKET)
            // ========================================================================
            for (ItemCarrito item : items) {
                insertarDetalleTicket(conexion, IDticketGenerado, item);
            }
            LOGGER.log(Level.INFO, "Detalles insertados: " + items.size() + " líneas.");

            // ========================================================================
            // PASO 3: Actualizar inventario (PIEZAS)
            // ========================================================================
            for (ItemCarrito item : items) {
                actualizarInventario(conexion, item.getIDpieza(), item.getCantidad());
            }
            LOGGER.log(Level.INFO, "Inventario actualizado para " + items.size() + " productos.");

            // ========================================================================
            // PASO 4: Registrar flujo de caja (CAJA) si el estado es 'Pagado'
            // ========================================================================
            if ("Pagado".equalsIgnoreCase(ticket.getEstado())) {
                insertarMovimientoCaja(conexion, IDticketGenerado, montoTotal, turno, IDusuarioOperador);
                LOGGER.log(Level.INFO, "Movimiento de caja registrado.");
            }

            // ========================================================================
            // PASO 5: COMMIT
            // ========================================================================
            conexion.commit();
            LOGGER.log(Level.INFO, "Transacción confirmada (COMMIT).");
            return true;

        } catch (SQLException e) {
            // ROLLBACK en caso de error
            if (conexion != null) {
                try {
                    conexion.rollback();
                    LOGGER.log(Level.SEVERE, "Transacción revertida (ROLLBACK): " + e.getMessage());
                } catch (SQLException rollbackEx) {
                    LOGGER.log(Level.SEVERE, "Error durante ROLLBACK: " + rollbackEx.getMessage());
                }
            }
            LOGGER.log(Level.SEVERE, "Error procesando venta: " + e.getMessage());
            e.printStackTrace();
            return false;

        } finally {
            // Restaurar auto-commit y cerrar conexión
            if (conexion != null) {
                try {
                    conexion.setAutoCommit(true);
                    conexion.close();
                } catch (SQLException e) {
                    LOGGER.log(Level.WARNING, "Error al cerrar conexión: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Inserta la cabecera del ticket y retorna el ID auto-generado.
     * TABLA: ticket (IDticket, Fecha, montoTotal, metodoPago, estado)
     *
     * @param conexion Conexión activa de BD
     * @param ticket   Objeto Ticket con datos
     * @param montoTotal Monto total de la venta
     * @return ID del ticket insertado, o -1 si hay error
     */
    private static int insertarTicket(Connection conexion, Ticket ticket, double montoTotal) 
            throws SQLException {
        
        String sql = "INSERT INTO ticket (Fecha, montoTotal, metodoPago, estado) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement pst = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pst.setTimestamp(1, Timestamp.valueOf(ticket.getFecha()));
            pst.setDouble(2, montoTotal);
            pst.setString(3, ticket.getMetodoPago());
            pst.setString(4, ticket.getEstado());
            
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se insertó el ticket.");
            }
            
            // Recuperar el ID auto-generado
            try (ResultSet generatedKeys = pst.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            return -1;
        }
    }

    /**
     * Inserta una línea de detalle en lista_ticket.
     * TABLA: lista_ticket (IDlista, IDpieza, id_prod_prov, IDticket, cantidad, subtotal, ...)
     *
     * @param conexion Conexión activa de BD
     * @param IDticket ID del ticket cabecera
     * @param item     ItemCarrito con datos del producto
     */
    private static void insertarDetalleTicket(Connection conexion, int IDticket, ItemCarrito item) 
            throws SQLException {
        
        double subtotal = item.calcularSubtotal();
        double ivaCalculado = item.calcularIva();
        double totalLinea = item.calcularTotalLinea();
        
        String sql = "INSERT INTO lista_ticket " +
                     "(IDpieza, id_prod_prov, IDticket, cantidad, subtotal, precio_compra, " +
                     "utilidad_pct, iva_pct, precio_venta, total_linea) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = conexion.prepareStatement(sql)) {
            pst.setInt(1, item.getIDpieza());
            pst.setInt(2, item.getId_prod_prov());
            pst.setInt(3, IDticket);
            pst.setInt(4, item.getCantidad());
            pst.setDouble(5, subtotal);
            pst.setDouble(6, item.getPrecio_compra());
            pst.setDouble(7, item.getUtilidad_pct());
            pst.setDouble(8, item.getIva_pct());
            pst.setDouble(9, item.getPrecio_venta());
            pst.setDouble(10, totalLinea);
            
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se insertó el detalle del ticket para IDpieza: " + item.getIDpieza());
            }
        }
    }

    /**
     * Actualiza el stock en la tabla piezas (resta la cantidad vendida).
     * TABLA: piezas (IDpieza, ..., stock, ...)
     * OPERACIÓN: UPDATE piezas SET stock = stock - ? WHERE IDpieza = ?
     *
     * @param conexion Conexión activa de BD
     * @param IDpieza  ID de la pieza
     * @param cantidad Cantidad a restar
     */
    private static void actualizarInventario(Connection conexion, int IDpieza, int cantidad) 
            throws SQLException {
        
        String sql = "UPDATE piezas SET stock = stock - ? WHERE IDpieza = ?";
        
        try (PreparedStatement pst = conexion.prepareStatement(sql)) {
            pst.setInt(1, cantidad);
            pst.setInt(2, IDpieza);
            
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se actualizó el stock para IDpieza: " + IDpieza);
            }
        }
    }

    /**
     * Registra el movimiento de caja (ingreso por venta pagada).
     * TABLA: caja (IDcaja, IDticket, Presupuesto, Ingresos, Fecha, turno, IDusuario)
     * CONDICIÓN: Solo se registra si el estado del ticket es 'Pagado'
     *
     * @param conexion        Conexión activa de BD
     * @param IDticket        ID del ticket de venta
     * @param montoIngreso    Monto que ingresa a caja
     * @param turno           Turno (Matutino/Vespertino)
     * @param IDusuarioOperador ID del usuario que opera la caja
     */
    private static void insertarMovimientoCaja(Connection conexion, int IDticket, double montoIngreso,
                                              String turno, int IDusuarioOperador) 
            throws SQLException {
        
        String sql = "INSERT INTO caja (IDticket, Presupuesto, Ingresos, Fecha, turno, IDusuario) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        
        try (PreparedStatement pst = conexion.prepareStatement(sql)) {
            pst.setInt(1, IDticket);
            pst.setDouble(2, 0.0);  // Presupuesto inicial (puede ajustarse según reglas de negocio)
            pst.setDouble(3, montoIngreso);  // Ingresos
            pst.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
            pst.setString(5, turno);
            pst.setInt(6, IDusuarioOperador);
            
            int rowsAffected = pst.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("No se registró el movimiento de caja.");
            }
        }
    }
}
