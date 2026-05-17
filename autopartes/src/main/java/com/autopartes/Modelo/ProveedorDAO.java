package com.autopartes.Modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * ProveedorDAO: Data Access Object para gestionar proveedores.
 * Operaciones CRUD básicas.
 */
public class ProveedorDAO {

    private Connection db;

    /**
     * Constructor: Obtiene la instancia de conexión a BD.
     */
    public ProveedorDAO() {
        this.db = Conexion.getInstancia();
    }

    /**
     * Obtiene todos los proveedores de la base de datos.
     *
     * @return Lista de Proveedor
     */
    public List<Proveedor> obtenerTodos() {
        List<Proveedor> proveedores = new ArrayList<>();

        try {
            String sql = "SELECT id_proveedor, razon_social, contacto, telefono, email, direccion " +
                    "FROM proveedores ORDER BY razon_social ASC";

            try (PreparedStatement ps = db.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    int id = rs.getInt("id_proveedor");
                    String razonSocial = rs.getString("razon_social");
                    String contacto = rs.getString("contacto");
                    String telefono = rs.getString("telefono");
                    String email = rs.getString("email");
                    String direccion = rs.getString("direccion");

                    proveedores.add(new Proveedor(id, razonSocial, contacto, telefono, email, direccion));
                }

                System.out.println("✓ " + proveedores.size() + " proveedores cargados.");

            }

        } catch (SQLException e) {
            System.err.println("✗ Error en obtenerTodos: " + e.getMessage());
            e.printStackTrace();
        }

        return proveedores;
    }

    /**
     * Obtiene un proveedor por ID.
     *
     * @param idProveedor ID del proveedor
     * @return Objeto Proveedor, null si no existe
     */
    public Proveedor obtenerPorId(int idProveedor) {
        try {
            String sql = "SELECT id_proveedor, razon_social, contacto, telefono, email, direccion " +
                    "FROM proveedores WHERE id_proveedor = ?";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, idProveedor);

                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int id = rs.getInt("id_proveedor");
                        String razonSocial = rs.getString("razon_social");
                        String contacto = rs.getString("contacto");
                        String telefono = rs.getString("telefono");
                        String email = rs.getString("email");
                        String direccion = rs.getString("direccion");

                        return new Proveedor(id, razonSocial, contacto, telefono, email, direccion);
                    }
                }

            }

        } catch (SQLException e) {
            System.err.println("✗ Error en obtenerPorId: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Inserta un nuevo proveedor en la base de datos.
     *
     * @return ID del proveedor insertado, -1 si falla
     */
    public int insertar(String razonSocial, String contacto, String telefono, String email, String direccion) {
        int idProveedor = -1;

        try {
            String sql = "INSERT INTO proveedores (razon_social, contacto, telefono, email, direccion) " +
                    "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps = db.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, razonSocial);
                ps.setString(2, contacto);
                ps.setString(3, telefono);
                ps.setString(4, email);
                ps.setString(5, direccion);
                ps.executeUpdate();

                try (ResultSet rs = ps.getGeneratedKeys()) {
                    if (rs.next()) {
                        idProveedor = rs.getInt(1);
                        System.out.println("✓ Proveedor insertado: ID=" + idProveedor);
                    }
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Error en insertar: " + e.getMessage());
            e.printStackTrace();
        }

        return idProveedor;
    }

    /**
     * Actualiza un proveedor existente.
     *
     * @return true si se actualizó, false si falla
     */
    public boolean actualizar(int idProveedor, String razonSocial, String contacto, String telefono, String email, String direccion) {
        try {
            String sql = "UPDATE proveedores SET razon_social=?, contacto=?, telefono=?, email=?, direccion=? " +
                    "WHERE id_proveedor=?";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setString(1, razonSocial);
                ps.setString(2, contacto);
                ps.setString(3, telefono);
                ps.setString(4, email);
                ps.setString(5, direccion);
                ps.setInt(6, idProveedor);

                int filasActualizadas = ps.executeUpdate();
                if (filasActualizadas > 0) {
                    System.out.println("✓ Proveedor actualizado: ID=" + idProveedor);
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Error en actualizar: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    /**
     * Elimina un proveedor (cascada en producto_proveedor).
     *
     * @return true si se eliminó, false si falla
     */
    public boolean eliminar(int idProveedor) {
        try {
            String sql = "DELETE FROM proveedores WHERE id_proveedor=?";

            try (PreparedStatement ps = db.prepareStatement(sql)) {
                ps.setInt(1, idProveedor);

                int filasEliminadas = ps.executeUpdate();
                if (filasEliminadas > 0) {
                    System.out.println("✓ Proveedor eliminado: ID=" + idProveedor);
                    return true;
                }
            }

        } catch (SQLException e) {
            System.err.println("✗ Error en eliminar: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
}
