package com.restaurantes.restaurantesaplicacion.model;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor; 
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RelacionUsuarioId implements Serializable {
    private Long seguidorId;
    private Long seguidoId;
}