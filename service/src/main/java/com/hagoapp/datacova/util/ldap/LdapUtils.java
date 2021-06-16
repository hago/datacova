package com.hagoapp.datacova.util.ldap;

import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.CoVaLogger;
import org.apache.directory.api.ldap.model.cursor.SearchCursor;
import org.apache.directory.api.ldap.model.entry.Attribute;
import org.apache.directory.api.ldap.model.entry.Entry;
import org.apache.directory.api.ldap.model.entry.Value;
import org.apache.directory.api.ldap.model.exception.LdapException;
import org.apache.directory.api.ldap.model.message.*;
import org.apache.directory.api.ldap.model.name.Dn;
import org.apache.directory.ldap.client.api.ConnectionClosedEventListener;
import org.apache.directory.ldap.client.api.LdapNetworkConnection;
import org.slf4j.Logger;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A wrapper of apache directory API to make use of Active Directory through
 * LDAP to query entities.
 *
 * @author suncj2
 * @since 2.0
 */
public class LdapUtils implements Closeable, ConnectionClosedEventListener {

    public static final String ATTRIBUTE_DISPLAY_NAME = "displayName";
    public static final String ATTRIBUTE_ITCODE = "cn";
    public static final String ATTRIBUTE_EMPLOYEE_NUMBER = "employeeNumber";
    public static final String ATTRIBUTE_EMPLOYEE_TYPE = "employeeType";
    public static final String ATTRIBUTE_GIVEN_NAME = "givenName";
    public static final String ATTRIBUTE_MAIL = "mail";
    public static final String ATTRIBUTE_MANAGER = "manager";
    public static final String ATTRIBUTE_TELEPHONE_NUMBER = "telephoneNumber";
    public static final String ATTRIBUTE_SN = "sn";
    public static final String ATTRIBUTE_PROXY_ADDRESS = "proxyAddresses";
    public static final String ATTRIBUTE_THUMBNAIL_PHOTO = "thumbnailPhoto";
    public static final String ATTRIBUTE_MEMBER_OF = "memberOf";
    public static final String ATTRIBUTE_CHINESE_NAME = "msExchExtensionAttribute16";
    public static final String ATTRIBUTE_TITLE = "title";
    public static final String ATTRIBUTE_DISTINGUISHED_NAME = "distinguishedName";
    public static final String ATTRIBUTE_DN = "dn";

    private static final int SEARCH_RETURN_LIMIT = 20;

    public static final List<String> DEFAULT_ATTRIBUTES = List.of(
            ATTRIBUTE_DISPLAY_NAME,
            ATTRIBUTE_ITCODE,
            ATTRIBUTE_EMPLOYEE_NUMBER,
            ATTRIBUTE_EMPLOYEE_TYPE,
            ATTRIBUTE_GIVEN_NAME,
            ATTRIBUTE_MAIL,
            ATTRIBUTE_MANAGER,
            ATTRIBUTE_TELEPHONE_NUMBER,
            ATTRIBUTE_SN,
            ATTRIBUTE_PROXY_ADDRESS,
            ATTRIBUTE_THUMBNAIL_PHOTO,
            ATTRIBUTE_MEMBER_OF,
            ATTRIBUTE_CHINESE_NAME,
            ATTRIBUTE_TITLE,
            ATTRIBUTE_DN,
            ATTRIBUTE_DISTINGUISHED_NAME
    );

    private final Logger logger = CoVaLogger.getLogger();

    /**
     * Constructor using default port(389), default bind dn(null), default bind password(null) and not to use ssl.
     *
     * @param serverName            LDAP server name
     * @param baseDistinguishedName Base distinguished name from which to find elements
     * @throws CoVaException if connect to server failed or bind failed
     */
    public LdapUtils(String serverName, String baseDistinguishedName) throws CoVaException {
        init(serverName, 389, baseDistinguishedName, null, null, false);
    }

    /**
     * Constructor using default base distinguished name(null, any follow-up operation must use explicit base_dn),
     * default bind dn(null), default bind password(null).
     *
     * @param serverName LDAP server name
     * @param serverPort LDAP server port
     * @param useSsl     whether to connect through ssl
     * @throws CoVaException if connect to server failed or bind failed
     */
    public LdapUtils(String serverName, int serverPort, Boolean useSsl) throws CoVaException {
        init(serverName, serverPort, null, null, null, useSsl);
    }

    /**
     * Constructor default bind dn(null), default bind password(null) and not to use ssl.
     *
     * @param serverName            LDAP server name
     * @param serverPort            LDAP server port
     * @param baseDistinguishedName Base distinguished name from which to find elements
     * @throws CoVaException if connect to server failed or bind failed
     */
    public LdapUtils(String serverName, int serverPort, String baseDistinguishedName) throws CoVaException {
        init(serverName, serverPort, baseDistinguishedName, null, null, false);
    }

    /**
     * Constructor using default port(389), default bind dn(null), default bind password(null).
     *
     * @param serverName            LDAP server name
     * @param baseDistinguishedName Base distinguished name from which to find elements
     * @param useSsl                whether to connect through ssl
     * @throws CoVaException if connect to server failed or bind failed
     */
    public LdapUtils(String serverName, String baseDistinguishedName, boolean useSsl) throws CoVaException {
        init(serverName, 389, baseDistinguishedName, null, null, useSsl);
    }

    /**
     * Constructor not to use ssl.
     *
     * @param serverName            LDAP server name
     * @param serverPort            LDAP server port
     * @param baseDistinguishedName Base distinguished name from which to find elements
     * @param bindDistinguishedName The distinguished name to bind with
     * @param bindPassword          The password to bind with
     * @throws CoVaException if connect to server failed or bind failed
     */
    public LdapUtils(String serverName, int serverPort, String baseDistinguishedName, String bindDistinguishedName,
                     String bindPassword) throws CoVaException {
        init(serverName, serverPort, baseDistinguishedName, bindDistinguishedName, bindPassword, false);
    }

    /**
     * Constructor with all arguments.
     *
     * @param serverName            LDAP server name
     * @param serverPort            LDAP server port
     * @param baseDistinguishedName Base distinguished name from which to find elements
     * @param bindDistinguishedName The distinguished name to bind with
     * @param bindPassword          The password to bind with
     * @param useSsl                whether to connect through ssl
     * @throws CoVaException if connect to server failed or bind failed
     */
    public LdapUtils(
            String serverName,
            int serverPort,
            String baseDistinguishedName,
            String bindDistinguishedName,
            String bindPassword,
            boolean useSsl
    ) throws CoVaException {
        init(serverName, serverPort, baseDistinguishedName, bindDistinguishedName, bindPassword, useSsl);
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

    private String createDistinguishedName(String userId) throws CoVaException {
        Pattern pattern = Pattern.compile("cn=([^,]+),(.*?),DC=lenovo,DC=com", Pattern.CASE_INSENSITIVE);
        Matcher match = pattern.matcher(userId);
        if (match.find()) {
            return userId;
        }
        int pos = userId.indexOf("@");
        String itCode = pos > 0 ? userId.substring(0, pos) : userId;
        Map<String, Object> userMap = getUser(itCode, List.of(ATTRIBUTE_ITCODE, ATTRIBUTE_DISTINGUISHED_NAME));
        if (userMap == null) {
            throw new CoVaException(String.format("user %s not found", userId));
        }
        return userMap.get(ATTRIBUTE_DISTINGUISHED_NAME).toString();
    }

    /**
     * Get specified user information with default attributes list.
     *
     * @param userName user name
     * @return A map containing attribute {@literal ->} value pairs on demand, or null if user not existed
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public Map<String, Object> getUser(String userName) throws CoVaException {
        return getUser(userName, DEFAULT_ATTRIBUTES);
    }

    /**
     * Get specified user information.
     *
     * @param userName   user name
     * @param attributes The attributes need to retrieve
     * @return A map containing attribute {@literal ->} value pairs on demand, or null if user not existed
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public Map<String, Object> getUser(String userName, List<String> attributes) throws CoVaException {
        final List<Map<String, Object>> list = search(String.format("(cn=%s)", userName), attributes, 1);

        if (list.isEmpty()) {
            return null;
        } else {
            return list.get(0);
        }
    }

    private List<Entry> rawSearch(String filter, List<String> attributes, Dn searchDn, long max)
            throws LdapException, IOException {
        String[] attributeArray = new String[attributes.size()];
        attributeArray = attributes.toArray(attributeArray);
        SearchRequest searchReq = new SearchRequestImpl()
                .setScope(SearchScope.SUBTREE)
                .setBase(searchDn)
                .setFilter(filter)
                .addAttributes(attributeArray)
                .setMessageId(1)
                .setSizeLimit(max)
                .setTimeLimit(timeOut);
        List<Entry> ret = new ArrayList<>();
        try (SearchCursor cur = conn.search(searchReq)) {
            cur.forEach(response -> {
                if (response instanceof SearchResultEntry) {
                    SearchResultEntry searchResultEntry = (SearchResultEntry) response;
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
        return search(filter, DEFAULT_ATTRIBUTES, baseDn, SEARCH_RETURN_LIMIT);
    }

    /**
     * Search entity from LDAP server with default base dn and default records amount.
     *
     * @param filter     filter expression
     * @param attributes attributes of target entities to retrieve
     * @return A List of entities found, each entity is represented as a map with attribute {@literal ->} value
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public List<Map<String, Object>> search(String filter, List<String> attributes) throws CoVaException {
        return search(filter, attributes, baseDn, SEARCH_RETURN_LIMIT);
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
        return search(filter, DEFAULT_ATTRIBUTES, baseDn, max);
    }

    /**
     * Search entity from LDAP server with default base dn.
     *
     * @param filter     filter expression
     * @param attributes attributes of target entities to retrieve
     * @param max        Max amount of records to return
     * @return A List of entities found, each entity is represented as a map with attribute {@literal ->} value
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public List<Map<String, Object>> search(String filter, List<String> attributes, long max) throws CoVaException {
        return search(filter, attributes, baseDn, max);
    }

    /**
     * Search entity from LDAP server with default records amount.
     *
     * @param filter        filter expression
     * @param attributes    attributes of target entities to retrieve
     * @param searchBasedDn Base directory to search in
     * @return A List of entities found, each entity is represented as a map with attribute {@literal ->} value
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public List<Map<String, Object>> search(String filter, List<String> attributes, String searchBasedDn)
            throws CoVaException {
        return search(filter, attributes, searchBasedDn, SEARCH_RETURN_LIMIT);
    }

    /**
     * Search entity from LDAP server.
     *
     * @param filter        filter expression
     * @param attributes    attributes of target entities to retrieve
     * @param searchBasedDn Base directory to search in
     * @param max           Max amount of records to return
     * @return A List of entities found, each entity is represented as a map with attribute {@literal ->} value
     * @throws CoVaException if LDAP binding failed or connection corrupted
     */
    public List<Map<String, Object>> search(String filter, List<String> attributes, String searchBasedDn, long max)
            throws CoVaException {
        List<Map<String, Object>> maps = new ArrayList<>();
        try {
            if (!bound) {
                bind();
            }
            Dn dn = new Dn(searchBasedDn);

            rawSearch(filter, attributes, dn, max).forEach(entry -> {
                Map<String, Object> map = new HashMap<>();
                attributes.forEach(attrName -> {
                    Attribute attr = entry.get(attrName);
                    if (attr == null) {
                        if (attrName.equals(ATTRIBUTE_DN)) {
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