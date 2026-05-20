//Controlador para mostrar el historial de ventas
//Se usa en ReporteVenta.fxml
package com.autopartes.Controlador;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReporteVentaC {

    @FXML private TableView<VentaItem> tableViewVentas;
    @FXML private TableColumn<VentaItem, Integer> colIdVenta;
    @FXML private TableColumn<VentaItem, String> colFecha;
    @FXML private TableColumn<VentaItem, Double> colTotal;

    private ObservableList<VentaItem> ventasData;

    // Configuración de conexión idéntica a tu Checkout
    private static final String URL = "jdbc:mysql://localhost:3306/dba?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @FXML
    public void initialize() {
        ventasData = FXCollections.observableArrayList();

        colIdVenta.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getIdVenta()).asObject());
        colFecha.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getFecha()));
        colTotal.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());

        tableViewVentas.setItems(ventasData);
        cargarVentasDesdeBD();
    }

    private void cargarVentasDesdeBD() {
        ventasData.clear();
        String sql = "SELECT IDticket, Fecha, montoTotal FROM ticket ORDER BY IDticket DESC";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("IDticket");
                String fecha = rs.getString("Fecha");
                double total = rs.getDouble("montoTotal");
                ventasData.add(new VentaItem(id, fecha, total));
            }

        } catch (SQLException e) {
            System.err.println("Error al cargar historial de reportes: " + e.getMessage());
        }
    }

    @FXML
    private void volverAtras(ActionEvent event) {
        GestorVistas.cambiarVista("CatalogoVendedor.fxml");
    }

    // POJO para mapeo de celdas
    //POJO: Plain Old Java Object, es una clase simple que se utiliza para representar datos sin lógica adicional. En este caso, VentaItem es un POJO que representa una venta con sus atributos idVenta, fecha y total.
    public static class VentaItem {
        private final int idVenta;
        private final String fecha;
        private final double total;

        public VentaItem(int idVenta, String fecha, double total) {
            this.idVenta = idVenta;
            this.fecha = fecha;
            this.total = total;
        }

        public int getIdVenta() { return idVenta; }
        public String getFecha() { return fecha; }
        public double getTotal() { return total; }
    }
}