package com.hagoapp.datacova.util.http;

import java.util.ArrayList;
import java.util.List;

/**
 * An implementation of http header X-Forward-For.
 *
 * @author suncj2
 * @since 2.0
 */
public class XForwardFor {
    private String clientIp;
    private List<String> proxies = new ArrayList<>();

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public List<String> getProxies() {
        return proxies;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(clientIp);
        for (String proxy : proxies) {
            sb.append(", ").append(proxy);
        }
        return sb.toString();
    }

    /**
     * Parse text of X-Forward-For header into object.
     *
     * @param text X-Forward-For header text
     * @return XForwardFor object or null if text is malformed.
     */
    public static XForwardFor parse(String text) {
        if (text == null) {
            return null;
        }
        String[] parts = text.split(",");
        if (parts.length == 0) {
            return null;
        }
        XForwardFor instance = new XForwardFor();
        instance.setClientIp(parts[0].trim());
        for (int i = 1; i < parts.length; i++) {
            instance.getProxies().add(parts[i].trim());
        }
        return instance;
    }
}
