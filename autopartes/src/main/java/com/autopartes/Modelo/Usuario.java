package com.autopartes.Modelo;

public class Usuario {
    private int idUsuario;
    private String username;
    private String rol;

    public Usuario(int idUsuario, String username, String rol) {
        this.idUsuario = idUsuario;
        this.username = username;
        this.rol = rol;
    }

    public int getIdUsuario() { return idUsuario; }
    public String getUsername() { return username; }
    public String getRol() { return rol; }
}