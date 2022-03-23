package com.cyecize.summer.areas.validation.objectmapper;

import com.cyecize.summer.common.annotations.Bean;
import com.cyecize.summer.common.annotations.BeanConfig;
import com.fasterxml.jackson.databind.ObjectMapper;

@BeanConfig
public class ObjectMapperBean {
    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
