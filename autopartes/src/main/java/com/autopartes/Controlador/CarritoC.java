package com.autopartes.Controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class CarritoC {

    // El botón que ya tenías para regresar
    @FXML
    private void volverCatalogo(ActionEvent event) {
        GestorVistas.cambiarVista("CatalogoVendedor.fxml");
    }

    // El NUEVO botón para ir a pagar
    @FXML
    private void irACheckout(ActionEvent event) {
        // Le decimos al Gestor que abra la vista de Checkout
        GestorVistas.cambiarVista("Checkout.fxml");
    }
}