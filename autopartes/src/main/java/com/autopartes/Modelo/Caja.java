package com.autopartes.Modelo;

import java.time.LocalDateTime;

/**
 * Modelo de Caja (Registro de movimientos de dinero).
 */
public class Caja {
    private int IDcaja;
    private int IDticket;
    private double presupuesto;
    private double ingresos;
    private LocalDateTime fecha;
    private String turno;      // Matutino, Vespertino
    private int IDusuario;

    public Caja() {
    }

    public Caja(int IDticket, double presupuesto, double ingresos, LocalDateTime fecha, 
                String turno, int IDusuario) {
        this.IDticket = IDticket;
        this.presupuesto = presupuesto;
        this.ingresos = ingresos;
        this.fecha = fecha;
        this.turno = turno;
        this.IDusuario = IDusuario;
    }

    public int getIDcaja() {
        return IDcaja;
    }

    public void setIDcaja(int IDcaja) {
        this.IDcaja = IDcaja;
    }

    public int getIDticket() {
        return IDticket;
    }

    public void setIDticket(int IDticket) {
        this.IDticket = IDticket;
    }

    public double getPresupuesto() {
        return presupuesto;
    }

    public void setPresupuesto(double presupuesto) {
        this.presupuesto = presupuesto;
    }

    public double getIngresos() {
        return ingresos;
    }

    public void setIngresos(double ingresos) {
        this.ingresos = ingresos;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public String getTurno() {
        return turno;
    }

    public void setTurno(String turno) {
        this.turno = turno;
    }

    public int getIDusuario() {
        return IDusuario;
    }

    public void setIDusuario(int IDusuario) {
        this.IDusuario = IDusuario;
    }
}
