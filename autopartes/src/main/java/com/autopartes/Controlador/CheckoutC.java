package com.autopartes.Controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class CheckoutC {

    @FXML
    private void volverCarrito(ActionEvent event) {
        // Regresamos al carrito usando el gestor centralizado
        GestorVistas.cambiarVista("CarritoVenta.fxml");
    }
}