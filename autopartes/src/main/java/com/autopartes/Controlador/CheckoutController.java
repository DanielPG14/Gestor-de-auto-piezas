package com.autopartes.Controlador;

import com.autopartes.Modelo.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CheckoutController {

    @FXML private TextField txtMetodoPago;
    @FXML private TextField txtTurno;
    @FXML private TextField txtUsuarioID;
    @FXML private Button btnConfirmarVenta;
    @FXML private Label lblEstadoTransaccion;

    @FXML
    private void confirmarVenta() {
        if (txtMetodoPago.getText().isEmpty() || txtTurno.getText().isEmpty() || txtUsuarioID.getText().isEmpty()) {
            mostrarError("Debe completar todos los campos");
            return;
        }

        try {
            String metodoPago = txtMetodoPago.getText();
            String turno = txtTurno.getText();
            int IDusuarioOperador = Integer.parseInt(txtUsuarioID.getText());

            Ticket ticket = new Ticket(
                LocalDateTime.now(),
                0.0,
                metodoPago,
                "Pagado"
            );

            // Obtenemos los items REALES del Singleton
            List<ItemCarrito> items = construirItemsDelCarrito();

            if (items.isEmpty()) {
                mostrarError("El carrito está vacío, no se puede procesar la venta.");
                return;
            }

            double montoTotal = items.stream().mapToDouble(ItemCarrito::getTotalLinea).sum();
            ticket.setMontoTotal(montoTotal);

            boolean exito = VentaDAO.procesarVenta(ticket, items, montoTotal, turno, IDusuarioOperador);

            if (exito) {
                mostrarExito("¡Venta procesada exitosamente! Total: $" + String.format("%.2f", montoTotal));
                limpiarFormulario();
                // Si la venta se guardó, vaciamos el carrito del sistema
                CarritoSingleton.getInstancia().vaciarCarrito();
            } else {
                mostrarError("Error al procesar la venta. La transacción fue revertida.");
            }

        } catch (NumberFormatException e) {
            mostrarError("El ID de Usuario debe ser un número válido.");
        }
    }

    private List<ItemCarrito> construirItemsDelCarrito() {
        // Tomamos los productos que están guardados en memoria globalmente
        return new ArrayList<>(CarritoSingleton.getInstancia().getItems());
    }

    private void mostrarExito(String mensaje) {
        lblEstadoTransaccion.setText(mensaje);
        lblEstadoTransaccion.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
    }

    private void mostrarError(String mensaje) {
        lblEstadoTransaccion.setText(mensaje);
        lblEstadoTransaccion.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
    }

    private void limpiarFormulario() {
        txtMetodoPago.clear();
        txtTurno.clear();
        txtUsuarioID.clear();
    }
}