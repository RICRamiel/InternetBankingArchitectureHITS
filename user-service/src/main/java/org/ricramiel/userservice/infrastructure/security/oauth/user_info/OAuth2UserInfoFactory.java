package org.ricramiel.userservice.infrastructure.security.oauth.user_info;

import org.ricramiel.userservice.infrastructure.exceptions.AuthException;
import org.ricramiel.userservice.infrastructure.security.oauth.user_info.providers_impl.GoogleOAuth2UserInfo;

import java.util.Map;

public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId, Map<String, Object> attributes) {
        if(registrationId.equalsIgnoreCase("google")) {
            return new GoogleOAuth2UserInfo(attributes);
        } else {
            throw new AuthException("Login with " + registrationId + " is not supported");
        }
    }
}