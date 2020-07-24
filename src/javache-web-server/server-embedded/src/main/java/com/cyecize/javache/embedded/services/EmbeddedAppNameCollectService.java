package com.cyecize.javache.embedded.services;

import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.embedded.internal.JavacheEmbeddedComponent;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.toyote.services.AppNameCollectService;

import java.util.List;

@JavacheEmbeddedComponent
public class EmbeddedAppNameCollectService implements AppNameCollectService {

    private final JavacheConfigService configService;

    @Autowired
    public EmbeddedAppNameCollectService(JavacheConfigService configService) {
        this.configService = configService;
    }

    @Override
    public List<String> getApplicationNames() {
        return List.of(this.configService.getConfigParamString(JavacheConfigValue.MAIN_APP_JAR_NAME));
    }
}
