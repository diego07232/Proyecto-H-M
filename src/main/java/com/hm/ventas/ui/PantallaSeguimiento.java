package com.hm.ventas.ui;

import com.hm.ventas.Envio;
import com.hm.ventas.Repartidor;
import java.awt.Font;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/** Pantalla "7. Seguimiento de envío". */
public class PantallaSeguimiento extends Pantalla {

    private static final DateTimeFormatter HH = DateTimeFormatter.ofPattern("HH:mm");
    private static final String[] ESTADOS = {"Pendiente", "Asignado", "En camino", "Entregado"};

    public PantallaSeguimiento(App app) {
        super(app);
    }

    @Override
    public void refrescar() {
        removeAll();
        Sesion s = app.getSesion();
        Envio envio = s.getEnvio2();
        botonAtras(app::volver);
        if (envio == null) {
            etiqueta("Aún no tienes envíos en curso.", Tema.sans(Font.PLAIN, 17),
                    Tema.GRIS_TEXTO, 40, 90);
            return;
        }

        etiqueta("Seguimiento de tu envío", Tema.serif(44), Tema.AZUL_NOCHE, 40, 64);
        etiqueta("Pedido N.° " + s.getPedido().getIdPedido() + " · Entrega en Lima",
                Tema.sans(Font.PLAIN, 14), Tema.GRIS_TEXTO, 40, 120);

        // Línea de progreso de 4 estados
        int actual = 0;
        for (int i = 0; i < ESTADOS.length; i++) {
            if (ESTADOS[i].equalsIgnoreCase(envio.getEstado())) {
                actual = i;
            }
        }
        boolean entregado = envio.verificarEntrega();
        for (int i = 0; i < 4; i++) {
            int cx = 120 + i * 280;
            if (i < 3) {
                JPanel linea = new JPanel();
                linea.setBackground(i < actual ? Tema.TURQUESA : Tema.MENTA_CLARO);
                poner(linea, cx + 24, 190, 232, 4);
            }
            boolean hecho = i < actual || (entregado && i == actual);
            boolean enCurso = i == actual && !entregado;
            Componentes.PanelRedondeado c = new Componentes.PanelRedondeado(
                    hecho ? Tema.AZUL_NOCHE : enCurso ? Tema.TURQUESA : Tema.MENTA_CLARO,
                    enCurso ? Tema.AZUL_NOCHE : null, 44);
            if (enCurso) {
                c.setColores(Tema.TURQUESA, Tema.AZUL_NOCHE, 2.5f);
            }
            JLabel n = Componentes.texto(String.valueOf(i + 1), Tema.sans(Font.BOLD, 15),
                    hecho || enCurso ? Tema.BLANCO : Tema.GRIS_TEXTO);
            n.setHorizontalAlignment(JLabel.CENTER);
            n.setBounds(0, 0, 44, 44);
            c.add(n);
            poner(c, cx - 22, 170, 44, 44);
            JLabel et = Componentes.texto(ESTADOS[i],
                    Tema.sans(i == actual ? Font.BOLD : Font.PLAIN, 14),
                    i <= actual ? Tema.AZUL_NOCHE : Tema.GRIS_TEXTO);
            et.setHorizontalAlignment(JLabel.CENTER);
            poner(et, cx - 70, 228, 140, 20);
        }

        // Repartidor
        Componentes.PanelRedondeado rep = new Componentes.PanelRedondeado(Tema.MENTA_CLARO, null, 22);
        poner(rep, 40, 288, 600, 200);
        Repartidor r = envio.getRepartidor();
        txt(rep, "Tu repartidor", Tema.sans(Font.PLAIN, 13), Tema.GRIS_TEXTO, 28, 24);
        if (r == null) {
            txt(rep, "Buscando repartidor…", Tema.sans(Font.BOLD, 24), Tema.AZUL_NOCHE, 28, 50);
            txt(rep, "Te avisaremos apenas se asigne uno.", Tema.sans(Font.PLAIN, 14),
                    Tema.GRIS_TEXTO, 28, 92);
        } else {
            txt(rep, r.getNombre(), Tema.sans(Font.BOLD, 24), Tema.AZUL_NOCHE, 28, 50);
            txt(rep, "Teléfono: " + r.getTelefono(), Tema.sans(Font.PLAIN, 15), Tema.GRIS_TEXTO, 28, 92);
            StringBuilder horas = new StringBuilder();
            if (s.getHoraAsignado() != null) {
                horas.append("Asignado a las ").append(s.getHoraAsignado().format(HH));
            }
            if (s.getHoraEnCamino() != null) {
                horas.append(" · En camino desde ").append(s.getHoraEnCamino().format(HH));
            }
            txt(rep, horas.toString(), Tema.sans(Font.PLAIN, 13), Tema.GRIS_TEXTO, 28, 122);
            Componentes.BotonPildora llamar = new Componentes.BotonPildora("Llamar al repartidor",
                    Componentes.EstiloBoton.CONTORNO, Tema.sans(Font.BOLD, 13),
                    () -> JOptionPane.showMessageDialog(app, "Llamando a " + r.getNombre()
                            + " al " + r.getTelefono() + "…", "Llamar al repartidor",
                            JOptionPane.INFORMATION_MESSAGE));
            llamar.setBounds(28, 146, 260, 44);
            rep.add(llamar);
        }

        // Dirección de entrega
        Componentes.PanelRedondeado dir =
                new Componentes.PanelRedondeado(Tema.BLANCO, Tema.BORDE.darker(), 22);
        poner(dir, 680, 288, 560, 200);
        var d = s.getEntregaCompra();
        txt(dir, "Dirección de entrega", Tema.sans(Font.PLAIN, 13), Tema.GRIS_TEXTO, 28, 24);
        txt(dir, d.direccion, Tema.sans(Font.BOLD, 19), Tema.AZUL_NOCHE, 28, 50);
        txt(dir, d.distrito + (d.referencia.isBlank() ? "" : " · " + d.referencia),
                Tema.sans(Font.PLAIN, 15), Tema.GRIS_TEXTO, 28, 84);
        StringBuilder prods = new StringBuilder("Productos: ");
        List<String[]> lista = s.getProductosComprados();
        for (int i = 0; i < lista.size(); i++) {
            prods.append(i > 0 ? ", " : "").append(lista.get(i)[0]).append(" x").append(lista.get(i)[1]);
        }
        JLabel lp = txt(dir, "<html>" + prods + "</html>", Tema.sans(Font.PLAIN, 13),
                Tema.GRIS_TEXTO, 28, 124);
        lp.setBounds(28, 120, 504, 60);
        lp.setVerticalAlignment(JLabel.TOP);

        // Historial
        etiqueta("Historial del envío", Tema.sans(Font.BOLD, 20), Tema.AZUL_NOCHE, 40, 528);
        int y = 572;
        List<String[]> hist = s.getHistorialEnvio();
        for (int i = 0; i < hist.size(); i++) {
            boolean ultimo = i == hist.size() - 1 && !entregado;
            Componentes.PanelRedondeado punto = new Componentes.PanelRedondeado(
                    ultimo ? Tema.TURQUESA : Tema.AZUL_NOCHE, null, 12);
            poner(punto, 40, y + 6, 12, 12);
            etiqueta(hist.get(i)[0], Tema.sans(Font.PLAIN, 15), Tema.AZUL_NOCHE, 70, y);
            etiqueta(hist.get(i)[1], Tema.sans(Font.PLAIN, 15), Tema.AZUL_NOCHE, 220, y);
            y += 40;
        }

        String mensaje = entregado ? "¡Tu pedido fue entregado!"
                : "Llegamos hoy antes de las 6 p. m.";
        etiqueta(mensaje, Tema.serif(26), Tema.TURQUESA, 40, Math.max(740, y + 30));
        if (!entregado) {
            etiqueta("(Simulación: el estado avanza automáticamente cada 6 segundos)",
                    Tema.sans(Font.PLAIN, 12), Tema.GRIS_DESACTIVADO, 40, Math.max(740, y + 30) + 40);
        }
        setAlto(Math.max(840, y + 140));
    }

    private static JLabel txt(JPanel p, String t, Font f, java.awt.Color c, int x, int y) {
        JLabel l = Componentes.texto(t, f, c);
        l.setBounds(x, y, Math.min(540, l.getPreferredSize().width + 4), l.getPreferredSize().height);
        p.add(l);
        return l;
    }
}
