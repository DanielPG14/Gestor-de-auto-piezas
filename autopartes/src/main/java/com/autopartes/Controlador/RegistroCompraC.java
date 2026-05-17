package com.autopartes.Controlador;

import com.autopartes.Modelo.CompraDAO;
import com.autopartes.Modelo.Pieza;
import com.autopartes.Modelo.PiezaDAO;
import com.autopartes.Modelo.Proveedor;
import com.autopartes.Modelo.ProveedorDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

/**
 * RegistroCompraC: Controlador para registrar entradas de compra (abastecimiento).
 * Funcionalidad:
 * - Seleccionar pieza interna del catálogo
 * - Elegir proveedor desde ComboBox
 * - Ingresar código específico del proveedor
 * - Capturar nuevo costo de compra
 * - Ingresar cantidad de unidades
 * - Registrar entrada transaccionalmente
 */
public class RegistroCompraC {

    // === COMPONENTES UI PARA CAPTURA DE DATOS ===
    @FXML private ComboBox<Pieza> cmbPieza;
    @FXML private ComboBox<Proveedor> cmbProveedor;
    @FXML private TextField txtCodigoProveedor;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtCantidad;

    // === COMPONENTES UI PARA INFORMACIÓN ===
    @FXML private Label lblStockActual;
    @FXML private Label lblProveedorInfo;
    @FXML private Button btnRegistrarCompra;
    @FXML private Button btnCancelar;

    // === TABLA PARA HISTORIAL DE COMPRAS RECIENTES ===
    @FXML private TableView<CompraItem> tableViewHistorial;
    @FXML private TableColumn<CompraItem, String> colPieza;
    @FXML private TableColumn<CompraItem, String> colProveedor;
    @FXML private TableColumn<CompraItem, String> colCodigo;
    @FXML private TableColumn<CompraItem, Double> colPrecio;
    @FXML private TableColumn<CompraItem, Integer> colCantidad;
    @FXML private TableColumn<CompraItem, String> colFecha;

    // === DAO INSTANCES ===
    private PiezaDAO piezaDAO;
    private ProveedorDAO proveedorDAO;
    private CompraDAO compraDAO;

    // === OBSERVABLE LISTS ===
    private ObservableList<Pieza> piezas;
    private ObservableList<Proveedor> proveedores;
    private ObservableList<CompraItem> historialCompras;

    /**
     * Inicializa el controlador.
     * Carga datos de piezas, proveedores e historial.
     */
    @FXML
    public void initialize() {
        piezaDAO = new PiezaDAO();
        proveedorDAO = new ProveedorDAO();
        compraDAO = new CompraDAO();

        piezas = FXCollections.observableArrayList();
        proveedores = FXCollections.observableArrayList();
        historialCompras = FXCollections.observableArrayList();

        // Cargar datos en ComboBox
        cargarPiezas();
        cargarProveedores();

        // Configurar tabla de historial
        configurarTablaHistorial();

        // Listeners
        cmbPieza.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                actualizarStockActual(newVal);
            }
        });

        cmbProveedor.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                actualizarInfoProveedor(newVal);
            }
        });

        // Cargar historial
        cargarHistorialCompras();
    }

    /**
     * Carga todas las piezas disponibles en el ComboBox.
     */
    private void cargarPiezas() {
        try {
            piezas.clear();
            piezas.addAll(piezaDAO.obtenerTodas());
            cmbPieza.setItems(piezas);
            System.out.println("✓ " + piezas.size() + " piezas cargadas.");
        } catch (Exception e) {
            System.err.println("✗ Error al cargar piezas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga todos los proveedores disponibles en el ComboBox.
     */
    private void cargarProveedores() {
        try {
            proveedores.clear();
            proveedores.addAll(proveedorDAO.obtenerTodos());
            cmbProveedor.setItems(proveedores);
            System.out.println("✓ " + proveedores.size() + " proveedores cargados.");
        } catch (Exception e) {
            System.err.println("✗ Error al cargar proveedores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Actualiza la etiqueta de stock actual según la pieza seleccionada.
     */
    private void actualizarStockActual(Pieza pieza) {
        lblStockActual.setText("Stock Actual: " + pieza.getStock() + " unidades");
    }

    /**
     * Actualiza la información del proveedor seleccionado.
     */
    private void actualizarInfoProveedor(Proveedor proveedor) {
        lblProveedorInfo.setText("Proveedor: " + proveedor.getRazonSocial() + " - " + proveedor.getContacto());
    }

    /**
     * Configura las columnas de la tabla de historial.
     */
    private void configurarTablaHistorial() {
        colPieza.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getPieza()));
        colProveedor.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProveedor()));
        colCodigo.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCodigoProveedor()));
        colPrecio.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getPrecioCompra()).asObject());
        colCantidad.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getCantidad()).asObject());
        colFecha.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFecha()));

        tableViewHistorial.setItems(historialCompras);
    }

    /**
     * Carga el historial de compras recientes (últimas 50).
     */
    private void cargarHistorialCompras() {
        try {
            historialCompras.clear();
            historialCompras.addAll(compraDAO.obtenerUltimasCompras(50));
            System.out.println("✓ Historial de compras cargado.");
        } catch (Exception e) {
            System.err.println("✗ Error al cargar historial: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Manejador del botón "Registrar Compra".
     * Valida datos y ejecuta la persistencia transaccional.
     */
    @FXML
    private void registrarCompra(ActionEvent event) {
        try {
            // Validaciones
            Pieza pieza = cmbPieza.getValue();
            Proveedor proveedor = cmbProveedor.getValue();
            String codigoProveedor = txtCodigoProveedor.getText().trim();
            String precioText = txtPrecioCompra.getText().trim();
            String cantidadText = txtCantidad.getText().trim();

            if (pieza == null) {
                mostrarAlerta("Validación", "Seleccione una pieza.");
                return;
            }

            if (proveedor == null) {
                mostrarAlerta("Validación", "Seleccione un proveedor.");
                return;
            }

            if (codigoProveedor.isEmpty()) {
                mostrarAlerta("Validación", "Ingrese el código del proveedor.");
                return;
            }

            if (precioText.isEmpty()) {
                mostrarAlerta("Validación", "Ingrese el precio de compra.");
                return;
            }

            if (cantidadText.isEmpty()) {
                mostrarAlerta("Validación", "Ingrese la cantidad de unidades.");
                return;
            }

            double precioCompra = Double.parseDouble(precioText);
            int cantidad = Integer.parseInt(cantidadText);

            if (precioCompra <= 0 || cantidad <= 0) {
                mostrarAlerta("Validación", "El precio y la cantidad deben ser mayores a cero.");
                return;
            }

            // Registrar compra mediante DAO
            int idCompra = compraDAO.registrarCompra(pieza.getIdPieza(), proveedor.getIdProveedor(),
                    codigoProveedor, precioCompra, cantidad);

            if (idCompra > 0) {
                mostrarAlerta("Éxito",
                    "Compra registrada exitosamente.\n" +
                    "ID Compra: " + idCompra + "\n" +
                    "Pieza: " + pieza.getNombre() + "\n" +
                    "Cantidad: " + cantidad + " unidades");

                // Limpiar formulario
                limpiarFormulario();

                // Recargar historial
                cargarHistorialCompras();

                // Recargar stock
                actualizarStockActual(pieza);

            } else {
                mostrarAlerta("Error", "No se pudo registrar la compra. Intente de nuevo.");
            }

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Ingrese valores numéricos válidos para precio y cantidad.");
        } catch (Exception e) {
            System.err.println("ERROR en registrarCompra: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error inesperado", "Ocurrió un error: " + e.getMessage());
        }
    }

    /**
     * Limpia el formulario después de registrar una compra.
     */
    private void limpiarFormulario() {
        cmbPieza.setValue(null);
        cmbProveedor.setValue(null);
        txtCodigoProveedor.clear();
        txtPrecioCompra.clear();
        txtCantidad.clear();
        lblStockActual.setText("Stock Actual: -");
        lblProveedorInfo.setText("Proveedor: -");
    }

    /**
     * Manejador del botón "Cancelar".
     */
    @FXML
    private void cancelar(ActionEvent event) {
        GestorVistas.cambiarVista("CatalogoVendedor.fxml");
    }

    /**
     * Muestra un diálogo de alerta.
     */
    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // =========================================================================
    // CLASE INTERNA PARA MODELAR ITEM DE HISTORIAL
    // =========================================================================

    /**
     * Clase interna para modelar un item del historial de compras.
     */
    public static class CompraItem {
        private final String pieza;
        private final String proveedor;
        private final String codigoProveedor;
        private final double precioCompra;
        private final int cantidad;
        private final String fecha;

        public CompraItem(String pieza, String proveedor, String codigoProveedor,
                         double precioCompra, int cantidad, String fecha) {
            this.pieza = pieza;
            this.proveedor = proveedor;
            this.codigoProveedor = codigoProveedor;
            this.precioCompra = precioCompra;
            this.cantidad = cantidad;
            this.fecha = fecha;
        }

        public String getPieza() { return pieza; }
        public String getProveedor() { return proveedor; }
        public String getCodigoProveedor() { return codigoProveedor; }
        public double getPrecioCompra() { return precioCompra; }
        public int getCantidad() { return cantidad; }
        public String getFecha() { return fecha; }
    }
}
