package com.restaurantes.restaurantesaplicacion.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.restaurantes.restaurantesaplicacion.dto.ValoracionDTO;
import com.restaurantes.restaurantesaplicacion.dto.ValoracionInputDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.ElementoRestauranteDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.RespuestaApiDTO;
import com.restaurantes.restaurantesaplicacion.exception.ConflictException;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.exception.ValoracionDuplicadaException;
import com.restaurantes.restaurantesaplicacion.mapper.ValoracionMapper;
import com.restaurantes.restaurantesaplicacion.model.EstadoSolicitud;
import com.restaurantes.restaurantesaplicacion.model.Foto;
import com.restaurantes.restaurantesaplicacion.model.RelacionUsuario;
import com.restaurantes.restaurantesaplicacion.model.Restaurante;
import com.restaurantes.restaurantesaplicacion.model.Usuario;
import com.restaurantes.restaurantesaplicacion.model.Valoracion;
import com.restaurantes.restaurantesaplicacion.model.ValoracionId;
import com.restaurantes.restaurantesaplicacion.repository.RestauranteRepository;
import com.restaurantes.restaurantesaplicacion.repository.UsuarioRepository;
import com.restaurantes.restaurantesaplicacion.repository.ValoracionRepository;

@Service
public class ValoracionServiceImpl implements ValoracionService {

        @Autowired
        private ValoracionRepository valoracionRepository;

        @Autowired
        private UsuarioRepository usuarioRepository;

        @Autowired
        private RestauranteRepository restauranteRepository;

        @Autowired
        private ValoracionMapper valoracionMapper;

        @Autowired
        private FileStorageService fileStorageService;

        @Autowired
        private OpenStreetMapService openStreetMapService;

        private static final Logger log = LoggerFactory.getLogger(ValoracionServiceImpl.class);
        

    @Override
    @Transactional
    public ValoracionDTO crearValoracion(String nombreUsuario, ValoracionInputDTO valoracionInputDTO,MultipartFile[] files) {
        log.info("Iniciando creación de valoración para usuario '{}' en restaurante '{}'", nombreUsuario, valoracionInputDTO.getNombreRestaurante());

        log.debug("Buscando usuario por nombre: {}", nombreUsuario);
        Usuario usuario = usuarioRepository.findByNombreUsuario(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con nombre: " + nombreUsuario));

        Optional<Restaurante> restauranteExistente = restauranteRepository.findByNombre(valoracionInputDTO.getNombreRestaurante());

        Restaurante restauranteParaValorar;

        if (restauranteExistente.isPresent()) {
            log.debug("Restaurante encontrado en la base de datos local.");
            restauranteParaValorar = restauranteExistente.get();
        } else {
            log.debug("Restaurante '{}' no encontrado localmente. Buscando en API externa.", valoracionInputDTO.getNombreRestaurante());
            RespuestaApiDTO respuestaExterna = openStreetMapService.buscarTodosRestaurantesExternos(valoracionInputDTO.getCiudad(), null, null,null);
            log.info("La API externa ha devuelto {} restaurantes para la ciudad '{}'. Nombres encontrados:", 
                respuestaExterna.getElements().size(), 
                valoracionInputDTO.getCiudad());

       
        respuestaExterna.getElements().stream().limit(20).forEach(elemento -> {
            log.info("- Nombre en OSM: '{}'", elemento.getTags().get("name"));
        });

            ElementoRestauranteDTO elementoExterno = respuestaExterna.getElements().stream()
                .filter(e -> valoracionInputDTO.getNombreRestaurante().equalsIgnoreCase(e.getTags().get("name")))
                .findFirst()
                .orElseThrow(() -> new ConflictException("Restaurante no encontrado ni en la base de datos local ni en la API externa."));

            log.info("Restaurante encontrado en la API externa. Guardándolo localmente...");

            Restaurante nuevoRestaurante = new Restaurante();
            nuevoRestaurante.setOsmId(elementoExterno.getId());
            nuevoRestaurante.setNombre(elementoExterno.getTags().getOrDefault("name", "Nombre no disponible"));
            nuevoRestaurante.setTipoCocina(elementoExterno.getTags().getOrDefault("cuisine", "No especificada"));
            nuevoRestaurante.setCiudad(valoracionInputDTO.getCiudad());
            nuevoRestaurante.setLat(elementoExterno.getLat());
            nuevoRestaurante.setLon(elementoExterno.getLon());
            restauranteParaValorar = restauranteRepository.save(nuevoRestaurante);
        }

        log.debug("Comprobando si ya existe valoración para usuario ID {} y restaurante ID {}", usuario.getId(), restauranteParaValorar.getId());
        ValoracionId valoracionId = new ValoracionId();
        valoracionId.setUsuarioId(usuario.getId());
        valoracionId.setRestauranteId(restauranteParaValorar.getId());

        if (valoracionRepository.existsById(valoracionId)) {
            log.warn("Intento de valoración duplicada...");
            throw new ValoracionDuplicadaException("Este usuario ya ha valorado este restaurante.");
        }

        Valoracion nuevaValoracion = new Valoracion();
        nuevaValoracion.setId(valoracionId);
        nuevaValoracion.setUsuario(usuario);
        nuevaValoracion.setRestaurante(restauranteParaValorar);
        nuevaValoracion.setPuntuacion(valoracionInputDTO.getPuntuacion());
        nuevaValoracion.setComentario(valoracionInputDTO.getComentario());
        nuevaValoracion.setFechaCreacion(LocalDateTime.now());

        Valoracion valoracionGuardada = valoracionRepository.save(nuevaValoracion);

         if (files != null) {
        log.debug("Procesando {} archivos para la valoración.", files.length);
        for (MultipartFile file : files) {
            if (!file.isEmpty()) {
                try {
                    log.debug("Subiendo archivo: {}", file.getOriginalFilename());
                    String fileUrl = fileStorageService.store(file);
                    log.info("Archivo subido a: {}", fileUrl);
                    
                    Foto foto = new Foto();
                    foto.setUrl(fileUrl); 
                    foto.setValoracion(valoracionGuardada);
                    valoracionGuardada.getFotos().add(foto);
                } catch (Exception e) {
                    log.error("FALLO al subir el archivo '{}'. Saltando este archivo.", file.getOriginalFilename(), e);
                }
            }
        }
    }

        Valoracion valoracionFinal = valoracionRepository.save(valoracionGuardada);
        log.info("Valoración creada con éxito.");
        return valoracionMapper.toDto(valoracionFinal);
    }
        
            
    @Override
    public List<ValoracionDTO> obtenerTodas() {
            return valoracionRepository.findAll()
                    .stream()
                    .map(valoracionMapper::toDto)
                    .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public Page<ValoracionDTO> obtenerFeedParaUsuario(String emailUsuario, Pageable pageable) {
        log.info("Obteniendo feed para el usuario '{}', página: {}", emailUsuario, pageable.getPageNumber());
        
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        List<Usuario> seguidos = usuario.getSiguiendo().stream()
            .filter(r -> r.getEstado() == EstadoSolicitud.ACEPTADO)
            .map(RelacionUsuario::getSeguido)
            .collect(Collectors.toList());
            
        if (seguidos.isEmpty()) {
            log.info("El usuario no sigue a nadie. Devolviendo feed vacío.");
            return Page.empty();
        }

        Page<Valoracion> valoracionesEntidad = valoracionRepository.findByUsuarioInOrderByFechaCreacionDesc(seguidos, pageable);
        return valoracionesEntidad.map(valoracion -> {
            ValoracionDTO dto = valoracionMapper.toDto(valoracion);
        
            List<String> urls = valoracion.getFotos().stream()
                                  .map(Foto::getUrl)
                                  .collect(Collectors.toList());
            dto.setFotos(urls);
            return dto;
        });
    }

    @Override
    @Transactional
    public void eliminarValoracion(String emailUsuario, Long restauranteId) {
        log.info("Iniciando eliminación de valoración para usuario '{}' en restaurante ID '{}'", emailUsuario, restauranteId);

      
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        ValoracionId valoracionId = new ValoracionId();
        valoracionId.setUsuarioId(usuario.getId());
        valoracionId.setRestauranteId(restauranteId);

       
        Valoracion valoracion = valoracionRepository.findById(valoracionId)
                .orElseThrow(() -> new ResourceNotFoundException("Valoración no encontrada para este usuario y restaurante"));

       
        if (!valoracion.getUsuario().getId().equals(usuario.getId())) {
            throw new SecurityException("Un usuario solo puede borrar sus propias valoraciones.");
        }

       
        valoracionRepository.delete(valoracion);
        log.info("Valoración eliminada con éxito.");
    }
    }
