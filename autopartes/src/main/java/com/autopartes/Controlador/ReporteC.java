package com.autopartes.Controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class ReporteC {

    @FXML
    private void volverCatalogo(ActionEvent event) {
        // Usamos el cadenero para regresar directamente al catálogo
        GestorVistas.cambiarVista("CatalogoVendedor.fxml");
    }
}