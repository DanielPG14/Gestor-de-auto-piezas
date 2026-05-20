//Controlador para mostrar reportes de ventas
//Se usa en Reporte.fxml
package com.autopartes.Controlador;

import com.autopartes.Modelo.ReporteDAO;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class ReporteC {

    @FXML private DatePicker datePickerInicio;
    @FXML private DatePicker datePickerFin;
    @FXML private Button btnFiltrar;
    @FXML private Label lblTotalAcumulado;

    @FXML private TableView<ReporteDAO.VentaPorPeriodo> tableViewVentas;
    @FXML private TableColumn<ReporteDAO.VentaPorPeriodo, Integer> colIdVenta;
    @FXML private TableColumn<ReporteDAO.VentaPorPeriodo, String> colFecha;
    @FXML private TableColumn<ReporteDAO.VentaPorPeriodo, String> colProducto;
    @FXML private TableColumn<ReporteDAO.VentaPorPeriodo, Integer> colCantidad;
    @FXML private TableColumn<ReporteDAO.VentaPorPeriodo, Double> colTotal;

    @FXML private BarChart<String, Number> ventasCategoriaChart;
    @FXML private CategoryAxis categoriaAxis;
    @FXML private NumberAxis valorAxis;

    private final ReporteDAO reporteDAO = new ReporteDAO();
    private final ObservableList<ReporteDAO.VentaPorPeriodo> ventasData = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        configurarTabla();
        configurarChart();
        cargarFiltrosIniciales();
        cargarVentasPorPeriodo();
        cargarVentasPorCategoria();
    }

    private void configurarTabla() {
        colIdVenta.setCellValueFactory(new PropertyValueFactory<>("idVenta"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colProducto.setCellValueFactory(new PropertyValueFactory<>("producto"));
        colCantidad.setCellValueFactory(new PropertyValueFactory<>("cantidad"));
        colTotal.setCellValueFactory(new PropertyValueFactory<>("total"));

        tableViewVentas.setItems(ventasData);
    }

    private void configurarChart() {
        categoriaAxis.setLabel("Categoría");
        valorAxis.setLabel("Total vendido");
        ventasCategoriaChart.setLegendVisible(false);
        ventasCategoriaChart.setAnimated(false);
    }

    private void cargarFiltrosIniciales() {
        LocalDate hoy = LocalDate.now();
        datePickerFin.setValue(hoy);
        datePickerInicio.setValue(hoy.minusMonths(1));
    }

    @FXML
    private void filtrarVentas(ActionEvent event) {
        cargarVentasPorPeriodo();
        cargarVentasPorCategoria();
    }

    private void cargarVentasPorPeriodo() {
        String inicio = datePickerInicio.getValue().atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String fin = datePickerFin.getValue().atTime(23, 59, 59).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        try {
            ventasData.clear();
            ventasData.addAll(reporteDAO.obtenerVentasPorPeriodo(inicio, fin));
            actualizarTotalAcumulado();
        } catch (Exception e) {
            mostrarAlerta("Error al cargar ventas", "No se pudieron obtener las ventas del periodo seleccionado.");
            e.printStackTrace();
        }
    }

    private void cargarVentasPorCategoria() {
        try {
            ventasCategoriaChart.getData().clear();
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            reporteDAO.obtenerTotalVendidoPorCategoria().forEach(totalPorCategoria -> {
                series.getData().add(new XYChart.Data<>(totalPorCategoria.getCategoria(), totalPorCategoria.getTotalVendido()));
            });
            ventasCategoriaChart.getData().add(series);
        } catch (Exception e) {
            mostrarAlerta("Error al cargar gráfico", "No se pudieron obtener los totales por categoría.");
            e.printStackTrace();
        }
    }

    private void actualizarTotalAcumulado() {
        double total = ventasData.stream().mapToDouble(ReporteDAO.VentaPorPeriodo::getTotal).sum();
        lblTotalAcumulado.setText(String.format("$ %.2f", total));
    }

    @FXML
    private void volverCatalogo(ActionEvent event) {
        GestorVistas.cambiarVista("CatalogoVendedor.fxml");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        javafx.scene.control.Alert alerta = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}
