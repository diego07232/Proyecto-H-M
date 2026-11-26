/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */

public class Pago implements ProcesablePago {

    private int idPago;
    private Pedido pedido;
    private String metodoPago;
    private double monto;
    private String estado;

    public Pago(int idPago, Pedido pedido,
                String metodoPago, double monto) {
        if (pedido == null || monto < 0) {
            throw new IllegalArgumentException(
                    "Datos del pago no válidos.");
        }

        this.idPago = idPago;
        this.pedido = pedido;
        this.metodoPago = metodoPago;
        this.monto = monto;
        this.estado = "Pendiente";
    }

    public int getIdPago() {
        return idPago;
    }

    public Pedido getPedido() {
        return pedido;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public double getMonto() {
        return monto;
    }

    public String getEstado() {
        return estado;
    }

    public void confirmarPago() {
        if (monto >= pedido.calcularTotal()) {
            estado = "Pagado";
        } else {
            estado = "Pago insuficiente";
        }
    }

    public void mostrarPago() {
        System.out.println("Pago N.°: " + idPago);
        System.out.println("Método: " + metodoPago);
        System.out.println("Monto: S/ " + monto);
        System.out.println("Estado: " + estado);
    }
    
    @Override
    public void procesarPago() {
    estado = "Procesado";
    System.out.println("Pago procesado mediante: " + metodoPago);
    }

    @Override
    public boolean verificarPago() {
    return "Procesado".equalsIgnoreCase(estado);
    }

}
