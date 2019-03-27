package com.cyecize.summer.areas.routing.services;

import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.models.annotationModels.ActionAnnotationHandlerContainer;
import com.cyecize.summer.areas.routing.models.annotationModels.AnnotationExtractedValue;
import com.cyecize.summer.areas.routing.utils.PathFormatter;
import com.cyecize.summer.common.annotations.routing.ExceptionListener;
import com.cyecize.summer.common.annotations.routing.GetMapping;
import com.cyecize.summer.common.annotations.routing.PostMapping;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ActionMethodScanningServiceImpl implements ActionMethodScanningService {

    private static final String GET = "GET";

    private static final String POST = "POST";

    private static final String EXCEPTION = "EXCEPTION";

    private static final List<ActionAnnotationHandlerContainer> methodAnnotationHandlers = new ArrayList<>();

    /*
      Initialize the supported routing annotations.
     */
    static {
        methodAnnotationHandlers.add(new ActionAnnotationHandlerContainer<>(GetMapping.class,
                (annotation -> new AnnotationExtractedValue(GET, annotation.produces(), annotation.value()))));

        methodAnnotationHandlers.add(new ActionAnnotationHandlerContainer<>(PostMapping.class,
                (annotation -> new AnnotationExtractedValue(POST, annotation.produces(), annotation.value()))));

        methodAnnotationHandlers.add(new ActionAnnotationHandlerContainer<>(ExceptionListener.class,
                (annotation -> new AnnotationExtractedValue(EXCEPTION, annotation.produces(), UUID.randomUUID().toString()))));

    }

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
     * If a proper annotation is present, adds the method to a map of action methods
     * where the key is the http method.
     */
    private void loadActionMethodsFromController(Class<?> controllerClass) {
        Arrays.stream(controllerClass.getDeclaredMethods()).forEach(m -> {
            m.setAccessible(true);

            AnnotationExtractedValue annotationExtractedValue = this.findRoutingAnnotation(m);

            if (annotationExtractedValue != null) {
                ActionMethod actionMethod = new ActionMethod(annotationExtractedValue.getPattern(), m, annotationExtractedValue.getContentType(), controllerClass);
                this.actionsByHttpMethod.get(annotationExtractedValue.getHttpMethod()).add(actionMethod);
            }
        });
    }

    /**
     * Gets all annotations, looks for one of the supported routing annotations and gets its value
     *
     * @param method - the method that will be scanned
     * @return null if no annotation is found
     */
    @SuppressWarnings("unchecked")
    private AnnotationExtractedValue findRoutingAnnotation(Method method) {
        for (ActionAnnotationHandlerContainer methodAnnotationHandler : methodAnnotationHandlers) {
            Class<? extends Annotation> annotationType = methodAnnotationHandler.getAnnotationType();

            if (method.isAnnotationPresent(annotationType)) {
                return methodAnnotationHandler.getAnnotationValue(method.getAnnotation(annotationType));
            }
        }

        return null;
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
