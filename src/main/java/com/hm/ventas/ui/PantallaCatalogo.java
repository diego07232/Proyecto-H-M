package com.hm.ventas.ui;

import com.hm.ventas.Categoria;
import com.hm.ventas.datos.ItemCatalogo;
import java.awt.BasicStroke;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.IntConsumer;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Pantalla "1. Catálogo" del prototipo: filtros a la izquierda y grilla de productos. */
public class PantallaCatalogo extends Pantalla {

    private Categoria categoria;      // null = todas
    private String talla;             // null = todas
    private String color;             // null = todos
    private Double precioMax;         // null = sin límite
    private String busqueda = "";
    private String seccion;           // Mujer / Hombre / Niños / Ofertas
    private boolean iniciado;

    private final List<Component> grilla = new ArrayList<>();
    private JLabel titulo;
    private JLabel subtitulo;
    private JLabel rangoPrecio;
    private int altoFiltros;

    public PantallaCatalogo(App app) {
        super(app);
    }

    // ------------------------------------------------------------------
    // Navegación desde la barra superior
    // ------------------------------------------------------------------

    public void irA(Categoria c, String sec) {
        categoria = c;
        seccion = sec;
        talla = null;
        color = null;
        precioMax = null;
        busqueda = "";
        app.limpiarBuscador();
        app.mostrar(App.CATALOGO);
    }

    public void irASeccion(String nombre) {
        irA(null, nombre);
    }

    public void buscar(String texto) {
        busqueda = texto == null ? "" : texto.trim();
        categoria = null;
        seccion = null;
        talla = null;
        color = null;
        precioMax = null;
        app.mostrar(App.CATALOGO);
    }

    // ------------------------------------------------------------------
    // Construcción
    // ------------------------------------------------------------------

    @Override
    public void refrescar() {
        if (!iniciado) {
            iniciado = true;
            // Como en el prototipo: arranca en la categoría Polos
            for (Categoria c : app.getRepo().getCategorias()) {
                if (c.getNombre().equalsIgnoreCase("Polos")) {
                    categoria = c;
                }
            }
        }
        removeAll();
        grilla.clear();
        construirFiltros();

        titulo = etiqueta(" ", Tema.serif(44), Tema.AZUL_NOCHE, 320, 26);
        subtitulo = etiqueta(" ", Tema.sans(Font.PLAIN, 14), Tema.GRIS_TEXTO, 320, 86);
        construirGrilla();
    }

    private List<ItemCatalogo> itemsDeCategoria() {
        List<ItemCatalogo> lista = new ArrayList<>();
        for (ItemCatalogo it : app.getRepo().getItems()) {
            if (categoria == null
                    || it.getCategoria().getIdCategoria() == categoria.getIdCategoria()) {
                lista.add(it);
            }
        }
        return lista;
    }

    private void construirFiltros() {
        int y = 38;
        etiqueta("Categoría", Tema.sans(Font.BOLD, 14), Tema.AZUL_NOCHE, 40, y);
        y += 26;
        for (Categoria c : app.getRepo().getCategorias()) {
            boolean sel = categoria != null && categoria.getIdCategoria() == c.getIdCategoria();
            if (sel) {
                JPanel barra = new JPanel();
                barra.setBackground(Tema.TURQUESA);
                poner(barra, 40, y + 1, 3, 18);
            }
            JLabel l = Componentes.enlace(c.getNombre(),
                    Tema.sans(sel ? Font.BOLD : Font.PLAIN, 14),
                    sel ? Tema.AZUL_NOCHE : Tema.GRIS_TEXTO, () -> {
                        categoria = sel ? null : c;
                        seccion = null;
                        busqueda = "";
                        talla = null;
                        color = null;
                        precioMax = null;
                        app.limpiarBuscador();
                        app.refrescarActual();
                    });
            poner(l, 54, y, 200, 20);
            y += 27;
        }

        // Tallas presentes en la categoría
        List<ItemCatalogo> base = itemsDeCategoria();
        Set<String> tallas = new LinkedHashSet<>();
        Set<String> colores = new LinkedHashSet<>();
        double min = Double.MAX_VALUE, max = 0;
        for (ItemCatalogo it : base) {
            tallas.addAll(it.getTallas());
            colores.add(it.getColor());
            min = Math.min(min, it.getPrecio());
            max = Math.max(max, it.getPrecio());
        }
        List<String> tallasOrdenadas = new ArrayList<>(tallas);
        tallasOrdenadas.sort(Comparator.comparingInt(PantallaCatalogo::ordenTalla));

        y += 16;
        etiqueta("Talla", Tema.sans(Font.BOLD, 14), Tema.AZUL_NOCHE, 40, y);
        y += 28;
        Componentes.SelectorTalla st = new Componentes.SelectorTalla(40, 34, 7, 5, true);
        st.setOpciones(tallasOrdenadas, Set.of(), talla);
        int altoTallas = st.altoNecesario();
        poner(st, 40, y, 240, altoTallas);
        st.setAlCambiar(t -> {
            talla = t;
            construirGrilla();
        });
        y += altoTallas + 20;

        JLabel lblColor = etiqueta("Color: " + (color == null ? "Todos" : color),
                Tema.sans(Font.BOLD, 14), Tema.AZUL_NOCHE, 40, y);
        y += 26;
        Componentes.SelectorColor sc = new Componentes.SelectorColor(36, 8, 5, true);
        sc.setColores(new ArrayList<>(colores), color);
        int altoColores = sc.altoNecesario();
        poner(sc, 37, y, 250, altoColores);
        sc.setAlCambiar(c -> {
            color = c;
            lblColor.setText("Color: " + (c == null ? "Todos" : c));
            lblColor.setSize(lblColor.getPreferredSize().width + 4, lblColor.getHeight());
            construirGrilla();
        });
        y += altoColores + 32;

        if (base.isEmpty()) {
            min = 0;
            max = 0;
        }
        final double pMin = Math.floor(min / 10) * 10;
        final double pMax = Math.ceil(max / 10) * 10;
        etiqueta("Precio", Tema.sans(Font.BOLD, 14), Tema.AZUL_NOCHE, 40, y);
        y += 26;
        double valor = precioMax == null ? pMax : precioMax;
        rangoPrecio = etiqueta(rango(pMin, valor), Tema.sans(Font.PLAIN, 13),
                Tema.GRIS_TEXTO, 40, y);
        rangoPrecio.setSize(200, rangoPrecio.getHeight());
        y += 30;
        SliderPrecio slider = new SliderPrecio(pMin, pMax, valor, v -> {
            precioMax = (double) v;
            rangoPrecio.setText(rango(pMin, v));
            construirGrilla();
        });
        poner(slider, 36, y, 230, 24);
        y += 46;

        poner(Componentes.enlace("Limpiar filtros", Tema.sans(Font.PLAIN, 13), Tema.TURQUESA,
                () -> {
                    talla = null;
                    color = null;
                    precioMax = null;
                    app.refrescarActual();
                }), 40, y, 120, 20);
        altoFiltros = y + 40;
    }

    private static String rango(double min, double max) {
        return "S/ " + (int) min + " – S/ " + (int) max;
    }

    static int ordenTalla(String t) {
        String[] orden = {"XS", "S", "M", "L", "XL", "XXL"};
        for (int i = 0; i < orden.length; i++) {
            if (orden[i].equalsIgnoreCase(t)) {
                return i;
            }
        }
        try {
            return 100 + Integer.parseInt(t);
        } catch (NumberFormatException e) {
            return 1000;
        }
    }

    private List<ItemCatalogo> filtrar() {
        List<ItemCatalogo> res = new ArrayList<>();
        String q = busqueda.toLowerCase(Locale.ROOT);
        for (ItemCatalogo it : itemsDeCategoria()) {
            if (talla != null && it.getVariante(talla) == null) {
                continue;
            }
            if (color != null && !color.equals(it.getColor())) {
                continue;
            }
            if (precioMax != null && it.getPrecio() > precioMax + 0.001) {
                continue;
            }
            if (!q.isEmpty()) {
                String texto = (it.getNombre() + " " + it.getColor() + " "
                        + it.getCategoria().getNombre()).toLowerCase(Locale.ROOT);
                if (!texto.contains(q)) {
                    continue;
                }
            }
            if ("Ofertas".equals(seccion) && it.getPrecio() > 60) {
                continue;
            }
            res.add(it);
        }
        if ("Ofertas".equals(seccion)) {
            res.sort(Comparator.comparingDouble(ItemCatalogo::getPrecio));
        }
        return res;
    }

    private void construirGrilla() {
        for (Component c : grilla) {
            remove(c);
        }
        grilla.clear();

        List<ItemCatalogo> lista = filtrar();
        String t;
        if (!busqueda.isEmpty()) {
            t = "Resultados para “" + busqueda + "”";
        } else if (seccion != null) {
            t = "Ofertas".equals(seccion) ? "Ofertas: hasta S/ 60" : seccion;
        } else if (categoria == null) {
            t = "Toda la colección";
        } else if (categoria.getNombre().equalsIgnoreCase("Polos")) {
            t = "Polos y camisetas";
        } else {
            t = categoria.getNombre();
        }
        titulo.setText(t);
        titulo.setSize(900, titulo.getPreferredSize().height);
        subtitulo.setText(lista.size() + (lista.size() == 1 ? " producto" : " productos")
                + " · Envío a Lima");
        subtitulo.setSize(500, subtitulo.getPreferredSize().height);

        for (int i = 0; i < lista.size(); i++) {
            int col = i % 4, fila = i / 4;
            JComponent card = tarjeta(lista.get(i));
            poner(card, 320 + col * 240, 128 + fila * 370, 200, 330);
            grilla.add(card);
        }
        if (lista.isEmpty()) {
            JLabel vacio = etiqueta("No encontramos prendas con esos filtros.",
                    Tema.sans(Font.PLAIN, 16), Tema.GRIS_TEXTO, 320, 140);
            grilla.add(vacio);
        }
        int filas = (lista.size() + 3) / 4;
        setAlto(Math.max(altoFiltros, 128 + filas * 370) + 20);
        revalidate();
        repaint();
    }

    private JComponent tarjeta(ItemCatalogo it) {
        Componentes.PanelRedondeado card =
                new Componentes.PanelRedondeado(Tema.BLANCO, Tema.BORDE, 16);
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Componentes.ImagenPrenda img = new Componentes.ImagenPrenda(16);
        img.setPrenda(it);
        if (it.isNuevo()) {
            img.setEtiqueta("Nuevo");
        }
        img.setBounds(0, 0, 200, 220);
        card.add(img);

        JLabel nombre = Componentes.texto(it.getNombre(), Tema.sans(Font.BOLD, 15), Tema.AZUL_NOCHE);
        nombre.setBounds(2, 232, 196, 22);
        card.add(nombre);
        JLabel col = Componentes.texto(it.getColor(), Tema.sans(Font.PLAIN, 12), Tema.GRIS_TEXTO);
        col.setBounds(2, 260, 196, 18);
        card.add(col);
        JLabel precio = Componentes.texto(Tema.soles(it.getPrecio()), Tema.sans(Font.BOLD, 18),
                Tema.AZUL_NOCHE);
        precio.setBounds(2, 281, 196, 24);
        card.add(precio);
        JLabel stock = it.isAgotado()
                ? Componentes.texto("Agotado", Tema.sans(Font.PLAIN, 12), Tema.ROJO)
                : Componentes.texto("Stock: " + it.getStockTotal() + " u.",
                        Tema.sans(Font.PLAIN, 12), Tema.GRIS_TEXTO);
        stock.setBounds(2, 306, 196, 18);
        card.add(stock);

        MouseAdapter clic = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                app.detalle.mostrarItem(it);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                card.setColores(Tema.BLANCO, Tema.TURQUESA, 1.5f);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                card.setColores(Tema.BLANCO, Tema.BORDE, 1f);
            }
        };
        card.addMouseListener(clic);
        img.addMouseListener(clic);
        card.setToolTipText(it.getNombre() + " · " + it.getColor());
        return card;
    }

    // ------------------------------------------------------------------
    // Barra de precio (rango) del prototipo
    // ------------------------------------------------------------------

    private static class SliderPrecio extends JComponent {
        private final double min, max;
        private double valor;
        private final IntConsumer alSoltar;

        SliderPrecio(double min, double max, double valor, IntConsumer alSoltar) {
            this.min = min;
            this.max = max;
            this.valor = valor;
            this.alSoltar = alSoltar;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            MouseAdapter m = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    mover(e.getX());
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    mover(e.getX());
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    alSoltar.accept((int) Math.round(SliderPrecio.this.valor));
                }
            };
            addMouseListener(m);
            addMouseMotionListener(m);
        }

        private void mover(int x) {
            int ancho = getWidth() - 20;
            double f = Math.max(0, Math.min(1, (x - 10) / (double) ancho));
            valor = max <= min ? max : min + f * (max - min);
            valor = Math.round(valor);
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Componentes.suave(g);
            int ancho = getWidth() - 20;
            int cy = getHeight() / 2;
            double f = max <= min ? 1 : (valor - min) / (max - min);
            int px = 10 + (int) (f * ancho);
            g2.setStroke(new BasicStroke(4, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(Tema.MENTA);
            g2.drawLine(10, cy, 10 + ancho, cy);
            g2.setColor(Tema.TURQUESA);
            g2.drawLine(10, cy, px, cy);
            g2.setColor(Tema.AZUL_NOCHE);
            g2.fillOval(px - 9, cy - 9, 18, 18);
            g2.dispose();
        }
    }
}
