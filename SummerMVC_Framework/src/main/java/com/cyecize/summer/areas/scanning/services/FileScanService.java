package com.cyecize.summer.areas.scanning.services;

import com.cyecize.summer.areas.scanning.exceptions.FileScanException;

import java.util.Set;

public interface FileScanService {
    Set<Class<?>> scanFiles() throws FileScanException;
}
