package com.retail.customer.rewards.entities;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private Order sampleOrder() {
        return Order.builder()
                .customerId("cust")
                .orderDate(LocalDate.now())
                .amount(BigDecimal.ONE)
                .build();
    }

    @Test
    void equals_sameReference_isTrue() {
        Order o = sampleOrder();
        assertTrue(o.equals(o));
    }

    @Test
    void equals_null_isFalse() {
        Order o = sampleOrder();
        assertFalse(o.equals(null));
    }

    @Test
    void equals_differentType_isFalse() {
        Order o = sampleOrder();
        assertFalse(o.equals(new Object()));
    }

    @Test
    void equals_bothIdsNonNull_equalWhenIdsEqual() {
        Order a = sampleOrder();
        Order b = sampleOrder();

        ReflectionTestUtils.setField(a, "id", 1L);
        ReflectionTestUtils.setField(b, "id", 1L);

        assertTrue(a.equals(b));
        assertTrue(b.equals(a));
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_oneIdNull_isFalse() {
        Order a = sampleOrder();
        Order b = sampleOrder();

        ReflectionTestUtils.setField(a, "id", 2L);
        // b.id remains null

        assertFalse(a.equals(b));
        assertFalse(b.equals(a));
    }

    @Test
    void hashCode_withId_usesIdHash() {
        Order o = sampleOrder();
        ReflectionTestUtils.setField(o, "id", 42L);

        assertEquals(Long.valueOf(42L).hashCode(), o.hashCode());
    }

    @Test
    void hashCode_transient_usesIdentityHash() {
        Order o = sampleOrder();
        // id is null
        int expected = System.identityHashCode(o);
        assertEquals(expected, o.hashCode());
    }
}
