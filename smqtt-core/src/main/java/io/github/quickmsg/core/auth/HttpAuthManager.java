package io.github.quickmsg.core.auth;

import io.github.quickmsg.common.auth.AuthManager;

/**
 * @author luxurong
 */
public class HttpAuthManager implements AuthManager {
    @Override
    public boolean auth(String userName, byte[] passwordInBytes, String clientIdentifier) {
        return false;
    }
}
