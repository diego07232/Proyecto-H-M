package com.hm.ventas.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** Colores y tipografías tomados del prototipo de Figma "H&M Ventas Online". */
public final class Tema {

    private Tema() {
    }

    // Paleta del prototipo
    public static final Color AZUL_NOCHE = new Color(0x2A2F63);   // barra, títulos, botones
    public static final Color AZUL_MEDIO = new Color(0x3E6D9C);   // buscador
    public static final Color MENTA = new Color(0xA7E0DB);        // botón carrito
    public static final Color MENTA_CLARO = new Color(0xE3F6F5);  // fondos de imagen / tarjetas
    public static final Color TURQUESA = new Color(0x5FB0C9);     // acentos, etiqueta Nuevo
    public static final Color BORDE = new Color(0xD6ECEE);        // bordes suaves
    public static final Color GRIS_TEXTO = new Color(0x666D80);   // texto secundario
    public static final Color GRIS_DESACTIVADO = new Color(0xA9AEBB);
    public static final Color ROJO = new Color(0xD0342C);         // Agotado / errores
    public static final Color BLANCO = Color.WHITE;
    public static final Color FONDO = Color.WHITE;

    private static final String SANS = elegirFuente("Inter", "Segoe UI", "Helvetica Neue",
            "Arial", "DejaVu Sans", Font.SANS_SERIF);
    private static final String SERIF = elegirFuente("DM Serif Display", "Playfair Display",
            "Georgia", "Times New Roman", "DejaVu Serif", Font.SERIF);

    public static Font sans(int estilo, float tam) {
        return new Font(SANS, estilo, Math.round(tam));
    }

    public static Font serif(float tam) {
        return new Font(SERIF, Font.BOLD, Math.round(tam));
    }

    private static String elegirFuente(String... candidatas) {
        Set<String> disponibles = new HashSet<>(Arrays.asList(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));
        for (String c : candidatas) {
            if (disponibles.contains(c)) {
                return c;
            }
        }
        return candidatas[candidatas.length - 1];
    }

    // Colores de las prendas (nombres de la tabla color + los del prototipo)
    private static final Map<String, Color> COLORES = Map.ofEntries(
            Map.entry("negro", new Color(0x222222)),
            Map.entry("blanco", new Color(0xFFFFFF)),
            Map.entry("azul", new Color(0x3E6D9C)),
            Map.entry("azul marino", new Color(0x2A2F63)),
            Map.entry("turquesa", new Color(0xA7E0DB)),
            Map.entry("celeste", new Color(0x5FB0C9)),
            Map.entry("gris", new Color(0x9AA0A8)),
            Map.entry("rojo", new Color(0xC2413A)),
            Map.entry("beige", new Color(0xD8C3A0)),
            Map.entry("verde", new Color(0x5E8C61)),
            Map.entry("rosado", new Color(0xE8A9BC)));

    public static Color colorDe(String nombre) {
        if (nombre == null) {
            return GRIS_DESACTIVADO;
        }
        return COLORES.getOrDefault(nombre.toLowerCase(Locale.ROOT), GRIS_DESACTIVADO);
    }

    private static final DecimalFormat FORMATO_SOLES;

    static {
        DecimalFormatSymbols s = new DecimalFormatSymbols(Locale.US);
        FORMATO_SOLES = new DecimalFormat("#,##0.00", s);
    }

    public static String soles(double monto) {
        return "S/ " + FORMATO_SOLES.format(monto);
    }

    /** Costo de envío a Lima: S/ 10, gratis desde S/ 150. */
    public static final double ENVIO_LIMA = 10.00;
    public static final double ENVIO_GRATIS_DESDE = 150.00;

    public static double calcularEnvio(double subtotal) {
        return (subtotal <= 0 || subtotal >= ENVIO_GRATIS_DESDE) ? 0 : ENVIO_LIMA;
    }
}
