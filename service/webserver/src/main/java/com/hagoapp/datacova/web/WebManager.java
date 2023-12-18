/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.web;

import com.hagoapp.datacova.utility.CoVaException;
import com.hagoapp.datacova.config.WebConfig;
import com.hagoapp.datacova.config.WebSocketConfig;
import com.hagoapp.datacova.util.http.RequestHelper;
import com.hagoapp.datacova.util.http.ResponseHelper;
import com.hagoapp.datacova.utility.StackTraceWriter;
import com.hagoapp.datacova.web.annotation.WebEndPoint;
import com.hagoapp.datacova.web.authentication.AuthenticatorFactory;
import com.hagoapp.datacova.web.websocket.*;
import com.hagoapp.datacova.web.websocket.connect.ConnectServerMessage;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Route;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Web server initialization manager.
 */
public class WebManager {

    private final Logger logger = LoggerFactory.getLogger(WebManager.class);
    private WebConfig webConfig;
    private static final Map<String, WebManager> instances = new ConcurrentHashMap<>();
    private final Map<String, WebHandler> handlers = new HashMap<>();
    private Vertx vertx;
    private HttpServer webServer;

    public static WebManager getManager(WebConfig config, List<String> packageNames) {
        String k = createServerKey(config);
        return instances.compute(k, (key, existed) -> {
            if (existed != null) {
                return existed;
            } else {
                var instance = new WebManager();
                try {
                    instance.createWebServer(config, packageNames);
                    return instance;
                } catch (CoVaException e) {
                    return null;
                }
            }
        });
    }

    private WebManager() {

    }

    private static String createServerKey(WebConfig config) {
        return config.toString();
    }

    private void createWebServer(WebConfig config, List<String> packageNames) throws CoVaException {
        if (!isWebRunning()) {
            webConfig = config;
            var options = new VertxOptions();
            options.getFileSystemOptions().setFileCacheDir(config.getTempDirectory());
            vertx = Vertx.vertx(options);
            var webOptions = new HttpServerOptions();
            webOptions.setUseProxyProtocol(true);
            webServer = vertx.createHttpServer(webOptions);
            var router = findRouter(vertx, packageNames);
            webServer.requestHandler(router);
            if (config.getWebSockets() != null) {
                setupWebSocket(config.getWebSockets());
            }
            webServer.listen(config.getPort(), config.getBindIp());
        }
    }

    public void shutDownWebServer() {
        logger.info("web server {} is shutting down", webConfig.getIdentity());
        try {
            webServer.close(handler -> {
                handlers.clear();
                instances.remove(createServerKey(webConfig));
                vertx.close();
                logger.info("web server {} shut down successfully", webConfig.getIdentity());
            });
        } catch (Exception e) {
            logger.error("error in web {} shutting down: {}", webConfig.getIdentity(), e.getMessage());
        }
    }

    public boolean isWebRunning() {
        return (webServer != null) && webServer.isMetricsEnabled();
    }

    private Router findRouter(Vertx vertx, List<String> packageNames) throws CoVaException {
        for (var packageName : packageNames) {
            loadAnnotatedWebHandlers(packageName);
            loadWebInterfaces(packageName);
        }
        var router = Router.router(vertx);
        router.route().failureHandler(context -> {
            var error = context.failure();
            var e = error instanceof InvocationTargetException ? error.getCause() : error;
            var code = context.statusCode();
            String errorMessage;
            if (e == null) {
                errorMessage = HttpResponseStatus.valueOf(code).reasonPhrase();
            } else {
                errorMessage = e.getMessage();
            }
            logger.info("{} {} {} from {}\t{}", webConfig.getIdentity(), context.request().method().name(),
                    context.request().path(), context.request().remoteAddress().host(), code);
            List<String> stacktrace;
            if (webConfig.isOutputStackTrace() && (e != null)) {
                stacktrace = StackTraceWriter.write(e, logger);
                ResponseHelper.respondError(context, HttpResponseStatus.valueOf(code), errorMessage,
                        Map.of("stackTrace", stacktrace));
            } else {
                ResponseHelper.respondError(context, HttpResponseStatus.valueOf(code), errorMessage);
            }
        });
        var bodyHandler = BodyHandler.create()
                .setBodyLimit(webConfig.getUploadSizeLimit())
                .setUploadsDirectory(webConfig.getUploadTempDirectory())
                .setDeleteUploadedFilesOnEnd(true);
        router.post().handler(bodyHandler);
        router.put().handler(bodyHandler);
        for (var handler : handlers.values()) {
            createCrossOriginRouteHandlers(router, handler);
            createAuthenticateHandlers(router, handler);
            createRouteHandlers(router, handler);
        }
        return router;
    }

    private void setupWebSocket(List<WebSocketConfig> webSockets) {
        final var acceptablePaths = webSockets.stream()
                .map(config -> config.getRoute().toLowerCase()).collect(Collectors.toList());
        webServer.webSocketHandler(event -> {
            var path = event.path().toLowerCase();
            if (!acceptablePaths.contains(path)) {
                logger.error("access attempt to unknown path {} is rejected", event.path());
                event.reject(HttpResponseStatus.NOT_FOUND.code());
                return;
            }
            var userInfo = Auth.authenticate(event);
            if (userInfo == null) {
                logger.error("Unauthorized access attempt to path {} is rejected", event.path());
                event.reject(HttpResponseStatus.FORBIDDEN.code());
                return;
            }
            var session = new WsSession();
            session.setUserInfo(userInfo);
            session.setRemoteIp(RequestHelper.getRemoteIp(event.headers(), event.remoteAddress().host()));
            session.setDeviceIdentity(RequestHelper.getUserAgent(event.headers()));
            var wsm = WebSocketManager.getManager();
            wsm.addUserSession(session, event);
            event.textMessageHandler(msg -> {
                MessageHandlerFactory factory;
                try {
                    factory = new MessageHandlerFactory(msg);
                    var handler = factory.createMessageHandler();
                    var message = ClientMessage.fromJson(msg);
                    if ((handler == null) || (message == null)) {
                        throw new IOException(String.format("Message can't be recognized: %s", msg));
                    } else {
                        var response = handler.handleMessage(event, message);
                        event.writeTextMessage(response.toJson());
                    }
                } catch (Exception e) {
                    event.writeTextMessage(new ErrorResponseMessage(e.getMessage()).toJson());
                }
            });
            event.closeHandler(ignore -> {
                wsm.removeSession(event);
                logger.info("web socket {} closed of device {} from {}", session.getId(),
                        session.getDeviceIdentity(), session.getRemoteIp());
            });
            event.writeTextMessage(new ConnectServerMessage().toJson());
            logger.info("web socket {} established for {} {}", session.getId(),
                    session.getDeviceIdentity(), session.getRemoteIp());
        });
    }

    private void createRouteHandlers(Router router, WebHandler handler) {
        var route = createRoute(router, handler);
        logger.debug("{} Create handler for {}", webConfig.getIdentity(), handler);
        if (handler.isBlocking()) {
            createBlockHandler(handler, route);
        } else {
            createNonBlockHandler(handler, route);
        }
    }

    private void createBlockHandler(WebHandler handler, Route route) {
        if (WebInterface.class.isAssignableFrom(handler.getInstanceClass())) {
            route.blockingHandler(context -> {
                try {
                    var instance = handler.getInstanceClass().getDeclaredConstructor().newInstance();
                    var webInterface = (WebInterface) instance;
                    WebInterface.Handler theHandler = webInterface.requestHandlers().get(handler.getMethod());
                    theHandler.handle(context);
                    context.next();
                } catch (NoSuchMethodException | InstantiationException |
                         IllegalAccessException | InvocationTargetException e) {
                    context.fail(e);
                }
            }).handler(logHandler);
        } else {
            route.blockingHandler(context -> {
                try {
                    var instance = handler.getInstanceClass().getDeclaredConstructor().newInstance();
                    handler.getFunction().invoke(instance, context);
                    context.next();
                } catch (NoSuchMethodException | InstantiationException |
                         IllegalAccessException | InvocationTargetException e) {
                    context.fail(e);
                }
            }).blockingHandler(logHandler);
        }
    }

    private void createNonBlockHandler(WebHandler handler, Route route) {
        if (WebInterface.class.isAssignableFrom(handler.getInstanceClass())) {
            route.handler(context -> {
                try {
                    var instance = handler.getInstanceClass().getDeclaredConstructor().newInstance();
                    var webInterface = (WebInterface) instance;
                    WebInterface.Handler theHandler = webInterface.requestHandlers().get(handler.getMethod());
                    theHandler.handle(context);
                    context.next();
                } catch (Exception e) {
                    context.fail(e);
                }
            }).handler(logHandler);
        } else {
            route.handler(context -> {
                try {
                    var instance = handler.getInstanceClass().getDeclaredConstructor().newInstance();
                    handler.getFunction().invoke(instance, context);
                    context.next();
                } catch (Exception e) {
                    context.fail(e);
                }
            }).handler(logHandler);
        }
    }

    private final Handler<RoutingContext> logHandler = context -> {
        var req = context.request();
        var method = req.method().name();
        var path = req.path();
        var remoteAddress = req.remoteAddress();
        var status = req.response().getStatusCode();
        logger.info("{} {} from {}\t{}", method, path, remoteAddress, status);
        if (!context.response().ended()) {
            context.response().end();
        }
    };

    private void createAuthenticateHandlers(Router router, WebHandler handler) {
        createRoute(router, handler).handler(context -> {
            if (handler.getAuthenticatorTypes().stream().noneMatch(authType -> {
                var authenticator = AuthenticatorFactory.createAuthenticator(authType);
                return (authenticator != null) && authenticator.authenticate(context);
            })) {
                logger.error("Authentication failed for {} {}", handler.getMethod(), handler.getPath());
                ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "Not authenticated");
            } else {
                logger.debug("authentication passed for {} {}, execute next handler",
                        handler.getMethod(), handler.getPath());
                context.next();
            }
        });
    }

    private void createCrossOriginRouteHandlers(Router router, WebHandler handler) {
        if (webConfig.isAllowCrossOriginResourceSharing()) {
            var origins = webConfig.getCrossOriginSources();
            var headers = getAllowedHeaders(handler);
            createRoute(router, handler)
                    .handler(createCrossOriginHandler(List.of(MethodName.GET), headers, origins));
        }
    }

    private CorsHandler createCrossOriginHandler(List<String> methods, List<String> headers, List<String> origins) {
        var allowHeaders = new HashSet<String>();
        allowHeaders.addAll(headers);
        allowHeaders.addAll(List.of(
                "Content-Type",
                "Access-Control-Allow-Method",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));
        var allowedMethods = new HashSet<String>();
        allowedMethods.add(MethodName.OPTIONS);
        allowedMethods.addAll(methods);
        logger.info("{} CORS allow: {} {} {}", webConfig.getIdentity(), allowedMethods, allowHeaders, origins);
        return CorsHandler.create()
                .addOrigins(origins)
                .allowedMethods(allowedMethods.stream().map(HttpMethod::valueOf).collect(Collectors.toSet()))
                .allowedHeaders(allowHeaders)
                .allowCredentials(true);
    }

    private Route createRoute(Router router, WebHandler handler) {
        var method = HttpMethod.valueOf(handler.getMethod());
        return handler.isPathAsRegex() ? router.routeWithRegex(method, handler.getPath()) :
                router.route(method, handler.getPath());
    }

    private List<String> getAllowedHeaders(WebHandler handler) {
        List<String> headers = new ArrayList<>();
        for (var authType : handler.getAuthenticatorTypes()) {
            var authenticator = AuthenticatorFactory.createAuthenticator(authType);
            assert authenticator != null;
            headers.addAll(Arrays.stream(authenticator.getHeaders()).collect(Collectors.toList()));
        }
        headers.addAll(handler.getHeaders());
        return headers;
    }

    private void loadAnnotatedWebHandlers(String packageName) throws CoVaException {
        logger.info("{} searching WebEndPoint annotated methods in {}", webConfig.getIdentity(), packageName);
        var reflections = new Reflections(packageName, Scanners.MethodsAnnotated);
        var methods = reflections.getMethodsAnnotatedWith(WebEndPoint.class);
        for (var method : methods) {
            var clz = method.getDeclaringClass();
            if ((packageName != null) && !clz.getPackageName().startsWith(packageName)) {
                continue;
            }
            if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                logger.error("{} Annotated web handler {}.{} is not public", webConfig.getIdentity(),
                        clz.getPackageName(), method.getName());
            }
            var paramTypes = method.getParameterTypes();
            if ((paramTypes.length < 1) || !paramTypes[0].isAssignableFrom(RoutingContext.class)) {
                logger.error("{} Annotated web handler {}.{} is not accepting RoutingContext", webConfig.getIdentity(),
                        clz.getPackageName(), method.getName());
            }
            WebEndPoint endPoint = method.getAnnotation(WebEndPoint.class);
            for (var httpMethod : endPoint.methods()) {
                var handler = new WebHandler();
                handler.setInstanceClass(clz);
                handler.setAuthenticatorTypes(Arrays.stream(endPoint.authTypes()).collect(Collectors.toList()));
                handler.setBlocking(endPoint.isBlocking());
                handler.setFunction(method);
                handler.setHeaders(Arrays.stream(endPoint.headers()).collect(Collectors.toList()));
                handler.setPath(endPoint.path());
                handler.setMethod(httpMethod);
                handler.setPathAsRegex(endPoint.isPathRegex());
                checkDuplicateHandler(handler, handlers);
                handlers.put(handler.getKey(), handler);
                logger.info("{} Annotated web handler found for {} {}", webConfig.getIdentity(),
                        httpMethod, endPoint.path());
            }
        }
    }

    private void loadWebInterfaces(String packageName) throws CoVaException {
        logger.info("{} searching WebInterface descendants in {}", webConfig.getIdentity(), packageName);
        var reflections = new Reflections(packageName, Scanners.SubTypes);
        var types = reflections.getSubTypesOf(WebInterface.class);
        for (var clz : types) {
            if ((packageName != null) && !clz.getPackageName().startsWith(packageName)) {
                continue;
            }
            try {
                var instance = clz.getDeclaredConstructor().newInstance();
                for (var entry : instance.requestHandlers().entrySet()) {
                    var handler = new WebHandler();
                    handler.setInstanceClass(clz);
                    handler.setAuthenticatorTypes(instance.getAuthTypes());
                    handler.setBlocking(instance.isBlocking());
                    handler.setFunction(entry.getValue().getClass().getMethod("handle", RoutingContext.class));
                    handler.setHeaders(instance.requireHeaders());
                    handler.setPath(instance.getPath());
                    handler.setMethod(entry.getKey());
                    handler.setPathAsRegex(instance.isPathRegex());
                    checkDuplicateHandler(handler, handlers);
                    handlers.put(handler.getKey(), handler);
                    logger.info("{} WebInterface descendant web handler found for {} {}", webConfig.getIdentity(),
                            entry.getKey(), instance.getPath());
                }
            } catch (NoSuchMethodException e) {
                logger.error("{} WebInterface descendant {} has no default constructor", webConfig.getIdentity(),
                        clz.getCanonicalName());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                logger.error("{} WebInterface descendant {} creation error: {}", webConfig.getIdentity(),
                        clz.getCanonicalName(), e.getMessage());
            }
        }
    }

    private void checkDuplicateHandler(WebHandler handler, Map<String, WebHandler> map) throws CoVaException {
        if (map.containsKey(handler.getKey())) {
            generateDuplicateException(handler, map.get(handler.getKey()));
        }
        if (handler.isPathAsRegex()) {
            var pattern = Pattern.compile(handler.getPath());
            var nonRegexHandlers = map.values().stream().filter(entry ->
                            !entry.getMethod().equals(handler.getMethod()) && !entry.isPathAsRegex())
                    .collect(Collectors.toList());
            for (var exHandler : nonRegexHandlers) {
                if (pattern.matcher(exHandler.getPath()).find()) {
                    generateDuplicateException(handler, exHandler);
                }
            }
        } else {
            var regexHandlers = map.values().stream().filter(webHandler -> webHandler.getMethod().equals(handler.getMethod()) &&
                    webHandler.isPathAsRegex()).collect(Collectors.toList());
            for (var exHandler : regexHandlers) {
                var p = Pattern.compile(exHandler.getPath());
                if (p.matcher(handler.getPath()).find()) {
                    generateDuplicateException(handler, exHandler);
                }
            }
        }
    }

    private void generateDuplicateException(WebHandler handler1, WebHandler handler2) throws CoVaException {
        var error = String.format("%s duplicated handler for %s [%s, %s] between %s.%s and %s.%s",
                webConfig.getIdentity(),
                handler1.getMethod(), handler1.getPath(), handler2.getPath(),
                handler1.getInstanceClass().getCanonicalName(), handler1.getFunction().getName(),
                handler2.getInstanceClass(), handler2.getFunction().getName());
        logger.error(error);
        throw new CoVaException(error);
    }
}
