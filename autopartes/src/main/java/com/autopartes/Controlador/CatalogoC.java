package com.autopartes.Controlador;

import com.autopartes.Modelo.PiezaDAO;
import com.autopartes.Modelo.Pieza;
import com.autopartes.Modelo.Sesion; // Importante para cerrar sesión
import javafx.event.ActionEvent; // Importante para los clics del menú
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.ArrayList;
import java.util.List;

public class CatalogoC {

    @FXML
    private FlowPane panelProductos;
    @FXML
    private TextField txtBuscarGeneral; // Campo de búsqueda general

    private PiezaDAO piezaDAO = new PiezaDAO(); // DAO para acceder a los datos de las piezas
    private List<Pieza> listaMaestra = new ArrayList<>(); // Lista de piezas

    @FXML
    public void initialize() {
        // Carga datos iniciales de la BD
        listaMaestra = piezaDAO.obtenerTodas();

        // Renderizar el catálogo completo por primera vez
        renderizarCatalogo(listaMaestra);

        // Agregar el listener para buscar automáticamente mientras se escribe
        txtBuscarGeneral.textProperty().addListener((observable, oldValue, newValue) -> {
            ejecutarFiltro(newValue);
        });
    }

    // Método intermedio que limpia y redibuja las tarjetas basadas en una lista específica
    private void renderizarCatalogo(List<Pieza> listaAProcesar) {
        panelProductos.getChildren().clear();
        for (Pieza p : listaAProcesar) {
            VBox tarjeta = crearTarjeta(p);
            panelProductos.getChildren().add(tarjeta);
        }
    }

    // Filtra en memoria la lista maestra y manda a redibujar el panel
    private void ejecutarFiltro(String textoBusqueda) {
        if (textoBusqueda == null || textoBusqueda.isEmpty()) {
            renderizarCatalogo(listaMaestra);
            return;
        }

        String filtro = textoBusqueda.toLowerCase().trim(); // Conversion a Min y quitar space para evitar problemas de búsqueda
        List<Pieza> listaFiltrada = new ArrayList<>(); // Lista temporal para resultados filtrados

        // Recorremos la lista maestra y aplicamos el filtro
        for (Pieza p : listaMaestra) {
            // Filtra por Nombre, ID o el ID del Estante
            if (p.getNombre().toLowerCase().contains(filtro) ||
                    String.valueOf(p.getIdPieza()).contains(filtro) ||
                    p.getIdEstante().toLowerCase().contains(filtro)) {

                listaFiltrada.add(p); // Si coincide con alguno de los criterios, se agrega a la lista filtrada
            }
        }

        renderizarCatalogo(listaFiltrada); // Renderiza lista filtrada en el panel
    }

    @FXML
    // Método vinculado al botón "Buscar" para ejecutar el filtro manualmente
    void filtrarCatalogo() {
        // Vinculado al onAction del botón "Buscar" físico por si deciden dar clic
        ejecutarFiltro(txtBuscarGeneral.getText());
    }

    // Método para crear una tarjeta visual de cada pieza, con su imagen, nombre, precio y estado
    private VBox crearTarjeta(Pieza p) {
        VBox vbox = new VBox(5);
        vbox.setPrefWidth(250);
        vbox.getStyleClass().add("tarjeta-stock-normal");
        vbox.setPadding(new Insets(20));

        Label lblStatus = new Label("DISPONIBLE");

        HBox hbImagen = new HBox();
        hbImagen.setAlignment(Pos.CENTER);
        hbImagen.setPrefHeight(160);

        ImageView img = new ImageView();
        img.setFitHeight(140);
        img.setFitWidth(200);
        img.setPreserveRatio(true);

        // Metodo para cargar imagen por defecto y no romper sistema
        try {
            String rutaImagen = p.getImagen();

            // Valida si es URL o local
            if (rutaImagen != null && (rutaImagen.startsWith("http://") || rutaImagen.startsWith("https://"))) {
                // Pasamos la URL directamente, JavaFX la descarga sola en segundo plano
                img.setImage(new Image(rutaImagen, true)); // El 'true' activa la carga asíncrona para que la app no se trabe
            } else {
                // busca en local
                String nombreArchivo = (rutaImagen != null && !rutaImagen.isEmpty()) ? rutaImagen : "default.jpg";
                String rutaImgLocal = "/com/autopartes/vistas/images/" + nombreArchivo;
                // carga la imagen desde el recurso local
                var stream = getClass().getResourceAsStream(rutaImgLocal);
                if (stream != null) {
                    img.setImage(new Image(stream));
                } else {
                    // Imagen por defecto si no encuentra el archivo local
                    var defaultStream = getClass().getResourceAsStream("/com/autopartes/vistas/images/default.jpg");
                    if (defaultStream != null)
                        img.setImage(new Image(defaultStream));
                }
            }
        } catch (Exception e) {
            System.err.println("Error crítico al cargar imagen: " + e.getMessage());
        }

        hbImagen.getChildren().add(img);

        Label lblMarca = new Label("REPUESTO");
        Label lblNombre = new Label(p.getNombre());
        lblNombre.setWrapText(true);
        lblNombre.setStyle("-fx-font-weight: bold; -fx-min-height: 40px;");

        Label lblPrecio = new Label("$" + String.format("%.2f", p.getPrecioActual()));
        lblPrecio.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 16px; -fx-font-weight: bold;");

        vbox.getChildren().addAll(lblStatus, hbImagen, lblMarca, lblNombre, lblPrecio);

        return vbox;
    }

    // ====================================================================
    // MÉTODOS DE NAVEGACIÓN DEL MENÚ HAMBURGUESA
    // ====================================================================

    @FXML
    private void irACatalogo(ActionEvent event) {
        GestorVistas.cambiarVista("CatalogoVendedor.fxml");
    }

    @FXML
    private void irAStock(ActionEvent event) {
        GestorVistas.cambiarVista("VistaStock.fxml");
    }

    @FXML
    private void irACarrito(ActionEvent event) {
        GestorVistas.cambiarVista("CarritoVenta.fxml");
    }

    @FXML
    private void irAReporte(ActionEvent event) {
        GestorVistas.cambiarVista("ReporteVenta.fxml");
    }

    @FXML
    private void cerrarSesion(ActionEvent event) {
        // Limpiamos los datos del usuario en la memoria
        Sesion.limpiarSesion();
        System.out.println("Sesión cerrada correctamente.");
        
        // Lo regresamos a la pantalla de Login
        GestorVistas.cambiarVista("Login.fxml");
    }
}