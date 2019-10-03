package com.cyecize.summer.areas.template.annotations;

import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface TemplateService {

    ServiceLifeSpan lifespan() default ServiceLifeSpan.SINGLETON;

    String serviceNameInTemplate();
}
