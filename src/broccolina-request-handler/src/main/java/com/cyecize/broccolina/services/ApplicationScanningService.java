package com.cyecize.broccolina.services;

import com.cyecize.solet.HttpSolet;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public interface ApplicationScanningService {

    List<String> getApplicationNames();

    Map<String, List<Class<HttpSolet>>> findSoletClasses() throws IOException, ClassNotFoundException;
}
