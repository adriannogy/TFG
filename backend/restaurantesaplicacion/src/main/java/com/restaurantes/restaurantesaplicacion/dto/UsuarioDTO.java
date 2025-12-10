package com.restaurantes.restaurantesaplicacion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    @NotBlank(message = "El nombre no puede estar vacío.")
    @Size(min = 1, message = "El nombre debe tener al menos 1 caracteres.")
    private String nombreUsuario;
    private String pwd;
    private String email;
}
