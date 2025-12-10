package com.restaurantes.restaurantesaplicacion.service;

import com.restaurantes.restaurantesaplicacion.dto.RestauranteDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.ElementoRestauranteDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.RespuestaApiDTO;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.mapper.RestauranteMapper;
import com.restaurantes.restaurantesaplicacion.model.Restaurante;
import com.restaurantes.restaurantesaplicacion.repository.RestauranteRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.restaurantes.restaurantesaplicacion.dto.RestauranteImportDTO;
import java.util.List;
import jakarta.persistence.criteria.Predicate;
import java.util.Optional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 



@Service
public class RestauranteServiceImpl implements RestauranteService { 

    private static final Logger log = LoggerFactory.getLogger(RestauranteServiceImpl.class);

        @Autowired
        private RestauranteRepository restauranteRepository;

        @Autowired
        private RestauranteMapper restauranteMapper;

        @Autowired
    private OpenStreetMapService openStreetMapService;


    @Override
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "restaurantes", allEntries = true),
        @CacheEvict(value = "busquedaRestaurantes", allEntries = true)
    })
    public RestauranteDTO guardarRestaurante(RestauranteDTO restauranteDTO) {
        log.info("Guardando o actualizando restaurante: {}", restauranteDTO.getNombre());
        Restaurante restaurante = restauranteMapper.toEntity(restauranteDTO);
        Restaurante restauranteGuardado = restauranteRepository.save(restaurante);
        return restauranteMapper.toDto(restauranteGuardado);
    }


        @Override
        @Cacheable(value = "restaurantes", key = "#id")
        public RestauranteDTO obtenerRestaurantePorId(Long id) {
            log.info("--- BUSCANDO RESTAURANTE CON ID {} DESDE LA BD ---", id);
            log.debug("Buscando restaurante con ID {} en la BD (sin caché).", id);

            Restaurante restaurante = restauranteRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Restaurante no encontrado con id: " + id));
            return restauranteMapper.toDto(restaurante);
        }


    
    @Override
public Page<ElementoRestauranteDTO> buscarRestaurantesExternos(String ciudad,String nombre, String tipoCocina, String direccion, Pageable pageable) {
    log.info("Delegando búsqueda externa al OpenStreetMapService para la ciudad: {}", ciudad);
    return openStreetMapService.buscarRestaurantesExternos(ciudad,nombre, tipoCocina, direccion, pageable);
}

    @Override
    @Cacheable("busquedaRestaurantes")
    public Page<RestauranteDTO>buscarRestaurantes(String nombre, String ciudad, String tipoCocina, String direccion, Pageable pageable) {
        log.info("--- BUSCANDO EN BD (BÚSQUEDA SIN CACHÉ) ---");
        log.debug("Realizando búsqueda en la BD (sin caché).");

        Specification<Restaurante> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new java.util.ArrayList<>();

            if (nombre != null && !nombre.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("nombre")), nombre.toLowerCase() + "%"));
            }
            if (ciudad != null && !ciudad.isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("ciudad")), ciudad.toLowerCase()));
            }
            if (tipoCocina != null && !tipoCocina.isEmpty()) {
                predicates.add(criteriaBuilder.equal(criteriaBuilder.lower(root.get("tipoCocina")), tipoCocina.toLowerCase()));
            }

           
            if (direccion != null && !direccion.isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("direccion")), "%" + direccion.toLowerCase() + "%"));
            }
           

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return restauranteRepository.findAll(spec, pageable)
                .map(restauranteMapper::toDto);
    }

        @Override
    public void sincronizarRestaurantesExternos(String ciudad) {
        log.info("Iniciando sincronización para la ciudad: {}", ciudad);

      
        RespuestaApiDTO respuestaApi = openStreetMapService.buscarTodosRestaurantesExternos(ciudad, null, null,null);
        log.debug("Se encontraron {} restaurantes en la API externa para '{}'.", respuestaApi.getElements().size(), ciudad);

        for (ElementoRestauranteDTO elemento : respuestaApi.getElements()) {
            Optional<Restaurante> existente = restauranteRepository.findByOsmId(elemento.getId());

            if (existente.isEmpty()) {
                Restaurante nuevoRestaurante = new Restaurante();
                nuevoRestaurante.setOsmId(elemento.getId());
                nuevoRestaurante.setNombre(elemento.getTags().getOrDefault("name", "Nombre no disponible"));
                nuevoRestaurante.setTipoCocina(elemento.getTags().getOrDefault("cuisine", "No especificada"));
                nuevoRestaurante.setCiudad(ciudad);
                nuevoRestaurante.setLat(elemento.getLat());
                nuevoRestaurante.setLon(elemento.getLon());
                String direccion = elemento.getTags().getOrDefault("addr:street", "");
                String numero = elemento.getTags().getOrDefault("addr:housenumber", "");
                nuevoRestaurante.setDireccion(direccion + " " + numero);

                restauranteRepository.save(nuevoRestaurante);
                log.info("Guardado nuevo restaurante: {}", nuevoRestaurante.getNombre());
            } else {
                log.debug("Restaurante ya existe, omitiendo: {}", existente.get().getNombre());
            }
        }
        log.info("Sincronización para {} terminada.", ciudad);
    }


    @Override
    @Transactional
    public RestauranteDTO importarRestauranteExterno(RestauranteImportDTO importDTO) {
        log.info("Iniciando importación para restaurante osmId: {}", importDTO.getOsmId());

        // 1. Comprobar si ya existe por osmId
        Optional<Restaurante> existente = restauranteRepository.findByOsmId(importDTO.getOsmId());

        if (existente.isPresent()) {
            log.debug("Restaurante ya existe en la BD local. Devolviendo.");
            return restauranteMapper.toDto(existente.get());
        }

        // 2. Si no existe, lo creamos
        log.info("Restaurante no existe. Creando nueva entrada en la BD.");
        Restaurante nuevoRestaurante = new Restaurante();
        nuevoRestaurante.setOsmId(importDTO.getOsmId());
        nuevoRestaurante.setNombre(importDTO.getNombre());
        nuevoRestaurante.setCiudad(importDTO.getCiudad());
        nuevoRestaurante.setDireccion(importDTO.getDireccion());
        nuevoRestaurante.setTipoCocina(importDTO.getTipoCocina());
        nuevoRestaurante.setLat(importDTO.getLat());
        nuevoRestaurante.setLon(importDTO.getLon());

        Restaurante restauranteGuardado = restauranteRepository.save(nuevoRestaurante);

        return restauranteMapper.toDto(restauranteGuardado);
    }
        

    }
