package com.autopartes.Modelo;

import javafx.beans.property.*;

public class Pieza {
    // Propiedades JavaFX para enlace con la UI
    private final IntegerProperty idPieza;
    private final StringProperty nombre;
    private final StringProperty razonSocialProveedor;
    private final StringProperty codigoProveedor;
    private final DoubleProperty precioCompra;
    private final StringProperty imagen;
    private final StringProperty idEstante;
    private final IntegerProperty nivelAsigned;
    private final IntegerProperty stock;
    private final IntegerProperty capMax;

    // Constructor
    public Pieza(int idPieza, String nombre, String razonSocialProveedor, String codigoProveedor, double precioCompra, String imagen, String idEstante, int nivelAsigned, int stock, int capMax) {
        this.idPieza = new SimpleIntegerProperty(idPieza);
        this.nombre = new SimpleStringProperty(nombre);
        this.razonSocialProveedor = new SimpleStringProperty(razonSocialProveedor);
        this.codigoProveedor = new SimpleStringProperty(codigoProveedor);
        this.precioCompra = new SimpleDoubleProperty(precioCompra);
        this.imagen = new SimpleStringProperty(imagen);
        this.idEstante = new SimpleStringProperty(idEstante);
        this.nivelAsigned = new SimpleIntegerProperty(nivelAsigned);
        this.stock = new SimpleIntegerProperty(stock);
        this.capMax = new SimpleIntegerProperty(capMax);
    }

    //GETTERS
    public int getIdPieza() { return idPieza.get(); }
    public String getNombre() { return nombre.get(); }
    public String getRazonSocialProveedor() { return razonSocialProveedor.get(); }
    public String getCodigoProveedor() { return codigoProveedor.get(); }
    public double getPrecioCompra() { return precioCompra.get(); }
    public String getImagen() { return imagen.get(); }
    public String getIdEstante() { return idEstante.get(); }
    public int getNivelAsigned() { return nivelAsigned.get(); }
    public int getStock() { return stock.get(); }
    public int getCapMax() { return capMax.get(); }

    //PROPERTY METHODS
    public IntegerProperty idPiezaProperty() { return idPieza; }
    public StringProperty nombreProperty() { return nombre; }
    public StringProperty razonSocialProveedorProperty() { return razonSocialProveedor; }
    public StringProperty codigoProveedorProperty() { return codigoProveedor; }
    public DoubleProperty precioCompraProperty() { return precioCompra; }
    public StringProperty imagenProperty() { return imagen; }
    public StringProperty idEstanteProperty() { return idEstante; }
    public IntegerProperty nivelAsignedProperty() { return nivelAsigned; }
    public IntegerProperty stockProperty() { return stock; }
    public IntegerProperty capMaxProperty() { return capMax; }

    //SETTERS
    public void setIdPieza(int idPieza) { this.idPieza.set(idPieza); }
    public void setNombre(String nombre) { this.nombre.set(nombre); }
    public void setRazonSocialProveedor(String razonSocialProveedor) { this.razonSocialProveedor.set(razonSocialProveedor); }
    public void setCodigoProveedor(String codigoProveedor) { this.codigoProveedor.set(codigoProveedor); }
    public void setPrecioCompra(double precioCompra) { this.precioCompra.set(precioCompra); }
    public void setImagen(String imagen) { this.imagen.set(imagen); }
    public void setIdEstante(String idEstante) { this.idEstante.set(idEstante); }
    public void setNivelAsigned(int nivelAsigned) { this.nivelAsigned.set(nivelAsigned); }
    public void setStock(int stock) { this.stock.set(stock); }
    public void setCapMax(int capMax) { this.capMax.set(capMax); }
}