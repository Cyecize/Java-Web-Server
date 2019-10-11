package com.cyecize.toyote.services;

import com.cyecize.toyote.exceptions.ResourceNotFoundException;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface ResourceLocationService {
    InputStream locateResource(String requestURL) throws ResourceNotFoundException, FileNotFoundException;
}
