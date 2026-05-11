package produkcja.meble.app.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;


@Entity
@Table(name="product")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class Product {

    @Id
    @GeneratedValue
    private Long id;

    private BigDecimal price;

    private double length;

    private double depth;

    private double height;

    private String colour;

    private String model;

    private String type;

    private String material;

    private String fabric;

    private String remarks;

    private String equipment;
}
    