/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */

public class Producto {

    private int idProducto;
    private String nombre;
    private String talla;
    private String color;
    private double precio;
    private int stock;
    private Categoria categoria;
    
    

    public Producto(int idProducto, String nombre,
                    String talla, String color,
                    double precio, int stock,
                    Categoria categoria) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.talla = talla;
        this.color = color;
        this.precio = precio;
        this.stock = stock;
        this.categoria = categoria;
    }

    public int getIdProducto() {
        return idProducto;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTalla() {
        return talla;
    }

    public void setTalla(String talla) {
        this.talla = talla;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        if (precio >= 0) {
            this.precio = precio;
        }
    }

    public int getStock() {
        return stock;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public boolean reducirStock(int cantidad) {
        if (cantidad <= 0 || cantidad > stock) {
            return false;
        }

        stock -= cantidad;
        return true;
    }

    public void aumentarStock(int cantidad) {
        if (cantidad > 0) {
            stock += cantidad;
        }
    }

    public void mostrarProducto() {
        System.out.println("Producto: " + nombre);
        System.out.println("Talla: " + talla);
        System.out.println("Color: " + color);
        System.out.println("Precio: S/ " + precio);
        System.out.println("Stock: " + stock);
    }
    
    private static int contadorProductos = 0;
    
    public static int getContadorProductos() {
    return contadorProductos;
    }
}
