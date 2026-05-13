package com.autopartes.Modelo;

import java.sql.*;

public class UsuarioDAO {
    public Usuario login(String user, String pass) {
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
            e.printStackTrace();
        }
        return null;
    }
}