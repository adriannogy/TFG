package com.restaurantes.restaurantesaplicacion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.restaurantes.restaurantesaplicacion.client.NominatimClient;
import com.restaurantes.restaurantesaplicacion.client.OpenStreetMapClient;
import com.restaurantes.restaurantesaplicacion.dto.osm.ElementoRestauranteDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.GeocodingResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.RespuestaApiDTO;
import com.restaurantes.restaurantesaplicacion.exception.ConflictException;

@ExtendWith(MockitoExtension.class)
public class OpenStreetMapServiceImplTest {

    @Mock
    private NominatimClient nominatimClient;

    @Mock
    private OpenStreetMapClient openStreetMapClient;

    @InjectMocks
    private OpenStreetMapServiceImpl openStreetMapService;

    private GeocodingResponseDTO mockGeoData;
    private RespuestaApiDTO mockRespuestaApi;

    @BeforeEach
    void setUp() {
        mockGeoData = new GeocodingResponseDTO();
        mockGeoData.setOsmId(12345L);
        mockGeoData.setOsmType("relation");

       
        mockRespuestaApi = new RespuestaApiDTO();
        mockRespuestaApi.setElements(Collections.singletonList(new ElementoRestauranteDTO()));
    }

    // --- Tests para buscarTodosRestaurantesExternos ---

    @Test
    public void buscarTodosRestaurantesExternos_cuandoCiudadNoSeEncuentra_lanzaConflictException() {
        // Given
        given(nominatimClient.searchForCity(anyString(), anyString(), any(Integer.class)))
                .willReturn(Collections.emptyList());

        // When & Then
        assertThrows(ConflictException.class, () -> {
            openStreetMapService.buscarTodosRestaurantesExternos("CiudadFalsa", null, null, null);
        });

        verify(openStreetMapClient, never()).buscarRestaurantes(anyString());
    }

    @Test
    public void buscarTodosRestaurantesExternos_conFiltros_construyeQueryCorrecta() {
        // Given
        given(nominatimClient.searchForCity(anyString(), anyString(), any(Integer.class)))
                .willReturn(List.of(mockGeoData));
        
        given(openStreetMapClient.buscarRestaurantes(anyString())).willReturn(mockRespuestaApi);
        
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        // When
        openStreetMapService.buscarTodosRestaurantesExternos("Madrid", "Restaurante", "tapas", "Calle Falsa");

        // Then
        verify(openStreetMapClient).buscarRestaurantes(queryCaptor.capture());
        String queryEnviada = queryCaptor.getValue();

        assertTrue(queryEnviada.contains("area(3600012345)"));
        assertTrue(queryEnviada.contains("[~\"name\"~\"^Restaurante\",i]"));
        assertTrue(queryEnviada.contains("[cuisine=tapas]"));
        assertTrue(queryEnviada.contains("[~\"addr:street\"~\"Calle Falsa\",i]"));
    }

    @Test
    public void buscarTodosRestaurantesExternos_sinFiltros_construyeQuerySimple() {
        // Given
        given(nominatimClient.searchForCity(anyString(), anyString(), any(Integer.class)))
                .willReturn(List.of(mockGeoData));
        
        given(openStreetMapClient.buscarRestaurantes(anyString())).willReturn(mockRespuestaApi);
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        // When
        openStreetMapService.buscarTodosRestaurantesExternos("Madrid", null, "", null);

        // Then
        verify(openStreetMapClient).buscarRestaurantes(queryCaptor.capture());
        String queryEnviada = queryCaptor.getValue();

        assertFalse(queryEnviada.contains("[~\"name\"~"));
        assertFalse(queryEnviada.contains("[cuisine="));
        assertFalse(queryEnviada.contains("[~\"addr:street\"~"));
        assertTrue(queryEnviada.contains("area(3600012345)"));
    }

    // --- Tests para buscarRestaurantesExternos (Paginación) ---

    @Test
    public void buscarRestaurantesExternos_conPaginacion_retornaPaginaCorrecta() {
        // Given
        List<ElementoRestauranteDTO> listaCompleta = IntStream.range(0, 25)
                .mapToObj(i -> {
                    ElementoRestauranteDTO e = new ElementoRestauranteDTO();
                    e.setId((long) i);
                    return e;
                })
                .collect(Collectors.toList());
        
        RespuestaApiDTO respuestaApiCompleta = new RespuestaApiDTO();
        respuestaApiCompleta.setElements(listaCompleta);

        given(nominatimClient.searchForCity(anyString(), anyString(), any(Integer.class)))
                .willReturn(List.of(mockGeoData));
        given(openStreetMapClient.buscarRestaurantes(anyString()))
                .willReturn(respuestaApiCompleta);

        Pageable pageable = PageRequest.of(1, 10);

        // When
        Page<ElementoRestauranteDTO> resultado = openStreetMapService.buscarRestaurantesExternos("Madrid", null, null, null, pageable);

        // Then
        assertEquals(25, resultado.getTotalElements()); 
        assertEquals(3, resultado.getTotalPages());   
        assertEquals(10, resultado.getContent().size()); 
        assertEquals(1L, resultado.getNumber());  
        assertEquals(10L, resultado.getContent().get(0).getId());
    }

    @Test
    public void buscarRestaurantesExternos_paginaFueraDeRango_retornaPaginaVacia() {
        // Given
        RespuestaApiDTO respuestaApiCompleta = new RespuestaApiDTO();
        respuestaApiCompleta.setElements(Collections.emptyList());

        given(nominatimClient.searchForCity(anyString(), anyString(), any(Integer.class)))
                .willReturn(List.of(mockGeoData));
        given(openStreetMapClient.buscarRestaurantes(anyString()))
                .willReturn(respuestaApiCompleta);

        Pageable pageable = PageRequest.of(2, 10);

        // When
        Page<ElementoRestauranteDTO> resultado = openStreetMapService.buscarRestaurantesExternos("Madrid", null, null, null, pageable);

        // Then
        assertEquals(0, resultado.getTotalElements());
        assertTrue(resultado.getContent().isEmpty());
    }

    // --- Tests para los Fallbacks ---

    @Test
    public void testFallbackBuscarTodos() {
        // Given
        Throwable ex = new RuntimeException("Error de red");

        // When
        RespuestaApiDTO resultado = openStreetMapService.fallbackBuscarTodos("Madrid", null, null, ex);

        // Then
        assertNotNull(resultado);
        assertNotNull(resultado.getElements());
        assertTrue(resultado.getElements().isEmpty());
    }

    @Test
    public void testFallbackBuscarRestaurantes() {
        // Given
        Throwable ex = new RuntimeException("Error de red");
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<ElementoRestauranteDTO> resultado = openStreetMapService.fallbackBuscarRestaurantes("Madrid", null, null, pageable, ex);

        // Then
        assertNotNull(resultado);
        assertEquals(0, resultado.getTotalElements());
        assertTrue(resultado.getContent().isEmpty());
    }

    // --- Tests Adicionales para Cobertura ---

    @Test
    public void buscarTodosRestaurantesExternos_cuandoOsmTypeEsWay_calculaAreaIdCorrectamente() {
        // Given
        mockGeoData.setOsmType("way");
        mockGeoData.setOsmId(10L);    

        given(nominatimClient.searchForCity(anyString(), anyString(), any(Integer.class)))
                .willReturn(List.of(mockGeoData));
        
        given(openStreetMapClient.buscarRestaurantes(anyString())).willReturn(mockRespuestaApi);
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        // When
        openStreetMapService.buscarTodosRestaurantesExternos("Madrid", null, null, null);

        // Then
        verify(openStreetMapClient).buscarRestaurantes(queryCaptor.capture());
        String queryEnviada = queryCaptor.getValue();

        assertTrue(queryEnviada.contains("area(2400000010)"));
    }

    @Test
    public void buscarTodosRestaurantesExternos_cuandoFiltrosSonStringsVacios_noAñadeFiltros() {
        // Given
        given(nominatimClient.searchForCity(anyString(), anyString(), any(Integer.class)))
                .willReturn(List.of(mockGeoData));
        
        given(openStreetMapClient.buscarRestaurantes(anyString())).willReturn(mockRespuestaApi);
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        // When
        openStreetMapService.buscarTodosRestaurantesExternos("Madrid", "", "", "");

        // Then
        verify(openStreetMapClient).buscarRestaurantes(queryCaptor.capture());
        String queryEnviada = queryCaptor.getValue();

        assertFalse(queryEnviada.contains("[~\"name\"~"));
        assertFalse(queryEnviada.contains("[cuisine="));
        assertFalse(queryEnviada.contains("[~\"addr:street\"~"));
    }

    @Test
    public void buscarTodosRestaurantesExternos_cuandoOsmTypeEsNode_usaOsmIdOriginal() {
        // Given
        mockGeoData.setOsmType("node");
        mockGeoData.setOsmId(999L);

        given(nominatimClient.searchForCity(anyString(), anyString(), any(Integer.class)))
                .willReturn(List.of(mockGeoData));
        
        given(openStreetMapClient.buscarRestaurantes(anyString())).willReturn(mockRespuestaApi);
        ArgumentCaptor<String> queryCaptor = ArgumentCaptor.forClass(String.class);

        // When
        openStreetMapService.buscarTodosRestaurantesExternos("Madrid", null, null, null);

        // Then
        verify(openStreetMapClient).buscarRestaurantes(queryCaptor.capture());
        String queryEnviada = queryCaptor.getValue();

        assertTrue(queryEnviada.contains("area(999)"));
    }
}