package com.autopartes.Modelo;

/**
 * Representa un item dentro del carrito de ventas.
 * Contiene la pieza seleccionada, la cantidad y el subtotal calculado.
 */
public class ItemCarrito {
    private final Pieza pieza;
    private final int cantidad;
    private final double subtotal;

    public ItemCarrito(Pieza pieza, int cantidad) {
        this.pieza = pieza;
        this.cantidad = cantidad;
        this.subtotal = pieza.getPrecioCompra() * cantidad;
    }

    public Pieza getPieza() {
        return pieza;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getPrecioUnitario() {
        return pieza.getPrecioCompra();
    }

    public double getIvaPct() {
        return 16.0;
    }

    public double getUtilidadPct() {
        return 30.0;
    }

    public double getTotalLinea() {
        return subtotal + calcularIva();
    }

    public double calcularIva() {
        return subtotal * (getIvaPct() / 100.0);
    }

    public double calcularTotalLinea() {
        return getTotalLinea();
    }

    public ItemCarrito withCantidad(int nuevaCantidad) {
        return new ItemCarrito(pieza, nuevaCantidad);
    }
}
