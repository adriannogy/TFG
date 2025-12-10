package com.restaurantes.restaurantesaplicacion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import com.restaurantes.restaurantesaplicacion.dto.PasswordChangeDTO;
import com.restaurantes.restaurantesaplicacion.dto.ProfileDTO;
import com.restaurantes.restaurantesaplicacion.dto.UserProfileDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioRegistroDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioSimpleDTO;
import com.restaurantes.restaurantesaplicacion.exception.BadRequestException;
import com.restaurantes.restaurantesaplicacion.exception.ConflictException;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.mapper.UsuarioMapper;
import com.restaurantes.restaurantesaplicacion.mapper.ValoracionMapper;
import com.restaurantes.restaurantesaplicacion.model.EstadoSolicitud;
import com.restaurantes.restaurantesaplicacion.model.RelacionUsuario;
import com.restaurantes.restaurantesaplicacion.model.RelacionUsuarioId;
import com.restaurantes.restaurantesaplicacion.model.Usuario;
import com.restaurantes.restaurantesaplicacion.repository.RelacionUsuarioRepository;
import com.restaurantes.restaurantesaplicacion.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    
    @Mock private UsuarioRepository usuarioRepository;
    @Mock private UsuarioMapper usuarioMapper;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private EmailService emailService;
    @Mock private ValoracionMapper valoracionMapper;
    @Mock private FileStorageService fileStorageService;
    @Mock private RelacionUsuarioRepository relacionRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private Usuario usuario;
    private UsuarioRegistroDTO registroDTO;
    private UsuarioResponseDTO usuarioResponseDTO;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@test.com");
        usuario.setNombreUsuario("testUser");
        usuario.setPwd("encodedPassword");
        usuario.setVerificado(true);
        usuario.setSiguiendo(new HashSet<>());
        usuario.setSeguidores(new HashSet<>());
        usuario.setValoraciones(new HashSet<>());

        registroDTO = new UsuarioRegistroDTO();
        registroDTO.setEmail("nuevo@test.com");
        registroDTO.setNombreUsuario("nuevoUser");
        registroDTO.setPwd("password123");

        usuarioResponseDTO = new UsuarioResponseDTO();
        usuarioResponseDTO.setId(1L);
        usuarioResponseDTO.setEmail("test@test.com");
        usuarioResponseDTO.setNombreUsuario("testUser");
    }

    private void mockSecurityContext(String email) {
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.getName()).thenReturn(email);
    }

    // --- Tests para obtenerTodosLosUsuarios ---
    @Test
    public void obtenerTodosLosUsuarios_retornaListaDTO() {
        given(usuarioRepository.findAll()).willReturn(List.of(usuario));
        given(usuarioMapper.toUsuarioResponseDto(usuario)).willReturn(usuarioResponseDTO);
        List<UsuarioResponseDTO> resultado = userService.obtenerTodosLosUsuarios();
        assertFalse(resultado.isEmpty());
        assertEquals("testUser", resultado.get(0).getNombreUsuario());
    }

    // --- Tests para obtenerUsuarioPorId ---
    @Test
    public void obtenerUsuarioPorId_cuandoExiste_retornaDTO() {
        given(usuarioRepository.findById(1L)).willReturn(Optional.of(usuario));
        given(usuarioMapper.toUsuarioResponseDto(usuario)).willReturn(usuarioResponseDTO);
        UsuarioResponseDTO resultado = userService.obtenerUsuarioPorId(1L);
        assertNotNull(resultado);
        assertEquals(1L, resultado.getId());
    }
    
    @Test
    public void obtenerUsuarioPorId_cuandoNoExiste_lanzaResourceNotFound() {
        given(usuarioRepository.findById(99L)).willReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.obtenerUsuarioPorId(99L);
        });
    }

    // --- Tests para crearUsuario ---
    @Test
    public void crearUsuario_cuandoEsNuevo_loGuardaYEnviaEmail() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(usuarioRepository.save(any(Usuario.class))).willReturn(usuario);
        given(usuarioMapper.toUsuarioResponseDto(any(Usuario.class))).willReturn(usuarioResponseDTO);
        doNothing().when(emailService).sendVerificationEmail(anyString(), anyString());

        userService.crearUsuario(registroDTO);

        verify(usuarioRepository).save(any(Usuario.class));
        verify(emailService).sendVerificationEmail(eq("test@test.com"), anyString());
    }

    @Test
    public void crearUsuario_cuandoYaExisteYEstaActivo_lanzaConflictException() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuario));
        assertThrows(ConflictException.class, () -> userService.crearUsuario(registroDTO));
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    public void crearUsuario_cuandoExistePeroEstaDeBaja_loReactiva() {
        usuario.setFechaBaja(LocalDateTime.now().minusDays(1));
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuario));
        given(passwordEncoder.encode(anyString())).willReturn("newEncodedPassword");
        given(usuarioRepository.save(any(Usuario.class))).willReturn(usuario);
        given(usuarioMapper.toUsuarioResponseDto(any(Usuario.class))).willReturn(usuarioResponseDTO);

        userService.crearUsuario(registroDTO);

        assertNull(usuario.getFechaBaja());
        verify(usuarioRepository).save(usuario);
    }
    
    @Test
    public void crearUsuario_fallaAlEnviarEmail_igualmenteCreaUsuario() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());
        given(passwordEncoder.encode(anyString())).willReturn("encodedPassword");
        given(usuarioRepository.save(any(Usuario.class))).willReturn(usuario);
        given(usuarioMapper.toUsuarioResponseDto(any(Usuario.class))).willReturn(usuarioResponseDTO);
        doThrow(new RuntimeException("Error de email")).when(emailService).sendVerificationEmail(anyString(), anyString());

        UsuarioResponseDTO resultado = userService.crearUsuario(registroDTO);

        assertNotNull(resultado);
        verify(usuarioRepository).save(any(Usuario.class));
    }


    // --- Tests para loadUserByUsername ---
    @Test
    public void loadUserByUsername_cuandoUsuarioEsValido_retornaUserDetails() {
        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        UserDetails userDetails = userService.loadUserByUsername("test@test.com");
        assertNotNull(userDetails);
        assertEquals("test@test.com", userDetails.getUsername());
    }

    @Test
    public void loadUserByUsername_cuandoNoEstaVerificado_lanzaLockedException() {
        usuario.setVerificado(false);
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuario));
        assertThrows(LockedException.class, () -> userService.loadUserByUsername("test@test.com"));
    }

    @Test
    public void loadUserByUsername_cuandoEstaDeBaja_lanzaDisabledException() {
        usuario.setFechaBaja(LocalDateTime.now());
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuario));
        assertThrows(DisabledException.class, () -> userService.loadUserByUsername("test@test.com"));
    }

    @Test
    public void loadUserByUsername_cuandoNoExiste_lanzaUsernameNotFoundException() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> userService.loadUserByUsername("fantasma@test.com"));
    }

    // --- Tests para generatePasswordResetToken ---
    @Test
    public void generatePasswordResetToken_exito_guardaTokenYEnviaEmail() {
        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        doNothing().when(emailService).sendPasswordResetEmail(anyString(), anyString());
        
        userService.generatePasswordResetToken("test@test.com");
        
        assertNotNull(usuario.getResetToken());
        assertNotNull(usuario.getResetTokenExpiry());
        verify(usuarioRepository).save(usuario);
        verify(emailService).sendPasswordResetEmail(eq("test@test.com"), anyString());
    }

    @Test
    public void generatePasswordResetToken_usuarioNoExiste_lanzaException() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> {
            userService.generatePasswordResetToken("fantasma@test.com");
        });
        verify(usuarioRepository, never()).save(any());
    }

    // --- Tests para resetPassword ---
    @Test
    public void resetPassword_conTokenValido_cambiaContraseña() {
        usuario.setResetToken("valid-token");
        usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(1));
        given(usuarioRepository.findByResetToken("valid-token")).willReturn(Optional.of(usuario));
        given(passwordEncoder.encode("newPass")).willReturn("newEncodedPassword");

        userService.resetPassword("valid-token", "newPass");

        assertEquals("newEncodedPassword", usuario.getPwd());
        assertNull(usuario.getResetToken());
        verify(usuarioRepository).save(usuario);
    }

    @Test
    public void resetPassword_conTokenInvalido_lanzaConflictException() {
        given(usuarioRepository.findByResetToken("invalid-token")).willReturn(Optional.empty());
        assertThrows(ConflictException.class, () -> {
            userService.resetPassword("invalid-token", "newPass");
        });
    }

    @Test
    public void resetPassword_conTokenExpirado_lanzaConflictException() {
        usuario.setResetToken("expired-token");
        usuario.setResetTokenExpiry(LocalDateTime.now().minusHours(1));
        given(usuarioRepository.findByResetToken("expired-token")).willReturn(Optional.of(usuario));

        assertThrows(ConflictException.class, () -> {
            userService.resetPassword("expired-token", "newPass");
        });
    }

    // --- Tests para darDeBajaMiCuenta ---
    @Test
    public void darDeBajaMiCuenta_exito_seteaFechaBaja() {
        mockSecurityContext("test@test.com");
        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        
        userService.darDeBajaMiCuenta();
        
        assertNotNull(usuario.getFechaBaja());
        verify(usuarioRepository).save(usuario);
    }
    
    @Test
    public void darDeBajaMiCuenta_usuarioNoEncontrado_lanzaUsernameNotFound() {
        mockSecurityContext("fantasma@test.com");
        given(usuarioRepository.findByEmail("fantasma@test.com")).willReturn(Optional.empty());
        
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.darDeBajaMiCuenta();
        });
    }

    // --- Tests para actualizarNombreUsuario ---
    @Test
    public void actualizarNombreUsuario_exito_actualizaNombre() {
        given(usuarioRepository.findByNombreUsuario("nuevoNombre")).willReturn(Optional.empty());
        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        given(usuarioRepository.save(any(Usuario.class))).willReturn(usuario);
        given(usuarioMapper.toUsuarioResponseDto(any(Usuario.class))).willReturn(usuarioResponseDTO);
        
        userService.actualizarNombreUsuario("test@test.com", "nuevoNombre");
        
        assertEquals("nuevoNombre", usuario.getNombreUsuario());
        verify(usuarioRepository).save(usuario);
    }
    
    @Test
    public void actualizarNombreUsuario_nombreYaExiste_lanzaConflictException() {
        given(usuarioRepository.findByNombreUsuario("nombreExistente")).willReturn(Optional.of(new Usuario()));
        
        assertThrows(ConflictException.class, () -> {
            userService.actualizarNombreUsuario("test@test.com", "nombreExistente");
        });
    }

    @Test
    public void actualizarNombreUsuario_usuarioNoEncontrado_lanzaUsernameNotFound() {
        given(usuarioRepository.findByNombreUsuario(anyString())).willReturn(Optional.empty());
        given(usuarioRepository.findByEmail("fantasma@test.com")).willReturn(Optional.empty());
        
        assertThrows(UsernameNotFoundException.class, () -> {
            userService.actualizarNombreUsuario("fantasma@test.com", "nuevoNombre");
        });
    }


    // --- Tests para actualizarEmail ---
    @Test
    public void actualizarEmail_exito_actualizaEmail() {
        given(usuarioRepository.findByEmail("nuevo@test.com")).willReturn(Optional.empty());
        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        given(usuarioRepository.save(any(Usuario.class))).willReturn(usuario);
        given(usuarioMapper.toUsuarioResponseDto(any(Usuario.class))).willReturn(usuarioResponseDTO);
        
        userService.actualizarEmail("test@test.com", "nuevo@test.com");
        
        assertEquals("nuevo@test.com", usuario.getEmail());
        verify(usuarioRepository).save(usuario);
    }
    
    @Test
    public void actualizarEmail_emailYaExiste_lanzaRuntimeException() {
        given(usuarioRepository.findByEmail("existente@test.com")).willReturn(Optional.of(new Usuario()));
        
        assertThrows(RuntimeException.class, () -> {
            userService.actualizarEmail("test@test.com", "existente@test.com");
        });
    }
    
    @Test
    public void actualizarEmail_emailEsElMismo_actualizaIgualmente() {
        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        given(usuarioRepository.save(any(Usuario.class))).willReturn(usuario);
        given(usuarioMapper.toUsuarioResponseDto(any(Usuario.class))).willReturn(usuarioResponseDTO);

        userService.actualizarEmail("test@test.com", "test@test.com");
        
        verify(usuarioRepository).save(usuario);
    }

    // --- Tests para actualizarFotoPerfil ---
    @Test
    public void actualizarFotoPerfil_exito_guardaUrl() {
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);
        given(fileStorageService.store(file)).willReturn("http://fake-url.com/foto.jpg");
        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        given(usuarioRepository.save(any(Usuario.class))).willReturn(usuario);
        given(usuarioMapper.toUsuarioResponseDto(any(Usuario.class))).willReturn(usuarioResponseDTO);
        
        userService.actualizarFotoPerfil("test@test.com", file);
        
        assertEquals("http://fake-url.com/foto.jpg", usuario.getFotoPerfilUrl());
        verify(usuarioRepository).save(usuario);
    }
    
    @Test
    public void actualizarFotoPerfil_archivoVacio_lanzaBadRequestException() {
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(true);
        
        assertThrows(BadRequestException.class, () -> {
            userService.actualizarFotoPerfil("test@test.com", file);
        });
    }
    
    @Test
    public void actualizarFotoPerfil_archivoNulo_lanzaBadRequestException() {
        assertThrows(BadRequestException.class, () -> {
            userService.actualizarFotoPerfil("test@test.com", null);
        });
    }

    @Test
    public void actualizarFotoPerfil_usuarioNoEncontrado_lanzaResourceNotFound() {
        MultipartFile file = mock(MultipartFile.class);
        given(file.isEmpty()).willReturn(false);
        given(fileStorageService.store(file)).willReturn("http://fake-url.com/foto.jpg");
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.actualizarFotoPerfil("fantasma@test.com", file);
        });
    }

    // --- Tests para getFullProfile ---
    @Test
    public void getFullProfile_retornaDTOConDatos() {
        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        
        ProfileDTO profile = userService.getFullProfile("test@test.com");
        
        assertNotNull(profile);
        assertEquals("testUser", profile.getNombreUsuario());
        verify(valoracionMapper, times(0)).toDto(any());
    }
    

    // --- Tests para verificarUsuario ---
    @Test
    public void verificarUsuario_conTokenValido_verificaYEnviaBienvenida() {
        usuario.setVerificado(false);
        given(usuarioRepository.findByTokenVerificacion("valid-token")).willReturn(Optional.of(usuario));
        doNothing().when(emailService).sendWelcomeEmail(anyString(), anyString());

        userService.verificarUsuario("valid-token");

        assertTrue(usuario.isVerificado());
        assertNull(usuario.getTokenVerificacion());
        verify(usuarioRepository).save(usuario);
        verify(emailService).sendWelcomeEmail(usuario.getEmail(), usuario.getNombreUsuario());
    }

    @Test
    public void verificarUsuario_conTokenInvalido_lanzaBadRequestException() {
        given(usuarioRepository.findByTokenVerificacion("invalid-token")).willReturn(Optional.empty());
        assertThrows(BadRequestException.class, () -> {
            userService.verificarUsuario("invalid-token");
        });
    }

    @Test
    public void verificarUsuario_fallaAlEnviarEmail_igualmenteVerifica() {
        usuario.setVerificado(false);
        given(usuarioRepository.findByTokenVerificacion("valid-token")).willReturn(Optional.of(usuario));
        doThrow(new RuntimeException("Error de email")).when(emailService).sendWelcomeEmail(anyString(), anyString());
        
        userService.verificarUsuario("valid-token");
        
        assertTrue(usuario.isVerificado());
        verify(usuarioRepository).save(usuario);
    }

    // --- Tests para changePassword ---
    @Test
    public void changePassword_conContraseñaCorrecta_laCambia() {
        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("oldPass");
        dto.setNewPassword("newPass");

        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        given(passwordEncoder.matches("oldPass", "encodedPassword")).willReturn(true);
        given(passwordEncoder.encode("newPass")).willReturn("newEncodedPassword");

        userService.changePassword("test@test.com", dto);

        assertEquals("newEncodedPassword", usuario.getPwd());
        verify(usuarioRepository).save(usuario);
    }
    
    @Test
    public void changePassword_conContraseñaIncorrecta_lanzaBadCredentialsException() {
        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("wrongPass");
        dto.setNewPassword("newPass");

        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));
        given(passwordEncoder.matches("wrongPass", "encodedPassword")).willReturn(false);

        assertThrows(BadCredentialsException.class, () -> {
            userService.changePassword("test@test.com", dto);
        });
        verify(usuarioRepository, never()).save(any());
    }

    @Test
    public void changePassword_cuandoUsuarioNoExiste_lanzaResourceNotFoundException() {
        // Given
        PasswordChangeDTO dto = new PasswordChangeDTO();
        dto.setOldPassword("old");
        dto.setNewPassword("new");
        
        given(usuarioRepository.findByEmail("fantasma@test.com")).willReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.changePassword("fantasma@test.com", dto);
        });

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(usuarioRepository, never()).save(any(Usuario.class));
    }


    // --- Tests para buscarUsuarios ---
    @Test
    public void buscarUsuarios_filtraUsuarioActual() {
        mockSecurityContext("test@test.com");
        
        Usuario otroUsuario = new Usuario();
        otroUsuario.setEmail("otro@test.com");
        
        given(usuarioRepository.findByNombreUsuarioStartingWithIgnoreCase("test"))
            .willReturn(List.of(usuario, otroUsuario));
            
        given(usuarioMapper.toSimpleDto(otroUsuario)).willReturn(new UsuarioSimpleDTO());

        List<UsuarioSimpleDTO> resultado = userService.buscarUsuarios("test");
        
        assertEquals(1, resultado.size());
        verify(usuarioMapper, times(1)).toSimpleDto(otroUsuario);
        verify(usuarioMapper, never()).toSimpleDto(usuario);
    }

    // --- Tests para verPerfilUsuario ---
    @Test
    public void verPerfilUsuario_perfilPublico_retornaDTOCompleto() {
        Usuario profileUser = new Usuario();
        profileUser.setNombreUsuario("perfilVisto");
        profileUser.setPrivate(false);
        profileUser.setSiguiendo(new HashSet<>());
        profileUser.setSeguidores(new HashSet<>());

        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuario));
        given(usuarioRepository.findByNombreUsuario("perfilVisto")).willReturn(Optional.of(profileUser));
        
        UserProfileDTO dto = userService.verPerfilUsuario("test@test.com", "perfilVisto");
        
        assertNotNull(dto.getSeguidoresCount());
    }
    
    @Test
    public void verPerfilUsuario_perfilPrivadoPeroSigue_retornaDTOCompleto() {
        Usuario profileUser = new Usuario();
        profileUser.setNombreUsuario("perfilVisto");
        profileUser.setPrivate(true); 
        profileUser.setSiguiendo(new HashSet<>());
        profileUser.setSeguidores(new HashSet<>());
        
        RelacionUsuarioId relacionId = new RelacionUsuarioId();
        relacionId.setSeguidorId(usuario.getId());
        relacionId.setSeguidoId(profileUser.getId());
        // -------------------------
        
        RelacionUsuario relacion = new RelacionUsuario();
        relacion.setEstado(EstadoSolicitud.ACEPTADO);

        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuario));
        given(usuarioRepository.findByNombreUsuario("perfilVisto")).willReturn(Optional.of(profileUser));
        given(relacionRepository.findById(relacionId)).willReturn(Optional.of(relacion)); 
        
        UserProfileDTO dto = userService.verPerfilUsuario("test@test.com", "perfilVisto");
        
        assertNotNull(dto.getSeguidoresCount());
    }
    
    @Test
    public void verPerfilUsuario_perfilPrivadoNoSigue_retornaDTOParcial() {
        Usuario profileUser = new Usuario();
        profileUser.setNombreUsuario("perfilVisto");
        profileUser.setPrivate(true); 
        
        RelacionUsuarioId relacionId = new RelacionUsuarioId();
        relacionId.setSeguidorId(usuario.getId());
        relacionId.setSeguidoId(profileUser.getId());
        // -------------------------

        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuario));
        given(usuarioRepository.findByNombreUsuario("perfilVisto")).willReturn(Optional.of(profileUser));
        given(relacionRepository.findById(relacionId)).willReturn(Optional.empty()); 
        
        UserProfileDTO dto = userService.verPerfilUsuario("test@test.com", "perfilVisto");
        
        assertNull(dto.getSeguidoresCount()); 
    }

    @Test
    public void verPerfilUsuario_viewerNoEncontrado_lanzaResourceNotFound() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.verPerfilUsuario("fantasma@test.com", "perfilVisto");
        });
    }
    
    @Test
    public void verPerfilUsuario_perfilNoEncontrado_lanzaResourceNotFound() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuario));
        given(usuarioRepository.findByNombreUsuario(anyString())).willReturn(Optional.empty());
        
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.verPerfilUsuario("test@test.com", "fantasma");
        });
    }

    @Test
    public void getFullProfile_conRelacionesYValoraciones_retornaDTOCompleto() {
      
        RelacionUsuario seguidorAceptado = new RelacionUsuario();
        seguidorAceptado.setEstado(EstadoSolicitud.ACEPTADO);
        
        RelacionUsuario seguidorPendiente = new RelacionUsuario();
        seguidorPendiente.setEstado(EstadoSolicitud.PENDIENTE);

        usuario.setSeguidores(new HashSet<>(List.of(seguidorAceptado, seguidorPendiente)));

        RelacionUsuario seguidoAceptado = new RelacionUsuario();
        seguidoAceptado.setEstado(EstadoSolicitud.ACEPTADO);
        usuario.setSiguiendo(new HashSet<>(List.of(seguidoAceptado)));

        com.restaurantes.restaurantesaplicacion.model.Foto fotoMock = 
            new com.restaurantes.restaurantesaplicacion.model.Foto();
        fotoMock.setUrl("http://foto.com/1.jpg");
        
        com.restaurantes.restaurantesaplicacion.model.Valoracion valoracionConFoto = 
            new com.restaurantes.restaurantesaplicacion.model.Valoracion();
        valoracionConFoto.setFotos(new HashSet<>(List.of(fotoMock)));
        com.restaurantes.restaurantesaplicacion.dto.ValoracionDTO dtoConFoto = 
            new com.restaurantes.restaurantesaplicacion.dto.ValoracionDTO();
        dtoConFoto.setComentario("Con foto");

        given(valoracionMapper.toDto(valoracionConFoto)).willReturn(dtoConFoto);
        usuario.getValoraciones().add(valoracionConFoto);

        given(usuarioRepository.findByEmail("test@test.com")).willReturn(Optional.of(usuario));

        ProfileDTO profile = userService.getFullProfile("test@test.com");

        assertNotNull(profile);
        
        assertEquals(1, profile.getSeguidosCount());
        assertEquals(1, profile.getSeguidoresCount());
        assertEquals(1, profile.getSolicitudesPendientesCount());

        assertEquals(1, profile.getValoraciones().size());
        assertEquals(1, profile.getValoraciones().get(0).getFotos().size());
        assertEquals("http://foto.com/1.jpg", profile.getValoraciones().get(0).getFotos().get(0));
    }

    @Test
    public void getFullProfile_usuarioNoEncontrado_lanzaResourceNotFound() {
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> {
            userService.getFullProfile("fantasma@test.com");
        });
    }


    @Test
    public void verPerfilUsuario_perfilPrivadoSinRelacion_retornaDTOParcial() {
        // Given
        Usuario profileUser = new Usuario();
        profileUser.setId(2L);
        profileUser.setNombreUsuario("perfilVisto");
        profileUser.setPrivate(true);

        RelacionUsuarioId relacionId = new RelacionUsuarioId();
        relacionId.setSeguidorId(usuario.getId());
        relacionId.setSeguidoId(profileUser.getId());

        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuario));
        given(usuarioRepository.findByNombreUsuario("perfilVisto")).willReturn(Optional.of(profileUser));
        
        given(relacionRepository.findById(relacionId)).willReturn(Optional.empty()); 
        
        // When
        UserProfileDTO dto = userService.verPerfilUsuario("test@test.com", "perfilVisto");
        
        // Then
        
        assertNull(dto.getRelationshipStatus());
        assertNull(dto.getSeguidoresCount());
        assertNull(dto.getSeguidosCount());
    }
}