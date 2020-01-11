package com.cyecize.summer.common.annotations;

import com.cyecize.solet.SoletConfig;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface Configuration {

    /**
     * Provide the name of the configuration that you want to receive.
     * Valid configuration names are all user provided configurations as well as values from {@link SoletConfig}.
     */
    String value();
}
