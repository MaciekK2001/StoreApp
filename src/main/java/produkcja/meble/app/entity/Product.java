package produkcja.meble.app.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false, unique = true)
    private String model;

        private String type;

    private String material;

    private String fabric;

    private String remarks;

    private String equipment;

    private String imageUrl;

    @Column(name = "lead_time_weeks")
    private Integer leadTimeWeeks;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "product_additional_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    @OrderColumn(name = "image_order")
    @Builder.Default
    private List<String> additionalImages = new ArrayList<>();
}
