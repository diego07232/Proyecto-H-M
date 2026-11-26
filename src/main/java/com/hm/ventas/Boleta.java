/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */

public class Boleta extends Comprobante implements GenerableComprobante {

    private String dni;
    private String nombreCliente;

    public Boleta(int idComprobante, String fecha,
                  double monto, String dni,
                  String nombreCliente) {
        super(idComprobante, fecha, monto);
        this.dni = dni;
        this.nombreCliente = nombreCliente;
    }

    public String getDni() {
        return dni;
    }

    public String getNombreCliente() {
        return nombreCliente;
    }

    @Override
    public void emitir() {
        System.out.println("Emitiendo boleta.");
        System.out.println("DNI: " + dni);
        System.out.println("Cliente: " + nombreCliente);
        mostrarDatos();
    }
}
