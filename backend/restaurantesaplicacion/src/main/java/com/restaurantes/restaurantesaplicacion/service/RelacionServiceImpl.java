package com.restaurantes.restaurantesaplicacion.service;

import com.restaurantes.restaurantesaplicacion.dto.UsuarioSimpleDTO;
import com.restaurantes.restaurantesaplicacion.exception.BadRequestException;
import com.restaurantes.restaurantesaplicacion.exception.ConflictException;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.mapper.UsuarioMapper;
import com.restaurantes.restaurantesaplicacion.model.EstadoSolicitud;
import com.restaurantes.restaurantesaplicacion.model.RelacionUsuario;
import com.restaurantes.restaurantesaplicacion.model.RelacionUsuarioId;
import com.restaurantes.restaurantesaplicacion.model.Usuario;
import com.restaurantes.restaurantesaplicacion.repository.RelacionUsuarioRepository;
import com.restaurantes.restaurantesaplicacion.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class RelacionServiceImpl implements RelacionService {

        private static final Logger log = LoggerFactory.getLogger(RelacionServiceImpl.class);

        @Autowired
        private UsuarioRepository usuarioRepository;

        @Autowired
        private RelacionUsuarioRepository relacionUsuarioRepository;

        @Autowired
        private UsuarioMapper usuarioMapper;



        @Override
        @Transactional
        @CacheEvict(value = "solicitudes", allEntries = true)
        public void solicitarSeguimiento(String emailSeguidor, String nombreSeguido) {
                 log.info("Iniciando solicitud de seguimiento de '{}' a '{}'", emailSeguidor, nombreSeguido);
        
                log.debug("Buscando usuario seguidor por email: {}", emailSeguidor);
                Usuario seguidor = usuarioRepository.findByEmail(emailSeguidor)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario seguidor no encontrado"));
                log.debug("Buscando usuario seguido por nombre: {}", nombreSeguido);
                Usuario seguido = usuarioRepository.findByNombreUsuario(nombreSeguido)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario a seguir no encontrado"));

                if (Objects.equals(seguidor.getId(), seguido.getId())) {
                         log.warn("Intento de auto-seguimiento por usuario ID: {}", seguidor.getId());
                        throw new BadRequestException("Un usuario no puede seguirse a sí mismo.");
                }

                RelacionUsuarioId id = new RelacionUsuarioId();
                id.setSeguidorId(seguidor.getId());
                id.setSeguidoId(seguido.getId());

                if (relacionUsuarioRepository.existsById(id)) {
                        log.warn("Ya existe una solicitud entre el usuario ID {} y el usuario ID {}", seguidor.getId(), seguido.getId());
                        throw new ConflictException("Ya existe una solicitud de seguimiento o ya se siguen.");
                }

                RelacionUsuario relacion = new RelacionUsuario();
                relacion.setId(id);
                relacion.setSeguidor(seguidor);
                relacion.setSeguido(seguido);
                relacion.setEstado(EstadoSolicitud.PENDIENTE);
                relacionUsuarioRepository.save(relacion);
                log.info("Usuario '{}' ha solicitado seguir a '{}'", seguidor.getNombreUsuario(), seguido.getNombreUsuario());
        }

        @Override
        @Transactional
        @Caching(evict = {
                @CacheEvict(value = "seguidores", allEntries = true),
                @CacheEvict(value = "siguiendo", allEntries = true),
                @CacheEvict(value = "solicitudes", allEntries = true)
        })
        public void aceptarSolicitud(String emailReceptor, String nombreEmisor) {
                log.info("Usuario '{}' está aceptando la solicitud de '{}'", emailReceptor, nombreEmisor);

                Usuario receptor = usuarioRepository.findByEmail(emailReceptor)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario receptor no encontrado"));
                Usuario emisor = usuarioRepository.findByNombreUsuario(nombreEmisor)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario emisor no encontrado"));

                RelacionUsuarioId id = new RelacionUsuarioId();
                id.setSeguidorId(emisor.getId());
                id.setSeguidoId(receptor.getId());

                RelacionUsuario relacion = relacionUsuarioRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Solicitud de seguimiento no encontrada."));

                relacion.setEstado(EstadoSolicitud.ACEPTADO);
                relacionUsuarioRepository.save(relacion);
                log.info("Usuario '{}' ha aceptado la solicitud de '{}'", receptor.getNombreUsuario(), emisor.getNombreUsuario());
        }

        @Override
        @Transactional
        @CacheEvict(value = "solicitudes", allEntries = true)
        public void rechazarSolicitud(String emailReceptor, String nombreEmisor) {
                Usuario receptor = usuarioRepository.findByEmail(emailReceptor)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario receptor no encontrado"));
                Usuario emisor = usuarioRepository.findByNombreUsuario(nombreEmisor)
                        .orElseThrow(() -> new UsernameNotFoundException("Usuario emisor no encontrado"));

                RelacionUsuarioId id = new RelacionUsuarioId();
                id.setSeguidorId(emisor.getId());
                id.setSeguidoId(receptor.getId());

                RelacionUsuario relacion = relacionUsuarioRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Solicitud de seguimiento no encontrada."));

                relacionUsuarioRepository.delete(relacion);
                log.info("Usuario '{}' ha rechazado (y eliminado) la solicitud de '{}'", receptor.getNombreUsuario(), emisor.getNombreUsuario());
        }

        

        @Override
        @Cacheable("seguidores")
        public List<UsuarioSimpleDTO> obtenerMisSeguidores(String email) {
                log.info("Buscando seguidores para el usuario autenticado '{}'", email);
                Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    
        return relacionUsuarioRepository.findBySeguidoAndEstado(usuario, EstadoSolicitud.ACEPTADO)
            .stream()
            .map(relacion -> usuarioMapper.toSimpleDto(relacion.getSeguidor()))
            .collect(Collectors.toList());
        }

       
        @Override
        @Cacheable("seguidores")
        public List<UsuarioSimpleDTO> obtenerSeguidoresDe(String emailViewer, String nombrePerfil) {
                log.info("Buscando seguidores de '{}', solicitado por '{}'", nombrePerfil, emailViewer);
                Usuario perfilUsuario = usuarioRepository.findByNombreUsuario(nombrePerfil)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario del perfil no encontrado"));
    
                verificarPermiso(emailViewer, perfilUsuario); 
    
                return relacionUsuarioRepository.findBySeguidoAndEstado(perfilUsuario, EstadoSolicitud.ACEPTADO)
            .stream()
            .map(relacion -> usuarioMapper.toSimpleDto(relacion.getSeguidor()))
            .collect(Collectors.toList());
        }
                

       

        @Override
        @Cacheable("siguiendo")
        public List<UsuarioSimpleDTO> obtenerMisSiguiendo(String email) {
                log.info("Buscando a quienes sigue el usuario autenticado '{}' desde la BD", email);
                 Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    
               
                return relacionUsuarioRepository.findBySeguidorAndEstado(usuario, EstadoSolicitud.ACEPTADO)
                .stream()
                .map(relacion -> usuarioMapper.toSimpleDto(relacion.getSeguido()))
                .collect(Collectors.toList());
        }

        @Override
        @Cacheable("siguiendo")
        public List<UsuarioSimpleDTO> obtenerSiguiendoDe(String emailViewer, String nombrePerfil) {
                log.info("Buscando a quienes sigue '{}', solicitado por '{}'", nombrePerfil, emailViewer);
                Usuario perfilUsuario = usuarioRepository.findByNombreUsuario(nombrePerfil)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario del perfil no encontrado"));
    
                verificarPermiso(emailViewer, perfilUsuario);
    
               
                return relacionUsuarioRepository.findBySeguidorAndEstado(perfilUsuario, EstadoSolicitud.ACEPTADO)
                .stream()
                .map(relacion -> usuarioMapper.toSimpleDto(relacion.getSeguido()))
                .collect(Collectors.toList());
        }

        @Override
        @Cacheable("solicitudes")
        public List<UsuarioSimpleDTO> obtenerSolicitudesPendientes(String email) {
                log.info("Buscando solicitudes pendientes para '{}' desde la BD", email);
                Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        
                List<RelacionUsuario> solicitudes = relacionUsuarioRepository.findBySeguidoAndEstado(usuario, EstadoSolicitud.PENDIENTE);

                return solicitudes.stream()
                .map(relacion -> usuarioMapper.toSimpleDto(relacion.getSeguidor()))
                .collect(Collectors.toList());
                }


                private void verificarPermiso(String emailViewer, Usuario perfilUsuario) {
                //El que mira es el dueño del perfil
                if (emailViewer.equalsIgnoreCase(perfilUsuario.getEmail())) {
                log.debug("Acceso permitido: El viewer es el dueño del perfil.");
                return; 
                }

                //Comprobar si el que mira es un seguidor aceptado del dueño del perfil
                boolean esSeguidorAceptado = perfilUsuario.getSeguidores().stream()
                .anyMatch(relacion -> 
                relacion.getEstado() == EstadoSolicitud.ACEPTADO &&
                relacion.getSeguidor().getEmail().equalsIgnoreCase(emailViewer)
            );

                if (esSeguidorAceptado) {
                log.debug("Acceso permitido: El viewer es un seguidor aceptado.");
                return; 
                }

                log.warn("Acceso denegado para '{}' al perfil de '{}'", emailViewer, perfilUsuario.getNombreUsuario());
                throw new AccessDeniedException("No tienes permiso para ver esta información.");
        }

        @Override
        @Transactional
        @Caching(evict = {
        @CacheEvict(value = "seguidores", allEntries = true),
        @CacheEvict(value = "siguiendo", allEntries = true)
                })
        public void eliminarSeguidor(String emailSeguido, String nombreSeguidor) {
        log.info("Iniciando eliminación del seguidor '{}' para el usuario '{}'", nombreSeguidor, emailSeguido);

        // 1. Buscamos ambos usuarios
        Usuario seguido = usuarioRepository.findByEmail(emailSeguido)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + emailSeguido));
        Usuario seguidor = usuarioRepository.findByNombreUsuario(nombreSeguidor)
                .orElseThrow(() -> new UsernameNotFoundException("Seguidor no encontrado: " + nombreSeguidor));

        // 2. Creamos el ID de la relación a buscar
        RelacionUsuarioId id = new RelacionUsuarioId();
        id.setSeguidorId(seguidor.getId());
        id.setSeguidoId(seguido.getId());

        // 3. Buscamos la relación y verificamos que exista y esté aceptada
        RelacionUsuario relacion = relacionUsuarioRepository.findById(id)
                .orElseThrow(() -> new ConflictException("No existe una relación con este seguidor."));

        if (relacion.getEstado() != EstadoSolicitud.ACEPTADO) {
                throw new ConflictException("No se puede eliminar un seguidor que no ha sido aceptado.");
        }

        // 4. Eliminamos la relación
        relacionUsuarioRepository.delete(relacion);
        log.info("El seguidor '{}' ha sido eliminado para el usuario '{}'", nombreSeguidor, emailSeguido);
        }



        @Override
        @Transactional
        @Caching(evict = {
        @CacheEvict(value = "seguidores", allEntries = true),
        @CacheEvict(value = "siguiendo", allEntries = true)
        })
        public void dejarDeSeguir(String emailSeguidor, String nombreSeguido) {
                log.info("Iniciando acción 'dejar de seguir' de '{}' a '{}'", emailSeguidor, nombreSeguido);

                Usuario seguidor = usuarioRepository.findByEmail(emailSeguidor)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario (seguidor) no encontrado"));
                Usuario seguido = usuarioRepository.findByNombreUsuario(nombreSeguido)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario (seguido) no encontrado"));
        
                
                RelacionUsuarioId id = new RelacionUsuarioId(seguidor.getId(), seguido.getId());
                relacionUsuarioRepository.deleteById(id);
                relacionUsuarioRepository.flush(); 
        
                log.info("'{}' ha dejado de seguir a '{}'", emailSeguidor, nombreSeguido);
        }

        }