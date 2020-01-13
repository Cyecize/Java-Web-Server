package com.cyecize.summer.areas.startup.util;

import com.cyecize.http.HttpRequestImpl;
import com.cyecize.http.HttpResponseImpl;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletRequestImpl;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.solet.HttpSoletResponseImpl;
import com.cyecize.summer.common.annotations.Bean;
import com.cyecize.summer.common.annotations.BeanConfig;

/**
 * This is required because Summer components rely on {@link HttpSoletRequest}
 * and {@link HttpSoletResponse} instances.
 * These instances will be replaced on each request.
 */
@BeanConfig
public class SoletRequestAndResponseBean {

    @Bean
    public HttpSoletRequestImpl request() {
        return new HttpSoletRequestImpl(new HttpRequestImpl());
    }

    @Bean
    public HttpSoletResponseImpl response() {
        return new HttpSoletResponseImpl(new HttpResponseImpl());
    }
}
