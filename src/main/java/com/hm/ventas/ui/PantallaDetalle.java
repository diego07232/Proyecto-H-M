package com.hm.ventas.ui;

import com.hm.ventas.datos.ItemCatalogo;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

/** Pantalla "2. Detalle de producto". */
public class PantallaDetalle extends Pantalla {

    private ItemCatalogo item;
    private String tallaSel;
    private int cantidad = 1;

    public PantallaDetalle(App app) {
        super(app);
    }

    public void mostrarItem(ItemCatalogo it) {
        this.item = it;
        this.cantidad = 1;
        // Selecciona por defecto la talla M si tiene stock, si no la primera con stock
        tallaSel = null;
        ItemCatalogo.Variante m = it.getVariante("M");
        if (m != null && m.stock > 0) {
            tallaSel = "M";
        } else {
            for (ItemCatalogo.Variante v : ordenadas(it)) {
                if (v.stock > 0) {
                    tallaSel = v.talla;
                    break;
                }
            }
        }
        app.mostrar(App.DETALLE);
    }

    private static List<ItemCatalogo.Variante> ordenadas(ItemCatalogo it) {
        List<ItemCatalogo.Variante> l = new ArrayList<>(it.getVariantes());
        l.sort((a, b) -> PantallaCatalogo.ordenTalla(a.talla) - PantallaCatalogo.ordenTalla(b.talla));
        return l;
    }

    @Override
    public void refrescar() {
        removeAll();
        if (item == null) {
            return;
        }
        botonAtras(() -> app.mostrar(App.CATALOGO));

        Componentes.ImagenPrenda foto = new Componentes.ImagenPrenda(24);
        foto.setPrenda(item);
        if (item.isNuevo()) {
            foto.setEtiqueta("Nuevo");
        }
        poner(foto, 40, 78, 560, 540);

        // Información
        String desc = item.getDescripcion() == null || item.getDescripcion().isBlank()
                ? item.getCategoria().getDescripcion() : item.getDescripcion();
        etiqueta(desc, Tema.sans(Font.PLAIN, 14), Tema.GRIS_TEXTO, 660, 78);
        // Reduce la fuente si el nombre es largo para que entre en 580 px
        float tam = 44;
        JLabel nombre = Componentes.texto(item.getNombre(), Tema.serif(tam), Tema.AZUL_NOCHE);
        while (nombre.getPreferredSize().width > 580 && tam > 26) {
            tam -= 2;
            nombre.setFont(Tema.serif(tam));
        }
        poner(nombre, 660, 100 + (int) ((44 - tam) / 2), 600, nombre.getPreferredSize().height);
        etiqueta(Tema.soles(item.getPrecio()), Tema.sans(Font.BOLD, 28), Tema.AZUL_NOCHE, 660, 176);
        etiqueta("Impuestos incluidos · Envío gratis desde S/ 150", Tema.sans(Font.PLAIN, 13),
                Tema.GRIS_TEXTO, 660, 224);

        // Color: los otros colores del mismo producto
        etiqueta("Color: " + item.getColor(), Tema.sans(Font.BOLD, 14), Tema.AZUL_NOCHE, 660, 250);
        List<ItemCatalogo> hermanos = new ArrayList<>();
        List<String> colores = new ArrayList<>();
        for (ItemCatalogo it : app.getRepo().getItems()) {
            if (it.getIdProducto() == item.getIdProducto() && !colores.contains(it.getColor())) {
                hermanos.add(it);
                colores.add(it.getColor());
            }
        }
        Componentes.SelectorColor sc = new Componentes.SelectorColor(36, 12, 8, false);
        sc.setColores(colores, item.getColor());
        sc.setAlCambiar(c -> {
            for (ItemCatalogo h : hermanos) {
                if (h.getColor().equals(c) && h != item) {
                    mostrarItem(h);
                }
            }
        });
        poner(sc, 657, 274, 500, 44);

        // Talla
        etiqueta("Talla", Tema.sans(Font.BOLD, 14), Tema.AZUL_NOCHE, 660, 334);
        poner(Componentes.enlace("Guía de tallas", Tema.sans(Font.PLAIN, 13), Tema.TURQUESA,
                () -> Dialogos.guiaTallas(app)), 1100, 336, 120, 18);
        List<String> tallas = new ArrayList<>();
        Set<String> sinStock = new HashSet<>();
        for (ItemCatalogo.Variante v : ordenadas(item)) {
            tallas.add(v.talla);
            if (v.stock <= 0) {
                sinStock.add(v.talla);
            }
        }
        Componentes.SelectorTalla st = new Componentes.SelectorTalla(72, 48, 8, 7, false);
        st.setOpciones(tallas, sinStock, tallaSel);
        st.setAlCambiar(t -> {
            tallaSel = t;
            cantidad = 1;
            app.refrescarActual();
        });
        poner(st, 660, 362, 600, st.altoNecesario());

        ItemCatalogo.Variante v = tallaSel == null ? null : item.getVariante(tallaSel);
        int stock = v == null ? 0 : v.stock;
        String txtStock = item.isAgotado() ? "Producto agotado"
                : v == null ? "Elige una talla"
                : "Stock disponible: " + stock + (stock == 1 ? " unidad" : " unidades");
        etiqueta(txtStock, Tema.sans(Font.PLAIN, 14),
                item.isAgotado() ? Tema.ROJO : Tema.GRIS_TEXTO, 660, 428);

        etiqueta("Cantidad", Tema.sans(Font.BOLD, 14), Tema.AZUL_NOCHE, 660, 468);
        Componentes.SelectorCantidad cant = new Componentes.SelectorCantidad();
        cant.setValor(cantidad, Math.max(1, stock));
        cant.setAlCambiar(n -> cantidad = n);
        poner(cant, 660, 496, 160, 48);

        Componentes.BotonPildora agregar = poner(new Componentes.BotonPildora(
                "Agregar al carrito", Componentes.EstiloBoton.PRIMARIO, Tema.sans(Font.BOLD, 16),
                () -> agregar(false)), 660, 576, 580, 60);
        Componentes.BotonPildora comprar = poner(new Componentes.BotonPildora(
                "Comprar ahora", Componentes.EstiloBoton.CONTORNO, Tema.sans(Font.BOLD, 16),
                () -> agregar(true)), 660, 652, 580, 56);
        if (stock <= 0) {
            agregar.setHabilitado(false);
            comprar.setHabilitado(false);
        }

        etiqueta("Envío a Lima en 24–48 h", Tema.sans(Font.PLAIN, 13), Tema.GRIS_TEXTO, 660, 734);
        etiqueta("Cambios y devoluciones en 30 días", Tema.sans(Font.PLAIN, 13),
                Tema.GRIS_TEXTO, 660, 760);
        etiqueta("SKU: " + item.getSku(), Tema.sans(Font.PLAIN, 11), Tema.GRIS_DESACTIVADO,
                660, 786);
        setAlto(800);
    }

    private void agregar(boolean comprarAhora) {
        ItemCatalogo.Variante v = tallaSel == null ? null : item.getVariante(tallaSel);
        if (v == null) {
            JOptionPane.showMessageDialog(app, "Elige una talla.", "Talla",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int enCarrito = 0;
        for (var l : app.getSesion().getCarrito()) {
            if (l.getVariante() == v) {
                enCarrito = l.getCantidad();
            }
        }
        if (enCarrito + cantidad > v.stock) {
            JOptionPane.showMessageDialog(app, "Solo hay " + v.stock + " unidades de la talla "
                    + v.talla + " (ya tienes " + enCarrito + " en el carrito).",
                    "Stock insuficiente", JOptionPane.WARNING_MESSAGE);
            return;
        }
        app.getSesion().agregar(item, v, cantidad);
        app.actualizarCarrito();
        app.avisar("✓ " + item.getNombre() + " (" + item.getColor() + ", " + v.talla
                + ") x" + cantidad + " agregado al carrito");
        if (comprarAhora) {
            app.mostrar(App.CARRITO);
        }
    }
}
