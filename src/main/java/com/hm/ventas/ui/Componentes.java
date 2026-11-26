package com.hm.ventas.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Path2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.border.AbstractBorder;

/** Componentes visuales reutilizables que reproducen los del prototipo. */
public final class Componentes {

    private Componentes() {
    }

    static Graphics2D suave(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    // ------------------------------------------------------------------
    // Texto
    // ------------------------------------------------------------------

    public static JLabel texto(String t, Font f, Color c) {
        JLabel l = new JLabel(t);
        l.setFont(f);
        l.setForeground(c);
        return l;
    }

    /** Texto con aspecto de enlace (cursor de mano y subrayado al pasar). */
    public static JLabel enlace(String t, Font f, Color c, Runnable accion) {
        JLabel l = texto(t, f, c);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                accion.run();
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                l.setText("<html><u>" + t + "</u></html>");
            }

            @Override
            public void mouseExited(MouseEvent e) {
                l.setText(t);
            }
        });
        return l;
    }

    // ------------------------------------------------------------------
    // Panel redondeado
    // ------------------------------------------------------------------

    public static class PanelRedondeado extends JPanel {
        private Color fondo;
        private Color borde;
        private final int radio;
        private float grosorBorde = 1f;

        public PanelRedondeado(Color fondo, Color borde, int radio) {
            super(null);
            this.fondo = fondo;
            this.borde = borde;
            this.radio = radio;
            setOpaque(false);
        }

        public void setColores(Color fondo, Color borde, float grosor) {
            this.fondo = fondo;
            this.borde = borde;
            this.grosorBorde = grosor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            float m = grosorBorde / 2f;
            RoundRectangle2D r = new RoundRectangle2D.Float(m, m, getWidth() - grosorBorde,
                    getHeight() - grosorBorde, radio, radio);
            if (fondo != null) {
                g2.setColor(fondo);
                g2.fill(r);
            }
            if (borde != null) {
                g2.setColor(borde);
                g2.setStroke(new BasicStroke(grosorBorde));
                g2.draw(r);
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Botón tipo píldora
    // ------------------------------------------------------------------

    public enum EstiloBoton { PRIMARIO, CONTORNO, MENTA }

    public static class BotonPildora extends JComponent {
        private String texto;
        private final EstiloBoton estilo;
        private final Runnable accion;
        private boolean encima;
        private boolean habilitado = true;

        public BotonPildora(String texto, EstiloBoton estilo, Font fuente, Runnable accion) {
            this.texto = texto;
            this.estilo = estilo;
            this.accion = accion;
            setFont(fuente);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFocusable(true);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (habilitado && accion != null) {
                        accion.run();
                    }
                }
            });
        }

        public void setTexto(String t) {
            this.texto = t;
            repaint();
        }

        public String getTexto() {
            return texto;
        }

        public void setHabilitado(boolean h) {
            habilitado = h;
            setCursor(Cursor.getPredefinedCursor(h ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            int w = getWidth(), h = getHeight();
            Color fondo, borde, letra;
            switch (estilo) {
                case PRIMARIO -> {
                    fondo = encima ? AZUL_HOVER : Tema.AZUL_NOCHE;
                    borde = null;
                    letra = Tema.BLANCO;
                }
                case MENTA -> {
                    fondo = encima ? new Color(0x93D6D0) : Tema.MENTA;
                    borde = null;
                    letra = Tema.AZUL_NOCHE;
                }
                default -> {
                    fondo = encima ? Tema.MENTA_CLARO : Tema.BLANCO;
                    borde = Tema.AZUL_NOCHE;
                    letra = Tema.AZUL_NOCHE;
                }
            }
            if (!habilitado) {
                fondo = new Color(0xC9CCD6);
                letra = Tema.BLANCO;
                borde = null;
            }
            RoundRectangle2D r = new RoundRectangle2D.Float(0.5f, 0.5f, w - 1f, h - 1f, h, h);
            g2.setColor(fondo);
            g2.fill(r);
            if (borde != null) {
                g2.setColor(borde);
                g2.setStroke(new BasicStroke(1.2f));
                g2.draw(r);
            }
            g2.setFont(getFont());
            g2.setColor(letra);
            FontMetrics fm = g2.getFontMetrics();
            int tx = (w - fm.stringWidth(texto)) / 2;
            int ty = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.drawString(texto, tx, ty);
            g2.dispose();
        }
    }

    private static final Color AZUL_HOVER = new Color(0x3A4185);

    // ------------------------------------------------------------------
    // Selector de tallas (chips)
    // ------------------------------------------------------------------

    public static class SelectorTalla extends JComponent {
        private final List<String> opciones = new ArrayList<>();
        private Set<String> deshabilitadas = Set.of();
        private String seleccion;
        private final int anchoChip;
        private final int altoChip;
        private final int separacion;
        private final int columnas;
        private Consumer<String> alCambiar = s -> { };
        private final boolean permiteDeseleccionar;

        public SelectorTalla(int anchoChip, int altoChip, int separacion, int columnas,
                             boolean permiteDeseleccionar) {
            this.anchoChip = anchoChip;
            this.altoChip = altoChip;
            this.separacion = separacion;
            this.columnas = columnas;
            this.permiteDeseleccionar = permiteDeseleccionar;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int col = e.getX() / (anchoChip + separacion);
                    int fila = e.getY() / (altoChip + separacion);
                    if (col >= columnas || e.getX() - col * (anchoChip + separacion) > anchoChip
                            || e.getY() - fila * (altoChip + separacion) > altoChip) {
                        return;
                    }
                    int i = fila * columnas + col;
                    if (i >= 0 && i < opciones.size()) {
                        String o = opciones.get(i);
                        if (deshabilitadas.contains(o)) {
                            return;
                        }
                        if (permiteDeseleccionar && o.equals(seleccion)) {
                            seleccion = null;
                        } else {
                            seleccion = o;
                        }
                        repaint();
                        alCambiar.accept(seleccion);
                    }
                }
            });
        }

        public void setOpciones(List<String> ops, Set<String> deshab, String sel) {
            opciones.clear();
            opciones.addAll(ops);
            deshabilitadas = deshab;
            seleccion = sel;
            repaint();
        }

        /** Alto total necesario según el número de filas. */
        public int altoNecesario() {
            int filas = Math.max(1, (opciones.size() + columnas - 1) / columnas);
            return filas * altoChip + (filas - 1) * separacion;
        }

        public String getSeleccion() {
            return seleccion;
        }

        public void setAlCambiar(Consumer<String> c) {
            alCambiar = c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            int h = altoChip;
            float tam = h > 40 ? 15 : 12;
            for (int i = 0; i < opciones.size(); i++) {
                String o = opciones.get(i);
                int x = (i % columnas) * (anchoChip + separacion);
                int y = (i / columnas) * (altoChip + separacion);
                RoundRectangle2D r = new RoundRectangle2D.Float(x + 0.5f, y + 0.5f,
                        anchoChip - 1f, h - 1f, h, h);
                boolean sel = o.equals(seleccion);
                boolean des = deshabilitadas.contains(o);
                if (sel) {
                    g2.setColor(Tema.AZUL_NOCHE);
                    g2.fill(r);
                    g2.setColor(Tema.BLANCO);
                } else {
                    g2.setColor(Tema.BLANCO);
                    g2.fill(r);
                    g2.setColor(Tema.BORDE.darker());
                    g2.setStroke(new BasicStroke(1f));
                    g2.draw(r);
                    g2.setColor(des ? Tema.GRIS_DESACTIVADO : Tema.AZUL_NOCHE);
                }
                g2.setFont(Tema.sans(des ? Font.PLAIN : Font.BOLD, tam));
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(o, x + (anchoChip - fm.stringWidth(o)) / 2,
                        y + (h - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Selector de color (círculos)
    // ------------------------------------------------------------------

    public static class SelectorColor extends JComponent {
        private final List<String> colores = new ArrayList<>();
        private String seleccion;
        private final int diametro;
        private final int separacion;
        private final int columnas;
        private Consumer<String> alCambiar = s -> { };
        private final boolean permiteDeseleccionar;

        public SelectorColor(int diametro, int separacion, int columnas,
                             boolean permiteDeseleccionar) {
            this.diametro = diametro;
            this.separacion = separacion;
            this.columnas = columnas;
            this.permiteDeseleccionar = permiteDeseleccionar;
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setToolTipText("");
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int i = indiceEn(e.getX(), e.getY());
                    if (i >= 0) {
                        String c = colores.get(i);
                        seleccion = (permiteDeseleccionar && c.equals(seleccion)) ? null : c;
                        repaint();
                        alCambiar.accept(seleccion);
                    }
                }
            });
        }

        private int indiceEn(int x, int y) {
            int paso = diametro + separacion;
            int col = (x - 3) / paso;
            int fila = (y - 3) / paso;
            if (col < 0 || col >= columnas || fila < 0) {
                return -1;
            }
            int i = fila * columnas + col;
            return i < colores.size() ? i : -1;
        }

        public void setColores(List<String> lista, String sel) {
            colores.clear();
            colores.addAll(lista);
            seleccion = sel;
            repaint();
        }

        public int altoNecesario() {
            int filas = Math.max(1, (colores.size() + columnas - 1) / columnas);
            return filas * (diametro + separacion) - separacion + 6;
        }

        public String getSeleccion() {
            return seleccion;
        }

        public void setAlCambiar(Consumer<String> c) {
            alCambiar = c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            for (int i = 0; i < colores.size(); i++) {
                String c = colores.get(i);
                int x = 3 + (i % columnas) * (diametro + separacion);
                int y = 3 + (i / columnas) * (diametro + separacion);
                g2.setColor(Tema.colorDe(c));
                g2.fillOval(x, y, diametro, diametro);
                g2.setStroke(new BasicStroke(1f));
                g2.setColor(Tema.BORDE.darker());
                g2.drawOval(x, y, diametro, diametro);
                if (c.equals(seleccion)) {
                    g2.setColor(Tema.AZUL_NOCHE);
                    g2.setStroke(new BasicStroke(2f));
                    g2.drawOval(x - 2, y - 2, diametro + 4, diametro + 4);
                }
            }
            g2.dispose();
        }

        @Override
        public String getToolTipText(MouseEvent e) {
            int i = indiceEn(e.getX(), e.getY());
            return i >= 0 ? colores.get(i) : null;
        }
    }

    // ------------------------------------------------------------------
    // Selector de cantidad  ( -  2  + )
    // ------------------------------------------------------------------

    public static class SelectorCantidad extends JComponent {
        private int valor = 1;
        private int maximo = 99;
        private IntConsumer alCambiar = v -> { };

        public SelectorCantidad() {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    int tercio = getWidth() / 3;
                    if (e.getX() < tercio && valor > 1) {
                        valor--;
                    } else if (e.getX() > 2 * tercio && valor < maximo) {
                        valor++;
                    } else {
                        return;
                    }
                    repaint();
                    alCambiar.accept(valor);
                }
            });
        }

        public void setValor(int v, int max) {
            maximo = Math.max(1, max);
            valor = Math.max(1, Math.min(v, maximo));
            repaint();
        }

        public int getValor() {
            return valor;
        }

        public void setAlCambiar(IntConsumer c) {
            alCambiar = c;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            int w = getWidth(), h = getHeight();
            g2.setColor(Tema.BLANCO);
            g2.fillRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setColor(Tema.BORDE.darker());
            g2.drawRoundRect(0, 0, w - 1, h - 1, h, h);
            g2.setFont(Tema.sans(Font.BOLD, 16));
            FontMetrics fm = g2.getFontMetrics();
            int base = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(valor > 1 ? Tema.AZUL_NOCHE : Tema.GRIS_DESACTIVADO);
            g2.drawString("−", w / 6 - fm.stringWidth("−") / 2, base);
            g2.setColor(valor < maximo ? Tema.AZUL_NOCHE : Tema.GRIS_DESACTIVADO);
            g2.drawString("+", 5 * w / 6 - fm.stringWidth("+") / 2, base);
            g2.setColor(Tema.AZUL_NOCHE);
            String v = String.valueOf(valor);
            g2.drawString(v, (w - fm.stringWidth(v)) / 2, base);
            g2.dispose();
        }
    }

    // ------------------------------------------------------------------
    // Campos de texto con borde redondeado
    // ------------------------------------------------------------------

    public static class BordeRedondeado extends AbstractBorder {
        private Color color;
        private final int radio;

        public BordeRedondeado(Color color, int radio) {
            this.color = color;
            this.radio = radio;
        }

        public void setColor(Color c) {
            this.color = c;
        }

        @Override
        public void paintBorder(java.awt.Component c, Graphics g, int x, int y, int w, int h) {
            Graphics2D g2 = suave(g);
            g2.setColor(color);
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawRoundRect(x, y, w - 1, h - 1, radio, radio);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(java.awt.Component c) {
            return new Insets(4, 16, 4, 16);
        }
    }

    public static JTextField campo(String valor) {
        JTextField f = new CampoRedondeado(valor);
        return f;
    }

    public static JPasswordField campoClave(String valor) {
        JPasswordField f = new JPasswordField(valor) {
            @Override
            protected void paintComponent(Graphics g) {
                pintarFondoCampo(this, g);
                super.paintComponent(g);
            }
        };
        estilizar(f);
        return f;
    }

    private static class CampoRedondeado extends JTextField {
        CampoRedondeado(String valor) {
            super(valor);
            estilizar(this);
        }

        @Override
        protected void paintComponent(Graphics g) {
            pintarFondoCampo(this, g);
            super.paintComponent(g);
        }
    }

    private static void pintarFondoCampo(JComponent c, Graphics g) {
        Graphics2D g2 = suave(g);
        g2.setColor(Tema.BLANCO);
        g2.fillRoundRect(0, 0, c.getWidth() - 1, c.getHeight() - 1, 14, 14);
        g2.dispose();
    }

    private static void estilizar(JTextField f) {
        f.setOpaque(false);
        f.setFont(Tema.sans(Font.PLAIN, 15));
        f.setForeground(Tema.AZUL_NOCHE);
        f.setCaretColor(Tema.AZUL_NOCHE);
        BordeRedondeado borde = new BordeRedondeado(Tema.BORDE.darker(), 14);
        f.setBorder(borde);
        f.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent e) {
                borde.setColor(Tema.TURQUESA);
                f.repaint();
            }

            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                borde.setColor(Tema.BORDE.darker());
                f.repaint();
            }
        });
    }

    public static void marcarError(JTextField f, boolean error) {
        if (f.getBorder() instanceof BordeRedondeado b) {
            b.setColor(error ? Tema.ROJO : Tema.BORDE.darker());
            f.repaint();
        }
    }

    public static JPanel vacio() {
        JPanel p = new JPanel(null);
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder());
        return p;
    }

    // ------------------------------------------------------------------
    // Imagen de prenda (ilustración vectorial en el color del producto)
    // ------------------------------------------------------------------

    public static class ImagenPrenda extends JComponent {
        private String color;
        private String categoria;
        private java.awt.image.BufferedImage imagen;
        private Color fondo = Tema.MENTA_CLARO;
        private final int radio;
        private String leyenda;
        private String etiqueta;

        public ImagenPrenda(int radio) {
            this.radio = radio;
        }

        public void setPrenda(com.hm.ventas.datos.ItemCatalogo item) {
            String categoria = item.getCategoria().getNombre();
            String color = item.getColor();
            imagen = item.getImagen();
            this.categoria = categoria == null ? "" : categoria.toLowerCase();
            this.color = color;
            repaint();
        }

        public void setFondo(Color c) {
            fondo = c;
            repaint();
        }

        public void setLeyenda(String l) {
            leyenda = l;
            repaint();
        }

        public void setEtiqueta(String e) {
            etiqueta = e;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            int w = getWidth(), h = getHeight();
            g2.setColor(fondo);
            g2.fillRoundRect(0, 0, w, h, radio, radio);

            if (imagen != null) {
                g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                        java.awt.RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                double escala = Math.min(w / (double) imagen.getWidth(), h / (double) imagen.getHeight());
                int ancho = (int) Math.round(imagen.getWidth() * escala);
                int alto = (int) Math.round(imagen.getHeight() * escala);
                g2.clip(new java.awt.geom.RoundRectangle2D.Double(0, 0, w, h, radio, radio));
                g2.drawImage(imagen, (w - ancho) / 2, (h - alto) / 2, ancho, alto, null);
            } else if (color != null) {
                Path2D forma = silueta(categoria, w, h);
                Color c = Tema.colorDe(color);
                g2.setColor(c);
                g2.fill(forma);
                g2.setColor(c.equals(Color.WHITE) ? Tema.BORDE.darker() : c.darker());
                g2.setStroke(new BasicStroke(1.3f));
                g2.draw(forma);
            }
            if (leyenda != null) {
                g2.setFont(Tema.sans(Font.PLAIN, 14));
                g2.setColor(Tema.GRIS_TEXTO);
                FontMetrics fm = g2.getFontMetrics();
                g2.drawString(leyenda, (w - fm.stringWidth(leyenda)) / 2, h - 24);
            }
            if (etiqueta != null) {
                g2.setFont(Tema.sans(Font.BOLD, 11));
                FontMetrics fm = g2.getFontMetrics();
                int ew = fm.stringWidth(etiqueta) + 16;
                int ex = w - ew - 12;
                g2.setColor(Tema.TURQUESA);
                g2.fillRoundRect(ex, 12, ew, 26, 26, 26);
                g2.setColor(Tema.BLANCO);
                g2.drawString(etiqueta, ex + 8, 12 + (26 - fm.getHeight()) / 2 + fm.getAscent());
            }
            g2.dispose();
        }

        /** Dibuja una silueta simple según la categoría de la prenda. */
        private static Path2D silueta(String cat, int w, int h) {
            double s = Math.min(w, h) * 0.62;
            double cx = w / 2.0, cy = h / 2.0;
            Path2D p = new Path2D.Double();
            if (cat.contains("pantal")) {
                double t = s * 0.5, alto = s * 0.95;
                double x0 = cx - t / 2 - s * 0.08, y0 = cy - alto / 2;
                double ancho = t + s * 0.16;
                p.moveTo(x0, y0);
                p.lineTo(x0 + ancho, y0);
                p.lineTo(x0 + ancho + s * 0.05, y0 + alto);
                p.lineTo(cx + s * 0.04, y0 + alto);
                p.lineTo(cx, y0 + alto * 0.32);
                p.lineTo(cx - s * 0.04, y0 + alto);
                p.lineTo(x0 - s * 0.05, y0 + alto);
                p.closePath();
            } else if (cat.contains("vestido")) {
                double y0 = cy - s * 0.48;
                p.moveTo(cx - s * 0.14, y0);
                p.lineTo(cx + s * 0.14, y0);
                p.lineTo(cx + s * 0.18, y0 + s * 0.35);
                p.lineTo(cx + s * 0.42, y0 + s * 0.95);
                p.lineTo(cx - s * 0.42, y0 + s * 0.95);
                p.lineTo(cx - s * 0.18, y0 + s * 0.35);
                p.closePath();
            } else if (cat.contains("zapat")) {
                double y0 = cy - s * 0.12;
                p.moveTo(cx - s * 0.48, y0 + s * 0.28);
                p.lineTo(cx - s * 0.48, y0 - s * 0.05);
                p.quadTo(cx - s * 0.30, y0 - s * 0.12, cx - s * 0.12, y0 - s * 0.02);
                p.quadTo(cx + s * 0.18, y0 + s * 0.06, cx + s * 0.40, y0 + s * 0.12);
                p.quadTo(cx + s * 0.52, y0 + s * 0.18, cx + s * 0.50, y0 + s * 0.28);
                p.closePath();
            } else if (cat.contains("acces")) {
                double r = s * 0.42;
                p.moveTo(cx - r, cy + r * 0.25);
                p.quadTo(cx - r, cy - r * 0.95, cx, cy - r * 0.95);
                p.quadTo(cx + r, cy - r * 0.95, cx + r, cy + r * 0.25);
                p.lineTo(cx + r * 1.45, cy + r * 0.38);
                p.lineTo(cx - r, cy + r * 0.38);
                p.closePath();
            } else {
                // polo / camisa / casaca
                boolean largo = cat.contains("casaca") || cat.contains("camisa");
                double hom = s * 0.5, cuerpo = s * 0.34;
                double y0 = cy - s * 0.42, y1 = cy + s * 0.48;
                double manga = largo ? s * 0.55 : s * 0.28;
                p.moveTo(cx - s * 0.12, y0);
                p.quadTo(cx, y0 + s * 0.1, cx + s * 0.12, y0);
                p.lineTo(cx + hom, y0 + s * 0.08);
                p.lineTo(cx + hom + s * 0.06, y0 + manga);
                p.lineTo(cx + cuerpo + s * 0.02, y0 + manga + s * 0.04);
                p.lineTo(cx + cuerpo, y1);
                p.lineTo(cx - cuerpo, y1);
                p.lineTo(cx - cuerpo - s * 0.02, y0 + manga + s * 0.04);
                p.lineTo(cx - hom - s * 0.06, y0 + manga);
                p.lineTo(cx - hom, y0 + s * 0.08);
                p.closePath();
            }
            return p;
        }
    }
}
