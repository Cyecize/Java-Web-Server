package com.cyecize.summer.areas.security.models;

import com.cyecize.summer.areas.security.interfaces.GrantedAuthority;
import com.cyecize.summer.areas.security.interfaces.UserDetails;

import java.util.ArrayList;
import java.util.Collection;

public class Principal {

    private UserDetails user;

    public Principal() {

    }

    public Principal(UserDetails user) {
        this.user = user;
    }

    public UserDetails getUser() {
        return user;
    }

    public void setUser(UserDetails user) {
        this.user = user;
    }

    public void logout() {
        this.setUser(null);
    }

    public boolean isUserPresent() {
        return this.user != null;
    }

    public boolean hasAuthority(String authority) {
        if (!this.isUserPresent()) {
            return false;
        }
        return this.user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority));
    }

    public Collection<GrantedAuthority> getAuthorities() {
        return this.isUserPresent() ? this.user.getAuthorities() : new ArrayList<>();
    }
}
