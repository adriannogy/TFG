package com.restaurantes.restaurantesaplicacion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import com.restaurantes.restaurantesaplicacion.model.Restaurante;

@Repository
public interface RestauranteRepository extends JpaRepository<Restaurante, Long>, JpaSpecificationExecutor<Restaurante> {
    
     Optional<Restaurante> findByOsmId(Long osmId);
     Optional<Restaurante> findByNombre(String nombre);
     List<Restaurante> findByNombreContainingIgnoreCase(String nombre);
}