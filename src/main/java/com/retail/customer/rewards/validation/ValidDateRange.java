package com.retail.customer.rewards.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DateRangeValidator.class)
public @interface ValidDateRange {
    String message() default "startDate must not be after endDate";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
