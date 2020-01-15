package com.example.mvcframework.annotaion;

import java.lang.annotation.*;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HjRequestMapping {
    String value() default "";
}
