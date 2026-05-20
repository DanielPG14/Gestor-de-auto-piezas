//Clase para manejar sesioes
package com.autopartes.Modelo;

public class Sesion {
    // Variable estática para login
    private static Usuario usuarioLogueado;

    // Se llama desde el Login al tener éxito
    public static void setUsuario(Usuario usuario) {
        usuarioLogueado = usuario;
    }

    // Se llama desde cualquier otra ventana para saber quién es el usuario
    public static Usuario getUsuario() {
        return usuarioLogueado;
    }

    public static String getRolActivo() {
        if (usuarioLogueado != null && usuarioLogueado.getRol() != null) {
            return usuarioLogueado.getRol().toLowerCase().trim();
        }
        return "invitado"; // Retorno seguro si no hay sesión iniciada
    }

    // Para cuando el usuario quiera cerrar sesión
    public static void limpiarSesion() {
        usuarioLogueado = null;
    }
}