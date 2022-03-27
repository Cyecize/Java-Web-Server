package com.cyecize.summer.areas.startup.util;

import com.cyecize.ioc.annotations.Bean;
import com.cyecize.ioc.annotations.Scope;
import com.cyecize.ioc.enums.ScopeType;
import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConfigImpl;
import com.cyecize.summer.common.annotations.BeanConfig;

@BeanConfig
public class SoletConfigBean {

    @Bean
    @Scope(ScopeType.PROXY)
    public SoletConfig soletConfig() {
        return new SoletConfigImpl();
    }
}
