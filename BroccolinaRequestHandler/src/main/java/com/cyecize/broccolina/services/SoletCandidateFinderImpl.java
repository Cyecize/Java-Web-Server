package com.cyecize.broccolina.services;

import com.cyecize.ioc.annotations.Autowired;
import com.cyecize.ioc.annotations.Service;
import com.cyecize.javache.JavacheConfigValue;
import com.cyecize.javache.services.JavacheConfigService;
import com.cyecize.solet.HttpSolet;
import com.cyecize.solet.HttpSoletRequest;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SoletCandidateFinderImpl implements SoletCandidateFinder {

    private final String rootAppName;

    private Map<String, HttpSolet> soletMap;

    private List<String> applicationNames;

    @Autowired
    public SoletCandidateFinderImpl(JavacheConfigService configService) {
        this.rootAppName = configService.getConfigParamString(JavacheConfigValue.MAIN_APP_JAR_NAME);
    }

    @Override
    public void init(Map<String, HttpSolet> soletMap, List<String> applicationNames) {
        this.soletMap = soletMap;
        this.applicationNames = applicationNames;
    }

    @Override
    public HttpSolet findSoletCandidate(HttpSoletRequest request) {
        request.setContextPath(this.resolveCurrentRequestAppName(request));

        final String requestUrl = request.getRequestURL();
        final Pattern applicationRouteMatchPattern = Pattern
                .compile(Pattern.quote(request.getContextPath() + "\\/[a-zA-Z0-9]+\\/"));

        final Matcher applicationRouteMatcher = applicationRouteMatchPattern.matcher(requestUrl);

        if (this.soletMap.containsKey(requestUrl)) {
            return this.soletMap.get(requestUrl);
        }

        if (applicationRouteMatcher.find()) {
            String applicationRoute = applicationRouteMatcher.group(0) + "*";
            if (this.soletMap.containsKey(applicationRoute)) {
                return this.soletMap.get(applicationRoute);
            }
        }

        if (this.soletMap.containsKey(request.getContextPath() + "/*")) {
            return this.soletMap.get(request.getContextPath() + "/*");
        }

        return null;
    }

    private String resolveCurrentRequestAppName(HttpSoletRequest request) {
        for (String applicationName : this.applicationNames) {
            if (request.getRequestURL().startsWith(applicationName) && !applicationName.equals(this.rootAppName)) {
                return applicationName;
            }
        }

        return "";
    }
}
