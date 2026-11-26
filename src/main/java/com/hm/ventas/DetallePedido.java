/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */

public class DetallePedido  {

    private int idDetalle;
    private Producto producto;
    private int cantidad;
    private double precioUnitario;

    public DetallePedido(int idDetalle, Producto producto,
                         int cantidad) {
        if (producto == null || cantidad <= 0
                || producto.getStock() < cantidad) {
            throw new IllegalArgumentException(
                    "Producto o cantidad no válidos.");
        }

        this.idDetalle = idDetalle;
        this.producto = producto;
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio();
    }

    public int getIdDetalle() {
        return idDetalle;
    }

    public Producto getProducto() {
        return producto;
    }

    public int getCantidad() {
        return cantidad;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public double calcularSubtotal() {
        return cantidad * precioUnitario;
    }

    public void mostrarDetalle() {
        System.out.println("Producto: " + producto.getNombre());
        System.out.println("Cantidad: " + cantidad);
        System.out.println("Subtotal: S/ " + calcularSubtotal());
    }
}
