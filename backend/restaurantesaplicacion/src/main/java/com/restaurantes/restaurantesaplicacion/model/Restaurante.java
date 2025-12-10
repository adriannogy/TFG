package com.restaurantes.restaurantesaplicacion.model;

import jakarta.persistence.*;

import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "restaurantes")
@Getter
@Setter
@ToString
@NoArgsConstructor
@EqualsAndHashCode(exclude = {"valoraciones", "favoritos"}) 
public class Restaurante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;
    private String direccion;
    private String ciudad;

    @Column(name = "tipo_cocina")
    private String tipoCocina;

    @Column(unique = true, name = "osm_id") 
    private Long osmId; 

    private Double lat;
    private Double lon;

    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Valoracion> valoraciones = new HashSet<>();

    @OneToMany(mappedBy = "restaurante", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Favorito> favoritos = new HashSet<>();
    
}
