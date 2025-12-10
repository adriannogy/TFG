package com.restaurantes.restaurantesaplicacion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurantes.restaurantesaplicacion.dto.ValoracionDTO;
import com.restaurantes.restaurantesaplicacion.model.Valoracion;

@Mapper(componentModel = "spring", uses = { RestauranteMapper.class })
public interface ValoracionMapper {


    
    @Mapping(source = "restaurante", target = "restaurante")
    @Mapping(source = "usuario.nombreUsuario", target = "nombreUsuario")
    @Mapping(target = "fotos", ignore = true)
    ValoracionDTO toDto(Valoracion valoracion);
}
