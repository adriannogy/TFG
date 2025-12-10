package com.restaurantes.restaurantesaplicacion.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
public class RestauranteDTO {
    private Long id;

    @NotBlank(message = "El nombre no puede estar vacío")
    private String nombre;
    
    private String direccion;

    @NotBlank(message = "La ciudad no puede estar vacía")
    private String ciudad;

    private String tipoCocina;
    private Double lat;
    private Double lon;

    @NotNull(message = "El osmId no puede ser nulo")
    private Long osmId;
}
