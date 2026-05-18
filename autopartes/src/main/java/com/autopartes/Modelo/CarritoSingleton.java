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
            ItemCarrito actualizado = actual.withCantidad(actual.getCantidad() + nuevoItem.getCantidad());
            int index = items.indexOf(actual);
            if (index >= 0) {
                items.set(index, actualizado);
            }
        } else {
            items.add(nuevoItem);
        }
    }

    public void eliminarItem(ItemCarrito item) {
        items.remove(item);
    }

    public void vaciarCarrito() {
        items.clear();
    }
}
