package com.cyecize.toyote.services;

import com.cyecize.toyote.exceptions.ResourceNotFoundException;

import java.io.File;
import java.io.FileNotFoundException;

public interface ResourceLocationService {
    File locateResource(String requestURL) throws ResourceNotFoundException, FileNotFoundException;
}
