package com.cyecize.summer.areas.routing.services;

import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.utils.PathFormatter;
import com.cyecize.summer.common.annotations.routing.ExceptionListener;
import com.cyecize.summer.common.annotations.routing.GetMapping;
import com.cyecize.summer.common.annotations.routing.PostMapping;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

        this.orderExceptionsByHierarchy();
        this.orderActionMethods(GET);
        this.orderActionMethods(POST);
        return this.actionsByHttpMethod;
    }

    /**
     * Iterates controller type and finds all methods with a given annotation.
     * Then ads the method to a set of methods with the same annotation.
     * Finally loads all located methods, again by annotationType.
     */
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

    /**
     * Checks if method is GET or POST and gets the annotation value.
     * If no annotation is present (the case with @ExceptionListener), generate a random route.
     * Create a new actionMethod and add it to the actionsMap.
     */
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

    /**
     * Orders exceptions by hierarchy in DESC order so that exceptions with high priority are accessed last
     * which will prevent ExceptionListeners from overriding each other.
     */
    private void orderExceptionsByHierarchy() {
        this.actionsByHttpMethod.put(EXCEPTION, this.actionsByHttpMethod.get(EXCEPTION).stream().sorted(ActionMethod::compareTo)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    /**
     * Orders action methods by absence of @PathVariable in asc, then by alphabetic order
     * which will prevent @PathVariable methods from matching constant routes.
     */
    private void orderActionMethods(String method) {
        final Pattern pathVarPattern = Pattern.compile("\\(\\?<.*?>\\[a-zA-Z0-9_-\\]\\+\\)");
        this.actionsByHttpMethod.put(method, this.actionsByHttpMethod.get(method).stream().sorted((m1, m2) -> {
            boolean m1Match = pathVarPattern.matcher(m1.getPattern()).find();
            boolean m2Match = pathVarPattern.matcher(m2.getPattern()).find();

            if (m1Match && !m2Match) {
                return 1;
            }
            if (m2Match && !m1Match) {
                return -1;
            }

            return m1.getPattern().compareTo(m2.getPattern());
        }).collect(Collectors.toCollection(LinkedHashSet::new)));
    }

}
