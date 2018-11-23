package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.PostConstructException;

import java.util.Collection;

public interface PostConstructInvokingService {
    void invokePostConstructMethod(Collection<Object> instances) throws PostConstructException;
}
