package com.cyecize.summer.areas.security;

import com.cyecize.summer.areas.security.interfaces.UserDetails;

public final class UserDetailsContextHolder {

    private static final ThreadLocal<UserDetails> userDetailsContainer = new ThreadLocal<>();

    public static UserDetails getUserDetails() {
        return userDetailsContainer.get();
    }

    public static void setUserDetails(UserDetails userDetails) {
        userDetailsContainer.set(userDetails);
    }
}
