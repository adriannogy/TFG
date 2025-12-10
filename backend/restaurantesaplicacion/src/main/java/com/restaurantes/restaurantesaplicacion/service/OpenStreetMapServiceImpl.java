package com.restaurantes.restaurantesaplicacion.service;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.restaurantes.restaurantesaplicacion.client.NominatimClient;
import com.restaurantes.restaurantesaplicacion.client.OpenStreetMapClient;
import com.restaurantes.restaurantesaplicacion.dto.osm.ElementoRestauranteDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.GeocodingResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.RespuestaApiDTO;
import com.restaurantes.restaurantesaplicacion.exception.ConflictException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Service
public class OpenStreetMapServiceImpl implements OpenStreetMapService {

    private static final Logger log = LoggerFactory.getLogger(OpenStreetMapServiceImpl.class);

    @Autowired
    private NominatimClient nominatimClient;
    @Autowired
    private OpenStreetMapClient openStreetMapClient;

    
    @Override
    @CircuitBreaker(name = "osm", fallbackMethod = "fallbackBuscarTodos")
    public RespuestaApiDTO buscarTodosRestaurantesExternos(String ciudad,String nombre, String tipoCocina, String direccion) {
        log.info("Iniciando búsqueda externa COMPLETA para la ciudad: '{}'", ciudad);
        
        // 1. Geocodificación para obtener el área de la ciudad
        List<GeocodingResponseDTO> geocodingResults = nominatimClient.searchForCity(ciudad + ", Spain", "json", 1);
        if (geocodingResults.isEmpty()) {
            throw new ConflictException("Ciudad no encontrada: " + ciudad);
        }
        GeocodingResponseDTO geoData = geocodingResults.get(0);

        long areaId = geoData.getOsmId();
        if ("relation".equals(geoData.getOsmType())) areaId += 3600000000L;
        else if ("way".equals(geoData.getOsmType())) areaId += 2400000000L;

        // 2. Construcción de la consulta
        String nameFilter = (nombre != null && !nombre.isEmpty()) ? String.format("[~\"name\"~\"^%s\",i]", nombre) : "";
        String cuisineFilter = (tipoCocina != null && !tipoCocina.isEmpty()) ? String.format("[cuisine=%s]", tipoCocina.toLowerCase()) : "";
        String addressFilter = (direccion != null && !direccion.isEmpty()) ? String.format("[~\"addr:street\"~\"%s\",i]", direccion) : "";
        String queryTemplate = "[out:json];area(%d)->.searchArea;node(area.searchArea)[amenity=restaurant]%s%s%s;out body;"; 
        String finalQuery = String.format(queryTemplate, areaId, nameFilter, cuisineFilter, addressFilter);

        log.info("Ejecutando consulta a Overpass API: {}", finalQuery);
        return openStreetMapClient.buscarRestaurantes(finalQuery);
    }

    @Override
    public Page<ElementoRestauranteDTO> buscarRestaurantesExternos(String ciudad,String nombre, String tipoCocina, String direccion, Pageable pageable) {
        
        RespuestaApiDTO respuestaCompleta = this.buscarTodosRestaurantesExternos(ciudad, nombre, tipoCocina, direccion);
        List<ElementoRestauranteDTO> todosLosRestaurantes = respuestaCompleta.getElements();

  
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), todosLosRestaurantes.size());
        
        List<ElementoRestauranteDTO> pageContent;
        if (start > todosLosRestaurantes.size()) {
            pageContent = Collections.emptyList();
        } else {
            pageContent = todosLosRestaurantes.subList(start, end);
        }

        
        return new PageImpl<>(pageContent, pageable, todosLosRestaurantes.size());
    }

   
   public RespuestaApiDTO fallbackBuscarTodos(String ciudad, String tipoCocina, String direccion, Throwable throwable) {
        log.error("API externa (búsqueda completa) no disponible para [ciudad={}]. Usando fallback. Error: {}", ciudad, throwable.getMessage());
        RespuestaApiDTO respuestaVacia = new RespuestaApiDTO();
        respuestaVacia.setElements(Collections.emptyList());
        return respuestaVacia;
    }

   
    public Page<ElementoRestauranteDTO> fallbackBuscarRestaurantes(String ciudad, String tipoCocina, String direccion, Pageable pageable, Throwable throwable) {
        log.error("API externa (búsqueda paginada) no disponible. Usando fallback. Error: {}", throwable.getMessage());
        return new PageImpl<>(Collections.emptyList(), pageable, 0);
    }
}