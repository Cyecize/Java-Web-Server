package com.cyecize.summer.areas.routing.services;

import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.models.annotationModels.ActionAnnotationHandlerContainer;
import com.cyecize.summer.areas.routing.models.annotationModels.AnnotationExtractedValue;
import com.cyecize.summer.areas.routing.utils.PathFormatter;
import com.cyecize.summer.common.annotations.routing.ExceptionListener;
import com.cyecize.summer.common.annotations.routing.GetMapping;
import com.cyecize.summer.common.annotations.routing.HttpMethod;
import com.cyecize.summer.common.annotations.routing.PostMapping;
import com.cyecize.summer.common.annotations.routing.RequestMapping;
import com.cyecize.summer.constants.ContentTypes;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ActionMethodScanningServiceImpl implements ActionMethodScanningService {

    private static final String EXCEPTION = "EXCEPTION";

    private static final List<ActionAnnotationHandlerContainer> METHOD_ANNOTATION_HANDLERS = new ArrayList<>();

    /*
      Initialize the supported routing annotations.
     */
    static {
        METHOD_ANNOTATION_HANDLERS.add(new ActionAnnotationHandlerContainer<>(GetMapping.class,
                (annotation -> new AnnotationExtractedValue(List.of(HttpMethod.GET.name()), annotation.produces(), annotation.value()))));

        METHOD_ANNOTATION_HANDLERS.add(new ActionAnnotationHandlerContainer<>(PostMapping.class,
                (annotation -> new AnnotationExtractedValue(List.of(HttpMethod.POST.name()), annotation.produces(), annotation.value()))));

        METHOD_ANNOTATION_HANDLERS.add(new ActionAnnotationHandlerContainer<>(ExceptionListener.class,
                (annotation -> new AnnotationExtractedValue(List.of(EXCEPTION), annotation.produces(), UUID.randomUUID().toString()))));

        METHOD_ANNOTATION_HANDLERS.add(new ActionAnnotationHandlerContainer<>(RequestMapping.class,
                annotation -> new AnnotationExtractedValue(Arrays.stream(annotation.methods()).map(Enum::name).collect(Collectors.toList()), annotation.produces(), annotation.value())));

    }

    private final PathFormatter pathFormatter;

    private Map<String, Set<ActionMethod>> actionsByHttpMethod;

    public ActionMethodScanningServiceImpl(PathFormatter pathFormatter) {
        this.pathFormatter = pathFormatter;
        this.actionsByHttpMethod = new HashMap<>();
        this.actionsByHttpMethod.put(EXCEPTION, new HashSet<>());
        Arrays.stream(HttpMethod.values()).forEach(m -> this.actionsByHttpMethod.put(m.name(), new HashSet<>()));
    }

    @Override
    public Map<String, Set<ActionMethod>> findActionMethods(Collection<ServiceDetails> controllers) {
        for (ServiceDetails controller : controllers) {
            this.loadActionMethodsFromController(controller);
        }

        this.orderExceptionsByHierarchy();
        Arrays.stream(HttpMethod.values()).forEach(m -> this.orderActionMethods(m.name()));

        return this.actionsByHttpMethod;
    }

    /**
     * Iterates controller type and finds all methods with a given annotation.
     * If a proper annotation is present, adds the method to a map of action methods
     * where the key is the http method.
     * <p>
     * Applies base route and base content type if @RequestParam annotation is present.
     */
    private void loadActionMethodsFromController(ServiceDetails controller) {
        final Class<?> controllerClass = controller.getServiceType();

        String baseRoute = "";
        String baseContentType = ContentTypes.NONE;

        if (controllerClass.isAnnotationPresent(RequestMapping.class)) {
            RequestMapping annotation = controllerClass.getAnnotation(RequestMapping.class);
            baseRoute = annotation.value();
            baseContentType = annotation.produces();
        }


        for (Method method : controllerClass.getDeclaredMethods()) {
            method.setAccessible(true);

            final AnnotationExtractedValue annotationExtractedValue = this.findRoutingAnnotation(method);

            if (annotationExtractedValue != null) {
                final String pattern = this.pathFormatter.formatPath(baseRoute + annotationExtractedValue.getPattern());
                String contentType = ContentTypes.NONE.equals(annotationExtractedValue.getContentType()) ?
                        baseContentType : annotationExtractedValue.getContentType();

                if (ContentTypes.NONE.equals(contentType)) {
                    contentType = ContentTypes.TEXT_HTML;
                }

                final ActionMethod actionMethod = new ActionMethod(pattern, baseRoute, method, contentType, controller);

                for (String httpMethod : annotationExtractedValue.getHttpMethods()) {
                    this.actionsByHttpMethod.get(httpMethod).add(actionMethod);
                }
            }
        }
    }

    /**
     * Gets all annotations, looks for one of the supported routing annotations and gets its value
     *
     * @param method - the method that will be scanned
     * @return null if no annotation is found
     */
    @SuppressWarnings("unchecked")
    private AnnotationExtractedValue findRoutingAnnotation(Method method) {
        for (ActionAnnotationHandlerContainer methodAnnotationHandler : METHOD_ANNOTATION_HANDLERS) {
            final Class<? extends Annotation> annotationType = methodAnnotationHandler.getAnnotationType();

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
        this.actionsByHttpMethod.put(
                EXCEPTION,
                this.actionsByHttpMethod.get(EXCEPTION)
                        .stream()
                        .sorted(ActionMethod::compareTo)
                        .collect(Collectors.toCollection(LinkedHashSet::new))
        );
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
