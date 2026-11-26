/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.hm.ventas;

public interface GestionPedidos {
    void consultarPedido(int idPedido);
    void actualizarEstadoPedido(int idPedido, String nuevoEstado);
}
