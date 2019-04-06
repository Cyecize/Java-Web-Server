package com.cyecize.summer.areas.validation.interfaces;

import com.cyecize.solet.HttpSoletRequest;

public interface DataAdapter<T> {

    T resolve(String paramName, HttpSoletRequest request);
}
