package com.autopartes.Controlador;

import com.autopartes.Modelo.PiezaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class AgregarPiezaC {

    @FXML private TextField txtNombre, txtPrecio, txtStock, txtNivel;
    @FXML private ComboBox<Integer> cbEstante;

    private PiezaDAO piezaDAO = new PiezaDAO();

    @FXML
    public void initialize() {
        // Aquí podrías cargar los IDs de los estantes desde la BD
        cbEstante.getItems().addAll(1, 2, 3); 
    }

    @FXML
    void guardarPieza() {
        // Lógica para llamar al DAO y hacer el INSERT
        System.out.println("Guardando: " + txtNombre.getText());
        // Después de guardar con éxito:
        cerrarModal();
    }

    @FXML
    void cerrarModal() {
        Stage stage = (Stage) txtNombre.getScene().getWindow();
        stage.close();
    }
}