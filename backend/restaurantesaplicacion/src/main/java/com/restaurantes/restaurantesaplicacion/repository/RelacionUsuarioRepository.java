package com.restaurantes.restaurantesaplicacion.repository;

import com.restaurantes.restaurantesaplicacion.model.EstadoSolicitud;
import com.restaurantes.restaurantesaplicacion.model.RelacionUsuario;
import com.restaurantes.restaurantesaplicacion.model.RelacionUsuarioId;
import com.restaurantes.restaurantesaplicacion.model.Usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RelacionUsuarioRepository extends JpaRepository<RelacionUsuario, RelacionUsuarioId> {

    List<RelacionUsuario> findBySeguidorAndEstado(Usuario seguidor, EstadoSolicitud estado);
    List<RelacionUsuario> findBySeguidoAndEstado(Usuario seguido, EstadoSolicitud estado);
}

