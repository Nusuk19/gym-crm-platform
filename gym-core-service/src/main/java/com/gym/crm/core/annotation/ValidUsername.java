package com.gym.crm.core.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.OverridesAttribute;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Constraint(validatedBy = {})
@Pattern(regexp = "^[a-zA-Z]+\\.[a-zA-Z]+(\\d+)?$")
@Target({FIELD, PARAMETER})
@Retention(RUNTIME)
public @interface ValidUsername {

    @OverridesAttribute(constraint = Pattern.class, name = "message")
    String message() default "must match username pattern (e.g. john.doe or john.doe1)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}