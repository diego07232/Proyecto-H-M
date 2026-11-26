/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */
public class Main {

    public static void main(String[] args) {

        // Crear un cliente
        cliente cliente = new cliente(
                1,
                "Carlos Pérez",
                "carlos@gmail.com",
                "clave123",
                "987654321",
                "Lima, Perú"
        );

        // Crear un administrador
        administrador administrador = new administrador(
                2,
                "Ana Torres",
                "ana@empresa.com",
                "admin123",
                "Administrador de ventas"
        );

        // Crear una categoría
        Categoria categoria = new Categoria(
                1, "Polos", "Polos de algodón"
        );

        // Crear un producto
        Producto producto = new Producto(
                1, "Polo básico",
                "M", "Negro",
                49.90, 20, categoria
        );

        // Crear un pedido y agregar un detalle
        Pedido pedido = new Pedido(
                1, cliente, "22/10/2026"
        );

        DetallePedido detalle = new DetallePedido(
                1, producto, 2
        );

        pedido.agregarDetalle(detalle);

        // Registrar el pago
        Pago pago = new Pago(
                1, pedido, "Tarjeta",
                pedido.calcularTotal()
        );

        pago.confirmarPago();

        // Crear el almacén
        Almacen almacen = new Almacen(
                1, "Almacén principal", "Lima"
        );

        // Crear el repartidor
        Repartidor repartidor = new Repartidor(
                1, "Luis Ramírez", "999888777"
        );

        // Crear el envío
        Envio envio = new Envio(
                1, pedido, cliente.getDireccion()
        );

        envio.asignarRepartidor(repartidor);
        
        

        // Mostrar resultados
        System.out.println("=== DATOS DEL CLIENTE ===");
        cliente.mostrarDatos();

        System.out.println("\n=== PRODUCTO ===");
        producto.mostrarProducto();

        System.out.println("\n=== PEDIDO ===");
        pedido.mostrarPedido();

        System.out.println("\n=== PAGO ===");
        pago.mostrarPago();

        System.out.println("\n=== ALMACÉN ===");
        almacen.mostrarAlmacen();

        System.out.println("\n=== ENVÍO ===");
        envio.mostrarEnvio();

        System.out.println("\n=== ADMINISTRADOR ===");
        administrador.mostrarDatos();
        administrador.gestionarProductos();
        Factura factura = new Factura(
        1,
        "22/10/2026",
        99.80,
        "20123456789",
        "Empresa Comercial S.A.C."
        );

        Boleta boleta = new Boleta(
        2,
        "22/10/2026",
        49.90,
        "12345678",
        "Carlos Pérez"
        );

        System.out.println("=== FACTURA ===");
        factura.emitir();

        System.out.println("\n=== BOLETA ===");
        boleta.emitir();

    }
}
