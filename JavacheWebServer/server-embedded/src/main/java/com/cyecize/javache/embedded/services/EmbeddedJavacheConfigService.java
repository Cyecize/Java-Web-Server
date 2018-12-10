package com.cyecize.javache.embedded.services;

import com.cyecize.javache.services.JavacheConfigServiceImpl;

import java.util.Map;

public class EmbeddedJavacheConfigService extends JavacheConfigServiceImpl {

    public EmbeddedJavacheConfigService(Map<String, Object> config) {
        super();
        this.setConfig(config);
    }

    /**
     * Leave blank since request handlers are pre-defined.
     */
    @Override
    protected void loadRequestHandlerConfig() {
        //do nothing
    }

    /**
     * Set runtime config if any.
     */
    private void setConfig(Map<String, Object> config) {
        for (Map.Entry<String, Object> entry : config.entrySet()) {
            super.configParameters.put(entry.getKey(), entry.getValue());
        }
    }
}
