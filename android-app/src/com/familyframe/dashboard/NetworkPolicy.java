package com.familyframe.dashboard;

import java.net.URI;
import java.net.URISyntaxException;

/** Fixed API endpoints used by the bundled dashboard. */
public final class NetworkPolicy {
    private NetworkPolicy() {}

    public static boolean permits(String value) {
        try {
            URI uri = new URI(value);
            if (!"https".equals(uri.getScheme()) || uri.getUserInfo() != null
                    || uri.getFragment() != null || (uri.getPort() != -1 && uri.getPort() != 443)) {
                return false;
            }
            String host = uri.getHost();
            String path = uri.getPath();
            return ("api.open-meteo.com".equals(host) && "/v1/forecast".equals(path))
                    || ("geocoding-api.open-meteo.com".equals(host) && "/v1/search".equals(path))
                    || ("api.aladhan.com".equals(host) && path.matches("/v1/calendar/[0-9]{4}/[0-9]{1,2}"));
        } catch (URISyntaxException | NullPointerException error) {
            return false;
        }
    }
}
