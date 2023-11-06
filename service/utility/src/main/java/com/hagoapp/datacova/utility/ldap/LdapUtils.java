/*
 * Copyright (c) 2021.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.hagoapp.datacova.utility.ldap;

import com.hagoapp.datacova.utility.CoVaException;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.SearchRequestImpl;
import org.apache.directory.api.ldap.model.message.SearchResultEntry;
import org.apache.directory.api.ldap.model.message.SearchScope;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.ConnectionClosedEventListener;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A wrapper of apache directory API to make use of Active Directory through
 * LDAP to query entities.
 *
 * @author suncj2
 * @since 2.0
 */
public class LdapUtils implements Closeable, ConnectionClosedEventListener {

    private static final int SEARCH_RETURN_LIMIT = 20;

    public static final List<String> DEFAULT_ATTRIBUTE_NAMES = List.of(
            "ATTRIBUTE_DISPLAY_NAME",
            "ATTRIBUTE_USERID",
            "ATTRIBUTE_EMPLOYEE_NUMBER",
            "ATTRIBUTE_EMPLOYEE_TYPE",
            "ATTRIBUTE_GIVEN_NAME",
            "ATTRIBUTE_MAIL",
            "ATTRIBUTE_MANAGER",
            "ATTRIBUTE_TELEPHONE_NUMBER",
            "ATTRIBUTE_SN",
            "ATTRIBUTE_PROXY_ADDRESS",
            "ATTRIBUTE_THUMBNAIL_PHOTO",
            "ATTRIBUTE_MEMBER_OF",
            "ATTRIBUTE_NATIVE_NAME",
            "ATTRIBUTE_TITLE",
            "ATTRIBUTE_DN",
            "ATTRIBUTE_DISTINGUISHED_NAME"
    );

    private final Logger logger = LoggerFactory.getLogger(LdapUtils.class);
    private final LdapConfig conf;

    /**
     * Constructor with <code>LdapConfig</code>.
     *
     * @param config config of ldap
     * @throws CoVaException if connect to server failed or bind failed
     */
    public LdapUtils(LdapConfig config) throws CoVaException {
        conf = config;
        conf.getAttributes().normalize();
        init(config.getHost(), config.getPort(), config.getBaseDistinguishName(), config.getBindDistinguishName(),
                config.getBindPassword(), config.isSsl());
    }

    private LdapNetworkConnection conn;
    private String server;
    private int port;
    private String baseDn;
    private String bindDn;
    private String bindPassword;
    private boolean ssl;
    private boolean bound = false;
    private int timeOut = 0;

    private void init(String serverName,
                      int serverPort,
                      String baseDistinguishedName,
                      String bindDistinguishedName,
                      String bindPassword,
                      boolean useSsl) throws CoVaException {
        this.server = serverName;
        this.port = serverPort;
        this.baseDn = baseDistinguishedName;
        this.bindDn = bindDistinguishedName;
        this.bindPassword = bindPassword;
        this.ssl = useSsl;
        createConnection();
        try {
            bind();
        } catch (LdapException e) {
            throw new CoVaException("init bind fail: " + e, e);
        }
    }

    private void createConnection() {
        conn = new LdapNetworkConnection(this.server, this.port, this.ssl);
        conn.addConnectionClosedEventListener(this);
    }

    /**
     * Whether this LDAP connection is bound with either bind_dn / password or user supplied credentials.
     *
     * @return True if bound
     */
    public boolean isBound() {
        return bound;
    }

    /**
     * Function to escape entity name used in LDAP.
     *
     * @param input Raw name
     * @return escaped name
     */
    public static String escape(String input) {
        return input == null ? null :
                input.replace("*", "\\2a")
                        .replace("(", "\\28")
                        .replace(")", "\\29")
                        .replace("/", "\\5c")
                        .replace("\\", "\\2f");
    }

    /**
     * Function to unescape value from LDAP.
     *
     * @param input value fetched from LDAP
     * @return raw value
     */
    public static String unEscape(String input) {
        return input == null ? null :
                input.replace("\\2a", "*")
                        .replace("\\28", "(")
                        .replace("\\29", ")")
                        .replace("\\5c", "/")
                        .replace("\\2f", "\\");
    }

    /**
     * Bind to LDAP server with anonymous or with bind_dn / password.
     *
     * @throws LdapException if binding failed
     */
    public void bind() throws LdapException {
        if ((conn == null) || !conn.isConnected()) {
            createConnection();
        }
        if (bindDn == null) {
            conn.bind();
        } else {
            conn.bind(bindDn, bindPassword);
            bound = true;
        }
    }

    /**
     * Bind to LDAP server with explicit credentials.
     *
     * @param userId       Distinguished name to bind with
     * @param bindPassword Password to bind with
     * @return True if binding successful
     */
    public boolean bind(String userId, String bindPassword) {
        try {
            String bindDistinguishedName = createDistinguishedName(userId);
            conn.bind(bindDistinguishedName, bindPassword);
            bindDn = bindDistinguishedName;
            this.bindPassword = bindPassword;
            bound = true;
        } catch (LdapException | CoVaException ex) {
            logger.error(ex.getMessage());
            bound = false;
        }
        return bound;
    }

    public String createDistinguishedName(String userId) throws CoVaException {
        if (userId.contains("=")) {
            return userId;
        }
        int pos = userId.indexOf("@");
        String un = pos > 0 ? userId.substring(0, pos) : userId;
        var dnField = conf.getAttributes().getActualAttribute(LdapAttributes.ATTRIBUTE_DISTINGUISHED_NAME);
        Map<String, Object> userMap = getUser(un, List.of(dnField));
        if (userMap == null) {
            throw new CoVaException(String.format("user %s not found", userId));
        }
        return userMap.get(dnField).toString();
    }

    /**
     * Get specified user information with default attributes list.
     *
     * @param userName user name
     * @return A map containing attribute {@literal ->} value pairs on demand, or null if user not existed
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public Map<String, Object> getUser(String userName) throws CoVaException {
        return getUser(userName, conf.getAttributes().getActualAttributes(DEFAULT_ATTRIBUTE_NAMES));
    }

    /**
     * Get specified user information.
     *
     * @param userName            user name
     * @param attributeIdentities The attributes need to retrieve
     * @return A map containing attribute {@literal ->} value pairs on demand, or null if user not existed
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public Map<String, Object> getUser(String userName, List<String> attributeIdentities) throws CoVaException {
        var cn = conf.getAttributes().getActualAttribute(LdapAttributes.ATTRIBUTE_USERID);
        final List<Map<String, Object>> list = search(String.format("(%s=%s)", cn, userName), attributeIdentities, 1);
        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    /**
     * Search specified user information whose id matches pattern "keyword*".
     *
     * @param userId user id
     * @return A map containing attribute {@literal ->} value pairs on demand, or null if user not existed
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public List<Map<String, Object>> searchUser(String userId, int count) throws CoVaException {
        return searchUser(userId, conf.getAttributes().getActualAttributes(DEFAULT_ATTRIBUTE_NAMES), count);
    }

    /**
     * Search specified user information whose id matches pattern "keyword*".
     *
     * @param userId              user id
     * @param attributeIdentities The attributes need to retrieve
     * @return A map containing attribute {@literal ->} value pairs on demand, or null if user not existed
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public List<Map<String, Object>> searchUser(String userId, List<String> attributeIdentities, int count)
            throws CoVaException {
        var uid = conf.getAttributes().getActualAttribute(LdapAttributes.ATTRIBUTE_USERID);
        var name = conf.getAttributes().getActualAttribute(LdapAttributes.ATTRIBUTE_DISPLAY_NAME);
        return search(String.format("(|(%s=%s*)(%s=%s*))", uid, userId, name, userId), attributeIdentities, count);
    }

    private List<Entry> rawSearch(String filter, List<String> attributeNames, Dn searchDn, long max)
            throws LdapException, IOException {
        var attributeArray = new String[attributeNames.size()];
        attributeArray = attributeNames.toArray(attributeArray);
        var searchReq = new SearchRequestImpl()
                .setScope(SearchScope.SUBTREE)
                .setBase(searchDn)
                .setFilter(filter)
                .addAttributes(attributeArray)
                .setMessageId(1)
                .setSizeLimit(max)
                .setTimeLimit(timeOut);
        var ret = new ArrayList<Entry>();
        try (var cur = conn.search(searchReq)) {
            cur.forEach(response -> {
                if (response instanceof SearchResultEntry) {
                    var searchResultEntry = (SearchResultEntry) response;
                    ret.add(searchResultEntry.getEntry());
                }
            });
        }
        return ret;
    }

    /**
     * Search entity from LDAP server with default attributes group, default base dn and default records amount.
     *
     * @param filter filter expression
     * @return A List of entities found, each entity is represented as a map with attribute {@literal ->} value
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public List<Map<String, Object>> search(String filter) throws CoVaException {
        return search(filter, conf.getAttributes().getActualAttributes(DEFAULT_ATTRIBUTE_NAMES), baseDn, SEARCH_RETURN_LIMIT);
    }

    /**
     * Search entity from LDAP server with default base dn and default records amount.
     *
     * @param filter              filter expression
     * @param attributeIdentities attributes of target entities to retrieve
     * @return A List of entities found, each entity is represented as a map with attribute {@literal ->} value
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public List<Map<String, Object>> search(String filter, List<String> attributeIdentities) throws CoVaException {
        return search(filter, attributeIdentities, baseDn, SEARCH_RETURN_LIMIT);
    }

    /**
     * Search entity from LDAP server with default attributes group, default base dn.
     *
     * @param filter filter expression
     * @param max    Max amount of records to return
     * @return A List of entities found, each entity is represented as a map with attribute {@literal ->} value
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public List<Map<String, Object>> search(String filter, long max) throws CoVaException {
        return search(filter, conf.getAttributes().getActualAttributes(DEFAULT_ATTRIBUTE_NAMES), baseDn, max);
    }

    /**
     * Search entity from LDAP server with default base dn.
     *
     * @param filter              filter expression
     * @param attributeIdentities attributes of target entities to retrieve
     * @param max                 Max amount of records to return
     * @return A List of entities found, each entity is represented as a map with attribute {@literal ->} value
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public List<Map<String, Object>> search(String filter, List<String> attributeIdentities, long max) throws CoVaException {
        return search(filter, attributeIdentities, baseDn, max);
    }

    /**
     * Search entity from LDAP server with default records amount.
     *
     * @param filter              filter expression
     * @param attributeIdentities attributes of target entities to retrieve
     * @param searchBasedDn       Base directory to search in
     * @return A List of entities found, each entity is represented as a map with attribute {@literal ->} value
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public List<Map<String, Object>> search(String filter, List<String> attributeIdentities, String searchBasedDn)
            throws CoVaException {
        return search(filter, attributeIdentities, searchBasedDn, SEARCH_RETURN_LIMIT);
    }

    /**
     * Search entity from LDAP server.
     *
     * @param filter              filter expression
     * @param attributeIdentities attributes of target entities to retrieve
     * @param searchBasedDn       Base directory to search in
     * @param max                 Max amount of records to return
     * @return A List of entities found, each entity is represented as a map with attribute {@literal ->} value
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public List<Map<String, Object>> search(String filter, List<String> attributeIdentities, String searchBasedDn, long max)
            throws CoVaException {
        List<Map<String, Object>> maps = new ArrayList<>();
        var dnField = conf.getAttributes().getActualAttribute(LdapAttributes.ATTRIBUTE_DISTINGUISHED_NAME);
        try {
            if (!bound) {
                bind();
            }
            var dn = new Dn(searchBasedDn);
            rawSearch(filter, attributeIdentities, dn, max).forEach(entry -> {
                var map = new HashMap<String, Object>();
                attributeIdentities.forEach(attrName -> {
                    Attribute attr = entry.get(attrName);
                    if (attr == null) {
                        if (attrName.equals(dnField)) {
                            map.put(attrName, entry.getDn().toString());
                        } else {
                            map.put(attrName, null);
                        }
                    } else {
                        map.put(attrName, getAttributeValue(attr));
                    }
                });
                maps.add(map);
            });
        } catch (LdapException e) {
            throw new CoVaException("Ldap search error: " + e, e);
        } catch (IOException e) {
            throw new CoVaException("Ldap IO error: " + e, e);
        }
        return maps;
    }

    private Object getAttributeValue(Attribute attr) {
        switch (attr.size()) {
            case 0:
                return null;
            case 1:
                return getActualValue(attr.get());
            default: {
                List<Object> ret = new ArrayList<>();
                attr.forEach(value -> ret.add(getActualValue(value)));
                return ret;
            }
        }
    }

    private Object getActualValue(Value value) {
        return value.isHumanReadable() ? value.getString() :
                value.getBytes();
    }

    /**
     * Close internal LDAP connection to free resource.
     */
    @Override
    public void close() {
        conn.close();
    }

    /**
     * Get time out limit.
     *
     * @return time out limit, 0 means no limit
     */
    public int getTimeOut() {
        return timeOut;
    }

    /**
     * Set up time out limit.
     *
     * @param timeOut time out limit, 0 means no limit
     */
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    @Override
    public void connectionClosed() {
        bound = false;
        conn = null;
    }
}
