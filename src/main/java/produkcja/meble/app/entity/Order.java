package produkcja.meble.app.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private UserAccount createdBy;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
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

    @Builder.Default
    @Column(name = "assembly_price", precision = 12, scale = 2)
    private BigDecimal assemblyPrice = BigDecimal.ZERO;

    @Column(name = "assembly_remarks")
    private String assemblyRemarks;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "discount_remarks")
    private String discountRemarks;

    @Builder.Default
    @Column(name = "transport_price", precision = 12, scale = 2)
    private BigDecimal transportPrice = BigDecimal.ZERO;

    @Column(name = "transport_remarks")
    private String transportRemarks;

    private String remarks;

    @Builder.Default
    @Column(name = "carry_products_price", precision = 12, scale = 2)
    private BigDecimal carryProductsPrice = BigDecimal.ZERO;

    @Column(name = "carry_products_remarks")
    private String carryProductsRemarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 32)
    @Builder.Default
    private OrderStatus status = OrderStatus.ACTIVE;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal deposit = BigDecimal.ZERO;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    @PostLoad
    private void initDefaults() {
        if (this.status == null) {
            this.status = this.active ? OrderStatus.ACTIVE : OrderStatus.COMPLETED;
        } else {
            this.active = (this.status == OrderStatus.ACTIVE);
        }
    }

    @PrePersist
    @PreUpdate
    private void syncStatusAndActive() {
        if (this.status == null) {
            this.status = this.active ? OrderStatus.ACTIVE : OrderStatus.COMPLETED;
        }
        this.active = (this.status == OrderStatus.ACTIVE);
    }

    public OrderStatus getStatus() {
        if (status == null) {
            return active ? OrderStatus.ACTIVE : OrderStatus.COMPLETED;
        }
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = (status != null) ? status : OrderStatus.ACTIVE;
        this.active = (this.status == OrderStatus.ACTIVE);
    }

    public void setActive(boolean active) {
        this.active = active;
        if (!active && (this.status == null || this.status == OrderStatus.ACTIVE)) {
            this.status = OrderStatus.COMPLETED;
        } else if (active) {
            this.status = OrderStatus.ACTIVE;
        }
    }
}
