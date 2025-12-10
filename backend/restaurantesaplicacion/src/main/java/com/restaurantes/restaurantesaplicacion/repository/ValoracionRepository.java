package com.restaurantes.restaurantesaplicacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.restaurantes.restaurantesaplicacion.model.Usuario;
import com.restaurantes.restaurantesaplicacion.model.Valoracion;
import com.restaurantes.restaurantesaplicacion.model.ValoracionId;
import java.util.List;

@Repository
public interface ValoracionRepository extends JpaRepository<Valoracion, ValoracionId> {
    Page<Valoracion> findByUsuarioInOrderByFechaCreacionDesc(List<Usuario> usuarios, Pageable pageable);
}
