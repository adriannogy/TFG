package com.restaurantes.restaurantesaplicacion.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProfileDTO {
    
    private String nombreUsuario;
    private String fotoPerfilUrl;
    private int seguidosCount;
    private int seguidoresCount;
    private int solicitudesPendientesCount;
    private List<ValoracionDTO> valoraciones;
}