package com.Project.GestionBanque.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = Utf8BytesValidator.class)
public @interface Utf8Bytes {
  String message() default "must contain at most {max} UTF-8 bytes";

  int max();

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
