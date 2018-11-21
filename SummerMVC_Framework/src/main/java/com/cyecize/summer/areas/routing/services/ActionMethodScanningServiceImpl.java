package com.cyecize.summer.areas.routing.services;

import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.utils.PathFormatter;
import com.cyecize.summer.common.annotations.routing.ExceptionListener;
import com.cyecize.summer.common.annotations.routing.GetMapping;
import com.cyecize.summer.common.annotations.routing.PostMapping;

import java.lang.reflect.Method;
import java.util.*;

public class ActionMethodScanningServiceImpl implements ActionMethodScanningService {

    private static final String CANNOT_PARSE_ROUTE_FORMAT = "Cannot parse route for method \"%s\".";

    private static final String GET = "GET";

    private static final String POST = "POST";

    private static final String EXCEPTION = "EXCEPTION";

    private final PathFormatter pathFormatter;

    private Map<String, Set<ActionMethod>> actionsByHttpMethod;

    public ActionMethodScanningServiceImpl(PathFormatter pathFormatter) {
        this.pathFormatter = pathFormatter;
        this.actionsByHttpMethod = new HashMap<>();
        this.actionsByHttpMethod.put(GET, new HashSet<>());
        this.actionsByHttpMethod.put(POST, new HashSet<>());
        this.actionsByHttpMethod.put(EXCEPTION, new HashSet<>());
    }

    @Override
    public Map<String, Set<ActionMethod>> findActionMethods(Map<Class<?>, Object> controllers) {
        for (Class<?> controllerType : controllers.keySet()) {
            this.loadActionMethodsFromController(controllerType);
        }
        return this.actionsByHttpMethod;
    }

    private void loadActionMethodsFromController(Class<?> controllerClass) {
        Set<Method> getMethods = new HashSet<>();
        Set<Method> postMethods = new HashSet<>();
        Set<Method> exceptionListeners = new HashSet<>();
        Arrays.stream(controllerClass.getMethods()).forEach(m -> {
            if (m.isAnnotationPresent(GetMapping.class)) {
                getMethods.add(m);
            }
            if (m.isAnnotationPresent(PostMapping.class)) {
                postMethods.add(m);
            }
            if (m.isAnnotationPresent(ExceptionListener.class)) {
                exceptionListeners.add(m);
            }
        });
        getMethods.forEach(m -> this.loadMethod(m, controllerClass, GET));
        postMethods.forEach(m -> this.loadMethod(m, controllerClass, POST));
        exceptionListeners.forEach(m -> this.loadMethod(m, controllerClass, EXCEPTION));
    }

    private void loadMethod(Method method, Class<?> controller, String httpMethod) {
        String pathPattern;
        switch (httpMethod) {
            case GET:
                pathPattern = this.pathFormatter.formatPath(method.getAnnotation(GetMapping.class).value());
                break;
            case POST:
                pathPattern = this.pathFormatter.formatPath(method.getAnnotation(PostMapping.class).value());
                break;
            case EXCEPTION:
                pathPattern = UUID.randomUUID().toString();
                break;
            default:
                throw new RuntimeException(String.format(CANNOT_PARSE_ROUTE_FORMAT, method.getName()));
        }
        ActionMethod actionMethod = new ActionMethod(pathPattern, method, controller);
        this.actionsByHttpMethod.get(httpMethod).add(actionMethod);
    }
}
