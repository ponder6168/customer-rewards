package com.retail.customer.rewards.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.Hibernate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Setter(AccessLevel.NONE)
    private Long id;

    @Column(name = "order_id", length = 128)
    private String orderId;

    @Column(name = "customer_id", nullable = false, length = 64)
    private String customerId;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;                        // same reference -> equal
        if (o == null) return false;                       // null -> not equal
        if (Hibernate.getClass(this) != Hibernate.getClass(o)) return false; // handle proxies
        Order other = (Order) o;

        // If either id is null (transient), they are not equal (reference-equality already handled)
        if (this.id == null || other.id == null) {
            return false;
        }

        return Objects.equals(this.id, other.id);         // both ids non-null -> compare ids
    }

    @Override
    public int hashCode() {
        // If id is present use it; otherwise fallback to identity hash so transient objects remain usable in collections
        return (id != null) ? id.hashCode() : System.identityHashCode(this);
    }
}