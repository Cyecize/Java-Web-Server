package com.cyecize.summer.common.extensions;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.summer.common.models.Model;

public interface InterceptorAdapter {

    default boolean preHandle(HttpSoletRequest request, HttpSoletResponse response, Object handler) throws Exception {
        return true;
    }

    default void postHandle(HttpSoletRequest request, HttpSoletResponse response, Object handler, Model model) throws Exception {

    }
}
