package com.restaurantes.restaurantesaplicacion.service;

import com.restaurantes.restaurantesaplicacion.dto.RestauranteDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
/**
 * Interfaz para el servicio que gestiona los restaurantes favoritos de los usuarios.
 */
public interface FavoritoService {
/**
     * Añade un restaurante a la lista de favoritos de un usuario.
     * @param emailUsuario Email del usuario autenticado.
     * @param restauranteId ID del restaurante a añadir.
     */
    void agregarFavorito(String emailUsuario, Long restauranteId);
/**
     * Elimina un restaurante de la lista de favoritos de un usuario.
     * @param emailUsuario Email del usuario autenticado.
     * @param restauranteId ID del restaurante a eliminar.
     */
    void eliminarFavorito(String emailUsuario, Long restauranteId);
/**
     * Obtiene la lista de restaurantes favoritos de un usuario.
     * @param emailUsuario Email del usuario autenticado.
     * @param pageable
     * @return Una lista de DTOs de los restaurantes favoritos.
     */
    Page<RestauranteDTO> obtenerFavoritos(String emailUsuario ,Pageable pageable);
}