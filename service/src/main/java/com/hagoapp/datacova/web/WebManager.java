package com.hagoapp.datacova.web;

import com.hagoapp.datacova.CoVaLogger;
import com.hagoapp.datacova.config.WebConfig;
import com.hagoapp.datacova.util.http.ResponseHelper;
import com.hagoapp.datacova.web.annotation.WebEndPoint;
import com.hagoapp.datacova.web.authentication.AuthType;
import com.hagoapp.datacova.web.authentication.Authenticator;
import com.hagoapp.datacova.web.authentication.AuthenticatorFactory;
import io.netty.handler.codec.http.HttpResponseStatus;
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
import org.apache.logging.log4j.Logger;
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Web server initialization manager.
 */
public class WebManager {

    private Logger logger;
    private WebConfig webConfig;
    private static final WebManager instance = new WebManager();

    public static WebManager getManager() {
        return instance;
    }

    public HttpServer createWebServer(WebConfig config) {
        return createWebServer(config, null);
    }

    public HttpServer createWebServer(WebConfig config, String packageName) {
        logger = CoVaLogger.getLogger();
        webConfig = config;
        VertxOptions options = new VertxOptions();
        options.getFileSystemOptions().setFileCacheDir(config.getTempDirectory());
        Vertx vx = Vertx.vertx(options);
        HttpServerOptions webOptions = new HttpServerOptions();
        HttpServer webServer = vx.createHttpServer(webOptions);
        Router router = findRouter(vx, packageName);
        webServer.requestHandler(router);
        webServer.listen(config.getPort(), config.getBindIp());
        return webServer;
    }

    private Router findRouter(Vertx vertx, String packageName) {
        Map<String, WebHandler> handlers = loadAnnotatedWebHandlers(packageName);
        handlers.putAll(loadWebInterfaces(packageName));
        Router router = Router.router(vertx);
        router.route().failureHandler(context -> {
            Throwable e = context.failure();
            logger.error("Unexpected server error: {}", e.getMessage());
            e.printStackTrace();
            ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR, e.getMessage(), Map.of(
                    "stackTrace", e.getStackTrace()
            ));
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
                    logger.info("{} {} from {}\t{}", handler.getMethod().name(), handler.getPath(),
                            context.request().remoteAddress().host(), context.response().getStatusCode());
                    if (!context.response().ended()) {
                        context.response().end();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                            "Unexpected Server Error", e.getMessage());
                    logger.info("{} {} from {}\t500", handler.getMethod().name(), handler.getPath(),
                            context.request().remoteAddress().host());
                }
            });
        } else {
            route.blockingHandler(context -> {
                try {
                    Object instance = handler.getInstanceClass().getDeclaredConstructor().newInstance();
                    handler.getFunction().invoke(instance, context);
                    logger.info("{} {} from {}\t{}", handler.getMethod().name(), handler.getPath(),
                            context.request().remoteAddress().host(), context.response().getStatusCode());
                    if (!context.response().ended()) {
                        context.response().end();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                            "Unexpected Server Error", e.getMessage());
                    logger.info("{} {} from {}\t500", handler.getMethod().name(), handler.getPath(),
                            context.request().remoteAddress().host());
                }
            });
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
                } catch (Throwable e) {
                    e.printStackTrace();
                    context.fail(e);
                }
            }).handler(context -> {
                logger.info("{} {} from {}\t{}", handler.getMethod().name(), handler.getPath(),
                        context.request().remoteAddress().host(), context.response().getStatusCode());
                if (!context.response().ended()) {
                    context.response().end();
                }
            }).failureHandler(context -> {
                ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        "Unexpected Server Error", context.failure().getMessage());
                logger.info("{} {} from {}\t500", handler.getMethod().name(), handler.getPath(),
                        context.request().remoteAddress().host());
            });
        } else {
            route.handler(context -> {
                try {
                    Object instance = handler.getInstanceClass().getDeclaredConstructor().newInstance();
                    handler.getFunction().invoke(instance, context);
                } catch (Throwable e) {
                    e.printStackTrace();
                    context.fail(e);
                }
            }).handler(context -> {
                logger.info("{} {} from {}\t{}", handler.getMethod().name(), handler.getPath(),
                        context.request().remoteAddress().host(), context.response().getStatusCode());
                if (!context.response().ended()) {
                    context.response().end();
                }
            }).failureHandler(context -> {
                ResponseHelper.respondError(context, HttpResponseStatus.INTERNAL_SERVER_ERROR,
                        "Unexpected Server Error", context.failure().getMessage());
                logger.info("{} {} from {}\t500", handler.getMethod().name(), handler.getPath(),
                        context.request().remoteAddress().host());
            });
        }
    }

    private void createAuthenticateHandlers(Router router, WebHandler handler) {
        createRoute(router, handler).handler(context -> {
            if (handler.getAuthenticatorTypes().stream().noneMatch(authType -> {
                Authenticator authenticator = AuthenticatorFactory.createAuthenticator(authType);
                return (authenticator != null) && authenticator.authenticate(context);
            })) {
                logger.error("Authentication failed for {} {}", handler.getMethod().name(), handler.getPath());
                ResponseHelper.respondError(context, HttpResponseStatus.FORBIDDEN, "Not authenticated");
            } else {
                logger.debug("authentication passed for {}, execute next handler", handler.getPath());
                context.next();
            }
        });
    }

    private void createCrossOriginRouteHandlers(Router router, WebHandler handler) {
        if (webConfig.isAllowCrossOriginResourceSharing() &&
                (List.of(HttpMethod.POST, HttpMethod.PUT).contains(handler.getMethod()))) {
            webConfig.getCrossOriginSources().forEach(origin -> {
                List<String> headers = getAllowedHeaders(handler);
                createRoute(router, handler).handler(
                        createCrossOriginHandler(List.of(handler.getMethod()), headers, origin));
            });
        }
    }

    private CorsHandler createCrossOriginHandler(List<HttpMethod> methods, List<String> allowHeaders, String pattern) {
        List<String> headers = new ArrayList<>();
        headers.addAll(allowHeaders);
        headers.addAll(List.of(
                "Content-Type",
                "Access-Control-Allow-Method",
                "Access-Control-Allow-Origin",
                "Access-Control-Allow-Credentials"
        ));
        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.OPTIONS);
        allowedMethods.addAll(methods);
        return CorsHandler.create(pattern).allowedHeaders(new HashSet<>(headers))
                .allowedMethods(allowedMethods);
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

    private Map<String, WebHandler> loadAnnotatedWebHandlers(String packageName) {
        logger.debug("searching annotated methods in {}", packageName);
        Reflections reflections = new Reflections(packageName, new MethodAnnotationsScanner());
        Set<Method> methods = reflections.getMethodsAnnotatedWith(WebEndPoint.class);
        Map<String, WebHandler> map = new HashMap<>();
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
                map.put(handler.getKey(), handler);
                logger.info("Annotated web handler found for {} {}", httpMethod.name(), endPoint.path());
            }
        }
        return map;
    }

    private Map<String, WebHandler> loadWebInterfaces(String packageName) {
        Reflections reflections = new Reflections(packageName, new SubTypesScanner());
        Set<Class<? extends WebInterface>> types = reflections.getSubTypesOf(WebInterface.class);
        Map<String, WebHandler> map = new HashMap<>();
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
                    map.put(handler.getKey(), handler);
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
        return map;
    }
}
