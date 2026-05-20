// Controlador para la vista del catálogo de piezas, con funcionalidades de búsqueda y adición al carrito.
package com.autopartes.Controlador;

import com.autopartes.Modelo.CarritoSingleton;
import com.autopartes.Modelo.ItemCarrito;
import com.autopartes.Modelo.Pieza;
import com.autopartes.Modelo.PiezaDAO;
import com.autopartes.Modelo.Sesion;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;

import java.util.List;

public class CatalogoC {

    // Componentes FXML vinculados a CatalogoVendedor.fxml
    @FXML private TextField txtBuscarGeneral;
    @FXML private TableView<Pieza> tablaPiezas;
    @FXML private TableColumn<Pieza, Integer> colIdPieza;
    @FXML private TableColumn<Pieza, String> colNombre;
    @FXML private TableColumn<Pieza, String> colProveedor;
    @FXML private TableColumn<Pieza, String> colCodigoProveedor;
    @FXML private TableColumn<Pieza, Double> colPrecioCompra;
    @FXML private TableColumn<Pieza, Integer> colStock;

    @FXML private Button btnAgregarCarrito;
    @FXML private Button btnReporte;
    @FXML private MenuItem menuReporte;

    // Capa de persistencia y listas de datos
    private final PiezaDAO piezaDAO = new PiezaDAO();
    private final ObservableList<Pieza> listaMaestraPiezas = FXCollections.observableArrayList();
    private FilteredList<Pieza> listaFiltradaPiezas;
    private Timeline debounceTimer;

    @FXML
    public void initialize() {
        configurarColumnasTabla();
        cargarDatosDesdeBD();
        configurarFiltroBusqueda();
    }

    /**
     * Vincula las columnas de la TableView con los atributos del modelo Pieza
     */
    private void configurarColumnasTabla() {
        colIdPieza.setCellValueFactory(new PropertyValueFactory<>("idPieza"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colProveedor.setCellValueFactory(new PropertyValueFactory<>("razonSocialProveedor"));
        colCodigoProveedor.setCellValueFactory(new PropertyValueFactory<>("codigoProveedor"));
        colPrecioCompra.setCellValueFactory(new PropertyValueFactory<>("precioCompra"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
    }

    /**
     * Recupera las refacciones desde el DAO y las monta en la lista observable
     */
    private void cargarDatosDesdeBD() {
        try {
            List<Pieza> piezasBD = piezaDAO.obtenerTodas();
            listaMaestraPiezas.setAll(piezasBD);
            
            // Inicializamos la lista filtrada apuntando a nuestra lista maestra
            listaFiltradaPiezas = new FilteredList<>(listaMaestraPiezas, p -> true);
            tablaPiezas.setItems(listaFiltradaPiezas);
            
            System.out.println("✓ Catálogo cargado con éxito. Total registros: " + piezasBD.size());
        } catch (Exception e) {
            System.err.println("❌ Error al cargar piezas desde el DAO: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error de base de datos", "No se pudieron recuperar las autopartes.", Alert.AlertType.ERROR);
        }
    }

    /**
     * Configura un listener para que la lista se filtre automáticamente 
     * mientras el usuario escribe en el TextField de búsqueda.
     */
    private void configurarFiltroBusqueda() {
        txtBuscarGeneral.textProperty().addListener((observable, oldValue, newValue) -> {
            if (debounceTimer != null) {
                debounceTimer.stop();
            }

            debounceTimer = new Timeline(new KeyFrame(Duration.millis(300), event -> {
                buscarPiezaEnBaseDeDatos(newValue);
            }));
            debounceTimer.play();
        });
    }

    private void buscarPiezaEnBaseDeDatos(String criterioTexto) {
        listaFiltradaPiezas.setPredicate(pieza -> {
            if (criterioTexto == null || criterioTexto.trim().isEmpty()) {
                return true;
            }

            String criterio = criterioTexto.toLowerCase().trim();
            boolean coincideNombre = pieza.getNombre() != null && pieza.getNombre().toLowerCase().contains(criterio);
            boolean coincideProveedor = pieza.getRazonSocialProveedor() != null && pieza.getRazonSocialProveedor().toLowerCase().contains(criterio);
            boolean coincideCodigo = pieza.getCodigoProveedor() != null && pieza.getCodigoProveedor().toLowerCase().contains(criterio);

            return coincideNombre || coincideProveedor || coincideCodigo;
        });
    }

    /**
     * Acción manual del botón "Buscar" (por si el usuario prefiere dar clic o presionar Enter)
     */
    @FXML
    private void filtrarCatalogo(ActionEvent event) {
        // El listener en textProperty() ya hace el trabajo en tiempo real, 
        // pero dejamos el método asignado para evitar excepciones del FXML.
        System.out.println("Búsqueda ejecutada: " + txtBuscarGeneral.getText());
    }

    /**
     * 🛒 ACCIÓN PRINCIPAL: Agrega el elemento seleccionado a la instancia del Carrito
     */
    @FXML
    private void agregarAlCarrito(ActionEvent event) {
        Pieza piezaSeleccionada = tablaPiezas.getSelectionModel().getSelectedItem();

        if (piezaSeleccionada == null) {
            mostrarAlerta("Selección requerida", "Por favor, seleccione una autoparte de la tabla para agregarla al carrito.", Alert.AlertType.WARNING);
            return;
        }

        // Validación defensiva de stock físico en tienda antes de procesar
        if (piezaSeleccionada.getStock() <= 0) {
            mostrarAlerta("Sin existencias", "La pieza '" + piezaSeleccionada.getNombre() + "' no cuenta con inventario disponible en almacén.", Alert.AlertType.ERROR);
            return;
        }

        ItemCarrito item = new ItemCarrito(piezaSeleccionada, 1);
        CarritoSingleton.getInstancia().agregarItem(item);

        mostrarAlerta("Pieza añadida", "La pieza se agregó al carrito correctamente.", Alert.AlertType.INFORMATION);
    }

    // --- Métodos del Menú de Navegación Lateral / Hamburguesa ---
    @FXML
    private void irACatalogo(ActionEvent event) {
        GestorVistas.cambiarVista("CatalogoVendedor.fxml");
    }

    @FXML
    private void irAMapaAlmacen(ActionEvent event) {
        GestorVistas.cambiarVista("VistaStock.fxml");
    }

    @FXML
    private void irACarrito(ActionEvent event) {
        GestorVistas.cambiarVista("CarritoVenta.fxml");
    }

    @FXML
    private void irAReporte(ActionEvent event) {
        GestorVistas.cambiarVista("ReporteVenta.fxml");
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        Sesion.limpiarSesion();
        GestorVistas.cambiarVista("Login.fxml");
    }

    private void mostrarAlerta(String titulo, String mensaje, Alert.AlertType tipo) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}