package com.cyecize.solet;

public interface HttpSolet {

    void init(SoletConfig soletConfig);

    void service(HttpSoletRequest request, HttpSoletResponse response) throws Exception;

    boolean isInitialized();

    boolean hasIntercepted();

    SoletConfig getSoletConfig();
}
