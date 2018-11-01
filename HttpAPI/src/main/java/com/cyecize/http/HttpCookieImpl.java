package com.cyecize.http;

public class HttpCookieImpl implements HttpCookie {

    private String name;

    private String value;

    private String path;

    public HttpCookieImpl(String name, String value){
        this.setName(name);
        this.setValue(value);
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getValue() {
        return this.value;
    }

    @Override
    public String getPath() {
        return this.path;
    }

    @Override
    public String toRFCString() {
        StringBuilder sb = new StringBuilder();
        sb.append(this.getName()).append("=").append(this.getValue());
        if(this.getPath() != null) {
            sb.append("; path=").append(this.getPath());
        }
        return sb.toString();
    }

    @Override
    public String toString() {
        return this.toRFCString();
    }
}
