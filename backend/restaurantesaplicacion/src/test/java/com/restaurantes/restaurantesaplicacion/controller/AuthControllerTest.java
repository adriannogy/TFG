package com.restaurantes.restaurantesaplicacion.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurantes.restaurantesaplicacion.dto.AuthRequestDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioRegistroDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioResponseDTO;
import com.restaurantes.restaurantesaplicacion.exception.ConflictException;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.security.JwtUtil;
import com.restaurantes.restaurantesaplicacion.service.UserService;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserService userService;

    // --- Tests para registerUser ---

    @Test
    public void registerUser_cuandoRegistroEsExitoso_retornaUsuarioCreadoY200OK() throws Exception {

        UsuarioRegistroDTO registroDTO = new UsuarioRegistroDTO();
        registroDTO.setEmail("test@test.com");
        registroDTO.setNombreUsuario("testuser");
        registroDTO.setPwd("password123");

        UsuarioResponseDTO usuarioCreado = new UsuarioResponseDTO();
        usuarioCreado.setId(1L);
        usuarioCreado.setNombreUsuario("testuser");
        usuarioCreado.setEmail("test@test.com");

        given(userService.crearUsuario(any(UsuarioRegistroDTO.class))).willReturn(usuarioCreado);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value(usuarioCreado.getNombreUsuario()))
                .andExpect(jsonPath("$.email").value(usuarioCreado.getEmail()));
    }

    @Test
    public void registerUser_cuandoUsuarioYaExiste_retorna409Conflict() throws Exception {
        // Given
        UsuarioRegistroDTO registroDTO = new UsuarioRegistroDTO();
        registroDTO.setEmail("existente@test.com");
        registroDTO.setNombreUsuario("existente");
        registroDTO.setPwd("password123");

        given(userService.crearUsuario(any(UsuarioRegistroDTO.class)))
                .willThrow(new ConflictException("Usuario ya existe")); 

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registroDTO)))
                .andExpect(status().isConflict());
    }

    // --- Tests para createAuthenticationToken ---

    @Test
    public void createAuthenticationToken_cuandoCredencialesSonValidas_retornaTokenY200OK() throws Exception {
      
        AuthRequestDTO authRequest = new AuthRequestDTO();
        authRequest.setEmail("user@test.com");
        authRequest.setPwd("password");

        UserDetails userDetails = new User("user@test.com", "password", new ArrayList<>());
        String fakeJwt = "fake.jwt.token";

        given(authenticationManager.authenticate(any())).willReturn(null); 
        given(userDetailsService.loadUserByUsername(authRequest.getEmail())).willReturn(userDetails);
        given(jwtUtil.generateToken(userDetails)).willReturn(fakeJwt);

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(content().string(fakeJwt));
    }

    @Test
    public void createAuthenticationToken_cuandoCredencialesSonInvalidas_retorna401Unauthorized() throws Exception {
        // Given
        AuthRequestDTO authRequest = new AuthRequestDTO();
        authRequest.setEmail("user@test.com");
        authRequest.setPwd("wrongpassword");

        willThrow(new BadCredentialsException("Credenciales incorrectas"))
                .given(authenticationManager).authenticate(any());

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
              
                .andExpect(status().isUnauthorized());
    }

    // --- Tests para forgotPassword ---

    @Test
    public void forgotPassword_cuandoEmailExiste_retornaMensajeExitoY200OK() throws Exception {
       
        Map<String, String> requestBody = Map.of("email", "existe@test.com");
        willDoNothing().given(userService).generatePasswordResetToken(anyString());

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().string("Se ha enviado un email de restablecimiento."));
    }

    @Test
    public void forgotPassword_cuandoEmailNoExiste_retorna404NotFound() throws Exception {
       
        Map<String, String> requestBody = Map.of("email", "noexiste@test.com");
        
        willThrow(new ResourceNotFoundException("Usuario no encontrado"))
                .given(userService).generatePasswordResetToken(anyString());

        mockMvc.perform(post("/api/auth/forgot-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isNotFound());
    }

    // --- Tests para resetPassword ---

    @Test
    public void resetPassword_cuandoTokenEsValido_retornaMensajeExitoY200OK() throws Exception {
   
        Map<String, String> requestBody = Map.of("pwd", "nuevaClave123");
        willDoNothing().given(userService).resetPassword(anyString(), anyString());

        mockMvc.perform(post("/api/auth/reset-password")
                .param("token", "token-valido")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(content().string("Contraseña restablecida con éxito."));
    }

    @Test
    public void resetPassword_cuandoTokenEsInvalido_retorna409Conflict() throws Exception { 
        // Given
        Map<String, String> requestBody = Map.of("pwd", "nuevaClave123");
        
        willThrow(new ConflictException("Token inválido"))
                .given(userService).resetPassword(anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/auth/reset-password")
                .param("token", "token-invalido")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isConflict());
    }

    // --- Tests para verifyUser ---

    @Test
    public void verifyUser_cuandoTokenEsValido_retornaMensajeExitoY200OK() throws Exception {
        willDoNothing().given(userService).verificarUsuario(anyString());

        mockMvc.perform(get("/api/auth/verify")
                .param("token", "token-valido"))
                .andExpect(status().isOk())
                .andExpect(content().string("¡Tu cuenta ha sido verificada con éxito! Ya puedes iniciar sesión."));
    }

    @Test
    public void verifyUser_cuandoTokenEsInvalido_retorna409Conflict() throws Exception {
        // Given
        willThrow(new ConflictException("Token inválido"))
                .given(userService).verificarUsuario(anyString());

        // When & Then
        mockMvc.perform(get("/api/auth/verify")
                .param("token", "token-invalido"))
                .andExpect(status().isConflict());
    }
}