package com.autopartes.Modelo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.Optional;

public class CarritoSingleton {

    private static CarritoSingleton instancia;
    private final ObservableList<ItemCarrito> items;

    private CarritoSingleton() {
        this.items = FXCollections.observableArrayList();
    }

    public static CarritoSingleton getInstancia() {
        if (instancia == null) {
            instancia = new CarritoSingleton();
        }
        return instancia;
    }

    public ObservableList<ItemCarrito> getItems() {
        return items;
    }

    public void agregarItem(ItemCarrito nuevoItem) {
        Optional<ItemCarrito> itemExistente = items.stream()
                .filter(item -> item.getPieza().getIdPieza() == nuevoItem.getPieza().getIdPieza())
                .findFirst();

        if (itemExistente.isPresent()) {
            ItemCarrito actual = itemExistente.get();
            // Modificamos la cantidad usando el nuevo método centralizado
            modificarCantidad(actual, actual.getCantidad() + nuevoItem.getCantidad());
        } else {
            items.add(nuevoItem);
        }
    }

    /**
     * 🔥 NUEVO MÉTODO CENTRALIZADO
     * Modifica la cantidad de un ítem y fuerza la notificación a la TableView y a los Listeners de totales.
     */
    public void modificarCantidad(ItemCarrito item, int nuevaCantidad) {
        if (items.contains(item) && nuevaCantidad > 0) {
            item.setCantidad(nuevaCantidad);
            
            // Reemplazar el elemento en su misma posición dispara el ListChangeListener 
            // que actualiza los lblSubtotal, lblIvaGeneral y lblTotal automáticamente.
            int index = items.indexOf(item);
            items.set(index, item);
        }
    }

    public void eliminarItem(ItemCarrito item) {
        items.remove(item);
    }

    public void vaciarCarrito() {
        items.clear();
    }
}