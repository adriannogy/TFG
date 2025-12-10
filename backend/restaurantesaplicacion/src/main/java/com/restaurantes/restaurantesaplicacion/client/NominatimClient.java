package com.restaurantes.restaurantesaplicacion.client;
import com.restaurantes.restaurantesaplicacion.dto.osm.GeocodingResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@FeignClient(name = "nominatim", url = "https://nominatim.openstreetmap.org")
public interface NominatimClient {
    @GetMapping("/search")
    List<GeocodingResponseDTO> searchForCity(@RequestParam("q") String query, @RequestParam("format") String format, @RequestParam("limit") int limit);
}