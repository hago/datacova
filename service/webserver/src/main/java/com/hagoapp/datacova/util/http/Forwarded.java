package com.hagoapp.datacova.util.http;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is an implementation of http header "Forwarded", see
 * <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Forwarded">Forwarded</a> for more details.
 *
 * @author suncj2
 * @since 2.0
 */
public class Forwarded {
    private String byIdentifier;
    private String forClientIp;
    private List<String> forProxies = new ArrayList<>();
    private String host;
    private Scheme protocol = Scheme.HTTPS;

    public String getByIdentifier() {
        return byIdentifier;
    }

    public void setByIdentifier(String byIdentifier) {
        this.byIdentifier = byIdentifier;
    }

    public String getForClientIp() {
        return forClientIp;
    }

    public void setForClientIp(String forClientIp) {
        this.forClientIp = forClientIp;
    }

    public List<String> getForProxies() {
        return forProxies;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Scheme getProtocol() {
        return protocol;
    }

    public void setProtocol(Scheme protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        List<String> items = new ArrayList<>();
        if (byIdentifier != null) {
            items.add(String.format("by: %s", byIdentifier));
        }
        if (forClientIp != null) {
            items.add(String.format("for: %s", forClientIp));
            for (String proxy : forProxies) {
                items.add(String.format("for: %s", proxy));
            }
        }
        if (host != null) {
            items.add(String.format("host: %s", host));
        }
        if (protocol != null) {
            items.add(String.format("protocol: %s", protocol.getValue()));
        }
        return String.join(", ", items);
    }

    /**
     * Parse text of Forwarded header into object.
     *
     * @param text Forwarded header text
     * @return Forwarded object or null if text is malformed.
     */
    public static Forwarded parse(String text) {
        var parts = text.split(",");
        var forwarded = new Forwarded();
        for (var part : parts) {
            var kv = part.split(":");
            if (kv.length != 2) {
                continue;
            }
            var value = kv[1].trim();
            switch (kv[0].trim().toLowerCase()) {
                case "by":
                    forwarded.setByIdentifier(value);
                    break;
                case "host":
                    forwarded.setHost(value);
                    break;
                case "protocol":
                    forwarded.setProtocol(Scheme.valueOf(value));
                    break;
                case "for":
                    if (forwarded.getForClientIp() == null) {
                        forwarded.setForClientIp(value);
                    } else {
                        forwarded.getForProxies().add(value);
                    }
                    break;
                default:
                    break;
            }
        }
        return forwarded;
    }
}
