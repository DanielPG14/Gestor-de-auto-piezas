package com.autopartes.Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class Transacciones {
    public boolean actualizarStock(int idPieza, int cantidad, boolean esEntrada) {
    // Si es entrada se suma (+), si es salida (venta) se resta (-)
    String sql = "UPDATE Piezas SET PrecioActual = PrecioActual WHERE IDpieza = ?"; 
    // Nota: Aquí deberías tener una columna 'stock' en la tabla Piezas. 
    // Si no la tienes, ¡hay que agregarla!
    
    String sqlStock = "UPDATE Piezas SET stock = stock + ? WHERE IDpieza = ?";
    
    try (Connection db = Conexion.getInstancia();
         PreparedStatement ps = db.prepareStatement(sqlStock)) {
        
        ps.setInt(1, esEntrada ? cantidad : -cantidad);
        ps.setInt(2, idPieza);
        
        return ps.executeUpdate() > 0;
    } catch (SQLException e) {
        e.printStackTrace();
        return false;
    }
}
}
