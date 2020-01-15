package com.example.mvcframework.annotaion;

import java.lang.annotation.*;

@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HjRequestParam {
    String value() default "";
}
