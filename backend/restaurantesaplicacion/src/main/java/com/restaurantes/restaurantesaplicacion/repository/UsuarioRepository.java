package com.restaurantes.restaurantesaplicacion.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import com.restaurantes.restaurantesaplicacion.model.Usuario;
import java.util.List;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByNombreUsuario(String nombreUsuario);
    Optional<Usuario> findByEmail(String email);
    Optional<Usuario> findByResetToken(String resetToken);
    Optional<Usuario> findByTokenVerificacion(String token);
    List<Usuario> findByNombreUsuarioStartingWithIgnoreCase(String nombreUsuario);
}
