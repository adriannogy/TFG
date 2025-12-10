package com.restaurantes.restaurantesaplicacion.model;

import jakarta.persistence.*;

import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "favoritos")
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"usuario", "restaurante"})
public class Favorito {

    @EmbeddedId
    private FavoritoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("restauranteId")
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    @Column(name = "fecha_agregado", nullable = false)
    private LocalDateTime fechaAgregado;

    
    
}