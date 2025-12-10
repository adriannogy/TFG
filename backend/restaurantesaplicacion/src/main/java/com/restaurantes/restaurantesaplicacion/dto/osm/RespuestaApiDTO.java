package com.restaurantes.restaurantesaplicacion.dto.osm;

import lombok.Data;
import java.util.List;

@Data
public class RespuestaApiDTO {
    private List<ElementoRestauranteDTO> elements;
}