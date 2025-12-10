package com.restaurantes.restaurantesaplicacion.model;

import java.util.HashSet;
import java.util.Set;
import java.time.LocalDateTime;
import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "valoraciones")
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"usuario", "restaurante", "fotos"}) 
public class Valoracion {

    @EmbeddedId 
    private ValoracionId id;

    

    @ManyToOne
    @MapsId("usuarioId")
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne
    @MapsId("restauranteId") 
    @JoinColumn(name = "restaurante_id")
    private Restaurante restaurante;

    @OneToMany(mappedBy = "valoracion", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Foto> fotos = new HashSet<>();

    @Column(nullable = false)
    private int puntuacion;

    @Column(nullable = false, updatable = false)
    private LocalDateTime fechaCreacion;

    private String comentario;
    
}
