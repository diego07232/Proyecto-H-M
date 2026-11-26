package com.hm.ventas.ui;

import com.hm.ventas.conexion;
import com.hm.ventas.datos.TiendaRepositorio;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;

/**
 * Aplicación de escritorio "H&M Ventas Online".
 * Implementa las 7 pantallas del prototipo de Figma:
 * 1. Catálogo, 2. Detalle, 3. Carrito, 4. Checkout, 5. Pago con tarjeta,
 * 6. Confirmación y 7. Seguimiento del envío, más el menú "Cuenta".
 */
public class App extends JFrame {

    public static final String CATALOGO = "catalogo";
    public static final String DETALLE = "detalle";
    public static final String CARRITO = "carrito";
    public static final String CHECKOUT = "checkout";
    public static final String PAGO = "pago";
    public static final String CONFIRMACION = "confirmacion";
    public static final String SEGUIMIENTO = "seguimiento";

    private final TiendaRepositorio repo;
    private final Sesion sesion;

    private final CardLayout tarjetas = new CardLayout();
    private final JPanel contenedor = new JPanel(tarjetas);
    private final Map<String, Pantalla> pantallas = new LinkedHashMap<>();
    private final Map<String, JScrollPane> scrolls = new LinkedHashMap<>();
    private String actual = CATALOGO;
    private String anterior = CATALOGO;

    private Componentes.BotonPildora botonCarrito;
    private JTextField buscador;
    private JLabel cuenta;
    private final JLabel estadoBD = new JLabel();
    private final JLabel aviso = new JLabel();
    private Timer timerAviso;

    final PantallaCatalogo catalogo;
    final PantallaDetalle detalle;

    public App(TiendaRepositorio repo) {
        super("H&M Ventas Online");
        this.repo = repo;
        this.sesion = new Sesion(repo);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout());
        raiz.setBackground(Tema.FONDO);
        raiz.add(crearBarraNavegacion(), BorderLayout.NORTH);
        raiz.add(contenedor, BorderLayout.CENTER);
        raiz.add(crearBarraEstado(), BorderLayout.SOUTH);
        setContentPane(raiz);

        catalogo = new PantallaCatalogo(this);
        detalle = new PantallaDetalle(this);
        registrar(CATALOGO, catalogo);
        registrar(DETALLE, detalle);
        registrar(CARRITO, new PantallaCarrito(this));
        registrar(CHECKOUT, new PantallaCheckout(this));
        registrar(PAGO, new PantallaPago(this));
        registrar(CONFIRMACION, new PantallaConfirmacion(this));
        registrar(SEGUIMIENTO, new PantallaSeguimiento(this));

        Rectangle pantalla = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds();
        setSize(Math.min(1316, pantalla.width), Math.min(1000, pantalla.height));
        setMinimumSize(new Dimension(900, 600));
        setLocationRelativeTo(null);
        mostrar(CATALOGO);
    }

    private void registrar(String nombre, Pantalla p) {
        // Centra el lienzo de 1280 px dentro de la ventana
        JPanel centrado = new JPanel(new GridBagLayout());
        centrado.setBackground(Tema.FONDO);
        java.awt.GridBagConstraints gc = new java.awt.GridBagConstraints();
        gc.anchor = java.awt.GridBagConstraints.NORTH;
        gc.weightx = 1;
        gc.weighty = 1;
        centrado.add(p, gc);
        JScrollPane sp = new JScrollPane(centrado);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.getVerticalScrollBar().setUnitIncrement(24);
        sp.getHorizontalScrollBar().setUnitIncrement(24);
        sp.getViewport().setBackground(Tema.FONDO);
        contenedor.add(sp, nombre);
        pantallas.put(nombre, p);
        scrolls.put(nombre, sp);
    }

    public TiendaRepositorio getRepo() { return repo; }
    public Sesion getSesion() { return sesion; }

    public void mostrar(String nombre) {
        if (!nombre.equals(actual)) {
            anterior = actual;
        }
        actual = nombre;
        Pantalla p = pantallas.get(nombre);
        p.refrescar();
        p.revalidate();
        p.repaint();
        tarjetas.show(contenedor, nombre);
        SwingUtilities.invokeLater(() -> scrolls.get(nombre).getVerticalScrollBar().setValue(0));
        actualizarCarrito();
    }

    public void volver() {
        mostrar(anterior == null || anterior.equals(actual) ? CATALOGO : anterior);
    }

    public void refrescarActual() {
        Pantalla p = pantallas.get(actual);
        p.refrescar();
        p.revalidate();
        p.repaint();
        actualizarCarrito();
    }

    public String getActual() { return actual; }

    public void actualizarCarrito() {
        if (botonCarrito != null) {
            botonCarrito.setTexto("Carrito (" + sesion.getUnidadesCarrito() + ")");
        }
    }

    /** Muestra un mensaje breve en la barra inferior. */
    public void avisar(String mensaje) {
        aviso.setText(mensaje);
        if (timerAviso != null) {
            timerAviso.stop();
        }
        timerAviso = new Timer(3500, e -> aviso.setText(""));
        timerAviso.setRepeats(false);
        timerAviso.start();
    }

    // ------------------------------------------------------------------
    // Barra de navegación (igual en todas las pantallas)
    // ------------------------------------------------------------------

    private JComponent crearBarraNavegacion() {
        JPanel barra = new JPanel(new GridBagLayout());
        barra.setBackground(Tema.AZUL_NOCHE);
        barra.setPreferredSize(new Dimension(Pantalla.ANCHO, 72));

        JPanel lienzo = new JPanel(null);
        lienzo.setOpaque(false);
        lienzo.setPreferredSize(new Dimension(Pantalla.ANCHO, 72));

        JLabel logo = Componentes.texto("H&M", Tema.serif(34), Tema.BLANCO);
        logo.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logo.setBounds(40, 14, 110, 44);
        logo.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                catalogo.irA(null, null);
            }
        });
        lienzo.add(logo);

        JLabel catalogoCompleto = Componentes.enlace("Todo el catálogo", Tema.sans(Font.PLAIN, 15),
                Tema.BLANCO, () -> catalogo.irA(null, null));
        catalogoCompleto.setBounds(200, 26, 160, 20);
        lienzo.add(catalogoCompleto);
        JLabel ofertas = Componentes.enlace("Ofertas", Tema.sans(Font.PLAIN, 15),
                Tema.BLANCO, () -> catalogo.irASeccion("Ofertas"));
        ofertas.setBounds(390, 26, 80, 20);
        lienzo.add(ofertas);

        Componentes.PanelRedondeado cajaBuscar =
                new Componentes.PanelRedondeado(Tema.AZUL_MEDIO, null, 40);
        cajaBuscar.setBounds(640, 16, 300, 40);
        buscador = new JTextField();
        buscador.setOpaque(false);
        buscador.setBorder(BorderFactory.createEmptyBorder());
        buscador.setForeground(Tema.BLANCO);
        buscador.setCaretColor(Tema.BLANCO);
        buscador.setFont(Tema.sans(Font.PLAIN, 14));
        buscador.setBounds(18, 6, 266, 28);
        buscador.addActionListener(e -> catalogo.buscar(buscador.getText()));
        JLabel placeholder = Componentes.texto("Buscar prendas", Tema.sans(Font.PLAIN, 14),
                new Color(0xDCE6F0));
        placeholder.setBounds(18, 6, 266, 28);
        buscador.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            private void act() {
                placeholder.setVisible(buscador.getText().isEmpty());
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) { act(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { act(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { act(); }
        });
        buscador.setToolTipText("Escribe y presiona Enter");
        cajaBuscar.add(buscador);
        cajaBuscar.add(placeholder);
        lienzo.add(cajaBuscar);

        botonCarrito = new Componentes.BotonPildora("Carrito (0)", Componentes.EstiloBoton.MENTA,
                Tema.sans(Font.BOLD, 14), () -> mostrar(CARRITO));
        botonCarrito.setBounds(1000, 16, 130, 40);
        lienzo.add(botonCarrito);

        cuenta = Componentes.enlace("Cuenta", Tema.sans(Font.PLAIN, 15), Tema.BLANCO,
                this::mostrarMenuCuenta);
        cuenta.setBounds(1160, 26, 70, 20);
        lienzo.add(cuenta);

        barra.add(lienzo);
        return barra;
    }

    public void limpiarBuscador() {
        buscador.setText("");
    }

    private JComponent crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(new Color(0xF4FAFA));
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Tema.BORDE),
                BorderFactory.createEmptyBorder(4, 14, 4, 14)));
        estadoBD.setFont(Tema.sans(Font.PLAIN, 12));
        if (repo.isConectado()) {
            estadoBD.setForeground(new Color(0x2E7D5B));
            estadoBD.setText("● Conectado a MySQL — " + conexion.getUrl().split("\\?")[0]
                    + "  ·  " + repo.getItems().size() + " artículos en catálogo");
        } else {
            estadoBD.setForeground(new Color(0xB26A00));
            estadoBD.setText("● Modo demostración (sin MySQL): " + resumir(repo.getMotivoSinConexion())
                    + "  — revisa db.properties");
            estadoBD.setToolTipText(repo.getMotivoSinConexion());
        }
        aviso.setFont(Tema.sans(Font.BOLD, 12));
        aviso.setForeground(Tema.AZUL_NOCHE);
        barra.add(estadoBD, BorderLayout.WEST);
        barra.add(aviso, BorderLayout.EAST);
        return barra;
    }

    private static String resumir(String s) {
        if (s == null) {
            return "";
        }
        String una = s.replace('\n', ' ');
        return una.length() > 90 ? una.substring(0, 90) + "…" : una;
    }

    // ------------------------------------------------------------------
    // Menú "Cuenta" (overlay del prototipo)
    // ------------------------------------------------------------------

    private void mostrarMenuCuenta() {
        JPopupMenu popup = new JPopupMenu();
        popup.setBorder(BorderFactory.createEmptyBorder());
        Componentes.PanelRedondeado panel =
                new Componentes.PanelRedondeado(Tema.BLANCO, Tema.BORDE.darker(), 18) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        Graphics2D g2 = (Graphics2D) g;
                        g2.setColor(Tema.BLANCO);
                        g2.fillRect(0, 0, getWidth(), getHeight());
                        super.paintComponent(g);
                    }
                };
        panel.setPreferredSize(new Dimension(280, 250));

        JLabel titulo = Componentes.texto("Mi cuenta", Tema.sans(Font.BOLD, 16), Tema.AZUL_NOCHE);
        titulo.setBounds(20, 16, 240, 22);
        panel.add(titulo);
        String quien = sesion.getEntrega().correo.isBlank() ? "Invitado"
                : sesion.getEntrega().nombreCompleto;
        JLabel sub = Componentes.texto(quien, Tema.sans(Font.PLAIN, 12), Tema.GRIS_TEXTO);
        sub.setBounds(110, 19, 160, 18);
        panel.add(sub);

        Object[][] opciones = {
            {"Mis pedidos", 60, (Runnable) () -> Dialogos.misPedidos(this)},
            {"Mis datos", 100, (Runnable) () -> Dialogos.misDatos(this)},
            {"Cerrar sesión", 140, (Runnable) this::cerrarSesion},
        };
        for (Object[] o : opciones) {
            Runnable r = (Runnable) o[2];
            JLabel l = Componentes.enlace((String) o[0], Tema.sans(Font.PLAIN, 15),
                    Tema.AZUL_NOCHE, () -> {
                        popup.setVisible(false);
                        r.run();
                    });
            l.setBounds(20, (Integer) o[1], 240, 22);
            panel.add(l);
        }
        JPanel sep = new JPanel();
        sep.setBackground(Tema.BORDE);
        sep.setBounds(20, 182, 240, 1);
        panel.add(sep);
        JLabel cerrar = Componentes.enlace("Cerrar", Tema.sans(Font.PLAIN, 14), Tema.GRIS_TEXTO,
                () -> popup.setVisible(false));
        cerrar.setBounds(20, 196, 100, 20);
        panel.add(cerrar);

        popup.add(panel);
        popup.show(cuenta, cuenta.getWidth() - 280, cuenta.getHeight() + 18);
    }

    private void cerrarSesion() {
        int r = JOptionPane.showConfirmDialog(this,
                "¿Cerrar sesión? Se vaciará el carrito y tus datos de entrega.",
                "Cerrar sesión", JOptionPane.OK_CANCEL_OPTION);
        if (r == JOptionPane.OK_OPTION) {
            sesion.cerrarSesion();
            mostrar(CATALOGO);
            avisar("Sesión cerrada");
        }
    }

    // ------------------------------------------------------------------
    // Arranque
    // ------------------------------------------------------------------

    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {
        }
        UIManager.put("ToolTip.font", Tema.sans(Font.PLAIN, 12));

        TiendaRepositorio repo = new TiendaRepositorio();
        if (args.length == 1 && "--verificar".equals(args[0])) {
            repo.cargar();
            if (!repo.isConectado()) {
                System.err.println("Error al cargar tienda_online: " + repo.getMotivoSinConexion());
                System.exit(1);
            }
            long imagenes = repo.getItems().stream().filter(it -> it.getImagen() != null).count();
            int variantes = repo.getItems().stream().mapToInt(it -> it.getVariantes().size()).sum();
            System.out.printf("MySQL conectado. Categorías: %d. Tarjetas: %d. Variantes: %d. Imágenes legibles: %d.%n",
                    repo.getCategorias().size(), repo.getItems().size(), variantes, imagenes);
            if (imagenes == 0 || imagenes != repo.getItems().size()) {
                System.exit(2);
            }
            return;
        }
        while (true) {
            repo.cargar();
            if (repo.isConectado()) {
                break;
            }
            String mensaje = "No se pudo cargar tienda_online. Comprueba que MySQL esté iniciado, "
                    + "que ejecutaste los scripts de la carpeta sql y que db.properties "
                    + "tenga el puerto, usuario y contraseña correctos.\n\n" + repo.getMotivoSinConexion();
            Object[] opciones = {"Reintentar", "Cerrar"};
            if (JOptionPane.showOptionDialog(null, mensaje, "Conexión con la tienda",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                    opciones, opciones[0]) != 0) {
                return;
            }
        }
        System.out.println("Conectado a MySQL: " + repo.getItems().size() + " artículos cargados.");

        SwingUtilities.invokeLater(() -> {
            App app = new App(repo);
            app.setVisible(true);
        });
    }
}
