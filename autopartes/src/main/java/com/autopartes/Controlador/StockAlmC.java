package com.autopartes.Controlador;

import com.autopartes.Modelo.Pieza;
import com.autopartes.Modelo.PiezaDAO;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.util.Optional;

public class StockAlmC extends StockC {

    // Nuevos componentes del formulario (Asegúrate de que coincidan con el FXML)
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtEstante;
    @FXML
    private TextField txtNivel;
    @FXML
    private TextField txtStock;
    @FXML
    private TextField txtCapMax;
    @FXML
    private TextField txtPrecio;
    @FXML
    private TextField txtCodigoProv;
    @FXML
    private ComboBox<String> cmbProveedores;

    private final PiezaDAO piezaDAO = new PiezaDAO();

    @FXML
    public void initialize() {
        super.initialize(); // Ejecuta la carga de tabla y listas del padre
        cargarProveedores();

        // Listener para que al seleccionar un elemento en la tabla, se llenen los
        // campos
        tablaStock.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null)
                cargarDetallesEnFormulario(newSel);
        });
    }

    private void cargarProveedores() {
        cmbProveedores.getItems().clear();
        cmbProveedores.getItems().addAll(piezaDAO.obtenerRazonSocialProveedores());
        if (!cmbProveedores.getItems().isEmpty()) {
            cmbProveedores.getSelectionModel().selectFirst();
        }
    }

    private void cargarDetallesEnFormulario(Pieza p) {
        txtNombre.setText(p.getNombre());
        txtEstante.setText(p.getIdEstante());
        txtNivel.setText(String.valueOf(p.getNivelAsigned()));
        txtStock.setText(String.valueOf(p.getStock()));
        txtCapMax.setText(String.valueOf(p.getCapMax()));
        txtPrecio.setText(String.valueOf(p.getPrecioCompra()));
        txtCodigoProv.setText(p.getCodigoProveedor());
        if (p.getRazonSocialProveedor() != null) {
            cmbProveedores.getSelectionModel().select(p.getRazonSocialProveedor());
        }
    }

    @FXML
    private void agregarPieza() {
        try {
            // Creamos la pieza
            Pieza nueva = new Pieza(
                    txtNombre.getText(),
                    txtEstante.getText(),
                    Integer.parseInt(txtNivel.getText()),
                    Integer.parseInt(txtStock.getText()),
                    Integer.parseInt(txtCapMax.getText()));

            String proveedorSeleccionado = cmbProveedores.getSelectionModel().getSelectedItem();
            if (proveedorSeleccionado == null || proveedorSeleccionado.isBlank()) {
                mostrarAlerta("Error", "Selecciona un proveedor válido de la lista.");
                return;
            }

            // Seteamos los valores extra que el DAO necesita para las tablas intermedias
            nueva.setRazonSocialProveedor(proveedorSeleccionado);
            nueva.setCodigoProveedor(txtCodigoProv.getText());
            nueva.setPrecioCompra(Double.parseDouble(txtPrecio.getText()));

            if (piezaDAO.registrarNuevaPieza(nueva)) {
                mostrarAlerta("Éxito", "Pieza registrada correctamente.");
                limpiarFormulario();
                actualizarTablaYComponentes();
            }
        } catch (Exception e) {
            mostrarAlerta("Error", "Revisa los campos numéricos: " + e.getMessage());
        }
    }

    @FXML
    private void actualizarPieza() {
        Pieza seleccionada = tablaStock.getSelectionModel().getSelectedItem();
        if (seleccionada == null) {
            mostrarAlerta("Error", "Selecciona una pieza de la tabla para actualizar.");
            return;
        }

        try {
            seleccionada.setNombre(txtNombre.getText());
            seleccionada.setIdEstante(txtEstante.getText());
            seleccionada.setNivelAsigned(Integer.parseInt(txtNivel.getText()));
            seleccionada.setStock(Integer.parseInt(txtStock.getText()));
            seleccionada.setCapMax(Integer.parseInt(txtCapMax.getText()));
            seleccionada.setPrecioCompra(Double.parseDouble(txtPrecio.getText()));
            seleccionada.setCodigoProveedor(txtCodigoProv.getText());
            seleccionada.setRazonSocialProveedor(cmbProveedores.getValue());

            if (piezaDAO.actualizarPieza(seleccionada)) {
                mostrarAlerta("Éxito", "Pieza actualizada correctamente.");
                actualizarTablaYComponentes();
                tablaStock.refresh();
            } else {
                mostrarAlerta("Error", "No se pudo actualizar la pieza en la base de datos.");
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "Revisa los valores numéricos: " + e.getMessage());
        }
    }

    @FXML
    private void limpiarFormulario() {
        txtNombre.clear();
        txtEstante.clear();
        txtNivel.clear();
        txtStock.clear();
        txtCapMax.clear();
        txtPrecio.clear();
        txtCodigoProv.clear();
        tablaStock.getSelectionModel().clearSelection();
    }

    @FXML
    private void irAReportes() {
        GestorVistas.cambiarVista("ReporteVentas.fxml");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}