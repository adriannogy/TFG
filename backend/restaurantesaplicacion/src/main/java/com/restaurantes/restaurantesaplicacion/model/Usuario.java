package com.restaurantes.restaurantesaplicacion.model;

import jakarta.persistence.*;

import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"valoraciones", "siguiendo", "seguidores", "favoritos"})
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String nombreUsuario;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String pwd; 

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<Valoracion> valoraciones = new HashSet<>();

    @Column(name = "fecha_baja")
    private LocalDateTime fechaBaja;

    @Column(name = "foto_perfil_url")
    private String fotoPerfilUrl;

    @Column(nullable = false)
    private boolean verificado = false;

    @Column(nullable = false)
    private boolean isPrivate = true;

    private String tokenVerificacion;

    @OneToMany(mappedBy = "seguidor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<RelacionUsuario> siguiendo = new HashSet<>();

    @OneToMany(mappedBy = "seguido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private Set<RelacionUsuario> seguidores = new HashSet<>();

    
    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Favorito> favoritos = new HashSet<>();

    private String resetToken;
    private LocalDateTime resetTokenExpiry;
}