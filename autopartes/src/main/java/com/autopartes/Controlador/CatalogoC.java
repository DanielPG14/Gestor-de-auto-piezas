package com.autopartes.Controlador;

import com.autopartes.Modelo.Pieza;
import com.autopartes.Modelo.PiezaDAO;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import java.util.ArrayList;
import java.util.List;

// Importaciones de soporte asumidas del proyecto (Ajusta los paquetes si cambian)
// import com.autopartes.Utilidades.GestorVistas;
// import com.autopartes.Utilidades.Sesion;

public class CatalogoC {

    @FXML
    private FlowPane panelProductos;
    @FXML
    private TextField txtBuscarGeneral;

    private PiezaDAO piezaDAO = new PiezaDAO();
    private List<Pieza> listaMaestra = new ArrayList<>();

    @FXML
    public void initialize() {
        // Carga los datos mapeados desde la consulta con INNER JOIN
        listaMaestra = piezaDAO.obtenerTodas();
        renderizarCatalogo(listaMaestra);
        
        // Listener en tiempo real para el buscador
        txtBuscarGeneral.textProperty().addListener((observable, oldValue, newValue) -> {
            ejecutarFiltro(newValue);
        });
    }

    private void renderizarCatalogo(List<Pieza> listaAProcesar) {
        panelProductos.getChildren().clear();
        for (Pieza p : listaAProcesar) {
            VBox tarjeta = crearTarjeta(p);
            panelProductos.getChildren().add(tarjeta);
        }
    }

    private void ejecutarFiltro(String textoBusqueda) {
        if (textoBusqueda == null || textoBusqueda.isEmpty()) {
            renderizarCatalogo(listaMaestra);
            return;
        }

        String filtro = textoBusqueda.toLowerCase().trim();
        List<Pieza> listaFiltrada = new ArrayList<>();
        
        for (Pieza p : listaMaestra) {
            // CORRECCIÓN: Validaciones defensivas contra valores nulos en BD
            boolean coincideNombre = p.getNombre() != null && p.getNombre().toLowerCase().contains(filtro);
            boolean coincideProveedor = p.getRazonSocialProveedor() != null && p.getRazonSocialProveedor().toLowerCase().contains(filtro);
            boolean coincideCodigo = p.getCodigoProveedor() != null && p.getCodigoProveedor().toLowerCase().contains(filtro);

            if (coincideNombre || coincideProveedor || coincideCodigo) {
                listaFiltrada.add(p);
            }
        }

        renderizarCatalogo(listaFiltrada);
    }

    @FXML
    void filtrarCatalogo() {
        ejecutarFiltro(txtBuscarGeneral.getText());
    }

    private VBox crearTarjeta(Pieza p) {
        // CORRECCIÓN: Agregadas todas las importaciones visuales de JavaFX correspondientes
        VBox vbox = new VBox(5);
        vbox.setPrefWidth(250);
        vbox.getStyleClass().add("tarjeta-stock-normal");
        vbox.setPadding(new Insets(20));

        Label lblStatus = new Label("DISPONIBLE");

        HBox hbImagen = new HBox();
        hbImagen.setAlignment(Pos.CENTER);
        ImageView img = new ImageView();
        img.setFitHeight(140);
        img.setFitWidth(200);
        img.setPreserveRatio(true);

        try {
            String rutaImagen = p.getImagen();
            if (rutaImagen != null && (rutaImagen.startsWith("http://") || rutaImagen.startsWith("https://"))) {
                img.setImage(new Image(rutaImagen, true));
            } else {
                String nombreArchivo = rutaImagen != null ? rutaImagen : "default.jpg";
                String rutaImgLocal = "/com/autopartes/vistas/images/" + nombreArchivo;
                var stream = getClass().getResourceAsStream(rutaImgLocal);
                if (stream != null) {
                    img.setImage(new Image(stream));
                } else {
                    var defaultStream = getClass().getResourceAsStream("/com/autopartes/vistas/images/default.jpg");
                    if (defaultStream != null)
                        img.setImage(new Image(defaultStream));
                }
            }
        } catch (Exception e) {
            System.err.println("Error crítico al cargar imagen: " + e.getMessage());
        }

        hbImagen.getChildren().add(img);

        // Muestra el nombre del proveedor en lugar de una etiqueta genérica estática si lo deseas
        String marcaProveedor = (p.getRazonSocialProveedor() != null) ? p.getRazonSocialProveedor().toUpperCase() : "REPUESTO";
        Label lblMarca = new Label(marcaProveedor);
        lblMarca.setStyle("-fx-font-size: 11px; -fx-text-fill: gray;");
        
        Label lblNombre = new Label(p.getNombre());
        lblNombre.setWrapText(true);
        lblNombre.setStyle("-fx-font-weight: bold; -fx-min-height: 40px;");

        // Pintamos el precio de costo base recuperado del historial vigente
        Label lblPrecioCosto = new Label("$" + String.format("%.2f", p.getPrecioCompra()));
        lblPrecioCosto.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 16px; -fx-font-weight: bold;");

        vbox.getChildren().addAll(lblStatus, hbImagen, lblMarca, lblNombre, lblPrecioCosto);

        return vbox;
    }

    // Métodos de navegación y sesión (Descomenta o ajusta según tus clases de utilidad reales)
    @FXML
    private void irACatalogo(ActionEvent event) {
        // GestorVistas.cambiarVista("CatalogoVendedor.fxml");
    }

    @FXML
    private void irAStock(ActionEvent event) {
        // GestorVistas.cambiarVista("VistaStock.fxml");
    }

    @FXML
    private void irACarrito(ActionEvent event) {
        // GestorVistas.cambiarVista("CarritoVenta.fxml");
    }

    @FXML
    private void irACerter(ActionEvent event) {
        // GestorVistas.cambiarVista("ReporteVenta.fxml");
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        // Sesion.limpiarSesion();
        System.out.println("Sesión cerrada correctamente.");
        // GestorVistas.cambiarVista("Login.fxml");
    }
}