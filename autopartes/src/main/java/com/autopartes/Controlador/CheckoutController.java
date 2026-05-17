package com.autopartes.Controlador;

import com.autopartes.Modelo.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de CHECKOUT - Ejemplo de uso del método procesarVenta.
 * 
 * RESPONSABILIDAD:
 * - Capturar datos del cliente
 * - Capturar método de pago y turno
 * - Validar antes de procesar
 * - Invocar VentaDAO.procesarVenta() con transacción ACID
 * - Manejar éxito/error y dar feedback al usuario
 */
public class CheckoutController {

    @FXML private TextField txtMetodoPago;
    @FXML private TextField txtTurno;
    @FXML private TextField txtUsuarioID;
    @FXML private Button btnConfirmarVenta;
    @FXML private Label lblEstadoTransaccion;

    /**
     * Ejemplo de cómo usar el método procesarVenta.
     * En un caso real, estos parámetros vendrían de:
     * - El carrito (items)
     * - Los campos del formulario (metodoPago, turno, IDusuario)
     * - La sesión del usuario (IDusuarioOperador)
     */
    @FXML
    private void confirmarVenta() {
        // 1. Validar campos
        if (txtMetodoPago.getText().isEmpty() || txtTurno.getText().isEmpty()) {
            mostrarError("Debe completar todos los campos");
            return;
        }

        // 2. Obtener datos del formulario
        String metodoPago = txtMetodoPago.getText();  // Efectivo, Tarjeta, Transferencia
        String turno = txtTurno.getText();             // Matutino, Vespertino
        int IDusuarioOperador = Integer.parseInt(txtUsuarioID.getText());

        // 3. Crear objeto Ticket
        Ticket ticket = new Ticket(
            LocalDateTime.now(),
            0.0,  // Se calculará después
            metodoPago,
            "Pagado"  // Estado: Pendiente, Pagado, Cancelado
        );

        // 4. Construir lista de items del carrito
        // EN PRODUCCIÓN: Estos vendrían del carrito real del usuario
        List<ItemCarrito> items = construirItemsDelCarrito();

        // 5. Calcular monto total
        double montoTotal = items.stream()
                .mapToDouble(ItemCarrito::calcularTotalLinea)
                .sum();
        ticket.setMontoTotal(montoTotal);

        // 6. LLAMAR AL MÉTODO MAESTRO TRANSACCIONAL
        boolean exito = VentaDAO.procesarVenta(ticket, items, montoTotal, turno, IDusuarioOperador);

        // 7. Manejar resultado
        if (exito) {
            mostrarExito("¡Venta procesada exitosamente! Total: $" + String.format("%.2f", montoTotal));
            limpiarFormulario();
        } else {
            mostrarError("Error al procesar la venta. La transacción fue revertida.");
        }
    }

    /**
     * Construye la lista de ItemCarrito desde el carrito real.
     * EN PRODUCCIÓN: Esta información vendría del modelo de carrito compartido.
     */
    private List<ItemCarrito> construirItemsDelCarrito() {
        List<ItemCarrito> items = new ArrayList<>();

        // Ejemplo: Agregar items al carrito
        // ItemCarrito item1 = new ItemCarrito(
        //     1,      // IDpieza
        //     10,     // id_prod_prov
        //     2,      // cantidad
        //     100.0,  // precio_compra
        //     20.0,   // utilidad_pct
        //     16.0,   // iva_pct
        //     120.0   // precio_venta (100 * (1 + 20/100))
        // );
        // items.add(item1);

        return items;
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
