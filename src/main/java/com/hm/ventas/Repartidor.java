/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */

public class Repartidor {

    private int idRepartidor;
    private String nombre;
    private String telefono;
    private String disponibilidad;

    public Repartidor(int idRepartidor, String nombre,
                      String telefono) {
        this.idRepartidor = idRepartidor;
        this.nombre = nombre;
        this.telefono = telefono;
        this.disponibilidad = "Disponible";
    }

    public int getIdRepartidor() {
        return idRepartidor;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getDisponibilidad() {
        return disponibilidad;
    }

    public void cambiarDisponibilidad(String disponibilidad) {
        this.disponibilidad = disponibilidad;
    }

    public void mostrarRepartidor() {
        System.out.println("Repartidor: " + nombre);
        System.out.println("Teléfono: " + telefono);
        System.out.println("Disponibilidad: " + disponibilidad);
    }
}
