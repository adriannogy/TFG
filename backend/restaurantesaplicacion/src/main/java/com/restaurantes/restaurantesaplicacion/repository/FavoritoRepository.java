package com.restaurantes.restaurantesaplicacion.repository;

import com.restaurantes.restaurantesaplicacion.model.Favorito;
import com.restaurantes.restaurantesaplicacion.model.FavoritoId;
import com.restaurantes.restaurantesaplicacion.model.Usuario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FavoritoRepository extends JpaRepository<Favorito, FavoritoId> {

    List<Favorito> findByUsuario(Usuario usuario);
    Page<Favorito> findByUsuario(Usuario usuario, Pageable pageable);
}