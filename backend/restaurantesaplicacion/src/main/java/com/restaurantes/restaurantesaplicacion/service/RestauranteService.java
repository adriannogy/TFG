package com.restaurantes.restaurantesaplicacion.service;

import com.restaurantes.restaurantesaplicacion.dto.RestauranteDTO;
import com.restaurantes.restaurantesaplicacion.dto.RestauranteImportDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.ElementoRestauranteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Interfaz para el servicio de gestión de restaurantes.
 * Define las operaciones de negocio relacionadas con los restaurantes.
 */
public interface RestauranteService {

 /**
     * Busca restaurantes en la API externa (OpenStreetMap) para una ciudad específica.
     * @param ciudad La ciudad donde buscar (ej: "Getafe").
     * @param tipoCocina Filtro opcional por tipo de cocina (ej: "tapas").
     * @param direccion Filtro opcional por dirección.
     * @param pageable Paginacion
     * @return Un DTO con la respuesta de la API externa.
     */
Page<ElementoRestauranteDTO> buscarRestaurantesExternos(String ciudad,String nombre, String tipoCocina, String direccion, Pageable pageable);
/**
     * Busca restaurantes en la base de datos local aplicando filtros dinámicos.
     * @param nombre Filtro por el inicio del nombre del restaurante (opcional).
     * @param ciudad Filtro por el nombre de la ciudad (opcional).
     * @param tipo_cocina Filtro por el tipo de cocina (opcional).
     * @param direccion Filtro por texto contenido en la dirección (opcional).
     * @param pageable Implementacion de paginacion
     * @return Una lista de DTOs de los restaurantes que coinciden con los filtros.
     */
    Page<RestauranteDTO> buscarRestaurantes(String nombre, String ciudad, String tipo_cocina,String direccion,Pageable pageable);
 /**
     * Guarda un nuevo restaurante o actualiza uno existente en la base de datos local.
     * @param restauranteDTO DTO con los datos del restaurante.
     * @return El DTO del restaurante guardado (con su ID).
     */
    RestauranteDTO guardarRestaurante(RestauranteDTO restauranteDTO);
/**
     * Sincroniza los restaurantes de una ciudad desde la API externa a la base de datos local.
     * Solo inserta los restaurantes que no existan previamente.
     * @param ciudad La ciudad a sincronizar.
     */
    void sincronizarRestaurantesExternos(String ciudad);
/**
     * Busca un restaurante por su ID en la base de datos local.
     * @param id El ID del restaurante a buscar.
     * @return El DTO del restaurante encontrado.
     * @throws com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException si el restaurante no existe.
     */
    RestauranteDTO obtenerRestaurantePorId(Long id);

/**
     * Importa un restaurante externo para anadirlo a favoritos.
     * @param importDTO
     * @return El DTO del restaurante encontrado.
     */
    RestauranteDTO importarRestauranteExterno(RestauranteImportDTO importDTO);
   
}
