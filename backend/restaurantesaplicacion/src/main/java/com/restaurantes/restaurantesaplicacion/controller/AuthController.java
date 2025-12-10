package com.restaurantes.restaurantesaplicacion.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurantes.restaurantesaplicacion.dto.AuthRequestDTO;
import com.restaurantes.restaurantesaplicacion.dto.AuthResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.ErrorResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioRegistroDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioResponseDTO;
import com.restaurantes.restaurantesaplicacion.security.JwtUtil;
import com.restaurantes.restaurantesaplicacion.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/auth")
@Tag(name = "1. Autenticación", description = "Endpoints para registro, login y recuperación de cuenta.")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserDetailsService userDetailsService;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private UserService userService;




    @Operation(summary = "Registrar un nuevo usuario")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Usuario registrado con éxito", 
                 content = @Content(schema = @Schema(implementation = UsuarioResponseDTO.class))),
    
    @ApiResponse(responseCode = "400", 
                 description = "Datos de registro inválidos (ej. validación fallida)",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )),
                 
    @ApiResponse(responseCode = "409", 
                 description = "El email o nombre de usuario ya está registrado.",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 ))
})
    @PostMapping("/register")
    public ResponseEntity<UsuarioResponseDTO> registerUser(@RequestBody UsuarioRegistroDTO registroDTO) {
        return ResponseEntity.ok(userService.crearUsuario(registroDTO));
    }



    @Operation(summary = "Iniciar sesión para obtener un token JWT")
    @ApiResponses(value = {
    // 1. Respuesta Exitosa (200) - Asegúrate de que apunte a tu DTO de respuesta de login
    @ApiResponse( responseCode = "200", description = "Login exitoso, devuelve el token JWT",
        content = @Content(
        mediaType = "application/json",
        schema = @Schema(implementation = AuthResponseDTO.class) 
    )
), 

    // 2. Credenciales Incorrectas (401) - USA EL CÓDIGO 401
    @ApiResponse(responseCode = "401", 
                 description = "Credenciales incorrectas (Email/contraseña no válidos)",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 )),
                 
    // 3. Cuenta Bloqueada/No Verificada (403)
    @ApiResponse(responseCode = "403", 
                 description = "Acceso denegado: La cuenta está bloqueada o no verificada.",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 ))
})
    @PostMapping("/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthRequestDTO authRequest) throws Exception {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getEmail(), authRequest.getPwd())
        );

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authRequest.getEmail());
        final String jwt = jwtUtil.generateToken(userDetails);

        return ResponseEntity.ok(jwt);
    }



    @Operation(summary = "Solicitar restablecimiento de contraseña")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email de restablecimiento enviado si el usuario existe"),
            @ApiResponse(responseCode = "404", description = "Usuario no encontrado con el email proporcionado",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 ))
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody Map<String, String> request) {
        userService.generatePasswordResetToken(request.get("email"));
        return ResponseEntity.ok("Se ha enviado un email de restablecimiento.");
    }



    @Operation(summary = "Restablecer la contraseña usando un token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Contraseña restablecida con éxito"),
            @ApiResponse(responseCode = "400", description = "Token inválido o expirado",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 ))
    })
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token, @RequestBody Map<String, String> request) {
        userService.resetPassword(token, request.get("pwd"));
        return ResponseEntity.ok("Contraseña restablecida con éxito.");
    }

    @Operation(summary = "Verificar una nueva cuenta de usuario usando un token")
    @ApiResponses(value = {
    @ApiResponse(responseCode = "200", description = "Usuario verificado con éxito"),
    @ApiResponse(responseCode = "400", description = "Token inválido o expirado",
                 content = @Content(
                     mediaType = "application/json",
                     schema = @Schema(implementation = ErrorResponseDTO.class)
                 ))
    })
    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
        userService.verificarUsuario(token);
        return ResponseEntity.ok("¡Tu cuenta ha sido verificada con éxito! Ya puedes iniciar sesión.");
    }
}