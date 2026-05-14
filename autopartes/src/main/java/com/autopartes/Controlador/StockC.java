package com.autopartes.Controlador;

import com.autopartes.Modelo.Pieza;
import com.autopartes.Modelo.PiezaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;


import java.util.List;

public class StockC {

    @FXML
    private TableView<Pieza> tablaStock;
    @FXML
    private TableColumn<Pieza, Integer> colID;
    @FXML
    private TableColumn<Pieza, String> colNombre;
    @FXML
    private TableColumn<Pieza, String> colEstante;
    @FXML
    private TableColumn<Pieza, Integer> colNivel;
    @FXML
    private TableColumn<Pieza, Integer> colStock;
    @FXML
    private TableColumn<Pieza, Integer> colCapMax;
    @FXML
    private Button btnAgregarPieza;

    @FXML
    private TextField txtBuscarGeneral;

    private PiezaDAO piezaDAO = new PiezaDAO();
    private ObservableList<Pieza> masterData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarSemaforo();
        cargarDatos();
        configurarFiltro();
    }

    @FXML
    private void volverCatalogo(ActionEvent event) {
        // Usamos el gestor para regresar
        GestorVistas.cambiarVista("CatalogoVendedor.fxml");
    }

    private void configurarColumnas() {
        colID.setCellValueFactory(new PropertyValueFactory<>("idPieza"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEstante.setCellValueFactory(new PropertyValueFactory<>("idEstante"));
        colNivel.setCellValueFactory(new PropertyValueFactory<>("nivelAsigned")); // 👈 Actualizado aquí
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCapMax.setCellValueFactory(new PropertyValueFactory<>("capMax"));
    }

    private void configurarSemaforo() {
        tablaStock.setRowFactory(tv -> new TableRow<Pieza>() {
            @Override
            protected void updateItem(Pieza pieza, boolean empty) {
                super.updateItem(pieza, empty);
                if (pieza == null || empty) {
                    setStyle("");
                } else {
                    double porcentaje = (double) pieza.getStock() / pieza.getCapMax();

                    if (pieza.getStock() <= 5) {
                        // Fondo rojo claro para stock crítico
                        setStyle("-fx-background-color: #ffcccc; -fx-text-background-color: black;");
                    } else if (porcentaje >= 0.9) {
                        // Fondo verde claro si está al 90% o más de su capacidad
                        setStyle("-fx-background-color: #ccffcc; -fx-text-background-color: black;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });
    }

    private void cargarDatos() {
        List<Pieza> lista = piezaDAO.obtenerTodas();
        masterData.setAll(lista);
        tablaStock.setItems(masterData);
    }

    private void configurarFiltro() {
        FilteredList<Pieza> filteredData = new FilteredList<>(masterData, p -> true);

        txtBuscarGeneral.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(pieza -> {
                if (newValue == null || newValue.isEmpty())
                    return true;

                String lowerCaseFilter = newValue.toLowerCase();

                if (pieza.getNombre().toLowerCase().contains(lowerCaseFilter))
                    return true;
                if (pieza.getIdEstante().toLowerCase().contains(lowerCaseFilter))
                    return true;
                if (String.valueOf(pieza.getIdPieza()).contains(lowerCaseFilter))
                    return true;

                return false;
            });
        });

        tablaStock.setItems(filteredData);
    }

    @FXML
    void filtrarStock() {
        cargarDatos();
    }

    @FXML
    void abrirModalAgregar() {
        try {
            //Cargar el FXML del modal
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autopartes/AgregarPieza.fxml"));
            Parent root = loader.load();

            // Crear una nueva ventana (Stage) para el modal
            Stage modalStage = new Stage();
            modalStage.setTitle("Registrar pieza nueva");

            //Bloquea la ventana de stock de atrás
            modalStage.initModality(Modality.WINDOW_MODAL);
            //Ventana actual como padre del modal
            Stage currentStage = (Stage) btnAgregarPieza.getScene().getWindow();
            modalStage.initOwner(currentStage);

            //Montar y mostrar
            Scene scene = new Scene(root);
            modalStage.setScene(scene);
            modalStage.setResizable(false); // Evita que deformen el formulario

            // showAndWait se queda pausado aquí hasta que el usuario cierre el modal
            modalStage.showAndWait();

            cargarDatos();

        } catch (Exception e) {
            System.err.println("Error al abrir la ventana de agregar piezas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}