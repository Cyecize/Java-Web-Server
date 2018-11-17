package com.cyecize.solet;

public interface HttpSolet {

    void init(SoletConfig soletConfig);

    void service(HttpSoletRequest request, HttpSoletResponse response) throws Exception;

    void setAppNamePrefix(String appName);

    boolean isInitialized();

    SoletConfig getSoletConfig();
}
