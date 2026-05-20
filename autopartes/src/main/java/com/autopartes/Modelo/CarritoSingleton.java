//Singleton: Clase que hace una única instancia del carrito de compras para toda la aplicación 
//permitiendo compartir el mismo carrito entre diferentes vistas y controladores sin necesidad de pasar referencias constantemente. 
// Facilita la gestión centralizada del carrito, 
// asegurando que todas las partes de la aplicación trabajen con el mismo conjunto de datos.
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

    public void modificarCantidad(ItemCarrito item, int nuevaCantidad) {
        if (items.contains(item) && nuevaCantidad > 0) {
            item.setCantidad(nuevaCantidad);
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