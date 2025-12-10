package com.restaurantes.restaurantesaplicacion.dto;

import lombok.Data;
import java.util.List;
@Data
public class ValoracionDTO {
    private RestauranteDTO restaurante;
    private int puntuacion;
    private String comentario;
    private String nombreUsuario; 
    private List<String> fotos;
    
}
