package com.cyecize.summer.common.annotations;

import com.cyecize.ioc.annotations.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.FIELD})
@AliasFor(com.cyecize.ioc.annotations.Autowired.class)
public @interface Autowired {

}
