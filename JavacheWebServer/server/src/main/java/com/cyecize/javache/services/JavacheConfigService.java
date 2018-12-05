package com.cyecize.javache.services;

import java.util.List;
import java.util.Map;

public interface JavacheConfigService {

    <T> T getConfigParam(String paramName, Class<T> type);

    List<String> getRequestHandlerPriority();

    Map<String, Object> getConfigParams();
}
