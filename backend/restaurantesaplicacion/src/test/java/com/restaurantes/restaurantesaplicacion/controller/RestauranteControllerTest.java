package com.restaurantes.restaurantesaplicacion.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.HashMap;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurantes.restaurantesaplicacion.dto.RestauranteDTO;
import com.restaurantes.restaurantesaplicacion.dto.RestauranteImportDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.ElementoRestauranteDTO;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.security.JwtUtil;
import com.restaurantes.restaurantesaplicacion.service.RestauranteService;

@WebMvcTest(RestauranteController.class)
@AutoConfigureMockMvc(addFilters = false)
public class RestauranteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestauranteService restauranteService;

    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private JwtUtil jwtUtil;

    private RestauranteDTO restauranteDTO;
    private Page<RestauranteDTO> paginaRestauranteDTO;
    private Page<ElementoRestauranteDTO> paginaElementoDTO;

    @BeforeEach
    void setUp() {
        restauranteDTO = new RestauranteDTO();
        restauranteDTO.setId(1L);
        restauranteDTO.setNombre("Restaurante Local");
        restauranteDTO.setCiudad("Ciudad Test");
        restauranteDTO.setOsmId(12345L);
        paginaRestauranteDTO = new PageImpl<>(List.of(restauranteDTO));

        ElementoRestauranteDTO elementoDTO = new ElementoRestauranteDTO();
        elementoDTO.setId(12345L);
        
        elementoDTO.setTags(new HashMap<>()); 

        paginaElementoDTO = new PageImpl<>(List.of(elementoDTO));
    }

    // --- Tests para buscarRestaurantes (GET /) ---

    @Test
    public void buscarRestaurantes_cuandoExisten_retornaPaginaY200OK() throws Exception {
        given(restauranteService.buscarRestaurantes(any(), any(), any(), any(), any(Pageable.class)))
                .willReturn(paginaRestauranteDTO);

        mockMvc.perform(get("/api/restaurantes")
                .param("ciudad", "Ciudad Test")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Restaurante Local"));
    }

    // --- Tests para obtenerRestaurantesExternos (GET /externos/buscar) ---

    @Test
    public void obtenerRestaurantesExternos_cuandoCiudadEsValida_retornaPaginaY200OK() throws Exception {
        given(restauranteService.buscarRestaurantesExternos(anyString(), any(), any(), any(), any(Pageable.class)))
                .willReturn(paginaElementoDTO);

        mockMvc.perform(get("/api/restaurantes/externos/buscar")
                .param("ciudad", "Madrid")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(12345L));
    }

    @Test
    public void obtenerRestaurantesExternos_cuandoCiudadNoExiste_retorna404NotFound() throws Exception {
        given(restauranteService.buscarRestaurantesExternos(anyString(), any(), any(), any(), any(Pageable.class)))
                .willThrow(new ResourceNotFoundException("Ciudad no encontrada"));

        mockMvc.perform(get("/api/restaurantes/externos/buscar")
                .param("ciudad", "FakeCity")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // --- Tests para sincronizar (POST /sincronizar) ---

    @Test
    public void sincronizar_cuandoEsExitoso_retornaMensajeY200OK() throws Exception {
        willDoNothing().given(restauranteService).sincronizarRestaurantesExternos(anyString());
        String ciudad = "Madrid";

        mockMvc.perform(post("/api/restaurantes/sincronizar")
                .param("ciudad", ciudad))
                .andExpect(status().isOk())
                .andExpect(content().string("Sincronización para la ciudad de " + ciudad + " iniciada."));
    }

    // --- Tests para obtenerPorId (GET /{id}) ---

    @Test
    public void obtenerPorId_cuandoIdExiste_retornaRestauranteY200OK() throws Exception {
        given(restauranteService.obtenerRestaurantePorId(1L)).willReturn(restauranteDTO);

        mockMvc.perform(get("/api/restaurantes/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nombre").value("Restaurante Local"));
    }

    @Test
    public void obtenerPorId_cuandoIdNoExiste_retorna404NotFound() throws Exception {
        given(restauranteService.obtenerRestaurantePorId(99L))
                .willThrow(new ResourceNotFoundException("Restaurante no encontrado"));

        mockMvc.perform(get("/api/restaurantes/{id}", 99L)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    // --- Tests para guardarRestaurante (POST /) ---

    @Test
    public void guardarRestaurante_cuandoDatosSonValidos_retornaRestauranteY201Created() throws Exception {
        given(restauranteService.guardarRestaurante(any(RestauranteDTO.class))).willReturn(restauranteDTO);

        mockMvc.perform(post("/api/restaurantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(restauranteDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Restaurante Local"));
    }

    @Test
    public void guardarRestaurante_cuandoDatosSonInvalidos_retorna400BadRequest() throws Exception {
        RestauranteDTO dtoInvalido = new RestauranteDTO();
        dtoInvalido.setNombre(null); 

        mockMvc.perform(post("/api/restaurantes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dtoInvalido)))
                .andExpect(status().isBadRequest());
    }

    // --- Tests para importarRestaurante (POST /importar) ---

    @Test
    public void importarRestaurante_cuandoDatosSonValidos_retornaRestauranteY200OK() throws Exception {
        RestauranteImportDTO importDTO = new RestauranteImportDTO();
        importDTO.setOsmId(12345L);
        importDTO.setNombre("Restaurante Importado");

        given(restauranteService.importarRestauranteExterno(any(RestauranteImportDTO.class))).willReturn(restauranteDTO);

        mockMvc.perform(post("/api/restaurantes/importar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(importDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Restaurante Local"));
    }
}