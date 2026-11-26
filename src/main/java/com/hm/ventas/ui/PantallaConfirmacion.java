package com.hm.ventas.ui;

import com.hm.ventas.Factura;
import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;
import javax.swing.JLabel;

/** Pantalla "6. Confirmación de pedido". */
public class PantallaConfirmacion extends Pantalla {

    public PantallaConfirmacion(App app) {
        super(app);
    }

    @Override
    public void refrescar() {
        removeAll();
        Sesion s = app.getSesion();
        if (s.getPedido() == null) {
            etiquetaCentrada("Aún no tienes pedidos.", Tema.sans(Font.PLAIN, 17), Tema.GRIS_TEXTO, 120);
            return;
        }

        poner(new IconoExito(), 600, 78, 80, 80);
        etiquetaCentrada("Pago confirmado", Tema.serif(52), Tema.AZUL_NOCHE, 186);
        String nombre = s.getEntregaCompra().nombreCompleto.split(" ")[0];
        etiquetaCentrada("Gracias por tu compra, " + nombre
                + ". Te enviaremos los detalles a tu correo.", Tema.sans(Font.PLAIN, 17),
                Tema.GRIS_TEXTO, 268);

        Componentes.PanelRedondeado p =
                new Componentes.PanelRedondeado(Tema.BLANCO, Tema.BORDE.darker(), 22);
        poner(p, 380, 358, 520, 300);
        texto(p, "Pedido N.° " + s.getPedido().getIdPedido(), Tema.sans(Font.BOLD, 20), 32, 28);
        texto(p, "Fecha: " + s.getFechaCompra().format(Sesion.FECHA), cuerpo(), 32, 66);
        texto(p, "Estado del pago: " + s.getPago().getEstado(), cuerpo(), 32, 96);
        String tipo = s.getComprobante() instanceof Factura ? "Factura electrónica"
                : "Boleta electrónica";
        texto(p, "Comprobante: " + tipo + " " + s.numeroComprobante(), cuerpo(), 32, 126);
        JLabel ver = Componentes.enlace("Ver", Tema.sans(Font.BOLD, 14), Tema.TURQUESA,
                () -> Dialogos.comprobante(app));
        ver.setBounds(440, 126, 50, 20);
        p.add(ver);
        String dir = s.getEntregaCompra().direccion + ", " + s.getEntregaCompra().distrito;
        texto(p, "Dirección: " + dir, cuerpo(), 32, 156).setSize(456, 20);
        javax.swing.JPanel sep = new javax.swing.JPanel();
        sep.setBackground(Tema.BORDE);
        sep.setBounds(32, 196, 456, 1);
        p.add(sep);
        texto(p, "Total pagado", Tema.sans(Font.BOLD, 19), 32, 216);
        JLabel total = Componentes.texto(Tema.soles(s.getTotalPagado()), Tema.sans(Font.BOLD, 19),
                Tema.AZUL_NOCHE);
        total.setHorizontalAlignment(JLabel.RIGHT);
        total.setBounds(288, 216, 200, 26);
        p.add(total);
        texto(p, "Pagado con " + s.getPago().getMetodoPago(), Tema.sans(Font.PLAIN, 12), 32, 252)
                .setForeground(Tema.GRIS_TEXTO);

        poner(new Componentes.BotonPildora("Seguir el envío", Componentes.EstiloBoton.PRIMARIO,
                Tema.sans(Font.BOLD, 16), () -> app.mostrar(App.SEGUIMIENTO)), 480, 698, 320, 56);
        JLabel volver = Componentes.enlace("Volver a la tienda", Tema.sans(Font.PLAIN, 15),
                Tema.AZUL_NOCHE, () -> app.catalogo.irA(null, null));
        volver.setHorizontalAlignment(JLabel.CENTER);
        poner(volver, 540, 774, 200, 22);
        setAlto(820);
    }

    private static Font cuerpo() {
        return Tema.sans(Font.PLAIN, 15);
    }

    private static JLabel texto(javax.swing.JPanel p, String t, Font f, int x, int y) {
        JLabel l = Componentes.texto(t, f, Tema.AZUL_NOCHE);
        l.setBounds(x, y, l.getPreferredSize().width + 4, l.getPreferredSize().height);
        p.add(l);
        return l;
    }

    /** Círculo menta con un check (ícono de éxito del prototipo). */
    private static class IconoExito extends JComponent {
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = Componentes.suave(g);
            g2.setColor(Tema.MENTA);
            g2.fillOval(0, 0, getWidth(), getHeight());
            g2.setColor(Tema.AZUL_NOCHE);
            g2.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            int w = getWidth();
            g2.drawPolyline(new int[]{w * 27 / 100, w * 44 / 100, w * 74 / 100},
                    new int[]{w * 52 / 100, w * 68 / 100, w * 36 / 100}, 3);
            g2.dispose();
        }
    }
}
