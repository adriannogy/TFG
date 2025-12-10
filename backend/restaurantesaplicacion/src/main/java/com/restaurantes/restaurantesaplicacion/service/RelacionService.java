package com.restaurantes.restaurantesaplicacion.service;

import com.restaurantes.restaurantesaplicacion.dto.UsuarioSimpleDTO;
import java.util.List;
/**
 * Interfaz para el servicio que gestiona las relaciones de seguimiento entre usuarios.
 */
public interface RelacionService {

/**
     * Un usuario (seguidor) envía una solicitud de seguimiento a otro (seguido).
     * @param emailSeguidor Email del usuario que envía la solicitud.
     * @param nombreSeguido Nombre del usuario que recibe la solicitud.
     */
    void solicitarSeguimiento(String emailSeguidor, String nombreSeguido);
/**
     * Un usuario (receptor) acepta una solicitud de seguimiento de otro (emisor).
     * @param emailReceptor Email del usuario que acepta.
     * @param nombreEmisor Nombre del usuario que envió la solicitud.
     */
    void aceptarSolicitud(String emailReceptor, String nombreEmisor);
 /**
     * Un usuario (receptor) rechaza una solicitud de seguimiento de otro (emisor).
     * @param emailReceptor Email del usuario que rechaza.
     * @param nombreEmisor Nombre del usuario que envió la solicitud.
     */
    void rechazarSolicitud(String emailReceptor, String nombreEmisor);
/**
     * Obtiene la lista de seguidores del propio perfil de usuario, verificando permisos.
     * @param email
     * @return Lista de seguidores.
     * @throws org.springframework.security.access.AccessDeniedException si el viewer no tiene permiso.
     */
    List<UsuarioSimpleDTO> obtenerMisSeguidores(String email);
/**
     * Obtiene la lista de seguidores de un perfil de usuario, verificando permisos.
     * @param emailViewer Email del usuario que realiza la consulta.
     * @param nombrePerfil Nombre del usuario cuyo perfil se está viendo.
     * @return Lista de seguidores.
     * @throws org.springframework.security.access.AccessDeniedException si el viewer no tiene permiso.
     */
    List<UsuarioSimpleDTO> obtenerSeguidoresDe(String emailViewer, String nombrePerfil);
/**
     * Obtiene la lista de usuarios a los que sigue el propio usuario del perfil, verificando permisos.
     * @param email
     * @return Lista de usuarios seguidos.
     * @throws org.springframework.security.access.AccessDeniedException si el viewer no tiene permiso.
     */
    List<UsuarioSimpleDTO> obtenerMisSiguiendo(String email);
/**
     * Obtiene la lista de usuarios a los que sigue sigue un perfil, verificando permisos.
     * @param emailViewer Email del usuario que realiza la consulta.
     * @param nombrePerfil Nombre del usuario cuyo perfil se está viendo.
     * @return Lista de usuarios seguidos.
     * @throws org.springframework.security.access.AccessDeniedException si el viewer no tiene permiso.
     */
    List<UsuarioSimpleDTO> obtenerSiguiendoDe(String emailViewer, String nombrePerfil);
/**
     * Obtiene la lista de solicitudes de seguimiento pendientes para el usuario autenticado.
     * @param email Email del usuario autenticado.
     * @return Lista de usuarios que han enviado una solicitud.
     */
    List<UsuarioSimpleDTO> obtenerSolicitudesPendientes(String email);
/**
     * Un usuario (seguido) elimina a uno de sus seguidores.
     * @param emailSeguido Email del usuario que elimina al seguidor.
     * @param nombreSeguidor Nombre del seguidor a eliminar.
     */
    void eliminarSeguidor(String emailSeguido, String nombreSeguidor);

/**
     * Un usuario (seguidor) deja de seguir a otro (seguido).
     * @param emailSeguidor Email del usuario que realiza la acción.
     * @param nombreSeguido Nombre del usuario que se va a dejar de seguir.
     */
    void dejarDeSeguir(String emailSeguidor, String nombreSeguido);

}