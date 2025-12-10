package com.restaurantes.restaurantesaplicacion.dto;

import lombok.Data;

@Data
public class RestauranteImportDTO {
    
    private Long osmId; 
    private String nombre;
    private String ciudad;
    private String direccion;
    private String tipoCocina;
    private Double lat;
    private Double lon;
}