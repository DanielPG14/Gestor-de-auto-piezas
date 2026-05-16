package com.autopartes.Controlador;

import com.autopartes.Modelo.Conexion;
import com.autopartes.Modelo.Pieza;
import com.autopartes.Modelo.PiezaDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage; // 👈 CORRECCIÓN 1: Importación de JavaFX añadida

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList; // 👈 CORRECCIÓN 1: Importaciones de Java Util añadidas
import java.util.List;

public class AgregarPiezaC {
    @FXML private TextField txtNombre;
    @FXML private TextField txtStock;
    @FXML private TextField txtNivel;
    @FXML private ComboBox<String> cbProveedor;
    @FXML private TextField txtPrecioCompra;
    @FXML private TextField txtCodigoProveedor;
    @FXML private TextField txtEstante; // 👈 CORRECCIÓN 3: Añadido campo para capturar el ID del estante físico
    @FXML private Button btnAgregarPieza;

    private PiezaDAO piezaDAO = new PiezaDAO();
    private List<String> proveedores;

    @FXML
    public void initialize() {
        // CORRECCIÓN 2: Cargamos los proveedores de forma segura
        proveedores = obtenerProveedores();
        cbProveedor.setItems(FXCollections.observableArrayList(proveedores));
        
        if (!proveedores.isEmpty()) {
            cbProveedor.getSelectionModel().selectFirst();
        }

        btnAgregarPieza.setOnAction(event -> guardarPieza());
    }

    private List<String> obtenerProveedores() {
        // CORRECCIÓN 2: Si el método aún no está en el DAO, manejamos una lista de respaldo para evitar que la app truene
        List<String> lista = piezaDAO.obtenerRazonSocialProveedores();
        if (lista == null || lista.isEmpty()) {
            lista = new ArrayList<>();
            lista.add("Proveedor General S.A."); // Respaldo temporal si la tabla proveedor está vacía
        }
        return lista;
    }

    @FXML
    private void guardarPieza() {
        try {
            String nombre = txtNombre.getText().trim();
            String estante = txtEstante != null ? txtEstante.getText().trim() : "1"; // Protección de IDestante
            String proveedor = cbProveedor.getSelectionModel().getSelectedItem();
            String codigoProveedor = txtCodigoProveedor.getText().trim();
            
            if (nombre.isEmpty() || txtStock.getText().isEmpty() || txtNivel.getText().isEmpty() || 
                proveedor == null || txtPrecioCompra.getText().isEmpty() || codigoProveedor.isEmpty() || estante.isEmpty()) {
                mostrarAlerta("Campos obligatorios", "Por favor, complete todos los campos del formulario.");
                return;
            }

            int stock = Integer.parseInt(txtStock.getText().trim());
            int nivel = Integer.parseInt(txtNivel.getText().trim());
            double precioCompra = Double.parseDouble(txtPrecioCompra.getText().trim());

            if (stock < 0 || nivel <= 0 || precioCompra <= 0) {
                mostrarAlerta("Valores inválidos", "El stock, nivel y precio deben ser mayores a cero.");
                return;
            }

            // CORRECCIÓN 3: Pasamos 'estante' en lugar de 'null' para cumplir con la firma del constructor y la FK de la BD
            Pieza nuevaPieza = new Pieza(0, nombre, proveedor, codigoProveedor, precioCompra, "default.jpg", estante, nivel, stock, 100);

            boolean resultado = piezaDAO.registrarNuevaPieza(nuevaPieza);
            if (resultado) {
                System.out.println("Pieza registrada exitosamente en base de datos.");
                cerrarModal();
            } else {
                mostrarAlerta("Error de persistencia", "No se pudo guardar la pieza. Verifique que el ID del estante y Proveedor existan en la base de datos.");
            }
        } catch (NumberFormatException e) {
            mostrarAlerta("Formato incorrecto", "Asegúrese de que los campos numéricos (Stock, Nivel, Precio) no contengan letras.");
        } catch (Exception e) {
            e.printStackTrace();
            mostrarAlerta("Error inesperado", "Se produjo un error al procesar el formulario.");
        }
    }

    private void cerrarModal() {
        Stage stage = (Stage) btnAgregarPieza.getScene().getWindow();
        if (stage != null) {
            stage.close();
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}