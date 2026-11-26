package com.hm.ventas.ui;

import com.hm.ventas.datos.LineaCarrito;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

/** Pantalla "3. Carrito". */
public class PantallaCarrito extends Pantalla {

    public PantallaCarrito(App app) {
        super(app);
    }

    @Override
    public void refrescar() {
        removeAll();
        Sesion s = app.getSesion();
        botonAtras(app::volver);
        etiqueta("Tu carrito", Tema.serif(44), Tema.AZUL_NOCHE, 40, 64);

        List<LineaCarrito> lineas = new ArrayList<>(s.getCarrito());
        if (lineas.isEmpty()) {
            etiqueta("Tu carrito está vacío.", Tema.sans(Font.PLAIN, 17), Tema.GRIS_TEXTO, 40, 160);
            poner(new Componentes.BotonPildora("Ver catálogo", Componentes.EstiloBoton.PRIMARIO,
                    Tema.sans(Font.BOLD, 15), () -> app.mostrar(App.CATALOGO)), 40, 200, 220, 52);
        }

        int y = 148;
        for (LineaCarrito l : lineas) {
            Componentes.ImagenPrenda img = new Componentes.ImagenPrenda(14);
            img.setPrenda(l.getItem());
            poner(img, 40, y, 120, 140);
            etiqueta(l.getItem().getNombre(), Tema.sans(Font.BOLD, 17), Tema.AZUL_NOCHE, 184, y + 6);
            etiqueta("Color: " + l.getItem().getColor() + " · Talla: " + l.getVariante().talla,
                    Tema.sans(Font.PLAIN, 14), Tema.GRIS_TEXTO, 184, y + 36);
            Componentes.SelectorCantidad c = new Componentes.SelectorCantidad();
            c.setValor(l.getCantidad(), l.getVariante().stock);
            c.setAlCambiar(n -> {
                l.setCantidad(n);
                app.refrescarActual();
            });
            poner(c, 184, y + 80, 160, 48);
            etiqueta(Tema.soles(l.getSubtotal()), Tema.sans(Font.BOLD, 17), Tema.AZUL_NOCHE,
                    640, y + 6);
            etiqueta(Tema.soles(l.getItem().getPrecio()) + " c/u", Tema.sans(Font.PLAIN, 13),
                    Tema.GRIS_TEXTO, 640, y + 36);
            poner(Componentes.enlace("Quitar", Tema.sans(Font.PLAIN, 14), Tema.TURQUESA, () -> {
                s.quitar(l);
                app.avisar("Producto quitado del carrito");
                app.refrescarActual();
            }), 640, y + 84, 60, 20);
            separador(40, y + 158, 760);
            y += 190;
        }

        // Resumen del pedido
        Componentes.PanelRedondeado res =
                new Componentes.PanelRedondeado(Tema.BLANCO, Tema.BORDE.darker(), 20);
        poner(res, 860, 148, 380, 330);
        agregarA(res, "Resumen del pedido", Tema.sans(Font.BOLD, 18), 24, 24, false, 0);
        agregarA(res, "Subtotal", Tema.sans(Font.PLAIN, 15), 24, 70, false, 0);
        agregarA(res, Tema.soles(s.getSubtotal()), Tema.sans(Font.PLAIN, 15), 24, 70, true, 332);
        double envio = s.getEnvio();
        agregarA(res, "Envío a Lima (gratis desde S/ 150)", Tema.sans(Font.PLAIN, 14),
                24, 104, false, 0);
        agregarA(res, s.getCarrito().isEmpty() ? "—" : envio == 0 ? "Gratis" : Tema.soles(envio),
                Tema.sans(Font.PLAIN, 15), 24, 104, true, 332);
        javax.swing.JPanel sep = new javax.swing.JPanel();
        sep.setBackground(Tema.BORDE);
        sep.setBounds(24, 146, 332, 1);
        res.add(sep);
        agregarA(res, "Total", Tema.sans(Font.BOLD, 20), 24, 166, false, 0);
        agregarA(res, Tema.soles(s.getTotal()), Tema.sans(Font.BOLD, 20), 24, 166, true, 332);
        if (!s.getCarrito().isEmpty() && envio > 0) {
            agregarA(res, "Te faltan " + Tema.soles(Tema.ENVIO_GRATIS_DESDE - s.getSubtotal())
                    + " para el envío gratis", Tema.sans(Font.PLAIN, 12), 24, 204, false, 0)
                    .setForeground(Tema.TURQUESA);
        }
        Componentes.BotonPildora pagar = new Componentes.BotonPildora("Ir a pagar",
                Componentes.EstiloBoton.PRIMARIO, Tema.sans(Font.BOLD, 16),
                () -> app.mostrar(App.CHECKOUT));
        pagar.setBounds(24, 236, 332, 56);
        pagar.setHabilitado(!s.getCarrito().isEmpty());
        res.add(pagar);

        poner(Componentes.enlace("Seguir comprando", Tema.sans(Font.PLAIN, 15), Tema.AZUL_NOCHE,
                () -> app.mostrar(App.CATALOGO)), 890, 518, 160, 20);

        setAlto(Math.max(600, y + 40));
    }

    /** Agrega un texto al panel; si alinearDerecha, lo alinea al borde derecho "ancho". */
    private javax.swing.JLabel agregarA(javax.swing.JPanel p, String t, Font f, int x, int y,
                                       boolean alinearDerecha, int ancho) {
        javax.swing.JLabel l = Componentes.texto(t, f, Tema.AZUL_NOCHE);
        int w = l.getPreferredSize().width + 2, h = l.getPreferredSize().height;
        if (alinearDerecha) {
            l.setBounds(x + ancho - w, y, w, h);
        } else {
            l.setBounds(x, y, w, h);
        }
        p.add(l);
        return l;
    }
}
