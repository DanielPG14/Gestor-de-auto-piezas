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
        // Asumiendo que tienes un método en tu DAO para obtener nombres o IDs
        cmbProveedores.getItems().addAll("Proveedor A", "Proveedor B", "Proveedor C");
    }

    private void cargarDetallesEnFormulario(Pieza p) {
        txtNombre.setText(p.getNombre());
        txtEstante.setText(String.valueOf(p.getIdEstante()));
        txtNivel.setText(String.valueOf(p.getNivelAsigned()));
        txtStock.setText(String.valueOf(p.getStock()));
        txtCapMax.setText(String.valueOf(p.getCapMax()));
        // txtPrecio y otros campos según tu modelo
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

            // Seteamos los valores extra que el DAO necesita para las tablas intermedias
            nueva.setRazonSocialProveedor(cmbProveedores.getValue());
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

        seleccionada.setNombre(txtNombre.getText());
        seleccionada.setStock(Integer.parseInt(txtStock.getText()));
        // ... setear el resto de campos

        if (piezaDAO.actualizarPieza(seleccionada)) {
            mostrarAlerta("Éxito", "Stock actualizado.");
            actualizarTablaYComponentes();
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