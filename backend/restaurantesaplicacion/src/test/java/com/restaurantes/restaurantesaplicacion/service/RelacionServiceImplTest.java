package com.restaurantes.restaurantesaplicacion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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

@ExtendWith(MockitoExtension.class)
public class RelacionServiceImplTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RelacionUsuarioRepository relacionUsuarioRepository;
    @Mock private UsuarioMapper usuarioMapper;

    @InjectMocks
    private RelacionServiceImpl relacionService;

    private Usuario usuarioSeguidor;
    private Usuario usuarioSeguido;
    private RelacionUsuario relacion;
    private UsuarioSimpleDTO usuarioSimpleDTO;

    @BeforeEach
    void setUp() {
        usuarioSeguidor = new Usuario();
        usuarioSeguidor.setId(1L);
        usuarioSeguidor.setEmail("seguidor@test.com");
        usuarioSeguidor.setNombreUsuario("Seguidor");
        usuarioSeguidor.setSeguidores(new HashSet<>()); 
        usuarioSeguidor.setSiguiendo(new HashSet<>());
        usuarioSeguido = new Usuario();
        usuarioSeguido.setId(2L);
        usuarioSeguido.setEmail("seguido@test.com");
        usuarioSeguido.setNombreUsuario("Seguido");
        usuarioSeguido.setSeguidores(new HashSet<>());
        usuarioSeguido.setSiguiendo(new HashSet<>());

        relacion = new RelacionUsuario();
        RelacionUsuarioId id = new RelacionUsuarioId(usuarioSeguidor.getId(), usuarioSeguido.getId());
        relacion.setId(id);
        relacion.setSeguidor(usuarioSeguidor);
        relacion.setSeguido(usuarioSeguido);
        relacion.setEstado(EstadoSolicitud.PENDIENTE);

        usuarioSimpleDTO = new UsuarioSimpleDTO();
        usuarioSimpleDTO.setNombreUsuario("UsuarioSimple");
    }

    // --- Tests para solicitarSeguimiento ---

    @Test
    public void solicitarSeguimiento_exito_guardaRelacionPendiente() {
        given(usuarioRepository.findByEmail("seguidor@test.com")).willReturn(Optional.of(usuarioSeguidor));
        given(usuarioRepository.findByNombreUsuario("Seguido")).willReturn(Optional.of(usuarioSeguido));
        given(relacionUsuarioRepository.existsById(any(RelacionUsuarioId.class))).willReturn(false);

        relacionService.solicitarSeguimiento("seguidor@test.com", "Seguido");

        verify(relacionUsuarioRepository).save(any(RelacionUsuario.class));
    }

    @Test
    public void solicitarSeguimiento_autoSeguimiento_lanzaBadRequest() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuarioSeguidor));
        given(usuarioRepository.findByNombreUsuario(anyString())).willReturn(Optional.of(usuarioSeguidor));

        assertThrows(BadRequestException.class, () -> {
            relacionService.solicitarSeguimiento("seguidor@test.com", "Seguidor");
        });
        verify(relacionUsuarioRepository, never()).save(any());
    }

    @Test
    public void solicitarSeguimiento_relacionYaExiste_lanzaConflict() {
        given(usuarioRepository.findByEmail("seguidor@test.com")).willReturn(Optional.of(usuarioSeguidor));
        given(usuarioRepository.findByNombreUsuario("Seguido")).willReturn(Optional.of(usuarioSeguido));
        given(relacionUsuarioRepository.existsById(any(RelacionUsuarioId.class))).willReturn(true);

        assertThrows(ConflictException.class, () -> {
            relacionService.solicitarSeguimiento("seguidor@test.com", "Seguido");
        });
        verify(relacionUsuarioRepository, never()).save(any());
    }

    // --- Tests para aceptarSolicitud ---

    @Test
    public void aceptarSolicitud_exito_actualizaAEstadoAceptado() {
        given(usuarioRepository.findByEmail("seguido@test.com")).willReturn(Optional.of(usuarioSeguido));
        given(usuarioRepository.findByNombreUsuario("Seguidor")).willReturn(Optional.of(usuarioSeguidor));
        given(relacionUsuarioRepository.findById(any(RelacionUsuarioId.class))).willReturn(Optional.of(relacion));

        relacionService.aceptarSolicitud("seguido@test.com", "Seguidor");

        assertEquals(EstadoSolicitud.ACEPTADO, relacion.getEstado());
        verify(relacionUsuarioRepository).save(relacion);
    }

    // --- Tests para rechazarSolicitud ---

    @Test
    public void rechazarSolicitud_exito_eliminaRelacion() {
        given(usuarioRepository.findByEmail("seguido@test.com")).willReturn(Optional.of(usuarioSeguido));
        given(usuarioRepository.findByNombreUsuario("Seguidor")).willReturn(Optional.of(usuarioSeguidor));
        given(relacionUsuarioRepository.findById(any(RelacionUsuarioId.class))).willReturn(Optional.of(relacion));

        relacionService.rechazarSolicitud("seguido@test.com", "Seguidor");

        verify(relacionUsuarioRepository).delete(relacion);
    }

    // --- Tests para obtenerMisSeguidores ---

    @Test
    public void obtenerMisSeguidores_retornaListaDTO() {
        given(usuarioRepository.findByEmail("seguido@test.com")).willReturn(Optional.of(usuarioSeguido));
        given(relacionUsuarioRepository.findBySeguidoAndEstado(usuarioSeguido, EstadoSolicitud.ACEPTADO))
                .willReturn(List.of(relacion));
        given(usuarioMapper.toSimpleDto(any(Usuario.class))).willReturn(usuarioSimpleDTO);

        List<UsuarioSimpleDTO> resultado = relacionService.obtenerMisSeguidores("seguido@test.com");

        assertEquals(1, resultado.size());
        assertEquals("UsuarioSimple", resultado.get(0).getNombreUsuario());
    }

    // --- Tests para obtenerSeguidoresDe (con verificacion de permisos) ---

    @Test
    public void obtenerSeguidoresDe_propietario_accesoPermitido() {
        given(usuarioRepository.findByNombreUsuario("Seguido")).willReturn(Optional.of(usuarioSeguido));
        given(relacionUsuarioRepository.findBySeguidoAndEstado(usuarioSeguido, EstadoSolicitud.ACEPTADO))
                .willReturn(List.of(relacion));
        given(usuarioMapper.toSimpleDto(any())).willReturn(usuarioSimpleDTO);

        List<UsuarioSimpleDTO> resultado = relacionService.obtenerSeguidoresDe("seguido@test.com", "Seguido");

        assertNotNull(resultado);
    }

    @Test
    public void obtenerSeguidoresDe_noSeguidor_accesoDenegado() {
        Usuario extraño = new Usuario();
        extraño.setEmail("extrano@test.com");
        
        given(usuarioRepository.findByNombreUsuario("Seguido")).willReturn(Optional.of(usuarioSeguido));

        assertThrows(AccessDeniedException.class, () -> {
            relacionService.obtenerSeguidoresDe("extrano@test.com", "Seguido");
        });
    }

    // --- Tests para obtenerMisSiguiendo ---
     @Test
    public void obtenerMisSiguiendo_retornaListaDTO() {
        given(usuarioRepository.findByEmail("seguidor@test.com")).willReturn(Optional.of(usuarioSeguidor));
        given(relacionUsuarioRepository.findBySeguidorAndEstado(usuarioSeguidor, EstadoSolicitud.ACEPTADO))
                .willReturn(List.of(relacion));
        given(usuarioMapper.toSimpleDto(any())).willReturn(usuarioSimpleDTO);

        List<UsuarioSimpleDTO> resultado = relacionService.obtenerMisSiguiendo("seguidor@test.com");

        assertEquals(1, resultado.size());
    }

    // --- Tests para obtenerSolicitudesPendientes ---
    @Test
    public void obtenerSolicitudesPendientes_retornaListaDTO() {
        given(usuarioRepository.findByEmail("seguido@test.com")).willReturn(Optional.of(usuarioSeguido));
        relacion.setEstado(EstadoSolicitud.PENDIENTE);
        given(relacionUsuarioRepository.findBySeguidoAndEstado(usuarioSeguido, EstadoSolicitud.PENDIENTE))
                .willReturn(List.of(relacion));
        given(usuarioMapper.toSimpleDto(any())).willReturn(usuarioSimpleDTO);

        List<UsuarioSimpleDTO> resultado = relacionService.obtenerSolicitudesPendientes("seguido@test.com");

        assertEquals(1, resultado.size());
    }

    // --- Tests para eliminarSeguidor ---
    @Test
    public void eliminarSeguidor_exito_eliminaRelacionAceptada() {
        relacion.setEstado(EstadoSolicitud.ACEPTADO); 
        given(usuarioRepository.findByEmail("seguido@test.com")).willReturn(Optional.of(usuarioSeguido));
        given(usuarioRepository.findByNombreUsuario("Seguidor")).willReturn(Optional.of(usuarioSeguidor));
        given(relacionUsuarioRepository.findById(any(RelacionUsuarioId.class))).willReturn(Optional.of(relacion));

        relacionService.eliminarSeguidor("seguido@test.com", "Seguidor");

        verify(relacionUsuarioRepository).delete(relacion);
    }

    @Test
    public void eliminarSeguidor_noAceptado_lanzaConflict() {
        relacion.setEstado(EstadoSolicitud.PENDIENTE);
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuarioSeguido));
        given(usuarioRepository.findByNombreUsuario(anyString())).willReturn(Optional.of(usuarioSeguidor));
        given(relacionUsuarioRepository.findById(any())).willReturn(Optional.of(relacion));

        assertThrows(ConflictException.class, () -> {
            relacionService.eliminarSeguidor("seguido@test.com", "Seguidor");
        });
        verify(relacionUsuarioRepository, never()).delete(any());
    }

    // --- Tests para dejarDeSeguir ---
    @Test
    public void dejarDeSeguir_exito_eliminaRelacion() {
        given(usuarioRepository.findByEmail("seguidor@test.com")).willReturn(Optional.of(usuarioSeguidor));
        given(usuarioRepository.findByNombreUsuario("Seguido")).willReturn(Optional.of(usuarioSeguido));

        relacionService.dejarDeSeguir("seguidor@test.com", "Seguido");

        verify(relacionUsuarioRepository).deleteById(any(RelacionUsuarioId.class));
    }

    @Test
    public void dejarDeSeguir_usuarioNoEncontrado_lanzaResourceNotFound() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> {
            relacionService.dejarDeSeguir("fantasma@test.com", "Seguido");
        });
        verify(relacionUsuarioRepository, never()).deleteById(any());
    }

    // --- Tests para obtenerSiguiendoDe ---

    @Test
    public void obtenerSiguiendoDe_propietario_accesoPermitido() {
        // Given
        given(usuarioRepository.findByNombreUsuario("Seguido")).willReturn(Optional.of(usuarioSeguido));
        given(relacionUsuarioRepository.findBySeguidorAndEstado(usuarioSeguido, EstadoSolicitud.ACEPTADO))
                .willReturn(List.of(relacion));
        given(usuarioMapper.toSimpleDto(any())).willReturn(usuarioSimpleDTO);

        // When
        List<UsuarioSimpleDTO> resultado = relacionService.obtenerSiguiendoDe("seguido@test.com", "Seguido");

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.size());
    }

    @Test
    public void obtenerSiguiendoDe_noSeguidor_accesoDenegado() {
        // Given
        given(usuarioRepository.findByNombreUsuario("Seguido")).willReturn(Optional.of(usuarioSeguido));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            relacionService.obtenerSiguiendoDe("extrano@test.com", "Seguido");
        });
    }

    @Test
    public void verificarPermiso_cuandoEsSeguidorAceptado_permiteAcceso() {
        // Given
        Usuario perfil = new Usuario();
        perfil.setEmail("perfil@test.com");
        perfil.setNombreUsuario("Perfil");
        perfil.setSeguidores(new java.util.HashSet<>());

        Usuario viewer = new Usuario();
        viewer.setEmail("viewer@test.com");

        RelacionUsuario relacion = new RelacionUsuario();
        relacion.setSeguidor(viewer);
        relacion.setSeguido(perfil);
        relacion.setEstado(EstadoSolicitud.ACEPTADO);

        perfil.getSeguidores().add(relacion);

        given(usuarioRepository.findByNombreUsuario("Perfil")).willReturn(Optional.of(perfil));
        given(relacionUsuarioRepository.findBySeguidoAndEstado(any(), any())).willReturn(new ArrayList<>());

        // When
        List<UsuarioSimpleDTO> resultado = relacionService.obtenerSeguidoresDe("viewer@test.com", "Perfil");

        // Then
        assertNotNull(resultado);
    }

    @Test
    public void solicitarSeguimiento_seguidorNoEncontrado_lanzaNotFound() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> relacionService.solicitarSeguimiento("fail", "user"));
    }

    @Test
    public void solicitarSeguimiento_seguidoNoEncontrado_lanzaNotFound() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuarioSeguidor));
        given(usuarioRepository.findByNombreUsuario(anyString())).willReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> relacionService.solicitarSeguimiento("ok", "fail"));
    }

    @Test
    public void aceptarSolicitud_relacionNoEncontrada_lanzaRuntimeException() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuarioSeguido));
        given(usuarioRepository.findByNombreUsuario(anyString())).willReturn(Optional.of(usuarioSeguidor));

        given(relacionUsuarioRepository.findById(any(RelacionUsuarioId.class))).willReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> relacionService.aceptarSolicitud("seguido@test.com", "Seguidor"));
    }

    // --- Tests Adicionales para Cobertura ---

    @Test
    public void rechazarSolicitud_solicitudNoEncontrada_lanzaRuntimeException() {
        // Given
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuarioSeguido));
        given(usuarioRepository.findByNombreUsuario(anyString())).willReturn(Optional.of(usuarioSeguidor));
        given(relacionUsuarioRepository.findById(any(RelacionUsuarioId.class))).willReturn(Optional.empty());

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            relacionService.rechazarSolicitud("seguido@test.com", "Seguidor");
        });
        verify(relacionUsuarioRepository, never()).delete(any());
    }

    @Test
    public void obtenerMisSeguidores_usuarioNoEncontrado_lanzaUsernameNotFoundException() {
        // Given
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            relacionService.obtenerMisSeguidores("fantasma@test.com");
        });
    }

    @Test
    public void obtenerMisSiguiendo_usuarioNoEncontrado_lanzaUsernameNotFoundException() {
        // Given
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            relacionService.obtenerMisSiguiendo("fantasma@test.com");
        });
    }

    @Test
    public void obtenerSolicitudesPendientes_usuarioNoEncontrado_lanzaUsernameNotFoundException() {
        // Given
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            relacionService.obtenerSolicitudesPendientes("fantasma@test.com");
        });
    }

    @Test
    public void eliminarSeguidor_cuandoRelacionNoEstaAceptada_lanzaConflictException() {
        // Given
        relacion.setEstado(EstadoSolicitud.PENDIENTE); 

        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuarioSeguido));
        given(usuarioRepository.findByNombreUsuario(anyString())).willReturn(Optional.of(usuarioSeguidor));
        given(relacionUsuarioRepository.findById(any(RelacionUsuarioId.class))).willReturn(Optional.of(relacion));

        // When & Then
        assertThrows(ConflictException.class, () -> {
            relacionService.eliminarSeguidor("seguido@test.com", "Seguidor");
        });

        verify(relacionUsuarioRepository, never()).delete(any());
    }


    @Test
    public void aceptarSolicitud_receptorNoEncontrado_lanzaUsernameNotFoundException() {
        // Given
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            relacionService.aceptarSolicitud("fantasma@test.com", "Seguidor");
        });
        verify(relacionUsuarioRepository, never()).save(any());
    }

    @Test
    public void aceptarSolicitud_emisorNoEncontrado_lanzaUsernameNotFoundException() {
        // Given
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuarioSeguido));
        given(usuarioRepository.findByNombreUsuario(anyString())).willReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            relacionService.aceptarSolicitud("seguido@test.com", "Fantasma");
        });
        verify(relacionUsuarioRepository, never()).save(any());
    }

    @Test
    public void rechazarSolicitud_receptorNoEncontrado_lanzaUsernameNotFoundException() {
        // Given
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            relacionService.rechazarSolicitud("fantasma@test.com", "Seguidor");
        });
        verify(relacionUsuarioRepository, never()).delete(any());
    }

    @Test
    public void rechazarSolicitud_emisorNoEncontrado_lanzaUsernameNotFoundException() {
        // Given
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuarioSeguido));
        given(usuarioRepository.findByNombreUsuario(anyString())).willReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            relacionService.rechazarSolicitud("seguido@test.com", "Fantasma");
        });
        verify(relacionUsuarioRepository, never()).delete(any());
    }


    @Test
    public void verificarPermiso_cuandoEsSeguidorPeroNoAceptado_deniegaAcceso() {
        // Given
        Usuario perfil = new Usuario();
        perfil.setEmail("perfil@test.com");
        perfil.setNombreUsuario("Perfil");
        perfil.setSeguidores(new java.util.HashSet<>());

        Usuario viewer = new Usuario();
        viewer.setEmail("viewer@test.com");

       
        RelacionUsuario relacion = new RelacionUsuario();
        relacion.setSeguidor(viewer);
        relacion.setSeguido(perfil);
        relacion.setEstado(EstadoSolicitud.PENDIENTE);

        perfil.getSeguidores().add(relacion);

        given(usuarioRepository.findByNombreUsuario("Perfil")).willReturn(Optional.of(perfil));

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            relacionService.obtenerSeguidoresDe("viewer@test.com", "Perfil");
        });
    }

    @Test
    public void obtenerSeguidoresDe_perfilNoEncontrado_lanzaUsernameNotFoundException() {
        // Given
        given(usuarioRepository.findByNombreUsuario("fantasma")).willReturn(Optional.empty());

        // When & Then
        assertThrows(UsernameNotFoundException.class, () -> {
            relacionService.obtenerSeguidoresDe("viewer@test.com", "fantasma");
        });
        
        verify(relacionUsuarioRepository, never()).findBySeguidoAndEstado(any(), any());
    }

    @Test
    public void verificarPermiso_cuandoOtroEsSeguidorAceptadoPeroNoViewer_deniegaAcceso() {
        // Given
        Usuario perfil = new Usuario();
        perfil.setEmail("perfil@test.com");
        perfil.setNombreUsuario("Perfil");
        perfil.setSeguidores(new java.util.HashSet<>());

        Usuario viewer = new Usuario();
        viewer.setEmail("viewer@test.com"); 

        Usuario otroSeguidor = new Usuario();
        otroSeguidor.setEmail("otro@test.com"); 

        RelacionUsuario relacion = new RelacionUsuario();
        relacion.setSeguidor(otroSeguidor);
        relacion.setSeguido(perfil);
        relacion.setEstado(EstadoSolicitud.ACEPTADO);
        
        perfil.getSeguidores().add(relacion);

       
        given(usuarioRepository.findByNombreUsuario("Perfil")).willReturn(Optional.of(perfil));
        

        // When & Then
        assertThrows(AccessDeniedException.class, () -> {
            relacionService.obtenerSeguidoresDe("viewer@test.com", "Perfil");
        });
    }
}