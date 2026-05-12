package com.autopartes.Modelo;

public class Usuario {
    private int idUsuario;
    private String username;
    private String rol;

    // Constructor completo para el DAO
    public Usuario(int idUsuario, String username, String rol) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.rol = rol;
    }

    // Getters
    public int getIdUsuario() { return idUsuario; }
    public String getUsername() { return username; }
    public String getRol() { return rol; }

    // Setters (opcionales, por si necesitas editar el perfil en tiempo de ejecución)
    public void setIdUsuario(int idUsuario) { this.idUsuario = idUsuario; }
    public void setUsername(String username) { this.username = username; }
    public void setRol(String rol) { this.rol = rol; }
    
    @Override
    public String toString() {
        return "Usuario: " + username + " | Rol: " + rol;
    }
}