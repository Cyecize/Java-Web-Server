package com.cyecize.summer.areas.startup.util;

import com.cyecize.ioc.annotations.Bean;
import com.cyecize.ioc.annotations.Scope;
import com.cyecize.ioc.enums.ScopeType;
import com.cyecize.summer.areas.validation.services.ObjectValidationService;
import com.cyecize.summer.areas.validation.services.ObjectValidationServiceImpl;
import com.cyecize.summer.common.annotations.BeanConfig;

@BeanConfig
public class ObjectValidationServiceBean {
    @Bean
    @Scope(ScopeType.PROXY)
    public ObjectValidationService soletConfig() {
        return new ObjectValidationServiceImpl(null);
    }
}
