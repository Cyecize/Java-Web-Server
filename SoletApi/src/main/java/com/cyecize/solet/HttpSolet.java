package com.cyecize.solet;

import java.io.IOException;

public interface HttpSolet {
    void init(SoletConfig soletConfig);

    void service(HttpSoletRequest request, HttpSoletResponse response) throws IOException;

    boolean isInitialized();

    SoletConfig getSoletConfig();
}
