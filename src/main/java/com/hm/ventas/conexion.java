/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Conexión con MySQL (base de datos tienda_online).
 *
 * Los datos de acceso se leen, en este orden de prioridad:
 *  1. Variables de entorno TIENDA_DB_URL, TIENDA_DB_USER, TIENDA_DB_PASSWORD
 *  2. Archivo db.properties junto al .jar (o en la carpeta del proyecto)
 *  3. Archivo db.properties dentro de src/main/resources
 *  4. Valores por defecto de abajo
 */
public class conexion {

    private static final String URL_DEFECTO =
            "jdbc:mysql://localhost:3306/tienda_online"
            + "?useSSL=false&allowPublicKeyRetrieval=true"
            + "&serverTimezone=America/Lima&connectTimeout=3000"
            + "&characterEncoding=UTF-8";

    private static final String USER_DEFECTO = "root";
    private static final String PASSWORD_DEFECTO = "";

    private static String url;
    private static String user;
    private static String password;
    private static String ultimoError = "";


    private static void cargarConfiguracion() {
        Properties p = new Properties();

        // db.properties dentro del .jar
        try (InputStream in = conexion.class.getResourceAsStream("/db.properties")) {
            if (in != null) {
                p.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            }
        } catch (Exception ignored) {
        }

        // db.properties junto al .jar o en la carpeta del proyecto
        // (si se abre target/Ventas-1.0-SNAPSHOT.jar con doble clic)
        try {
            Path jar = Path.of(conexion.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            Path dir = Files.isDirectory(jar) ? jar : jar.getParent();
            for (Path cand : new Path[]{dir.getParent().resolve("db.properties"),
                                        dir.resolve("db.properties")}) {
                cargarSiExiste(p, cand);
            }
        } catch (Exception ignored) {
        }

        // db.properties en la carpeta desde donde se ejecuta (tiene prioridad)
        cargarSiExiste(p, Path.of("db.properties"));

        url = valor("TIENDA_DB_URL", p.getProperty("db.url"), URL_DEFECTO);
        user = valor("TIENDA_DB_USER", p.getProperty("db.user"), USER_DEFECTO);
        password = valor("TIENDA_DB_PASSWORD", p.getProperty("db.password"), PASSWORD_DEFECTO);
    }

    private static void cargarSiExiste(Properties p, Path archivo) {
        if (archivo != null && Files.exists(archivo)) {
            try (InputStream in = Files.newInputStream(archivo)) {
                p.load(new java.io.InputStreamReader(in, java.nio.charset.StandardCharsets.UTF_8));
            } catch (Exception ignored) {
            }
        }
    }

    private static String valor(String env, String prop, String defecto) {
        String e = System.getenv(env);
        if (e != null && !e.isBlank()) {
            return e;
        }
        if (prop != null && !prop.isBlank()) {
            return prop.trim();
        }
        return defecto;
    }

    public static Connection conectar() {
        try {
            cargarConfiguracion();
            Connection conexion = DriverManager.getConnection(url, user, password);
            ultimoError = "";
            return conexion;
        } catch (SQLException e) {
            ultimoError = e.getMessage();
            System.out.println("Error de conexión: " + e.getMessage());
            return null;
        }
    }

    public static String getUltimoError() {
        return ultimoError;
    }

    public static String getUrl() {
        return url;
    }
}
