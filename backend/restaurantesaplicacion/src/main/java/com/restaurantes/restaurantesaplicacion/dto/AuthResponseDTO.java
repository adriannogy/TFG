package com.restaurantes.restaurantesaplicacion.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Proporciona getters, setters, toString, equals y hashCode (Lombok)
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Respuesta devuelta al iniciar sesión con éxito, contiene el token de acceso.")
public class AuthResponseDTO {

    private String token;
    private String tokenType = "Bearer";
    private String nombreUsuario;

}