/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */

public abstract class Comprobante {

    private int idComprobante;
    private String fecha;
    private double monto;
    public static final double IGV = 0.18;
    //En este caso el IGV, se trata de un impuesto que se realiza a cualquier venta de una
    //empresa que se dedique a la venta de productos

    public Comprobante(int idComprobante,
                       String fecha, double monto) {
        this.idComprobante = idComprobante;
        this.fecha = fecha;
        this.monto = monto;
    }

    public int getIdComprobante() {
        return idComprobante;
    }

    public String getFecha() {
        return fecha;
    }

    public double getMonto() {
        return monto;
    }

    public void mostrarDatos() {
        System.out.println("Comprobante: " + idComprobante);
        System.out.println("Fecha: " + fecha);
        System.out.println("Monto: S/ " + monto);
    }
    
    public abstract void emitir();
}
