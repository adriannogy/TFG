package com.restaurantes.restaurantesaplicacion.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.restaurantes.restaurantesaplicacion.dto.ValoracionDTO;
import com.restaurantes.restaurantesaplicacion.dto.ValoracionInputDTO;
/**
 * Interfaz para el servicio de gestión de valoraciones.
 */
public interface ValoracionService {
    
/**
     * Crea una nueva valoración para un restaurante.
     * Si el restaurante no existe en la BD local, intenta importarlo desde la API externa.
     * @param nombreUsuario El nombre del usuario que realiza la valoración.
     * @param valoracionInputDTO DTO con los datos de la valoración.
     * @return El DTO de la valoración creada.
     * @throws com.restaurantes.restaurantesaplicacion.exception.ValoracionDuplicadaException 
     * @throws com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException
     */
    ValoracionDTO crearValoracion(String nombreUsuario, ValoracionInputDTO valoracionInputDTO, MultipartFile[] files);
  /**
     * Obtiene una lista de todas las valoraciones existentes.
     * @return Una lista de DTOs de valoración.
     */
    List<ValoracionDTO> obtenerTodas();


    /**
     * Obtiene el feed  del usuario
     * @param emailUsuario
     * @param pageable
     * @return El DTO de la valoración
     */
    Page<ValoracionDTO> obtenerFeedParaUsuario(String emailUsuario, Pageable pageable);


/**
     * Eliminar resena
     * @param emailUsuario
     * @param restauranteId
     */
    void eliminarValoracion(String emailUsuario, Long restauranteId);
}
