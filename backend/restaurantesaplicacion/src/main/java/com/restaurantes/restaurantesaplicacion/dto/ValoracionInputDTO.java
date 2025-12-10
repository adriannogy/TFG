package com.restaurantes.restaurantesaplicacion.dto;

import lombok.Data;

@Data
public class ValoracionInputDTO {
    private String nombreRestaurante;
    private int puntuacion;
    private String comentario;
    private String ciudad;
    
}
