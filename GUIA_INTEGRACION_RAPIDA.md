# 🔌 GUÍA RÁPIDA DE INTEGRACIÓN - SNIPPETS DE CÓDIGO

## 1. Activar Botón "Generar Cotización" en CarritoVenta.fxml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<VBox xmlns="http://javafx.com/javafx" xmlns:fx="http://javafx.com/fxml" 
      fx:controller="com.autopartes.Controlador.CarritoVentaC">
    
    <!-- Tabla del carrito -->
    <TableView fx:id="tableViewCarrito">
        <!-- Columnas existentes... -->
    </TableView>
    
    <!-- Botones de acción -->
    <HBox spacing="10">
        <Button fx:id="btnAgregarItem" text="Agregar Item" onAction="#agregarItemAlCarrito"/>
        <Button fx:id="btnGenerarCotizacion" text="📄 Generar Cotización" 
                onAction="#generarCotizacion" style="-fx-text-fill: #0066cc;"/>
        <Button fx:id="btnIrACheckout" text="➜ Ir a Checkout" 
                onAction="#irACheckout" style="-fx-text-fill: #00aa00;"/>
    </HBox>
</VBox>
```

---

## 2. Configurar RegistroCompra.fxml (NUEVO)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.geometry.Insets?>

<VBox spacing="15" style="-fx-padding: 20;" xmlns="http://javafx.com/javafx" 
      xmlns:fx="http://javafx.com/fxml" 
      fx:controller="com.autopartes.Controlador.RegistroCompraC">
    
    <!-- FORMULARIO DE ENTRADA -->
    <TitledPane text="Registrar Compra" collapsible="false">
        <VBox spacing="10" style="-fx-padding: 10;">
            
            <HBox spacing="10">
                <Label text="Pieza:" minWidth="80"/>
                <ComboBox fx:id="cmbPieza" prefWidth="300"/>
            </HBox>
            
            <HBox spacing="10">
                <Label text="Stock Actual:" minWidth="80"/>
                <Label fx:id="lblStockActual" text="N/A" style="-fx-text-fill: #0066cc; -fx-font-weight: bold;"/>
            </HBox>
            
            <HBox spacing="10">
                <Label text="Proveedor:" minWidth="80"/>
                <ComboBox fx:id="cmbProveedor" prefWidth="300"/>
            </HBox>
            
            <HBox spacing="10">
                <Label text="Info:" minWidth="80"/>
                <Label fx:id="lblProveedorInfo" text="Seleccione proveedor"/>
            </HBox>
            
            <HBox spacing="10">
                <Label text="Código Proveedor:" minWidth="80"/>
                <TextField fx:id="txtCodigoProveedor" prefWidth="200" promptText="Ej: PRV-001-ABC"/>
            </HBox>
            
            <HBox spacing="10">
                <Label text="Precio Compra ($):" minWidth="80"/>
                <TextField fx:id="txtPrecioCompra" prefWidth="150" promptText="0.00"/>
            </HBox>
            
            <HBox spacing="10">
                <Label text="Cantidad (Unidades):" minWidth="80"/>
                <TextField fx:id="txtCantidad" prefWidth="150" promptText="0"/>
            </HBox>
            
            <HBox spacing="10" style="-fx-padding: 10; -fx-background-color: #f0f0f0;">
                <Button fx:id="btnRegistrarCompra" text="✓ Registrar Compra" 
                        onAction="#registrarCompra" 
                        style="-fx-font-size: 12; -fx-padding: 10; -fx-text-fill: white; 
                               -fx-background-color: #00aa00;"/>
                <Button fx:id="btnCancelar" text="✗ Cancelar" 
                        onAction="#cancelar"
                        style="-fx-font-size: 12; -fx-padding: 10;"/>
            </HBox>
        </VBox>
    </TitledPane>
    
    <!-- HISTORIAL DE COMPRAS -->
    <TitledPane text="Historial de Compras Recientes" collapsible="false">
        <TableView fx:id="tableViewHistorial" prefHeight="300">
            <columns>
                <TableColumn fx:id="colPieza" text="Pieza" prefWidth="150"/>
                <TableColumn fx:id="colProveedor" text="Proveedor" prefWidth="120"/>
                <TableColumn fx:id="colCodigo" text="Código Proveedor" prefWidth="120"/>
                <TableColumn fx:id="colPrecio" text="Precio Compra" prefWidth="100"/>
                <TableColumn fx:id="colCantidad" text="Cantidad" prefWidth="80"/>
                <TableColumn fx:id="colFecha" text="Fecha" prefWidth="150"/>
            </columns>
        </TableView>
    </TitledPane>
</VBox>
```

---

## 3. Actualizar Checkout.fxml (Completo)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>
<?import javafx.geometry.Insets?>

<VBox spacing="20" style="-fx-padding: 30;" xmlns="http://javafx.com/javafx" 
      xmlns:fx="http://javafx.com/fxml" 
      fx:controller="com.autopartes.Controlador.CheckoutC">
    
    <Label text="CHECKOUT - DATOS DEL CLIENTE" style="-fx-font-size: 16; -fx-font-weight: bold;"/>
    
    <!-- DATOS DEL CLIENTE -->
    <GridPane hgap="15" vgap="10">
        <Label text="Razón Social:" GridPane.rowIndex="0" GridPane.columnIndex="0"/>
        <TextField fx:id="txtRazonSocial" GridPane.rowIndex="0" GridPane.columnIndex="1" 
                   promptText="Nombre o Razón Social" prefWidth="300"/>
        
        <Label text="RFC:" GridPane.rowIndex="1" GridPane.columnIndex="0"/>
        <TextField fx:id="txtRFC" GridPane.rowIndex="1" GridPane.columnIndex="1" 
                   promptText="RFC del Cliente" prefWidth="300"/>
        
        <Label text="Dirección Fiscal:" GridPane.rowIndex="2" GridPane.columnIndex="0"/>
        <TextArea fx:id="txtDireccionFiscal" GridPane.rowIndex="2" GridPane.columnIndex="1" 
                  promptText="Dirección completa" prefWidth="300" prefHeight="60"/>
    </GridPane>
    
    <Separator/>
    
    <!-- RESUMEN DE TOTALES -->
    <VBox spacing="10" style="-fx-padding: 15; -fx-border-color: #cccccc; -fx-border-radius: 5;">
        <Label text="RESUMEN DE VENTA" style="-fx-font-size: 14; -fx-font-weight: bold;"/>
        
        <HBox spacing="20">
            <VBox spacing="5">
                <Label text="Subtotal (sin IVA):" style="-fx-font-size: 12;"/>
                <Label fx:id="lblSubtotalVenta" text="$0.00" 
                       style="-fx-font-size: 16; -fx-text-fill: #0066cc; -fx-font-weight: bold;"/>
            </VBox>
            
            <VBox spacing="5">
                <Label text="IVA (16%):" style="-fx-font-size: 12;"/>
                <Label fx:id="lblIvaVenta" text="$0.00" 
                       style="-fx-font-size: 16; -fx-text-fill: #ff9900; -fx-font-weight: bold;"/>
            </VBox>
            
            <VBox spacing="5">
                <Label text="TOTAL A PAGAR:" style="-fx-font-size: 12; -fx-font-weight: bold;"/>
                <Label fx:id="lblTotalVenta" text="$0.00" 
                       style="-fx-font-size: 20; -fx-text-fill: #00aa00; -fx-font-weight: bold;"/>
            </VBox>
        </HBox>
    </VBox>
    
    <!-- BOTONES DE ACCIÓN -->
    <HBox spacing="15" style="-fx-alignment: center;">
        <Button fx:id="btnConfirmarVenta" text="✓ CONFIRMAR VENTA" 
                onAction="#confirmarVenta"
                style="-fx-font-size: 14; -fx-padding: 12 30; -fx-text-fill: white;
                       -fx-background-color: #00aa00;"/>
        <Button fx:id="btnVolverCarrito" text="← Volver al Carrito" 
                onAction="#volverCarrito"
                style="-fx-font-size: 12; -fx-padding: 10 20;"/>
    </HBox>
</VBox>
```

---

## 4. Actualizar ReporteVenta.fxml (Completo)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<?import javafx.scene.control.*?>
<?import javafx.scene.layout.*?>

<VBox spacing="15" style="-fx-padding: 20;" xmlns="http://javafx.com/javafx" 
      xmlns:fx="http://javafx.com/fxml" 
      fx:controller="com.autopartes.Controlador.ReporteVentaC">
    
    <Label text="REPORTE DE VENTAS" style="-fx-font-size: 18; -fx-font-weight: bold;"/>
    
    <!-- TABLA PRINCIPAL DE VENTAS -->
    <TitledPane text="Historial de Ventas" collapsible="false">
        <TableView fx:id="tableViewVentas" prefHeight="250">
            <columns>
                <TableColumn fx:id="colIdVenta" text="ID Venta" prefWidth="80"/>
                <TableColumn fx:id="colFecha" text="Fecha" prefWidth="150"/>
                <TableColumn fx:id="colCliente" text="Cliente" prefWidth="200"/>
                <TableColumn fx:id="colTotal" text="Total ($)" prefWidth="100"/>
            </columns>
        </TableView>
    </TitledPane>
    
    <!-- INFORMACIÓN DE VENTA SELECCIONADA -->
    <VBox spacing="10" style="-fx-padding: 15; -fx-border-color: #cccccc;">
        <Label fx:id="lblClienteSeleccionado" text="Cliente: -" 
               style="-fx-font-size: 12; -fx-font-weight: bold;"/>
        <Label fx:id="lblFechaVenta" text="Fecha: -" style="-fx-font-size: 12;"/>
        <Label fx:id="lblTotalVenta" text="Total: $0.00" 
               style="-fx-font-size: 14; -fx-text-fill: #00aa00; -fx-font-weight: bold;"/>
    </VBox>
    
    <!-- TABLA DE DETALLES -->
    <TitledPane text="Detalle de Productos" collapsible="false">
        <TableView fx:id="tableViewDetalles" prefHeight="250">
            <columns>
                <TableColumn fx:id="colProducto" text="Producto" prefWidth="150"/>
                <TableColumn fx:id="colPrecioCompra" text="Precio Base ($)" prefWidth="100"/>
                <TableColumn fx:id="colUtilidadPct" text="Utilidad (%)" prefWidth="80"/>
                <TableColumn fx:id="colIvaPct" text="IVA (%)" prefWidth="70"/>
                <TableColumn fx:id="colTotalLinea" text="Total Línea ($)" prefWidth="100"/>
            </columns>
        </TableView>
    </TitledPane>
    
    <!-- BOTONES -->
    <HBox spacing="10" style="-fx-alignment: center;">
        <Button fx:id="btnVerDetalle" text="👁 Ver Detalle" onAction="#verDetalle"/>
        <Button fx:id="btnVolver" text="← Volver" onAction="#volver"/>
    </HBox>
</VBox>
```

---

## 5. Actualizar GestorVistas.java (Agregar Permisos)

```java
private static boolean tienePermiso(String nombreFxml) {
    if (nombreFxml.equals("Login.fxml")) {
        return true;
    }

    Usuario usuarioActual = Sesion.getUsuario();
    if (usuarioActual == null) {
        return false;
    }

    String rol = usuarioActual.getRol().trim();

    switch (rol) {
        case "Cajero":
            return nombreFxml.equals("CatalogoVendedor.fxml") || 
                   nombreFxml.equals("VistaStock.fxml") || 
                   nombreFxml.equals("CarritoVenta.fxml") || 
                   nombreFxml.equals("Checkout.fxml") || 
                   nombreFxml.equals("ReporteVenta.fxml");

        case "Almacenista":
            return nombreFxml.equals("VistaStockAlm.fxml") ||
                   nombreFxml.equals("VistaStock.fxml") ||
                   nombreFxml.equals("RegistroCompra.fxml") ||  // ← NUEVO
                   nombreFxml.equals("ReporteVenta.fxml");

        case "Vendedor":
            return nombreFxml.equals("ReporteVenta.fxml");

        default:
            return false;
    }
}
```

---

## 6. Script de Inicialización de Datos (EJEMPLO)

```sql
-- Insertar proveedores de ejemplo
INSERT INTO proveedores (razon_social, contacto, telefono, email, direccion) VALUES
('Autopartes García', 'Carlos García', '555-1234', 'carlos@autopartes.com', 'Calle Principal 123'),
('Distribuidora Mecánica', 'Juan López', '555-5678', 'juan@mecanica.com', 'Av. Industrial 456'),
('Importadora de Refacciones', 'María González', '555-9999', 'maria@importadora.com', 'Zona Industrial 789');

-- Verificar datos
SELECT * FROM proveedores;

-- Insertar cliente de ejemplo
INSERT INTO clientes (nombre, rfc, direccion) VALUES
('Taller Automotriz XYZ', 'TAX-123456789', 'Calle Comercial 100');

-- Ejecutar procedimiento de consulta de precios
CALL ConsultarPreciosPorProveedor(1);
```

---

## 7. Ejemplo de Consulta en Java (Para debugging)

```java
// Obtener todas las ventas
List<VentaDAO.VentaItem> ventas = ventaDAO.obtenerTodasLasVentas();
for (VentaDAO.VentaItem venta : ventas) {
    System.out.println("ID: " + venta.getIdVenta() + 
                       " | Cliente: " + venta.getCliente() + 
                       " | Total: $" + String.format("%.2f", venta.getTotal()));
}

// Obtener detalles de una venta
List<VentaDAO.DetalleVentaItem> detalles = ventaDAO.obtenerDetallesVenta(1);
for (VentaDAO.DetalleVentaItem detalle : detalles) {
    System.out.println("Producto: " + detalle.getProducto() + 
                       " | Precio: $" + detalle.getPrecioCompra() + 
                       " | Utilidad: " + detalle.getUtilidadPct() + "% " +
                       " | Total: $" + String.format("%.2f", detalle.getTotalLinea()));
}
```

---

## 8. Flujo Completo en Menú Principal

```java
// En App.java o ControladorMenuPrincipal.java
@FXML
private void irACarrito() {
    GestorVistas.cambiarVista("CarritoVenta.fxml");
}

@FXML
private void irARegistroCompra() {
    GestorVistas.cambiarVista("RegistroCompra.fxml");
}

@FXML
private void irAReportes() {
    GestorVistas.cambiarVista("ReporteVenta.fxml");
}
```

---

## 9. Comando Terminal para Ejecutar SQL Desde Java

```java
// Opción: Leer y ejecutar script SQL desde aplicación
try {
    String scriptPath = "estructura_base_de_datos.sql";
    ProcessBuilder pb = new ProcessBuilder(
        "mysql", "-u", "root", "-p", "DBA"
    );
    pb.redirectInput(new File(scriptPath));
    Process process = pb.start();
    process.waitFor();
    System.out.println("Script SQL ejecutado correctamente.");
} catch (IOException | InterruptedException e) {
    e.printStackTrace();
}
```

---

## 10. Checklist de Verificación

- [ ] Script SQL ejecutado sin errores
- [ ] Tablas creadas en BD (`SHOW TABLES;`)
- [ ] Triggers activos (`SHOW TRIGGERS;`)
- [ ] FXML files actualizado con componentes
- [ ] GestorVistas.java con permisos de RegistroCompra
- [ ] Proveedor.java compilado
- [ ] ProveedorDAO.java compilado
- [ ] CompraDAO.java compilado
- [ ] RegistroCompraC.java compilado
- [ ] VentaDAO.java métodos de lectura compilados
- [ ] CarritoVentaC.java con cotizaciones compilado
- [ ] Prueba de generación de cotización (.txt)
- [ ] Prueba de flujo Carrito → Checkout → Venta
- [ ] Prueba de Registro de Compra transaccional
- [ ] Verificar historial de compras en tabla
- [ ] Verificar reportes de ventas

---

**Última actualización**: 16 de Mayo de 2026  
**Versión**: 1.0 Release
