package com.autopartes.Modelo;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class ItemCarrito {
    private final Pieza pieza;
    private final IntegerProperty cantidad; // Cambiado a Property de JavaFX

    public ItemCarrito(Pieza pieza, int cantidad) {
        this.pieza = pieza;
        this.cantidad = new SimpleIntegerProperty(cantidad);
    }

    public Pieza getPieza() { return pieza; }
    
    // --- Métodos para la propiedad cantidad ---
    public int getCantidad() { return cantidad.get(); }
    public void setCantidad(int cantidad) { this.cantidad.set(cantidad); }
    public IntegerProperty cantidadProperty() { return cantidad; } // Permite a la tabla observar cambios
    
    public double getPrecioUnitario() { return pieza.getPrecioCompra(); }
    public double getSubtotal() { return pieza.getPrecioCompra() * getCantidad(); }
    public double getIvaPct() { return 16.0; }
    public double getUtilidadPct() { return 30.0; }

    public double getTotalLinea() {
        double subtotal = getSubtotal();
        return subtotal + (subtotal * (getIvaPct() / 100.0));
    }
    
    public double calcularIva() { return getSubtotal() * (getIvaPct() / 100.0); }
}