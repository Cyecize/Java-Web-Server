package com.cyecize.summer.areas.startup.callbacks;

import com.cyecize.ioc.enums.ScopeType;
import com.cyecize.ioc.events.ServiceDetailsCreated;
import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.lang.annotation.Annotation;

public class ComponentScopeHandler implements ServiceDetailsCreated {

    private static final String LIFESPAN_METHOD_NAME = "lifespan";

    @Override
    public void serviceDetailsCreated(ServiceDetails serviceDetails) {
        if (serviceDetails.getAnnotation() == null || !this.isLifespanMethodPresent(serviceDetails.getAnnotation())) {
            return;
        }

        final ServiceLifeSpan lifeSpan = this.getAnnotationValue(serviceDetails.getAnnotation());
        serviceDetails.setScopeType(lifeSpan.getScopeType());
    }

    private boolean isLifespanMethodPresent(Annotation annotation) {
        try {
            annotation.annotationType().getMethod(LIFESPAN_METHOD_NAME);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    private ServiceLifeSpan getAnnotationValue(Annotation annotation) {
        try {
            return (ServiceLifeSpan) annotation.annotationType().getMethod(LIFESPAN_METHOD_NAME).invoke(annotation);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
