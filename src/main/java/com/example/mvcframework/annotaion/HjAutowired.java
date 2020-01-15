package com.example.mvcframework.annotaion;

import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HjAutowired {
    String value() default "";
}
