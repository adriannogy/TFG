package com.restaurantes.restaurantesaplicacion.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurantes.restaurantesaplicacion.dto.ProfileDTO;
import com.restaurantes.restaurantesaplicacion.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/perfil")
@Tag(name = "8. Perfil", description = "Endpoints para obtener datos combinados del perfil del usuario.")
@SecurityRequirement(name = "bearerAuth")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Obtener los datos completos del perfil del usuario autenticado")
     @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Datos del perfil")
   })
    @GetMapping("/me")
    public ResponseEntity<ProfileDTO> getMyProfile(Principal principal) {
        ProfileDTO profile = userService.getFullProfile(principal.getName());
        return ResponseEntity.ok(profile);
    }
}