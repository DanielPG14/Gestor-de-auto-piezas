package com.autopartes.Modelo;

public class Estante {
    private int idEstante;
    private int idPieza;
    private String nombrePieza; 
    private int pisos;
    private String ubicacionAlmacen;

    public Estante(int idEstante, int idPieza, String nombrePieza, int pisos, String ubicacionAlmacen) {
        this.idEstante = idEstante;
        this.idPieza = idPieza;
        this.nombrePieza = nombrePieza;
        this.pisos = pisos;
        this.ubicacionAlmacen = ubicacionAlmacen;
    }

    //getters
    public int getIdEstante() {return idEstante;}
    public int getIdPieza() {return idPieza;}
    public String getNombrePieza() {return nombrePieza;}
    public int getPisos() {return pisos;}
    public String getUbicacionAlmacen() {return ubicacionAlmacen;}
}