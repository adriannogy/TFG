package com.restaurantes.restaurantesaplicacion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import com.restaurantes.restaurantesaplicacion.model.RelacionUsuario;
import com.restaurantes.restaurantesaplicacion.model.Restaurante;
import com.restaurantes.restaurantesaplicacion.model.Usuario;
import com.restaurantes.restaurantesaplicacion.model.Valoracion;
import com.restaurantes.restaurantesaplicacion.model.ValoracionId;
import com.restaurantes.restaurantesaplicacion.repository.RestauranteRepository;
import com.restaurantes.restaurantesaplicacion.repository.UsuarioRepository;
import com.restaurantes.restaurantesaplicacion.repository.ValoracionRepository;

@ExtendWith(MockitoExtension.class)
public class ValoracionServiceImplTest {

    @Mock private ValoracionRepository valoracionRepository;
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private RestauranteRepository restauranteRepository;
    @Mock private ValoracionMapper valoracionMapper;
    @Mock private FileStorageService fileStorageService;
    @Mock private OpenStreetMapService openStreetMapService;

    @InjectMocks
    private ValoracionServiceImpl valoracionService;

    private Usuario usuario;
    private Restaurante restaurante;
    private ValoracionInputDTO inputDTO;
    private Valoracion valoracion;
    private ValoracionDTO valoracionDTO;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setNombreUsuario("testUser");
        usuario.setEmail("test@test.com");

        restaurante = new Restaurante();
        restaurante.setId(10L);
        restaurante.setNombre("Restaurante Test");
        restaurante.setOsmId(12345L);

        inputDTO = new ValoracionInputDTO();
        inputDTO.setNombreRestaurante("Restaurante Test");
        inputDTO.setCiudad("Madrid");
        inputDTO.setPuntuacion(5);
        inputDTO.setComentario("Genial");

        valoracion = new Valoracion();
        ValoracionId id = new ValoracionId();
        id.setUsuarioId(usuario.getId());
        id.setRestauranteId(restaurante.getId());
        valoracion.setId(id);
        valoracion.setUsuario(usuario);
        valoracion.setRestaurante(restaurante);
        valoracion.setPuntuacion(5);

        valoracionDTO = new ValoracionDTO();
        valoracionDTO.setNombreUsuario("testUser");
        valoracionDTO.setPuntuacion(5);
    }

    // --- Tests para crearValoracion ---

    @Test
    public void crearValoracion_cuandoRestauranteExiste_loCrea() {
        // Given
        given(usuarioRepository.findByNombreUsuario("testUser")).willReturn(Optional.of(usuario));
        given(restauranteRepository.findByNombre("Restaurante Test")).willReturn(Optional.of(restaurante));
        given(valoracionRepository.existsById(any(ValoracionId.class))).willReturn(false);
        given(valoracionRepository.save(any(Valoracion.class))).willReturn(valoracion);
        given(valoracionMapper.toDto(any(Valoracion.class))).willReturn(valoracionDTO);

        // When
        ValoracionDTO resultado = valoracionService.crearValoracion("testUser", inputDTO, null);

        // Then
        assertNotNull(resultado);
        assertEquals(5, resultado.getPuntuacion());
        verify(valoracionRepository, times(2)).save(any(Valoracion.class));
    }

    @Test
    public void crearValoracion_cuandoRestauranteNoExiste_loBuscaYLoCrea() {
        // Given
        given(usuarioRepository.findByNombreUsuario("testUser")).willReturn(Optional.of(usuario));
        given(restauranteRepository.findByNombre("Restaurante Test")).willReturn(Optional.empty());

        ElementoRestauranteDTO elementoOSM = new ElementoRestauranteDTO();
        elementoOSM.setId(999L);
        elementoOSM.setTags(new HashMap<>(Map.of("name", "Restaurante Test", "cuisine", "Tapas")));
        RespuestaApiDTO respuestaOSM = new RespuestaApiDTO();
        respuestaOSM.setElements(List.of(elementoOSM));

        given(openStreetMapService.buscarTodosRestaurantesExternos(anyString(), isNull(), isNull(), isNull()))
                .willReturn(respuestaOSM);
        given(restauranteRepository.save(any(Restaurante.class))).willReturn(restaurante);
        given(valoracionRepository.existsById(any(ValoracionId.class))).willReturn(false);
        given(valoracionRepository.save(any(Valoracion.class))).willReturn(valoracion);
        given(valoracionMapper.toDto(any(Valoracion.class))).willReturn(valoracionDTO);

        // When
        valoracionService.crearValoracion("testUser", inputDTO, null);

        // Then
        verify(openStreetMapService).buscarTodosRestaurantesExternos(eq("Madrid"), isNull(), isNull(), isNull());
        verify(restauranteRepository).save(any(Restaurante.class));
        verify(valoracionRepository, times(2)).save(any(Valoracion.class));
    }

    @Test
    public void crearValoracion_conFotos_subeLasFotos() {
        // Given
        given(usuarioRepository.findByNombreUsuario("testUser")).willReturn(Optional.of(usuario));
        given(restauranteRepository.findByNombre("Restaurante Test")).willReturn(Optional.of(restaurante));
        given(valoracionRepository.save(any(Valoracion.class))).willReturn(valoracion);
        given(valoracionMapper.toDto(any(Valoracion.class))).willReturn(valoracionDTO);
        given(fileStorageService.store(any())).willReturn("http://fake-url.com/foto.jpg");

        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);

        // When
        valoracionService.crearValoracion("testUser", inputDTO, new MultipartFile[]{mockFile});

        // Then
        verify(fileStorageService, times(1)).store(mockFile);
        assertEquals(1, valoracion.getFotos().size());
    }

    @Test
    public void crearValoracion_cuandoYaExiste_lanzaValoracionDuplicadaException() {
        // Given
        given(usuarioRepository.findByNombreUsuario("testUser")).willReturn(Optional.of(usuario));
        given(restauranteRepository.findByNombre("Restaurante Test")).willReturn(Optional.of(restaurante));
        given(valoracionRepository.existsById(any(ValoracionId.class))).willReturn(true); // Ya existe

        // When & Then
        assertThrows(ValoracionDuplicadaException.class, () -> {
            valoracionService.crearValoracion("testUser", inputDTO, null);
        });
        verify(valoracionRepository, never()).save(any());
    }

    // --- Tests para obtenerTodas ---
    @Test
    public void obtenerTodas_retornaListaDTO() {
        // Given
        given(valoracionRepository.findAll()).willReturn(List.of(valoracion));
        given(valoracionMapper.toDto(valoracion)).willReturn(valoracionDTO);

        // When
        List<ValoracionDTO> resultado = valoracionService.obtenerTodas();

        // Then
        assertEquals(1, resultado.size());
        assertEquals("testUser", resultado.get(0).getNombreUsuario());
    }

    // --- Tests para obtenerFeedParaUsuario ---
    @Test
    public void obtenerFeedParaUsuario_conSeguidos_retornaPagina() {
        // Given
        Usuario seguido = new Usuario();
        seguido.setId(2L);
        RelacionUsuario relacion = new RelacionUsuario();
        relacion.setSeguidor(usuario);
        relacion.setSeguido(seguido);
        relacion.setEstado(EstadoSolicitud.ACEPTADO);
        usuario.getSiguiendo().add(relacion);

        Pageable pageable = PageRequest.of(0, 10);
        Page<Valoracion> paginaValoraciones = new PageImpl<>(List.of(valoracion));

        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        given(valoracionRepository.findByUsuarioInOrderByFechaCreacionDesc(anyList(), eq(pageable)))
                .willReturn(paginaValoraciones);
        given(valoracionMapper.toDto(valoracion)).willReturn(valoracionDTO);

        // When
        Page<ValoracionDTO> resultado = valoracionService.obtenerFeedParaUsuario("test@test.com", pageable);

        // Then
        assertEquals(1, resultado.getTotalElements());
        verify(valoracionRepository).findByUsuarioInOrderByFechaCreacionDesc(anyList(), eq(pageable));
    }

    @Test
    public void obtenerFeedParaUsuario_sinSeguidos_retornaPaginaVacia() {
        // Given
        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));

        // When
        Page<ValoracionDTO> resultado = valoracionService.obtenerFeedParaUsuario("test@test.com", PageRequest.of(0, 10));

        // Then
        assertTrue(resultado.isEmpty());
        verify(valoracionRepository, never()).findByUsuarioInOrderByFechaCreacionDesc(anyList(), any());
    }

    // --- Tests para eliminarValoracion ---

    @Test
    public void eliminarValoracion_cuandoEsPropia_laElimina() {
        // Given
        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        given(valoracionRepository.findById(any(ValoracionId.class))).willReturn(Optional.of(valoracion));
        willDoNothing().given(valoracionRepository).delete(valoracion);

        // When
        valoracionService.eliminarValoracion("test@test.com", 10L);

        // Then
        verify(valoracionRepository, times(1)).delete(valoracion);
    }

    @Test
    public void eliminarValoracion_cuandoNoEsPropia_lanzaSecurityException() {
        // Given
        Usuario otroUsuario = new Usuario();
        otroUsuario.setId(99L);
        
        given(usuarioRepository.findByEmail("otro@test.com")).willReturn(Optional.of(otroUsuario));
        given(valoracionRepository.findById(any(ValoracionId.class))).willReturn(Optional.of(valoracion));

        // When & Then
        assertThrows(SecurityException.class, () -> {
            valoracionService.eliminarValoracion("otro@test.com", 10L);
        });
        verify(valoracionRepository, never()).delete(any());
    }

    @Test
    public void eliminarValoracion_cuandoNoExiste_lanzaResourceNotFoundException() {
        // Given
        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        given(valoracionRepository.findById(any(ValoracionId.class))).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            valoracionService.eliminarValoracion("test@test.com", 10L);
        });
    }

    // --- Tests Adicionales para Cobertura (crearValoracion) ---

    @Test
    public void crearValoracion_conArchivoVacio_loIgnora() {
        // Given
        given(usuarioRepository.findByNombreUsuario("testUser")).willReturn(Optional.of(usuario));
        given(restauranteRepository.findByNombre("Restaurante Test")).willReturn(Optional.of(restaurante));
        given(valoracionRepository.save(any(Valoracion.class))).willReturn(valoracion);
        given(valoracionMapper.toDto(any(Valoracion.class))).willReturn(valoracionDTO);

        MultipartFile emptyFile = mock(MultipartFile.class);
        when(emptyFile.isEmpty()).thenReturn(true);

        // When
        valoracionService.crearValoracion("testUser", inputDTO, new MultipartFile[]{emptyFile});

        // Then
        verify(fileStorageService, never()).store(any());
    }

    @Test
    public void crearValoracion_cuandoFallaSubidaDeFoto_continuaYGuardaValoracion() {
        // Given
        given(usuarioRepository.findByNombreUsuario("testUser")).willReturn(Optional.of(usuario));
        given(restauranteRepository.findByNombre("Restaurante Test")).willReturn(Optional.of(restaurante));
        given(valoracionRepository.save(any(Valoracion.class))).willReturn(valoracion);
        given(valoracionMapper.toDto(any(Valoracion.class))).willReturn(valoracionDTO);

        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.isEmpty()).thenReturn(false);
        when(badFile.getOriginalFilename()).thenReturn("bad.jpg");
        when(fileStorageService.store(badFile)).thenThrow(new RuntimeException("Error de subida"));

        // When
        valoracionService.crearValoracion("testUser", inputDTO, new MultipartFile[]{badFile});

        // Then
        verify(valoracionRepository, times(2)).save(any(Valoracion.class));
    }


    @Test
    public void obtenerFeedParaUsuario_filtraRelacionesNoAceptadas() {
        // Given
        RelacionUsuario relacionPendiente = new RelacionUsuario();
        relacionPendiente.setEstado(EstadoSolicitud.PENDIENTE);
        usuario.getSiguiendo().add(relacionPendiente);

        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));

        // When
        Page<ValoracionDTO> resultado = valoracionService.obtenerFeedParaUsuario("test@test.com", PageRequest.of(0, 10));

        // Then
        assertTrue(resultado.isEmpty()); 
        verify(valoracionRepository, never()).findByUsuarioInOrderByFechaCreacionDesc(anyList(), any());
    }

    @Test
    public void crearValoracion_cuandoUsuarioNoExiste_lanzaUsernameNotFoundException() {
        // Given
        given(usuarioRepository.findByNombreUsuario(anyString())).willReturn(Optional.empty());

        // When & Then
        assertThrows(org.springframework.security.core.userdetails.UsernameNotFoundException.class, () -> {
            valoracionService.crearValoracion("usuarioFantasma", inputDTO, null);
        });

        verify(valoracionRepository, never()).save(any());
    }


    @Test
    public void crearValoracion_cuandoRestauranteNoExisteNiEnOsm_lanzaConflictException() {
        // Given
        given(usuarioRepository.findByNombreUsuario("testUser")).willReturn(Optional.of(usuario));
        given(restauranteRepository.findByNombre("Restaurante Fantasma")).willReturn(Optional.empty());

        RespuestaApiDTO respuestaVacia = new RespuestaApiDTO();
        respuestaVacia.setElements(Collections.emptyList());
        given(openStreetMapService.buscarTodosRestaurantesExternos(anyString(), isNull(), isNull(), isNull()))
                .willReturn(respuestaVacia);

        ValoracionInputDTO inputFantasma = new ValoracionInputDTO();
        inputFantasma.setNombreRestaurante("Restaurante Fantasma");
        inputFantasma.setCiudad("Madrid");
        inputFantasma.setPuntuacion(5);

        // When & Then
        assertThrows(ConflictException.class, () -> {
            valoracionService.crearValoracion("testUser", inputFantasma, null);
        });
        verify(restauranteRepository, never()).save(any());
    }

    @Test
    public void obtenerFeedParaUsuario_cuandoUsuarioNoExiste_lanzaResourceNotFoundException() {
        // Given
        given(usuarioRepository.findByEmail("fantasma@test.com")).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            valoracionService.obtenerFeedParaUsuario("fantasma@test.com", PageRequest.of(0, 10));
        });
    }

    @Test
    public void eliminarValoracion_cuandoUsuarioNoExiste_lanzaResourceNotFoundException() {
        // Given
        given(usuarioRepository.findByEmail("fantasma@test.com")).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            valoracionService.eliminarValoracion("fantasma@test.com", 10L);
        });
        verify(valoracionRepository, never()).delete(any());
    }

}