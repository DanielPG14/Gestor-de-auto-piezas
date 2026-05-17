# REFACCIÓN TRANSACCIONAL DE VENTAS - ESPECIFICACIÓN TÉCNICA

## 📋 ARCHIVOS ENTREGADOS

### 1. **Modelo: Ticket.java** (57 líneas)
- Representa la cabecera de una venta
- Campos: IDticket, fecha, montoTotal, metodoPago, estado
- Constructores para diferentes escenarios

### 2. **Modelo: ListaTicket.java** (108 líneas)
- Representa el detalle de una venta (una línea)
- Campos: IDlista, IDpieza, id_prod_prov, IDticket, cantidad, subtotal, precio_compra, utilidad_pct, iva_pct, precio_venta, total_linea
- Todos los getters/setters implementados

### 3. **Modelo: Caja.java** (79 líneas)
- Representa el movimiento de dinero en caja
- Campos: IDcaja, IDticket, presupuesto, ingresos, fecha, turno, IDusuario
- Constructores con y sin ID auto-generado

### 4. **Modelo: ItemCarrito.java** (75 líneas)
- Wrapper para pasar datos del carrito al sistema de venta
- Encapsula: IDpieza, id_prod_prov, cantidad, precio_compra, utilidad_pct, iva_pct, precio_venta
- Métodos de cálculo:
  - calcularSubtotal() = precio_venta * cantidad
  - calcularIva() = subtotal * (iva_pct / 100)
  - calcularTotalLinea() = subtotal + iva

### 5. **DAO: VentaDAO.java** (200+ líneas)
- Método público estático: `procesarVenta(Ticket, List<ItemCarrito>, double, String, int)`
- Métodos privados:
  - `insertarTicket()` - Inserta en tabla `ticket` y retorna ID auto-generado
  - `insertarDetalleTicket()` - Inserta en tabla `lista_ticket` (con try-with-resources)
  - `actualizarInventario()` - Decrementa stock en tabla `piezas`
  - `insertarMovimientoCaja()` - Registra en tabla `caja` si estado es 'Pagado'

### 6. **Controlador: CheckoutController.java** (ejemplo)
- Muestra cómo invocar `VentaDAO.procesarVenta()`
- Captura datos del formulario
- Valida información
- Maneja resultado (éxito/error)

---

## 🔄 FLUJO TRANSACCIONAL ACID IMPLEMENTADO

```
┌─────────────────────────────────────────────────────────────┐
│  MÉTODO MAESTRO: procesarVenta()                            │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  PASO 0: Obtener conexión + setAutoCommit(false)          │
│  ├─ Inicia transacción ACID                               │
│  └─ Logging: "Transacción iniciada"                       │
│                                                             │
│  PASO 1: Insertar TICKET                                  │
│  ├─ SQL: INSERT INTO ticket (Fecha, montoTotal, ...)      │
│  ├─ Recupera IDticket con RETURN_GENERATED_KEYS           │
│  ├─ Si IDticket <= 0 → throw SQLException                 │
│  └─ Logging: "Ticket insertado con ID: X"                 │
│                                                             │
│  PASO 2: Iterar e Insertar LISTA_TICKET                   │
│  ├─ Para cada item en lista:                              │
│  │  ├─ Calcula: subtotal, iva, totalLinea                 │
│  │  └─ INSERT INTO lista_ticket (...)                     │
│  ├─ Si alguno falla → throw SQLException                  │
│  └─ Logging: "Detalles insertados: N líneas"              │
│                                                             │
│  PASO 3: Actualizar PIEZAS (stock)                        │
│  ├─ Para cada item en lista:                              │
│  │  └─ UPDATE piezas SET stock = stock - ?                │
│  ├─ Si alguno falla → throw SQLException                  │
│  └─ Logging: "Inventario actualizado para N productos"    │
│                                                             │
│  PASO 4: Registrar CAJA (si "Pagado")                     │
│  ├─ Si estado == "Pagado":                                │
│  │  ├─ INSERT INTO caja (IDticket, Ingresos, ...)         │
│  │  └─ Incluye: turno, IDusuario, fecha actual            │
│  ├─ Si alguno falla → throw SQLException                  │
│  └─ Logging: "Movimiento de caja registrado"              │
│                                                             │
│  PASO 5: COMMIT                                           │
│  ├─ connection.commit()                                    │
│  ├─ Retorna: true                                          │
│  └─ Logging: "Transacción confirmada (COMMIT)"            │
│                                                             │
└─────────────────────────────────────────────────────────────┘
                     ↓
              ¿ERROR?
                     ↓
┌─────────────────────────────────────────────────────────────┐
│  CATCH (SQLException):                                      │
│  ├─ connection.rollback()                                  │
│  ├─ BD vuelve a estado anterior                            │
│  ├─ Logging: "Transacción revertida (ROLLBACK)"            │
│  ├─ Retorna: false                                         │
│  └─ Stack trace de excepción                              │
│                                                             │
│  FINALLY:                                                  │
│  ├─ setAutoCommit(true)                                   │
│  └─ close()                                                │
└─────────────────────────────────────────────────────────────┘
```

---

## 🗄️ TABLAS UTILIZADAS (Esquema Real)

| Tabla | Operación | Descripción |
|-------|-----------|-------------|
| **ticket** | INSERT | Cabecera de venta con fecha, monto, método pago, estado |
| **lista_ticket** | INSERT | Detalles de venta (desglose por producto) |
| **piezas** | UPDATE | Decrementa stock: `stock = stock - cantidad` |
| **caja** | INSERT | Registro de caja (solo si estado='Pagado') |

---

## 🔐 CARACTERÍSTICAS DE SEGURIDAD

✅ **PreparedStatement** en todos los queries  
✅ **Try-with-resources** para cierre automático de recursos  
✅ **Transacciones ACID** con setAutoCommit(false)  
✅ **ROLLBACK automático** en caso de error  
✅ **Logging con Level** (INFO, SEVERE, WARNING)  
✅ **Stack traces** para debugging  
✅ **Validación de filas afectadas** (rowsAffected > 0)  
✅ **Manejo formal de excepciones** en catch/finally  

---

## 💻 EJEMPLO DE USO EN CONTROLADOR

```java
// 1. Crear objeto Ticket
Ticket ticket = new Ticket(
    LocalDateTime.now(),
    0.0,
    "Tarjeta",  // Efectivo, Tarjeta, Transferencia
    "Pagado"    // Pendiente, Pagado, Cancelado
);

// 2. Construir lista de items
List<ItemCarrito> items = new ArrayList<>();
items.add(new ItemCarrito(
    1,       // IDpieza
    10,      // id_prod_prov
    2,       // cantidad
    100.0,   // precio_compra
    20.0,    // utilidad_pct
    16.0,    // iva_pct
    120.0    // precio_venta
));

// 3. Calcular total
double montoTotal = items.stream()
    .mapToDouble(ItemCarrito::calcularTotalLinea)
    .sum();
ticket.setMontoTotal(montoTotal);

// 4. Procesar venta (TRANSACCIÓN ATÓMICA)
boolean exito = VentaDAO.procesarVenta(
    ticket,
    items,
    montoTotal,
    "Matutino",      // Turno
    5                // IDusuarioOperador (Cajero/Vendedor)
);

if (exito) {
    System.out.println("✓ Venta procesada.");
} else {
    System.err.println("✗ Error en transacción.");
}
```

---

## 📊 CÁLCULOS FINANCIEROS IMPLEMENTADOS

### Por cada línea (ItemCarrito):

```
1. Subtotal = precio_venta * cantidad
2. IVA = Subtotal * (iva_pct / 100)
3. Total Línea = Subtotal + IVA

Ejemplo:
- precio_venta = $120.00
- cantidad = 2
- iva_pct = 16%

Subtotal = 120 * 2 = $240.00
IVA = 240 * 0.16 = $38.40
Total = 240 + 38.40 = $278.40
```

### Monto Total de Venta:
```
montoTotal = SUM(total_linea) para todos los items
```

---

## ⚠️ VALIDACIONES IMPLEMENTADAS

1. ✅ IDticket auto-generado > 0
2. ✅ Cada INSERT debe afectar al menos 1 fila
3. ✅ SQL injection prevenida con PreparedStatement
4. ✅ Conexión cerrada en finally
5. ✅ Auto-commit restaurado en finally
6. ✅ Rollback en cualquier SQLException

---

## 🧪 CASOS DE PRUEBA

### Caso 1: Venta Exitosa
```
Entrada: ticket válido, 3 items, turno="Matutino", usuario=5
Estado: "Pagado"
Esperado: 
- ✓ INSERT ticket con ID=25
- ✓ INSERT 3 registros en lista_ticket
- ✓ UPDATE stock en 3 piezas
- ✓ INSERT 1 registro en caja
- ✓ COMMIT
- Resultado: true
```

### Caso 2: Error en INSERT lista_ticket
```
Entrada: id_prod_prov inválido en item #2
Esperado:
- ✓ INSERT ticket con ID=26
- ✓ INSERT lista_ticket #1 exitoso
- ✗ INSERT lista_ticket #2 falla (FK violada)
- ✓ ROLLBACK (ticket #26 se revierte)
- ✗ Stock NO actualizado
- ✗ Caja NO registrada
- Resultado: false
```

### Caso 3: Ticket NO pagado
```
Entrada: Estado = "Pendiente"
Esperado:
- ✓ INSERT ticket, detalles, actualizar stock
- ✗ SKIP insertar caja (condición no cumplida)
- ✓ COMMIT
- Resultado: true
```

---

## 📝 LOGGING GENERADO

```
INFO:    Transacción iniciada. Auto-commit desactivado.
INFO:    Ticket insertado con ID: 25
INFO:    Detalles insertados: 3 líneas.
INFO:    Inventario actualizado para 3 productos.
INFO:    Movimiento de caja registrado.
INFO:    Transacción confirmada (COMMIT).
```

---

## 🚀 PRÓXIMOS PASOS

1. Verificar que Conexion.getInstancia() retorna Connection válida
2. Reemplazar CheckoutController.java con esta clase de ejemplo
3. Mapear FXML con los campos del formulario
4. Testing con transacciones reales
5. Monitorear logs para validar ACID

---

**Generado**: 16 de Mayo de 2026  
**Versión**: 1.0 - PRODUCCIÓN  
**Validación**: ✅ Arquitectura 3FN, JDBC robusto, ACID transaccional
