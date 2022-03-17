package com.cyecize.summer.areas.validation.annotations;

import com.cyecize.summer.areas.validation.interfaces.DataAdapter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER})
public @interface ConvertedBy {

    Class<? extends DataAdapter<?>> value();
}
