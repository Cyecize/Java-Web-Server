package com.cyecize.summer.areas.routing.services;

import com.cyecize.ioc.annotations.Qualifier;
import com.cyecize.ioc.utils.AliasFinder;
import com.cyecize.ioc.utils.AnnotationUtils;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.areas.routing.exceptions.ActionInvocationException;
import com.cyecize.summer.areas.routing.exceptions.HttpNotFoundException;
import com.cyecize.summer.areas.routing.exceptions.UnsatisfiedPathVariableParamException;
import com.cyecize.summer.areas.routing.exceptions.UnsatisfiedRequestParamException;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.utils.PrimitiveTypeDataResolver;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.validation.annotations.ConvertedBy;
import com.cyecize.summer.areas.validation.annotations.Valid;
import com.cyecize.summer.areas.validation.interfaces.BindingResult;
import com.cyecize.summer.areas.validation.interfaces.DataAdapter;
import com.cyecize.summer.areas.validation.services.DataAdapterStorageService;
import com.cyecize.summer.areas.validation.services.ObjectBindingService;
import com.cyecize.summer.areas.validation.services.ObjectValidationService;
import com.cyecize.summer.common.annotations.routing.ExceptionListener;
import com.cyecize.summer.common.annotations.routing.PathVariable;
import com.cyecize.summer.common.annotations.routing.RequestParam;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionMethodInvokingServiceImpl implements ActionMethodInvokingService {

    private static final String CANNOT_INSTANTIATE_CLASS_FORMAT = "Cannot create an instance of class \"%s\" because it relies on dependencies.";

    private static final String EXCEPTION = "EXCEPTION";

    private final DependencyContainer dependencyContainer;

    private final ObjectBindingService bindingService;

    private final ObjectValidationService validationService;

    private final DataAdapterStorageService dataAdapters;

    private Map<String, Set<ActionMethod>> actionMethods;

    private PrimitiveTypeDataResolver dataResolver;

    private HttpSoletRequest currentRequest;

    public ActionMethodInvokingServiceImpl(DependencyContainer dependencyContainer, ObjectBindingService bindingService,
                                           ObjectValidationService validationService, DataAdapterStorageService dataAdapters,
                                           Map<String, Set<ActionMethod>> actionMethods) {

        this.dependencyContainer = dependencyContainer;
        this.bindingService = bindingService;
        this.validationService = validationService;
        this.dataAdapters = dataAdapters;
        this.actionMethods = actionMethods;
        this.dataResolver = new PrimitiveTypeDataResolver();
    }

    /**
     * Searches and returns action method that matches the request route.
     * If no matching method is found and request is not a resource, throw {@link HttpNotFoundException}
     */
    @Override
    public ActionMethod findAction(HttpSoletRequest request) throws HttpNotFoundException {
        this.currentRequest = request;
        final ActionMethod actionMethod = this.findActionMethod();

        if (actionMethod == null) {
            if (!this.currentRequest.isResource()) {
                throw new HttpNotFoundException(this.currentRequest.getRequestURL());
            }
        }

        return actionMethod;
    }

    /**
     * Extracts path variable from actionMethod, invokes action method
     * and returns new {@link ActionInvokeResult}.
     */
    @Override
    public ActionInvokeResult invokeMethod(ActionMethod actionMethod) {
        final Map<String, Object> pathVariables = this.getPathVariables(actionMethod);
        final Object methodResult = this.invokeAction(actionMethod, pathVariables);

        this.currentRequest = null;
        return new ActionInvokeResult(actionMethod, methodResult, actionMethod.getContentType());
    }

    /**
     * Adds exception with all parents to the dependencyContainer, then searches for exception listeners.
     * If listener is found, invoke the method, else return null.
     */
    @Override
    public ActionInvokeResult invokeMethod(Exception ex) {
        this.currentRequest = this.dependencyContainer.getService(HttpSoletRequest.class);
        final List<Throwable> exceptionStack = this.getExceptionStack(ex);
        exceptionStack.forEach(this.dependencyContainer::addFlashService);

        final ActionMethod actionMethod = this.findActionMethod(exceptionStack);
        if (actionMethod == null) {
            return null;
        }

        final Object methodResult = this.invokeAction(actionMethod, new HashMap<>());
        this.currentRequest = null;

        return new ActionInvokeResult(actionMethod, methodResult, actionMethod.getContentType());
    }

    /**
     * Get the controller for the actionMethod.
     * Collect method parameters.
     * Invoke method and return result, or throw ActionInvocationException if
     * errors occur.
     */
    private Object invokeAction(ActionMethod actionMethod, Map<String, Object> pathVariables) {
        final Object controllerInstance = actionMethod.getController().getInstance();

        try {
            final Object[] methodParams = this.getMethodParameters(actionMethod, pathVariables);
            actionMethod.getMethod().setAccessible(true);
            return actionMethod.getMethod().invoke(controllerInstance, methodParams);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ActionInvocationException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Finds action method requested params by looking in the platform beans.
     * If {@link RequestParam} is present, it looks for query/body parameter.
     * If {@link PathVariable} is present, it looks in the pathVariables.
     * If the object is not found, then it is considered to be a service.
     * If the object is not a service, it is considered a binding model and it is populated and validated if needed.
     */
    private Object[] getMethodParameters(ActionMethod actionMethod, Map<String, Object> pathVariables) {
        final Parameter[] parameters = actionMethod.getMethod().getParameters();
        final Object[] parameterInstances = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(RequestParam.class)) {
                parameterInstances[i] = this.handleRequestParam(parameter, parameter.getAnnotation(RequestParam.class));
                continue;
            }

            if (parameter.isAnnotationPresent(PathVariable.class)) { //TODO: use pathVariable's val
                parameterInstances[i] = pathVariables.get(parameter.getName());
                continue;
            }

            if (AliasFinder.isAnnotationPresent(parameter.getAnnotations(), Qualifier.class)) {
                final String qualifier = AnnotationUtils.getAnnotationValue(
                        AliasFinder.getAnnotation(parameter.getAnnotations(), Qualifier.class)
                ).toString();

                parameterInstances[i] = this.dependencyContainer.getService(parameter.getType(), qualifier);
            } else {
                parameterInstances[i] = this.dependencyContainer.getService(parameter.getType());
            }

            if (parameterInstances[i] != null) continue;

            parameterInstances[i] = this.dependencyContainer.getFlashService(parameter.getType());
            if (parameterInstances[i] != null) continue;

            try {
                final Object instanceOfBindingModel = parameter.getType().getConstructor().newInstance();
                this.bindingService.populateBindingModel(instanceOfBindingModel);
                if (parameter.isAnnotationPresent(Valid.class)) {
                    this.validationService.validateBindingModel(
                            instanceOfBindingModel,
                            this.dependencyContainer.getService(BindingResult.class)
                    );
                }

                parameterInstances[i] = instanceOfBindingModel;
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException cause) {
                throw new RuntimeException(String.format(CANNOT_INSTANTIATE_CLASS_FORMAT, parameter.getType().getName()), cause);
            }
        }

        return parameterInstances;
    }

    /**
     * Looks for a parameter in query parameters then in Body parameters.
     * If a key is present, resolve the value to the desired data type and return the result.
     *
     * @throws UnsatisfiedRequestParamException if the request param value is missing and the value is required.
     */
    private Object handleRequestParam(Parameter parameter, RequestParam requestParam) {
        final String paramName = requestParam.value();

        Map.Entry<String, String> matchingKeyValuePair = this.currentRequest.getQueryParameters().entrySet().stream()
                .filter(kvp -> kvp.getKey().equals(paramName))
                .findFirst().orElse(null);

        if (matchingKeyValuePair == null && this.currentRequest.getBodyParameters() != null) {
            matchingKeyValuePair = this.currentRequest.getBodyParameters().entrySet().stream()
                    .filter(kvp -> kvp.getKey().equals(paramName))
                    .findFirst().orElse(null);
        }

        Object resultValue = null;

        if (matchingKeyValuePair != null) {
            if (parameter.isAnnotationPresent(ConvertedBy.class)) {
                final DataAdapter dataAdapter = this.dataAdapters.getDataAdapter(parameter.getAnnotation(ConvertedBy.class).value());

                if (dataAdapter != null) {
                    resultValue = dataAdapter.resolve(paramName, this.currentRequest);
                }

            } else {
                resultValue = this.dataResolver.resolve(parameter.getType(), matchingKeyValuePair.getValue());
            }
        }

        if (resultValue == null) {
            if (requestParam.required()) {
                throw new UnsatisfiedRequestParamException(requestParam.value());
            } else {
                return this.dataResolver.defaultValue(parameter.getType());
            }
        }

        return resultValue;
    }

    /**
     * Get matcher for the given action method.
     * Find all parameters with {@link PathVariable} annotation and get the value from
     * the matcher where the group name is the PathVariable.value()
     * Then resolve that value with dataResolver to any primitive type
     * or use a custom data adapter if {@link ConvertedBy} annotation is present.
     *
     * @throws UnsatisfiedPathVariableParamException if the there is no value for a given {@link PathVariable}
     *                                               and that value is required.
     */
    private Map<String, Object> getPathVariables(ActionMethod actionMethod) {
        final Map<String, Object> pathVariables = new HashMap<>();
        final Pattern routePattern = Pattern.compile(actionMethod.getPattern());
        final Matcher routeMatcher = routePattern.matcher(this.currentRequest.getRelativeRequestURL());
        routeMatcher.find(); //always true

        for (Parameter param : actionMethod.getMethod().getParameters()) {
            if (param.isAnnotationPresent(PathVariable.class)) {
                final PathVariable pathVariable = param.getAnnotation(PathVariable.class);

                final String pathParamName = pathVariable.value();
                final String paramValue = routeMatcher.group(pathParamName);

                Object pathVariableValue = null;
                if (param.isAnnotationPresent(ConvertedBy.class)) {
                    final DataAdapter dataAdapter = this.dataAdapters.getDataAdapter(
                            param.getAnnotation(ConvertedBy.class).value()
                    );

                    if (dataAdapter != null) {
                        //add to request so that custom data adapter can pick it up.
                        this.currentRequest.addBodyParameter(pathParamName, paramValue);
                        pathVariableValue = dataAdapter.resolve(pathParamName, this.currentRequest);
                    }

                } else {
                    pathVariableValue = this.dataResolver.resolve(param.getType(), paramValue);
                }

                if (pathVariableValue == null && pathVariable.required()) {
                    throw new UnsatisfiedPathVariableParamException(actionMethod, pathVariable);
                }

                pathVariables.put(param.getName(), pathVariableValue);
            }
        }

        return pathVariables;
    }

    /**
     * Checks if the request method key is present in the actionMethods map.
     * Filters the action method whose url matches the request url.
     */
    private ActionMethod findActionMethod() {
        if (!this.actionMethods.containsKey(this.currentRequest.getMethod().toUpperCase())) {
            return null;
        }

        return this.actionMethods.get(this.currentRequest.getMethod().toUpperCase()).stream()
                .filter(action -> Pattern.matches(action.getPattern(), this.currentRequest.getRelativeRequestURL()))
                .findFirst().orElse(null);
    }

    /**
     * Iterates through every exception thrown and looks for assignable exception listener.
     * Exception listeners are sorted by Inheritance where the highest in the hierarchy are last
     * to avoid overriding exceptions as much as possible.
     */
    private ActionMethod findActionMethod(List<Throwable> exceptionStack) {
        if (!this.actionMethods.containsKey(EXCEPTION)) {
            return null;
        }

        for (ActionMethod exMethod : this.actionMethods.get(EXCEPTION)) {
            Class<?> actionExType = exMethod.getMethod().getAnnotation(ExceptionListener.class).value();
            for (Throwable throwable : exceptionStack) {
                if (actionExType.isAssignableFrom(throwable.getClass())) {
                    return exMethod;
                }
            }
        }

        return null;
    }

    /**
     * Simple recursive method to get all causes for an exception.
     * Used in case of the developer listening for a specific cause, no the currently thrown exception.
     */
    private List<Throwable> getExceptionStack(Throwable ex) {
        final List<Throwable> thList = new ArrayList<>();
        if (ex == null) {
            return thList;
        }

        thList.add(ex);
        thList.addAll(this.getExceptionStack(ex.getCause()));

        return thList;
    }
}
