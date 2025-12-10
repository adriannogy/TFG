package com.restaurantes.restaurantesaplicacion.client;
import com.restaurantes.restaurantesaplicacion.dto.osm.RespuestaApiDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "openstreetmap", url = "https://overpass-api.de")
public interface OpenStreetMapClient {
    @PostMapping(value = "/api/interpreter")
    RespuestaApiDTO buscarRestaurantes(@RequestBody String query);
}