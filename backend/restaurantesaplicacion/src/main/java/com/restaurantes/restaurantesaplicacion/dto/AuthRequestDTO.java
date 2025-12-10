package com.restaurantes.restaurantesaplicacion.dto;

import lombok.Data;

@Data
public class AuthRequestDTO {
    private String email;
    private String pwd;
}