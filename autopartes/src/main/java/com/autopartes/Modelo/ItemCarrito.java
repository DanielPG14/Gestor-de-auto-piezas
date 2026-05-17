package com.autopartes.Modelo;

/**
 * Wrapper para pasar datos del carrito al método de venta.
 * Encapsula toda la información necesaria de cada item vendido.
 */
public class ItemCarrito {
    private int IDpieza;
    private int id_prod_prov;
    private int cantidad;
    private double precio_compra;
    private double utilidad_pct;
    private double iva_pct;
    private double precio_venta;

    public ItemCarrito(int IDpieza, int id_prod_prov, int cantidad, double precio_compra,
                       double utilidad_pct, double iva_pct, double precio_venta) {
        this.IDpieza = IDpieza;
        this.id_prod_prov = id_prod_prov;
        this.cantidad = cantidad;
        this.precio_compra = precio_compra;
        this.utilidad_pct = utilidad_pct;
        this.iva_pct = iva_pct;
        this.precio_venta = precio_venta;
    }

    // Getters
    public int getIDpieza() {
        return IDpieza;
    }

    public int getId_prod_prov() {
        return id_prod_prov;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecio_compra() {
        return precio_compra;
    }

    public double getUtilidad_pct() {
        return utilidad_pct;
    }

    public double getIva_pct() {
        return iva_pct;
    }

    public double getPrecio_venta() {
        return precio_venta;
    }

    /**
     * Calcula el subtotal (precio_venta * cantidad).
     */
    public double calcularSubtotal() {
        return precio_venta * cantidad;
    }

    /**
     * Calcula el IVA de la línea.
     */
    public double calcularIva() {
        return calcularSubtotal() * (iva_pct / 100.0);
    }

    /**
     * Calcula el total de la línea (subtotal + IVA).
     */
    public double calcularTotalLinea() {
        return calcularSubtotal() + calcularIva();
    }
}
