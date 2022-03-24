package com.cyecize.summer.areas.startup.util;

import com.cyecize.http.HttpRequestImpl;
import com.cyecize.http.HttpResponseImpl;
import com.cyecize.http.HttpSession;
import com.cyecize.http.HttpSessionImpl;
import com.cyecize.ioc.annotations.Bean;
import com.cyecize.ioc.annotations.Scope;
import com.cyecize.ioc.enums.ScopeType;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletRequestImpl;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.solet.HttpSoletResponseImpl;
import com.cyecize.summer.common.annotations.BeanConfig;

/**
 * This is required because Summer components rely on {@link HttpSoletRequest}
 * and {@link HttpSoletResponse} instances.
 * These instances will be replaced on each request.
 */
@BeanConfig
public class SoletRequestAndResponseBean {

    @Bean
    @Scope(ScopeType.PROXY)
    public HttpSoletRequest request() {
        return new HttpSoletRequestImpl(new HttpRequestImpl());
    }

    @Bean
    @Scope(ScopeType.PROXY)
    public HttpSoletResponse response() {
        return new HttpSoletResponseImpl(new HttpResponseImpl());
    }

    @Bean
    @Scope(ScopeType.PROXY)
    public HttpSession session() {
        return new HttpSessionImpl();
    }
}
