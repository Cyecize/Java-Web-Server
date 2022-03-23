package com.cyecize.summer.areas.validation.interfaces;

import com.cyecize.solet.HttpSoletRequest;
import com.fasterxml.jackson.databind.ObjectMapper;

public interface DataAdapter<T> {

    /**
     * @param paramName - name of request body param or actual value if called from {@link ObjectMapper}
     * @param request   - current http request or null if called from {@link ObjectMapper}
     * @return - converted value.
     */
    T resolve(String paramName, HttpSoletRequest request);
}
