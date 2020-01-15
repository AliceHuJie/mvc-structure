package com.example.mvcframework.annotaion;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HjService {
    String value() default "";
}
