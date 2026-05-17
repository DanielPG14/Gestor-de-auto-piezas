package com.autopartes.Modelo;

/**
 * Modelo de detalle de ticket (lista_ticket).
 */
public class ListaTicket {
    private int IDlista;
    private int IDpieza;
    private int id_prod_prov;
    private int IDticket;
    private int cantidad;
    private double subtotal;
    private double precio_compra;
    private double utilidad_pct;
    private double iva_pct;
    private double precio_venta;
    private double total_linea;

    public ListaTicket() {
    }

    public ListaTicket(int IDpieza, int id_prod_prov, int cantidad, double subtotal,
                       double precio_compra, double utilidad_pct, double iva_pct,
                       double precio_venta, double total_linea) {
        this.IDpieza = IDpieza;
        this.id_prod_prov = id_prod_prov;
        this.cantidad = cantidad;
        this.subtotal = subtotal;
        this.precio_compra = precio_compra;
        this.utilidad_pct = utilidad_pct;
        this.iva_pct = iva_pct;
        this.precio_venta = precio_venta;
        this.total_linea = total_linea;
    }

    public int getIDlista() {
        return IDlista;
    }

    public void setIDlista(int IDlista) {
        this.IDlista = IDlista;
    }

    public int getIDpieza() {
        return IDpieza;
    }

    public void setIDpieza(int IDpieza) {
        this.IDpieza = IDpieza;
    }

    public int getId_prod_prov() {
        return id_prod_prov;
    }

    public void setId_prod_prov(int id_prod_prov) {
        this.id_prod_prov = id_prod_prov;
    }

    public int getIDticket() {
        return IDticket;
    }

    public void setIDticket(int IDticket) {
        this.IDticket = IDticket;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getPrecio_compra() {
        return precio_compra;
    }

    public void setPrecio_compra(double precio_compra) {
        this.precio_compra = precio_compra;
    }

    public double getUtilidad_pct() {
        return utilidad_pct;
    }

    public void setUtilidad_pct(double utilidad_pct) {
        this.utilidad_pct = utilidad_pct;
    }

    public double getIva_pct() {
        return iva_pct;
    }

    public void setIva_pct(double iva_pct) {
        this.iva_pct = iva_pct;
    }

    public double getPrecio_venta() {
        return precio_venta;
    }

    public void setPrecio_venta(double precio_venta) {
        this.precio_venta = precio_venta;
    }

    public double getTotal_linea() {
        return total_linea;
    }

    public void setTotal_linea(double total_linea) {
        this.total_linea = total_linea;
    }
}
