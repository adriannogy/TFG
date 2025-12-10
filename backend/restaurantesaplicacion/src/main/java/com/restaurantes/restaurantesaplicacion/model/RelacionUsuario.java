package com.restaurantes.restaurantesaplicacion.model;

import jakarta.persistence.*;

import lombok.*;
@Entity
@Table(name = "relaciones_usuarios")
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"seguidor", "seguido"}) 
public class RelacionUsuario {

    @EmbeddedId
    private RelacionUsuarioId id;

    @ManyToOne
    @MapsId("seguidorId")
    @JoinColumn(name = "seguidor_id")
    private Usuario seguidor;

    @ManyToOne
    @MapsId("seguidoId")
    @JoinColumn(name = "seguido_id")
    private Usuario seguido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoSolicitud estado;
}