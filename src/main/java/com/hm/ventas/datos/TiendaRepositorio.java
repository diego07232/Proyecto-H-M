package com.hm.ventas.datos;

import com.hm.ventas.Categoria;
import com.hm.ventas.conexion;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Acceso a datos de la tienda.
 *
 * Si hay conexión con MySQL (tienda_online) lee y guarda en la base de datos.
 * Si falla la carga, la interfaz muestra el error y permite reintentar.
 */
public class TiendaRepositorio {

    /** Resumen de una venta para la ventana "Mis pedidos". */
    public static class ResumenVenta {
        public final int idVenta;
        public final LocalDateTime fecha;
        public final String estado;
        public final double total;
        public final String metodoPago;
        public final String estadoPago;

        public ResumenVenta(int idVenta, LocalDateTime fecha, String estado,
                            double total, String metodoPago, String estadoPago) {
            this.idVenta = idVenta;
            this.fecha = fecha;
            this.estado = estado;
            this.total = total;
            this.metodoPago = metodoPago;
            this.estadoPago = estadoPago;
        }
    }

    /** Datos de entrega y cliente capturados en el checkout. */
    public static class DatosEntrega {
        public String nombreCompleto = "";
        public String telefono = "";
        public String correo = "";
        public String direccion = "";
        public String distrito = "";
        public String referencia = "";
    }

    private boolean conectado;
    private String motivoSinConexion = "";
    private final List<Categoria> categorias = new ArrayList<>();
    private final List<ItemCatalogo> items = new ArrayList<>();

    // Ventas guardadas en memoria cuando no hay base de datos
    private final List<ResumenVenta> ventasMemoria = new ArrayList<>();
    private final Map<Integer, String> correoVentaMemoria = new LinkedHashMap<>();
    private int siguienteVentaMemoria = 1;

    public void cargar() {
        categorias.clear();
        items.clear();
        try (Connection cn = conexion.conectar()) {
            if (cn == null) {
                throw new SQLException(conexion.getUltimoError());
            }
            cargarDesdeBD(cn);
            conectado = true;
        } catch (SQLException e) {
            conectado = false;
            motivoSinConexion = e.getMessage() == null ? "sin conexión" : e.getMessage();
            categorias.clear();
            items.clear();
            // La interfaz solo se abre después de cargar la base correctamente.
        }
    }

    public boolean isConectado() { return conectado; }
    public String getMotivoSinConexion() { return motivoSinConexion; }
    public List<Categoria> getCategorias() { return categorias; }
    public List<ItemCatalogo> getItems() { return items; }

    // ------------------------------------------------------------------
    // Lectura del catálogo
    // ------------------------------------------------------------------

    private void cargarDesdeBD(Connection cn) throws SQLException {
        Map<Integer, Categoria> mapaCat = new LinkedHashMap<>();
        try (Statement st = cn.createStatement();
             ResultSet rs = st.executeQuery(
                     "SELECT id_categoria, nombre, descripcion FROM categoria ORDER BY id_categoria")) {
            while (rs.next()) {
                Categoria c = new Categoria(rs.getInt(1), rs.getString(2), rs.getString(3));
                mapaCat.put(c.getIdCategoria(), c);
                categorias.add(c);
            }
        }

        String sql = "SELECT p.id_producto, p.nombre, p.descripcion, p.precio, p.sku, "
                + "p.id_categoria, co.nombre AS color, t.nombre AS talla, "
                + "vp.id_variante, vp.stock "
                + "FROM variante_producto vp "
                + "JOIN producto p ON p.id_producto = vp.id_producto "
                + "JOIN talla t ON t.id_talla = vp.id_talla "
                + "JOIN color co ON co.id_color = vp.id_color "
                + "ORDER BY p.id_producto, co.id_color, t.id_talla";

        Map<String, ItemCatalogo> porClave = new LinkedHashMap<>();
        int maxId = 0;
        try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                int idProd = rs.getInt("id_producto");
                String color = rs.getString("color");
                String clave = idProd + "|" + color;
                ItemCatalogo item = porClave.get(clave);
                if (item == null) {
                    item = new ItemCatalogo(idProd, rs.getString("nombre"),
                            rs.getString("descripcion"), rs.getDouble("precio"),
                            rs.getString("sku"), mapaCat.get(rs.getInt("id_categoria")), color);
                    porClave.put(clave, item);
                }
                item.agregarVariante(rs.getInt("id_variante"), rs.getString("talla"),
                        rs.getInt("stock"));
                maxId = Math.max(maxId, idProd);
            }
        }
        items.addAll(porClave.values());

        // Una imagen por modelo y color, compartida por todas sus tallas.
        String consultaImagen = "SELECT i.id_producto, c.nombre, i.contenido "
                + "FROM imagen_producto_color i JOIN color c ON c.id_color = i.id_color";
        try (Statement st = cn.createStatement(); ResultSet rs = st.executeQuery(consultaImagen)) {
            while (rs.next()) {
                ItemCatalogo item = porClave.get(rs.getInt(1) + "|" + rs.getString(2));
                if (item != null) {
                    try (java.io.InputStream in = rs.getBinaryStream(3)) {
                        item.setImagen(javax.imageio.ImageIO.read(in));
                    } catch (java.io.IOException e) {
                        System.err.println("Imagen ilegible para " + item.getSku());
                    }
                }
            }
        }

        // Etiqueta "Nuevo": el producto más reciente de cada categoría
        Map<Integer, Integer> ultimoPorCategoria = new LinkedHashMap<>();
        for (ItemCatalogo it : items) {
            ultimoPorCategoria.merge(it.getCategoria().getIdCategoria(),
                    it.getIdProducto(), Math::max);
        }
        for (ItemCatalogo it : items) {
            it.setNuevo(ultimoPorCategoria.get(it.getCategoria().getIdCategoria())
                    == it.getIdProducto());
        }
    }

    // ------------------------------------------------------------------
    // Registro de una venta completa (cliente, dirección, venta, detalle, pago)
    // ------------------------------------------------------------------

    /**
     * Registra la compra. Devuelve el número de venta (id_venta).
     * Lanza IllegalStateException si algún producto ya no tiene stock.
     */
    public int registrarVenta(DatosEntrega datos, List<LineaCarrito> lineas,
                              double envio, String metodoPago, String transaccionId)
            throws SQLException {
        double total = envio;
        for (LineaCarrito l : lineas) {
            total += l.getSubtotal();
        }
        total = Math.round(total * 100.0) / 100.0;

        if (!conectado) {
            for (LineaCarrito l : lineas) {
                if (l.getVariante().stock < l.getCantidad()) {
                    throw new IllegalStateException("Stock insuficiente para "
                            + l.getItem().getNombre() + " talla " + l.getVariante().talla);
                }
            }
            for (LineaCarrito l : lineas) {
                l.getVariante().stock -= l.getCantidad();
            }
            int id = siguienteVentaMemoria++;
            ventasMemoria.add(0, new ResumenVenta(id, LocalDateTime.now(), "preparando",
                    total, metodoPago, "aprobado"));
            correoVentaMemoria.put(id, datos.correo.toLowerCase());
            return id;
        }

        try (Connection cn = conexion.conectar()) {
            if (cn == null) {
                throw new SQLException(conexion.getUltimoError());
            }
            cn.setAutoCommit(false);
            try {
                int idCliente = obtenerOCrearCliente(cn, datos);
                int idDireccion = crearDireccion(cn, idCliente, datos);

                // Bloquear y validar stock de cada variante
                for (LineaCarrito l : lineas) {
                    try (PreparedStatement ps = cn.prepareStatement(
                            "SELECT stock FROM variante_producto WHERE id_variante = ? FOR UPDATE")) {
                        ps.setInt(1, l.getVariante().idVariante);
                        try (ResultSet rs = ps.executeQuery()) {
                            int stock = rs.next() ? rs.getInt(1) : 0;
                            if (stock < l.getCantidad()) {
                                throw new IllegalStateException("Stock insuficiente para "
                                        + l.getItem().getNombre() + " talla "
                                        + l.getVariante().talla + " (quedan " + stock + ")");
                            }
                        }
                    }
                }

                int idVenta;
                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO venta (id_cliente, id_direccion, fecha_venta, tipo_venta, estado, total) "
                        + "VALUES (?, ?, ?, 'online', 'preparando', ?)",
                        Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, idCliente);
                    ps.setInt(2, idDireccion);
                    ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
                    ps.setDouble(4, total);
                    ps.executeUpdate();
                    idVenta = claveGenerada(ps);
                }

                for (LineaCarrito l : lineas) {
                    try (PreparedStatement ps = cn.prepareStatement(
                            "INSERT INTO detalle_venta (id_venta, id_variante, cantidad, precio_unitario, subtotal) "
                            + "VALUES (?, ?, ?, ?, ?)")) {
                        ps.setInt(1, idVenta);
                        ps.setInt(2, l.getVariante().idVariante);
                        ps.setInt(3, l.getCantidad());
                        ps.setDouble(4, l.getItem().getPrecio());
                        ps.setDouble(5, Math.round(l.getSubtotal() * 100.0) / 100.0);
                        ps.executeUpdate();
                    }
                    try (PreparedStatement ps = cn.prepareStatement(
                            "UPDATE variante_producto SET stock = stock - ? WHERE id_variante = ?")) {
                        ps.setInt(1, l.getCantidad());
                        ps.setInt(2, l.getVariante().idVariante);
                        ps.executeUpdate();
                    }
                }

                try (PreparedStatement ps = cn.prepareStatement(
                        "INSERT INTO pago (id_venta, metodo_pago, estado_pago, transaccion_pasarela_id, fecha_pago) "
                        + "VALUES (?, ?, 'aprobado', ?, ?)")) {
                    ps.setInt(1, idVenta);
                    ps.setString(2, metodoPago);
                    ps.setString(3, transaccionId);
                    ps.setTimestamp(4, Timestamp.valueOf(LocalDateTime.now()));
                    ps.executeUpdate();
                }

                cn.commit();

                // Reflejar el nuevo stock en el catálogo cargado
                for (LineaCarrito l : lineas) {
                    l.getVariante().stock -= l.getCantidad();
                }
                return idVenta;
            } catch (SQLException | RuntimeException e) {
                cn.rollback();
                throw e;
            } finally {
                cn.setAutoCommit(true);
            }
        }
    }

    private int obtenerOCrearCliente(Connection cn, DatosEntrega d) throws SQLException {
        String[] partes = separarNombre(d.nombreCompleto);
        try (PreparedStatement ps = cn.prepareStatement(
                "SELECT id_cliente FROM cliente WHERE email = ?")) {
            ps.setString(1, d.correo);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt(1);
                    try (PreparedStatement up = cn.prepareStatement(
                            "UPDATE cliente SET nombre = ?, apellido = ?, telefono = ? WHERE id_cliente = ?")) {
                        up.setString(1, partes[0]);
                        up.setString(2, partes[1]);
                        up.setString(3, d.telefono);
                        up.setInt(4, id);
                        up.executeUpdate();
                    }
                    return id;
                }
            }
        }
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO cliente (nombre, apellido, email, telefono) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, partes[0]);
            ps.setString(2, partes[1]);
            ps.setString(3, d.correo);
            ps.setString(4, d.telefono);
            ps.executeUpdate();
            return claveGenerada(ps);
        }
    }

    private int crearDireccion(Connection cn, int idCliente, DatosEntrega d) throws SQLException {
        String calle = d.direccion;
        if (!d.referencia.isBlank()) {
            calle = calle + " (Ref.: " + d.referencia + ")";
        }
        if (calle.length() > 200) {
            calle = calle.substring(0, 200);
        }
        try (PreparedStatement ps = cn.prepareStatement(
                "INSERT INTO direccion (id_cliente, calle, ciudad, estado_provincia, codigo_postal, pais) "
                + "VALUES (?, ?, ?, 'Lima', NULL, 'Perú')",
                Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idCliente);
            ps.setString(2, calle);
            ps.setString(3, d.distrito);
            ps.executeUpdate();
            return claveGenerada(ps);
        }
    }

    private static int claveGenerada(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("No se obtuvo el ID generado");
    }

    static String[] separarNombre(String completo) {
        String limpio = completo == null ? "" : completo.trim().replaceAll("\\s+", " ");
        int espacio = limpio.indexOf(' ');
        if (espacio < 0) {
            return new String[]{limpio, "-"};
        }
        return new String[]{limpio.substring(0, espacio), limpio.substring(espacio + 1)};
    }

    // ------------------------------------------------------------------
    // Estado de la venta (seguimiento del envío) y consultas
    // ------------------------------------------------------------------

    public void actualizarEstadoVenta(int idVenta, String estado) {
        if (!conectado) {
            for (int i = 0; i < ventasMemoria.size(); i++) {
                ResumenVenta r = ventasMemoria.get(i);
                if (r.idVenta == idVenta) {
                    ventasMemoria.set(i, new ResumenVenta(r.idVenta, r.fecha, estado,
                            r.total, r.metodoPago, r.estadoPago));
                }
            }
            return;
        }
        try (Connection cn = conexion.conectar()) {
            if (cn == null) {
                return;
            }
            try (PreparedStatement ps = cn.prepareStatement(
                    "UPDATE venta SET estado = ? WHERE id_venta = ?")) {
                ps.setString(1, estado);
                ps.setInt(2, idVenta);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            System.out.println("No se pudo actualizar la venta: " + e.getMessage());
        }
    }

    public List<ResumenVenta> listarVentasDe(String correo) {
        List<ResumenVenta> lista = new ArrayList<>();
        if (!conectado) {
            for (ResumenVenta r : ventasMemoria) {
                if (correo.equalsIgnoreCase(correoVentaMemoria.get(r.idVenta))) {
                    lista.add(r);
                }
            }
            return lista;
        }
        String sql = "SELECT v.id_venta, v.fecha_venta, v.estado, v.total, "
                + "pa.metodo_pago, pa.estado_pago "
                + "FROM venta v JOIN cliente c ON c.id_cliente = v.id_cliente "
                + "LEFT JOIN pago pa ON pa.id_pago = ("
                + "  SELECT MAX(p2.id_pago) FROM pago p2 WHERE p2.id_venta = v.id_venta) "
                + "WHERE c.email = ? ORDER BY v.fecha_venta DESC, v.id_venta DESC";
        try (Connection cn = conexion.conectar()) {
            if (cn == null) {
                return lista;
            }
            try (PreparedStatement ps = cn.prepareStatement(sql)) {
                ps.setString(1, correo);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        lista.add(new ResumenVenta(rs.getInt(1),
                                rs.getTimestamp(2).toLocalDateTime(), rs.getString(3),
                                rs.getDouble(4), rs.getString(5), rs.getString(6)));
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("No se pudieron leer los pedidos: " + e.getMessage());
        }
        return lista;
    }
}
