package com.autopartes.Controlador;

import com.autopartes.Modelo.Pieza;
import com.autopartes.Modelo.PiezaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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

    @FXML private TableView<Pieza> tablaStock;
    @FXML private TableColumn<Pieza, Integer> colIDPieza;
    @FXML private TableColumn<Pieza, String> colNombre;
    @FXML private TableColumn<Pieza, String> colProveedor;
    @FXML private TableColumn<Pieza, String> colCodigoProveedor;
    @FXML private TableColumn<Pieza, Double> colPrecioCosto;
    @FXML private TableColumn<Pieza, String> colEstante;
    @FXML private TableColumn<Pieza, Integer> colNivelPiso;
    @FXML private TableColumn<Pieza, Integer> colStockActual;
    @FXML private TextField txtBuscarGeneral;
    @FXML private Button btnAgregarPieza; // 👈 CORRECCIÓN 3: Declaración del botón FXML indispensable para el modal

    private PiezaDAO piezaDAO = new PiezaDAO();
    private ObservableList<Pieza> masterData = FXCollections.observableArrayList();
    private FilteredList<Pieza> filteredData; // Guardado como atributo de clase para un mejor control

    @FXML
    public void initialize() {
        configurarColumnas();
        configurarSemaforo();
        cargarDatos();
        configurarFiltro();
    }

    private void configurarColumnas() {
        // CORRECCIÓN 1: Importación de PropertyValueFactory añadida con éxito
        colIDPieza.setCellValueFactory(new PropertyValueFactory<>("idPieza"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("razonSocialProveedor"));
        colCodigoProveedor.setCellValueFactory(new PropertyValueFactory<>("codigoProveedor"));
        colPrecioCosto.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colEstante.setCellValueFactory(new PropertyValueFactory<>("idEstante"));
        colNivelPiso.setCellValueFactory(new PropertyValueFactory<>("nivelAsigned"));
        colStockActual.setCellValueFactory(new PropertyValueFactory<>("stock"));
    }

    private void configurarSemaforo() {
        tablaStock.setRowFactory(tv -> new TableRow<Pieza>() {
            @Override
            protected void updateItem(Pieza pieza, boolean empty) {
                super.updateItem(pieza, empty);
                if (pieza == null || empty) {
                    setStyle("");
                } else {
                    // CORRECCIÓN 2: Validación defensiva para evitar división por cero en capMax
                    double capMax = pieza.getCapMax() > 0 ? pieza.getCapMax() : 100;
                    double porcentaje = (double) pieza.getStock() / capMax;

                    if (pieza.getStock() <= 5) {
                        setStyle("-fx-background-color: #ffcccc; -fx-text-background-color: black;");
                    } else if (porcentaje >= 0.9) {
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
        // Si ya configuramos el FilteredList, asignamos los datos directo al contenedor observable
    }

    private void configurarFiltro() {
        filteredData = new FilteredList<>(masterData, p -> true);

        txtBuscarGeneral.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(pieza -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String lowerCaseFilter = newValue.toLowerCase().trim();

                if (pieza.getNombre() != null && pieza.getNombre().toLowerCase().contains(lowerCaseFilter))
                    return true;
                if (pieza.getRazonSocialProveedor() != null && pieza.getRazonSocialProveedor().toLowerCase().contains(lowerCaseFilter))
                    return true;
                if (pieza.getCodigoProveedor() != null && pieza.getCodigoProveedor().toLowerCase().contains(lowerCaseFilter))
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
        // CORRECCIÓN 4: En vez de sobrecargar la BD, el botón físico limpia el buscador y refresca la lista maestra
        cargarDatos();
        txtBuscarGeneral.clear();
    }

    @FXML
    void abrirModalAgregar() {
        try {
            // CORRECCIÓN 1: Agregadas todas las clases de carga de layouts e hilos de vista (Stage, Scene, Modality)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/autopartes/vistas/AgregarPieza.fxml"));
            Parent root = loader.load();

            Stage modalStage = new Stage();
            modalStage.setTitle("Registrar Nueva Pieza (Control de Almacén)");
            modalStage.initModality(Modality.WINDOW_MODAL);
            
            Stage currentStage = (Stage) btnAgregarPieza.getScene().getWindow();
            modalStage.initOwner(currentStage);

            Scene scene = new Scene(root);
            modalStage.setScene(scene);
            modalStage.setResizable(false);

            modalStage.showAndWait();

            // Refresca la tabla al cerrar la ventana emergente de forma automática
            cargarDatos();

        } catch (Exception e) {
            System.err.println("Error crítico al abrir la ventana de agregar piezas: " + e.getMessage());
            e.printStackTrace();
        }
    }
}