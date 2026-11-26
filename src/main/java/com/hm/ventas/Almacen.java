/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */

public class Almacen implements GestionInventario {

    private int idAlmacen;
    private String nombre;
    private String ubicacion;
    private int stock;

    public Almacen(int idAlmacen, String nombre,
                   String ubicacion) {
        this.idAlmacen = idAlmacen;
        this.nombre = nombre;
        this.ubicacion = ubicacion;
        this.stock = 0;
    }

    public int getIdAlmacen() {
        return idAlmacen;
    }

    public String getNombre() {
        return nombre;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void registrarEntrada(Producto producto,
                                 int cantidad) {
        if (producto != null && cantidad > 0) {
            producto.aumentarStock(cantidad);
        }
    }

    public boolean registrarSalida(Producto producto,
                                   int cantidad) {
        if (producto == null) {
            return false;
        }

        return producto.reducirStock(cantidad);
    }

    public void mostrarAlmacen() {
        System.out.println("Almacén: " + nombre);
        System.out.println("Ubicación: " + ubicacion);
    }
    
    @Override
    public void registrarEntrada(int cantidad) {
    if (cantidad > 0) {
        stock += cantidad;
        System.out.println("Entrada registrada: " + cantidad);
    } else {
        System.out.println("La cantidad debe ser positiva.");
    }
    }

    @Override
    public boolean registrarSalida(int cantidad) {
    if (cantidad > 0 && cantidad <= stock) {
        stock -= cantidad;
        System.out.println("Salida registrada: " + cantidad);
        return true;
    }

    System.out.println("Stock insuficiente o cantidad inválida.");
    return false;
}
}
