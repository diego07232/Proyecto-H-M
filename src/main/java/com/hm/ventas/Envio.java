/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */

public class Envio implements Enviable {

    private int idEnvio;
    private Pedido pedido;
    private Repartidor repartidor;
    private String direccionEntrega;
    private String estado;

    public Envio(int idEnvio, Pedido pedido,
                 String direccionEntrega) {
        if (pedido == null) {
            throw new IllegalArgumentException(
                    "El envío debe estar asociado a un pedido.");
        }

        this.idEnvio = idEnvio;
        this.pedido = pedido;
        this.direccionEntrega = direccionEntrega;
        this.estado = "Pendiente";
        this.repartidor = null;
    }

    public int getIdEnvio() {
        return idEnvio;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public Repartidor getRepartidor() {
        return repartidor;
    }

    public String getDireccionEntrega() {
        return direccionEntrega;
    }

    public String getEstado() {
        return estado;
    }

    public void asignarRepartidor(Repartidor repartidor) {
        if (repartidor != null
                && "Disponible".equals(
                        repartidor.getDisponibilidad())) {
            this.repartidor = repartidor;
            repartidor.cambiarDisponibilidad("Ocupado");
            this.estado = "Asignado";
        }
    }

    public void actualizarEstado(String estado) {
        this.estado = estado;
    }

    public void mostrarEnvio() {
        System.out.println("Envío N.°: " + idEnvio);
        System.out.println("Pedido: " + pedido.getIdPedido());
        System.out.println("Dirección: " + direccionEntrega);
        System.out.println("Estado: " + estado);

        if (repartidor != null) {
            System.out.println(
                    "Repartidor: " + repartidor.getNombre());
        }
    }
    
    @Override
    public void iniciarEnvio() {
    estado = "En camino";
    System.out.println("El envío ha comenzado.");
    }

    @Override
    public void actualizarEstadoEnvio(String nuevoEstado) {
    estado = nuevoEstado;
    System.out.println("Estado del envío: " + estado);
    }

    @Override
    public boolean verificarEntrega() {
    return "Entregado".equalsIgnoreCase(estado);
    }
}
