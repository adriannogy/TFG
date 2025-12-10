package com.restaurantes.restaurantesaplicacion.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.restaurantes.restaurantesaplicacion.dto.RestauranteDTO;
import com.restaurantes.restaurantesaplicacion.exception.ConflictException;
import com.restaurantes.restaurantesaplicacion.mapper.RestauranteMapper;
import com.restaurantes.restaurantesaplicacion.model.Favorito;
import com.restaurantes.restaurantesaplicacion.model.FavoritoId;
import com.restaurantes.restaurantesaplicacion.model.Restaurante;
import com.restaurantes.restaurantesaplicacion.model.Usuario;
import com.restaurantes.restaurantesaplicacion.repository.FavoritoRepository;
import com.restaurantes.restaurantesaplicacion.repository.RestauranteRepository;
import com.restaurantes.restaurantesaplicacion.repository.UsuarioRepository;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
public class FavoritoServiceImpl implements FavoritoService {

    private static final Logger log = LoggerFactory.getLogger(FavoritoServiceImpl.class);

    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private RestauranteRepository restauranteRepository;
    @Autowired
    private FavoritoRepository favoritoRepository;
    @Autowired
    private RestauranteMapper restauranteMapper;

    @Override
    @Transactional
    @CacheEvict(value = "favoritos", allEntries = true)
    public void agregarFavorito(String emailUsuario, Long restauranteId) {
         log.info("Iniciando adición de favorito para usuario '{}' y restaurante ID '{}'", emailUsuario, restauranteId);
          log.debug("Buscando usuario y restaurante en la base de datos.");
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ConflictException("Usuario no encontrado"));
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
                .orElseThrow(() -> new ConflictException("Restaurante no encontrado"));

        FavoritoId id = new FavoritoId();
        id.setUsuarioId(usuario.getId());
        id.setRestauranteId(restaurante.getId());

        if (favoritoRepository.existsById(id)) {
            log.warn("El usuario '{}' ya tiene el restaurante '{}' en favoritos.", emailUsuario, restaurante.getNombre());
            return;
        }

        Favorito favorito = new Favorito();
        favorito.setId(id);
        favorito.setUsuario(usuario);
        favorito.setRestaurante(restaurante);
        favorito.setFechaAgregado(LocalDateTime.now());
        favoritoRepository.save(favorito);
        log.info("Restaurante '{}' añadido a favoritos para el usuario '{}'", restaurante.getNombre(), emailUsuario);
    }

    
    @Override
    @Transactional 
    @CacheEvict(value = "favoritos", allEntries = true)
    public void eliminarFavorito(String emailUsuario, Long restauranteId) {
        log.info("Iniciando eliminación de favorito para usuario '{}' y restaurante ID '{}'", emailUsuario, restauranteId);
        log.debug("Buscando usuario en la base de datos.");
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new ConflictException("Usuario no encontrado"));
        Restaurante restaurante = restauranteRepository.findById(restauranteId)
            .orElseThrow(() -> new ConflictException("Restaurante no encontrado"));

        FavoritoId id = new FavoritoId();
        id.setUsuarioId(usuario.getId());
        id.setRestauranteId(restaurante.getId());

        if (!favoritoRepository.existsById(id)) {
            log.warn("El usuario '{}' intentó eliminar un restaurante '{}' que no estaba en sus favoritos.", emailUsuario, restaurante.getNombre());
            return; 
        }

        favoritoRepository.deleteById(id);
        favoritoRepository.flush(); 

        log.info("Restaurante '{}' eliminado de favoritos para el usuario '{}'", restaurante.getNombre(), emailUsuario);
    }

    @Override
    @Cacheable(value = "favoritos", key = "{#emailUsuario, #pageable.pageNumber, #pageable.pageSize}")
    public Page<RestauranteDTO> obtenerFavoritos(String emailUsuario, Pageable pageable) {
        log.info("Solicitud para obtener favoritos del usuario '{}', Página: {}", emailUsuario, pageable.getPageNumber());
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new ConflictException("Usuario no encontrado"));

        
        return favoritoRepository.findByUsuario(usuario, pageable)
            .map(Favorito::getRestaurante)
            .map(restauranteMapper::toDto);
    }
}