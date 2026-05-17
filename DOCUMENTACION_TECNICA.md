# 📋 GESTOR-DE-AUTO-PIEZAS: DOCUMENTACIÓN TÉCNICA DE ENTREGA

**Fecha**: 16 de Mayo de 2026  
**Estado**: MEGA-SPRINT COMPLETADO  
**Líneas de Código**: ~1400+ LOC  
**Fases Implementadas**: A, B, C, D, E

---

## 🎯 RESUMEN EJECUTIVO

Se ha completado la arquitectura completa del sistema POS (Point of Sale) y gestión de inventario para Gestor-de-Auto-Piezas en **5 fases**:

| Fase | Módulo | Estado |
|------|--------|--------|
| **A** | Flujo POS Completo (Carrito → Checkout → Venta) | ✅ Completo |
| **B** | Generación de Cotizaciones | ✅ Completo |
| **C** | Sistema de Abastecimiento (Compras) | ✅ Completo |
| **D** | Reportes y Analítica | ✅ Completo |
| **E** | Base de Datos Avanzada (Triggers, SP) | ✅ Completo |

---

## 📦 ARCHIVOS ENTREGADOS

### Controladores JavaFX
```
src/main/java/com/autopartes/Controlador/
├── GestorVistas.java (MEJORADO)
│   └── + cambiarVistaConControlador<T>(String) - Método genérico para pasar datos
├── CarritoVentaC.java (MEJORADO)
│   ├── + irACheckout() - Integración con CheckoutC
│   └── + generarCotizacion() - Exportar cotizaciones a .txt
├── CheckoutC.java (EXISTENTE)
│   ├── + inicializarDatos(ObservableList, subtotal, iva, total)
│   ├── + confirmarVenta() - Transacción BD
│   ├── + generarTicket() - Formateo de factura
│   └── + guardarTicketEnArchivo() - Persistencia archivo
└── RegistroCompraC.java (NUEVO)
    ├── ComboBox de piezas y proveedores
    ├── Captura de código proveedor, precio, cantidad
    └── + registrarCompra() - Transacción abastecimiento
```

### Modelos y DAOs
```
src/main/java/com/autopartes/Modelo/
├── VentaDAO.java (MEJORADO)
│   ├── + procesarVenta() - Transacción ACID (4 pasos)
│   ├── + obtenerTodasLasVentas() - Para ReporteVentaC
│   ├── + obtenerDetallesVenta(int idVenta) - Desglose
│   └── Clases internas: VentaItem, DetalleVentaItem
├── Proveedor.java (NUEVO)
│   └── Modelo JavaFX Properties (6 campos)
├── ProveedorDAO.java (NUEVO)
│   ├── + obtenerTodos() - Cargar ComboBox
│   ├── + obtenerPorId(), insertar(), actualizar(), eliminar()
│   └── CRUD operativo
└── CompraDAO.java (NUEVO)
    ├── + registrarCompra() - Transacción (5 pasos)
    ├── + verificarOInsertarProductoProveedor() - Relación N:M
    ├── + cerrarHistorialPrecioAnterior() - Auditoría 3FN
    ├── + insertarHistorialPrecio() - Nuevo costo
    ├── + incrementarStock() - Suma cantidad
    └── + obtenerUltimasCompras(int limite) - Historial
```

### Base de Datos
```
estructura_base_de_datos.sql (COMPLETO)
├── TABLAS (Normalizadas 3FN):
│   ├── proveedores - Almacenamiento centralizado
│   ├── producto_proveedor - Relación N:M
│   ├── historial_precio - Auditoría costos
│   ├── compra - Entradas de abastecimiento
│   ├── clientes - Datos clientes
│   ├── ventas - Cabecera transacciones
│   └── detalle_venta - Líneas de venta
├── TRIGGERS (4):
│   ├── tr_actualizar_total_venta - Suma automática
│   ├── tr_restar_stock_venta - Decremento stock
│   ├── tr_validar_stock_venta - Prevención underselling
│   └── tr_sumar_stock_compra - Incremento entrada
├── PROCEDIMIENTOS ALMACENADOS (3):
│   ├── ConsultarPreciosPorProveedor - Matriz precios competitivos
│   ├── ReporteVentasPorFecha - Ventas por rango
│   └── ProductosStockBajo - Alertas reorden
└── VISTAS (3):
    ├── vw_ventas_por_cliente - Resumen cliente
    ├── vw_productos_mas_vendidos - Top ingresos
    └── vw_proveedores_productos - Matriz proveedor-producto
```

---

## 🔄 FLUJOS IMPLEMENTADOS

### FASE A: Flujo POS Completo

```
1. CarritoVentaC (Selección de productos con % utilidad e IVA)
   │
   ├─ btnAgregarItem → agregarItemAlCarrito()
   │  └─ ItemCarrito wraps Pieza + utilidadPct + ivaPct
   │
   └─ btnIrACheckout → irACheckout()
      │
      ├─ Valida carrito no vacío
      ├─ Calcula totales: Σ(precioVenta), Σ(IVA), Total = subtotal + IVA
      │
      └─ GestorVistas.cambiarVistaConControlador<CheckoutC>("Checkout.fxml")
         │
         └─ checkoutController.inicializarDatos(items, subtotal, iva, total)
            │
            └─ CheckoutC
               │
               ├─ UI: TextFields cliente (razonSocial, RFC, direccion)
               ├─ UI: Labels totales (lblSubtotalVenta, lblIvaVenta, lblTotalVenta)
               │
               └─ btnConfirmarVenta → confirmarVenta()
                  │
                  ├─ Valida datos cliente
                  │
                  └─ int idVenta = ventaDAO.procesarVenta(...)
                     │
                     ├─ PASO A: obtenerOInsertarCliente(razonSocial, RFC, direccion)
                     │  └─ SELECT id_cliente FROM clientes WHERE rfc = ?
                     │     ├─ Si existe: return id_cliente
                     │     └─ Si no existe: INSERT + return id generado
                     │
                     ├─ PASO B: insertarVenta(id_cliente)
                     │  └─ INSERT INTO ventas (id_cliente, fecha, total) VALUES (?, NOW(), 0)
                     │     └─ return id_venta
                     │
                     ├─ PASO C: FOR EACH ItemCarrito → insertarDetalleVenta(idVenta, item)
                     │  └─ INSERT INTO detalle_venta 
                     │     (id_venta, id_producto, precio_compra_historico, utilidad_pct, iva_pct, 
                     │      precio_venta_calculado, iva_calculado, total_linea)
                     │
                     ├─ PASO D: FOR EACH item → actualizarStock(idPieza)
                     │  └─ UPDATE piezas SET stock = stock - 1 WHERE IDpieza = ?
                     │
                     ├─ db.commit() ✓ | db.rollback() ✗
                     │
                     └─ Retorna idVenta > 0
                        │
                        ├─ generarTicket(idVenta, razonSocial, RFC, direccion)
                        │  └─ Estructura textual con encabezado, detalles, pie
                        │
                        ├─ guardarTicketEnArchivo("Ticket_Venta_[ID]_[TIMESTAMP].txt")
                        │
                        ├─ mostrarAlerta("Éxito", "Venta procesada: ID=" + idVenta)
                        │
                        └─ limpiarFormulario()
```

### FASE B: Generación de Cotizaciones

```
CarritoVentaC
│
└─ btnGenerarCotizacion → generarCotizacion()
   │
   ├─ Valida carrito no vacío
   │
   ├─ Calcula totales (igual que checkout)
   │
   ├─ generarEstructuraCotizacion(subtotal, ivaTotal, total)
   │  └─ Estructura textual (SIN inserción en BD)
   │
   └─ guardarCotizacionEnArchivo("Cotizacion_[TIMESTAMP].txt")
      │
      └─ FileWriter escribe en disco
         │
         ├─ Nota: "Esta cotización NO genera transacción ni modifica inventario"
         └─ Vigencia: 30 días
```

### FASE C: Sistema de Abastecimiento

```
RegistroCompraC (Controlador)
│
├─ UI: cmbPieza (ComboBox de piezas)
├─ UI: cmbProveedor (ComboBox de proveedores)
├─ UI: txtCodigoProveedor (Código específico del proveedor)
├─ UI: txtPrecioCompra (Costo de adquisición)
├─ UI: txtCantidad (Unidades a recibir)
├─ UI: lblStockActual (Label dinámico)
├─ UI: tableViewHistorial (Últimas 50 compras)
│
└─ btnRegistrarCompra → registrarCompra()
   │
   ├─ Validaciones (campos no vacíos, valores > 0)
   │
   └─ int idCompra = compraDAO.registrarCompra(idProduto, idProveedor, codigo, precio, cantidad)
      │
      ├─ db.setAutoCommit(false) ← TRANSACCIÓN
      │
      ├─ PASO A: verificarOInsertarProductoProveedor(idProducto, idProveedor, codigoProveedor)
      │  └─ SELECT id_prod_prov FROM producto_proveedor WHERE id_producto = ? AND id_proveedor = ?
      │     ├─ Si existe: return id_prod_prov
      │     └─ Si no existe: INSERT + return id generado
      │        └─ Resultado: tabla N:M establecida
      │
      ├─ PASO B: cerrarHistorialPrecioAnterior(idProdProv)
      │  └─ UPDATE historial_precio SET fecha_fin = NOW()
      │     WHERE id_prod_prov = ? AND fecha_fin IS NULL
      │
      ├─ PASO C: insertarHistorialPrecio(idProdProv, precioCompra)
      │  └─ INSERT INTO historial_precio 
      │     (id_prod_prov, precio_compra, fecha_inicio, fecha_fin)
      │     VALUES (?, ?, NOW(), NULL)
      │     └─ Auditoría 3FN: nuevo costo vigente
      │
      ├─ PASO D: incrementarStock(idPieza, cantidad)
      │  └─ UPDATE piezas SET stock = stock + ? WHERE IDpieza = ?
      │
      ├─ PASO E: insertarCompra(idProducto, idProveedor, precioCompra, cantidad)
      │  └─ INSERT INTO compra 
      │     (id_producto, id_proveedor, fecha, precio_compra, cantidad, total)
      │     VALUES (?, ?, NOW(), ?, ?, ?)
      │
      ├─ db.commit() ✓ | db.rollback() ✗
      │
      └─ Retorna idCompra > 0
         │
         ├─ mostrarAlerta("Éxito", "Compra registrada: ID=" + idCompra)
         ├─ limpiarFormulario()
         ├─ cargarHistorialCompras()
         └─ actualizarStockActual(pieza)
```

### FASE D: Reportes y Analítica

```
ReporteVentaC (Controlador)
│
├─ tableViewVentas (Ventas principales)
│  └─ Columnas: ID Venta | Fecha | Cliente | Total
│
├─ Listener en tableViewVentas seleccionada
│  └─ → cargarDetallesVenta(ventaSeleccionada)
│
└─ tableViewDetalles (Desglose de productos)
   └─ Columnas: Producto | PrecioCompra | Utilidad% | IVA% | TotalLinea

Métodos en VentaDAO:

1. obtenerTodasLasVentas(): List<VentaItem>
   └─ SELECT v.id_venta, v.fecha, c.nombre, v.total
      FROM ventas v
      INNER JOIN clientes c ON v.id_cliente = c.id_cliente
      ORDER BY v.fecha DESC

2. obtenerDetallesVenta(idVenta): List<DetalleVentaItem>
   └─ SELECT dv.id_detalle, p.nombre, dv.precio_compra_historico,
            dv.utilidad_pct, dv.iva_pct, dv.precio_venta_calculado,
            dv.iva_calculado, dv.total_linea
      FROM detalle_venta dv
      INNER JOIN piezas p ON dv.id_producto = p.IDpieza
      WHERE dv.id_venta = ?
```

### FASE E: Base de Datos Avanzada

**Triggers implementados:**

1. `tr_actualizar_total_venta` - Después de insertar detalle_venta
   ```
   UPDATE ventas SET total = SUM(total_linea) WHERE id_venta = NEW.id_venta
   ```

2. `tr_restar_stock_venta` - Después de insertar detalle_venta
   ```
   UPDATE piezas SET stock = stock - 1 WHERE IDpieza = NEW.id_producto
   ```

3. `tr_validar_stock_venta` - Antes de insertar detalle_venta
   ```
   IF stock <= 0 THEN SIGNAL SQLSTATE '45000'
   ```

4. `tr_sumar_stock_compra` - Después de insertar compra
   ```
   UPDATE piezas SET stock = stock + NEW.cantidad WHERE IDpieza = NEW.id_producto
   ```

**Procedimientos Almacenados:**

1. `ConsultarPreciosPorProveedor(producto_id)`
   - Matriz: Proveedor | Código Proveedor | Precio Vigente | Contacto

2. `ReporteVentasPorFecha(fecha_inicio, fecha_fin)`
   - Ventas filtradas por rango + Cantidad de productos

3. `ProductosStockBajo(nivel_minimo)`
   - Alertas de productos con stock por debajo de nivel

---

## 🔧 PASOS DE INTEGRACIÓN

### 1. Ejecutar Script SQL
```bash
mysql -u root -p DBA < estructura_base_de_datos.sql
```
Esto crea:
- ✅ 7 tablas normalizadas
- ✅ 4 triggers automáticos
- ✅ 3 procedimientos almacenados
- ✅ 3 vistas para consultas frecuentes

### 2. Actualizar FXML Files

#### Checkout.fxml
```xml
<VBox>
    <TextField fx:id="txtRazonSocial" promptText="Razón Social"/>
    <TextField fx:id="txtRFC" promptText="RFC"/>
    <TextField fx:id="txtDireccionFiscal" promptText="Dirección Fiscal"/>
    
    <HBox>
        <Label fx:id="lblSubtotalVenta" text="$0.00"/>
        <Label fx:id="lblIvaVenta" text="$0.00"/>
        <Label fx:id="lblTotalVenta" text="$0.00"/>
    </HBox>
    
    <Button fx:id="btnConfirmarVenta" text="Confirmar Venta" onAction="#confirmarVenta"/>
    <Button fx:id="btnVolverCarrito" text="Volver" onAction="#volverCarrito"/>
</VBox>
```

#### RegistroCompra.fxml (NUEVO)
```xml
<VBox>
    <ComboBox fx:id="cmbPieza" promptText="Seleccione Pieza"/>
    <ComboBox fx:id="cmbProveedor" promptText="Seleccione Proveedor"/>
    <TextField fx:id="txtCodigoProveedor" promptText="Código Proveedor"/>
    <TextField fx:id="txtPrecioCompra" promptText="Precio Compra"/>
    <TextField fx:id="txtCantidad" promptText="Cantidad"/>
    
    <Label fx:id="lblStockActual"/>
    <Label fx:id="lblProveedorInfo"/>
    
    <TableView fx:id="tableViewHistorial">
        <TableColumn fx:id="colPieza" text="Pieza"/>
        <TableColumn fx:id="colProveedor" text="Proveedor"/>
        <TableColumn fx:id="colCodigo" text="Código"/>
        <TableColumn fx:id="colPrecio" text="Precio"/>
        <TableColumn fx:id="colCantidad" text="Cantidad"/>
        <TableColumn fx:id="colFecha" text="Fecha"/>
    </TableView>
    
    <Button fx:id="btnRegistrarCompra" text="Registrar Compra" onAction="#registrarCompra"/>
</VBox>
```

#### ReporteVenta.fxml (ACTUALIZAR)
```xml
<VBox>
    <TableView fx:id="tableViewVentas">
        <TableColumn fx:id="colIdVenta" text="ID Venta"/>
        <TableColumn fx:id="colFecha" text="Fecha"/>
        <TableColumn fx:id="colCliente" text="Cliente"/>
        <TableColumn fx:id="colTotal" text="Total"/>
    </TableView>
    
    <HBox>
        <Label fx:id="lblClienteSeleccionado"/>
        <Label fx:id="lblFechaVenta"/>
        <Label fx:id="lblTotalVenta"/>
    </HBox>
    
    <TableView fx:id="tableViewDetalles">
        <TableColumn fx:id="colProducto" text="Producto"/>
        <TableColumn fx:id="colPrecioCompra" text="Precio Compra"/>
        <TableColumn fx:id="colUtilidadPct" text="Utilidad%"/>
        <TableColumn fx:id="colIvaPct" text="IVA%"/>
        <TableColumn fx:id="colTotalLinea" text="Total Línea"/>
    </TableView>
    
    <Button fx:id="btnVerDetalle" text="Ver Detalle" onAction="#verDetalle"/>
</VBox>
```

#### CarritoVenta.fxml (AGREGAR BOTÓN)
```xml
<Button fx:id="btnGenerarCotizacion" text="Generar Cotización" onAction="#generarCotizacion"/>
```

### 3. Actualizar GestorVistas (Permisos)

```java
case "Almacenista":
    return nombreFxml.equals("VistaStockAlm.fxml") ||
           nombreFxml.equals("RegistroCompra.fxml") ||  // ← NUEVO
           nombreFxml.equals("VistaStock.fxml") || 
           nombreFxml.equals("ReporteVenta.fxml");

case "Cajero":
    return nombreFxml.equals("CatalogoVendedor.fxml") || 
           nombreFxml.equals("CarritoVenta.fxml") || 
           nombreFxml.equals("Checkout.fxml") || 
           nombreFxml.equals("ReporteVenta.fxml");
```

### 4. Actualizar Menú de Navegación

Agregar opciones en menú principal:
- "Registro de Compra" → RegistroCompra.fxml
- "Generar Cotización" → Desde CarritoVenta
- "Reporte de Ventas" → ReporteVenta.fxml

---

## 📊 ESTRUCTURA DE DATOS 3FN

### Relaciones Principales:
```
proveedores (1) ─────────────┬─ (N) producto_proveedor (N) ─── (1) piezas
                              │
                              └─ (1) historial_precio (fechas)
                              
clientes (1) ────────────────┬─ (N) ventas (1) ─────────────┬─ (N) detalle_venta
                              │                                │
                              │                                └─ (N) piezas

ProveedoresN ───────────────┬─ (N) compra (N) ────────────────────┬─ (1) piezas
                              │
                              └─ (1) producto_proveedor (1) ── (N) piezas
```

---

## 🚀 CONSIDERACIONES DE PRODUCCIÓN

### Security
- ✅ Validaciones en aplicación (no solo BD)
- ✅ RFC como índice único en clientes
- ✅ Restricciones FK en cascada

### Performance
- ✅ Índices en columnas frecuentes (fecha, rfc, stock)
- ✅ Vistas SQL para consultas complejas
- ✅ Procedimientos almacenados para operaciones bulk

### Auditoría
- ✅ Histórico de precios (historial_precio)
- ✅ Timestamps en todas las transacciones
- ✅ Congelación de datos históricos en detalle_venta

---

## 📝 NOTAS TÉCNICAS

1. **Fórmulas Financieras** (Estrictas):
   - `precioVenta = precioCompra + (precioCompra * (utilidad% / 100))`
   - `ivaLinea = precioVenta * (iva% / 100)`
   - `totalLinea = precioVenta + ivaLinea`

2. **Transacciones ACID**:
   - Todas las operaciones críticas usan `setAutoCommit(false)`
   - Rollback automático ante cualquier excepción
   - Commit al final si todo es exitoso

3. **Cierre de Recursos**:
   - try-with-resources para PreparedStatement y ResultSet
   - Conexión se mantiene abierta (Singleton)

4. **Encapsulamiento**:
   - Métodos privados para lógica interna
   - Métodos públicos solo para interfaz DAO
   - Propiedades JavaFX en modelos

---

## 📞 SOPORTE Y DEBUGGING

### Logs Estándar
```java
System.out.println("✓ Operación exitosa");  // Éxito
System.err.println("✗ Error: " + mensaje);   // Error
```

### Validaciones BD
```sql
-- Verificar integridad de ventas
SELECT COUNT(*) FROM detalle_venta WHERE id_venta IS NULL;

-- Ver stock actual
SELECT nombre, stock FROM piezas WHERE stock < 10;

-- Auditoria de precios
SELECT * FROM historial_precio WHERE fecha_fin IS NOT NULL LIMIT 10;
```

---

**Generado**: 16 Mayo 2026 | Versión: 1.0 | Estado: PRODUCCIÓN
