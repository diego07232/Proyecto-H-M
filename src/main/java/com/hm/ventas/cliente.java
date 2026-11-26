/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */

public class cliente extends Usuario {

    private String telefono;
    private String direccion;

    public cliente(int idUsuario, String nombre,
                   String correo, String contrasena,
                   String telefono, String direccion) {
        super(idUsuario, nombre, correo, contrasena);
        this.telefono = telefono;
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public void realizarPedido() {
        System.out.println("El cliente " + getNombre()
                + " puede realizar un pedido.");
    }
}
