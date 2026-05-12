package com.autopartes;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

public class App extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        // Cargamos el archivo FXML desde la carpeta de recursos
        // Asegúrate de que el nombre coincida exactamente (ej: pantalla.fxml)
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/com/autopartes/VistaStock.fxml"));        
        Parent root = loader.load();
        Scene scene = new Scene(root);
        
        stage.setTitle("Gestor de Auto-piezas - Prueba de Conexión");
        stage.setScene(scene);
        stage.setResizable(true);
        stage.setMaximized(true);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}