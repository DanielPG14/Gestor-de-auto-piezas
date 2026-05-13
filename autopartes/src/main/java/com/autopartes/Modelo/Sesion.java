package com.autopartes.Modelo;

public class Sesion {
    //Variable estatica para login
    private static Usuario usuarioLogueado;

    // Se llama desde el Login al tener éxito
    public static void setUsuario(Usuario usuario) {
        usuarioLogueado = usuario;
    }

    // Se llama desde cualquier otra ventana para saber quién es el usuario
    public static Usuario getUsuario() {
        return usuarioLogueado;
    }

    // Para cuando el usuario quiera cerrar sesión
    public static void limpiarSesion() {
        usuarioLogueado = null;
    }
}
