//Controlador para vista de stock en cajero
//Permite visualizar el stock de piezas, filtrar por estante y buscar por nombre o ID
//Solo operaciones de consulta, sin edición ni eliminación
package com.autopartes.Controlador;

import com.autopartes.Modelo.Pieza;
import com.autopartes.Modelo.PiezaDAO;
import com.autopartes.Modelo.EstanteDAO;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Duration;
import java.util.List;

public class StockC {

    @FXML
    protected TableView<Pieza> tablaStock;
    @FXML
    protected TableColumn<Pieza, Integer> colID;
    @FXML
    protected TableColumn<Pieza, String> colNombre;
    @FXML
    protected TableColumn<Pieza, String> colEstante;
    @FXML
    protected TableColumn<Pieza, Integer> colNivel;
    @FXML
    protected TableColumn<Pieza, Integer> colStock;
    @FXML
    protected TableColumn<Pieza, Integer> colCapMax;
    @FXML
    protected TextField txtBuscarGeneral;

    @FXML
    protected ListView<String> listaEstantes;
    @FXML
    protected Button btnAnterior;
    @FXML
    protected Button btnSiguiente;
    @FXML
    protected Label lblPaginaActual;
    @FXML
    protected Label lblContador;

    private String criterioBusqueda = "";
    private Timeline debounceTimer;

    private int paginaActual = 1;
    private final int TAMANO_PAGINA = 100;
    private int filtroEstanteId = -1;

    private final PiezaDAO piezaDAO = new PiezaDAO();
    private final EstanteDAO estanteDAO = new EstanteDAO();

    @FXML
    public void initialize() {
        colID.setCellValueFactory(new PropertyValueFactory<>("idPieza"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEstante.setCellValueFactory(new PropertyValueFactory<>("idEstante"));
        colNivel.setCellValueFactory(new PropertyValueFactory<>("nivelAsigned"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCapMax.setCellValueFactory(new PropertyValueFactory<>("capMax"));

        cargarEstantesDesdeBD();

        configurarFiltroEstantes();
        configurarFiltroBusqueda();

        actualizarTablaYComponentes();
    }

    private void cargarEstantesDesdeBD() {
        if (listaEstantes.getItems() != null) {
            listaEstantes.getItems().clear();
        }

        ObservableList<String> opcionesEstantes = FXCollections.observableArrayList();
        opcionesEstantes.add("Todos");

        List<Integer> idsEstantes = estanteDAO.obtenerListaIds();
        for (Integer id : idsEstantes) {
            opcionesEstantes.add("Estante " + id);
        }

        listaEstantes.setItems(opcionesEstantes);
        listaEstantes.getSelectionModel().selectFirst();
    }

    private void configurarFiltroEstantes() {
        listaEstantes.getSelectionModel().selectedItemProperty().addListener((obs, viejo, nuevoSeleccionado) -> {
            if (nuevoSeleccionado != null) {
                if (nuevoSeleccionado.equals("Todos")) {
                    filtroEstanteId = -1;
                } else {
                    try {
                        String numeroString = nuevoSeleccionado.replace("Estante ", "").trim();
                        filtroEstanteId = Integer.parseInt(numeroString);
                    } catch (NumberFormatException e) {
                        System.err.println("Error al parsear el ID del estante: " + e.getMessage());
                        filtroEstanteId = -1;
                    }
                }
                paginaActual = 1;
                actualizarTablaYComponentes();
            }
        });
    }

    private void configurarFiltroBusqueda() {
        if (txtBuscarGeneral == null) {
            return;
        }

        txtBuscarGeneral.textProperty().addListener((observable, oldValue, newValue) -> {
            if (debounceTimer != null) {
                debounceTimer.stop();
            }

            debounceTimer = new Timeline(new KeyFrame(Duration.millis(300), event -> {
                criterioBusqueda = newValue != null ? newValue.trim() : "";
                paginaActual = 1;
                actualizarTablaYComponentes();
            }));
            debounceTimer.play();
        });
    }

    protected void actualizarTablaYComponentes() {
        List<Pieza> piezasFiltradas = piezaDAO.obtenerPiezas(paginaActual, TAMANO_PAGINA, filtroEstanteId,
                criterioBusqueda);
        tablaStock.setItems(FXCollections.observableArrayList(piezasFiltradas));

        lblContador.setText("Mostrando: " + piezasFiltradas.size() + " artículos");

        int totalRegistros = piezaDAO.contarTotalPiezas(filtroEstanteId, criterioBusqueda);
        int totalPaginas = (int) Math.ceil((double) totalRegistros / TAMANO_PAGINA);
        if (totalPaginas == 0)
            totalPaginas = 1;

        if (paginaActual > totalPaginas) {
            paginaActual = totalPaginas;
        }

        lblPaginaActual.setText("Página " + paginaActual + " de " + totalPaginas);

        btnAnterior.setDisable(paginaActual == 1);
        btnSiguiente.setDisable(paginaActual >= totalPaginas);
    }

    @FXML
    private void paginaAnterior() {
        if (paginaActual > 1) {
            paginaActual--;
            actualizarTablaYComponentes();
        }
    }

    @FXML
    private void paginaSiguiente() {
        int totalRegistros = piezaDAO.contarTotalPiezas(filtroEstanteId, criterioBusqueda);
        int totalPaginas = (int) Math.ceil((double) totalRegistros / TAMANO_PAGINA);

        if (paginaActual < totalPaginas) {
            paginaActual++;
            actualizarTablaYComponentes();
        }
    }

    @FXML
    private void filtrarStock() {
        listaEstantes.getSelectionModel().selectFirst();

        this.filtroEstanteId = -1;
        this.criterioBusqueda = "";
        if (txtBuscarGeneral != null) {
            txtBuscarGeneral.clear();
        }

        this.paginaActual = 1;
        actualizarTablaYComponentes();
    }

    @FXML
    private void irACatalogo() {
        GestorVistas.cambiarVista("CatalogoVendedor.fxml");
    }

    @FXML
    private void irACarrito() {
        GestorVistas.cambiarVista("CarritoVenta.fxml");
    }

    @FXML
    private void irAMapaAlmacen() {
        GestorVistas.cambiarVista("MapaAlmacen.fxml");
    }

    @FXML
    private void cerrarSesion() {
        com.autopartes.Modelo.Sesion.setUsuario(null);
        GestorVistas.cambiarVista("Login.fxml");
    }

    @FXML
    private void irAReporte(ActionEvent event) {
        GestorVistas.cambiarVista("ReporteVenta.fxml");
    }
}