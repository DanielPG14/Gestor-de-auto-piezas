// Controlador para gestionar el carrito de ventas, permitiendo agregar, modificar y eliminar ítems, así como calcular totales e impuestos.
package com.autopartes.Controlador;

import com.autopartes.Modelo.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class CarritoVentaC {

    @FXML
    private TableView<ItemCarrito> tableViewCarrito;
    @FXML
    private TableColumn<ItemCarrito, String> colNombre;
    @FXML
    private TableColumn<ItemCarrito, Integer> colCantidad;
    @FXML
    private TableColumn<ItemCarrito, Double> colPrecioVenta;
    @FXML
    private TableColumn<ItemCarrito, Double> colIvaPct;
    @FXML
    private TableColumn<ItemCarrito, Double> colTotalLinea;
    @FXML
    private Label lblProductoSeleccionado, lblSubtotal, lblIvaGeneral, lblTotal;
    @FXML
    private TextField txtCantidad;
    @FXML
    private ComboBox<String> comboMetodoPago;

    private final CarritoSingleton carritoSingleton = CarritoSingleton.getInstancia();
    private Pieza piezaSeleccionada;

    @FXML
    public void initialize() {
        configurarTabla();
        tableViewCarrito.setItems(carritoSingleton.getItems());

        carritoSingleton.getItems().addListener((ListChangeListener<? super ItemCarrito>) c -> actualizarTotales());

        if (comboMetodoPago != null) {
            comboMetodoPago.setItems(
                    FXCollections.observableArrayList("Efectivo", "Tarjeta de Crédito/Débito", "Transferencia"));
            comboMetodoPago.getSelectionModel().selectFirst();
        }
        actualizarTotales();
    }

    private void configurarTabla() {
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPieza().getNombre()));
        colCantidad.setCellValueFactory(d -> d.getValue().cantidadProperty().asObject());
        colPrecioVenta.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrecioUnitario()).asObject());
        colIvaPct.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getIvaPct()).asObject());
        colTotalLinea.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalLinea()).asObject());
    }

    @FXML
    private void volverCatalogo(ActionEvent event) {
        GestorVistas.cambiarVista("CatalogoVendedor.fxml");
    }

    @FXML
    private void agregarItemAlCarrito(ActionEvent event) {
        if (piezaSeleccionada == null) {
            mostrarAlerta("Sin selección", "No hay ninguna pieza cargada desde el catálogo.");
            return;
        }
        try {
            int cantidad = txtCantidad != null && !txtCantidad.getText().isEmpty()
                    ? Integer.parseInt(txtCantidad.getText().trim())
                    : 1;
            carritoSingleton.agregarItem(new ItemCarrito(piezaSeleccionada, cantidad));
            piezaSeleccionada = null;
            actualizarTotales();
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Cantidad inválida");
        }
    }

    @FXML
    private void aumentarCantidadItem(ActionEvent event) {
        ItemCarrito seleccion = tableViewCarrito.getSelectionModel().getSelectedItem();
        if (seleccion != null) {
            carritoSingleton.modificarCantidad(seleccion, seleccion.getCantidad() + 1);
            tableViewCarrito.refresh();
        } else {
            mostrarAlerta("Aviso", "Selecciona un producto de la tabla primero.");
        }
    }

    @FXML
    private void disminuirCantidadItem(ActionEvent event) {
        ItemCarrito seleccion = tableViewCarrito.getSelectionModel().getSelectedItem();
        if (seleccion != null) {
            if (seleccion.getCantidad() > 1) {
                carritoSingleton.modificarCantidad(seleccion, seleccion.getCantidad() - 1);
                tableViewCarrito.refresh();
            } else {
                mostrarAlerta("Aviso",
                        "La cantidad mínima es 1. Usa 'Eliminar ítem' si deseas quitarlo completamente.");
            }
        } else {
            mostrarAlerta("Aviso", "Selecciona un producto de la tabla primero.");
        }
    }

    @FXML
    private void eliminarItemSeleccionado(ActionEvent event) {
        ItemCarrito seleccion = tableViewCarrito.getSelectionModel().getSelectedItem();
        if (seleccion != null) {
            carritoSingleton.eliminarItem(seleccion);
            actualizarTotales();
        } else {
            mostrarAlerta("Aviso", "Selecciona un producto de la tabla primero.");
        }
    }

    @FXML
    private void irACheckout(ActionEvent event) {
        if (carritoSingleton.getItems().isEmpty()) {
            mostrarAlerta("Atención", "El carrito está vacío, agrega productos primero.");
            return;
        }
        GestorVistas.cambiarVista("Checkout.fxml");
    }

    @FXML
    private void irAReporte(ActionEvent event) {
        GestorVistas.cambiarVista("ReporteVenta.fxml");
    }

    @FXML
    private void vaciarCarrito(ActionEvent event) {
        carritoSingleton.vaciarCarrito();
        actualizarTotales();
    }

    private void actualizarTotales() {
        double sub = carritoSingleton.getItems().stream().mapToDouble(ItemCarrito::getSubtotal).sum();
        double iva = carritoSingleton.getItems().stream().mapToDouble(ItemCarrito::calcularIva).sum();
        lblSubtotal.setText(String.format("$%.2f", sub));
        lblIvaGeneral.setText(String.format("$%.2f", iva));
        lblTotal.setText(String.format("$%.2f", sub + iva));
    }

    private void mostrarAlerta(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(t);
        a.setContentText(m);
        a.showAndWait();
    }
}