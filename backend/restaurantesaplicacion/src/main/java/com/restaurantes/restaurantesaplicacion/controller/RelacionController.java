package com.restaurantes.restaurantesaplicacion.controller;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurantes.restaurantesaplicacion.dto.ErrorResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioSimpleDTO;
import com.restaurantes.restaurantesaplicacion.service.RelacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/relaciones")
@Tag(name = "6. Relaciones (Seguimiento)", description = "Endpoints para gestionar el sistema de seguimiento entre usuarios.")
@SecurityRequirement(name = "bearerAuth")
public class RelacionController {

    private static final Logger log = LoggerFactory.getLogger(RelacionController.class);

    @Autowired
    private RelacionService relacionService;

    @Operation(summary = "Enviar una solicitud para seguir a otro usuario")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Solicitud enviada con éxito"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
            ),
            @ApiResponse(responseCode = "404", description = "Usuario a seguir no encontrado",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
            )
    })
    @PostMapping("/seguir/{nombreUsuario}")
    public ResponseEntity<String> solicitarSeguimiento(@PathVariable String nombreUsuario, Principal principal) {
        log.info("Petición de '{}' para seguir a '{}'", principal.getName(), nombreUsuario);
        relacionService.solicitarSeguimiento(principal.getName(), nombreUsuario);
        return ResponseEntity.ok("Solicitud de seguimiento enviada a " + nombreUsuario);
    }

    @Operation(summary = "Aceptar una solicitud de seguimiento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Solicitud aceptada"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
            ),
            @ApiResponse(responseCode = "404", description = "Solicitud o usuario emisor no encontrado",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
            )
    })
    @PostMapping("/solicitudes/aceptar/{nombreEmisor}")
    public ResponseEntity<String> aceptarSolicitud(@PathVariable String nombreEmisor, Principal principal) {
        log.info("Petición de '{}' para aceptar a '{}'", principal.getName(), nombreEmisor);
        relacionService.aceptarSolicitud(principal.getName(), nombreEmisor);
        return ResponseEntity.ok("Has aceptado a " + nombreEmisor);
    }

    @Operation(summary = "Rechazar una solicitud de seguimiento")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Solicitud rechazada"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
            ),
            @ApiResponse(responseCode = "404", description = "Solicitud o usuario emisor no encontrado",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
            )
    })
    @PostMapping("/solicitudes/rechazar/{nombreEmisor}")
    public ResponseEntity<String> rechazarSolicitud(@PathVariable String nombreEmisor, Principal principal) {
        log.info("Petición de '{}' para rechazar a '{}'", principal.getName(), nombreEmisor);
        relacionService.rechazarSolicitud(principal.getName(), nombreEmisor);
        return ResponseEntity.ok("Has rechazado a " + nombreEmisor);
    }

    @Operation(summary = "Ver la lista de TUS PROPIOS seguidores")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Lista obtenida"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado",
         content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
    ),
    })
    @GetMapping("/seguidores")
    public ResponseEntity<List<UsuarioSimpleDTO>> obtenerMisSeguidores(Principal principal) {
        return ResponseEntity.ok(relacionService.obtenerMisSeguidores(principal.getName()));
    }

    @Operation(summary = "Ver la lista de seguidores de OTRO usuario")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Lista obtenida"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado",
         content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
    ),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
         content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
    )
    })
    @GetMapping("/{nombreUsuario}/seguidores")
    public ResponseEntity<List<UsuarioSimpleDTO>> obtenerSeguidoresDeUsuario(@PathVariable String nombreUsuario, Principal principal) {
        return ResponseEntity.ok(relacionService.obtenerSeguidoresDe(principal.getName(), nombreUsuario));
    }

    
    
    @Operation(summary = "Ver la lista de usuarios a los que TÚ sigues")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Lista obtenida"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado",
         content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
    ),
    })
    @GetMapping("/siguiendo")
    public ResponseEntity<List<UsuarioSimpleDTO>> obtenerMisSiguiendo(Principal principal) {
        return ResponseEntity.ok(relacionService.obtenerMisSiguiendo(principal.getName()));
    }

    @Operation(summary = "Ver la lista de usuarios a los que sigue OTRO usuario")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Lista obtenida"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado",
         content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
    ),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
         content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
    )
    })
    @GetMapping("/{nombreUsuario}/siguiendo")
    public ResponseEntity<List<UsuarioSimpleDTO>> obtenerSiguiendoDeUsuario(@PathVariable String nombreUsuario, Principal principal) {
        return ResponseEntity.ok(relacionService.obtenerSiguiendoDe(principal.getName(), nombreUsuario));
    }
    
    @Operation(summary = "Ver la lista de solicitudes pendientes")
    @GetMapping("/solicitudes/pendientes")
    public ResponseEntity<List<UsuarioSimpleDTO>> obtenerSolicitudesPendientes(Principal principal) {
        log.info("Petición para obtener solicitudes pendientes para '{}'", principal.getName());
        return ResponseEntity.ok(relacionService.obtenerSolicitudesPendientes(principal.getName()));
    }

    
    @Operation(summary = "Eliminar un seguidor")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Seguidor eliminado"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado",
             content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        ),
        @ApiResponse(responseCode = "404", description = "Seguidor no encontrado en tu lista",
             content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        )
    })
    @DeleteMapping("/seguidores/{nombreSeguidor}")
    public ResponseEntity<String> eliminarSeguidor(@PathVariable String nombreSeguidor, Principal principal) {
        String emailUsuarioLogueado = principal.getName();
        log.info("Petición de '{}' para eliminar a su seguidor '{}'", emailUsuarioLogueado, nombreSeguidor);

        relacionService.eliminarSeguidor(emailUsuarioLogueado, nombreSeguidor);

        return ResponseEntity.ok("El seguidor ha sido eliminado con éxito.");
    }


    @Operation(summary = "Dejar de seguir a un usuario (o cancelar una solicitud)")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "204", description = "Dejado de seguir con éxito"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado",
         content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
    ),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
         content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
    )
    })
    @DeleteMapping("/dejar-de-seguir/{nombreUsuario}")
    public ResponseEntity<String> dejarDeSeguir(@PathVariable String nombreUsuario, Principal principal) {
        String emailUsuarioLogueado = principal.getName();
        log.info("Petición de '{}' para dejar de seguir a '{}'", emailUsuarioLogueado, nombreUsuario);
    
        relacionService.dejarDeSeguir(emailUsuarioLogueado, nombreUsuario);
    
        return ResponseEntity.ok("Has dejado de seguir a " + nombreUsuario);
    }

}