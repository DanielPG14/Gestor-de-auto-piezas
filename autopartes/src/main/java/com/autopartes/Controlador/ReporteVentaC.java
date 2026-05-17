package com.autopartes.Controlador;

import com.autopartes.Modelo.VentaDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * ReporteVentaC: Controlador para la vista de Reportes de Ventas.
 * Gestiona:
 * - Visualización de historial de ventas
 * - Desglose de detalles por venta
 * - Navegación y consultas
 */
public class ReporteVentaC {

    // === COMPONENTES UI PARA TABLA DE VENTAS ===
    @FXML private TableView<VentaItem> tableViewVentas;
    @FXML private TableColumn<VentaItem, Integer> colIdVenta;
    @FXML private TableColumn<VentaItem, String> colFecha;
    @FXML private TableColumn<VentaItem, String> colCliente;
    @FXML private TableColumn<VentaItem, Double> colTotal;

    // === COMPONENTES UI PARA DETALLES ===
    @FXML private Label lblClienteSeleccionado;
    @FXML private Label lblFechaVenta;
    @FXML private Label lblTotalVenta;

    @FXML private TableView<DetalleVentaItem> tableViewDetalles;
    @FXML private TableColumn<DetalleVentaItem, String> colProducto;
    @FXML private TableColumn<DetalleVentaItem, Double> colPrecioCompra;
    @FXML private TableColumn<DetalleVentaItem, Integer> colUtilidadPct;
    @FXML private TableColumn<DetalleVentaItem, Double> colIvaPct;
    @FXML private TableColumn<DetalleVentaItem, Double> colTotalLinea;

    // === BOTONES ===
    @FXML private Button btnVerDetalle;
    @FXML private Button btnVolver;

    // === PROPIEDADES INTERNAS ===
    private VentaDAO ventaDAO;
    private ObservableList<VentaItem> ventasData;
    private ObservableList<DetalleVentaItem> detallesData;

    /**
     * Inicializa el controlador.
     * Configura las columnas de las tablas y carga los datos de ventas.
     */
    @FXML
    public void initialize() {
        ventaDAO = new VentaDAO();
        ventasData = FXCollections.observableArrayList();
        detallesData = FXCollections.observableArrayList();

        // === CONFIGURACIÓN DE TABLA DE VENTAS ===
        colIdVenta.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getIdVenta()).asObject());
        colFecha.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFecha()));
        colCliente.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getCliente()));
        colTotal.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());

        tableViewVentas.setItems(ventasData);

        // === CONFIGURACIÓN DE TABLA DE DETALLES ===
        colProducto.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getProducto()));
        colPrecioCompra.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getPrecioCompra()).asObject());
        colUtilidadPct.setCellValueFactory(cellData -> 
            new SimpleIntegerProperty(cellData.getValue().getUtilidadPct()).asObject());
        colIvaPct.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getIvaPct()).asObject());
        colTotalLinea.setCellValueFactory(cellData -> 
            new SimpleDoubleProperty(cellData.getValue().getTotalLinea()).asObject());

        tableViewDetalles.setItems(detallesData);

        // Listener para seleccionar fila en tabla de ventas
        tableViewVentas.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                cargarDetallesVenta(newVal);
            }
        });

        // Cargar ventas iniciales
        cargarVentas();
    }

    /**
     * Carga la lista de ventas desde la base de datos.
     * (NOTA: Esta es una estructura base. El método real requeriría un DAO completo)
     */
    private void cargarVentas() {
        try {
            // TODO: Implementar consulta a BD para obtener lista de ventas
            // ventasData.addAll(ventaDAO.obtenerTodasLasVentas());
            
            System.out.println("Cargando ventas desde BD...");
            // Por ahora, mostrar datos de ejemplo para estructura
            mostrarDatosEjemplo();

        } catch (Exception e) {
            System.err.println("Error al cargar ventas: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Carga los detalles de una venta específica.
     * (NOTA: Esta es una estructura base)
     */
    private void cargarDetallesVenta(VentaItem venta) {
        try {
            detallesData.clear();
            
            // TODO: Implementar consulta a BD para obtener detalles de venta
            // List<DetalleVentaItem> detalles = ventaDAO.obtenerDetallesVenta(venta.getIdVenta());
            // detallesData.addAll(detalles);

            lblClienteSeleccionado.setText("Cliente: " + venta.getCliente());
            lblFechaVenta.setText("Fecha: " + venta.getFecha());
            lblTotalVenta.setText("Total: $" + String.format("%.2f", venta.getTotal()));

            System.out.println("Detalles cargados para venta ID=" + venta.getIdVenta());

        } catch (Exception e) {
            System.err.println("Error al cargar detalles: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Manejador del botón "Ver Detalle".
     */
    @FXML
    private void verDetalle(ActionEvent event) {
        VentaItem ventaSeleccionada = tableViewVentas.getSelectionModel().getSelectedItem();
        
        if (ventaSeleccionada == null) {
            mostrarAlerta("Selección", "Por favor, seleccione una venta primero.");
            return;
        }

        cargarDetallesVenta(ventaSeleccionada);
        mostrarAlerta("Detalle", "Detalles de la venta " + ventaSeleccionada.getIdVenta() + " cargados.");
    }

    /**
     * Manejador del botón "Volver".
     */
    @FXML
    private void volver(ActionEvent event) {
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

    /**
     * Carga datos de ejemplo para la estructura base.
     * (ELIMINAR cuando se implemente la consulta a BD)
     */
    private void mostrarDatosEjemplo() {
        ventasData.addAll(
            new VentaItem(1, "2026-05-16 10:30:00", "Cliente A", 1500.00),
            new VentaItem(2, "2026-05-16 11:45:00", "Cliente B", 2300.50),
            new VentaItem(3, "2026-05-16 14:20:00", "Cliente C", 890.75)
        );

        detallesData.addAll(
            new DetalleVentaItem("Pieza X1", 100.00, 20, 16.0, 299.20),
            new DetalleVentaItem("Pieza X2", 200.00, 15, 16.0, 459.20),
            new DetalleVentaItem("Pieza X3", 50.00, 30, 16.0, 77.60)
        );
    }

    // =========================================================================
    // CLASES INTERNAS PARA MODELAR ITEMS DE TABLA
    // =========================================================================

    /**
     * Clase interna para modelar un item de venta en la tabla.
     */
    public static class VentaItem {
        private final int idVenta;
        private final String fecha;
        private final String cliente;
        private final double total;

        public VentaItem(int idVenta, String fecha, String cliente, double total) {
            this.idVenta = idVenta;
            this.fecha = fecha;
            this.cliente = cliente;
            this.total = total;
        }

        public int getIdVenta() { return idVenta; }
        public String getFecha() { return fecha; }
        public String getCliente() { return cliente; }
        public double getTotal() { return total; }
    }

    /**
     * Clase interna para modelar un detalle de venta (producto en venta).
     */
    public static class DetalleVentaItem {
        private final String producto;
        private final double precioCompra;
        private final int utilidadPct;
        private final double ivaPct;
        private final double totalLinea;

        public DetalleVentaItem(String producto, double precioCompra, int utilidadPct, double ivaPct, double totalLinea) {
            this.producto = producto;
            this.precioCompra = precioCompra;
            this.utilidadPct = utilidadPct;
            this.ivaPct = ivaPct;
            this.totalLinea = totalLinea;
        }

        public String getProducto() { return producto; }
        public double getPrecioCompra() { return precioCompra; }
        public int getUtilidadPct() { return utilidadPct; }
        public double getIvaPct() { return ivaPct; }
        public double getTotalLinea() { return totalLinea; }
    }
}
