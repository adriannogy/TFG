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
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import com.restaurantes.restaurantesaplicacion.dto.UsuarioSimpleDTO;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.security.JwtUtil;
import com.restaurantes.restaurantesaplicacion.service.RelacionService;

@WebMvcTest(RelacionController.class)
@AutoConfigureMockMvc(addFilters = false) 
public class RelacionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RelacionService relacionService; 

    // Mocks de seguridad
    @MockBean
    private AuthenticationManager authenticationManager;
    @MockBean
    private UserDetailsService userDetailsService;
    @MockBean
    private JwtUtil jwtUtil;

    private Principal mockPrincipal;
    private UsuarioSimpleDTO usuarioSimpleDTO;
    private List<UsuarioSimpleDTO> listaUsuariosDTO;

    @BeforeEach
    void setUp() {
        mockPrincipal = () -> "usuario.logueado@test.com";

        usuarioSimpleDTO = new UsuarioSimpleDTO();
        usuarioSimpleDTO.setNombreUsuario("testUser");
        
        listaUsuariosDTO = List.of(usuarioSimpleDTO);
    }

    // --- 1. Tests para solicitarSeguimiento (POST /seguir/{nombreUsuario}) ---
    @Test
    public void solicitarSeguimiento_cuandoUsuarioExiste_retorna200OK() throws Exception {
        willDoNothing().given(relacionService).solicitarSeguimiento(anyString(), anyString());
        String usuarioASeguir = "userAseguir";

        mockMvc.perform(post("/api/relaciones/seguir/{nombreUsuario}", usuarioASeguir)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().string("Solicitud de seguimiento enviada a " + usuarioASeguir));
    }

    @Test
    public void solicitarSeguimiento_cuandoUsuarioNoExiste_retorna404NotFound() throws Exception {
        willThrow(new ResourceNotFoundException("No existe")).given(relacionService).solicitarSeguimiento(anyString(), anyString());

        mockMvc.perform(post("/api/relaciones/seguir/{nombreUsuario}", "noexiste")
                .principal(mockPrincipal))
                .andExpect(status().isNotFound());
    }

    // --- 2. Tests para aceptarSolicitud (POST /solicitudes/aceptar/{nombreEmisor}) ---
    @Test
    public void aceptarSolicitud_cuandoSolicitudExiste_retorna200OK() throws Exception {
        willDoNothing().given(relacionService).aceptarSolicitud(anyString(), anyString());
        String nombreEmisor = "emisor";

        mockMvc.perform(post("/api/relaciones/solicitudes/aceptar/{nombreEmisor}", nombreEmisor)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().string("Has aceptado a " + nombreEmisor));
    }

    // --- 3. Tests para rechazarSolicitud (POST /solicitudes/rechazar/{nombreEmisor}) ---
    @Test
    public void rechazarSolicitud_cuandoSolicitudExiste_retorna200OK() throws Exception {
        willDoNothing().given(relacionService).rechazarSolicitud(anyString(), anyString());
        String nombreEmisor = "emisor";

        mockMvc.perform(post("/api/relaciones/solicitudes/rechazar/{nombreEmisor}", nombreEmisor)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().string("Has rechazado a " + nombreEmisor));
    }

    // --- 4. Tests para obtenerMisSeguidores (GET /seguidores) ---
    @Test
    public void obtenerMisSeguidores_cuandoExisten_retornaListaY200OK() throws Exception {
        given(relacionService.obtenerMisSeguidores(anyString())).willReturn(listaUsuariosDTO);

        mockMvc.perform(get("/api/relaciones/seguidores")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreUsuario").value("testUser"));
    }

    // --- 5. Tests para obtenerSeguidoresDeUsuario (GET /{nombreUsuario}/seguidores) ---
    @Test
    public void obtenerSeguidoresDeUsuario_cuandoUsuarioExiste_retornaListaY200OK() throws Exception {
        given(relacionService.obtenerSeguidoresDe(anyString(), anyString())).willReturn(listaUsuariosDTO);

        mockMvc.perform(get("/api/relaciones/{nombreUsuario}/seguidores", "otroUser")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreUsuario").value("testUser"));
    }

    // --- 6. Tests para obtenerMisSiguiendo (GET /siguiendo) ---
    @Test
    public void obtenerMisSiguiendo_cuandoExisten_retornaListaY200OK() throws Exception {
        given(relacionService.obtenerMisSiguiendo(anyString())).willReturn(listaUsuariosDTO);

        mockMvc.perform(get("/api/relaciones/siguiendo")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreUsuario").value("testUser"));
    }

    // --- 7. Tests para obtenerSiguiendoDeUsuario (GET /{nombreUsuario}/siguiendo) ---
    @Test
    public void obtenerSiguiendoDeUsuario_cuandoUsuarioExiste_retornaListaY200OK() throws Exception {
        given(relacionService.obtenerSiguiendoDe(anyString(), anyString())).willReturn(listaUsuariosDTO);

        mockMvc.perform(get("/api/relaciones/{nombreUsuario}/siguiendo", "otroUser")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreUsuario").value("testUser"));
    }

    // --- 8. Tests para obtenerSolicitudesPendientes (GET /solicitudes/pendientes) ---
    @Test
    public void obtenerSolicitudesPendientes_cuandoExisten_retornaListaY200OK() throws Exception {
        given(relacionService.obtenerSolicitudesPendientes(anyString())).willReturn(listaUsuariosDTO);

        mockMvc.perform(get("/api/relaciones/solicitudes/pendientes")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombreUsuario").value("testUser"));
    }

    // --- 9. Tests para eliminarSeguidor (DELETE /seguidores/{nombreSeguidor}) ---
    @Test
    public void eliminarSeguidor_cuandoEsExitoso_retornaMensajeY200OK() throws Exception {
        willDoNothing().given(relacionService).eliminarSeguidor(anyString(), anyString());

        mockMvc.perform(delete("/api/relaciones/seguidores/{nombreSeguidor}", "exSeguidor")
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().string("El seguidor ha sido eliminado con éxito."));
    }
    
    @Test
    public void eliminarSeguidor_cuandoSeguidorNoExiste_retorna404NotFound() throws Exception {
        willThrow(new ResourceNotFoundException("No encontrado")).given(relacionService).eliminarSeguidor(anyString(), anyString());

        mockMvc.perform(delete("/api/relaciones/seguidores/{nombreSeguidor}", "noexiste")
                .principal(mockPrincipal))
                .andExpect(status().isNotFound());
    }

    // --- 10. Tests para dejarDeSeguir (DELETE /dejar-de-seguir/{nombreUsuario}) ---
    @Test
    public void dejarDeSeguir_cuandoEsExitoso_retornaMensajeY200OK() throws Exception {
        willDoNothing().given(relacionService).dejarDeSeguir(anyString(), anyString());
        String usuarioADejar = "userAborrar";

        mockMvc.perform(delete("/api/relaciones/dejar-de-seguir/{nombreUsuario}", usuarioADejar)
                .principal(mockPrincipal))
                .andExpect(status().isOk())
                .andExpect(content().string("Has dejado de seguir a " + usuarioADejar));
    }
}