package com.cyecize.javache.api;

import com.cyecize.ioc.annotations.AliasFor;
import com.cyecize.ioc.annotations.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation intended to replace {@link Service} for classes that are not going to be used from
 * Javache Embedded and only from the dedicated web server.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@AliasFor(Service.class)
public @interface JavacheComponent {

}
