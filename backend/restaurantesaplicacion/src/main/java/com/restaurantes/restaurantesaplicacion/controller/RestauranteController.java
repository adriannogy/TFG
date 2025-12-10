package com.restaurantes.restaurantesaplicacion.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurantes.restaurantesaplicacion.dto.ErrorResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.RestauranteDTO;
import com.restaurantes.restaurantesaplicacion.dto.RestauranteImportDTO;
import com.restaurantes.restaurantesaplicacion.dto.osm.ElementoRestauranteDTO;
import com.restaurantes.restaurantesaplicacion.service.RestauranteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;


@RestController
@RequestMapping("/api/restaurantes")
@Tag(name = "3. Gestión de Restaurantes", description = "Endpoints para crear y buscar restaurantes.")
public class RestauranteController {

     private static final Logger log = LoggerFactory.getLogger(RestauranteController.class);


    @Autowired
    private RestauranteService restauranteService;

    
    
    @Operation(summary = "Buscar restaurantes en la base de datos local con filtros opcionales")
    @GetMapping
    public ResponseEntity<Page<RestauranteDTO>> buscarRestaurantes(
            @RequestParam(required = false) String nombre,
            @RequestParam(required = false) String ciudad,
            @RequestParam(required = false) String tipo_cocina,
            @RequestParam(required = false) String direccion,
            Pageable pageable) {
         log.info("Petición GET a /api/restaurantes/buscar recibida con parámetros: nombre={}, ciudad={}, categoria={}", nombre, ciudad, tipo_cocina);
        return new ResponseEntity<>(restauranteService.buscarRestaurantes(nombre, ciudad, tipo_cocina,direccion,pageable), HttpStatus.OK);
    }

 


    @Operation(summary = "Obtener los Restaurantes de la API")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Búsqueda exitosa"),
            @ApiResponse(responseCode = "404", description = "Ciudad no encontrada en la API externa",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
            )
    })
    @GetMapping("/externos/buscar")
    public ResponseEntity<Page<ElementoRestauranteDTO>> obtenerRestaurantesExternos(
        @RequestParam(required = false) String nombre,
        @RequestParam String ciudad,
        @RequestParam(required = false) String tipoCocina,
        @RequestParam(required = false) String direccion,
        Pageable pageable){
        return new ResponseEntity<>(restauranteService.buscarRestaurantesExternos(ciudad, nombre,tipoCocina, direccion, pageable), HttpStatus.OK);
    }

   
    @Operation(summary = "Sincronizar con la API externa")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Sincronización iniciada"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
            ),
            @ApiResponse(responseCode = "504", description = "Timeout: la API externa tardó demasiado en responder",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/sincronizar")
    public ResponseEntity<String> sincronizar(@RequestParam String ciudad) {
        restauranteService.sincronizarRestaurantesExternos(ciudad);
        return new ResponseEntity<>("Sincronización para la ciudad de " + ciudad + " iniciada.", HttpStatus.OK);
    }

    @Operation(summary = "Obtener un restaurante por su ID (público)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Restaurante encontrado"),
            @ApiResponse(responseCode = "404", description = "Restaurante no encontrado",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<RestauranteDTO> obtenerPorId(@PathVariable Long id) {
        log.info("Recibida petición GET para obtener restaurante con ID: {}", id);
        return new ResponseEntity<>(restauranteService.obtenerRestaurantePorId(id), HttpStatus.OK);
    }


    @Operation(summary = "Guardar un restaurante")
     @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Restaurante creado con éxito"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la petición",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
            ),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (sin token válido)",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping
    public ResponseEntity<RestauranteDTO> guardarRestaurante(@Valid @RequestBody RestauranteDTO restauranteDTO) {
        RestauranteDTO restauranteGuardado = restauranteService.guardarRestaurante(restauranteDTO);
        return new ResponseEntity<>(restauranteGuardado, HttpStatus.CREATED);
    }


    @Operation(summary = "Importa un restaurante desde la API externa a la BD local")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Restaurante importado o encontrado con éxito"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado",
             content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping("/importar")
    public ResponseEntity<RestauranteDTO> importarRestaurante(@RequestBody RestauranteImportDTO importDTO) {
        RestauranteDTO restaurante = restauranteService.importarRestauranteExterno(importDTO);
        return ResponseEntity.ok(restaurante);
    }

}
