-- TIENDA ONLINE - SCRIPT FINAL TODO EN UNO - LIMA, PERU
-- Crea la base de datos, las 12 tablas y 1,057 registros de demostracion.
-- Incluye 100 clientes, 100 direcciones, 100 ventas y 100 pagos ficticios.
-- Precios expresados en soles (PEN); nombres, correos y telefonos ficticios.
-- Compatible con MySQL Workbench + MySQL 8.0; funciona desde una base vacia.
-- Seguro de repetir: NO elimina tablas reales ni datos existentes.
-- Corregidos los errores 1067 (TIMESTAMP temporal) y 1175 (Safe Update).
-- AVISO: Solo para fines de clase y demostracion, no para datos reales.

-- SISTEMA DE VENTAS ONLINE Y ADMINISTRACION
-- Basado en las 12 tablas del diagrama proporcionado.
-- Compatible con MySQL 8.0+ (motor InnoDB).
-- No borra bases de datos ni tablas existentes.

CREATE DATABASE IF NOT EXISTS tienda_online
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE tienda_online;

-- 1. CLIENTES
CREATE TABLE IF NOT EXISTS cliente (
    id_cliente INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL,
    telefono VARCHAR(20),
    fecha_registro TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cliente_email UNIQUE (email)
) ENGINE=InnoDB;

-- 2. DIRECCIONES (un cliente puede tener varias direcciones)
CREATE TABLE IF NOT EXISTS direccion (
    id_direccion INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    calle VARCHAR(200) NOT NULL,
    ciudad VARCHAR(100) NOT NULL,
    estado_provincia VARCHAR(100) NOT NULL,
    codigo_postal VARCHAR(20),
    pais VARCHAR(50) NOT NULL,
    INDEX idx_direccion_cliente (id_cliente),
    CONSTRAINT fk_direccion_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 3. CATEGORIAS
CREATE TABLE IF NOT EXISTS categoria (
    id_categoria INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- 4. TALLAS
CREATE TABLE IF NOT EXISTS talla (
    id_talla INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(20) NOT NULL,
    CONSTRAINT uq_talla_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- 5. COLORES
CREATE TABLE IF NOT EXISTS color (
    id_color INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL,
    CONSTRAINT uq_color_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- 6. PRODUCTOS
CREATE TABLE IF NOT EXISTS producto (
    id_producto INT AUTO_INCREMENT PRIMARY KEY,
    id_categoria INT NOT NULL,
    nombre VARCHAR(150) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(10,2) NOT NULL,
    sku VARCHAR(50) NOT NULL,
    CONSTRAINT uq_producto_sku UNIQUE (sku),
    INDEX idx_producto_categoria (id_categoria),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria(id_categoria)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_producto_precio CHECK (precio >= 0)
) ENGINE=InnoDB;

-- 7. VARIANTES DE PRODUCTOS (producto + talla + color)
CREATE TABLE IF NOT EXISTS variante_producto (
    id_variante INT AUTO_INCREMENT PRIMARY KEY,
    id_producto INT NOT NULL,
    id_talla INT NOT NULL,
    id_color INT NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    stock_minimo INT NOT NULL DEFAULT 0,
    CONSTRAINT uq_variante_combinacion UNIQUE (id_producto, id_talla, id_color),
    INDEX idx_variante_talla (id_talla),
    INDEX idx_variante_color (id_color),
    CONSTRAINT fk_variante_producto FOREIGN KEY (id_producto)
        REFERENCES producto(id_producto)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_variante_talla FOREIGN KEY (id_talla)
        REFERENCES talla(id_talla)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_variante_color FOREIGN KEY (id_color)
        REFERENCES color(id_color)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_variante_stock CHECK (stock >= 0),
    CONSTRAINT chk_variante_stock_minimo CHECK (stock_minimo >= 0)
) ENGINE=InnoDB;

-- 8. CARRITOS
CREATE TABLE IF NOT EXISTS carrito (
    id_carrito INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_carrito_cliente (id_cliente),
    CONSTRAINT fk_carrito_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente)
        ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB;

-- 9. DETALLES DE CARRITO
CREATE TABLE IF NOT EXISTS detalle_carrito (
    id_detalle_carrito INT AUTO_INCREMENT PRIMARY KEY,
    id_carrito INT NOT NULL,
    id_variante INT NOT NULL,
    cantidad INT NOT NULL,
    CONSTRAINT uq_detalle_carrito_variante UNIQUE (id_carrito, id_variante),
    INDEX idx_detalle_carrito_variante (id_variante),
    CONSTRAINT fk_detalle_carrito_carrito FOREIGN KEY (id_carrito)
        REFERENCES carrito(id_carrito)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT fk_detalle_carrito_variante FOREIGN KEY (id_variante)
        REFERENCES variante_producto(id_variante)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_detalle_carrito_cantidad CHECK (cantidad > 0)
) ENGINE=InnoDB;

-- 10. VENTAS
CREATE TABLE IF NOT EXISTS venta (
    id_venta INT AUTO_INCREMENT PRIMARY KEY,
    id_cliente INT NOT NULL,
    id_direccion INT NOT NULL,
    fecha_venta TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tipo_venta VARCHAR(20) NOT NULL DEFAULT 'online',
    estado VARCHAR(30) NOT NULL DEFAULT 'pendiente',
    total DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    INDEX idx_venta_cliente (id_cliente),
    INDEX idx_venta_direccion (id_direccion),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente(id_cliente)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_venta_direccion FOREIGN KEY (id_direccion)
        REFERENCES direccion(id_direccion)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_venta_total CHECK (total >= 0)
) ENGINE=InnoDB;

-- 11. DETALLES DE VENTA
CREATE TABLE IF NOT EXISTS detalle_venta (
    id_detalle_venta INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    id_variante INT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    subtotal DECIMAL(10,2) NOT NULL,
    INDEX idx_detalle_venta_venta (id_venta),
    INDEX idx_detalle_venta_variante (id_variante),
    CONSTRAINT fk_detalle_venta_venta FOREIGN KEY (id_venta)
        REFERENCES venta(id_venta)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT fk_detalle_venta_variante FOREIGN KEY (id_variante)
        REFERENCES variante_producto(id_variante)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT chk_detalle_venta_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_venta_precio CHECK (precio_unitario >= 0),
    CONSTRAINT chk_detalle_venta_subtotal CHECK (subtotal >= 0)
) ENGINE=InnoDB;

-- 12. PAGOS (admite varios intentos de pago por venta)
CREATE TABLE IF NOT EXISTS pago (
    id_pago INT AUTO_INCREMENT PRIMARY KEY,
    id_venta INT NOT NULL,
    metodo_pago VARCHAR(50) NOT NULL,
    estado_pago VARCHAR(30) NOT NULL DEFAULT 'pendiente',
    transaccion_pasarela_id VARCHAR(100),
    fecha_pago TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pago_venta (id_venta),
    CONSTRAINT fk_pago_venta FOREIGN KEY (id_venta)
        REFERENCES venta(id_venta)
        ON UPDATE CASCADE ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ---------------------------------------------------------------
-- CARGA DE DATOS COHERENTES PARA LAS 12 TABLAS
-- ---------------------------------------------------------------

USE tienda_online;

-- 1/12 CLIENTE: 100 clientes. Email unico evita duplicados.
INSERT IGNORE INTO cliente (nombre, apellido, email, telefono, fecha_registro)
VALUES
    ('Álvaro', 'Ramos Sánchez', 'alvaro.ramos001@example.com', '907192842', '2026-08-14 02:22:02'),
    ('Alonso', 'Gutiérrez Acosta', 'alonso.gutierrez002@example.com', '901764319', '2026-05-26 09:33:23'),
    ('Lucía', 'Torres Palomino', 'lucia.torres003@example.com', '967743509', '2026-08-05 15:22:44'),
    ('Luis', 'Huerta Mendoza', 'luis.huerta004@example.com', '968804448', '2025-11-15 22:03:05'),
    ('Luis', 'Mejía Navarro', 'luis.mejia005@example.com', '932341634', '2026-06-28 06:04:16'),
    ('Mateo', 'Rodríguez Ramírez', 'mateo.rodriguez006@example.com', '954174850', '2025-04-27 13:02:12'),
    ('Lucía', 'Cabrera Villanueva', 'lucia.cabrera007@example.com', '920216739', '2026-09-03 15:09:09'),
    ('Eduardo', 'Huamán Guerrero', 'eduardo.huaman008@example.com', '919591466', '2025-03-05 12:55:06'),
    ('Jorge', 'Ayala Silva', 'jorge.ayala009@example.com', '956894816', '2026-03-24 08:53:23'),
    ('Eduardo', 'Núñez Tapia', 'eduardo.nunez010@example.com', '944226503', '2025-10-25 20:20:18'),
    ('Axel', 'Tello Aguilar', 'axel.tello011@example.com', '956618098', '2026-02-25 07:56:28'),
    ('Kevin', 'Quispe Campos', 'kevin.quispe012@example.com', '929200819', '2025-05-01 20:56:27'),
    ('Renato', 'Castro Cárdenas', 'renato.castro013@example.com', '909276866', '2026-10-16 16:02:06'),
    ('Daniela', 'Flores Quispe', 'daniela.flores014@example.com', '912319521', '2026-05-07 04:08:58'),
    ('Andrea', 'Soto Flores', 'andrea.soto015@example.com', '948086180', '2026-05-01 15:55:25'),
    ('Paola', 'Quispe Torres', 'paola.quispe016@example.com', '907711439', '2025-02-17 08:11:48'),
    ('Karina', 'Poma Vargas', 'karina.poma017@example.com', '986447493', '2025-09-11 12:24:04'),
    ('David', 'García Sánchez', 'david.garcia018@example.com', '920287021', '2026-06-04 18:30:19'),
    ('Juan', 'Acosta Flores', 'juan.acosta019@example.com', '991048356', '2026-08-11 09:33:56'),
    ('Karina', 'García Aguilar', 'karina.garcia020@example.com', '995964355', '2026-09-05 02:25:07'),
    ('Mariana', 'Torres Soto', 'mariana.torres021@example.com', '984522979', '2025-09-01 07:52:00'),
    ('Bruno', 'Paredes Espinoza', 'bruno.paredes022@example.com', '911679426', '2025-05-18 20:07:28'),
    ('Alessandra', 'Flores Gutiérrez', 'alessandra.flores023@example.com', '976172183', '2025-06-27 19:51:41'),
    ('Sebastián', 'Aguilar Vega', 'sebastian.aguilar024@example.com', '990544390', '2025-06-16 23:31:12'),
    ('Sofía', 'Paredes Núñez', 'sofia.paredes025@example.com', '984573294', '2026-07-25 19:58:37'),
    ('Anthony', 'Mendoza Palomino', 'anthony.mendoza026@example.com', '903396534', '2025-09-22 09:37:09'),
    ('Axel', 'Vega Silva', 'axel.vega027@example.com', '972648854', '2026-01-17 22:57:05'),
    ('Claudia', 'Núñez Mejía', 'claudia.nunez028@example.com', '994640749', '2025-04-22 08:59:25'),
    ('Mateo', 'Gutiérrez Peña', 'mateo.gutierrez029@example.com', '945157497', '2025-02-21 23:59:50'),
    ('César', 'Paredes Aguilar', 'cesar.paredes030@example.com', '903979763', '2025-09-16 18:34:54'),
    ('Lucía', 'Paredes Ramírez', 'lucia.paredes031@example.com', '991871177', '2026-09-26 11:06:50'),
    ('Daniela', 'Peña Ramírez', 'daniela.pena032@example.com', '952599025', '2025-04-21 08:10:15'),
    ('Eduardo', 'Peña Tello', 'eduardo.pena033@example.com', '978159921', '2026-03-16 20:20:32'),
    ('Diego', 'Espinoza Vargas', 'diego.espinoza034@example.com', '978877039', '2025-07-05 20:32:43'),
    ('Alessandra', 'Valdivia Gutiérrez', 'alessandra.valdivia035@example.com', '919568586', '2025-06-14 16:08:00'),
    ('José', 'Espinoza Cruz', 'jose.espinoza036@example.com', '950977108', '2026-09-17 03:24:07'),
    ('Natalia', 'Ramírez Navarro', 'natalia.ramirez037@example.com', '909489631', '2025-12-13 09:22:53'),
    ('Mateo', 'Torres Calderón', 'mateo.torres038@example.com', '907992848', '2025-05-08 11:49:13'),
    ('David', 'Soto Ramírez', 'david.soto039@example.com', '930461676', '2025-10-29 19:02:26'),
    ('Miguel', 'López Mejía', 'miguel.lopez040@example.com', '993917496', '2025-10-23 12:02:52'),
    ('Fernando', 'Rojas Silva', 'fernando.rojas041@example.com', '951205672', '2025-11-03 23:37:09'),
    ('Daniela', 'León Sánchez', 'daniela.leon042@example.com', '905835936', '2026-07-23 04:12:16'),
    ('Víctor', 'Paredes Quispe', 'victor.paredes043@example.com', '924498783', '2026-02-05 16:13:55'),
    ('Milagros', 'Poma Herrera', 'milagros.poma044@example.com', '985648520', '2025-06-26 19:23:35'),
    ('Alejandra', 'Cárdenas Castillo', 'alejandra.cardenas045@example.com', '936630594', '2026-03-01 07:14:53'),
    ('Diana', 'Medina Mejía', 'diana.medina046@example.com', '999577178', '2025-06-25 07:34:12'),
    ('Axel', 'Vega Rodríguez', 'axel.vega047@example.com', '948737082', '2025-04-13 07:57:40'),
    ('Lucía', 'Cárdenas Cruz', 'lucia.cardenas048@example.com', '977926966', '2025-02-17 12:25:04'),
    ('Diego', 'Ayala Villanueva', 'diego.ayala049@example.com', '995014756', '2026-02-06 05:04:46'),
    ('David', 'Tello Ramírez', 'david.tello050@example.com', '985330087', '2026-08-02 08:19:20'),
    ('Axel', 'Guerrero Salazar', 'axel.guerrero051@example.com', '993930724', '2026-02-20 11:12:17'),
    ('David', 'Sánchez Campos', 'david.sanchez052@example.com', '965234612', '2026-03-21 00:26:40'),
    ('Rocío', 'Guerrero Rojas', 'rocio.guerrero053@example.com', '967313915', '2025-05-08 18:50:55'),
    ('Óscar', 'Díaz Tello', 'oscar.diaz054@example.com', '907966495', '2025-12-29 21:44:21'),
    ('Diego', 'Silva Ramírez', 'diego.silva055@example.com', '979888503', '2025-08-22 06:54:25'),
    ('Andrea', 'León Mejía', 'andrea.leon056@example.com', '935541168', '2026-07-27 04:39:49'),
    ('Sofía', 'Rojas Cárdenas', 'sofia.rojas057@example.com', '989642598', '2026-04-09 16:53:33'),
    ('Katherine', 'López Espinoza', 'katherine.lopez058@example.com', '934779879', '2025-01-21 08:10:44'),
    ('Milagros', 'Peña Huerta', 'milagros.pena059@example.com', '968663551', '2025-05-24 19:35:39'),
    ('Óscar', 'Tapia Ayala', 'oscar.tapia060@example.com', '909446117', '2025-05-29 22:31:48'),
    ('Lucía', 'Díaz Castro', 'lucia.diaz061@example.com', '995965749', '2025-03-27 07:12:28'),
    ('Diana', 'Aguilar Díaz', 'diana.aguilar062@example.com', '918308848', '2026-02-01 19:10:01'),
    ('Miguel', 'Ramírez Cabrera', 'miguel.ramirez063@example.com', '969637567', '2026-09-21 23:22:09'),
    ('José', 'Huamán Torres', 'jose.huaman064@example.com', '925511972', '2025-09-20 21:02:12'),
    ('Miguel', 'Sánchez Castro', 'miguel.sanchez065@example.com', '916591161', '2025-06-24 11:52:37'),
    ('Luisa', 'Rojas Yupanqui', 'luisa.rojas066@example.com', '968633287', '2025-11-07 03:21:30'),
    ('Ximena', 'León Aguilar', 'ximena.leon067@example.com', '925562078', '2025-10-07 16:16:43'),
    ('Juan', 'Mejía Chávez', 'juan.mejia068@example.com', '954601431', '2026-04-22 12:51:53'),
    ('Karina', 'Silva Tapia', 'karina.silva069@example.com', '974025190', '2025-01-27 20:52:29'),
    ('Valeria', 'García Paredes', 'valeria.garcia070@example.com', '980455654', '2026-04-24 22:22:17'),
    ('Kevin', 'Cabrera Sánchez', 'kevin.cabrera071@example.com', '993619229', '2026-04-20 05:03:07'),
    ('José', 'López Cabrera', 'jose.lopez072@example.com', '951030945', '2025-02-22 05:24:17'),
    ('Cristian', 'Mendoza Valdivia', 'cristian.mendoza073@example.com', '902754250', '2025-10-04 03:14:54'),
    ('Anthony', 'Castro Valdivia', 'anthony.castro074@example.com', '985279937', '2025-03-21 18:30:02'),
    ('Bruno', 'Tapia Cruz', 'bruno.tapia075@example.com', '960564606', '2025-06-21 02:13:22'),
    ('Camila', 'Castro Rojas', 'camila.castro076@example.com', '973353182', '2025-07-15 20:39:24'),
    ('Miguel', 'Huamán León', 'miguel.huaman077@example.com', '944514585', '2026-02-25 05:40:16'),
    ('Paola', 'Calderón Vega', 'paola.calderon078@example.com', '924000763', '2026-09-14 04:18:02'),
    ('Carlos', 'Villanueva Navarro', 'carlos.villanueva079@example.com', '956014589', '2025-02-25 17:37:06'),
    ('Anthony', 'Valdivia Campos', 'anthony.valdivia080@example.com', '932580912', '2026-03-18 00:00:10'),
    ('Ángel', 'Villanueva Yupanqui', 'angel.villanueva081@example.com', '995612829', '2026-04-04 08:22:21'),
    ('Diana', 'Villanueva Navarro', 'diana.villanueva082@example.com', '960215167', '2026-05-14 08:02:57'),
    ('Andrea', 'Acosta Cabrera', 'andrea.acosta083@example.com', '952940736', '2025-01-21 02:13:39'),
    ('Alejandra', 'Mendoza Ramírez', 'alejandra.mendoza084@example.com', '950664785', '2025-06-21 07:51:21'),
    ('Fernando', 'Paredes Ramos', 'fernando.paredes085@example.com', '907859844', '2025-08-01 00:26:34'),
    ('Víctor', 'Valdivia Yupanqui', 'victor.valdivia086@example.com', '979569517', '2026-05-08 21:04:56'),
    ('Sofía', 'Ayala Yupanqui', 'sofia.ayala087@example.com', '969433700', '2025-12-25 14:26:34'),
    ('Lucía', 'Acosta Huerta', 'lucia.acosta088@example.com', '937037522', '2025-02-13 20:11:00'),
    ('Claudia', 'Tapia Herrera', 'claudia.tapia089@example.com', '969680336', '2026-07-18 16:30:46'),
    ('Camila', 'Salazar García', 'camila.salazar090@example.com', '945454091', '2026-09-23 01:00:13'),
    ('Sebastián', 'Aguilar Palomino', 'sebastian.aguilar091@example.com', '910799178', '2025-06-21 14:40:43'),
    ('Alonso', 'Torres Peña', 'alonso.torres092@example.com', '916037012', '2025-07-26 23:54:16'),
    ('Óscar', 'Chávez Herrera', 'oscar.chavez093@example.com', '918960757', '2026-05-29 20:48:30'),
    ('Axel', 'Ayala Mendoza', 'axel.ayala094@example.com', '906357817', '2026-04-03 08:46:25'),
    ('Alonso', 'Palomino Herrera', 'alonso.palomino095@example.com', '916048385', '2026-02-12 14:21:42'),
    ('Álvaro', 'Gutiérrez Rojas', 'alvaro.gutierrez096@example.com', '963020809', '2025-09-02 01:09:55'),
    ('Natalia', 'Cárdenas Castro', 'natalia.cardenas097@example.com', '964197987', '2026-06-07 04:34:50'),
    ('Ximena', 'Mendoza Silva', 'ximena.mendoza098@example.com', '975195661', '2026-06-26 05:40:02'),
    ('Diana', 'Acosta Flores', 'diana.acosta099@example.com', '997845567', '2025-04-26 09:17:57'),
    ('Fernando', 'Castro Castillo', 'fernando.castro100@example.com', '931984548', '2025-02-01 20:29:12');

-- 2/12 CATEGORIA: 6 categorias.
INSERT IGNORE INTO categoria (nombre, descripcion) VALUES
  ('Polos', 'Polos de algodón, deportivos y casuales'),
  ('Camisas', 'Camisas y blusas para uso casual'),
  ('Pantalones', 'Jeans, joggers y pantalones de tela'),
  ('Casacas', 'Casacas urbanas e impermeables'),
  ('Vestidos', 'Vestidos casuales y para ocasiones especiales'),
  ('Zapatillas', 'Calzado casual y deportivo');

-- 3/12 TALLA: 17 tallas para ropa, pantalones y zapatillas.
INSERT IGNORE INTO talla (nombre) VALUES
  ('XS'),
  ('S'),
  ('M'),
  ('L'),
  ('XL'),
  ('XXL'),
  ('28'),
  ('30'),
  ('32'),
  ('34'),
  ('36'),
  ('37'),
  ('38'),
  ('39'),
  ('40'),
  ('41'),
  ('42');

-- 4/12 COLOR: 9 colores.
INSERT IGNORE INTO color (nombre) VALUES
  ('Blanco'),
  ('Negro'),
  ('Azul'),
  ('Rojo'),
  ('Beige'),
  ('Gris'),
  ('Verde'),
  ('Rosado'),
  ('Celeste');

-- Tabla temporal: catalogo de 25 modelos con sus dos tallas y dos colores.
DROP TEMPORARY TABLE IF EXISTS demo_productos_stage;
CREATE TEMPORARY TABLE demo_productos_stage (
  n INT PRIMARY KEY,
  sku VARCHAR(50) NOT NULL,
  categoria VARCHAR(100) NOT NULL,
  nombre VARCHAR(150) NOT NULL,
  descripcion VARCHAR(255) NOT NULL,
  precio DECIMAL(10,2) NOT NULL,
  talla1 VARCHAR(20) NOT NULL,
  talla2 VARCHAR(20) NOT NULL,
  color1 VARCHAR(50) NOT NULL,
  color2 VARCHAR(50) NOT NULL
) ENGINE=InnoDB;
INSERT INTO demo_productos_stage VALUES
  (1, 'DEMO-LIM-PO001', 'Polos', 'Polo básico de algodón', 'Polo unisex de algodón suave para uso diario', '39.90', 'S', 'M', 'Blanco', 'Negro'),
  (2, 'DEMO-LIM-PO002', 'Polos', 'Polo oversize urbano', 'Polo de corte holgado y manga corta', '59.90', 'M', 'L', 'Negro', 'Beige'),
  (3, 'DEMO-LIM-PO003', 'Polos', 'Polo estampado clásico', 'Polo casual con estampado discreto', '44.90', 'S', 'L', 'Blanco', 'Azul'),
  (4, 'DEMO-LIM-PO004', 'Polos', 'Polo deportivo dry-fit', 'Tela ligera de secado rápido para entrenamiento', '49.90', 'M', 'XL', 'Negro', 'Gris'),
  (5, 'DEMO-LIM-PO005', 'Polos', 'Polo manga larga', 'Polo de algodón de manga larga', '54.90', 'S', 'M', 'Verde', 'Beige'),
  (6, 'DEMO-LIM-CA006', 'Camisas', 'Camisa Oxford casual', 'Camisa de algodón con botones para uso diario', '99.90', 'M', 'L', 'Blanco', 'Celeste'),
  (7, 'DEMO-LIM-CA007', 'Camisas', 'Camisa fresca de lino', 'Camisa ligera ideal para clima templado', '109.90', 'S', 'M', 'Blanco', 'Beige'),
  (8, 'DEMO-LIM-CA008', 'Camisas', 'Camisa a cuadros', 'Camisa casual con diseño a cuadros', '89.90', 'M', 'L', 'Azul', 'Rojo'),
  (9, 'DEMO-LIM-CA009', 'Camisas', 'Blusa casual manga corta', 'Blusa ligera de estilo urbano', '79.90', 'S', 'M', 'Rosado', 'Blanco'),
  (10, 'DEMO-LIM-PA010', 'Pantalones', 'Jean slim azul', 'Jean de corte ajustado para hombre', '129.90', '30', '32', 'Azul', 'Negro'),
  (11, 'DEMO-LIM-PA011', 'Pantalones', 'Jean recto clásico', 'Jean de tiro medio y corte recto', '119.90', '28', '30', 'Azul', 'Gris'),
  (12, 'DEMO-LIM-PA012', 'Pantalones', 'Pantalón cargo urbano', 'Pantalón con bolsillos laterales funcionales', '139.90', '30', '32', 'Verde', 'Negro'),
  (13, 'DEMO-LIM-PA013', 'Pantalones', 'Pantalón chino casual', 'Pantalón de tela para vestir informal', '119.90', '32', '34', 'Beige', 'Azul'),
  (14, 'DEMO-LIM-PA014', 'Pantalones', 'Jogger de algodón', 'Pantalón jogger cómodo con cintura elástica', '89.90', 'M', 'L', 'Gris', 'Negro'),
  (15, 'DEMO-LIM-CQ015', 'Casacas', 'Casaca denim clásica', 'Casaca de mezclilla con bolsillos', '179.90', 'M', 'L', 'Azul', 'Negro'),
  (16, 'DEMO-LIM-CQ016', 'Casacas', 'Casaca bomber ligera', 'Casaca urbana con cierre frontal', '199.90', 'S', 'M', 'Negro', 'Verde'),
  (17, 'DEMO-LIM-CQ017', 'Casacas', 'Casaca impermeable', 'Casaca ligera para lluvia y viento', '219.90', 'M', 'L', 'Azul', 'Gris'),
  (18, 'DEMO-LIM-CQ018', 'Casacas', 'Casaca cortaviento', 'Casaca deportiva con capucha', '159.90', 'M', 'XL', 'Negro', 'Rojo'),
  (19, 'DEMO-LIM-VE019', 'Vestidos', 'Vestido floral midi', 'Vestido de largo medio con estampado floral', '129.90', 'S', 'M', 'Rosado', 'Celeste'),
  (20, 'DEMO-LIM-VE020', 'Vestidos', 'Vestido casual de algodón', 'Vestido cómodo de estilo minimalista', '99.90', 'S', 'M', 'Negro', 'Beige'),
  (21, 'DEMO-LIM-VE021', 'Vestidos', 'Vestido de fiesta sencillo', 'Vestido de ocasión con corte elegante', '179.90', 'M', 'L', 'Negro', 'Rojo'),
  (22, 'DEMO-LIM-ZA022', 'Zapatillas', 'Zapatillas urbanas blancas', 'Zapatillas de suela cómoda para uso diario', '189.90', '38', '40', 'Blanco', 'Negro'),
  (23, 'DEMO-LIM-ZA023', 'Zapatillas', 'Zapatillas running', 'Zapatillas ligeras para caminata y deporte', '229.90', '40', '42', 'Negro', 'Azul'),
  (24, 'DEMO-LIM-ZA024', 'Zapatillas', 'Zapatillas de lona unisex', 'Zapatillas clásicas de lona', '119.90', '38', '41', 'Blanco', 'Rojo'),
  (25, 'DEMO-LIM-ZA025', 'Zapatillas', 'Zapatillas casual beige', 'Zapatillas de estilo versátil y urbano', '169.90', '37', '39', 'Beige', 'Rosado');

-- 5/12 PRODUCTO: 25 productos de tienda de ropa, precios en soles.
INSERT IGNORE INTO producto (id_categoria, nombre, descripcion, precio, sku)
SELECT c.id_categoria, s.nombre, s.descripcion, s.precio, s.sku
FROM demo_productos_stage s
JOIN categoria c ON c.nombre = s.categoria;

-- 6/12 VARIANTE_PRODUCTO: 4 variantes por producto = 100.
DROP TEMPORARY TABLE IF EXISTS demo_variantes_stage;
CREATE TEMPORARY TABLE demo_variantes_stage (
  ordinal INT PRIMARY KEY,
  sku VARCHAR(50) NOT NULL,
  talla VARCHAR(20) NOT NULL,
  color VARCHAR(50) NOT NULL,
  stock INT NOT NULL,
  stock_minimo INT NOT NULL
) ENGINE=InnoDB;

INSERT INTO demo_variantes_stage (ordinal, sku, talla, color, stock, stock_minimo)
SELECT (p.n - 1) * 4 + k.pos, p.sku,
       CASE WHEN k.pos <= 2 THEN p.talla1 ELSE p.talla2 END,
       CASE WHEN MOD(k.pos, 2) = 1 THEN p.color1 ELSE p.color2 END,
       12 + MOD(p.n * 11 + k.pos * 7, 40),
       4 + MOD(p.n + k.pos, 3)
FROM demo_productos_stage p
CROSS JOIN (SELECT 1 AS pos UNION ALL SELECT 2 UNION ALL SELECT 3 UNION ALL SELECT 4) k;

INSERT IGNORE INTO variante_producto (id_producto, id_talla, id_color, stock, stock_minimo)
SELECT p.id_producto, t.id_talla, co.id_color, s.stock, s.stock_minimo
FROM demo_variantes_stage s
JOIN producto p ON p.sku = s.sku
JOIN talla t ON t.nombre = s.talla
JOIN color co ON co.nombre = s.color;

-- Se obtienen los IDs reales de cada variante, sin asumir numeracion consecutiva.
DROP TEMPORARY TABLE IF EXISTS demo_mapa_variantes;
CREATE TEMPORARY TABLE demo_mapa_variantes (
  ordinal INT PRIMARY KEY,
  id_variante INT NOT NULL,
  precio DECIMAL(10,2) NOT NULL
) ENGINE=InnoDB;
INSERT INTO demo_mapa_variantes (ordinal, id_variante, precio)
SELECT s.ordinal, vp.id_variante, p.precio
FROM demo_variantes_stage s
JOIN producto p ON p.sku = s.sku
JOIN talla t ON t.nombre = s.talla
JOIN color co ON co.nombre = s.color
JOIN variante_producto vp ON vp.id_producto = p.id_producto
                        AND vp.id_talla = t.id_talla
                        AND vp.id_color = co.id_color;

-- Tabla temporal: relaciona los 100 correos con direcciones, carritos y ventas.
DROP TEMPORARY TABLE IF EXISTS demo_clientes_stage;
CREATE TEMPORARY TABLE demo_clientes_stage (
  n INT PRIMARY KEY,
  email VARCHAR(150) NOT NULL,
  calle VARCHAR(200) NOT NULL,
  distrito VARCHAR(100) NOT NULL,
  fecha_carrito DATETIME NOT NULL,
  fecha_venta DATETIME NOT NULL,
  estado VARCHAR(30) NOT NULL,
  metodo_pago VARCHAR(50) NOT NULL,
  fecha_pago DATETIME NOT NULL
) ENGINE=InnoDB;
INSERT INTO demo_clientes_stage VALUES
  (1, 'alvaro.ramos001@example.com', 'Jr. Los Cedros 384', 'Lima Cercado', '2026-10-12 18:00:00', '2026-10-20 18:00:00', 'preparando', 'Transferencia bancaria', '2026-10-20 19:00:00'),
  (2, 'alonso.gutierrez002@example.com', 'Pasaje Los Pinos 310', 'San Juan de Lurigancho', '2026-10-15 18:00:00', '2026-10-18 18:00:00', 'enviado', 'Yape', '2026-10-19 01:00:00'),
  (3, 'lucia.torres003@example.com', 'Calle Los Álamos 1133', 'Chorrillos', '2026-10-12 18:00:00', '2026-09-03 18:00:00', 'entregado', 'Plin', '2026-09-04 00:00:00'),
  (4, 'luis.huerta004@example.com', 'Jr. La Amistad 735', 'Breña', '2026-10-20 18:00:00', '2026-08-20 18:00:00', 'entregado', 'Tarjeta de débito', '2026-08-20 19:00:00'),
  (5, 'luis.mejia005@example.com', 'Jr. Los Cedros 1490', 'Independencia', '2026-10-13 18:00:00', '2026-09-13 18:00:00', 'entregado', 'Yape', '2026-09-13 20:00:00'),
  (6, 'mateo.rodriguez006@example.com', 'Jr. Los Claveles 835', 'Comas', '2026-10-18 18:00:00', '2026-08-27 18:00:00', 'entregado', 'Tarjeta de débito', '2026-08-27 23:00:00'),
  (7, 'lucia.cabrera007@example.com', 'Jr. La Amistad 1454', 'Lima Cercado', '2026-10-16 18:00:00', '2026-09-05 15:09:09', 'entregado', 'Tarjeta de crédito', '2026-09-05 20:09:09'),
  (8, 'eduardo.huaman008@example.com', 'Calle Los Girasoles 539', 'San Juan de Lurigancho', '2026-10-15 18:00:00', '2026-09-26 18:00:00', 'entregado', 'Yape', '2026-09-26 22:00:00'),
  (9, 'jorge.ayala009@example.com', 'Jr. Los Cedros 156', 'Magdalena del Mar', '2026-10-14 18:00:00', '2026-10-09 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-10-09 20:00:00'),
  (10, 'eduardo.nunez010@example.com', 'Calle Los Sauces 1204', 'San Juan de Lurigancho', '2026-10-20 18:00:00', '2026-10-20 18:00:00', 'pendiente', 'Yape', '2026-10-20 19:00:00'),
  (11, 'axel.tello011@example.com', 'Jr. Los Robles 458', 'Breña', '2026-10-14 18:00:00', '2026-10-18 18:00:00', 'preparando', 'Yape', '2026-10-18 19:00:00'),
  (12, 'kevin.quispe012@example.com', 'Pasaje Los Pinos 1217', 'Ate', '2026-10-12 18:00:00', '2026-10-14 18:00:00', 'enviado', 'Transferencia bancaria', '2026-10-15 01:00:00'),
  (13, 'renato.castro013@example.com', 'Calle Las Gardenias 1540', 'San Martín de Porres', '2026-10-20 18:00:00', '2026-10-19 16:02:06', 'entregado', 'Transferencia bancaria', '2026-10-19 22:02:06'),
  (14, 'daniela.flores014@example.com', 'Calle Los Sauces 579', 'Pueblo Libre', '2026-10-19 18:00:00', '2026-09-09 18:00:00', 'entregado', 'Yape', '2026-09-09 23:00:00'),
  (15, 'andrea.soto015@example.com', 'Pasaje Los Pinos 500', 'Comas', '2026-10-12 18:00:00', '2026-09-25 18:00:00', 'entregado', 'Plin', '2026-09-26 01:00:00'),
  (16, 'paola.quispe016@example.com', 'Av. Los Jardines 1006', 'Lince', '2026-10-12 18:00:00', '2026-08-06 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-08-06 20:00:00'),
  (17, 'karina.poma017@example.com', 'Calle Las Gardenias 1445', 'San Juan de Lurigancho', '2026-10-14 18:00:00', '2026-07-31 18:00:00', 'entregado', 'Tarjeta de débito', '2026-07-31 23:00:00'),
  (18, 'david.garcia018@example.com', 'Av. Los Olivos 1520', 'Santa Anita', '2026-10-20 18:00:00', '2026-09-28 18:00:00', 'entregado', 'Tarjeta de débito', '2026-09-29 01:00:00'),
  (19, 'juan.acosta019@example.com', 'Jr. Los Claveles 585', 'San Martín de Porres', '2026-10-18 18:00:00', '2026-09-10 18:00:00', 'entregado', 'Plin', '2026-09-10 19:00:00'),
  (20, 'karina.garcia020@example.com', 'Calle Los Girasoles 1542', 'Rímac', '2026-10-15 18:00:00', '2026-10-21 18:00:00', 'pendiente', 'Yape', '2026-10-21 22:00:00'),
  (21, 'mariana.torres021@example.com', 'Calle Los Girasoles 1326', 'Pueblo Libre', '2026-10-21 18:00:00', '2026-10-18 18:00:00', 'preparando', 'Plin', '2026-10-18 21:00:00'),
  (22, 'bruno.paredes022@example.com', 'Av. Los Jardines 987', 'Lince', '2026-10-20 18:00:00', '2026-10-15 18:00:00', 'enviado', 'Yape', '2026-10-15 20:00:00'),
  (23, 'alessandra.flores023@example.com', 'Calle Las Violetas 865', 'La Victoria', '2026-10-15 18:00:00', '2026-07-29 18:00:00', 'entregado', 'Yape', '2026-07-29 23:00:00'),
  (24, 'sebastian.aguilar024@example.com', 'Jr. La Amistad 358', 'Independencia', '2026-10-20 18:00:00', '2026-07-27 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-07-28 00:00:00'),
  (25, 'sofia.paredes025@example.com', 'Av. Los Jardines 784', 'Rímac', '2026-10-12 18:00:00', '2026-09-22 18:00:00', 'entregado', 'Tarjeta de débito', '2026-09-23 01:00:00'),
  (26, 'anthony.mendoza026@example.com', 'Jr. Las Flores 838', 'Santa Anita', '2026-10-19 18:00:00', '2026-09-01 18:00:00', 'entregado', 'Yape', '2026-09-01 22:00:00'),
  (27, 'axel.vega027@example.com', 'Jr. Los Claveles 1141', 'San Martín de Porres', '2026-10-19 18:00:00', '2026-09-15 18:00:00', 'entregado', 'Tarjeta de débito', '2026-09-15 23:00:00'),
  (28, 'claudia.nunez028@example.com', 'Calle Los Girasoles 1385', 'Santa Anita', '2026-10-17 18:00:00', '2026-09-20 18:00:00', 'entregado', 'Plin', '2026-09-20 20:00:00'),
  (29, 'mateo.gutierrez029@example.com', 'Calle Los Girasoles 568', 'Rímac', '2026-10-19 18:00:00', '2026-07-25 18:00:00', 'entregado', 'Transferencia bancaria', '2026-07-26 01:00:00'),
  (30, 'cesar.paredes030@example.com', 'Calle Los Sauces 328', 'Lima Cercado', '2026-10-14 18:00:00', '2026-10-21 18:00:00', 'pendiente', 'Yape', '2026-10-22 00:00:00'),
  (31, 'lucia.paredes031@example.com', 'Jr. Los Claveles 862', 'San Martín de Porres', '2026-10-14 18:00:00', '2026-10-18 18:00:00', 'preparando', 'Tarjeta de crédito', '2026-10-19 00:00:00'),
  (32, 'daniela.pena032@example.com', 'Calle Los Girasoles 1454', 'Jesús María', '2026-10-12 18:00:00', '2026-10-14 18:00:00', 'enviado', 'Tarjeta de crédito', '2026-10-14 23:00:00'),
  (33, 'eduardo.pena033@example.com', 'Jr. Los Claveles 1079', 'Ate', '2026-10-13 18:00:00', '2026-08-19 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-08-19 19:00:00'),
  (34, 'diego.espinoza034@example.com', 'Jr. Los Claveles 611', 'Ate', '2026-10-16 18:00:00', '2026-08-08 18:00:00', 'entregado', 'Tarjeta de débito', '2026-08-08 19:00:00'),
  (35, 'alessandra.valdivia035@example.com', 'Pasaje Los Pinos 627', 'Rímac', '2026-10-12 18:00:00', '2026-09-26 18:00:00', 'entregado', 'Transferencia bancaria', '2026-09-26 21:00:00'),
  (36, 'jose.espinoza036@example.com', 'Jr. Las Flores 794', 'Lince', '2026-10-15 18:00:00', '2026-09-20 03:24:07', 'entregado', 'Tarjeta de débito', '2026-09-20 06:24:07'),
  (37, 'natalia.ramirez037@example.com', 'Av. Los Próceres 862', 'Pueblo Libre', '2026-10-15 18:00:00', '2026-08-03 18:00:00', 'entregado', 'Transferencia bancaria', '2026-08-03 20:00:00'),
  (38, 'mateo.torres038@example.com', 'Jr. Los Claveles 242', 'Independencia', '2026-10-18 18:00:00', '2026-09-23 18:00:00', 'entregado', 'Transferencia bancaria', '2026-09-23 23:00:00'),
  (39, 'david.soto039@example.com', 'Calle Los Girasoles 681', 'Comas', '2026-10-15 18:00:00', '2026-09-13 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-09-13 20:00:00'),
  (40, 'miguel.lopez040@example.com', 'Calle Las Violetas 1532', 'Chorrillos', '2026-10-20 18:00:00', '2026-10-21 18:00:00', 'pendiente', 'Plin', '2026-10-21 20:00:00'),
  (41, 'fernando.rojas041@example.com', 'Calle Los Álamos 1114', 'Santiago de Surco', '2026-10-13 18:00:00', '2026-10-20 18:00:00', 'preparando', 'Tarjeta de crédito', '2026-10-20 19:00:00'),
  (42, 'daniela.leon042@example.com', 'Calle Los Sauces 1171', 'Villa El Salvador', '2026-10-19 18:00:00', '2026-10-16 18:00:00', 'enviado', 'Tarjeta de crédito', '2026-10-16 21:00:00'),
  (43, 'victor.paredes043@example.com', 'Jr. Las Flores 920', 'Magdalena del Mar', '2026-10-17 18:00:00', '2026-08-15 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-08-15 22:00:00'),
  (44, 'milagros.poma044@example.com', 'Jr. Las Flores 193', 'San Juan de Lurigancho', '2026-10-14 18:00:00', '2026-09-16 18:00:00', 'entregado', 'Tarjeta de débito', '2026-09-16 23:00:00'),
  (45, 'alejandra.cardenas045@example.com', 'Av. Los Olivos 1178', 'Ate', '2026-10-16 18:00:00', '2026-10-08 18:00:00', 'entregado', 'Yape', '2026-10-08 22:00:00'),
  (46, 'diana.medina046@example.com', 'Calle Los Álamos 1248', 'Chorrillos', '2026-10-12 18:00:00', '2026-10-08 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-10-08 23:00:00'),
  (47, 'axel.vega047@example.com', 'Jr. Las Flores 885', 'Jesús María', '2026-10-15 18:00:00', '2026-08-30 18:00:00', 'entregado', 'Yape', '2026-08-30 20:00:00'),
  (48, 'lucia.cardenas048@example.com', 'Jr. Las Flores 1201', 'Villa El Salvador', '2026-10-21 18:00:00', '2026-09-18 18:00:00', 'entregado', 'Tarjeta de débito', '2026-09-18 22:00:00'),
  (49, 'diego.ayala049@example.com', 'Calle Las Violetas 1547', 'Rímac', '2026-10-12 18:00:00', '2026-07-31 18:00:00', 'entregado', 'Tarjeta de débito', '2026-07-31 21:00:00'),
  (50, 'david.tello050@example.com', 'Av. Las Palmeras 221', 'Villa María del Triunfo', '2026-10-13 18:00:00', '2026-10-20 18:00:00', 'pendiente', 'Tarjeta de crédito', '2026-10-20 21:00:00'),
  (51, 'axel.guerrero051@example.com', 'Calle Los Sauces 1270', 'San Borja', '2026-10-12 18:00:00', '2026-10-18 18:00:00', 'preparando', 'Tarjeta de crédito', '2026-10-18 22:00:00'),
  (52, 'david.sanchez052@example.com', 'Pasaje Los Pinos 379', 'Santa Anita', '2026-10-16 18:00:00', '2026-10-14 18:00:00', 'enviado', 'Plin', '2026-10-14 19:00:00'),
  (53, 'rocio.guerrero053@example.com', 'Jr. Los Robles 860', 'Comas', '2026-10-18 18:00:00', '2026-09-20 18:00:00', 'entregado', 'Yape', '2026-09-20 20:00:00'),
  (54, 'oscar.diaz054@example.com', 'Calle Las Violetas 1237', 'San Miguel', '2026-10-14 18:00:00', '2026-08-11 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-08-12 00:00:00'),
  (55, 'diego.silva055@example.com', 'Calle Las Violetas 914', 'Lince', '2026-10-16 18:00:00', '2026-09-30 18:00:00', 'entregado', 'Yape', '2026-09-30 21:00:00'),
  (56, 'andrea.leon056@example.com', 'Calle Los Sauces 1447', 'Chorrillos', '2026-10-21 18:00:00', '2026-08-17 18:00:00', 'entregado', 'Yape', '2026-08-17 20:00:00'),
  (57, 'sofia.rojas057@example.com', 'Av. Los Olivos 409', 'Lima Cercado', '2026-10-20 18:00:00', '2026-09-10 18:00:00', 'entregado', 'Transferencia bancaria', '2026-09-10 20:00:00'),
  (58, 'katherine.lopez058@example.com', 'Calle Los Girasoles 341', 'Ate', '2026-10-20 18:00:00', '2026-09-16 18:00:00', 'entregado', 'Yape', '2026-09-17 01:00:00'),
  (59, 'milagros.pena059@example.com', 'Calle Los Girasoles 1305', 'Lince', '2026-10-15 18:00:00', '2026-09-29 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-09-30 01:00:00'),
  (60, 'oscar.tapia060@example.com', 'Calle Los Álamos 1026', 'Carabayllo', '2026-10-20 18:00:00', '2026-10-21 18:00:00', 'pendiente', 'Plin', '2026-10-21 23:00:00'),
  (61, 'lucia.diaz061@example.com', 'Jr. La Amistad 1130', 'Lima Cercado', '2026-10-14 18:00:00', '2026-10-19 18:00:00', 'preparando', 'Tarjeta de débito', '2026-10-19 20:00:00'),
  (62, 'diana.aguilar062@example.com', 'Jr. Los Claveles 1477', 'Lince', '2026-10-12 18:00:00', '2026-10-14 18:00:00', 'enviado', 'Tarjeta de crédito', '2026-10-14 20:00:00'),
  (63, 'miguel.ramirez063@example.com', 'Jr. Los Cedros 580', 'Villa El Salvador', '2026-10-16 18:00:00', '2026-10-07 18:00:00', 'entregado', 'Yape', '2026-10-07 21:00:00'),
  (64, 'jose.huaman064@example.com', 'Pasaje Los Pinos 632', 'Villa El Salvador', '2026-10-14 18:00:00', '2026-08-30 18:00:00', 'entregado', 'Tarjeta de débito', '2026-08-30 19:00:00'),
  (65, 'miguel.sanchez065@example.com', 'Calle Los Girasoles 610', 'San Juan de Lurigancho', '2026-10-14 18:00:00', '2026-09-19 18:00:00', 'entregado', 'Plin', '2026-09-20 00:00:00'),
  (66, 'luisa.rojas066@example.com', 'Calle Los Sauces 1486', 'Comas', '2026-10-12 18:00:00', '2026-08-31 18:00:00', 'entregado', 'Tarjeta de débito', '2026-08-31 22:00:00'),
  (67, 'ximena.leon067@example.com', 'Calle Los Sauces 1025', 'Lince', '2026-10-17 18:00:00', '2026-08-08 18:00:00', 'entregado', 'Tarjeta de débito', '2026-08-09 00:00:00'),
  (68, 'juan.mejia068@example.com', 'Calle Las Violetas 682', 'Santa Anita', '2026-10-14 18:00:00', '2026-08-14 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-08-15 01:00:00'),
  (69, 'karina.silva069@example.com', 'Jr. Los Claveles 582', 'San Borja', '2026-10-15 18:00:00', '2026-09-01 18:00:00', 'entregado', 'Plin', '2026-09-01 20:00:00'),
  (70, 'valeria.garcia070@example.com', 'Jr. Los Cedros 609', 'Los Olivos', '2026-10-18 18:00:00', '2026-10-21 18:00:00', 'pendiente', 'Yape', '2026-10-21 21:00:00'),
  (71, 'kevin.cabrera071@example.com', 'Calle Los Sauces 1574', 'Chorrillos', '2026-10-14 18:00:00', '2026-10-19 18:00:00', 'preparando', 'Yape', '2026-10-19 20:00:00'),
  (72, 'jose.lopez072@example.com', 'Calle Las Violetas 491', 'Surquillo', '2026-10-19 18:00:00', '2026-10-17 18:00:00', 'enviado', 'Tarjeta de crédito', '2026-10-17 20:00:00'),
  (73, 'cristian.mendoza073@example.com', 'Jr. Los Robles 931', 'Magdalena del Mar', '2026-10-18 18:00:00', '2026-08-31 18:00:00', 'entregado', 'Yape', '2026-08-31 19:00:00'),
  (74, 'anthony.castro074@example.com', 'Jr. Los Robles 984', 'San Juan de Lurigancho', '2026-10-14 18:00:00', '2026-07-27 18:00:00', 'entregado', 'Yape', '2026-07-28 00:00:00'),
  (75, 'bruno.tapia075@example.com', 'Pasaje Los Pinos 809', 'Rímac', '2026-10-12 18:00:00', '2026-08-16 18:00:00', 'entregado', 'Transferencia bancaria', '2026-08-17 01:00:00'),
  (76, 'camila.castro076@example.com', 'Calle Los Girasoles 1271', 'Rímac', '2026-10-21 18:00:00', '2026-10-10 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-10-10 22:00:00'),
  (77, 'miguel.huaman077@example.com', 'Av. Las Palmeras 1187', 'San Juan de Lurigancho', '2026-10-17 18:00:00', '2026-09-07 18:00:00', 'entregado', 'Tarjeta de débito', '2026-09-07 22:00:00'),
  (78, 'paola.calderon078@example.com', 'Pasaje Los Pinos 650', 'Carabayllo', '2026-10-16 18:00:00', '2026-09-16 04:18:02', 'entregado', 'Yape', '2026-09-16 06:18:02'),
  (79, 'carlos.villanueva079@example.com', 'Calle Las Violetas 1459', 'Villa María del Triunfo', '2026-10-14 18:00:00', '2026-08-25 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-08-25 20:00:00'),
  (80, 'anthony.valdivia080@example.com', 'Jr. Los Robles 134', 'San Miguel', '2026-10-16 18:00:00', '2026-10-20 18:00:00', 'pendiente', 'Yape', '2026-10-20 23:00:00'),
  (81, 'angel.villanueva081@example.com', 'Av. Las Palmeras 530', 'Los Olivos', '2026-10-13 18:00:00', '2026-10-20 18:00:00', 'preparando', 'Plin', '2026-10-20 21:00:00'),
  (82, 'diana.villanueva082@example.com', 'Calle Los Girasoles 1042', 'Lima Cercado', '2026-10-13 18:00:00', '2026-10-17 18:00:00', 'enviado', 'Tarjeta de débito', '2026-10-17 22:00:00'),
  (83, 'andrea.acosta083@example.com', 'Calle Las Gardenias 1405', 'Villa María del Triunfo', '2026-10-16 18:00:00', '2026-08-05 18:00:00', 'entregado', 'Yape', '2026-08-05 20:00:00'),
  (84, 'alejandra.mendoza084@example.com', 'Jr. Los Robles 591', 'Santiago de Surco', '2026-10-15 18:00:00', '2026-08-21 18:00:00', 'entregado', 'Transferencia bancaria', '2026-08-21 21:00:00'),
  (85, 'fernando.paredes085@example.com', 'Av. Las Palmeras 789', 'Magdalena del Mar', '2026-10-18 18:00:00', '2026-09-22 18:00:00', 'entregado', 'Tarjeta de débito', '2026-09-22 22:00:00'),
  (86, 'victor.valdivia086@example.com', 'Jr. Los Robles 993', 'Jesús María', '2026-10-12 18:00:00', '2026-08-25 18:00:00', 'entregado', 'Yape', '2026-08-25 22:00:00'),
  (87, 'sofia.ayala087@example.com', 'Av. Los Olivos 1175', 'Villa El Salvador', '2026-10-19 18:00:00', '2026-09-21 18:00:00', 'entregado', 'Plin', '2026-09-22 01:00:00'),
  (88, 'lucia.acosta088@example.com', 'Av. Los Olivos 549', 'Ate', '2026-10-20 18:00:00', '2026-09-18 18:00:00', 'entregado', 'Plin', '2026-09-18 22:00:00'),
  (89, 'claudia.tapia089@example.com', 'Av. Los Jardines 440', 'Independencia', '2026-10-19 18:00:00', '2026-09-29 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-09-29 20:00:00'),
  (90, 'camila.salazar090@example.com', 'Av. Los Próceres 1238', 'Ate', '2026-10-21 18:00:00', '2026-10-21 18:00:00', 'pendiente', 'Tarjeta de crédito', '2026-10-22 01:00:00'),
  (91, 'sebastian.aguilar091@example.com', 'Av. Los Jardines 943', 'Villa María del Triunfo', '2026-10-16 18:00:00', '2026-10-19 18:00:00', 'preparando', 'Plin', '2026-10-20 00:00:00'),
  (92, 'alonso.torres092@example.com', 'Av. Los Próceres 1315', 'Villa María del Triunfo', '2026-10-13 18:00:00', '2026-10-17 18:00:00', 'enviado', 'Tarjeta de crédito', '2026-10-17 23:00:00'),
  (93, 'oscar.chavez093@example.com', 'Jr. Los Cedros 609', 'Rímac', '2026-10-18 18:00:00', '2026-10-10 18:00:00', 'entregado', 'Yape', '2026-10-10 19:00:00'),
  (94, 'axel.ayala094@example.com', 'Av. Los Jardines 1405', 'Magdalena del Mar', '2026-10-14 18:00:00', '2026-09-18 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-09-18 22:00:00'),
  (95, 'alonso.palomino095@example.com', 'Av. Los Próceres 534', 'Ate', '2026-10-18 18:00:00', '2026-09-19 18:00:00', 'entregado', 'Tarjeta de débito', '2026-09-19 19:00:00'),
  (96, 'alvaro.gutierrez096@example.com', 'Calle Los Sauces 1595', 'San Juan de Lurigancho', '2026-10-15 18:00:00', '2026-08-11 18:00:00', 'entregado', 'Plin', '2026-08-12 01:00:00'),
  (97, 'natalia.cardenas097@example.com', 'Calle Las Violetas 569', 'Independencia', '2026-10-13 18:00:00', '2026-08-18 18:00:00', 'entregado', 'Tarjeta de crédito', '2026-08-18 21:00:00'),
  (98, 'ximena.mendoza098@example.com', 'Jr. Las Flores 1526', 'San Borja', '2026-10-19 18:00:00', '2026-10-03 18:00:00', 'entregado', 'Transferencia bancaria', '2026-10-03 20:00:00'),
  (99, 'diana.acosta099@example.com', 'Jr. Los Robles 922', 'Independencia', '2026-10-15 18:00:00', '2026-09-26 18:00:00', 'entregado', 'Plin', '2026-09-27 01:00:00'),
  (100, 'fernando.castro100@example.com', 'Jr. La Amistad 492', 'Carabayllo', '2026-10-13 18:00:00', '2026-10-21 18:00:00', 'pendiente', 'Tarjeta de débito', '2026-10-22 00:00:00');

-- 7/12 DIRECCION: una direccion ficticia de Lima por cliente (100).
-- codigo_postal NULL: no se inventan codigos reales.
INSERT INTO direccion (id_cliente, calle, ciudad, estado_provincia, codigo_postal, pais)
SELECT c.id_cliente, s.calle, s.distrito, 'Lima', NULL, 'Perú'
FROM demo_clientes_stage s
JOIN cliente c ON c.email = s.email
LEFT JOIN direccion d_exist ON d_exist.id_cliente = c.id_cliente
                           AND d_exist.calle = s.calle
                           AND d_exist.ciudad = s.distrito
WHERE d_exist.id_direccion IS NULL;

-- 8/12 CARRITO: uno por cliente (100).
INSERT INTO carrito (id_cliente, fecha_creacion)
SELECT c.id_cliente, s.fecha_carrito
FROM demo_clientes_stage s
JOIN cliente c ON c.email = s.email
LEFT JOIN carrito ca_exist ON ca_exist.id_cliente = c.id_cliente
                          AND ca_exist.fecha_creacion = s.fecha_carrito
WHERE ca_exist.id_carrito IS NULL;

-- 9/12 DETALLE_CARRITO: dos variantes distintas por carrito (200).
INSERT INTO detalle_carrito (id_carrito, id_variante, cantidad)
SELECT ca.id_carrito, m.id_variante, 1 + MOD(s.n + k.pos, 3)
FROM demo_clientes_stage s
JOIN cliente c ON c.email = s.email
JOIN carrito ca ON ca.id_cliente = c.id_cliente AND ca.fecha_creacion = s.fecha_carrito
CROSS JOIN (SELECT 1 AS pos UNION ALL SELECT 2) k
JOIN demo_mapa_variantes m ON m.ordinal = 1 + MOD(s.n * 17 + k.pos * 29, 100)
LEFT JOIN detalle_carrito dc_exist ON dc_exist.id_carrito = ca.id_carrito
                                 AND dc_exist.id_variante = m.id_variante
WHERE dc_exist.id_detalle_carrito IS NULL;

-- 10/12 VENTA: 100 pedidos con la direccion correspondiente a su cliente.
INSERT INTO venta (id_cliente, id_direccion, fecha_venta, tipo_venta, estado, total)
SELECT c.id_cliente, d.id_direccion, s.fecha_venta, 'online', s.estado, 0.00
FROM demo_clientes_stage s
JOIN cliente c ON c.email = s.email
JOIN direccion d ON d.id_cliente = c.id_cliente AND d.calle = s.calle AND d.ciudad = s.distrito
LEFT JOIN venta v_exist ON v_exist.id_cliente = c.id_cliente
                       AND v_exist.fecha_venta = s.fecha_venta
WHERE v_exist.id_venta IS NULL;

-- 11/12 DETALLE_VENTA: dos variantes por venta (200).
-- Subtotal = cantidad * precio_unitario.
INSERT INTO detalle_venta (id_venta, id_variante, cantidad, precio_unitario, subtotal)
SELECT v.id_venta, m.id_variante,
       1 + MOD(s.n + k.pos, 2),
       m.precio,
       ROUND((1 + MOD(s.n + k.pos, 2)) * m.precio, 2)
FROM demo_clientes_stage s
JOIN cliente c ON c.email = s.email
JOIN venta v ON v.id_cliente = c.id_cliente AND v.fecha_venta = s.fecha_venta
CROSS JOIN (SELECT 1 AS pos UNION ALL SELECT 2) k
JOIN demo_mapa_variantes m ON m.ordinal = 1 + MOD(s.n * 19 + k.pos * 23, 100)
LEFT JOIN detalle_venta dv_exist ON dv_exist.id_venta = v.id_venta
                               AND dv_exist.id_variante = m.id_variante
WHERE dv_exist.id_detalle_venta IS NULL;

-- Corrige los totales de las ventas de PRUEBA usando sus detalles.
-- Workbench suele tener SQL_SAFE_UPDATES=1; lo pausamos solo para este UPDATE
-- y lo devolvemos a su valor original al finalizar la instruccion.
SET @demo_sql_safe_updates_anterior = @@SESSION.sql_safe_updates;
SET SESSION sql_safe_updates = 0;
UPDATE venta v
JOIN cliente c ON c.id_cliente = v.id_cliente
JOIN demo_clientes_stage s ON s.email = c.email AND s.fecha_venta = v.fecha_venta
JOIN (
  SELECT id_venta, ROUND(SUM(subtotal), 2) AS total_calculado
  FROM detalle_venta GROUP BY id_venta
) tot ON tot.id_venta = v.id_venta
SET v.total = tot.total_calculado
WHERE v.id_venta > 0;
SET SESSION sql_safe_updates = @demo_sql_safe_updates_anterior;

-- 12/12 PAGO: 100 intentos con Yape, Plin, tarjetas o transferencia.
-- Si el pedido esta pendiente, el pago queda pendiente; de lo contrario aprobado.
INSERT INTO pago (id_venta, metodo_pago, estado_pago, transaccion_pasarela_id, fecha_pago)
SELECT v.id_venta, s.metodo_pago,
       CASE WHEN s.estado = 'pendiente' THEN 'pendiente' ELSE 'aprobado' END,
       CONCAT('DEMO-LIMA-', LPAD(s.n, 3, '0')), s.fecha_pago
FROM demo_clientes_stage s
JOIN cliente c ON c.email = s.email
JOIN venta v ON v.id_cliente = c.id_cliente AND v.fecha_venta = s.fecha_venta
LEFT JOIN pago pa_exist ON pa_exist.id_venta = v.id_venta
                       AND pa_exist.transaccion_pasarela_id = CONCAT('DEMO-LIMA-', LPAD(s.n, 3, '0'))
WHERE pa_exist.id_pago IS NULL;

-- VALIDACION 1: las 12 tablas con sus cantidades.
SELECT 'cliente' AS tabla, COUNT(*) AS registros FROM cliente
UNION ALL SELECT 'direccion', COUNT(*) FROM direccion
UNION ALL SELECT 'categoria', COUNT(*) FROM categoria
UNION ALL SELECT 'talla', COUNT(*) FROM talla
UNION ALL SELECT 'color', COUNT(*) FROM color
UNION ALL SELECT 'producto', COUNT(*) FROM producto
UNION ALL SELECT 'variante_producto', COUNT(*) FROM variante_producto
UNION ALL SELECT 'carrito', COUNT(*) FROM carrito
UNION ALL SELECT 'detalle_carrito', COUNT(*) FROM detalle_carrito
UNION ALL SELECT 'venta', COUNT(*) FROM venta
UNION ALL SELECT 'detalle_venta', COUNT(*) FROM detalle_venta
UNION ALL SELECT 'pago', COUNT(*) FROM pago
ORDER BY tabla;

-- VALIDACION 2: ventas cuyo total difiera de la suma de subtotales (debe ser 0).
SELECT COUNT(*) AS ventas_con_total_incorrecto
FROM venta v
JOIN cliente c ON c.id_cliente = v.id_cliente
JOIN demo_clientes_stage s ON s.email = c.email AND s.fecha_venta = v.fecha_venta
JOIN (SELECT id_venta, SUM(subtotal) AS total_detalle FROM detalle_venta GROUP BY id_venta) d
     ON d.id_venta = v.id_venta
WHERE ABS(v.total - d.total_detalle) > 0.01;

-- VALIDACION 3: muestra diez ventas con cliente, distrito, total y pago.
SELECT c.nombre, c.apellido, d.ciudad AS distrito,
       v.fecha_venta, v.estado, v.total, pa.metodo_pago, pa.estado_pago
FROM demo_clientes_stage s
JOIN cliente c ON c.email = s.email
JOIN direccion d ON d.id_cliente = c.id_cliente AND d.calle = s.calle AND d.ciudad = s.distrito
JOIN venta v ON v.id_cliente = c.id_cliente AND v.fecha_venta = s.fecha_venta
JOIN pago pa ON pa.id_venta = v.id_venta
            AND pa.transaccion_pasarela_id = CONCAT('DEMO-LIMA-', LPAD(s.n, 3, '0'))
ORDER BY v.fecha_venta DESC
LIMIT 10;

-- Se eliminan UNICAMENTE las tablas temporales auxiliares.
DROP TEMPORARY TABLE IF EXISTS demo_mapa_variantes;
DROP TEMPORARY TABLE IF EXISTS demo_variantes_stage;
DROP TEMPORARY TABLE IF EXISTS demo_productos_stage;
DROP TEMPORARY TABLE IF EXISTS demo_clientes_stage;

