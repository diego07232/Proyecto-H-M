package com.hm.ventas.datos;

import com.hm.ventas.Categoria;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Una tarjeta del catálogo: un producto en un color concreto, con las tallas
 * disponibles (cada talla es una fila de variante_producto).
 */
public class ItemCatalogo {

    /** Variante = combinación producto + talla + color con su stock. */
    public static class Variante {
        public final int idVariante;
        public final String talla;
        public int stock;

        public Variante(int idVariante, String talla, int stock) {
            this.idVariante = idVariante;
            this.talla = talla;
            this.stock = stock;
        }
    }

    private final int idProducto;
    private final String nombre;
    private final String descripcion;
    private final double precio;
    private final String sku;
    private final Categoria categoria;
    private final String color;
    private final Map<String, Variante> variantes = new LinkedHashMap<>();
    private boolean nuevo;
    private java.awt.image.BufferedImage imagen;

    public java.awt.image.BufferedImage getImagen() { return imagen; }
    void setImagen(java.awt.image.BufferedImage imagen) { this.imagen = imagen; }

    public ItemCatalogo(int idProducto, String nombre, String descripcion,
                        double precio, String sku, Categoria categoria, String color) {
        this.idProducto = idProducto;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.sku = sku;
        this.categoria = categoria;
        this.color = color;
    }

    public void agregarVariante(int idVariante, String talla, int stock) {
        variantes.put(talla, new Variante(idVariante, talla, stock));
    }

    public int getIdProducto() { return idProducto; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public double getPrecio() { return precio; }
    public String getSku() { return sku; }
    public Categoria getCategoria() { return categoria; }
    public String getColor() { return color; }
    public boolean isNuevo() { return nuevo; }
    public void setNuevo(boolean nuevo) { this.nuevo = nuevo; }

    public List<Variante> getVariantes() {
        return new ArrayList<>(variantes.values());
    }

    public Variante getVariante(String talla) {
        return variantes.get(talla);
    }

    public List<String> getTallas() {
        return new ArrayList<>(variantes.keySet());
    }

    public int getStockTotal() {
        int total = 0;
        for (Variante v : variantes.values()) {
            total += v.stock;
        }
        return total;
    }

    public boolean isAgotado() {
        return getStockTotal() <= 0;
    }
}
