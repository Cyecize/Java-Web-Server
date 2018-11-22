package com.cyecize.summer.areas.security.interfaces;

import java.util.Collection;

public interface UserDetails {

    String getUsername();

    String getPassword();

    Collection<GrantedAuthority> getAuthorities();
}
