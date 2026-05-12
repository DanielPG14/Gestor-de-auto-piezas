package com.autopartes.Modelo;

import java.sql.*;

public class Transacciones {

    public boolean realizarOperacion(int idPieza, int cantidad, boolean esEntrada, int idUsuario) {
        Connection db = null;
        try {
            db = Conexion.getInstancia();
            db.setAutoCommit(false); // Iniciamos transacción atómica

            // 1. Actualizar el Stock en la tabla Piezas
            String sqlStock = "UPDATE Piezas SET stock = stock + ? WHERE IDpieza = ?";
            try (PreparedStatement psStock = db.prepareStatement(sqlStock)) {
                psStock.setInt(1, esEntrada ? cantidad : -cantidad);
                psStock.setInt(2, idPieza);
                psStock.executeUpdate();
            }

            // 2. Registrar en la tabla Caja (vinculando al usuario)
            // Asumiendo que 'caja' tiene: IDpieza, IDusuario, fecha, tipo_movimiento
            String sqlCaja = "INSERT INTO caja (IDpieza, IDusuario, fecha) VALUES (?, ?, NOW())";
            try (PreparedStatement psCaja = db.prepareStatement(sqlCaja)) {
                psCaja.setInt(1, idPieza);
                psCaja.setInt(2, idUsuario);
                psCaja.executeUpdate();
            }

            db.commit(); // Si todo sale bien, guardamos cambios
            return true;

        } catch (SQLException e) {
            if (db != null) {
                try { db.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            e.printStackTrace();
            return false;
        } finally {
            if (db != null) {
                try { db.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}