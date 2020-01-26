package com.cyecize.summer.areas.routing.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;

public interface RequestProcessor {
    boolean processRequest(HttpSoletRequest request, HttpSoletResponse response);
}
