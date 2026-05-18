package com.autopartes.Modelo;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ReporteDAO {

    public ObservableList<VentaPorPeriodo> obtenerVentasPorPeriodo(String inicio, String fin) throws SQLException {
        if (inicio == null || fin == null) {
            throw new IllegalArgumentException("Los parámetros de fecha no pueden ser nulos.");
        }

        String sql = "SELECT t.IDticket, t.Fecha, p.nombre AS producto, lt.cantidad, lt.total_linea " +
                     "FROM ticket t " +
                     "JOIN lista_ticket lt ON t.IDticket = lt.IDticket " +
                     "JOIN piezas p ON lt.IDpieza = p.IDpieza " +
                     "WHERE t.Fecha BETWEEN ? AND ? " +
                     "ORDER BY t.Fecha";

        ObservableList<VentaPorPeriodo> ventas = FXCollections.observableArrayList();
        try (Connection conexion = Conexion.getInstancia();
             PreparedStatement pst = conexion.prepareStatement(sql)) {

            pst.setString(1, inicio);
            pst.setString(2, fin);

            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    ventas.add(new VentaPorPeriodo(
                            rs.getInt("IDticket"),
                            rs.getString("Fecha"),
                            rs.getString("producto"),
                            rs.getInt("cantidad"),
                            rs.getDouble("total_linea")
                    ));
                }
            }
        }
        return ventas;
    }

    public ObservableList<TotalVendidoCategoria> obtenerTotalVendidoPorCategoria() throws SQLException {
        String sql = "SELECT p.idEstante AS categoria, SUM(lt.total_linea) AS totalVendido " +
                     "FROM lista_ticket lt " +
                     "JOIN piezas p ON lt.IDpieza = p.IDpieza " +
                     "GROUP BY p.idEstante";

        ObservableList<TotalVendidoCategoria> totales = FXCollections.observableArrayList();
        try (Connection conexion = Conexion.getInstancia();
             PreparedStatement pst = conexion.prepareStatement(sql);
             ResultSet rs = pst.executeQuery()) {

            while (rs.next()) {
                totales.add(new TotalVendidoCategoria(
                        rs.getString("categoria"),
                        rs.getDouble("totalVendido")
                ));
            }
        }
        return totales;
    }

    public static class VentaPorPeriodo {
        private final int idVenta;
        private final String fecha;
        private final String producto;
        private final int cantidad;
        private final double total;

        public VentaPorPeriodo(int idVenta, String fecha, String producto, int cantidad, double total) {
            this.idVenta = idVenta;
            this.fecha = fecha;
            this.producto = producto;
            this.cantidad = cantidad;
            this.total = total;
        }

        public int getIdVenta() {
            return idVenta;
        }

        public String getFecha() {
            return fecha;
        }

        public String getProducto() {
            return producto;
        }

        public int getCantidad() {
            return cantidad;
        }

        public double getTotal() {
            return total;
        }
    }

    public static class TotalVendidoCategoria {
        private final String categoria;
        private final double totalVendido;

        public TotalVendidoCategoria(String categoria, double totalVendido) {
            this.categoria = categoria;
            this.totalVendido = totalVendido;
        }

        public String getCategoria() {
            return categoria;
        }

        public double getTotalVendido() {
            return totalVendido;
        }
    }
}
