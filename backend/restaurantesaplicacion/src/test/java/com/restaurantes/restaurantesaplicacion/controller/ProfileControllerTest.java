package com.restaurantes.restaurantesaplicacion.controller;

import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import com.restaurantes.restaurantesaplicacion.dto.ProfileDTO;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.security.JwtUtil;
import com.restaurantes.restaurantesaplicacion.service.UserService;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ProfileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;
  


    @Test
    public void getMyProfile_cuandoUsuarioEstaAutenticado_retornaProfileDTOY200OK() throws Exception {
        // Given
        String emailUsuario = "usuario@logueado.com";
        Principal mockPrincipal = () -> emailUsuario;

        ProfileDTO profileMock = new ProfileDTO();
        profileMock.setNombreUsuario("testUser");
        profileMock.setSeguidoresCount(10);
        profileMock.setSeguidosCount(5);
        profileMock.setValoraciones(new ArrayList<>()); 

        given(userService.getFullProfile(emailUsuario)).willReturn(profileMock);

        // When
        mockMvc.perform(get("/api/perfil/me")
                .principal(mockPrincipal) 
                .contentType(MediaType.APPLICATION_JSON))

        // Then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("testUser"))
                .andExpect(jsonPath("$.seguidoresCount").value(10))
                .andExpect(jsonPath("$.seguidosCount").value(5));
    }

    @Test
    public void getMyProfile_cuandoUsuarioNoExiste_retorna404NotFound() throws Exception {
        // Given
        String emailUsuario = "usuario.fantasma@test.com";
        Principal mockPrincipal = () -> emailUsuario;

        given(userService.getFullProfile(emailUsuario))
                .willThrow(new ResourceNotFoundException("Usuario no encontrado"));

        // When & Then
        mockMvc.perform(get("/api/perfil/me")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }
}