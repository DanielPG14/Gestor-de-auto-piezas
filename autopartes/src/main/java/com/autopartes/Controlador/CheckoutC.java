package com.autopartes.Controlador;

import com.autopartes.Modelo.*;
// 💡 Agrega aquí los paquetes correctos de tu DAO y tus utilidades de vistas:

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * CheckoutC: Controlador para la vista de Checkout (Pago y Generación de Factura)
 */
public class CheckoutC {

    // === COMPONENTES UI PARA DATOS DEL CLIENTE ===
    @FXML private TextField txtRazonSocial;
    @FXML private TextField txtRFC;
    @FXML private TextField txtDireccionFiscal;

    // === COMPONENTES UI PARA TOTALES ===
    @FXML private Label lblSubtotalVenta;
    @FXML private Label lblIvaVenta;
    @FXML private Label lblTotalVenta;

    // === BOTONES DE ACCIÓN ===
    @FXML private Button btnConfirmarVenta;
    @FXML private Button btnVolverCarrito;

    // === PROPIEDADES INTERNAS ===
    private ObservableList<CarritoVentaC.ItemCarrito> itemsCarrito;
    private double subtotalVenta = 0;
    private double ivaVenta = 0;
    private double totalVenta = 0;

    // 💡 Instanciamos el DAO de manera limpia para evitar errores de contexto estático
    private final VentaDAO ventaDAO = new VentaDAO();

    /**
     * Inicializa el controlador con datos del carrito.
     */
    public void inicializarDatos(ObservableList<CarritoVentaC.ItemCarrito> items, 
                                 double subtotal, double iva, double total) {
        this.itemsCarrito = items;
        this.subtotalVenta = subtotal;
        this.ivaVenta = iva;
        this.totalVenta = total;

        actualizarEtiquetasTotales();
    }

    private void actualizarEtiquetasTotales() {
        lblSubtotalVenta.setText(String.format("$%.2f", subtotalVenta));
        lblIvaVenta.setText(String.format("$%.2f", ivaVenta));
        lblTotalVenta.setText(String.format("$%.2f", totalVenta));
    }

    /**
     * Manejador del botón "Confirmar Venta".
     */
    @FXML
    private void confirmarVenta(ActionEvent event) {
        try {
            if (!validarDatosCliente()) {
                mostrarAlerta("Validación", "Por favor, complete todos los datos del cliente.");
                return;
            }

            if (itemsCarrito == null || itemsCarrito.isEmpty()) {
                mostrarAlerta("Carrito vacío", "No hay artículos para procesar.");
                return;
            }

            String razonSocial = txtRazonSocial.getText().trim();
            String rfc = txtRFC.getText().trim();
            String direccion = txtDireccionFiscal.getText().trim();

            String turno = obtenerTurnoActual();  
            int IDusuarioOperador = obtenerIDUsuarioActual();  

            List<ItemCarrito> items = convertirItemsCarrito(itemsCarrito);

            Ticket ticket = new Ticket(
                LocalDateTime.now(),
                totalVenta,
                "Efectivo",  
                "Pagado"    
            );

            // 💡 Cambiado a 'ventaDAO' (instancia) en lugar de la Clase estática
            boolean exito = this.ventaDAO.procesarVenta(ticket, items, totalVenta, turno, IDusuarioOperador);

            if (exito) {
                String ticketTexto = generarTicket(-1, razonSocial, rfc, direccion);
                guardarTicketEnArchivo(ticketTexto, -1);

                mostrarAlerta("Éxito", "Venta procesada exitosamente.\nTotal: $" + String.format("%.2f", totalVenta));
                limpiarFormulario();
                
                // Redirección activa si lo requieres:
                GestorVistas.cambiarVista("CatalogoVendedor.fxml");
            } else {
                mostrarAlerta("Error", "No se pudo procesar la venta. La transacción fue revertida.");
            }

        } catch (Exception e) {
            System.err.println("ERROR en confirmarVenta: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error inesperado", "Ocurrió un error: " + e.getMessage());
        }
    }

    /**
     * Convierte ObservableList a List para el DAO.
     */
    private List<ItemCarrito> convertirItemsCarrito(ObservableList<CarritoVentaC.ItemCarrito> items) {
        List<ItemCarrito> listaItems = new ArrayList<>();
        
        for (CarritoVentaC.ItemCarrito itemCarrito : items) {
            Pieza pieza = itemCarrito.getPieza();
            
            ItemCarrito item = new ItemCarrito(
                pieza.getIdPieza(),           
                obtenerIdProdProv(pieza),     
                1,                            
                pieza.getPrecioCompra(),       
                itemCarrito.getUtilidadPct(),  
                itemCarrito.getIvaPct(),       
                itemCarrito.getPrecioVenta()   
            );
            
            listaItems.add(item);
        }
        return listaItems;
    }

    private int obtenerIdProdProv(Pieza pieza) {
        // 💡 Dejamos la pieza por si planeas meter la consulta real a la BD después
        return 1;  
    }

    private String obtenerTurnoActual() {
        int hora = LocalDateTime.now().getHour();
        return hora < 14 ? "Matutino" : "Vespertino";
    }

    private int obtenerIDUsuarioActual() {
        return 1;
    }

    private boolean validarDatosCliente() {
        return txtRazonSocial.getText() != null && !txtRazonSocial.getText().trim().isEmpty() &&
               txtRFC.getText() != null && !txtRFC.getText().trim().isEmpty() &&
               txtDireccionFiscal.getText() != null && !txtDireccionFiscal.getText().trim().isEmpty();
    }

    /**
     * Genera la estructura del ticket/factura como String.
     */
    private String generarTicket(int idVenta, String razonSocial, String rfc, String direccion) {
        StringBuilder ticket = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        ticket.append("═══════════════════════════════════════════════════\n");
        ticket.append("                      TICKET DE VENTA\n");
        ticket.append("═══════════════════════════════════════════════════\n\n");
        ticket.append(String.format("ID Venta: %d\n", idVenta));
        ticket.append(String.format("Fecha: %s\n", LocalDateTime.now().format(formatter)));
        ticket.append("\n");

        ticket.append("--- CLIENTE ---\n");
        ticket.append(String.format("Razón Social: %s\n", razonSocial));
        ticket.append(String.format("RFC: %s\n", rfc));
        ticket.append(String.format("Dirección: %s\n", direccion));
        ticket.append("\n");

        ticket.append("--- DETALLE DE PRODUCTOS ---\n");
        ticket.append(String.format("%-30s | %10s | %8s | %6s | %10s\n", 
            "Producto", "Costo Base", "Utilidad", "IVA%", "Total"));
        ticket.append("─".repeat(80) + "\n");

        for (CarritoVentaC.ItemCarrito item : itemsCarrito) {
            String nombre = item.getPieza().getNombre();
            double precioBase = item.getPieza().getPrecioCompra();
            int utilidad = item.getUtilidadPct();
            double ivaPct = item.getIvaPct();
            double totalLinea = item.getTotalLinea();

            // 💡 Se eliminó la variable local 'ivaCalculado' que no se usaba para quitar el warning

            ticket.append(String.format("%-30s | $%9.2f | %7d%% | %5.1f%% | $%9.2f\n",
                nombre.length() > 30 ? nombre.substring(0, 27) + "..." : nombre,
                precioBase, utilidad, ivaPct, totalLinea));
        }

        ticket.append("─".repeat(80) + "\n");

        ticket.append(String.format("%-50s Subtotal: $%9.2f\n", "", subtotalVenta));
        ticket.append(String.format("%-50s IVA:      $%9.2f\n", "", ivaVenta));
        ticket.append("═".repeat(80) + "\n");
        ticket.append(String.format("%-50s TOTAL:    $%9.2f\n", "", totalVenta));
        ticket.append("═".repeat(80) + "\n\n");
        ticket.append("¡Gracias por su compra!\n");
        ticket.append("═══════════════════════════════════════════════════\n");

        return ticket.toString();
    }

    private void guardarTicketEnArchivo(String ticket, int idVenta) {
        try {
            String nombreArchivo = String.format("Ticket_Venta_%d_%s.txt", 
                idVenta, System.currentTimeMillis());
            
            try (FileWriter writer = new FileWriter(nombreArchivo)) {
                writer.write(ticket);
                System.out.println("Ticket guardado en: " + nombreArchivo);
            }
        } catch (IOException e) {
            System.err.println("Error al guardar el ticket: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void limpiarFormulario() {
        txtRazonSocial.clear();
        txtRFC.clear();
        txtDireccionFiscal.clear();
        itemsCarrito.clear();
        actualizarEtiquetasTotales();
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.INFORMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    /**
     * Manejador del botón "Volver al Carrito".
     */
    @FXML
    private void volverCarrito(ActionEvent event) {
        GestorVistas.cambiarVista("CarritoVenta.fxml");
    }
}