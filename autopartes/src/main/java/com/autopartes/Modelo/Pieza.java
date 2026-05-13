package com.autopartes.Modelo;

import javafx.beans.property.*;

public class Pieza {
    private final IntegerProperty idPieza;
    private final StringProperty nombre;
    private final DoubleProperty precioActual;
    private final StringProperty imagen;
    private final StringProperty idEstante;
    private final IntegerProperty nivelAsigned;
    private final IntegerProperty stock;
    private final IntegerProperty capMax;

    // Constructor
    public Pieza(int idPieza, String nombre, double precioActual, String imagen, String idEstante, int nivelAsigned, int stock, int capMax) {
        this.idPieza = new SimpleIntegerProperty(idPieza);
        this.nombre = new SimpleStringProperty(nombre);
        this.precioActual = new SimpleDoubleProperty(precioActual);
        this.imagen = new SimpleStringProperty(imagen);
        this.idEstante = new SimpleStringProperty(idEstante);
        this.nivelAsigned = new SimpleIntegerProperty(nivelAsigned);
        this.stock = new SimpleIntegerProperty(stock);
        this.capMax = new SimpleIntegerProperty(capMax);
    }

    //GETTERS
    public int getIdPieza() { return idPieza.get(); }
    public String getNombre() { return nombre.get(); }
    public double getPrecioActual() { return precioActual.get(); }
    public String getImagen() { return imagen.get(); }
    public String getIdEstante() { return idEstante.get(); }
    public int getNivelAsigned() { return nivelAsigned.get(); }
    public int getStock() { return stock.get(); }
    public int getCapMax() { return capMax.get(); }

    //PROPERTY METHODS
    public IntegerProperty idPiezaProperty() { return idPieza; }
    public StringProperty nombreProperty() { return nombre; }
    public DoubleProperty precioActualProperty() { return precioActual; }
    public StringProperty imagenProperty() { return imagen; }
    public StringProperty idEstanteProperty() { return idEstante; }
    public IntegerProperty nivelAsignedProperty() { return nivelAsigned; }
    public IntegerProperty stockProperty() { return stock; }
    public IntegerProperty capMaxProperty() { return capMax; }
}