package com.hm.ventas.ui;

import com.hm.ventas.Boleta;
import com.hm.ventas.Comprobante;
import com.hm.ventas.DetallePedido;
import com.hm.ventas.Envio;
import com.hm.ventas.Factura;
import com.hm.ventas.Pago;
import com.hm.ventas.Pedido;
import com.hm.ventas.Producto;
import com.hm.ventas.Repartidor;
import com.hm.ventas.cliente;
import com.hm.ventas.datos.ItemCatalogo;
import com.hm.ventas.datos.LineaCarrito;
import com.hm.ventas.datos.TiendaRepositorio;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.Timer;

/**
 * Estado de la compra en curso: carrito, datos de entrega, comprobante,
 * pago y envío. Usa las clases del modelo del proyecto (cliente, Pedido,
 * DetallePedido, Pago, Boleta, Factura, Envio, Repartidor).
 */
public class Sesion {

    public static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    public static final DateTimeFormatter HORA = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private final TiendaRepositorio repo;
    private final List<LineaCarrito> carrito = new ArrayList<>();
    private final TiendaRepositorio.DatosEntrega entrega = new TiendaRepositorio.DatosEntrega();

    // Comprobante elegido en el checkout
    private boolean factura;
    private String dni = "12345678";
    private String ruc = "";
    private String razonSocial = "";

    // Resultado de la última compra
    private Pedido pedido;
    private Pago pago;
    private Comprobante comprobante;
    private Envio envio;
    private double totalPagado;
    private double envioPagado;
    private LocalDateTime fechaCompra;
    private List<String[]> productosComprados = new ArrayList<>();
    private TiendaRepositorio.DatosEntrega entregaCompra;
    private final List<String[]> historialEnvio = new ArrayList<>();
    private LocalDateTime horaAsignado;
    private LocalDateTime horaEnCamino;
    private Timer simulador;
    private Runnable alCambiarEnvio = () -> { };

    private final List<Repartidor> repartidores = new ArrayList<>(List.of(
            new Repartidor(1, "Luis Ramírez", "999 888 777"),
            new Repartidor(2, "María Quispe", "988 777 666"),
            new Repartidor(3, "Jorge Huamán", "977 666 555")));

    public Sesion(TiendaRepositorio repo) {
        this.repo = repo;
        cargarClientePorDefecto();
    }

    /** Cliente de ejemplo del prototipo (el mismo que usa Main.java). */
    public final void cargarClientePorDefecto() {
        entrega.nombreCompleto = "Carlos Pérez";
        entrega.telefono = "987 654 321";
        entrega.correo = "carlos@gmail.com";
        entrega.direccion = "Av. Abancay 123";
        entrega.distrito = "Cercado de Lima";
        entrega.referencia = "Frente al parque";
    }

    public void cerrarSesion() {
        carrito.clear();
        entrega.nombreCompleto = "";
        entrega.telefono = "";
        entrega.correo = "";
        entrega.direccion = "";
        entrega.distrito = "";
        entrega.referencia = "";
        dni = "";
    }

    public TiendaRepositorio getRepo() { return repo; }
    public TiendaRepositorio.DatosEntrega getEntrega() { return entrega; }

    // ------------------------------------------------------------------
    // Carrito
    // ------------------------------------------------------------------

    public List<LineaCarrito> getCarrito() { return carrito; }

    public int getUnidadesCarrito() {
        int n = 0;
        for (LineaCarrito l : carrito) {
            n += l.getCantidad();
        }
        return n;
    }

    /** Agrega al carrito; si la variante ya está, suma la cantidad (sin pasar el stock). */
    public void agregar(ItemCatalogo item, ItemCatalogo.Variante v, int cantidad) {
        for (LineaCarrito l : carrito) {
            if (l.getVariante() == v) {
                l.setCantidad(l.getCantidad() + cantidad);
                return;
            }
        }
        LineaCarrito nueva = new LineaCarrito(item, v, 1);
        nueva.setCantidad(cantidad);
        carrito.add(nueva);
    }

    public void quitar(LineaCarrito l) {
        carrito.remove(l);
    }

    public double getSubtotal() {
        double s = 0;
        for (LineaCarrito l : carrito) {
            s += l.getSubtotal();
        }
        return s;
    }

    public double getEnvio() {
        return Tema.calcularEnvio(getSubtotal());
    }

    public double getTotal() {
        return getSubtotal() + getEnvio();
    }

    // ------------------------------------------------------------------
    // Comprobante
    // ------------------------------------------------------------------

    public boolean isFactura() { return factura; }
    public void setFactura(boolean f) { factura = f; }
    public String getDni() { return dni; }
    public void setDni(String d) { dni = d; }
    public String getRuc() { return ruc; }
    public void setRuc(String r) { ruc = r; }
    public String getRazonSocial() { return razonSocial; }
    public void setRazonSocial(String r) { razonSocial = r; }

    // ------------------------------------------------------------------
    // Pago
    // ------------------------------------------------------------------

    /**
     * Procesa la compra completa. Se llama desde un hilo de fondo.
     * Informa los estados del pago: Pendiente → Procesado → Pagado.
     */
    public void procesarCompra(String marcaTarjeta, String ultimos4, Consumer<String> estado)
            throws Exception {
        if (carrito.isEmpty()) {
            throw new IllegalStateException("El carrito está vacío.");
        }
        LocalDateTime ahora = LocalDateTime.now();
        double subtotal = getSubtotal();
        double costoEnvio = getEnvio();
        double total = subtotal + costoEnvio;

        // Modelo de dominio (antes de descontar stock)
        cliente cli = new cliente(0, entrega.nombreCompleto, entrega.correo, "",
                entrega.telefono, entrega.direccion + ", " + entrega.distrito);
        List<Producto> productos = new ArrayList<>();
        for (LineaCarrito l : carrito) {
            productos.add(l.aProducto());
        }

        estado.accept("Pendiente");
        Thread.sleep(700);

        // Pago "provisional" para pasar por el estado Procesado
        Pedido provisional = new Pedido(0, cli, ahora.format(FECHA));
        for (int i = 0; i < carrito.size(); i++) {
            provisional.agregarDetalle(new DetallePedido(i + 1, productos.get(i),
                    carrito.get(i).getCantidad()));
        }
        String metodo = "Tarjeta " + marcaTarjeta;
        Pago p = new Pago(0, provisional, metodo, total);
        p.procesarPago();
        if (!p.verificarPago()) {
            throw new IllegalStateException("La pasarela no pudo procesar el pago.");
        }
        estado.accept("Procesado");
        Thread.sleep(700);

        // Registro en la base de datos (cliente, dirección, venta, detalle, pago)
        String transaccion = "HM-" + System.currentTimeMillis() + "-" + ultimos4;
        int idVenta = repo.registrarVenta(entrega, carrito, costoEnvio, metodo, transaccion);

        // Objetos definitivos con el número de venta real
        Pedido ped = new Pedido(idVenta, cli, ahora.format(FECHA));
        for (int i = 0; i < carrito.size(); i++) {
            ped.agregarDetalle(new DetallePedido(i + 1, productos.get(i),
                    carrito.get(i).getCantidad()));
        }
        Pago definitivo = new Pago(idVenta, ped, metodo, total);
        definitivo.procesarPago();
        definitivo.confirmarPago();   // estado = "Pagado"
        ped.setEstado("Pagado");

        Comprobante comp = factura
                ? new Factura(idVenta, ahora.format(FECHA), total, ruc, razonSocial)
                : new Boleta(idVenta, ahora.format(FECHA), total, dni, entrega.nombreCompleto);
        comp.emitir(); // imprime el comprobante en la consola

        List<String[]> lineas = new ArrayList<>();
        for (LineaCarrito l : carrito) {
            lineas.add(new String[]{l.getItem().getNombre(), String.valueOf(l.getCantidad()),
                    Tema.soles(l.getSubtotal()), l.getItem().getColor(), l.getVariante().talla,
                    Tema.soles(l.getItem().getPrecio())});
        }

        TiendaRepositorio.DatosEntrega copia = new TiendaRepositorio.DatosEntrega();
        copia.nombreCompleto = entrega.nombreCompleto;
        copia.telefono = entrega.telefono;
        copia.correo = entrega.correo;
        copia.direccion = entrega.direccion;
        copia.distrito = entrega.distrito;
        copia.referencia = entrega.referencia;

        synchronized (this) {
            this.pedido = ped;
            this.pago = definitivo;
            this.comprobante = comp;
            this.totalPagado = total;
            this.envioPagado = costoEnvio;
            this.fechaCompra = ahora;
            this.productosComprados = lineas;
            this.entregaCompra = copia;
            this.envio = new Envio(idVenta, ped, copia.direccion + ", " + copia.distrito);
            this.horaAsignado = null;
            this.horaEnCamino = null;
            historialEnvio.clear();
            historialEnvio.add(new String[]{ahora.format(HORA), "Pedido confirmado y pagado"});
        }
        carrito.clear();
        estado.accept(definitivo.getEstado());
    }

    // ------------------------------------------------------------------
    // Seguimiento del envío (simulado: avanza un estado cada pocos segundos)
    // ------------------------------------------------------------------

    public void iniciarSimulacionEnvio(Runnable alCambiar) {
        this.alCambiarEnvio = alCambiar;
        if (simulador != null) {
            simulador.stop();
        }
        simulador = new Timer(6000, e -> avanzarEnvio());
        simulador.start();
    }

    public void setAlCambiarEnvio(Runnable r) {
        this.alCambiarEnvio = r;
    }

    /** Pendiente → Asignado → En camino → Entregado. */
    public void avanzarEnvio() {
        if (envio == null) {
            return;
        }
        LocalDateTime ahora = LocalDateTime.now();
        switch (envio.getEstado()) {
            case "Pendiente" -> {
                for (Repartidor r : repartidores) {
                    if ("Disponible".equals(r.getDisponibilidad())) {
                        envio.asignarRepartidor(r); // estado = "Asignado"
                        break;
                    }
                }
                if ("Asignado".equals(envio.getEstado())) {
                    horaAsignado = ahora;
                    historialEnvio.add(new String[]{ahora.format(HORA),
                            "Repartidor asignado: " + envio.getRepartidor().getNombre()});
                }
            }
            case "Asignado" -> {
                envio.iniciarEnvio(); // estado = "En camino"
                horaEnCamino = ahora;
                historialEnvio.add(new String[]{ahora.format(HORA), "Envío en camino"});
                repo.actualizarEstadoVenta(pedido.getIdPedido(), "enviado");
            }
            case "En camino" -> {
                envio.actualizarEstadoEnvio("Entregado");
                historialEnvio.add(new String[]{ahora.format(HORA), "Pedido entregado"});
                repo.actualizarEstadoVenta(pedido.getIdPedido(), "entregado");
                if (envio.getRepartidor() != null) {
                    envio.getRepartidor().cambiarDisponibilidad("Disponible");
                }
                if (simulador != null) {
                    simulador.stop();
                }
            }
            default -> {
                return;
            }
        }
        alCambiarEnvio.run();
    }

    // Getters del último pedido
    public Pedido getPedido() { return pedido; }
    public Pago getPago() { return pago; }
    public Comprobante getComprobante() { return comprobante; }
    public Envio getEnvio2() { return envio; }
    public double getTotalPagado() { return totalPagado; }
    public double getEnvioPagado() { return envioPagado; }
    public LocalDateTime getFechaCompra() { return fechaCompra; }
    public List<String[]> getProductosComprados() { return productosComprados; }
    public TiendaRepositorio.DatosEntrega getEntregaCompra() { return entregaCompra; }
    public List<String[]> getHistorialEnvio() { return historialEnvio; }
    public LocalDateTime getHoraAsignado() { return horaAsignado; }
    public LocalDateTime getHoraEnCamino() { return horaEnCamino; }

    public String numeroComprobante() {
        if (comprobante == null) {
            return "";
        }
        String serie = comprobante instanceof Factura ? "F001-" : "B001-";
        return serie + String.format("%08d", comprobante.getIdComprobante());
    }
}
