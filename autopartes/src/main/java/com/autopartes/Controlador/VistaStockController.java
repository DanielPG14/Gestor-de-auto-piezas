package com.autopartes.Controlador;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;

public class VistaStockController {

    // Vinculamos los elementos que tienen fx:id en el Scene Builder
    @FXML
    private Button btnRegistrarEntrada;

    @FXML
    private TextField txtBuscarEstante;

    @FXML
    private FlowPane contenedorTarjetas;

    // Esta función se ejecuta al darle clic a "Registrar Entrada"
    @FXML
    private void abrirModalEntrada(ActionEvent event) {
        System.out.println("Abriendo ventana para registrar entrada...");
        // Aquí después pondremos el código para abrir el modal
    }

    // Esta función se ejecuta al darle clic a "Filtrar"
    @FXML
    private void filtrarPorEstante(ActionEvent event) {
        String estanteBuscado = txtBuscarEstante.getText();
        System.out.println("Buscando el estante: " + estanteBuscado);
        // Aquí después pondremos la lógica para filtrar las tarjetas
    }
}