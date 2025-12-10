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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurantes.restaurantesaplicacion.dto.RestauranteDTO;
import com.restaurantes.restaurantesaplicacion.dto.ValoracionDTO;
import com.restaurantes.restaurantesaplicacion.dto.ValoracionInputDTO;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.exception.ValoracionDuplicadaException;
import com.restaurantes.restaurantesaplicacion.security.JwtUtil;
import com.restaurantes.restaurantesaplicacion.service.ValoracionService;

@WebMvcTest(ValoracionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ValoracionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper; 

    @MockBean
    private ValoracionService valoracionService; 

    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private JwtUtil jwtUtil;

    private Principal mockPrincipal;
    private ValoracionDTO valoracionDTO;
    private RestauranteDTO restauranteDTO;
    private Page<ValoracionDTO> paginaValoraciones;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "usuario@test.com";

        restauranteDTO = new RestauranteDTO();
        restauranteDTO.setId(1L);
        restauranteDTO.setNombre("Restaurante de Prueba");

        valoracionDTO = new ValoracionDTO();
        valoracionDTO.setRestaurante(restauranteDTO);
        valoracionDTO.setNombreUsuario("usuarioTest");
        valoracionDTO.setPuntuacion(5);
        valoracionDTO.setComentario("Genial!");

        paginaValoraciones = new PageImpl<>(List.of(valoracionDTO));
    }

    // --- Tests para crearValoracion (POST /usuario/{nombreUsuario}) ---

    @Test
    public void crearValoracion_conArchivos_retornaValoracionCreadaY201Created() throws Exception {
        // Given
        ValoracionInputDTO inputDTO = new ValoracionInputDTO();
        inputDTO.setComentario("Test con foto");
        inputDTO.setPuntuacion(5);
        inputDTO.setNombreRestaurante("Restaurante de Prueba");

        String valoracionJson = objectMapper.writeValueAsString(inputDTO);

        MockMultipartFile file = new MockMultipartFile(
                "files",
                "foto.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "imagedata".getBytes()
        );

        // 3. Configuramos el mock del servicio
        given(valoracionService.crearValoracion(anyString(), any(ValoracionInputDTO.class), any(MultipartFile[].class)))
                .willReturn(valoracionDTO);

        // When & Then
        mockMvc.perform(multipart("/api/valoraciones/usuario/{nombreUsuario}", "usuarioTest")
                .file(file) 
                .param("valoracion", valoracionJson)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comentario").value("Genial!"));
    }

    @Test
    public void crearValoracion_cuandoEsDuplicada_retorna409Conflict() throws Exception {
        // Given
        ValoracionInputDTO inputDTO = new ValoracionInputDTO();
        inputDTO.setComentario("Test duplicado");
        inputDTO.setPuntuacion(3);
        String valoracionJson = objectMapper.writeValueAsString(inputDTO);

       
        given(valoracionService.crearValoracion(
                anyString(), 
                any(ValoracionInputDTO.class), 
                isNull() 
        )).willThrow(new ValoracionDuplicadaException("Ya has valorado este restaurante"));

        // When & Then
        mockMvc.perform(multipart("/api/valoraciones/usuario/{nombreUsuario}", "usuarioTest")
                .param("valoracion", valoracionJson)
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isConflict()); 
    }
    
    // --- Tests para obtenerTodasLasValoraciones (GET /) ---
    
    @Test
    public void obtenerTodasLasValoraciones_cuandoExisten_retornaListaY200OK() throws Exception {
        // Given
        given(valoracionService.obtenerTodas()).willReturn(List.of(valoracionDTO));

        // When & Then
        mockMvc.perform(get("/api/valoraciones")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comentario").value("Genial!"));
    }

    @Test
    public void obtenerTodasLasValoraciones_cuandoNoExisten_retornaListaVaciaY200OK() throws Exception {
        // Given
        given(valoracionService.obtenerTodas()).willReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/api/valoraciones")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // --- Tests para obtenerFeed (GET /feed) ---

    @Test
    public void obtenerFeed_cuandoHayFeed_retornaPaginaY200OK() throws Exception {
        // Given
        given(valoracionService.obtenerFeedParaUsuario(anyString(), any(Pageable.class)))
                .willReturn(paginaValoraciones);

        // When & Then
        mockMvc.perform(get("/api/valoraciones/feed")
                .principal(mockPrincipal)
                .param("page", "0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombreUsuario").value("usuarioTest"));
    }

    @Test
    public void obtenerFeed_cuandoFeedEstaVacio_retornaPaginaVaciaY200OK() throws Exception {
        // Given
        given(valoracionService.obtenerFeedParaUsuario(anyString(), any(Pageable.class)))
                .willReturn(Page.empty());

        // When & Then
        mockMvc.perform(get("/api/valoraciones/feed")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    // --- Tests para eliminarValoracion (DELETE /{restauranteId}) ---

    @Test
    public void eliminarValoracion_cuandoDatosSonValidos_retorna204NoContent() throws Exception {
        // Given
        Long restauranteId = 1L;
        willDoNothing().given(valoracionService).eliminarValoracion(anyString(), any(Long.class));

        // When & Then
        mockMvc.perform(delete("/api/valoraciones/{restauranteId}", restauranteId)
                .principal(mockPrincipal))
                .andExpect(status().isNoContent()); 
    }

    @Test
    public void eliminarValoracion_cuandoValoracionNoExiste_retorna404NotFound() throws Exception {
        // Given
        Long restauranteId = 99L;
        willThrow(new ResourceNotFoundException("Valoración no encontrada"))
                .given(valoracionService).eliminarValoracion(anyString(), any(Long.class));

        // When & Then
        mockMvc.perform(delete("/api/valoraciones/{restauranteId}", restauranteId)
                .principal(mockPrincipal))
                .andExpect(status().isNotFound());
    }
}