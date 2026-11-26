package com.hm.ventas.datos;

import com.hm.ventas.Categoria;
import java.util.List;

/**
 * Datos del prototipo de Figma. Se usan sólo cuando no hay conexión con MySQL,
 * para que la aplicación funcione igualmente en modo demostración.
 */
final class DatosDemo {

    private DatosDemo() {
    }

    private static int idVariante = 1000;

    static void cargar(List<Categoria> categorias, List<ItemCatalogo> items) {
        Categoria polos = new Categoria(1, "Polos", "Polos de algodón");
        Categoria pantalones = new Categoria(2, "Pantalones", "Jeans, joggers y pantalones de tela");
        Categoria casacas = new Categoria(3, "Casacas", "Casacas urbanas e impermeables");
        Categoria accesorios = new Categoria(4, "Accesorios", "Gorras, bolsos y más");
        categorias.add(polos);
        categorias.add(pantalones);
        categorias.add(casacas);
        categorias.add(accesorios);

        // Las 8 tarjetas del prototipo "1. Catálogo"
        items.add(item(1, "Polo básico", 49.90, polos, "Negro", false, 20));
        items.add(item(2, "Polo oversize", 59.90, polos, "Blanco", true, 12));
        items.add(item(3, "Polo rayas marinas", 44.90, polos, "Azul marino", false, 8));
        items.add(item(4, "Polo cuello V", 39.90, polos, "Celeste", false, 0));
        items.add(item(5, "Polo estampado", 54.90, polos, "Turquesa", true, 15));
        items.add(item(6, "Polo premium algodón", 79.90, polos, "Negro", false, 5));
        items.add(item(7, "Polo manga larga", 69.90, polos, "Gris", false, 9));
        items.add(item(1, "Polo básico", 49.90, polos, "Blanco", false, 30));

        items.add(item(10, "Jean slim", 89.90, pantalones, "Azul marino", false, 14));
        items.add(item(11, "Jogger de algodón", 69.90, pantalones, "Gris", true, 10));
        items.add(item(12, "Casaca denim", 129.90, casacas, "Azul marino", false, 6));
        items.add(item(13, "Casaca cortaviento", 99.90, casacas, "Negro", true, 7));
        items.add(item(14, "Gorra clásica", 34.90, accesorios, "Negro", false, 25));
        items.add(item(15, "Bolso de lona", 59.90, accesorios, "Blanco", false, 11));
    }

    /** Reparte el stock total entre las tallas S, M, L y XL (XS agotada). */
    private static ItemCatalogo item(int id, String nombre, double precio, Categoria cat,
                                     String color, boolean nuevo, int stockTotal) {
        ItemCatalogo it = new ItemCatalogo(id, nombre, "", precio,
                "DEMO-" + id + "-" + color, cat, color);
        it.setNuevo(nuevo);
        String[] tallas = {"XS", "S", "M", "L", "XL"};
        int[] reparto = new int[5];
        if (stockTotal > 0) {
            // La talla M concentra el stock (como en el prototipo: M = 20 u.)
            reparto[2] = stockTotal;
            reparto[1] = Math.max(0, stockTotal / 2);
            reparto[3] = Math.max(0, stockTotal / 3);
            reparto[4] = Math.max(0, stockTotal / 4);
        }
        for (int i = 0; i < tallas.length; i++) {
            it.agregarVariante(idVariante++, tallas[i], reparto[i]);
        }
        return it;
    }
}
