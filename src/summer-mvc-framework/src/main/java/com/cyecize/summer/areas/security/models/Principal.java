package com.cyecize.summer.areas.security.models;

import com.cyecize.http.HttpSession;
import com.cyecize.summer.areas.security.interfaces.GrantedAuthority;
import com.cyecize.summer.areas.security.interfaces.UserDetails;
import com.cyecize.summer.common.annotations.Autowired;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.constants.SecurityConstants;

import java.util.ArrayList;
import java.util.Collection;

@Component(lifespan = ServiceLifeSpan.REQUEST)
public class Principal {

    private UserDetails user;

    public Principal() {

    }

    @Autowired
    public Principal(HttpSession session) {
        this.user = (UserDetails) session.getAttribute(SecurityConstants.SESSION_USER_DETAILS_KEY);
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
