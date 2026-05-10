//Código para pieza con propiedades JavaFX, necesario para la tabla y la interfaz
package com.autopartes.Modelo;

import javafx.beans.property.*;

public class Pieza {
    private final IntegerProperty idPieza;
    private final StringProperty nombre;
    private final DoubleProperty precioActual;
    private final StringProperty imagen;

    public Pieza(int idPieza, String nombre, double precioActual, String imagen) {
        this.idPieza = new SimpleIntegerProperty(idPieza);
        this.nombre = new SimpleStringProperty(nombre);
        this.precioActual = new SimpleDoubleProperty(precioActual);
        this.imagen = new SimpleStringProperty(imagen);
    }

    // Getters necesarios para la lógica y la interfaz
    public String getNombre() { return nombre.get(); }
    public double getPrecioActual() { return precioActual.get(); }
    public String getImagen() { return imagen.get(); }
}