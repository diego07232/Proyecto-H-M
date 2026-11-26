/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */

public class Factura extends Comprobante implements GenerableComprobante {

    private String ruc;
    private String razonSocial;
    

    public Factura(int idComprobante, String fecha,
                   double monto, String ruc,
                   String razonSocial) {
        super(idComprobante, fecha, monto);
        this.ruc = ruc;
        this.razonSocial = razonSocial;
    }

    public String getRuc() {
        return ruc;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    @Override
    public void emitir() {
        System.out.println("Emitiendo factura.");
        System.out.println("RUC: " + ruc);
        System.out.println("Razón social: " + razonSocial);
        mostrarDatos();
    }
}
