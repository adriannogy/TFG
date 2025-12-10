package com.restaurantes.restaurantesaplicacion.dto;

import com.restaurantes.restaurantesaplicacion.model.EstadoSolicitud;
import lombok.Data;
import java.util.List;

@Data
public class UserProfileDTO {

    private String nombreUsuario;
    private String fotoPerfilUrl;
    private boolean isPrivate;
    private EstadoSolicitud relationshipStatus; 
    private Integer seguidosCount;
    private Integer seguidoresCount;
    private List<ValoracionDTO> valoraciones;
}