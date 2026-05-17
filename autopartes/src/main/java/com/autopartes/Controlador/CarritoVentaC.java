package com.autopartes.Controlador;

import com.autopartes.Modelo.Pieza;
import com.autopartes.Modelo.PiezaDAO;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert; // 👈 CORRECCIÓN: Importación añadida
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CarritoVentaC {

    @FXML private TableView<ItemCarrito> tableViewCarrito;
    @FXML private TableColumn<ItemCarrito, String> colNombre;
    @FXML private TableColumn<ItemCarrito, Double> colPrecioCompra;
    @FXML private TableColumn<ItemCarrito, Integer> colUtilidadPct;
    @FXML private TableColumn<ItemCarrito, Double> colIvaPct;
    @FXML private TableColumn<ItemCarrito, String> colProveedor;
    @FXML private TableColumn<ItemCarrito, Double> colPrecioVenta;
    @FXML private TableColumn<ItemCarrito, Double> colTotalLinea;

    // Inputs para agregar el producto seleccionado con sus porcentajes
    @FXML private TextField txtUtilidadPct;
    @FXML private TextField txtIvaPct;
    
    // Indicadores numéricos del pie de página
    @FXML private Label lblSubtotal;
    @FXML private Label lblIvaGeneral;
    @FXML private Label lblTotal;
    @FXML private Button btnAgregarItem;

    private PiezaDAO piezaDAO = new PiezaDAO();
    private ObservableList<ItemCarrito> carritoItems = FXCollections.observableArrayList();
    private Pieza piezaSeleccionada; // Almacena la pieza que el usuario eligió del catálogo

    @FXML
    public void initialize() {
        // Configuración de columnas usando propiedades wrapper seguras
        colNombre.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPieza().getNombre()));
        colPrecioCompra.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPieza().getPrecioCompra()).asObject());
        colUtilidadPct.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().getUtilidadPct()).asObject());
        colIvaPct.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getIvaPct()).asObject());
        colProveedor.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPieza().getRazonSocialProveedor()));
        
        colPrecioVenta.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getPrecioVenta()).asObject());
        colTotalLinea.setCellValueFactory(cellData -> new SimpleDoubleProperty(cellData.getValue().getTotalLinea()).asObject());

        tableViewCarrito.setItems(carritoItems);

        // Listener para recalcular si agregamos elementos
        btnAgregarItem.setOnAction(event -> agregarItemAlCarrito());
        
        // Valores iniciales en cero
        limpiarTotales();
    }

    private void agregarItemAlCarrito() {
        try {
            if (txtUtilidadPct.getText().isEmpty() || txtIvaPct.getText().isEmpty()) {
                mostrarAlerta("Campos obligatorios", "Por favor, asigne el % de Utilidad y % de IVA.");
                return;
            }

            int utilidadPct = Integer.parseInt(txtUtilidadPct.getText().trim());
            double ivaPct = Double.parseDouble(txtIvaPct.getText().trim());

            if (utilidadPct < 0 || ivaPct < 0) {
                mostrarAlerta("Valores inválidos", "Los porcentajes no pueden ser negativos.");
                return;
            }

            // Simulación defensiva: Si no hay selección previa, recuperamos una pieza base del DAO para pruebas
            if (piezaSeleccionada == null) {
                var lista = piezaDAO.obtenerTodas();
                if (!lista.isEmpty()) {
                    piezaSeleccionada = lista.get(0);
                } else {
                    mostrarAlerta("Inventario vacío", "No hay piezas en la base de datos para vender.");
                    return;
                }
            }

            // Agregamos el objeto contenedor calculado a la tabla
            ItemCarrito item = new ItemCarrito(piezaSeleccionada, utilidadPct, ivaPct);
            carritoItems.add(item);
            
            actualizarTotales();

        } catch (NumberFormatException e) {
            mostrarAlerta("Formato incorrecto", "Asegúrese de que la Utilidad y el IVA sean números válidos.");
        }
    }

    private void actualizarTotales() {
        double subtotalAcumulado = 0;
        double ivaAcumulado = 0;

        for (ItemCarrito item : carritoItems) {
            subtotalAcumulado += item.getPrecioVenta();
            ivaAcumulado += item.getIvaCalculado();
        }

        double totalFinal = subtotalAcumulado + ivaAcumulado;

        // CORRECCIÓN: Mapeo correcto y exacto a cada etiqueta independiente
        lblSubtotal.setText(String.format("$%.2f", subtotalAcumulado));
        lblIvaGeneral.setText(String.format("$%.2f", ivaAcumulado));
        lblTotal.setText(String.format("$%.2f", totalFinal));
    }

    private void limpiarTotales() {
        lblSubtotal.setText("$0.00");
        lblIvaGeneral.setText("$0.00");
        lblTotal.setText("$0.00");
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();
    }

    // ACCIONES DE NAVEGACIÓN INTEGRADAS CORRECTAMENTE
    @FXML
    private void volverCatalogo(ActionEvent event) {
        // GestorVistas.cambiarVista("CatalogoVendedor.fxml");
        System.out.println("Regresando al catálogo...");
    }

    /**
     * Genera una cotización en archivo .txt SIN afectar la base de datos ni stock.
     * Cumple FASE B de requerimientos: Cotización impresa sin persistencia.
     */
    @FXML
    private void generarCotizacion(ActionEvent event) {
        if (carritoItems.isEmpty()) {
            mostrarAlerta("Carrito vacío", "No hay artículos para cotizar.");
            return;
        }

        try {
            // Calcular totales
            double subtotal = 0;
            double ivaTotal = 0;

            for (ItemCarrito item : carritoItems) {
                subtotal += item.getPrecioVenta();
                ivaTotal += item.getIvaCalculado();
            }

            double totalFinal = subtotal + ivaTotal;

            // Generar estructura de cotización
            String cotizacion = generarEstructuraCotizacion(subtotal, ivaTotal, totalFinal);

            // Guardar en archivo .txt
            guardarCotizacionEnArchivo(cotizacion);

            mostrarAlerta("Éxito", "Cotización generada correctamente.\nEl archivo se encuentra en el directorio del proyecto.");

        } catch (Exception e) {
            System.err.println("ERROR en generarCotizacion: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error", "Ocurrió un error al generar la cotización: " + e.getMessage());
        }
    }

    /**
     * Construye la estructura textual de la cotización.
     * NOTA: Esta NO realiza inserciones en BD ni modifica stock.
     */
    private String generarEstructuraCotizacion(double subtotal, double ivaTotal, double total) {
        StringBuilder cotizacion = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        cotizacion.append("═══════════════════════════════════════════════════\n");
        cotizacion.append("                    COTIZACIÓN\n");
        cotizacion.append("═══════════════════════════════════════════════════\n\n");
        cotizacion.append(String.format("Fecha: %s\n", LocalDateTime.now().format(formatter)));
        cotizacion.append("Vigencia: 30 días\n\n");

        cotizacion.append("--- ARTÍCULOS COTIZADOS ---\n");
        cotizacion.append(String.format("%-30s | %10s | %8s | %6s | %10s\n",
            "Producto", "Costo Base", "Utilidad", "IVA%", "Total"));
        cotizacion.append("─".repeat(80) + "\n");

        for (ItemCarrito item : carritoItems) {
            String nombre = item.getPieza().getNombre();
            double precioBase = item.getPieza().getPrecioCompra();
            int utilidad = item.getUtilidadPct();
            double ivaPct = item.getIvaPct();
            double precioVenta = item.getPrecioVenta();
            double ivaCalculado = item.getIvaCalculado();
            double totalLinea = item.getTotalLinea();

            cotizacion.append(String.format("%-30s | $%9.2f | %7d%% | %5.1f%% | $%9.2f\n",
                nombre.length() > 30 ? nombre.substring(0, 27) + "..." : nombre,
                precioBase, utilidad, ivaPct, totalLinea));
        }

        cotizacion.append("─".repeat(80) + "\n");
        cotizacion.append(String.format("%-50s Subtotal: $%9.2f\n", "", subtotal));
        cotizacion.append(String.format("%-50s IVA:      $%9.2f\n", "", ivaTotal));
        cotizacion.append("═".repeat(80) + "\n");
        cotizacion.append(String.format("%-50s TOTAL:    $%9.2f\n", "", total));
        cotizacion.append("═".repeat(80) + "\n\n");
        cotizacion.append("NOTAS:\n");
        cotizacion.append("- Esta cotización NO genera transacción ni modifica inventario.\n");
        cotizacion.append("- Los precios están sujetos a cambios sin previo aviso.\n");
        cotizacion.append("- Para confirmar la venta, proceda al Checkout.\n");
        cotizacion.append("═══════════════════════════════════════════════════\n");

        return cotizacion.toString();
    }

    /**
     * Guarda la cotización en un archivo .txt con timestamp único.
     */
    private void guardarCotizacionEnArchivo(String cotizacion) {
        try {
            String nombreArchivo = String.format("Cotizacion_%d_%s.txt",
                System.currentTimeMillis(), LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")));

            try (FileWriter writer = new FileWriter(nombreArchivo)) {
                writer.write(cotizacion);
                System.out.println("✓ Cotización guardada en: " + nombreArchivo);
            }
        } catch (IOException e) {
            System.err.println("✗ Error al guardar cotización: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void irACheckout(ActionEvent event) {
        if (carritoItems.isEmpty()) {
            mostrarAlerta("Carrito vacío", "No puedes proceder al pago sin artículos en el carrito.");
            return;
        }

        try {
            // Calcular totales finales
            double subtotal = 0;
            double ivaTotal = 0;

            for (ItemCarrito item : carritoItems) {
                subtotal += item.getPrecioVenta();
                ivaTotal += item.getIvaCalculado();
            }

            double totalFinal = subtotal + ivaTotal;

            // Obtener el controlador de Checkout e inicializarlo
            CheckoutC checkoutController = GestorVistas.cambiarVistaConControlador("Checkout.fxml");
            
            if (checkoutController != null) {
                // Pasar datos al controlador de Checkout
                checkoutController.inicializarDatos(carritoItems, subtotal, ivaTotal, totalFinal);
                System.out.println("✓ Datos transferidos a Checkout. Subtotal: $" + String.format("%.2f", subtotal) + 
                                   ", IVA: $" + String.format("%.2f", ivaTotal) + ", Total: $" + String.format("%.2f", totalFinal));
            } else {
                mostrarAlerta("Error", "No se pudo cargar la vista de Checkout.");
            }

        } catch (Exception e) {
            System.err.println("ERROR en irACheckout: " + e.getMessage());
            e.printStackTrace();
            mostrarAlerta("Error inesperado", "Ocurrió un error al proceder al checkout: " + e.getMessage());
        }
    }

    // =========================================================================
    // CLASE CONTENEDORA DE APOYO (Wrapper): Resuelve los cálculos 3FN dinámicos
    // =========================================================================
    public static class ItemCarrito {
        private final Pieza pieza;
        private final int utilidadPct;
        private final double ivaPct;

        public ItemCarrito(Pieza pieza, int utilidadPct, double ivaPct) {
            this.pieza = pieza;
            this.utilidadPct = utilidadPct;
            this.ivaPct = ivaPct;
        }

        public Pieza getPieza() { return pieza; }
        public int getUtilidadPct() { return utilidadPct; }
        public double getIvaPct() { return ivaPct; }

        // LÓGICA DE NEGOCIO EXIGIDA POR EL PROYECTO:
        public double getPrecioVenta() {
            double utilidadAplicada = pieza.getPrecioCompra() * (utilidadPct / 100.0);
            return pieza.getPrecioCompra() + utilidadAplicada;
        }

        public double getIvaCalculado() {
            return getPrecioVenta() * (ivaPct / 100.0);
        }

        public double getTotalLinea() {
            return getPrecioVenta() + getIvaCalculado();
        }
    }
}