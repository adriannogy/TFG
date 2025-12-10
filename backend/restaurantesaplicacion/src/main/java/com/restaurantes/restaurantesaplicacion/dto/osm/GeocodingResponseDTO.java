package com.restaurantes.restaurantesaplicacion.dto.osm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class GeocodingResponseDTO {
    private String lat;
    private String lon;

    @JsonProperty("osm_id")
    private long osmId;

    @JsonProperty("osm_type")
    private String osmType;
}