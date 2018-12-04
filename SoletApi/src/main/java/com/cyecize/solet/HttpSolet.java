package com.cyecize.solet;

public interface HttpSolet {

    void init(SoletConfig soletConfig);

    void service(HttpSoletRequest request, HttpSoletResponse response) throws Exception;

    void setAssetsFolder(String dir);

    void setAppNamePrefix(String appName);

    boolean isInitialized();

    boolean hasIntercepted();

    SoletConfig getSoletConfig();
}
