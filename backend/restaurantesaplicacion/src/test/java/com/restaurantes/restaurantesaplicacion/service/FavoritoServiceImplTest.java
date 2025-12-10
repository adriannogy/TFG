package com.restaurantes.restaurantesaplicacion.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.restaurantes.restaurantesaplicacion.dto.RestauranteDTO;
import com.restaurantes.restaurantesaplicacion.exception.ConflictException;
import com.restaurantes.restaurantesaplicacion.mapper.RestauranteMapper;
import com.restaurantes.restaurantesaplicacion.model.Favorito;
import com.restaurantes.restaurantesaplicacion.model.FavoritoId;
import com.restaurantes.restaurantesaplicacion.model.Restaurante;
import com.restaurantes.restaurantesaplicacion.model.Usuario;
import com.restaurantes.restaurantesaplicacion.repository.FavoritoRepository;
import com.restaurantes.restaurantesaplicacion.repository.RestauranteRepository;
import com.restaurantes.restaurantesaplicacion.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
public class FavoritoServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private RestauranteRepository restauranteRepository;
    @Mock
    private FavoritoRepository favoritoRepository;
    @Mock
    private RestauranteMapper restauranteMapper;

    @InjectMocks
    private FavoritoServiceImpl favoritoService;

    private Usuario usuario;
    private Restaurante restaurante;
    private FavoritoId favoritoId;

    @BeforeEach
    void setUp() {
        usuario = new Usuario();
        usuario.setId(1L);
        usuario.setEmail("test@user.com");

        restaurante = new Restaurante();
        restaurante.setId(10L);
        restaurante.setNombre("Restaurante de Prueba");

        favoritoId = new FavoritoId();
        favoritoId.setUsuarioId(usuario.getId());
        favoritoId.setRestauranteId(restaurante.getId());
    }

    // --- Tests para agregarFavorito ---

    @Test
    public void agregarFavorito_cuandoEsExitoso_guardaElFavorito() {
        // Given
        given(usuarioRepository.findByEmail("test@user.com")).willReturn(Optional.of(usuario));
        given(restauranteRepository.findById(10L)).willReturn(Optional.of(restaurante));
        given(favoritoRepository.existsById(favoritoId)).willReturn(false); // No existe
        given(favoritoRepository.save(any(Favorito.class))).willReturn(new Favorito()); // Guardado exitoso

        // When
        favoritoService.agregarFavorito("test@user.com", 10L);

        // Then 
        verify(favoritoRepository, times(1)).save(any(Favorito.class));
    }

    @Test
    public void agregarFavorito_cuandoUsuarioNoExiste_lanzaConflictException() {
        // Given
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty());

        // When & Then
        assertThrows(ConflictException.class, () -> {
            favoritoService.agregarFavorito("test@user.com", 10L);
        });
        verify(favoritoRepository, never()).save(any());
    }

    @Test
    public void agregarFavorito_cuandoRestauranteNoExiste_lanzaConflictException() {
        // Given
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuario));
        given(restauranteRepository.findById(anyLong())).willReturn(Optional.empty());

        // When & Then
        assertThrows(ConflictException.class, () -> {
            favoritoService.agregarFavorito("test@user.com", 10L);
        });
        verify(favoritoRepository, never()).save(any());
    }

    @Test
    public void agregarFavorito_cuandoYaEsFavorito_noGuardaNada() {
        // Given
        given(usuarioRepository.findByEmail("test@user.com")).willReturn(Optional.of(usuario));
        given(restauranteRepository.findById(10L)).willReturn(Optional.of(restaurante));
        given(favoritoRepository.existsById(favoritoId)).willReturn(true); 

        // When
        favoritoService.agregarFavorito("test@user.com", 10L);

        // Then
        verify(favoritoRepository, never()).save(any());
    }

    // --- Tests para eliminarFavorito ---

    @Test
    public void eliminarFavorito_cuandoEsExitoso_eliminaElFavorito() {
        // Given
        given(usuarioRepository.findByEmail("test@user.com")).willReturn(Optional.of(usuario));
        given(restauranteRepository.findById(10L)).willReturn(Optional.of(restaurante));
        given(favoritoRepository.existsById(favoritoId)).willReturn(true);
        willDoNothing().given(favoritoRepository).deleteById(favoritoId);
        willDoNothing().given(favoritoRepository).flush(); 

        // When
        favoritoService.eliminarFavorito("test@user.com", 10L);

        // Then
        verify(favoritoRepository, times(1)).deleteById(favoritoId);
        verify(favoritoRepository, times(1)).flush();
    }

    @Test
    public void eliminarFavorito_cuandoNoEsFavorito_noEliminaNada() {
        // Given
        given(usuarioRepository.findByEmail("test@user.com")).willReturn(Optional.of(usuario));
        given(restauranteRepository.findById(10L)).willReturn(Optional.of(restaurante));
        given(favoritoRepository.existsById(favoritoId)).willReturn(false);

        // When
        favoritoService.eliminarFavorito("test@user.com", 10L);

        // Then
        verify(favoritoRepository, never()).deleteById(any());
    }

    // --- Tests para obtenerFavoritos ---

    @Test
    public void obtenerFavoritos_cuandoExistenFavoritos_retornaPaginaDTO() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        Favorito favoritoEntidad = new Favorito();
        favoritoEntidad.setRestaurante(restaurante);
        Page<Favorito> paginaFavoritos = new PageImpl<>(List.of(favoritoEntidad), pageable, 1);
        
       
        RestauranteDTO restauranteDTO = new RestauranteDTO();
        restauranteDTO.setId(10L);
        restauranteDTO.setNombre("Restaurante de Prueba");

        given(usuarioRepository.findByEmail("test@user.com")).willReturn(Optional.of(usuario));
        given(favoritoRepository.findByUsuario(usuario, pageable)).willReturn(paginaFavoritos);
        
        given(restauranteMapper.toDto(restaurante)).willReturn(restauranteDTO);

        // When
        Page<RestauranteDTO> resultado = favoritoService.obtenerFavoritos("test@user.com", pageable);

        // Then
        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Restaurante de Prueba", resultado.getContent().get(0).getNombre());
    }

    // --- Tests para obtenerFavoritos (Caminos de Error) ---

    @Test
    public void obtenerFavoritos_cuandoUsuarioNoExiste_lanzaConflictException() {
        // Given
        Pageable pageable = PageRequest.of(0, 5);
        
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty()); 

        // When & Then
        
        assertThrows(ConflictException.class, () -> {
            favoritoService.obtenerFavoritos("test@user.com", pageable);
        });
        
        verify(favoritoRepository, never()).findByUsuario(any(), any());
    }

// --- Tests para eliminarFavorito (Caminos de Error) ---

    @Test
    public void eliminarFavorito_cuandoUsuarioNoExiste_lanzaConflictException() {
        // Given
      
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.empty()); 

        // When & Then
        assertThrows(ConflictException.class, () -> {
            favoritoService.eliminarFavorito("test@user.com", 10L);
        });

        verify(favoritoRepository, never()).deleteById(any());
    }

    @Test
    public void eliminarFavorito_cuandoRestauranteNoExiste_lanzaConflictException() {
        // Given
        given(usuarioRepository.findByEmail(anyString())).willReturn(Optional.of(usuario));
        given(restauranteRepository.findById(anyLong())).willReturn(Optional.empty()); 

        // When & Then
        assertThrows(ConflictException.class, () -> {
            favoritoService.eliminarFavorito("test@user.com", 10L);
        });

        verify(favoritoRepository, never()).deleteById(any());
    }
}