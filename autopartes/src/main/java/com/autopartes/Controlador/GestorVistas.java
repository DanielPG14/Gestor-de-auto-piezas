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

    // Se llama desde App.java al iniciar el programa
    public static void setVentanaPrincipal(Stage stage) {
        ventanaPrincipal = stage;
    }

    /**
     * Valida si el usuario en sesión tiene permiso para ver el FXML solicitado.
     */
    private static boolean tienePermiso(String nombreFxml) {
        // El Login siempre es accesible
        if (nombreFxml.equals("Login.fxml")) {
            return true;
        }

        Usuario usuarioActual = Sesion.getUsuario();
        
        // Si no hay sesión, bloqueamos cualquier intento de navegación
        if (usuarioActual == null) {
            return false;
        }

        String rol = usuarioActual.getRol().trim();

        // Lógica de permisos por rol
        switch (rol) {
            case "Cajero":
                return nombreFxml.equals("CatalogoVendedor.fxml") || 
                       nombreFxml.equals("VistaStock.fxml") || 
                       nombreFxml.equals("CarritoVenta.fxml") || 
                       nombreFxml.equals("Checkout.fxml") || 
                       nombreFxml.equals("ReporteVenta.fxml");

            case "Almacenista":
                return nombreFxml.equals("VistaStockAlm.fxml") ||
                       nombreFxml.equals("VistaStock.fxml") || 
                       //nombreFxml.equals("GenerarListaVendedor.fxml") || 
                       nombreFxml.equals("ReporteVenta.fxml");

            case "Vendedor":
                //return nombreFxml.equals("ListaGenerada.fxml") || 
                       //nombreFxml.equals("ListaProveedores.fxml");

            default:
                return false;
        }
    }

    /**
     * Cambia la escena actual de la ventana principal.
     */
    public static void cambiarVista(String nombreFxml) {
        // 1. Verificación de seguridad
        if (!tienePermiso(nombreFxml)) {
            String nombreRol = (Sesion.getUsuario() != null) ? Sesion.getUsuario().getRol() : "Desconocido";
            
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Acceso Denegado");
            alerta.setHeaderText("Restricción de Seguridad");
            alerta.setContentText("Tu perfil de " + nombreRol + " no tiene autorización para esta vista.");
            alerta.showAndWait();
            return; 
        }

        // 2. Carga del archivo FXML
        try {
            // Ajusta la ruta si tus FXML están dentro de una carpeta /vistas/
            FXMLLoader loader = new FXMLLoader(GestorVistas.class.getResource("/com/autopartes/" + nombreFxml));
            Parent root = loader.load();
            
            Scene nuevaEscena = new Scene(root);
            ventanaPrincipal.setScene(nuevaEscena);
            
            // 3. Título dinámico según el estado de la sesión
            if (Sesion.getUsuario() != null) {
                ventanaPrincipal.setTitle("Gestor de Auto-Piezas | " + Sesion.getUsuario().getRol());
            } else {
                ventanaPrincipal.setTitle("Gestor de Auto-Piezas | Acceso");
            }
            
            ventanaPrincipal.show();
            
        } catch (IOException e) {
            System.err.println("Error crítico: No se pudo cargar el archivo " + nombreFxml);
            e.printStackTrace();
        }
    }

    /**
     * Cambia de vista y permite pasar un controlador para inicialización personalizada.
     * Útil para transferir datos entre controladores (ej: Carrito → Checkout).
     */
    public static <T> T cambiarVistaConControlador(String nombreFxml) {
        if (!tienePermiso(nombreFxml)) {
            String nombreRol = (Sesion.getUsuario() != null) ? Sesion.getUsuario().getRol() : "Desconocido";
            
            Alert alerta = new Alert(Alert.AlertType.WARNING);
            alerta.setTitle("Acceso Denegado");
            alerta.setHeaderText("Restricción de Seguridad");
            alerta.setContentText("Tu perfil de " + nombreRol + " no tiene autorización para esta vista.");
            alerta.showAndWait();
            return null;
        }

        try {
            FXMLLoader loader = new FXMLLoader(GestorVistas.class.getResource("/com/autopartes/" + nombreFxml));
            Parent root = loader.load();
            
            Scene nuevaEscena = new Scene(root);
            ventanaPrincipal.setScene(nuevaEscena);
            
            if (Sesion.getUsuario() != null) {
                ventanaPrincipal.setTitle("Gestor de Auto-Piezas | " + Sesion.getUsuario().getRol());
            } else {
                ventanaPrincipal.setTitle("Gestor de Auto-Piezas | Acceso");
            }
            
            ventanaPrincipal.show();
            
            return loader.getController();
            
        } catch (IOException e) {
            System.err.println("Error crítico: No se pudo cargar el archivo " + nombreFxml);
            e.printStackTrace();
            return null;
        }
    }
}