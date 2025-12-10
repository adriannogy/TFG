package com.restaurantes.restaurantesaplicacion.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "fotos")
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(exclude = "valoracion")
public class Foto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String url;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumns({
        @JoinColumn(name = "valoracion_usuario_id", referencedColumnName = "usuario_id"),
        @JoinColumn(name = "valoracion_restaurante_id", referencedColumnName = "restaurante_id")
    })
    @ToString.Exclude
    private Valoracion valoracion;
}