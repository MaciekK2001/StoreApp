package produkcja.meble.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "product")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    private double length;

    private double depth;

    private double height;

    private String colour;

    @Column(nullable = false)
    private String model;

    @Enumerated(EnumType.STRING)
    private FurnitureType type;

    private String material;

    private String fabric;

    private String remarks;

    private String equipment;

    private String imageUrl;
}
