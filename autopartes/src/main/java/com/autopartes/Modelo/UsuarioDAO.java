package com.autopartes.Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UsuarioDAO {
    public Usuario login(String user, String pass) {
        // Usamos 'users' que es la tabla que no tiene conflictos de archivos
        String sql = "SELECT IDusuario, username, rol FROM users WHERE username = ? AND password = ?";
        
        try (Connection db = Conexion.getInstancia();
             PreparedStatement ps = db.prepareStatement(sql)) {
            
            ps.setString(1, user);
            ps.setString(2, pass);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Usuario(
                    rs.getInt("IDusuario"),
                    rs.getString("username"),
                    rs.getString("rol")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error en el login: " + e.getMessage());
        }
        return null;
    }
}