package com.restaurantes.restaurantesaplicacion.repository;

import com.restaurantes.restaurantesaplicacion.model.Foto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FotoRepository extends JpaRepository<Foto, Long> {
   
}