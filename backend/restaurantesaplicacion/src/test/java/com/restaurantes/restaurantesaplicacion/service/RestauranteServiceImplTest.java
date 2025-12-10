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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.restaurantes.restaurantesaplicacion.dto.RestauranteDTO;
import com.restaurantes.restaurantesaplicacion.dto.RestauranteImportDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.ElementoRestauranteDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.RespuestaApiDTO;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.mapper.RestauranteMapper;
import com.restaurantes.restaurantesaplicacion.model.Restaurante;
import com.restaurantes.restaurantesaplicacion.repository.RestauranteRepository;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@ExtendWith(MockitoExtension.class)
public class RestauranteServiceImplTest {

    @Mock
    private RestauranteRepository restauranteRepository;
    @Mock
    private RestauranteMapper restauranteMapper;
    @Mock
    private OpenStreetMapService openStreetMapService;

    @Mock private Root<Restaurante> root;
    @Mock private CriteriaQuery<?> query;
    @Mock private CriteriaBuilder cb;
    @Mock private Path<Object> path; 
    @Mock private Expression<String> expression;
    @Mock private Predicate predicate;

    @InjectMocks
    private RestauranteServiceImpl restauranteService;

    private Restaurante restaurante;
    private RestauranteDTO restauranteDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        restaurante = new Restaurante();
        restaurante.setId(1L);
        restaurante.setOsmId(12345L);
        restaurante.setNombre("Restaurante de Prueba");

        restauranteDTO = new RestauranteDTO();
        restauranteDTO.setId(1L);
        restauranteDTO.setOsmId(12345L);
        restauranteDTO.setNombre("Restaurante de Prueba");

        pageable = PageRequest.of(0, 10);
        lenient().when(root.get(anyString())).thenReturn(path);
        lenient().when(cb.lower(any())).thenReturn(expression);
        lenient().when(cb.like(any(), anyString())).thenReturn(predicate);
        lenient().when(cb.equal(any(), any())).thenReturn(predicate);
        lenient().when(cb.and(any())).thenReturn(predicate);
    }

    // --- 1. Test para guardarRestaurante ---
    @Test
    public void guardarRestaurante_cuandoEsExitoso_retornaDTO() {
        // Given
        given(restauranteMapper.toEntity(restauranteDTO)).willReturn(restaurante);
        given(restauranteRepository.save(restaurante)).willReturn(restaurante);
        given(restauranteMapper.toDto(restaurante)).willReturn(restauranteDTO);

        // When
        RestauranteDTO resultado = restauranteService.guardarRestaurante(restauranteDTO);

        // Then
        assertNotNull(resultado);
        assertEquals("Restaurante de Prueba", resultado.getNombre());
        verify(restauranteRepository, times(1)).save(restaurante);
    }

    // --- 2. Tests para obtenerRestaurantePorId ---
    @Test
    public void obtenerRestaurantePorId_cuandoExiste_retornaDTO() {
        // Given
        given(restauranteRepository.findById(1L)).willReturn(Optional.of(restaurante));
        given(restauranteMapper.toDto(restaurante)).willReturn(restauranteDTO);

        // When
        RestauranteDTO resultado = restauranteService.obtenerRestaurantePorId(1L);

        // Then
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }

    @Test
    public void obtenerRestaurantePorId_cuandoNoExiste_lanzaResourceNotFoundException() {
        // Given
        given(restauranteRepository.findById(99L)).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            restauranteService.obtenerRestaurantePorId(99L);
        });
        verify(restauranteMapper, never()).toDto(any());
    }

    // --- 3. Test para buscarRestaurantesExternos ---
    @Test
    public void buscarRestaurantesExternos_delegaCorrectamente() {
        // Given
        Page<ElementoRestauranteDTO> paginaEsperada = new PageImpl<>(Collections.emptyList());
        given(openStreetMapService.buscarRestaurantesExternos("Madrid", "Test", null, null, pageable))
                .willReturn(paginaEsperada);

        // When
        Page<ElementoRestauranteDTO> resultado = restauranteService.buscarRestaurantesExternos("Madrid", "Test", null, null, pageable);

        // Then
        assertNotNull(resultado);
        assertSame(paginaEsperada, resultado);
        verify(openStreetMapService, times(1)).buscarRestaurantesExternos("Madrid", "Test", null, null, pageable);
    }


    // --- 5. Tests para sincronizarRestaurantesExternos ---
    @Test
    public void sincronizarRestaurantesExternos_cuandoRestauranteEsNuevo_loGuarda() {
        // Given
        ElementoRestauranteDTO elementoNuevo = new ElementoRestauranteDTO();
        elementoNuevo.setId(111L);
        elementoNuevo.setTags(new HashMap<>(Map.of("name", "Restaurante Nuevo", "cuisine", "Tapas")));
        RespuestaApiDTO respuestaApi = new RespuestaApiDTO();
        respuestaApi.setElements(List.of(elementoNuevo));

        given(openStreetMapService.buscarTodosRestaurantesExternos("Madrid", null, null, null))
                .willReturn(respuestaApi);
        given(restauranteRepository.findByOsmId(111L)).willReturn(Optional.empty());
        ArgumentCaptor<Restaurante> restauranteCaptor = ArgumentCaptor.forClass(Restaurante.class);

        // When
        restauranteService.sincronizarRestaurantesExternos("Madrid");

        // Then
        verify(restauranteRepository, times(1)).save(restauranteCaptor.capture());
        assertEquals("Restaurante Nuevo", restauranteCaptor.getValue().getNombre());
        assertEquals(111L, restauranteCaptor.getValue().getOsmId());
    }

    @Test
    public void sincronizarRestaurantesExternos_cuandoRestauranteYaExiste_noLoGuarda() {
        // Given
        ElementoRestauranteDTO elementoExistente = new ElementoRestauranteDTO();
        elementoExistente.setId(12345L);
        elementoExistente.setTags(new HashMap<>(Map.of("name", "Restaurante de Prueba")));
        RespuestaApiDTO respuestaApi = new RespuestaApiDTO();
        respuestaApi.setElements(List.of(elementoExistente));

        given(openStreetMapService.buscarTodosRestaurantesExternos("Madrid", null, null, null))
                .willReturn(respuestaApi);
        given(restauranteRepository.findByOsmId(12345L)).willReturn(Optional.of(restaurante));

        // When
        restauranteService.sincronizarRestaurantesExternos("Madrid");

        // Then
        verify(restauranteRepository, never()).save(any());
    }

    // --- 6. Tests para importarRestauranteExterno ---
    @Test
    public void importarRestauranteExterno_cuandoNoExiste_loCreaYRetornaDTO() {
        // Given
        RestauranteImportDTO importDTO = new RestauranteImportDTO();
        importDTO.setOsmId(222L);
        importDTO.setNombre("Importado");

        given(restauranteRepository.findByOsmId(222L)).willReturn(Optional.empty());
        ArgumentCaptor<Restaurante> restauranteCaptor = ArgumentCaptor.forClass(Restaurante.class);
        given(restauranteRepository.save(restauranteCaptor.capture())).willAnswer(invocation -> invocation.getArgument(0));
        given(restauranteMapper.toDto(any(Restaurante.class))).willAnswer(invocation -> {
            Restaurante r = invocation.getArgument(0);
            RestauranteDTO dto = new RestauranteDTO();
            dto.setOsmId(r.getOsmId());
            dto.setNombre(r.getNombre());
            return dto;
        });

        // When
        RestauranteDTO resultado = restauranteService.importarRestauranteExterno(importDTO);

        // Then
        verify(restauranteRepository, times(1)).save(any(Restaurante.class));
        assertEquals(222L, restauranteCaptor.getValue().getOsmId());
        assertEquals("Importado", resultado.getNombre());
    }

    @Test
    public void importarRestauranteExterno_cuandoYaExiste_noCreaYRetornaDTO() {
        // Given
        RestauranteImportDTO importDTO = new RestauranteImportDTO();
        importDTO.setOsmId(12345L);

        given(restauranteRepository.findByOsmId(12345L)).willReturn(Optional.of(restaurante));
        given(restauranteMapper.toDto(restaurante)).willReturn(restauranteDTO);

        // When
        RestauranteDTO resultado = restauranteService.importarRestauranteExterno(importDTO);

        // Then
        verify(restauranteRepository, never()).save(any());
        assertEquals(restauranteDTO.getNombre(), resultado.getNombre());
    }


    // --- 4. Test para buscarRestaurantes (locales) ---

    @Test
    public void buscarRestaurantes_conTodosLosFiltros_retornaPaginaDTO() {
        // Given
        Page<Restaurante> paginaEntidades = new PageImpl<>(List.of(restaurante));
        given(restauranteRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(paginaEntidades);
        given(restauranteMapper.toDto(restaurante)).willReturn(restauranteDTO);
        ArgumentCaptor<Specification<Restaurante>> specCaptor = ArgumentCaptor.forClass(Specification.class);

        // When
        restauranteService.buscarRestaurantes("Prueba", "Madrid", "Tapas", "Calle", pageable);

        // Then
        verify(restauranteRepository).findAll(specCaptor.capture(), any(Pageable.class));
        specCaptor.getValue().toPredicate(root, query, cb);
    }

    @Test
    public void buscarRestaurantes_sinFiltros_retornaPaginaDTO() {
        // Given
        Page<Restaurante> paginaEntidades = new PageImpl<>(List.of(restaurante));
        given(restauranteRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(paginaEntidades);
        given(restauranteMapper.toDto(restaurante)).willReturn(restauranteDTO);

        ArgumentCaptor<Specification<Restaurante>> specCaptor = ArgumentCaptor.forClass(Specification.class);

        // When
        restauranteService.buscarRestaurantes(null, null, null, null, pageable);

        // Then
        verify(restauranteRepository).findAll(specCaptor.capture(), any(Pageable.class));
        specCaptor.getValue().toPredicate(root, query, cb);
    }
    @Test
    public void buscarRestaurantes_conFiltrosVacios_retornaPaginaDTO() {
        // Given
        Page<Restaurante> paginaEntidades = new PageImpl<>(List.of(restaurante));
        given(restauranteRepository.findAll(any(Specification.class), any(Pageable.class)))
                .willReturn(paginaEntidades);
        given(restauranteMapper.toDto(restaurante)).willReturn(restauranteDTO);
        ArgumentCaptor<Specification<Restaurante>> specCaptor = ArgumentCaptor.forClass(Specification.class);

        // When
        restauranteService.buscarRestaurantes("", "", "", "", pageable);

        // Then
        verify(restauranteRepository).findAll(specCaptor.capture(), any(Pageable.class));
        specCaptor.getValue().toPredicate(root, query, cb);
    }
}