package com.restaurantes.restaurantesaplicacion.service;

import org.springframework.security.authentication.LockedException;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioRegistroDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioResponseDTO;
import com.restaurantes.restaurantesaplicacion.dto.UsuarioSimpleDTO;
import com.restaurantes.restaurantesaplicacion.dto.ValoracionDTO;
import com.restaurantes.restaurantesaplicacion.exception.ConflictException;
import com.restaurantes.restaurantesaplicacion.exception.ResourceNotFoundException;
import com.restaurantes.restaurantesaplicacion.mapper.UsuarioMapper;
import com.restaurantes.restaurantesaplicacion.model.EstadoSolicitud;
import com.restaurantes.restaurantesaplicacion.model.Foto;
import com.restaurantes.restaurantesaplicacion.model.RelacionUsuario;
import com.restaurantes.restaurantesaplicacion.model.RelacionUsuarioId;
import com.restaurantes.restaurantesaplicacion.model.Usuario;
import com.restaurantes.restaurantesaplicacion.repository.RelacionUsuarioRepository;
import com.restaurantes.restaurantesaplicacion.repository.UsuarioRepository;
import java.util.UUID;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.restaurantes.restaurantesaplicacion.exception.BadRequestException;
import org.springframework.web.multipart.MultipartFile;
import com.restaurantes.restaurantesaplicacion.dto.ProfileDTO;
import com.restaurantes.restaurantesaplicacion.mapper.ValoracionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import java.util.ArrayList;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory;
import java.time.LocalDateTime;
import com.restaurantes.restaurantesaplicacion.dto.PasswordChangeDTO;
import org.springframework.security.authentication.BadCredentialsException;
import com.restaurantes.restaurantesaplicacion.dto.UserProfileDTO;


@Service
public class UserServiceImpl implements UserService, UserDetailsService {

        private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

        @Autowired
        private UsuarioRepository usuarioRepository;

        @Autowired
        private UsuarioMapper usuarioMapper;

        @Autowired
        @Lazy
        private PasswordEncoder passwordEncoder;

        @Autowired
        private EmailService emailService;

        @Autowired
        private ValoracionMapper valoracionMapper;

        @Autowired
        private FileStorageService fileStorageService;

        @Autowired
        private RelacionUsuarioRepository relacionRepository;

    @Override
    @Cacheable("usuarios")
    public List<UsuarioResponseDTO> obtenerTodosLosUsuarios() {
            log.info("--- OBTENIENDO TODOS LOS USUARIOS  DESDE LA BD ---");
            return usuarioRepository.findAll()
                    .stream()
                    .map(usuarioMapper::toUsuarioResponseDto) 
                    .collect(Collectors.toList());
        }

    @Override  
    @Cacheable(value = "restaurantes", key = "#id")
    public UsuarioResponseDTO obtenerUsuarioPorId(Long id) {
            log.info("--- BUSCANDO USUARIO CON ID {} DESDE LA BD ---", id);
            Usuario usuario = usuarioRepository.findById(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
            return usuarioMapper.toUsuarioResponseDto(usuario); 
        }


    

    @Override
    @Transactional 
    @CacheEvict(value = "usuarios", allEntries = true)
    public UsuarioResponseDTO crearUsuario(UsuarioRegistroDTO registroDTO) {
        log.info("Iniciando proceso de creación/reactivación para el email: {}", registroDTO.getEmail());

        Optional<Usuario> usuarioExistente = usuarioRepository.findByEmail(registroDTO.getEmail());

        if (usuarioExistente.isPresent()) {
            Usuario usuario = usuarioExistente.get();
        
            if (usuario.getFechaBaja() != null) {
                log.info("Reactivando cuenta para el email: {}", registroDTO.getEmail());

                usuario.setNombreUsuario(registroDTO.getNombreUsuario());
                usuario.setPwd(passwordEncoder.encode(registroDTO.getPwd()));
                
            
                usuario.setFechaBaja(null);
                
                Usuario usuarioReactivado = usuarioRepository.save(usuario);
                return usuarioMapper.toUsuarioResponseDto(usuarioReactivado);
            } else {

                log.warn("Intento de registro con un email ya en uso y activo: {}", registroDTO.getEmail());
                throw new ConflictException("El email ya está registrado y la cuenta está activa.");
            }
        } else {

            log.info("Creando nuevo usuario con email: {}", registroDTO.getEmail());
            Usuario nuevoUsuario = new Usuario();
            nuevoUsuario.setNombreUsuario(registroDTO.getNombreUsuario());
            nuevoUsuario.setEmail(registroDTO.getEmail());
            String passwordHasheada = passwordEncoder.encode(registroDTO.getPwd());
            nuevoUsuario.setPwd(passwordHasheada);
            nuevoUsuario.setVerificado(false);
            String token = UUID.randomUUID().toString();
            nuevoUsuario.setTokenVerificacion(token);
            Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);
            
            try {
            emailService.sendVerificationEmail(usuarioGuardado.getEmail(), token);
            log.info("Email de verificación enviado a {}", usuarioGuardado.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar email de verificación a {}: {}", usuarioGuardado.getEmail(), e.getMessage());
        }
            log.info("Usuario creado", registroDTO.getEmail());
            return usuarioMapper.toUsuarioResponseDto(usuarioGuardado);
        }

    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Intentando cargar usuario por email para autenticación: {}", email);
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Intento de login fallido .Usuario no encontrado: " + email));
                    log.info("usuario: {}", usuario.getNombreUsuario());
        
            if (usuario.getFechaBaja() != null) {
                log.warn("Intento de login de una cuenta inactiva: {}", email);
                throw new DisabledException("La cuenta de usuario ha sido dada de baja.");
            }
            if (!usuario.isVerificado()) {
                log.warn("Intento de login de una cuenta no verificada: {}", email);
                throw new LockedException("La cuenta no ha sido verificada. Por favor, revisa tu email.");
            }

            return new User(usuario.getEmail(), usuario.getPwd(), new ArrayList<>());
        }


    @Override
    public void generatePasswordResetToken(String email) {
        log.info("Iniciando generación de token de reseteo para email: {}", email);
        log.debug("Buscando usuario en la BD con email: {}", email);
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("No se encontró un usuario con ese email."));

            String token = UUID.randomUUID().toString();
            usuario.setResetToken(token);
            usuario.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); 

            usuarioRepository.save(usuario);
            log.info("Token generado para {}. Enviando email.", email);

            emailService.sendPasswordResetEmail(usuario.getEmail(), token);
        }

    @Override
    public void resetPassword(String token, String newPassword) {
        log.info("Iniciando restablecimiento de contraseña con token.");
        log.debug("Buscando usuario por token de reseteo.");
            Usuario usuario = usuarioRepository.findByResetToken(token) 
                    .orElseThrow(() -> new ConflictException("Token inválido o no encontrado."));

            if (usuario.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
                log.warn("Intento de restablecer contraseña con token expirado para usuario: {}", usuario.getEmail());
                throw new ConflictException("El token ha expirado.");
            }

            usuario.setPwd(passwordEncoder.encode(newPassword));
            usuario.setResetToken(null);
            usuario.setResetTokenExpiry(null);

            usuarioRepository.save(usuario);
        }

    @Override
    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public void darDeBajaMiCuenta() {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            log.info("Iniciando proceso de baja para el usuario: {}", email);
            log.debug("Buscando usuario en la BD para dar de baja: {}", email);
            Usuario usuario = usuarioRepository.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("No se encontró el usuario con email: " + email));

            
            usuario.setFechaBaja(LocalDateTime.now());

            usuarioRepository.save(usuario); 

            log.info("Cuenta para {} dada de baja con éxito.", email);
        }

    @Override
    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public UsuarioResponseDTO actualizarNombreUsuario(String emailActual, String nuevoNombre) {
        log.info("Actualizando nombre de usuario para: {}", emailActual);
       
        if (usuarioRepository.findByNombreUsuario(nuevoNombre).isPresent()) {
          
            log.warn("Intento de actualizar a un nombre de usuario que ya existe: {}", nuevoNombre);
            throw new ConflictException("El nuevo nombre de usuario ya está en uso.");
        }

   
        log.debug("Buscando usuario actual por email: {}", emailActual);
        Usuario usuario = usuarioRepository.findByEmail(emailActual)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        usuario.setNombreUsuario(nuevoNombre);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        log.info("Nombre de usuario actualizado a: {}", nuevoNombre);
        return usuarioMapper.toUsuarioResponseDto(usuarioActualizado);
    }

    @Override
    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public UsuarioResponseDTO actualizarEmail(String emailActual, String nuevoEmail) {
        log.info("Iniciando actualización de email para: {}", emailActual);


        if (!emailActual.equalsIgnoreCase(nuevoEmail) && usuarioRepository.findByEmail(nuevoEmail).isPresent()) {
            log.warn("Intento de actualizar a un email que ya existe: {}", nuevoEmail);
            throw new RuntimeException("El nuevo email ya está en uso por otra cuenta.");
        }

        Usuario usuario = usuarioRepository.findByEmail(emailActual)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        usuario.setEmail(nuevoEmail);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        log.info("Email actualizado con éxito a: {}", nuevoEmail);
        return usuarioMapper.toUsuarioResponseDto(usuarioActualizado);
    }


    @Override
    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true) 
    public UsuarioResponseDTO actualizarFotoPerfil(String email, MultipartFile file) {
        log.info("Iniciando actualización de foto de perfil para: {}", email);

        // 1. Validar el archivo
        if (file == null || file.isEmpty()) {
            log.warn("Intento de subir foto de perfil sin archivo.");
            throw new BadRequestException("El archivo de imagen no puede estar vacío.");
        }

        // 2. Subir el archivo al servicio (Cloudinary)
        String fileUrl = fileStorageService.store(file);
        log.info("Foto subida al almacenamiento externo: {}", fileUrl);

        // 3. Buscar al usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con email: " + email));

        // 4. Actualizar la URL en la entidad y guardar
        usuario.setFotoPerfilUrl(fileUrl);
        Usuario usuarioActualizado = usuarioRepository.save(usuario);

        log.info("URL de foto de perfil actualizada en la base de datos.");

        // 5. Devolver el DTO actualizado
        return usuarioMapper.toUsuarioResponseDto(usuarioActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfileDTO getFullProfile(String email) {
        log.info("Construyendo perfil completo para: {}", email);
    
    // 1. Busca el usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    
        ProfileDTO profile = new ProfileDTO();
        profile.setNombreUsuario(usuario.getNombreUsuario());
        profile.setFotoPerfilUrl(usuario.getFotoPerfilUrl());

    // 2. Calcula las estadísticas DENTRO de la transacción
        int seguidosCount = (int) usuario.getSiguiendo().stream()
            .filter(r -> r.getEstado() == EstadoSolicitud.ACEPTADO).count();
        int seguidoresCount = (int) usuario.getSeguidores().stream()
            .filter(r -> r.getEstado() == EstadoSolicitud.ACEPTADO).count();
        int solicitudesPendientesCount = (int) usuario.getSeguidores().stream()
            .filter(r -> r.getEstado() == EstadoSolicitud.PENDIENTE).count();
    
        profile.setSeguidosCount(seguidosCount);
        profile.setSeguidoresCount(seguidoresCount);
        profile.setSolicitudesPendientesCount(solicitudesPendientesCount);

    // 3. Mapea las valoraciones (con sus fotos) DENTRO de la transacción
        List<ValoracionDTO> valoraciones = usuario.getValoraciones().stream()
            .map(valoracion -> {
               
                ValoracionDTO dto = valoracionMapper.toDto(valoracion);
            
                List<String> urls = valoracion.getFotos().stream()
                                      .map(Foto::getUrl)
                                      .collect(Collectors.toList());
                dto.setFotos(urls);
                return dto;
            })
            .collect(Collectors.toList());
            
        profile.setValoraciones(valoraciones);

    return profile;
    }

    @Override
        @Transactional
    public void verificarUsuario(String token) {
        log.info("Intentando verificar usuario con token: {}", token);
        Usuario usuario = usuarioRepository.findByTokenVerificacion(token)
            .orElseThrow(() -> new BadRequestException("El token de verificación es inválido o ha expirado."));

        usuario.setVerificado(true);
        usuario.setTokenVerificacion(null); 
        usuarioRepository.save(usuario);

        log.info("Usuario {} verificado con éxito.", usuario.getEmail());

    
        try {
            emailService.sendWelcomeEmail(usuario.getEmail(), usuario.getNombreUsuario());
        } catch (Exception e) {
            log.error("Error al enviar email de bienvenida (post-verificación) a {}: {}", usuario.getEmail(), e.getMessage());
        }
    }



    @Override
    @Transactional
    @CacheEvict(value = "usuarios", allEntries = true)
    public void changePassword(String email, PasswordChangeDTO dto) {
        log.info("Iniciando cambio de contraseña para: {}", email);

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        // 1. Verificar la contraseña antigua
        if (!passwordEncoder.matches(dto.getOldPassword(), usuario.getPwd())) {
            log.warn("Intento fallido de cambio de contraseña: contraseña antigua incorrecta para {}", email);
            throw new BadCredentialsException("La contraseña antigua es incorrecta.");
        }

        // 2. Hashear y guardar la nueva contraseña
        usuario.setPwd(passwordEncoder.encode(dto.getNewPassword()));
        usuarioRepository.save(usuario);

        log.info("Contraseña actualizada con éxito para: {}", email);
    }



    @Override
    public List<UsuarioSimpleDTO> buscarUsuarios(String query) {
        log.debug("Buscando usuarios que contengan: {}", query);

        
        String emailUsuarioActual = SecurityContextHolder.getContext().getAuthentication().getName();

        List<Usuario> usuariosEncontrados = usuarioRepository.findByNombreUsuarioStartingWithIgnoreCase(query);

      
        return usuariosEncontrados.stream()
                .filter(usuario -> !usuario.getEmail().equals(emailUsuarioActual))
                .map(usuarioMapper::toSimpleDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional(readOnly = true)
    public UserProfileDTO verPerfilUsuario(String emailViewer, String nombrePerfil) {
        log.info("Usuario '{}' solicita ver el perfil de '{}'", emailViewer, nombrePerfil);

        Usuario viewer = usuarioRepository.findByEmail(emailViewer)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario (viewer) no encontrado"));
        Usuario profileUser = usuarioRepository.findByNombreUsuario(nombrePerfil)
                .orElseThrow(() -> new ResourceNotFoundException("Perfil de usuario no encontrado"));

        UserProfileDTO dto = new UserProfileDTO();
        dto.setNombreUsuario(profileUser.getNombreUsuario());
        dto.setFotoPerfilUrl(profileUser.getFotoPerfilUrl());
        dto.setPrivate(profileUser.isPrivate());

        // 1. Determinar el estado de la relación
        RelacionUsuarioId relacionId = new RelacionUsuarioId(viewer.getId(), profileUser.getId());
        Optional<RelacionUsuario> relacion = relacionRepository.findById(relacionId);

        if (relacion.isPresent()) {
            dto.setRelationshipStatus(relacion.get().getEstado());
        } else {
            dto.setRelationshipStatus(null);
        }

        // 2. Si el perfil NO es privado, o si ya son amigos, mostrar los detalles
        boolean puedeVerDetalles = !profileUser.isPrivate() || 
                                   (relacion.isPresent() && relacion.get().getEstado() == EstadoSolicitud.ACEPTADO);

        if (puedeVerDetalles) {
            dto.setSeguidosCount((int) profileUser.getSiguiendo().stream()
                    .filter(r -> r.getEstado() == EstadoSolicitud.ACEPTADO).count());
            dto.setSeguidoresCount((int) profileUser.getSeguidores().stream()
                    .filter(r -> r.getEstado() == EstadoSolicitud.ACEPTADO).count());

            List<ValoracionDTO> valoraciones = profileUser.getValoraciones().stream()
                .map(valoracion -> {
                    ValoracionDTO dtoVal = valoracionMapper.toDto(valoracion);
                    List<String> urls = valoracion.getFotos().stream()
                                          .map(Foto::getUrl)
                                          .collect(Collectors.toList());
                    dtoVal.setFotos(urls);
                    return dtoVal;
                })
                .collect(Collectors.toList());
            
            dto.setValoraciones(valoraciones);
        }

        return dto;
    }

    }