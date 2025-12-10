package com.restaurantes.restaurantesaplicacion.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.restaurantes.restaurantesaplicacion.dto.osm.ElementoRestauranteDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.RespuestaApiDTO;

/**
 * Interfaz para el servicio que se comunica con las APIs de OpenStreetMap.
 */
public interface OpenStreetMapService {
/**
     * Busca restaurantes en la API externa de OpenStreetMap (Overpass)
     * usando geocodificación de Nominatim para encontrar el área de la ciudad.
     * @param ciudad La ciudad donde buscar (ej: "Getafe").
     * @param tipoCocina Filtro opcional por tipo de cocina (ej: "tapas").
     * @param direccion Filtro opcional por dirección.
     * @param pageable Implementacion de la paginacion
     * @return Un DTO con la respuesta de la API externa.
     */
        Page<ElementoRestauranteDTO> buscarRestaurantesExternos(String ciudad,String nombre, String tipoCocina, String direccion, Pageable pageable);
        RespuestaApiDTO buscarTodosRestaurantesExternos(String ciudad, String nombre, String tipoCocina, String direccion);
    }