package com.restaurantes.restaurantesaplicacion.model;

import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Embeddable
@Data
public class FavoritoId implements Serializable {
    private Long usuarioId;
    private Long restauranteId;
}