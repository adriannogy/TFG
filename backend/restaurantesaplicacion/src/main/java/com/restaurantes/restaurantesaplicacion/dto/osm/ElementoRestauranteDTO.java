package com.restaurantes.restaurantesaplicacion.dto.osm;

import lombok.Data;
import java.util.Map;

@Data
public class ElementoRestauranteDTO {
    private long id;
    private double lat;
    private double lon;
    private Map<String, String> tags; // Aquí vendrá el nombre, tipo de cocina, etc.
}