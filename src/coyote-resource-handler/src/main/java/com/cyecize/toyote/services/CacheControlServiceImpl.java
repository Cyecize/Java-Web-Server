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

    /**
     * Adds caching header to the given response.
     * <p>
     * If caching is not enabled or the caching header is already present, do nothing.
     * <p>
     * Uses Cache-Control header to set up caching options.
     * Cache-Control header value from the request is prioritized if present,
     * otherwise value from the config for that specific media type will be used (if present)
     *
     * @param request       - current request.
     * @param response      - current response.
     * @param fileMediaType - current file media type.
     */
    @Override
    public void addCachingHeader(HttpRequest request, HttpResponse response, String fileMediaType) {
        if (!this.isCachingEnabled() || this.hasCacheHeader(response)) {
            return;
        }

        String responseCacheControl = request.getHeader(ToyoteConstants.CACHE_CONTROL_HEADER_NAME);
        if (responseCacheControl == null && this.mediaTypeCacheMap.containsKey(fileMediaType)) {
            responseCacheControl = this.mediaTypeCacheMap.get(fileMediaType);
        }

        if (responseCacheControl != null) {
            response.addHeader(ToyoteConstants.CACHE_CONTROL_HEADER_NAME, responseCacheControl);
        }
    }

    private boolean isCachingEnabled() {
        return this.configService.getConfigParam(JavacheConfigValue.ENABLE_RESOURCE_CACHING, boolean.class);
    }

    private boolean hasCacheHeader(HttpResponse response) {
        return response.getHeaders().containsKey(ToyoteConstants.CACHE_CONTROL_HEADER_NAME);
    }
}
