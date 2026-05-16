package com.autopartes;

import com.autopartes.Controlador.GestorVistas; // Importamos nuestro gestor
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        GestorVistas.setVentanaPrincipal(stage);
        
        stage.setTitle("Gestor de Auto-Piezas - Inicio de Sesión");
        stage.setResizable(true);
        
        GestorVistas.cambiarVista("Login.fxml");
    }

    public static void main(String[] args) {
        launch();
    }
}