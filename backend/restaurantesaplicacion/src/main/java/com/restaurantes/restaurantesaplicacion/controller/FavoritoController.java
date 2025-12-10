package com.restaurantes.restaurantesaplicacion.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurantes.restaurantesaplicacion.dto.ErrorResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.RestauranteDTO;
import com.restaurantes.restaurantesaplicacion.service.FavoritoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "7. Favoritos", description = "Endpoints para gestionar los restaurantes favoritos de un usuario.")
@SecurityRequirement(name = "bearerAuth")
public class FavoritoController {

    @Autowired
    private FavoritoService favoritoService;

   @Operation(summary = "Obtener la lista de restaurantes favoritos del usuario autenticado")
   @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de favoritos a continuacion")
   })
    @GetMapping
    public ResponseEntity<Page<RestauranteDTO>> obtenerMisFavoritos(Principal principal, Pageable pageable) {
        return ResponseEntity.ok(favoritoService.obtenerFavoritos(principal.getName(), pageable));
    }


    @Operation(summary = "Añadir un restaurante a favoritos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Restaurante añadido a favoritos"),
        @ApiResponse(responseCode = "404", description = "Restaurante no encontrado",
            content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        )
    })
    @PostMapping("/{restauranteId}")
    public ResponseEntity<Void> agregarFavorito(@PathVariable Long restauranteId, Principal principal) {
        favoritoService.agregarFavorito(principal.getName(), restauranteId);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Eliminar un restaurante de favoritos")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Restaurante eliminado de favoritos"),
        @ApiResponse(responseCode = "404", description = "Restaurante no encontrado",
            content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        )
    })
    @DeleteMapping("/{restauranteId}")
    public ResponseEntity<Void> eliminarFavorito(@PathVariable Long restauranteId, Principal principal) {
        favoritoService.eliminarFavorito(principal.getName(), restauranteId);
        return ResponseEntity.noContent().build();
    }
}