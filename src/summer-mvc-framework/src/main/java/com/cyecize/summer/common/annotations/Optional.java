package com.cyecize.summer.common.annotations;

import com.cyecize.ioc.annotations.AliasFor;
import com.cyecize.ioc.annotations.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
@AliasFor(Nullable.class)
public @interface Optional {

}
