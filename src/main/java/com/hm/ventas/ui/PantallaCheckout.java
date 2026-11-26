package com.hm.ventas.ui;

import com.hm.ventas.datos.TiendaRepositorio;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

/** Pantalla "4. Checkout (entrega y comprobante)". */
public class PantallaCheckout extends Pantalla {

    private final JTextField nombre = Componentes.campo("");
    private final JTextField telefono = Componentes.campo("");
    private final JTextField correo = Componentes.campo("");
    private final JTextField direccion = Componentes.campo("");
    private final JTextField distrito = Componentes.campo("");
    private final JTextField referencia = Componentes.campo("");
    private final JTextField dni = Componentes.campo("");
    private final JTextField ruc = Componentes.campo("");
    private final JTextField razon = Componentes.campo("");

    public PantallaCheckout(App app) {
        super(app);
    }

    @Override
    public void refrescar() {
        removeAll();
        Sesion s = app.getSesion();
        if (s.getCarrito().isEmpty()) {
            botonAtras(() -> app.mostrar(App.CARRITO));
            etiqueta("Tu carrito está vacío.", Tema.sans(Font.PLAIN, 17), Tema.GRIS_TEXTO, 40, 90);
            return;
        }
        TiendaRepositorio.DatosEntrega d = s.getEntrega();
        nombre.setText(d.nombreCompleto);
        telefono.setText(d.telefono);
        correo.setText(d.correo);
        direccion.setText(d.direccion);
        distrito.setText(d.distrito);
        referencia.setText(d.referencia);
        dni.setText(s.getDni());
        ruc.setText(s.getRuc());
        razon.setText(s.getRazonSocial());

        botonAtras(() -> {
            guardar();
            app.mostrar(App.CARRITO);
        });
        pasos(s.isFactura() ? 2 : 1);

        etiqueta("Datos de entrega", Tema.sans(Font.BOLD, 20), Tema.AZUL_NOCHE, 40, 140);
        campo("Nombre completo", nombre, 40, 178, 300);
        campo("Teléfono", telefono, 360, 178, 300);
        campo("Correo electrónico", correo, 40, 268, 620);
        campo("Dirección", direccion, 40, 358, 620);
        campo("Distrito", distrito, 40, 448, 300);
        campo("Referencia (opcional)", referencia, 360, 448, 300);

        // Tipo de comprobante
        etiqueta("Tipo de comprobante", Tema.sans(Font.BOLD, 20), Tema.AZUL_NOCHE, 720, 140);
        opcionComprobante("Boleta", "Con DNI", !s.isFactura(), 720, false);
        opcionComprobante("Factura", "Con RUC", s.isFactura(), 990, true);
        if (s.isFactura()) {
            campo("RUC (11 dígitos)", ruc, 720, 318, 520);
            campo("Razón social", razon, 720, 408, 520);
        } else {
            campo("DNI (8 dígitos)", dni, 720, 318, 520);
        }

        // Resumen de pago
        Componentes.PanelRedondeado res = new Componentes.PanelRedondeado(Tema.MENTA_CLARO, null, 20);
        poner(res, 40, 578, 620, 100);
        JLabel t1 = Componentes.texto("Total a pagar", Tema.sans(Font.PLAIN, 14), Tema.GRIS_TEXTO);
        t1.setBounds(24, 18, 200, 20);
        res.add(t1);
        JLabel t2 = Componentes.texto(Tema.soles(s.getTotal()), Tema.sans(Font.BOLD, 28),
                Tema.AZUL_NOCHE);
        t2.setBounds(24, 42, 300, 36);
        res.add(t2);
        JLabel t3 = Componentes.texto(s.getEnvio() == 0 ? "Envío a Lima gratis"
                : "Incluye envío a Lima (" + Tema.soles(s.getEnvio()) + ")",
                Tema.sans(Font.PLAIN, 13), Tema.GRIS_TEXTO);
        t3.setBounds(340, 50, 270, 20);
        res.add(t3);

        poner(new Componentes.BotonPildora("Continuar al pago", Componentes.EstiloBoton.PRIMARIO,
                Tema.sans(Font.BOLD, 16), this::continuar), 720, 578, 520, 60);
        setAlto(720);
    }

    private void campo(String titulo, JTextField f, int x, int y, int w) {
        etiqueta(titulo, Tema.sans(Font.PLAIN, 12), Tema.GRIS_TEXTO, x, y);
        Componentes.marcarError(f, false);
        poner(f, x, y + 22, w, 52);
    }

    private void opcionComprobante(String titulo, String sub, boolean sel, int x, boolean esFactura) {
        Componentes.PanelRedondeado p = new Componentes.PanelRedondeado(
                sel ? Tema.MENTA_CLARO : Tema.BLANCO,
                sel ? Tema.AZUL_NOCHE : Tema.BORDE.darker(), 18);
        if (sel) {
            p.setColores(Tema.MENTA_CLARO, Tema.AZUL_NOCHE, 2f);
        }
        p.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JLabel a = Componentes.texto(titulo, Tema.sans(Font.BOLD, 20), Tema.AZUL_NOCHE);
        a.setBounds(20, 22, 200, 28);
        p.add(a);
        JLabel b = Componentes.texto(sub, Tema.sans(Font.PLAIN, 13), Tema.GRIS_TEXTO);
        b.setBounds(20, 58, 200, 20);
        p.add(b);
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                guardar();
                app.getSesion().setFactura(esFactura);
                app.refrescarActual();
            }
        });
        poner(p, x, 178, 250, 110);
    }

    private void guardar() {
        Sesion s = app.getSesion();
        TiendaRepositorio.DatosEntrega d = s.getEntrega();
        d.nombreCompleto = nombre.getText().trim();
        d.telefono = telefono.getText().trim();
        d.correo = correo.getText().trim();
        d.direccion = direccion.getText().trim();
        d.distrito = distrito.getText().trim();
        d.referencia = referencia.getText().trim();
        s.setDni(dni.getText().trim());
        s.setRuc(ruc.getText().trim());
        s.setRazonSocial(razon.getText().trim());
    }

    private void continuar() {
        guardar();
        Sesion s = app.getSesion();
        List<String> errores = new ArrayList<>();
        validar(nombre, nombre.getText().trim().length() >= 3, "Ingresa tu nombre completo.", errores);
        validar(telefono, telefono.getText().replaceAll("[\\s-]", "").matches("9\\d{8}"),
                "El teléfono debe tener 9 dígitos y empezar con 9.", errores);
        validar(correo, correo.getText().trim().matches("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$"),
                "Ingresa un correo electrónico válido.", errores);
        validar(direccion, direccion.getText().trim().length() >= 5, "Ingresa la dirección.", errores);
        validar(distrito, distrito.getText().trim().length() >= 3, "Ingresa el distrito.", errores);
        if (s.isFactura()) {
            validar(ruc, ruc.getText().trim().matches("(10|15|17|20)\\d{9}"),
                    "El RUC debe tener 11 dígitos y empezar con 10, 15, 17 o 20.", errores);
            validar(razon, razon.getText().trim().length() >= 3, "Ingresa la razón social.", errores);
        } else {
            validar(dni, dni.getText().trim().matches("\\d{8}"), "El DNI debe tener 8 dígitos.",
                    errores);
        }
        if (!errores.isEmpty()) {
            JOptionPane.showMessageDialog(app, String.join("\n", errores),
                    "Revisa tus datos", JOptionPane.WARNING_MESSAGE);
            return;
        }
        app.mostrar(App.PAGO);
    }

    private static void validar(JTextField f, boolean ok, String msg, List<String> errores) {
        Componentes.marcarError(f, !ok);
        if (!ok) {
            errores.add("• " + msg);
        }
    }
}
