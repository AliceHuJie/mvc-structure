package com.example.mvcframework.annotaion;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HjController {
    String value() default "";
}
