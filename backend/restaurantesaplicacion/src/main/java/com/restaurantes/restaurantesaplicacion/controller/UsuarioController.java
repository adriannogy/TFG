package com.restaurantes.restaurantesaplicacion.controller;

import java.security.Principal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.restaurantes.restaurantesaplicacion.dto.ErrorResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.PasswordChangeDTO;
import com.restaurantes.restaurantesaplicacion.dto.UserProfileDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioEmailUpdateDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioNombreUpdateDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioSimpleDTO;
import com.restaurantes.restaurantesaplicacion.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@Tag(name = "2. Gestión de Usuarios", description = "Endpoints para gestionar perfiles de usuario (requiere autenticación).")
@SecurityRequirement(name = "bearerAuth")
public class UsuarioController {

    private static final Logger log = LoggerFactory.getLogger(UsuarioController.class);


    @Autowired
    private UserService userService;


    @Operation(summary = "Obtener todos los usuarios (Rol de Admin conceptual)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista de usuarios obtenida"),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (sin token válido)",
             content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        )
    })
    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> obtenerTodos() {
        log.info("Recibida petición GET para obtener todos los usuarios");
        return new ResponseEntity<>(userService.obtenerTodosLosUsuarios(), HttpStatus.OK);
    }


    @Operation(summary = "Obtener un usuario por su ID (Rol de Admin conceptual)")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "usuario correcto"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos",
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
    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerPorId(@PathVariable Long id) {
         log.info("Recibida petición GET para obtener usuario con ID: {}", id);
        return new ResponseEntity<>(userService.obtenerUsuarioPorId(id), HttpStatus.OK);
    }

    @Operation(summary = "Dar de baja la cuenta del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Cuenta dada de baja con éxito" ),
        @ApiResponse(responseCode = "403", description = "Acceso denegado (sin token válido)",
             content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        )
    })
    @DeleteMapping("/me")
    public ResponseEntity<String> darDeBajaMiCuenta() {
    userService.darDeBajaMiCuenta();
    return new ResponseEntity<>("La cuenta ha sido dada de baja con éxito.", HttpStatus.OK);
    }

    @Operation(summary = "Actualizar el nombre")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Nombre actualizado con éxito"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o el nombre ya está en uso",
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
    @PutMapping("/nombre-usuario")
    public ResponseEntity<UsuarioResponseDTO> actualizarNombreUsuario(
            Principal principal, 
            @Valid @RequestBody UsuarioNombreUpdateDTO nombreUpdateDTO) {

        log.info("Petición PUT para actualizar nombre de usuario: {}", principal.getName());
        String emailActual = principal.getName();
        UsuarioResponseDTO usuarioActualizado = userService.actualizarNombreUsuario(emailActual, nombreUpdateDTO.getNombreUsuario());
        return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
    }

    @Operation(summary = "Actualizar el email")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Email actualizado con éxito"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o el email ya está en uso",
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
    @PutMapping("/email")
    public ResponseEntity<UsuarioResponseDTO> actualizarEmail(
            Principal principal, 
            @Valid @RequestBody UsuarioEmailUpdateDTO emailUpdateDTO) {

        log.info("Petición PUT para actualizar email de: {}", principal.getName());
        String emailActual = principal.getName();
        UsuarioResponseDTO usuarioActualizado = userService.actualizarEmail(emailActual, emailUpdateDTO.getEmail());
        return new ResponseEntity<>(usuarioActualizado, HttpStatus.OK);
    }

    @Operation(summary = "Actualizar la foto de perfil del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Foto de perfil actualizada con éxito"),
        @ApiResponse(responseCode = "400", description = "No se proporcionó ningún archivo",
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
    @PutMapping(value = "/perfil/foto", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UsuarioResponseDTO> actualizarFotoPerfil(
            Principal principal, 
            @RequestParam("file") MultipartFile file) {

        log.info("Petición PUT para actualizar foto de perfil de: {}", principal.getName());
        UsuarioResponseDTO usuarioActualizado = userService.actualizarFotoPerfil(principal.getName(), file);
        return ResponseEntity.ok(usuarioActualizado);
    }

    @Operation(summary = "Cambiar la contraseña del usuario autenticado")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Contraseña actualizada con éxito"),
        @ApiResponse(responseCode = "401", description = "La contraseña antigua es incorrecta",
             content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        ),
        @ApiResponse(responseCode = "403", description = "Acceso denegado",
             content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
        )
    })
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/password")
    public ResponseEntity<String> changePassword(
            Principal principal, 
            @RequestBody PasswordChangeDTO dto) {

        userService.changePassword(principal.getName(), dto);
        return ResponseEntity.ok("Contraseña actualizada con éxito.");
    }


    @Operation(summary = "Buscar usuarios por nombre de usuario")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Búsqueda exitosa"),
    @ApiResponse(responseCode = "403", description = "Acceso denegado",
         content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
    )
    })
    @GetMapping("/buscar")
    public ResponseEntity<List<UsuarioSimpleDTO>> buscarUsuarios(@RequestParam("q") String query) {
        log.info("Recibida petición GET para buscar usuarios con query: {}", query);
        return ResponseEntity.ok(userService.buscarUsuarios(query));
    }


    @Operation(summary = "Ver el perfil de otro usuario")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Perfil del usuario"),
    @ApiResponse(responseCode = "404", description = "Usuario no encontrado",
         content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )
    )
    })
    @GetMapping("/{nombreUsuario}/perfil")
    public ResponseEntity<UserProfileDTO> verPerfilDeUsuario(
        @PathVariable String nombreUsuario, Principal principal) {

        UserProfileDTO profile = userService.verPerfilUsuario(principal.getName(), nombreUsuario);
        return ResponseEntity.ok(profile);
    }
}