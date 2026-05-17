package com.autopartes.Modelo;

import javafx.beans.property.*;

/**
 * Modelo: Proveedor
 * Representa un proveedor de piezas.
 * Estructura normalizada (3FN) para gestión de múltiples proveedores.
 */
public class Proveedor {
    private final IntegerProperty idProveedor;
    private final StringProperty razonSocial;
    private final StringProperty contacto;
    private final StringProperty telefono;
    private final StringProperty email;
    private final StringProperty direccion;

    /**
     * Constructor con parámetros.
     */
    public Proveedor(int idProveedor, String razonSocial, String contacto, String telefono, String email, String direccion) {
        this.idProveedor = new SimpleIntegerProperty(idProveedor);
        this.razonSocial = new SimpleStringProperty(razonSocial);
        this.contacto = new SimpleStringProperty(contacto);
        this.telefono = new SimpleStringProperty(telefono);
        this.email = new SimpleStringProperty(email);
        this.direccion = new SimpleStringProperty(direccion);
    }

    // GETTERS
    public int getIdProveedor() { return idProveedor.get(); }
    public String getRazonSocial() { return razonSocial.get(); }
    public String getContacto() { return contacto.get(); }
    public String getTelefono() { return telefono.get(); }
    public String getEmail() { return email.get(); }
    public String getDireccion() { return direccion.get(); }

    // PROPERTY METHODS
    public IntegerProperty idProveedorProperty() { return idProveedor; }
    public StringProperty razonSocialProperty() { return razonSocial; }
    public StringProperty contactoProperty() { return contacto; }
    public StringProperty telefonoProperty() { return telefono; }
    public StringProperty emailProperty() { return email; }
    public StringProperty direccionProperty() { return direccion; }

    // SETTERS
    public void setIdProveedor(int idProveedor) { this.idProveedor.set(idProveedor); }
    public void setRazonSocial(String razonSocial) { this.razonSocial.set(razonSocial); }
    public void setContacto(String contacto) { this.contacto.set(contacto); }
    public void setTelefono(String telefono) { this.telefono.set(telefono); }
    public void setEmail(String email) { this.email.set(email); }
    public void setDireccion(String direccion) { this.direccion.set(direccion); }

    @Override
    public String toString() {
        return razonSocial.get();
    }
}
