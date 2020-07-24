package com.cyecize.broccolina.services;

import com.cyecize.solet.HttpSolet;
import com.cyecize.solet.HttpSoletRequest;

import java.util.List;
import java.util.Map;

public interface SoletCandidateFinder {

    void init(Map<String, HttpSolet> soletMap, List<String> applicationNames);

    HttpSolet findSoletCandidate(HttpSoletRequest request);
}
