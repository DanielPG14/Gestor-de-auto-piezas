package com.autopartes.Controlador;

import com.autopartes.Modelo.Pieza;
import com.autopartes.Modelo.PiezaDAO;
import com.autopartes.Modelo.EstanteDAO;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import java.util.List;

public class StockC {

    // Componentes emparejados perfectamente con los fx:id de tu FXML
    @FXML protected TableView<Pieza> tablaStock;
    @FXML protected TableColumn<Pieza, Integer> colID;
    @FXML protected TableColumn<Pieza, String> colNombre;
    @FXML protected TableColumn<Pieza, String> colEstante;
    @FXML protected TableColumn<Pieza, Integer> colNivel;
    @FXML protected TableColumn<Pieza, Integer> colStock;
    @FXML protected TableColumn<Pieza, Integer> colCapMax;

    @FXML protected ListView<String> listaEstantes; 
    @FXML protected Button btnAnterior;
    @FXML protected Button btnSiguiente;
    @FXML protected Label lblPaginaActual;
    @FXML protected Label lblContador;

    private int paginaActual = 1;
    private final int TAMANO_PAGINA = 100;
    private int filtroEstanteId = -1;

    private final PiezaDAO piezaDAO = new PiezaDAO();
    private final EstanteDAO estanteDAO = new EstanteDAO();

    @FXML
    public void initialize() {
        // 1. Vincular las columnas de la tabla con las propiedades del modelo Pieza
        // Nota: Asegúrate de que estos nombres coincidan con los atributos/getters de
        // tu clase Pieza.java
        colID.setCellValueFactory(new PropertyValueFactory<>("idPieza"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEstante.setCellValueFactory(new PropertyValueFactory<>("idEstante"));
        colNivel.setCellValueFactory(new PropertyValueFactory<>("nivelAsigned"));
        colStock.setCellValueFactory(new PropertyValueFactory<>("stock"));
        colCapMax.setCellValueFactory(new PropertyValueFactory<>("capMax"));

        // 2. Cargar los estantes reales desde la base de datos SQL
        cargarEstantesDesdeBD();

        // 3. Escuchar la selección de la lista lateral para filtrar en tiempo real
        configurarFiltroEstantes();

        // 4. Primera carga de datos en la tabla
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

    protected void actualizarTablaYComponentes() {
        // Obtener piezas paginadas y filtradas por estante
        List<Pieza> piezasFiltradas = piezaDAO.obtenerPiezas(paginaActual, TAMANO_PAGINA, filtroEstanteId);
        tablaStock.setItems(FXCollections.observableArrayList(piezasFiltradas));

        // Actualizar el contador de la esquina superior
        lblContador.setText("Mostrando: " + piezasFiltradas.size() + " artículos");

        // Calcular paginación dinámica basada en registros reales de SQL
        int totalRegistros = piezaDAO.contarTotalPiezas(filtroEstanteId);
        int totalPaginas = (int) Math.ceil((double) totalRegistros / TAMANO_PAGINA);
        if (totalPaginas == 0)
            totalPaginas = 1;

        if (paginaActual > totalPaginas) {
            paginaActual = totalPaginas;
        }

        // Sincronizar etiqueta de páginas de tu FXML
        lblPaginaActual.setText("Página " + paginaActual + " de " + totalPaginas);

        // Bloqueo inteligente de botones de navegación
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
        int totalRegistros = piezaDAO.contarTotalPiezas(filtroEstanteId);
        int totalPaginas = (int) Math.ceil((double) totalRegistros / TAMANO_PAGINA);

        if (paginaActual < totalPaginas) {
            paginaActual++;
            actualizarTablaYComponentes();
        }
    }

    @FXML
    private void filtrarStock() {
        // 1. Resetea el combo box o la lista si es necesario
        listaEstantes.getSelectionModel().selectFirst();

        // 2. Resetea el filtro de estante
        this.filtroEstanteId = -1;

        // 3. Limpia el buscador de texto si tienes uno (según tu FXML tienes
        // txtBuscarGeneral)
        // txtBuscarGeneral.clear();

        // 4. Recarga la tabla
        this.paginaActual = 1;
        actualizarTablaYComponentes();
    }

    // Métodos de navegación y barra superior definidos en tu FXML (vacíos para que
    // no te den error al compilar)
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
        // ¡OJO AQUÍ! Asegúrate de que el nombre del archivo sea exactamente
        // como está en tu carpeta (ej. "MapaAlmacen.fxml")
        GestorVistas.cambiarVista("MapaAlmacen.fxml");
    }

    @FXML
    private void cerrarSesion() {
        // Limpiamos la sesión antes de ir al login
        com.autopartes.Modelo.Sesion.setUsuario(null);
        GestorVistas.cambiarVista("Login.fxml");
    }
}