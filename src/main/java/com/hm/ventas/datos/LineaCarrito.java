package com.hm.ventas.datos;

import com.hm.ventas.Producto;

/** Una línea del carrito: una variante (producto+color+talla) y su cantidad. */
public class LineaCarrito {

    private final ItemCatalogo item;
    private final ItemCatalogo.Variante variante;
    private int cantidad;

    public LineaCarrito(ItemCatalogo item, ItemCatalogo.Variante variante, int cantidad) {
        this.item = item;
        this.variante = variante;
        this.cantidad = cantidad;
    }

    public ItemCatalogo getItem() { return item; }
    public ItemCatalogo.Variante getVariante() { return variante; }
    public int getCantidad() { return cantidad; }

    public void setCantidad(int cantidad) {
        this.cantidad = Math.max(1, Math.min(cantidad, Math.max(1, variante.stock)));
    }

    public double getSubtotal() {
        return item.getPrecio() * cantidad;
    }

    /** Convierte la línea al modelo de dominio del proyecto (clase Producto). */
    public Producto aProducto() {
        return new Producto(variante.idVariante, item.getNombre(), variante.talla,
                item.getColor(), item.getPrecio(), variante.stock, item.getCategoria());
    }
}
