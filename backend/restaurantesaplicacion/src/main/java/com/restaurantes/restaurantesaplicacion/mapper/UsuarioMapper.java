package com.restaurantes.restaurantesaplicacion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurantes.restaurantesaplicacion.dto.UsuarioResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioSimpleDTO;
import com.restaurantes.restaurantesaplicacion.model.Usuario;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
   
    UsuarioResponseDTO toUsuarioResponseDto(Usuario usuario);
    UsuarioSimpleDTO toSimpleDto(Usuario usuario);
    
    @Mapping(target = "valoraciones", ignore = true)
    @Mapping(target = "siguiendo", ignore = true)
    @Mapping(target = "seguidores", ignore = true)
    @Mapping(target = "favoritos", ignore = true)
    Usuario toEntity(UsuarioResponseDTO usuarioResponseDTO);
    
}