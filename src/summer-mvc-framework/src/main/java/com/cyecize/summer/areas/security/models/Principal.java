package com.cyecize.summer.areas.security.models;

import com.cyecize.summer.areas.security.UserDetailsContextHolder;
import com.cyecize.summer.areas.security.interfaces.GrantedAuthority;
import com.cyecize.summer.areas.security.interfaces.UserDetails;
import com.cyecize.summer.common.annotations.Component;

import java.util.ArrayList;
import java.util.Collection;

@Component
public class Principal {

    public Principal() {

    }

    public UserDetails getUser() {
        return UserDetailsContextHolder.getUserDetails();
    }

    public void setUser(UserDetails user) {
        UserDetailsContextHolder.setUserDetails(user);
    }

    public void logout() {
        this.setUser(null);
    }

    public boolean isUserPresent() {
        return this.getUser() != null;
    }

    public boolean hasAuthority(String authority) {
        if (!this.isUserPresent()) {
            return false;
        }

        return this.getUser().getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    public Collection<GrantedAuthority> getAuthorities() {
        return this.isUserPresent() ? this.getUser().getAuthorities() : new ArrayList<>();
    }
}
