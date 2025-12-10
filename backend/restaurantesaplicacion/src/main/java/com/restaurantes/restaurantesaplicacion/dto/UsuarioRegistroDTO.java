package com.restaurantes.restaurantesaplicacion.dto;

import lombok.Data;

@Data
public class UsuarioRegistroDTO {
    private String nombreUsuario;
    private String email;
    private String pwd;
}