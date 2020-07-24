package com.cyecize.toyote.services;

import com.cyecize.toyote.exceptions.ResourceNotFoundException;

import java.io.File;

public interface ResourceLocationService {
    File locateResource(String requestURL) throws ResourceNotFoundException;
}
