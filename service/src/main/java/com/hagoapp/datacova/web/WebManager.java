/*
 * Copyright (c) 2020.
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 */

package com.hagoapp.datacova.web;

import com.google.gson.Gson;
import com.hagoapp.datacova.CoVaException;
import com.hagoapp.datacova.CoVaLogger;
import com.hagoapp.datacova.config.WebConfig;
import com.hagoapp.datacova.config.WebSocketConfig;
import com.hagoapp.datacova.user.UserInfo;
import com.hagoapp.datacova.util.StackTraceWriter;
import com.hagoapp.datacova.util.http.ResponseHelper;
import com.hagoapp.datacova.web.annotation.WebEndPoint;
import com.hagoapp.datacova.web.authentication.AuthType;
import com.hagoapp.datacova.web.authentication.Authenticator;
import com.hagoapp.datacova.web.authentication.AuthenticatorFactory;
import com.hagoapp.datacova.web.websocket.*;
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
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Web server initialization manager.
 */
public class WebManager {

    private final Logger logger = CoVaLogger.getLogger();
    private WebConfig webConfig;
    private static final WebManager instance = new WebManager();
    private final Map<String, WebHandler> handlers = new HashMap<>();
    private Vertx vertx;
    private HttpServer webServer;

    public static WebManager getManager() {
        return instance;
    }

    public void createWebServer(WebConfig config) throws CoVaException {
        Set<String> packageNames = Arrays.stream(Package.getPackages()).map(Package::getName).collect(Collectors.toSet());
        createWebServer(config, new ArrayList<>(packageNames));
    }

    public void createWebServer(WebConfig config, List<String> packageNames) throws CoVaException {
        if (!isWebRunning()) {
            webConfig = config;
            VertxOptions options = new VertxOptions();
            options.getFileSystemOptions().setFileCacheDir(config.getTempDirectory());
            vertx = Vertx.vertx(options);
            HttpServerOptions webOptions = new HttpServerOptions();
            webServer = vertx.createHttpServer(webOptions);
            Router router = findRouter(vertx, packageNames);
            webServer.requestHandler(router);
            if (config.getWebSockets() != null) {
                setupWebSocket(config.getWebSockets());
            }
            webServer.listen(config.getPort(), config.getBindIp());
        }
    }

    public void shutDownWebServer() {
        try {
            webServer.close(handler -> {
                vertx.close();
                webServer = null;
                vertx = null;
                handlers.clear();
            });
        } catch (Throwable e) {
            //
        }
    }

    public boolean isWebRunning() {
        return (webServer != null) && webServer.isMetricsEnabled();
    }

    private Router findRouter(Vertx vertx, List<String> packageNames) throws CoVaException {
        for (String packageName : packageNames) {
            loadAnnotatedWebHandlers(packageName);
            loadWebInterfaces(packageName);
        }
        Router router = Router.router(vertx);
        router.route().failureHandler(context -> {
            Throwable error = context.failure();
            Throwable e = error instanceof InvocationTargetException ? error.getCause() : error;
            int code = context.statusCode();
            String errorMessage;
            if (e == null) {
                errorMessage = HttpResponseStatus.valueOf(code).reasonPhrase();
            } else {
                errorMessage = e.getMessage();
            }
            logger.info("{} {} from {}\t{}", context.request().method().name(), context.request().path(),
                    context.request().remoteAddress().host(), code);
            //logger.debug("message {}", errorMessage);
            List<String> stacktrace = new ArrayList<>();
            if (webConfig.isOutputStackTrace() && (e != null)) {
                stacktrace = StackTraceWriter.write(e, logger);
                ResponseHelper.respondError(context, HttpResponseStatus.valueOf(code), errorMessage,
                        Map.of("stackTrace", stacktrace));
            } else {
                ResponseHelper.respondError(context, HttpResponseStatus.valueOf(code), errorMessage);
            }
        });
        BodyHandler bodyHandler = BodyHandler.create()
                .setBodyLimit(webConfig.getUploadSizeLimit())
                .setUploadsDirectory(webConfig.getUploadTempDirectory())
                .setDeleteUploadedFilesOnEnd(true);
        router.post().handler(bodyHandler);
        router.put().handler(bodyHandler);
        for (WebHandler handler : handlers.values()) {
            createCrossOriginRouteHandlers(router, handler);
            createAuthenticateHandlers(router, handler);
            createRouteHandlers(router, handler);
        }
        return router;
    }

    private void setupWebSocket(List<WebSocketConfig> webSockets) {
        final List<String> acceptablePaths = webSockets.stream()
                .map(config -> config.getRoute().toLowerCase()).collect(Collectors.toList());
        webServer.webSocketHandler(event -> {
            String path = event.path().toLowerCase();
            if (!acceptablePaths.contains(path)) {
                logger.error("access attempt to unknown path {} is rejected", event.path());
                event.reject(HttpResponseStatus.NOT_FOUND.code());
                return;
            }
            UserInfo userInfo = Auth.authenticate(event);
            if (userInfo == null) {
                logger.error("Unauthorized access attempt to path {} is rejected", event.path());
                event.reject(HttpResponseStatus.FORBIDDEN.code());
                return;
            }
            WebSocketManager wsm = WebSocketManager.getManager();
            wsm.addUserSession(userInfo, event);
            event.textMessageHandler(msg -> {
                MessageHandlerFactory factory = null;
                try {
                    factory = new MessageHandlerFactory(msg);
                    var handler = factory.createMessageHandler();
                    var message = new Gson().fromJson(msg, ClientMessage.class);
                    if ((handler == null) || (message == null)) {
                        throw new IOException(String.format("Message can't be recognized: %s", msg));
                    } else {
                        var response = handler.handleMessage(event, message);
                        event.writeTextMessage(new Gson().toJson(response));
                    }
                } catch (Exception e) {
                    event.writeTextMessage(new ErrorResponseMessage(e.getMessage()).toJson());
                }
            });
            event.closeHandler(ignore -> wsm.removeSession(event));
        });
    }

    private void createRouteHandlers(Router router, WebHandler handler) {
        Route route = createRoute(router, handler);
        logger.debug("Create handler for {}", handler.toString());
        if (handler.isBlocking()) {
            createBlockHandler(handler, route);
        } else {
            createNonBlockHandler(handler, route);
        }
    }

    @SuppressWarnings("unchecked")
    private void createBlockHandler(WebHandler handler, Route route) {
        if (WebInterface.class.isAssignableFrom(handler.getInstanceClass())) {
            route.blockingHandler(context -> {
                try {
                    Object instance = handler.getInstanceClass().getDeclaredConstructor().newInstance();
                    WebInterface webInterface = (WebInterface) instance;
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
                    Object instance = handler.getInstanceClass().getDeclaredConstructor().newInstance();
                    handler.getFunction().invoke(instance, context);
                    context.next();
                } catch (NoSuchMethodException | InstantiationException |
                        IllegalAccessException | InvocationTargetException e) {
                    context.fail(e);
                }
            }).blockingHandler(logHandler);
        }
    }

    @SuppressWarnings("unchecked")
    private void createNonBlockHandler(WebHandler handler, Route route) {
        if (WebInterface.class.isAssignableFrom(handler.getInstanceClass())) {
            route.handler(context -> {
                try {
                    Object instance = handler.getInstanceClass().getDeclaredConstructor().newInstance();
                    WebInterface webInterface = (WebInterface) instance;
                    WebInterface.Handler theHandler = webInterface.requestHandlers().get(handler.getMethod());
                    theHandler.handle(context);
                    context.next();
                } catch (Throwable e) {
                    context.fail(e);
                }
            }).handler(logHandler);
        } else {
            route.handler(context -> {
                try {
                    Object instance = handler.getInstanceClass().getDeclaredConstructor().newInstance();
                    handler.getFunction().invoke(instance, context);
                    context.next();
                } catch (Throwable e) {
                    context.fail(e);
                }
            }).handler(logHandler);
        }
    }

    private final Handler<RoutingContext> logHandler = context -> {
        logger.info("{} {} from {}\t{}", context.request().method().name(), context.request().path(),
                context.request().remoteAddress().host(), context.response().getStatusCode());
        if (!context.response().ended()) {
            context.response().end();
        }
    };

    private void createAuthenticateHandlers(Router router, WebHandler handler) {
        createRoute(router, handler).handler(context -> {
            if (handler.getAuthenticatorTypes().stream().noneMatch(authType -> {
                Authenticator authenticator = AuthenticatorFactory.createAuthenticator(authType);
                return (authenticator != null) && authenticator.authenticate(context);
            })) {
                logger.error("Authentication failed for {} {}", handler.getMethod().name(), handler.getPath());
                ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "Not authenticated");
            } else {
                logger.debug("authentication passed for {} {}, execute next handler",
                        handler.getMethod().name(), handler.getPath());
                context.next();
            }
        });
    }

    private void createCrossOriginRouteHandlers(Router router, WebHandler handler) {
        if (webConfig.isAllowCrossOriginResourceSharing()) {
            String pattern = String.join("|", webConfig.getCrossOriginSources());
            List<String> headers = getAllowedHeaders(handler);
            createRoute(router, handler)
                    .handler(createCrossOriginHandler(List.of(handler.getMethod()), headers, pattern));
        }
    }

    private CorsHandler createCrossOriginHandler(List<HttpMethod> methods, List<String> headers, String pattern) {
        Set<String> allowHeaders = new HashSet<>();
        allowHeaders.addAll(headers);
        allowHeaders.addAll(List.of(
                "Content-Type",
                "Access-Control-Allow-Method",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));
        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.OPTIONS);
        allowedMethods.addAll(methods);
        logger.info("CORS allow: {} {} {}", allowedMethods, allowHeaders, pattern);
        return CorsHandler.create(pattern).allowedMethods(allowedMethods).allowedHeaders(allowHeaders)
                .allowCredentials(true);
    }

    private Route createRoute(Router router, WebHandler handler) {
        return handler.isPathAsRegex() ? router.routeWithRegex(handler.getMethod(), handler.getPath()) :
                router.route(handler.getMethod(), handler.getPath());
    }

    private List<String> getAllowedHeaders(WebHandler handler) {
        List<String> headers = new ArrayList<>();
        for (AuthType authType : handler.getAuthenticatorTypes()) {
            Authenticator authenticator = AuthenticatorFactory.createAuthenticator(authType);
            assert authenticator != null;
            headers.addAll(Arrays.stream(authenticator.getHeaders()).collect(Collectors.toList()));
        }
        headers.addAll(handler.getHeaders());
        return headers;
    }

    private void loadAnnotatedWebHandlers(String packageName) throws CoVaException {
        logger.info("searching WebEndPoint annotated methods in {}", packageName);
        Reflections reflections = new Reflections(packageName, new MethodAnnotationsScanner());
        Set<Method> methods = reflections.getMethodsAnnotatedWith(WebEndPoint.class);
        for (Method method : methods) {
            Class<?> clz = method.getDeclaringClass();
            if ((packageName != null) && !clz.getPackageName().startsWith(packageName)) {
                continue;
            }
            if ((method.getModifiers() & Modifier.PUBLIC) == 0) {
                logger.error("Annotated web handler {}.{} is not public",
                        clz.getPackageName(), method.getName());
            }
            Class<?>[] paramTypes = method.getParameterTypes();
            if ((paramTypes.length < 1) || !paramTypes[0].isAssignableFrom(RoutingContext.class)) {
                logger.error("Annotated web handler {}.{} is not accepting RoutingContext",
                        clz.getPackageName(), method.getName());
            }
            WebEndPoint endPoint = method.getAnnotation(WebEndPoint.class);
            for (HttpMethod httpMethod : endPoint.methods()) {
                WebHandler handler = new WebHandler();
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
                logger.info("Annotated web handler found for {} {}", httpMethod.name(), endPoint.path());
            }
        }
    }

    private void loadWebInterfaces(String packageName) throws CoVaException {
        logger.info("searching WebInterface descendants in {}", packageName);
        Reflections reflections = new Reflections(packageName, new SubTypesScanner());
        Set<Class<? extends WebInterface>> types = reflections.getSubTypesOf(WebInterface.class);
        for (Class<? extends WebInterface> clz : types) {
            if ((packageName != null) && !clz.getPackageName().startsWith(packageName)) {
                continue;
            }
            try {
                WebInterface instance = clz.getDeclaredConstructor().newInstance();
                for (Map.Entry<HttpMethod, WebInterface.Handler> entry : instance.requestHandlers().entrySet()) {
                    WebHandler handler = new WebHandler();
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
                    logger.info("WebInterface descendant web handler found for {} {}",
                            entry.getKey().name(), instance.getPath());
                }
            } catch (NoSuchMethodException e) {
                logger.error("WebInterface descendant {} has no default constructor", clz.getCanonicalName());
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                logger.error("WebInterface descendant {} creation error: {}",
                        clz.getCanonicalName(), e.getMessage());
            }
        }
    }

    private void checkDuplicateHandler(WebHandler handler, Map<String, WebHandler> map) throws CoVaException {
        if (map.containsKey(handler.getKey())) {
            generateDuplicateException(handler, map.get(handler.getKey()));
        }
        if (handler.isPathAsRegex()) {
            Pattern pattern = Pattern.compile(handler.getPath());
            List<WebHandler> nonRegexHandlers = map.values().stream().filter(entry ->
                    !entry.getMethod().equals(handler.getMethod()) && !entry.isPathAsRegex())
                    .collect(Collectors.toList());
            for (WebHandler exHandler : nonRegexHandlers) {
                if (pattern.matcher(exHandler.getPath()).find()) {
                    generateDuplicateException(handler, exHandler);
                }
            }
        } else {
            List<WebHandler> regexHandlers = map.values().stream().filter(webHandler -> webHandler.getMethod().equals(handler.getMethod()) &&
                    webHandler.isPathAsRegex()).collect(Collectors.toList());
            for (WebHandler exHandler : regexHandlers) {
                Pattern p = Pattern.compile(exHandler.getPath());
                if (p.matcher(handler.getPath()).find()) {
                    generateDuplicateException(handler, exHandler);
                }
            }
        }
    }

    private void generateDuplicateException(WebHandler handler1, WebHandler handler2) throws CoVaException {
        String error = String.format("duplicated handler for %s [%s, %s] between %s.%s and %s.%s",
                handler1.getMethod().name(), handler1.getPath(), handler2.getPath(),
                handler1.getInstanceClass().getCanonicalName(), handler1.getFunction().getName(),
                handler2.getInstanceClass(), handler2.getFunction().getName());
        logger.error(error);
        throw new CoVaException(error);
    }
}
