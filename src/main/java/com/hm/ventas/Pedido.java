/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */

import java.util.ArrayList;
import java.util.List;

public class Pedido {

    private int idPedido;
    private cliente cliente;
    private String fecha;
    private String estado;
    private final List<DetallePedido> detalles;

    public Pedido(int idPedido, cliente cliente, String fecha) {
        if (cliente == null) {
            throw new IllegalArgumentException(
                    "El pedido debe tener un cliente.");
        }

        this.idPedido = idPedido;
        this.cliente = cliente;
        this.fecha = fecha;
        this.estado = "Pendiente";
        this.detalles = new ArrayList<>();
        contadorPedidos++;
    }

    public int getIdPedido() {
        return idPedido;
    }

    public cliente getCliente() {
        return cliente;
    }

    public String getFecha() {
        return fecha;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public void agregarDetalle(DetallePedido detalle) {
        if (detalle != null) {
            detalles.add(detalle);
        }
    }

    public double calcularTotal() {
        double total = 0;

        for (DetallePedido detalle : detalles) {
            total += detalle.calcularSubtotal();
        }

        return total;
    }

    public void mostrarPedido() {
        System.out.println("Pedido: " + idPedido);
        System.out.println("Cliente: " + cliente.getNombre());
        System.out.println("Fecha: " + fecha);
        System.out.println("Estado: " + estado);

        for (DetallePedido detalle : detalles) {
            detalle.mostrarDetalle();
        }

        System.out.println("Total: S/ " + calcularTotal());
    }
    
    private static int contadorPedidos = 0;
    
    public static int getContadorPedidos() {
    return contadorPedidos;
    }
}
