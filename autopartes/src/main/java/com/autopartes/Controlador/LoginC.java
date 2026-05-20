//Controlador para validar el login del usuario
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

        Usuario u = dao.login(user, pass);
        
        if (u != null) {
            Sesion.setUsuario(u);

            String rol = u.getRol().trim();
            
            switch (rol) {
                case "Cajero":
                    // El cajero va directo a vender
                    GestorVistas.cambiarVista("CatalogoVendedor.fxml");
                    break;
                    
                case "Almacenista":
                    GestorVistas.cambiarVista("VistaStockAlm.fxml");
                    break;
                    
                case "Vendedor":
                    GestorVistas.cambiarVista("ListaGenerada.fxml"); 
                    break;
                    
                default:
                    lblError.setText("Error: Rol no reconocido en el sistema (" + rol + ").");
                    break;
            }

        } else {
            lblError.setText("Error: Usuario o contraseña incorrectos.");
        }
    }
}