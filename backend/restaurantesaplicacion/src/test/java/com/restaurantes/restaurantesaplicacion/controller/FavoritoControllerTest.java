package com.restaurantes.restaurantesaplicacion.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.util.Collections;
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

import com.restaurantes.restaurantesaplicacion.dto.RestauranteDTO;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.security.JwtUtil;
import com.restaurantes.restaurantesaplicacion.service.FavoritoService;

@WebMvcTest(FavoritoController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FavoritoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FavoritoService favoritoService; // Mock de la dependencia principal

    // Mocks de seguridad
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private JwtUtil jwtUtil;

    private Principal mockPrincipal;
    private RestauranteDTO restauranteDTO;
    private Page<RestauranteDTO> paginaRestaurantes;

    @BeforeEach
    void setUp() {
       
        mockPrincipal = () -> "usuario@test.com";

        restauranteDTO = new RestauranteDTO();
        restauranteDTO.setId(1L);
        restauranteDTO.setNombre("Restaurante Falso");
        restauranteDTO.setCiudad("Ciudad Test");

       
        paginaRestaurantes = new PageImpl<>(List.of(restauranteDTO));
    }

    // --- Tests para obtenerMisFavoritos (GET /api/favoritos) ---

    @Test
    public void obtenerMisFavoritos_cuandoExistenFavoritos_retornaPaginaY200OK() throws Exception {
        // Given
        given(favoritoService.obtenerFavoritos(anyString(), any(Pageable.class))).willReturn(paginaRestaurantes);

        // When & Then
        mockMvc.perform(get("/api/favoritos")
                .principal(mockPrincipal)
                .param("page", "0")
                .param("size", "5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Restaurante Falso"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    public void obtenerMisFavoritos_cuandoNoExistenFavoritos_retornaPaginaVaciaY200OK() throws Exception {
        // Given
        Page<RestauranteDTO> paginaVacia = new PageImpl<>(Collections.emptyList());
        given(favoritoService.obtenerFavoritos(anyString(), any(Pageable.class))).willReturn(paginaVacia);

        // When & Then
        mockMvc.perform(get("/api/favoritos")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    // --- Tests para agregarFavorito (POST /api/favoritos/{restauranteId}) ---

    @Test
    public void agregarFavorito_cuandoDatosSonValidos_retorna200OK() throws Exception {
        // Given
        Long restauranteId = 1L;
       
        willDoNothing().given(favoritoService).agregarFavorito(anyString(), any(Long.class));

        // When & Then
        mockMvc.perform(post("/api/favoritos/{restauranteId}", restauranteId)
                .principal(mockPrincipal))
                .andExpect(status().isOk());
    }

    @Test
    public void agregarFavorito_cuandoRestauranteNoExiste_retorna404NotFound() throws Exception {
        // Given
        Long restauranteId = 99L;
        
        willThrow(new ResourceNotFoundException("Restaurante no encontrado"))
                .given(favoritoService).agregarFavorito(anyString(), any(Long.class));

        // When & Then
        mockMvc.perform(post("/api/favoritos/{restauranteId}", restauranteId)
                .principal(mockPrincipal))
                .andExpect(status().isNotFound());
    }

    // --- Tests para eliminarFavorito (DELETE /api/favoritos/{restauranteId}) ---

    @Test
    public void eliminarFavorito_cuandoDatosSonValidos_retorna204NoContent() throws Exception {
        // Given
        Long restauranteId = 1L;
        willDoNothing().given(favoritoService).eliminarFavorito(anyString(), any(Long.class));

        // When & Then
        mockMvc.perform(delete("/api/favoritos/{restauranteId}", restauranteId)
                .principal(mockPrincipal))
                .andExpect(status().isNoContent());
    }

    @Test
    public void eliminarFavorito_cuandoFavoritoNoExiste_retorna404NotFound() throws Exception {
        // Given
        Long restauranteId = 99L;
        willThrow(new ResourceNotFoundException("Favorito no encontrado"))
                .given(favoritoService).eliminarFavorito(anyString(), any(Long.class));

        // When & Then
        mockMvc.perform(delete("/api/favoritos/{restauranteId}", restauranteId)
                .principal(mockPrincipal))
                .andExpect(status().isNotFound());
    }
}