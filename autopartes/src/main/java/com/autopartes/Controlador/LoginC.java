package com.autopartes.Controlador;

import com.autopartes.Modelo.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.event.ActionEvent;

public class LoginC {
    @FXML
    private TextField txtUser;
    @FXML
    private PasswordField txtPass;
    @FXML
    private Label lblError;

    private UsuarioDAO dao = new UsuarioDAO();

    @FXML
    private void manejarLogin(ActionEvent event) {
        String user = txtUser.getText();
        String pass = txtPass.getText();

        // 1. Validamos las credenciales en la base de datos
        Usuario u = dao.login(user, pass);
        
        if (u != null) {
            // 2. Guardamos el objeto completo en nuestra clase Sesion
            Sesion.setUsuario(u);
            System.out.println("VINCULACIÓN EXITOSA: " + Sesion.getUsuario().getUsername());
            
            // 3. Obtenemos el rol y lo limpiamos de posibles espacios invisibles
            String rol = u.getRol().trim();
            
            // 4. Delegamos la navegación al Gestor de Vistas según el rol
            switch (rol) {
                case "Cajero":
                    // El cajero va directo a vender
                    GestorVistas.cambiarVista("CatalogoVendedor.fxml");
                    break;
                    
                case "Almacenista":
                    // El almacenista va a gestionar el inventario
                    GestorVistas.cambiarVista("VistaStockAlm.fxml");
                    break;
                    
                case "Vendedor":
                    // El vendedor va a ver las listas generadas
                    GestorVistas.cambiarVista("ListaGenerada.fxml"); 
                    break;
                    
                default:
                    // Si por algún motivo tiene un rol extraño en la base de datos
                    lblError.setText("Error: Rol no reconocido en el sistema (" + rol + ").");
                    break;
            }

        } else {
            // Si el usuario no existe o la contraseña está mal
            lblError.setText("Error: Usuario o contraseña incorrectos.");
        }
    }
}