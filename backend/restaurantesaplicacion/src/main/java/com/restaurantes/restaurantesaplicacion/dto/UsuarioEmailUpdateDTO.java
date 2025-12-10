package com.restaurantes.restaurantesaplicacion.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UsuarioEmailUpdateDTO {
    @NotBlank
    @Email
    private String email;
}