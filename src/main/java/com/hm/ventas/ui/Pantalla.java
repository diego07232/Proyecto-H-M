package com.hm.ventas.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 * Pantalla base. Usa posiciones absolutas tomadas del prototipo de Figma
 * (lienzo de 1280 px de ancho; la barra de navegación ocupa los primeros 72 px
 * y se dibuja aparte, por eso aquí las coordenadas Y van restadas en 72).
 */
public abstract class Pantalla extends JPanel {

    public static final int ANCHO = 1280;
    protected final App app;

    protected Pantalla(App app) {
        super(null);
        this.app = app;
        setBackground(Tema.FONDO);
        setOpaque(true);
        setAlto(888);
    }

    protected final void setAlto(int alto) {
        Dimension d = new Dimension(ANCHO, alto);
        setPreferredSize(d);
        setSize(d);
        revalidate();
    }

    /** Se llama cada vez que la pantalla se muestra: debe redibujar sus datos. */
    public abstract void refrescar();

    protected <T extends Component> T poner(T c, int x, int y, int w, int h) {
        c.setBounds(x, y, w, h);
        add(c);
        return c;
    }

    protected JLabel etiqueta(String t, Font f, java.awt.Color c, int x, int y) {
        JLabel l = Componentes.texto(t, f, c);
        Dimension d = l.getPreferredSize();
        l.setBounds(x, y, d.width + 4, d.height);
        add(l);
        return l;
    }

    protected JLabel etiquetaCentrada(String t, Font f, java.awt.Color c, int y) {
        JLabel l = Componentes.texto(t, f, c);
        l.setHorizontalAlignment(JLabel.CENTER);
        l.setBounds(0, y, ANCHO, l.getPreferredSize().height);
        add(l);
        return l;
    }

    protected void botonAtras(Runnable accion) {
        poner(new Componentes.BotonPildora("‹ Atrás", Componentes.EstiloBoton.CONTORNO,
                Tema.sans(Font.BOLD, 14), accion), 40, 20, 112, 38);
    }

    /** Indicador de pasos del checkout: 1 Entrega — 2 Comprobante — 3 Pago. */
    protected void pasos(int actual) {
        String[] nombres = {"Entrega", "Comprobante", "Pago"};
        for (int i = 0; i < 3; i++) {
            int x = 40 + i * 240;
            boolean alcanzado = i + 1 <= actual;
            Componentes.PanelRedondeado c = new Componentes.PanelRedondeado(
                    alcanzado ? Tema.AZUL_NOCHE : Tema.MENTA_CLARO, null, 32);
            JLabel n = Componentes.texto(String.valueOf(i + 1), Tema.sans(Font.BOLD, 13),
                    alcanzado ? Tema.BLANCO : Tema.GRIS_TEXTO);
            n.setHorizontalAlignment(JLabel.CENTER);
            n.setBounds(0, 0, 32, 32);
            c.add(n);
            poner(c, x, 78, 32, 32);
            etiqueta(nombres[i], Tema.sans(i + 1 == actual ? Font.BOLD : Font.PLAIN, 14),
                    Tema.AZUL_NOCHE, x + 42, 84);
            if (i < 2) {
                JPanel linea = new JPanel();
                linea.setBackground(i + 1 < actual ? Tema.TURQUESA : Tema.BORDE);
                poner(linea, x + 160, 93, 60, 2);
            }
        }
    }

    /** Línea separadora de 1 px. */
    protected JComponent separador(int x, int y, int w) {
        JPanel s = new JPanel();
        s.setBackground(Tema.BORDE);
        s.setBounds(x, y, w, 1);
        add(s);
        return s;
    }
}
