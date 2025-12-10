package com.restaurantes.restaurantesaplicacion.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import java.io.Serializable;

@Embeddable 
@Data
public class ValoracionId implements Serializable {

    @Column(name = "usuario_id")
    private Long usuarioId;

    @Column(name = "restaurante_id")
    private Long restauranteId;
    
}
