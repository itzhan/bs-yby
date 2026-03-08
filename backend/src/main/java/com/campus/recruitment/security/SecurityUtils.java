package com.campus.recruitment.security;

import com.campus.recruitment.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static LoginUser getLoginUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof LoginUser) {
            return (LoginUser) auth.getPrincipal();
        }
        return null;
    }

    public static Long getCurrentUserId() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUser().getId() : null;
    }

    public static String getCurrentRole() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUser().getRole() : null;
    }

    public static User getCurrentUser() {
        LoginUser loginUser = getLoginUser();
        return loginUser != null ? loginUser.getUser() : null;
    }
}
