package com.restaurantes.restaurantesaplicacion.dto;

import lombok.Data;

@Data
public class UsuarioResponseDTO {
    private Long id;
    private String nombreUsuario;
    private String email;
    private String fotoPerfilUrl;
}