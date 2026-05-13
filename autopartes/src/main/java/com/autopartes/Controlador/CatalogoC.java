package com.autopartes.Controlador;

import com.autopartes.Modelo.PiezaDAO;
import com.autopartes.Modelo.Pieza;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import java.util.List;

public class CatalogoC {

    @FXML
    private FlowPane panelProductos;
    private PiezaDAO piezaDAO = new PiezaDAO();

    @FXML
    public void initialize() {
        cargarProductos();
    }

    private void cargarProductos() {
        panelProductos.getChildren().clear();
        List<Pieza> lista = piezaDAO.obtenerTodas();

        for (Pieza p : lista) {
            VBox tarjeta = crearTarjeta(p);
            panelProductos.getChildren().add(tarjeta);
        }
    }

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

        try {
            String nombreArchivo = (p.getImagen() != null && !p.getImagen().isEmpty()) ? p.getImagen() : "default.jpg";
            String rutaImg = "/com/autopartes/vistas/images/" + nombreArchivo;

            var stream = getClass().getResourceAsStream(rutaImg);

            if (stream != null) {
                img.setImage(new Image(stream));
            } else {
                System.err.println("No se encontró la imagen: " + rutaImg + ". Cargando default.");
                var defaultStream = getClass().getResourceAsStream("/com/autopartes/vistas/images/default.jpg");
                if (defaultStream != null)
                    img.setImage(new Image(defaultStream));
            }
        } catch (Exception e) {
            System.err.println("Error crítico al cargar imagen: " + e.getMessage());
        }

        hbImagen.getChildren().add(img);

        Label lblMarca = new Label("REPUESTO");
        Label lblNombre = new Label(p.getNombre());
        lblNombre.setWrapText(true);
        lblNombre.setStyle("-fx-font-weight: bold; -fx-min-height: 40px;"); // Altura mínima para que no se deforme

        Label lblPrecio = new Label("$" + String.format("%.2f", p.getPrecioActual()));
        lblPrecio.setStyle("-fx-text-fill: #2ecc71; -fx-font-size: 16px; -fx-font-weight: bold;");

        vbox.getChildren().addAll(lblStatus, hbImagen, lblMarca, lblNombre, lblPrecio);

        return vbox;
    }
}