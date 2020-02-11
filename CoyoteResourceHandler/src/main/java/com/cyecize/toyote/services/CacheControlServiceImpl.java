package com.cyecize.toyote.services;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpResponse;
import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.toyote.ToyoteConstants;
import com.cyecize.toyote.utils.CachingExpressingParser;

import java.util.Map;

@Service
public class CacheControlServiceImpl implements CacheControlService {

    private final JavacheConfigService configService;

    private Map<String, String> mediaTypeCacheMap;

    @Autowired
    public CacheControlServiceImpl(JavacheConfigService configService) {
        this.configService = configService;
    }

    @Override
    public void init() {
        this.mediaTypeCacheMap = CachingExpressingParser.parseExpression(this.configService.getConfigParamString(
                JavacheConfigValue.RESOURCE_CACHING_EXPRESSION
        ));
    }

    @Override
    public void addCachingHeader(HttpRequest request, HttpResponse response, String fileMediaType) {
        if (!this.isCachingEnabled() || this.hasCacheHeader(response) || !this.mediaTypeCacheMap.containsKey(fileMediaType)) {
            return;
        }

        String responseCacheControl = request.getHeader(ToyoteConstants.CACHE_CONTROL_HEADER_NAME);
        if (responseCacheControl == null) {
            responseCacheControl = this.mediaTypeCacheMap.get(fileMediaType);
        }

        response.addHeader(ToyoteConstants.CACHE_CONTROL_HEADER_NAME, responseCacheControl);
    }

    private boolean isCachingEnabled() {
        return this.configService.getConfigParam(JavacheConfigValue.ENABLE_RESOURCE_CACHING, boolean.class);
    }

    private boolean hasCacheHeader(HttpResponse response) {
        return response.getHeaders().containsKey(ToyoteConstants.CACHE_CONTROL_HEADER_NAME);
    }
}
