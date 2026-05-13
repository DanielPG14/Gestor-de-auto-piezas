package com.autopartes.Controlador;

import java.io.IOException;

import com.autopartes.Modelo.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

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
            System.out.println("VINCULACIÓN EXITOSA: " + Sesion.getUsuario().getUsername());
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/com/autopartes/CatalogoVendedor.fxml"));

                Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.setTitle("Panel Principal - " + u.getRol()); // Título dinámico
                stage.show();

            } catch (IOException e) {
                lblError.setText("Error al cargar la ventana principal.");
                e.printStackTrace();
            }
        } else {
            lblError.setText("Error: Usuario no encontrado.");
        }
    }
}