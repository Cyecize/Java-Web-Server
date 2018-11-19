package com.cyecize.summer;

import com.cyecize.solet.BaseHttpSolet;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.solet.WebSolet;

@WebSolet("/*")
public class DispatcherSolet extends BaseHttpSolet {

    @Override
    protected void doGet(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        response.setContent("Hello, this is default get!");
    }

    @Override
    protected void doPost(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        response.setContent("Hello, this is default post!");
    }
}
