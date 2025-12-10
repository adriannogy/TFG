package com.restaurantes.restaurantesaplicacion.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioNombreUpdateDTO {
    @NotBlank
    private String nombreUsuario;
}