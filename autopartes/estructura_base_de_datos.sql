-- ============================================================================
-- SCRIPT SQL AVANZADO: ESTRUCTURA DE TABLAS Y LÓGICA DE NEGOCIO
-- GESTOR DE AUTO-PIEZAS (3FN) - MySQL
-- ============================================================================

-- ============================================================================
-- TABLA: proveedores
-- Propósito: Almacenar datos de proveedores (normalizado)
-- ============================================================================
CREATE TABLE IF NOT EXISTS proveedores (
    id_proveedor INT AUTO_INCREMENT PRIMARY KEY,
    razon_social VARCHAR(255) NOT NULL UNIQUE,
    contacto VARCHAR(255),
    telefono VARCHAR(20),
    email VARCHAR(255),
    direccion TEXT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_razon_social (razon_social)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: piezas (Modificada)
-- Propósito: Catálogo de productos (normalizado, sin duplicados)
-- ============================================================================
CREATE TABLE IF NOT EXISTS piezas (
    IDpieza INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    razon_social_proveedor VARCHAR(255),
    codigo_proveedor VARCHAR(50),
    precio_compra DECIMAL(10, 2) NOT NULL,
    imagen VARCHAR(500),
    id_estante VARCHAR(50),
    nivel_assigned INT,
    stock INT DEFAULT 0,
    cap_max INT,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_codigo_proveedor (codigo_proveedor),
    INDEX idx_id_estante (id_estante),
    INDEX idx_stock (stock)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: producto_proveedor (Relación N:M)
-- Propósito: Vincular múltiples proveedores a un mismo producto (3FN)
-- ============================================================================
CREATE TABLE IF NOT EXISTS producto_proveedor (
    id_prod_prov INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    id_proveedor INT NOT NULL,
    codigo_proveedor VARCHAR(50) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_producto) REFERENCES piezas(IDpieza) ON DELETE CASCADE,
    FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor) ON DELETE CASCADE,
    UNIQUE KEY uk_producto_proveedor (id_producto, id_proveedor),
    INDEX idx_id_producto (id_producto),
    INDEX idx_id_proveedor (id_proveedor)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: historial_precio
-- Propósito: Mantener histórico de precios de compra (auditoría 3FN)
-- ============================================================================
CREATE TABLE IF NOT EXISTS historial_precio (
    id_historial INT AUTO_INCREMENT PRIMARY KEY,
    id_prod_prov INT NOT NULL,
    precio_compra DECIMAL(10, 2) NOT NULL,
    fecha_inicio TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_fin TIMESTAMP NULL,
    FOREIGN KEY (id_prod_prov) REFERENCES producto_proveedor(id_prod_prov) ON DELETE CASCADE,
    INDEX idx_id_prod_prov (id_prod_prov),
    INDEX idx_fecha_inicio (fecha_inicio),
    INDEX idx_fecha_fin (fecha_fin)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: compra (Cabecera de compra)
-- Propósito: Registro de entradas de abastecimiento
-- ============================================================================
CREATE TABLE IF NOT EXISTS compra (
    id_compra INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    id_proveedor INT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    precio_compra DECIMAL(10, 2) NOT NULL,
    cantidad INT NOT NULL,
    total DECIMAL(12, 2) NOT NULL,
    FOREIGN KEY (id_producto) REFERENCES piezas(IDpieza) ON DELETE RESTRICT,
    FOREIGN KEY (id_proveedor) REFERENCES proveedores(id_proveedor) ON DELETE RESTRICT,
    INDEX idx_fecha (fecha),
    INDEX idx_id_producto (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: clientes
-- Propósito: Almacenar datos de clientes (normalizado, RFC único)
-- ============================================================================
CREATE TABLE IF NOT EXISTS clientes (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    rfc VARCHAR(20) UNIQUE NOT NULL,
    direccion TEXT NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_rfc (rfc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: ventas
-- Propósito: Registro de transacciones de venta (cabecera)
-- ============================================================================
CREATE TABLE IF NOT EXISTS ventas (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    fecha TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    total DECIMAL(10, 2) DEFAULT 0,
    FOREIGN KEY (id_cliente) REFERENCES clientes(id_cliente) ON DELETE CASCADE,
    INDEX idx_id_cliente (id_cliente),
    INDEX idx_fecha (fecha)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TABLA: detalle_venta
-- Propósito: Desglose de productos por venta (histórico de cálculos)
-- ============================================================================
CREATE TABLE IF NOT EXISTS detalle_venta (
    id_detalle INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    id_producto INT NOT NULL,
    precio_compra_historico DECIMAL(10, 2) NOT NULL,
    utilidad_pct INT NOT NULL,
    iva_pct DECIMAL(5, 2) NOT NULL,
    precio_venta_calculado DECIMAL(10, 2) NOT NULL,
    iva_calculado DECIMAL(10, 2) NOT NULL,
    total_linea DECIMAL(10, 2) NOT NULL,
    fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_venta) REFERENCES ventas(id_venta) ON DELETE CASCADE,
    FOREIGN KEY (id_producto) REFERENCES piezas(IDpieza) ON DELETE RESTRICT,
    INDEX idx_id_venta (id_venta),
    INDEX idx_id_producto (id_producto)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ============================================================================
-- TRIGGER 1: Actualizar total de venta después de insertar detalle
-- ============================================================================
DELIMITER //

DROP TRIGGER IF EXISTS tr_actualizar_total_venta //

CREATE TRIGGER tr_actualizar_total_venta
AFTER INSERT ON detalle_venta
FOR EACH ROW
BEGIN
    UPDATE ventas 
    SET total = (
        SELECT SUM(total_linea) 
        FROM detalle_venta 
        WHERE id_venta = NEW.id_venta
    )
    WHERE id_venta = NEW.id_venta;
END //

DELIMITER ;

-- ============================================================================
-- TRIGGER 2: Restar stock al registrar una venta (detalle_venta)
-- ============================================================================
DELIMITER //

DROP TRIGGER IF EXISTS tr_restar_stock_venta //

CREATE TRIGGER tr_restar_stock_venta
AFTER INSERT ON detalle_venta
FOR EACH ROW
BEGIN
    UPDATE piezas 
    SET stock = stock - 1 
    WHERE IDpieza = NEW.id_producto;
END //

DELIMITER ;

-- ============================================================================
-- TRIGGER 3: Validar stock antes de insertar detalle_venta
-- ============================================================================
DELIMITER //

DROP TRIGGER IF EXISTS tr_validar_stock_venta //

CREATE TRIGGER tr_validar_stock_venta
BEFORE INSERT ON detalle_venta
FOR EACH ROW
BEGIN
    DECLARE stock_disponible INT;
    
    SELECT stock INTO stock_disponible 
    FROM piezas 
    WHERE IDpieza = NEW.id_producto;
    
    IF stock_disponible <= 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Stock insuficiente para este producto';
    END IF;
END //

DELIMITER ;

-- ============================================================================
-- TRIGGER 4: Sumar stock al registrar una compra
-- ============================================================================
DELIMITER //

DROP TRIGGER IF EXISTS tr_sumar_stock_compra //

CREATE TRIGGER tr_sumar_stock_compra
AFTER INSERT ON compra
FOR EACH ROW
BEGIN
    UPDATE piezas 
    SET stock = stock + NEW.cantidad 
    WHERE IDpieza = NEW.id_producto;
END //

DELIMITER ;

-- ============================================================================
-- PROCEDIMIENTO ALMACENADO 1: Consultar precios por proveedor
-- Propósito: Permitir al vendedor elegir el precio más competitivo en tiempo real
-- ============================================================================
DELIMITER //

DROP PROCEDURE IF EXISTS ConsultarPreciosPorProveedor //

CREATE PROCEDURE ConsultarPreciosPorProveedor(IN producto_id INT)
BEGIN
    SELECT 
        prov.razon_social as Proveedor,
        pp.codigo_proveedor as 'Código Proveedor',
        hp.precio_compra as 'Precio Vigente',
        hp.fecha_inicio as 'Vigente Desde',
        prov.telefono as Contacto,
        prov.email as Email
    FROM producto_proveedor pp
    INNER JOIN proveedores prov ON pp.id_proveedor = prov.id_proveedor
    INNER JOIN historial_precio hp ON pp.id_prod_prov = hp.id_prod_prov
    WHERE pp.id_producto = producto_id
    AND hp.fecha_fin IS NULL
    ORDER BY hp.precio_compra ASC;
END //

DELIMITER ;

-- ============================================================================
-- PROCEDIMIENTO ALMACENADO 2: Generar reporte de ventas por rango de fechas
-- ============================================================================
DELIMITER //

DROP PROCEDURE IF EXISTS ReporteVentasPorFecha //

CREATE PROCEDURE ReporteVentasPorFecha(IN fecha_inicio DATE, IN fecha_fin DATE)
BEGIN
    SELECT 
        v.id_venta as 'ID Venta',
        v.fecha,
        c.nombre as Cliente,
        c.rfc,
        v.total as 'Total Venta',
        COUNT(dv.id_detalle) as 'Cantidad Productos'
    FROM ventas v
    INNER JOIN clientes c ON v.id_cliente = c.id_cliente
    LEFT JOIN detalle_venta dv ON v.id_venta = dv.id_venta
    WHERE DATE(v.fecha) BETWEEN fecha_inicio AND fecha_fin
    GROUP BY v.id_venta, v.fecha, c.nombre, c.rfc, v.total
    ORDER BY v.fecha DESC;
END //

DELIMITER ;

-- ============================================================================
-- PROCEDIMIENTO ALMACENADO 3: Productos con stock bajo
-- ============================================================================
DELIMITER //

DROP PROCEDURE IF EXISTS ProductosStockBajo //

CREATE PROCEDURE ProductosStockBajo(IN nivel_minimo INT)
BEGIN
    SELECT 
        IDpieza as 'ID Producto',
        nombre as Producto,
        stock as 'Stock Actual',
        cap_max as 'Stock Máximo',
        (cap_max - stock) as 'Unidades Faltantes',
        razon_social_proveedor as Proveedor
    FROM piezas
    WHERE stock <= nivel_minimo
    ORDER BY stock ASC;
END //

DELIMITER ;

-- ============================================================================
-- ÍNDICES ADICIONALES PARA OPTIMIZACIÓN
-- ============================================================================
CREATE INDEX idx_compra_fecha ON compra(fecha);
CREATE INDEX idx_detalle_fecha_registro ON detalle_venta(fecha_registro);
CREATE INDEX idx_venta_total ON ventas(total);

-- ============================================================================
-- VISTAS ÚTILES PARA CONSULTAS FRECUENTES
-- ============================================================================

-- Vista: Resumen de ventas por cliente
CREATE OR REPLACE VIEW vw_ventas_por_cliente AS
SELECT 
    c.id_cliente,
    c.nombre,
    c.rfc,
    COUNT(v.id_venta) as total_transacciones,
    SUM(v.total) as total_gastado
FROM clientes c
LEFT JOIN ventas v ON c.id_cliente = v.id_cliente
GROUP BY c.id_cliente, c.nombre, c.rfc;

-- Vista: Productos más vendidos
CREATE OR REPLACE VIEW vw_productos_mas_vendidos AS
SELECT 
    p.IDpieza,
    p.nombre,
    COUNT(dv.id_detalle) as cantidad_vendida,
    SUM(dv.total_linea) as total_ingresos
FROM detalle_venta dv
INNER JOIN piezas p ON dv.id_producto = p.IDpieza
GROUP BY p.IDpieza, p.nombre
ORDER BY cantidad_vendida DESC;

-- Vista: Proveedores activos con sus productos
CREATE OR REPLACE VIEW vw_proveedores_productos AS
SELECT 
    prov.id_proveedor,
    prov.razon_social,
    p.IDpieza,
    p.nombre as producto,
    pp.codigo_proveedor,
    hp.precio_compra as precio_vigente
FROM proveedores prov
INNER JOIN producto_proveedor pp ON prov.id_proveedor = pp.id_proveedor
INNER JOIN piezas p ON pp.id_producto = p.IDpieza
INNER JOIN historial_precio hp ON pp.id_prod_prov = hp.id_prod_prov
WHERE hp.fecha_fin IS NULL;

-- ============================================================================
-- CONSULTAS DE REFERENCIA PARA DESARROLLO
-- ============================================================================

-- Detalle completo de una venta específica
SELECT 
    v.id_venta,
    c.nombre as cliente,
    v.fecha,
    dv.id_producto,
    p.nombre as producto,
    dv.precio_compra_historico,
    dv.utilidad_pct,
    dv.iva_pct,
    dv.precio_venta_calculado,
    dv.iva_calculado,
    dv.total_linea
FROM ventas v
JOIN clientes c ON v.id_cliente = c.id_cliente
JOIN detalle_venta dv ON v.id_venta = dv.id_venta
JOIN piezas p ON dv.id_producto = p.IDpieza
WHERE v.id_venta = 1
ORDER BY dv.id_detalle;

-- Historial de precios de un producto
SELECT 
    prov.razon_social,
    pp.codigo_proveedor,
    hp.precio_compra,
    hp.fecha_inicio,
    hp.fecha_fin,
    CASE WHEN hp.fecha_fin IS NULL THEN 'VIGENTE' ELSE 'HISTÓRICO' END as estado
FROM historial_precio hp
INNER JOIN producto_proveedor pp ON hp.id_prod_prov = pp.id_prod_prov
INNER JOIN proveedores prov ON pp.id_proveedor = prov.id_proveedor
WHERE pp.id_producto = 1
ORDER BY hp.fecha_inicio DESC;
