// Controlador para la pantalla de checkout, donde se muestra el resumen de la venta
// y se confirma la transacción. Aquí se maneja la lógica de validación de inventario,
// inserción del ticket, actualización de stock y registro de detalles de venta.
package com.autopartes.Controlador;

import com.autopartes.Modelo.CarritoSingleton;
import com.autopartes.Modelo.ItemCarrito;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public class CheckoutC {

    @FXML private TableView<ItemCarrito> tablaCheckout;
    @FXML private TableColumn<ItemCarrito, String> colNombre;
    @FXML private TableColumn<ItemCarrito, Integer> colCantidad;
    @FXML private TableColumn<ItemCarrito, Double> colPrecio;
    @FXML private TableColumn<ItemCarrito, Double> colTotal;

    @FXML private Label lblSubtotalVenta;
    @FXML private Label lblIvaVenta;
    @FXML private Label lblTotalVenta;

    private final CarritoSingleton carrito = CarritoSingleton.getInstancia();
    private static final String URL = "jdbc:mysql://localhost:3306/dba?serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    @FXML
    public void initialize() {
        colNombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPieza().getNombre()));
        colCantidad.setCellValueFactory(d -> d.getValue().cantidadProperty().asObject());
        colPrecio.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPrecioUnitario()).asObject());
        colTotal.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getTotalLinea()).asObject());

        tablaCheckout.setItems(carrito.getItems());
        cargarTotales();
    }

    private void cargarTotales() {
        double subtotal = carrito.getItems().stream().mapToDouble(ItemCarrito::getSubtotal).sum();
        double iva = carrito.getItems().stream().mapToDouble(ItemCarrito::calcularIva).sum();
        double total = subtotal + iva;

        lblSubtotalVenta.setText(String.format("$%.2f", subtotal));
        lblIvaVenta.setText(String.format("$%.2f", iva));
        lblTotalVenta.setText(String.format("$%.2f", total));
    }

    @FXML
    private void volverCarrito(ActionEvent event) {
        GestorVistas.cambiarVista("CarritoVenta.fxml");
    }

    @FXML
    private void confirmarVenta(ActionEvent event) {
        if (carrito.getItems().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Operación no válida", "El carrito está vacío.");
            return;
        }

        try (Connection conn = java.sql.DriverManager.getConnection(URL, USER, PASSWORD)) {
            conn.setAutoCommit(false); // Activamos control transaccional estricto

            // === 1. VALIDACIÓN PREVIA DE INVENTARIO ===
            String sqlCheckStock = "SELECT stock, nombre FROM piezas WHERE IDpieza = ?";
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheckStock)) {
                for (ItemCarrito item : carrito.getItems()) {
                    psCheck.setInt(1, item.getPieza().getIdPieza());
                    try (ResultSet rs = psCheck.executeQuery()) {
                        if (rs.next()) {
                            int stockActual = rs.getInt("stock");
                            if (item.getCantidad() > stockActual) {
                                conn.rollback(); // Cancelamos cualquier acción previa
                                mostrarAlerta(Alert.AlertType.ERROR, "Inventario Insuficiente", 
                                    "No puedes vender " + item.getCantidad() + " unidades de '" + 
                                    rs.getString("nombre") + "'. Solo quedan " + stockActual + " en almacén.");
                                return;
                            }
                        } else {
                            conn.rollback();
                            mostrarAlerta(Alert.AlertType.ERROR, "Error de Consistencia", 
                                "La pieza con ID " + item.getPieza().getIdPieza() + " no existe en la BD.");
                            return;
                        }
                    }
                }
            }

            // === 2. INSERTAR TICKET MAESTRO ===
            String sqlTicket = "INSERT INTO ticket (Fecha, montoTotal, metodoPago, estado) VALUES (?, ?, ?, ?)";
            PreparedStatement psTicket = conn.prepareStatement(sqlTicket, Statement.RETURN_GENERATED_KEYS);
            psTicket.setTimestamp(1, Timestamp.valueOf(LocalDateTime.now()));
            psTicket.setDouble(2, calcularTotal());
            psTicket.setString(3, "Efectivo"); 
            psTicket.setString(4, "Pagado");   
            psTicket.executeUpdate();

            ResultSet rs = psTicket.getGeneratedKeys();
            int ticketId = 0;
            if (rs.next()) {
                ticketId = rs.getInt(1);
            }

            // === 3. DISMINUIR INVENTARIO (UPDATE STOCK) & REGISTRAR DETALLES ===
            String sqlRestarStock = "UPDATE piezas SET stock = stock - ? WHERE IDpieza = ?";
            String sqlDetalle = "INSERT INTO lista_ticket (IDpieza, id_prod_prov, IDticket, cantidad, subtotal, precio_compra, utilidad_pct, iva_pct, precio_venta, total_linea) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement psRestar = conn.prepareStatement(sqlRestarStock);
                 PreparedStatement psDetalle = conn.prepareStatement(sqlDetalle)) {

                for (ItemCarrito item : carrito.getItems()) {
                    // Lógica para restar existencias
                    psRestar.setInt(1, item.getCantidad());
                    psRestar.setInt(2, item.getPieza().getIdPieza());
                    psRestar.addBatch();

                    // Lógica para el detalle de la venta
                    psDetalle.setInt(1, item.getPieza().getIdPieza());
                    psDetalle.setInt(2, 1); // Proveedor por defecto
                    psDetalle.setInt(3, ticketId);
                    psDetalle.setInt(4, item.getCantidad());
                    psDetalle.setDouble(5, item.getSubtotal());
                    psDetalle.setDouble(6, item.getPieza().getPrecioCompra());
                    psDetalle.setDouble(7, item.getUtilidadPct());
                    psDetalle.setDouble(8, item.getIvaPct());
                    psDetalle.setDouble(9, item.getPrecioUnitario());
                    psDetalle.setDouble(10, item.getTotalLinea());
                    psDetalle.addBatch();
                }

                psRestar.executeBatch();
                psDetalle.executeBatch();
            }

            conn.commit(); // Todo ha salido perfecto, consolidamos los cambios
            
            mostrarAlerta(Alert.AlertType.INFORMATION, "Venta Exitosa", "Ticket #" + ticketId + " generado e inventario actualizado.");
            carrito.vaciarCarrito();
            GestorVistas.cambiarVista("CatalogoVendedor.fxml");

        } catch (SQLException e) {
            System.err.println("Error en la transacción: " + e.getMessage());
            mostrarAlerta(Alert.AlertType.ERROR, "Error SQL", "Fallo al procesar: " + e.getMessage());
        }
    }

    private double calcularTotal() {
        return carrito.getItems().stream().mapToDouble(ItemCarrito::getTotalLinea).sum();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }
}