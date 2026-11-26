package com.hm.ventas.ui;

import com.hm.ventas.Comprobante;
import com.hm.ventas.Factura;
import com.hm.ventas.Boleta;
import com.hm.ventas.datos.TiendaRepositorio;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.format.DateTimeFormatter;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/** Ventanas emergentes: guía de tallas, mis pedidos, mis datos y comprobante. */
final class Dialogos {

    private Dialogos() {
    }

    static void guiaTallas(App app) {
        String[] cols = {"Talla", "Pecho (cm)", "Cintura (cm)", "Largo (cm)"};
        Object[][] filas = {
            {"XS", "82–86", "66–70", "66"},
            {"S", "87–92", "71–76", "68"},
            {"M", "93–98", "77–82", "70"},
            {"L", "99–104", "83–88", "72"},
            {"XL", "105–110", "89–94", "74"},
            {"XXL", "111–116", "95–100", "76"},
        };
        JTable t = tabla(new DefaultTableModel(filas, cols));
        JScrollPane sp = new JScrollPane(t);
        sp.setPreferredSize(new Dimension(460, 160));
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.add(sp, BorderLayout.CENTER);
        p.add(new JLabel("Pantalones: talla = cintura en pulgadas (28–34). "
                + "Zapatillas: talla peruana (37–42)."), BorderLayout.SOUTH);
        JOptionPane.showMessageDialog(app, p, "Guía de tallas", JOptionPane.PLAIN_MESSAGE);
    }

    static void misPedidos(App app) {
        String correo = app.getSesion().getEntrega().correo;
        if (correo.isBlank()) {
            JOptionPane.showMessageDialog(app, "Inicia sesión registrando tus datos en \"Mis datos\".",
                    "Mis pedidos", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        List<TiendaRepositorio.ResumenVenta> ventas = app.getRepo().listarVentasDe(correo);
        DateTimeFormatter f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        DefaultTableModel m = new DefaultTableModel(
                new String[]{"Pedido N.°", "Fecha", "Estado", "Total", "Pago", "Estado del pago"}, 0);
        for (TiendaRepositorio.ResumenVenta v : ventas) {
            m.addRow(new Object[]{v.idVenta, v.fecha.format(f), v.estado, Tema.soles(v.total),
                v.metodoPago, v.estadoPago});
        }
        JPanel p = new JPanel(new BorderLayout(0, 8));
        p.add(new JLabel("Pedidos de " + correo
                + (app.getRepo().isConectado() ? " (base de datos tienda_online)" : " (modo demostración)")),
                BorderLayout.NORTH);
        if (ventas.isEmpty()) {
            p.add(new JLabel("Todavía no tienes pedidos registrados."), BorderLayout.CENTER);
        } else {
            JScrollPane sp = new JScrollPane(tabla(m));
            sp.setPreferredSize(new Dimension(720, Math.min(320, 40 + ventas.size() * 26)));
            p.add(sp, BorderLayout.CENTER);
        }
        JOptionPane.showMessageDialog(app, p, "Mis pedidos", JOptionPane.PLAIN_MESSAGE);
    }

    static void misDatos(App app) {
        TiendaRepositorio.DatosEntrega d = app.getSesion().getEntrega();
        JTextField nombre = new JTextField(d.nombreCompleto, 24);
        JTextField correo = new JTextField(d.correo);
        JTextField telefono = new JTextField(d.telefono);
        JTextField direccion = new JTextField(d.direccion);
        JTextField distrito = new JTextField(d.distrito);
        JTextField referencia = new JTextField(d.referencia);
        JPanel p = new JPanel(new GridLayout(0, 2, 8, 8));
        p.add(new JLabel("Nombre completo"));
        p.add(nombre);
        p.add(new JLabel("Correo electrónico"));
        p.add(correo);
        p.add(new JLabel("Teléfono"));
        p.add(telefono);
        p.add(new JLabel("Dirección"));
        p.add(direccion);
        p.add(new JLabel("Distrito"));
        p.add(distrito);
        p.add(new JLabel("Referencia"));
        p.add(referencia);
        int r = JOptionPane.showConfirmDialog(app, p, "Mis datos", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (r == JOptionPane.OK_OPTION) {
            d.nombreCompleto = nombre.getText().trim();
            d.correo = correo.getText().trim();
            d.telefono = telefono.getText().trim();
            d.direccion = direccion.getText().trim();
            d.distrito = distrito.getText().trim();
            d.referencia = referencia.getText().trim();
            app.avisar("Datos actualizados");
            app.refrescarActual();
        }
    }

    /** Muestra la boleta o factura con el desglose del IGV (18 %). */
    static void comprobante(App app) {
        Sesion s = app.getSesion();
        Comprobante c = s.getComprobante();
        if (c == null) {
            return;
        }
        StringBuilder sb = new StringBuilder();
        boolean esFactura = c instanceof Factura;
        sb.append("              H&M VENTAS ONLINE\n");
        sb.append(esFactura ? "         FACTURA ELECTRÓNICA\n" : "          BOLETA ELECTRÓNICA\n");
        sb.append("              ").append(s.numeroComprobante()).append("\n");
        sb.append("------------------------------------------------\n");
        sb.append("Fecha: ").append(c.getFecha()).append("\n");
        if (c instanceof Factura fac) {
            sb.append("RUC: ").append(fac.getRuc()).append("\n");
            sb.append("Razón social: ").append(fac.getRazonSocial()).append("\n");
        } else if (c instanceof Boleta b) {
            sb.append("DNI: ").append(b.getDni()).append("\n");
            sb.append("Cliente: ").append(b.getNombreCliente()).append("\n");
        }
        sb.append("------------------------------------------------\n");
        for (String[] l : s.getProductosComprados()) {
            String desc = l[0] + " " + l[3] + " " + l[4] + " x" + l[1];
            sb.append(String.format("%-34s%14s%n", recortar(desc, 34), l[2]));
        }
        if (s.getEnvioPagado() > 0) {
            sb.append(String.format("%-34s%14s%n", "Envío a Lima", Tema.soles(s.getEnvioPagado())));
        }
        double total = c.getMonto();
        double base = total / (1 + Comprobante.IGV);
        double igv = total - base;
        sb.append("------------------------------------------------\n");
        sb.append(String.format("%-34s%14s%n", "Op. gravada", Tema.soles(base)));
        sb.append(String.format("%-34s%14s%n", "IGV (18 %)", Tema.soles(igv)));
        sb.append(String.format("%-34s%14s%n", "TOTAL", Tema.soles(total)));
        sb.append("\nPagado con ").append(s.getPago().getMetodoPago())
                .append(" · Estado: ").append(s.getPago().getEstado()).append("\n");

        JTextArea a = new JTextArea(sb.toString());
        a.setEditable(false);
        a.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        a.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JOptionPane.showMessageDialog(app, new JScrollPane(a),
                esFactura ? "Factura electrónica" : "Boleta electrónica", JOptionPane.PLAIN_MESSAGE);
    }

    private static String recortar(String s, int n) {
        return s.length() <= n ? s : s.substring(0, n - 1) + "…";
    }

    private static JTable tabla(DefaultTableModel m) {
        JTable t = new JTable(m) {
            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        t.setRowHeight(26);
        t.setFont(Tema.sans(Font.PLAIN, 13));
        t.getTableHeader().setFont(Tema.sans(Font.BOLD, 13));
        return t;
    }
}
