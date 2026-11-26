/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.hm.ventas;

/**
 *
 * @author GUTIERREZCAMPOSFARID
 */


public class administrador extends Usuario implements GestionProductos, GestionPedidos {

    private String cargo;

    public administrador(int idUsuario, String nombre,
                         String correo, String contrasena,
                         String cargo) {
        super(idUsuario, nombre, correo, contrasena);
        this.cargo = cargo;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public void gestionarProductos() {
        System.out.println("Administrador gestionando productos.");
    }

    public void gestionarPedidos() {
        System.out.println("Administrador gestionando pedidos.");
    }
    
    @Override
    public void registrarProducto(Producto producto) {
    System.out.println("Producto registrado: " + producto.getNombre());
    }

    @Override
    public void actualizarProducto(Producto producto) {
    System.out.println("Producto actualizado: " + producto.getNombre());
    }

    @Override
    public void eliminarProducto(int idProducto) {
    System.out.println("Solicitud de eliminación del producto: " + idProducto);
    }

    @Override
    public void consultarPedido(int idPedido) {
    System.out.println("Consultando pedido: " + idPedido);
    }

    @Override
    public void actualizarEstadoPedido(int idPedido, String nuevoEstado) {
    System.out.println("Pedido " + idPedido + " actualizado a: " + nuevoEstado);
    }
}
