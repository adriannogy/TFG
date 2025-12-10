package com.restaurantes.restaurantesaplicacion.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.security.Principal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurantes.restaurantesaplicacion.dto.PasswordChangeDTO;
import com.restaurantes.restaurantesaplicacion.dto.UserProfileDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioEmailUpdateDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioNombreUpdateDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioSimpleDTO;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.security.JwtUtil;
import com.restaurantes.restaurantesaplicacion.service.UserService;

@WebMvcTest(UsuarioController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private JwtUtil jwtUtil;

    private Principal mockPrincipal;
    private UsuarioResponseDTO usuarioResponseDTO;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "usuario@test.com";

        usuarioResponseDTO = new UsuarioResponseDTO();
        usuarioResponseDTO.setId(1L);
        usuarioResponseDTO.setNombreUsuario("usuarioTest");
        usuarioResponseDTO.setEmail("usuario@test.com");
    }

    // --- 1. Test para obtenerTodos (GET /) ---
    @Test
    public void obtenerTodos_cuandoExistenUsuarios_retornaListaY200OK() throws Exception {
        given(userService.obtenerTodosLosUsuarios()).willReturn(List.of(usuarioResponseDTO));

        mockMvc.perform(get("/api/usuarios")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreUsuario").value("usuarioTest"));
    }

    // --- 2. Test para obtenerPorId (GET /{id}) ---
    @Test
    public void obtenerPorId_cuandoIdExiste_retornaUsuarioY200OK() throws Exception {
        given(userService.obtenerUsuarioPorId(1L)).willReturn(usuarioResponseDTO);

        mockMvc.perform(get("/api/usuarios/{id}", 1L)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    public void obtenerPorId_cuandoIdNoExiste_retorna404NotFound() throws Exception {
        given(userService.obtenerUsuarioPorId(99L)).willThrow(new ResourceNotFoundException("No existe"));

        mockMvc.perform(get("/api/usuarios/{id}", 99L)
                .principal(mockPrincipal))
                .andExpect(status().isNotFound());
    }

    // --- 3. Test para darDeBajaMiCuenta (DELETE /me) ---
    @Test
    public void darDeBajaMiCuenta_cuandoEsExitoso_retornaMensajeY200OK() throws Exception {
        willDoNothing().given(userService).darDeBajaMiCuenta();

        mockMvc.perform(delete("/api/usuarios/me")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().string("La cuenta ha sido dada de baja con éxito."));
    }

    // --- 4. Test para actualizarNombreUsuario (PUT /nombre-usuario) ---
    @Test
    public void actualizarNombreUsuario_cuandoEsExitoso_retornaUsuarioActualizadoY200OK() throws Exception {
        UsuarioNombreUpdateDTO nombreDTO = new UsuarioNombreUpdateDTO();
        nombreDTO.setNombreUsuario("nuevoNombre");
        
        usuarioResponseDTO.setNombreUsuario("nuevoNombre");

        given(userService.actualizarNombreUsuario(anyString(), anyString())).willReturn(usuarioResponseDTO);

        mockMvc.perform(put("/api/usuarios/nombre-usuario")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nombreDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("nuevoNombre"));
    }

    @Test
    public void actualizarNombreUsuario_cuandoNombreEsInvalido_retorna400BadRequest() throws Exception {

        UsuarioNombreUpdateDTO nombreDTO = new UsuarioNombreUpdateDTO();
        nombreDTO.setNombreUsuario(""); 

        mockMvc.perform(put("/api/usuarios/nombre-usuario")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nombreDTO)))
                .andExpect(status().isBadRequest());
    }

    // --- 5. Test para actualizarEmail (PUT /email) ---
    @Test
    public void actualizarEmail_cuandoEmailEsValido_retornaUsuarioActualizadoY200OK() throws Exception {
        UsuarioEmailUpdateDTO emailDTO = new UsuarioEmailUpdateDTO();
        emailDTO.setEmail("nuevo@email.com");
        
        usuarioResponseDTO.setEmail("nuevo@email.com");

        given(userService.actualizarEmail(anyString(), anyString())).willReturn(usuarioResponseDTO);

        mockMvc.perform(put("/api/usuarios/email")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emailDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("nuevo@email.com"));
    }

    // --- 6. Test para actualizarFotoPerfil (PUT /perfil/foto) ---
    @Test
public void actualizarFotoPerfil_cuandoHayArchivo_retornaUsuarioActualizadoY200OK() throws Exception {
    MockMultipartFile file = new MockMultipartFile(
            "file",
            "foto.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "imagedata".getBytes()
    );

    given(userService.actualizarFotoPerfil(anyString(), any(MultipartFile.class)))
            .willReturn(usuarioResponseDTO);

    mockMvc.perform(multipart(HttpMethod.PUT, "/api/usuarios/perfil/foto")
            .file(file)
            .principal(mockPrincipal))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombreUsuario").value("usuarioTest"));
}

    // --- 7. Test para changePassword (PUT /password) ---
    @Test
    public void changePassword_cuandoContraseñaEsCorrecta_retornaMensajeY200OK() throws Exception {
        PasswordChangeDTO passDTO = new PasswordChangeDTO();
        passDTO.setOldPassword("antigua");
        passDTO.setNewPassword("nueva");

        willDoNothing().given(userService).changePassword(anyString(), any(PasswordChangeDTO.class));

        mockMvc.perform(put("/api/usuarios/password")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passDTO)))
                .andExpect(status().isOk())
                .andExpect(content().string("Contraseña actualizada con éxito."));
    }

    @Test
    public void changePassword_cuandoContraseñaAntiguaEsIncorrecta_retorna401Unauthorized() throws Exception {
        PasswordChangeDTO passDTO = new PasswordChangeDTO();
        passDTO.setOldPassword("incorrecta");
        passDTO.setNewPassword("nueva");

        willThrow(new BadCredentialsException("Contraseña antigua incorrecta"))
                .given(userService).changePassword(anyString(), any(PasswordChangeDTO.class));

        mockMvc.perform(put("/api/usuarios/password")
                .principal(mockPrincipal)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passDTO)))
                .andExpect(status().isUnauthorized());
    }

    // --- 8. Test para buscarUsuarios (GET /buscar) ---
    @Test
    public void buscarUsuarios_cuandoEncuentra_retornaListaSimpleY200OK() throws Exception {
        UsuarioSimpleDTO simpleDTO = new UsuarioSimpleDTO();
        simpleDTO.setNombreUsuario("usuarioBuscado");
        
        given(userService.buscarUsuarios("test")).willReturn(List.of(simpleDTO));

        mockMvc.perform(get("/api/usuarios/buscar")
                .param("q", "test")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreUsuario").value("usuarioBuscado"));
    }

    // --- 9. Test para verPerfilDeUsuario (GET /{nombreUsuario}/perfil) ---
    @Test
    public void verPerfilDeUsuario_cuandoUsuarioExiste_retornaProfileDTOY200OK() throws Exception {
        UserProfileDTO userProfile = new UserProfileDTO();
        userProfile.setNombreUsuario("perfilVisto");
        userProfile.setSeguidoresCount(5);

        given(userService.verPerfilUsuario(anyString(), anyString())).willReturn(userProfile);

        mockMvc.perform(get("/api/usuarios/{nombreUsuario}/perfil", "perfilVisto")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreUsuario").value("perfilVisto"));
    }

    @Test
    public void verPerfilDeUsuario_cuandoUsuarioNoExiste_retorna404NotFound() throws Exception {
        given(userService.verPerfilUsuario(anyString(), anyString()))
                .willThrow(new ResourceNotFoundException("No encontrado"));

        mockMvc.perform(get("/api/usuarios/{nombreUsuario}/perfil", "noexiste")
                .principal(mockPrincipal))
                .andExpect(status().isNotFound());
    }
}