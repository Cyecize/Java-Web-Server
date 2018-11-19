package com.cyecize.summer.common.annotations;

import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Controller {
    ServiceLifeSpan lifeSpan() default ServiceLifeSpan.SINGLETON;
}
