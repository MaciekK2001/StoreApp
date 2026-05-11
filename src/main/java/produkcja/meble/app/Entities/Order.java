package produkcja.meble.app.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

@Entity
@Table(name="orders")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Order {

    @Id
    @GeneratedValue
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "delivery_date")
    private LocalDateTime deliveryDate;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "fulfillment_time")
    private int fulfillmentTime;

    @Column(name = "contact_remarks")
    private String contactRemarks;

    private String address;

    @Column(name = "assembly_price")
    private double assemblyPrice;

    @Column(name = "assembly_remarks")
    private String assemblyRemarks;

    private double discount;

    @Column(name = "discount_remarks")
    private String discountRemarks;

    @Column(name = "transport_price")
    private double transportPrice;

    @Column(name = "transport_remarks")
    private String transportRemarks;

    private String remarks;

    @Column(name = "carry_products_price")
    private double carryProductsPrice;

    @Column(name = "carry_products_remarks")
    private String carryProductsRemarks;

    private boolean isActive = true;

    private double deposit;

    @ToString.Exclude
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }
}
