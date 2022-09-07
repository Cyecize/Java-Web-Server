package com.cyecize.solet;

import com.cyecize.http.HttpResponse;

public interface HttpSoletResponse extends HttpResponse {

    void sendRedirect(String location);

    SoletOutputStream getOutputStream();
}
