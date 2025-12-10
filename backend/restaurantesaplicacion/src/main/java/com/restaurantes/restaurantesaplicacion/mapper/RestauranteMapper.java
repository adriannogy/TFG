package com.restaurantes.restaurantesaplicacion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurantes.restaurantesaplicacion.dto.RestauranteDTO;
import com.restaurantes.restaurantesaplicacion.model.Restaurante;


@Mapper(componentModel = "spring")
public interface RestauranteMapper {


    @Mapping(source = "osmId", target = "osmId")
    RestauranteDTO toDto(Restaurante restaurante);

    

    @Mapping(target = "valoraciones", ignore = true)
    @Mapping(target = "favoritos", ignore = true)
    @Mapping(target = "osmId", source = "osmId") 
    Restaurante toEntity(RestauranteDTO restauranteDTO);
    
}