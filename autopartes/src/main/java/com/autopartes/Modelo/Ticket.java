package com.autopartes.Modelo;

import java.time.LocalDateTime;

/**
 * Modelo de Ticket (Cabecera de Venta)
 * Representa una transacción de venta en el sistema.
 */
public class Ticket {
    private int IDticket;
    private LocalDateTime fecha;
    private double montoTotal;
    private String metodoPago; // Efectivo, Tarjeta, Transferencia
    private String estado;     // Pendiente, Pagado, Cancelado

    // Constructor vacío
    public Ticket() {
    }

    // Constructor con parámetros
    public Ticket(int IDticket, LocalDateTime fecha, double montoTotal, String metodoPago, String estado) {
        this.IDticket = IDticket;
        this.fecha = fecha;
        this.montoTotal = montoTotal;
        this.metodoPago = metodoPago;
        this.estado = estado;
    }

    // Constructor sin ID (para creación)
    public Ticket(LocalDateTime fecha, double montoTotal, String metodoPago, String estado) {
        this.fecha = fecha;
        this.montoTotal = montoTotal;
        this.metodoPago = metodoPago;
        this.estado = estado;
    }

    // Getters y Setters
    public int getIDticket() {
        return IDticket;
    }

    public void setIDticket(int IDticket) {
        this.IDticket = IDticket;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public double getMontoTotal() {
        return montoTotal;
    }

    public void setMontoTotal(double montoTotal) {
        this.montoTotal = montoTotal;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    @Override
    public String toString() {
        return "Ticket{" +
                "IDticket=" + IDticket +
                ", fecha=" + fecha +
                ", montoTotal=" + montoTotal +
                ", metodoPago='" + metodoPago + '\'' +
                ", estado='" + estado + '\'' +
                '}';
    }
}
