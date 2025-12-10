package com.restaurantes.restaurantesaplicacion.service;


import com.restaurantes.restaurantesaplicacion.dto.PasswordChangeDTO;
import com.restaurantes.restaurantesaplicacion.dto.ProfileDTO;
import com.restaurantes.restaurantesaplicacion.dto.UserProfileDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioRegistroDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioSimpleDTO;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;
/**
 * Interfaz para el servicio de gestión de usuarios.
 * Define las operaciones de negocio relacionadas con los usuarios.
 */
public interface UserService {
    
/**
     * Obtiene una lista de todos los usuarios activos.
     *
     * @return una lista de DTOs de usuario.
     */
    List<UsuarioResponseDTO> obtenerTodosLosUsuarios();
 /**
     * Busca un usuario por su ID.
     *
     * @param id El ID del usuario a buscar.
     * @return un DTO con la información del usuario encontrado.
     * @throws com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException si no se encuentra el usuario.
     */
    UsuarioResponseDTO obtenerUsuarioPorId(Long id);

/**
     * Crea un nuevo usuario o reactiva uno inactivo.
     *
     * @param usuarioDTO DTO con los datos para el registro (nombre, email, contraseña).
     * @return un DTO con la información del usuario creado o reactivado, sin la contraseña.
     * @throws com.restaurantes.restaurantesaplicacion.exception.ConflictException si el email ya está en uso por una cuenta activa.
     */
    UsuarioResponseDTO crearUsuario(UsuarioRegistroDTO  usuarioDTO);

/**
     * Inicia el proceso de restablecimiento de contraseña para un usuario.
     * Genera un token y lo envía al email del usuario.
     *
     * @param email El email del usuario que ha olvidado su contraseña.
     * @throws com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException si no se encuentra un usuario con ese email.
     */
    void generatePasswordResetToken(String email);
/**
     * Restablece la contraseña de un usuario usando un token válido.
     * @param token El token de restablecimiento recibido por email.
     * @param newPassword La nueva contraseña a establecer.
     * @throws com.restaurantes.restaurantesaplicacion.exception.BadRequestException si el token es inválido o ha expirado.
     */
    void resetPassword(String token, String newPassword);
/**
     * Marca la cuenta del usuario autenticado como inactiva.
     * No borra el registro, solo establece una fecha de baja.
     */
    void darDeBajaMiCuenta();
 /**
     * Actualiza el nombre de usuario del usuario autenticado.
     * @param emailActual El email actual del usuario que realiza la acción.
     * @param nuevoNombre El nuevo nombre de usuario a establecer.
     * @return El DTO del usuario con los datos actualizados.
     * @throws com.restaurantes.restaurantesaplicacion.exception.ConflictException si el nuevo nombre de usuario ya está en uso.
     */
    UsuarioResponseDTO actualizarNombreUsuario(String emailActual, String nuevoNombre);
 /**
     * Actualiza el email del usuario autenticado.
     * @param emailActual El email actual del usuario que realiza la acción.
     * @param nuevoEmail El nuevo email a establecer.
     * @return El DTO del usuario con los datos actualizados.
     * @throws com.restaurantes.restaurantesaplicacion.exception.ConflictException si el nuevo email ya está en uso.
     */
    UsuarioResponseDTO actualizarEmail(String emailActual, String nuevoEmail);


    /**
     * Actualiza la foto de perfil del usuario.
     * Sube el archivo al servicio de almacenamiento y guarda la URL en el usuario.
     *
     * @param email El email del usuario autenticado.
     * @param file El archivo de imagen a subir.
     * @return El DTO del usuario con la URL de la foto actualizada.
     */
    UsuarioResponseDTO actualizarFotoPerfil(String email, MultipartFile file);

    /**
     * Obtiene el perfil completo del usuario
     *
     * @param email El email del usuario autenticado.
     * @return El DTO del Perfil del usuario
     */
    ProfileDTO getFullProfile(String email);
    /**
     * email de verificacion de registro
     *
     * @param token
     */
    void verificarUsuario(String token);

    /**
     * Cambio de contrasena
     * 
     *
     * @param email
     * @param dto
     */
    void changePassword(String email, PasswordChangeDTO dto);


    /**
     * Busca usuarios
     *
     * @param query
     * @return Lista de usuarios
     */
    List<UsuarioSimpleDTO> buscarUsuarios(String query);

     /**
     * Ver perfil de un usuario
     *
     * @param emailViewer
     * @param nombrePerfil
     * @return Perfil de usuario
     */
    UserProfileDTO verPerfilUsuario(String emailViewer, String nombrePerfil);
}
