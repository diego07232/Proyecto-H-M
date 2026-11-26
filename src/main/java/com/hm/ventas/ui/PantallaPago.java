package com.hm.ventas.ui;

import com.hm.ventas.datos.LineaCarrito;
import java.awt.Font;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Pantalla "5. Pago con tarjeta". */
public class PantallaPago extends Pantalla {

    private final JTextField numero = Componentes.campo("4242 4242 4242 4242");
    private final JTextField vence = Componentes.campo("12 / 29");
    private final JPasswordField cvv = Componentes.campoClave("");

    private JLabel visMarca;
    private JLabel visNumero;
    private JLabel visVence;
    private JLabel estadoPago;
    private Componentes.BotonPildora pagar;
    private boolean procesando;

    public PantallaPago(App app) {
        super(app);
        DocumentListener dl = new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { actualizarTarjeta(); }
            public void removeUpdate(DocumentEvent e) { actualizarTarjeta(); }
            public void changedUpdate(DocumentEvent e) { actualizarTarjeta(); }
        };
        numero.getDocument().addDocumentListener(dl);
        vence.getDocument().addDocumentListener(dl);
        numero.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusLost(java.awt.event.FocusEvent e) {
                String d = soloDigitos(numero.getText());
                numero.setText(d.replaceAll("(.{4})(?!$)", "$1 "));
            }
        });
    }

    @Override
    public void refrescar() {
        removeAll();
        Sesion s = app.getSesion();
        if (s.getCarrito().isEmpty()) {
            botonAtras(() -> app.mostrar(App.CATALOGO));
            etiqueta("No hay productos por pagar.", Tema.sans(Font.PLAIN, 17), Tema.GRIS_TEXTO, 40, 90);
            return;
        }
        botonAtras(() -> app.mostrar(App.CHECKOUT));
        pasos(3);
        etiqueta("Pago seguro", Tema.serif(44), Tema.AZUL_NOCHE, 40, 126);

        // Tarjeta visual
        Componentes.PanelRedondeado tarjeta =
                new Componentes.PanelRedondeado(Tema.AZUL_NOCHE, null, 28);
        poner(tarjeta, 40, 198, 520, 260);
        visMarca = Componentes.texto("VISA", Tema.sans(Font.BOLD, 20), Tema.MENTA);
        visMarca.setHorizontalAlignment(JLabel.RIGHT);
        visMarca.setBounds(300, 28, 180, 26);
        tarjeta.add(visMarca);
        visNumero = Componentes.texto("", Tema.sans(Font.PLAIN, 26), Tema.BLANCO);
        visNumero.setBounds(40, 118, 460, 34);
        tarjeta.add(visNumero);
        JLabel titular = Componentes.texto("TITULAR", Tema.sans(Font.PLAIN, 10), Tema.MENTA);
        titular.setBounds(40, 190, 100, 13);
        tarjeta.add(titular);
        JLabel nombre = Componentes.texto(s.getEntrega().nombreCompleto.toUpperCase(Locale.ROOT),
                Tema.sans(Font.BOLD, 14), Tema.BLANCO);
        nombre.setBounds(40, 206, 300, 20);
        tarjeta.add(nombre);
        JLabel lv = Componentes.texto("VENCE", Tema.sans(Font.PLAIN, 10), Tema.MENTA);
        lv.setBounds(360, 190, 100, 13);
        tarjeta.add(lv);
        visVence = Componentes.texto("", Tema.sans(Font.BOLD, 14), Tema.BLANCO);
        visVence.setBounds(360, 206, 100, 20);
        tarjeta.add(visVence);

        etiqueta("Número de tarjeta", Tema.sans(Font.PLAIN, 12), Tema.GRIS_TEXTO, 40, 488);
        poner(numero, 40, 510, 520, 52);
        etiqueta("Vencimiento (MM/AA)", Tema.sans(Font.PLAIN, 12), Tema.GRIS_TEXTO, 40, 582);
        poner(vence, 40, 604, 250, 52);
        etiqueta("CVV", Tema.sans(Font.PLAIN, 12), Tema.GRIS_TEXTO, 310, 582);
        poner(cvv, 310, 604, 250, 52);
        etiqueta("Pago simulado: no se realiza ningún cargo real.", Tema.sans(Font.PLAIN, 12),
                Tema.GRIS_TEXTO, 40, 672);
        actualizarTarjeta();

        // Resumen de compra
        Componentes.PanelRedondeado res =
                new Componentes.PanelRedondeado(Tema.MENTA_CLARO, null, 24);
        poner(res, 620, 198, 600, 420);
        fila(res, "Resumen de compra", null, 26, Tema.sans(Font.BOLD, 18));
        int y = 80;
        for (LineaCarrito l : s.getCarrito()) {
            fila(res, l.getItem().getNombre() + " (" + l.getVariante().talla + ") x"
                    + l.getCantidad(), Tema.soles(l.getSubtotal()), y, Tema.sans(Font.PLAIN, 15));
            y += 34;
            if (y > 290) {
                break;
            }
        }
        fila(res, "Envío a Lima", s.getEnvio() == 0 ? "Gratis" : Tema.soles(s.getEnvio()), y,
                Tema.sans(Font.PLAIN, 15));
        javax.swing.JPanel sep = new javax.swing.JPanel();
        sep.setBackground(Tema.BORDE.darker());
        sep.setBounds(28, y + 42, 544, 1);
        res.add(sep);
        fila(res, "Total", Tema.soles(s.getTotal()), y + 62, Tema.sans(Font.BOLD, 20));

        pagar = poner(new Componentes.BotonPildora("Pagar " + Tema.soles(s.getTotal()),
                Componentes.EstiloBoton.PRIMARIO, Tema.sans(Font.BOLD, 16), this::pagar),
                620, 644, 600, 60);
        estadoPago = etiqueta("", Tema.sans(Font.PLAIN, 13), Tema.GRIS_TEXTO, 620, 720);
        mostrarEstado(null);
        setAlto(780);
    }

    private void fila(javax.swing.JPanel p, String izq, String der, int y, Font f) {
        JLabel a = Componentes.texto(izq, f, Tema.AZUL_NOCHE);
        a.setBounds(28, y, 400, a.getPreferredSize().height);
        p.add(a);
        if (der != null) {
            JLabel b = Componentes.texto(der, f, Tema.AZUL_NOCHE);
            b.setHorizontalAlignment(JLabel.RIGHT);
            b.setBounds(400, y, 172, b.getPreferredSize().height);
            p.add(b);
        }
    }

    private void mostrarEstado(String actual) {
        String[] estados = {"Pendiente", "Procesado", "Pagado"};
        StringBuilder sb = new StringBuilder("<html>Estado del pago: ");
        for (int i = 0; i < estados.length; i++) {
            if (i > 0) {
                sb.append(" → ");
            }
            if (estados[i].equals(actual)) {
                sb.append("<b style='color:#2A2F63'>").append(estados[i]).append("</b>");
            } else {
                sb.append(estados[i]);
            }
        }
        sb.append("</html>");
        estadoPago.setText(sb.toString());
        estadoPago.setSize(500, estadoPago.getPreferredSize().height);
    }

    private static String soloDigitos(String s) {
        return s.replaceAll("\\D", "");
    }

    private void actualizarTarjeta() {
        if (visNumero == null) {
            return;
        }
        String d = soloDigitos(numero.getText());
        String ult = d.length() >= 4 ? d.substring(d.length() - 4) : d;
        visNumero.setText("**** **** **** " + (ult.isEmpty() ? "····" : ult));
        visMarca.setText(marca(d));
        String v = soloDigitos(vence.getText());
        visVence.setText(v.length() >= 4 ? v.substring(0, 2) + "/" + v.substring(2, 4) : "MM/AA");
    }

    private static String marca(String d) {
        if (d.startsWith("4")) {
            return "VISA";
        }
        if (d.matches("^(5[1-5]|2[2-7]).*")) {
            return "MASTERCARD";
        }
        if (d.matches("^3[47].*")) {
            return "AMEX";
        }
        return "TARJETA";
    }

    /** Algoritmo de Luhn para validar el número de tarjeta. */
    static boolean luhn(String d) {
        int suma = 0;
        boolean doble = false;
        for (int i = d.length() - 1; i >= 0; i--) {
            int n = d.charAt(i) - '0';
            if (doble) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            suma += n;
            doble = !doble;
        }
        return suma % 10 == 0;
    }

    private void pagar() {
        if (procesando) {
            return;
        }
        List<String> errores = new ArrayList<>();
        String d = soloDigitos(numero.getText());
        boolean okNum = d.length() >= 13 && d.length() <= 19 && luhn(d);
        Componentes.marcarError(numero, !okNum);
        if (!okNum) {
            errores.add("• Número de tarjeta no válido.");
        }
        String v = soloDigitos(vence.getText());
        boolean okVence = false;
        if (v.length() == 4) {
            int mes = Integer.parseInt(v.substring(0, 2));
            int anio = 2000 + Integer.parseInt(v.substring(2));
            okVence = mes >= 1 && mes <= 12
                    && !YearMonth.of(anio, mes).isBefore(YearMonth.now());
        }
        Componentes.marcarError(vence, !okVence);
        if (!okVence) {
            errores.add("• Fecha de vencimiento no válida o vencida (MM/AA).");
        }
        String c = new String(cvv.getPassword());
        int largoCvv = d.matches("^3[47].*") ? 4 : 3;
        boolean okCvv = c.matches("\\d{" + largoCvv + "}");
        Componentes.marcarError(cvv, !okCvv);
        if (!okCvv) {
            errores.add("• El CVV debe tener " + largoCvv + " dígitos.");
        }
        if (!errores.isEmpty()) {
            JOptionPane.showMessageDialog(app, String.join("\n", errores),
                    "Datos de la tarjeta", JOptionPane.WARNING_MESSAGE);
            return;
        }

        procesando = true;
        pagar.setHabilitado(false);
        pagar.setTexto("Procesando pago…");
        String marca = marca(d);
        String ult4 = d.substring(d.length() - 4);
        new SwingWorker<Void, String>() {
            @Override
            protected Void doInBackground() throws Exception {
                app.getSesion().procesarCompra(
                        marca.substring(0, 1) + marca.substring(1).toLowerCase(Locale.ROOT),
                        ult4, this::publish);
                return null;
            }

            @Override
            protected void process(List<String> chunks) {
                mostrarEstado(chunks.get(chunks.size() - 1));
            }

            @Override
            protected void done() {
                procesando = false;
                try {
                    get();
                    cvv.setText("");
                    app.getSesion().iniciarSimulacionEnvio(() -> SwingUtilities.invokeLater(() -> {
                        if (App.SEGUIMIENTO.equals(app.getActual())) {
                            app.refrescarActual();
                        }
                    }));
                    app.mostrar(App.CONFIRMACION);
                } catch (Exception ex) {
                    Throwable causa = ex.getCause() != null ? ex.getCause() : ex;
                    mostrarEstado(null);
                    pagar.setHabilitado(true);
                    pagar.setTexto("Pagar " + Tema.soles(app.getSesion().getTotal()));
                    JOptionPane.showMessageDialog(app,
                            "No se pudo completar el pago:\n" + causa.getMessage(),
                            "Pago rechazado", JOptionPane.ERROR_MESSAGE);
                    app.refrescarActual();
                }
            }
        }.execute();
    }
}
