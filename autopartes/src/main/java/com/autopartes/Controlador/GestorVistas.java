package com.autopartes.Controlador;

import com.autopartes.Modelo.Sesion;
import com.autopartes.Modelo.Usuario;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;
import java.io.IOException;

public class GestorVistas {

    private static Stage ventanaPrincipal;

    public static void setVentanaPrincipal(Stage stage) {
        ventanaPrincipal = stage;
    }

    private static boolean tienePermiso(String nombreFxml) {
        if (nombreFxml.equals("Login.fxml"))
            return true;

        Usuario usuarioActual = Sesion.getUsuario();
        if (usuarioActual == null)
            return false;

        String rol = usuarioActual.getRol().trim().toLowerCase();

        if (rol.contains("cajero") || rol.contains("vendedor")) {
            return nombreFxml.equals("CatalogoVendedor.fxml") ||
                    nombreFxml.equals("VistaStock.fxml") ||
                    nombreFxml.equals("CarritoVenta.fxml") ||
                    nombreFxml.equals("ReporteVenta.fxml") ||
                    nombreFxml.equals("Checkout.fxml"); 
        }

        if (rol.contains("almacenista")) {
            return nombreFxml.equals("ControlInventario.fxml") ||
                    nombreFxml.equals("VistaStockAlm.fxml") ||
                    nombreFxml.equals("MapaAlmacen.fxml");
        }

        if (rol.contains("admin") || rol.contains("administrador")) {
            return true;
        }

        return false;
    }

    public static void cambiarVista(String nombreFxml) {
        if (!tienePermiso(nombreFxml)) {
            String nombreRol = (Sesion.getUsuario() != null) ? Sesion.getUsuario().getRol() : "Desconocido";
            mostrarAlertaAccesoDenegado(nombreRol, nombreFxml);
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(GestorVistas.class.getResource("/com/autopartes/" + nombreFxml));
            Parent root = loader.load();
            Scene nuevaEscena = new Scene(root);
            ventanaPrincipal.setScene(nuevaEscena);
            actualizarTitulo();
            ventanaPrincipal.show();
        } catch (IOException e) {
            mostrarAlertaErrorCarga(nombreFxml, e);
        }
    }

    public static <T> T cambiarVistaConControlador(String nombreFxml) {
        if (!tienePermiso(nombreFxml)) {
            String nombreRol = (Sesion.getUsuario() != null) ? Sesion.getUsuario().getRol() : "Desconocido";
            mostrarAlertaAccesoDenegado(nombreRol, nombreFxml);
            return null; 
        }

        try {
            FXMLLoader loader = new FXMLLoader(GestorVistas.class.getResource("/com/autopartes/" + nombreFxml));
            Parent root = loader.load();
            Scene nuevaEscena = new Scene(root);
            ventanaPrincipal.setScene(nuevaEscena);
            actualizarTitulo();
            ventanaPrincipal.show();
            return loader.getController();
        } catch (IOException e) {
            mostrarAlertaErrorCarga(nombreFxml, e);
            return null;
        }
    }

    private static void actualizarTitulo() {
        if (ventanaPrincipal != null) {
            if (Sesion.getUsuario() != null) {
                ventanaPrincipal.setTitle("Gestor de Auto-Piezas | " + Sesion.getUsuario().getRol());
            } else {
                ventanaPrincipal.setTitle("Gestor de Auto-Piezas | Acceso");
            }
        }
    }

    private static void mostrarAlertaAccesoDenegado(String nombreRol, String nombreFxml) {
        Alert alerta = new Alert(Alert.AlertType.WARNING);
        alerta.setTitle("Acceso Denegado");
        alerta.setHeaderText("Restricción de Seguridad");
        alerta.setContentText("Tu perfil de " + nombreRol + " no tiene autorización para la vista: " + nombreFxml);
        alerta.showAndWait();
    }

    private static void mostrarAlertaErrorCarga(String nombreFxml, IOException e) {
        System.err.println("Error crítico: No se pudo cargar el archivo " + nombreFxml);
        e.printStackTrace();
        Alert alertaError = new Alert(Alert.AlertType.ERROR);
        alertaError.setTitle("Error de Carga");
        alertaError.setHeaderText("No se pudo encontrar o renderizar la vista");
        alertaError.setContentText("Archivo conflictivo: " + nombreFxml + "\nRevisa la consola para más detalles.");
        alertaError.showAndWait();
    }
}