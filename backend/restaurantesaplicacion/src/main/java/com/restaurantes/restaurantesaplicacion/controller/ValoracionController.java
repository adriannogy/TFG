package com.restaurantes.restaurantesaplicacion.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurantes.restaurantesaplicacion.dto.ErrorResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.ValoracionDTO;
import com.restaurantes.restaurantesaplicacion.dto.ValoracionInputDTO;
import com.restaurantes.restaurantesaplicacion.service.ValoracionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/valoraciones")
@Tag(name = "5. Valoraciones", description = "Endpoints para crear y ver valoraciones.")
public class ValoracionController {

    @Autowired
    private ValoracionService valoracionService;

    @Autowired
    private ObjectMapper objectMapper;


    @Operation(summary = "Crear una nueva valoración (protegido)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Valoración creada con éxito"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (sin token válido)",
             content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        ),
        @ApiResponse(responseCode = "404", description = "Usuario o Restaurante no encontrado",
             content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        ),
        @ApiResponse(responseCode = "409", description = "Conflicto: El usuario ya ha valorado este restaurante",
             content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/usuario/{nombreUsuario}")
    public ResponseEntity<ValoracionDTO> crearValoracion(
            @PathVariable String nombreUsuario,
            @RequestParam("valoracion") String valoracionJson,
            @RequestParam(value = "files", required = false) MultipartFile[] files) throws Exception {
        
        ValoracionInputDTO valoracionInputDTO = objectMapper.readValue(valoracionJson, ValoracionInputDTO.class);

        ValoracionDTO nuevaValoracion = valoracionService.crearValoracion(nombreUsuario, valoracionInputDTO, files);
        return new ResponseEntity<>(nuevaValoracion, HttpStatus.CREATED);
    }

    @Operation(summary = "Obtener todas las valoraciones (público)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de todas las valoraciones")
    })
    @GetMapping 
    public ResponseEntity<List<ValoracionDTO>> obtenerTodasLasValoraciones() {
        return new ResponseEntity<>(valoracionService.obtenerTodas(), HttpStatus.OK);
    }

    @Operation(summary = "Obtener el feed de reseñas de usuarios seguidos (protegido)")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/feed")
    public ResponseEntity<Page<ValoracionDTO>> obtenerFeed(Principal principal, Pageable pageable) {
        Page<ValoracionDTO> feed = valoracionService.obtenerFeedParaUsuario(principal.getName(), pageable);
        return ResponseEntity.ok(feed);
    }

    @Operation(summary = "Eliminar una valoración propia (protegido)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Valoración eliminada con éxito"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado",
             content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        ),
        @ApiResponse(responseCode = "404", description = "Valoración no encontrada",
             content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @DeleteMapping("/{restauranteId}")
    public ResponseEntity<Void> eliminarValoracion(
            @PathVariable Long restauranteId,
            Principal principal) {
        
        valoracionService.eliminarValoracion(principal.getName(), restauranteId);
        return ResponseEntity.noContent().build();
    }
}
