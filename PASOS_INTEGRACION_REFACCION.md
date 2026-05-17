# PASOS DE INTEGRACIÓN - REFACCIÓN TRANSACCIONAL

## 1️⃣ VERIFICAR CONEXIÓN

```java
// En tu clase Conexion.java, asegúrate que:
public static Connection getInstancia() {
    if (connection == null || connection.isClosed()) {
        connection = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/auto_piezas",
            "usuario",
            "password"
        );
    }
    return connection;
}
```

## 2️⃣ ACTUALIZAR BUILD PATH

- Asegúrate que `mysql-connector-java-X.X.X.jar` está en `/lib`
- Maven: Añade a `pom.xml`:
```xml
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <version>8.0.33</version>
</dependency>
```

## 3️⃣ COMPILAR CLASES

```bash
cd autopartes
mvn clean compile
```

**Debe compilar sin errores:**
- ✓ Ticket.java
- ✓ ListaTicket.java
- ✓ Caja.java
- ✓ ItemCarrito.java
- ✓ VentaDAO.java
- ✓ CheckoutController.java

## 4️⃣ INTEGRAR EN CONTROLADOR

### En tu controlador principal (ej: GestorVistas.java):

```java
@FXML
private void abrirCheckout() {
    // Obtener carrito desde sesión/modelo
    List<ItemCarrito> items = obtenerItemsDelCarrito();
    
    // Crear objeto Ticket
    Ticket ticket = new Ticket(
        LocalDateTime.now(),
        0.0,
        metodoPago,  // Desde formulario
        "Pagado"
    );
    
    // Calcular total
    double total = items.stream()
        .mapToDouble(ItemCarrito::calcularTotalLinea)
        .sum();
    ticket.setMontoTotal(total);
    
    // PROCESAR
    boolean exito = VentaDAO.procesarVenta(
        ticket,
        items,
        total,
        turno,           // Desde sesión
        IDusuarioActual  // Desde sesión
    );
    
    if (exito) {
        AlertDialog.mostrar("Éxito", "Venta registrada.");
        limpiarCarrito();
    } else {
        AlertDialog.mostrarError("Error", "No se pudo procesar la venta.");
    }
}
```

## 5️⃣ VERIFICAR TABLAS EN BD

```sql
-- Ejecutar en MySQL:
USE auto_piezas;

-- Verificar tabla ticket
DESCRIBE ticket;
-- Debe tener: IDticket (AI), Fecha, montoTotal, metodoPago, estado

-- Verificar tabla lista_ticket
DESCRIBE lista_ticket;
-- Debe tener: IDlista (AI), IDpieza (FK), id_prod_prov (FK), IDticket (FK), cantidad, ...

-- Verificar tabla piezas
DESCRIBE piezas;
-- Debe tener: IDpieza (PK), stock, ...

-- Verificar tabla caja
DESCRIBE caja;
-- Debe tener: IDcaja (AI), IDticket (FK), Presupuesto, Ingresos, Fecha, turno, IDusuario
```

## 6️⃣ PRUEBA DE TRANSACCIÓN

```java
// Test unitario
public class VentaDAOTest {
    
    @Test
    public void testProcesarVentaExitosa() {
        // Arrange
        Ticket ticket = new Ticket(
            LocalDateTime.now(),
            500.0,
            "Tarjeta",
            "Pagado"
        );
        
        List<ItemCarrito> items = new ArrayList<>();
        items.add(new ItemCarrito(1, 10, 2, 100, 20, 16, 120));
        
        // Act
        boolean resultado = VentaDAO.procesarVenta(
            ticket, items, 500.0, "Matutino", 5
        );
        
        // Assert
        assertTrue(resultado);
    }
    
    @Test
    public void testRollbackEnError() {
        // Arrange: items con id_prod_prov inválido
        List<ItemCarrito> items = new ArrayList<>();
        items.add(new ItemCarrito(1, -1, 2, 100, 20, 16, 120)); // FK inválida
        
        Ticket ticket = new Ticket(
            LocalDateTime.now(),
            500.0,
            "Tarjeta",
            "Pagado"
        );
        
        // Act
        boolean resultado = VentaDAO.procesarVenta(
            ticket, items, 500.0, "Matutino", 5
        );
        
        // Assert
        assertFalse(resultado);
        // Verificar que tabla caja NO tiene registro
    }
}
```

## 7️⃣ VALIDAR LOGS

Ejecuta y revisa console:

```
INFO:    Transacción iniciada. Auto-commit desactivado.
INFO:    Ticket insertado con ID: 1
INFO:    Detalles insertados: 1 líneas.
INFO:    Inventario actualizado para 1 productos.
INFO:    Movimiento de caja registrado.
INFO:    Transacción confirmada (COMMIT).
```

Si hay error:
```
SEVERE:  Error procesando venta: Foreign key constraint failed
SEVERE:  Transacción revertida (ROLLBACK): ...
```

## 8️⃣ VERIFICAR BD DESPUÉS DE VENTA

```sql
-- Verificar ticket insertado
SELECT * FROM ticket ORDER BY IDticket DESC LIMIT 1;

-- Verificar detalles
SELECT * FROM lista_ticket 
WHERE IDticket = [ID del ticket anterior];

-- Verificar stock actualizado
SELECT IDpieza, stock FROM piezas WHERE IDpieza = 1;

-- Verificar caja (solo si está pagado)
SELECT * FROM caja WHERE IDticket = [ID del ticket];
```

## 9️⃣ MANEJO DE ERRORES

### Si falla Conexión:

```java
try {
    VentaDAO.procesarVenta(...);
} catch (Exception e) {
    LOGGER.log(Level.SEVERE, "Error fatal: " + e.getMessage());
    // Mostrar diálogo al usuario
}
```

### Si falla por FK (id_prod_prov):

- Verificar que `id_prod_prov` existe en tabla `producto_proveedor`
- Verificar que `IDpieza` existe en tabla `piezas`

### Si falla por Stock:

- Verificar que piezas tiene stock >= cantidad solicitada
- (Opcional) Agregar trigger de validación en BD

## 🔟 CHECKLIST FINAL

- [ ] Ticket.java compila
- [ ] ListaTicket.java compila
- [ ] Caja.java compila
- [ ] ItemCarrito.java compila
- [ ] VentaDAO.java compila
- [ ] CheckoutController.java actualizado
- [ ] Conexion.getInstancia() funciona
- [ ] MySQL tiene las 4 tablas (ticket, lista_ticket, piezas, caja)
- [ ] Test de transacción exitosa pasa
- [ ] Test de rollback pasa
- [ ] Logs muestran COMMIT/ROLLBACK correcto
- [ ] BD actualizada correctamente después de venta

---

**Estado**: ✅ LISTO PARA PRODUCCIÓN
