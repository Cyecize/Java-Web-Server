package com.cyecize.summer.areas.routing.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.areas.routing.exceptions.ActionInvocationException;
import com.cyecize.summer.areas.routing.exceptions.HttpNotFoundException;
import com.cyecize.summer.areas.routing.exceptions.UnsatisfiedPathVariableParamException;
import com.cyecize.summer.areas.routing.exceptions.UnsatisfiedRequestParamException;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.utils.PrimitiveTypeDataResolver;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.validation.annotations.ConvertedBy;
import com.cyecize.summer.areas.validation.annotations.Valid;
import com.cyecize.summer.areas.validation.interfaces.BindingResult;
import com.cyecize.summer.areas.validation.interfaces.DataAdapter;
import com.cyecize.summer.areas.validation.services.DataAdapterStorageService;
import com.cyecize.summer.areas.validation.services.ObjectBindingService;
import com.cyecize.summer.areas.validation.services.ObjectValidationService;
import com.cyecize.summer.common.annotations.Controller;
import com.cyecize.summer.common.annotations.routing.ExceptionListener;
import com.cyecize.summer.common.annotations.routing.PathVariable;
import com.cyecize.summer.common.annotations.routing.RequestParam;
import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
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

    private Map<Class<?>, Object> controllers;

    private PrimitiveTypeDataResolver dataResolver;

    private HttpSoletRequest currentRequest;

    public ActionMethodInvokingServiceImpl(DependencyContainer dependencyContainer, ObjectBindingService bindingService,
                                           ObjectValidationService validationService, DataAdapterStorageService dataAdapters,
                                           Map<String, Set<ActionMethod>> actionMethods, Map<Class<?>, Object> controllers) {

        this.dependencyContainer = dependencyContainer;
        this.bindingService = bindingService;
        this.validationService = validationService;
        this.dataAdapters = dataAdapters;
        this.actionMethods = actionMethods;
        this.controllers = controllers;
        this.dataResolver = new PrimitiveTypeDataResolver();
    }

    /**
     * Searches and returns action method that matches the request route.
     * If not matching method is not found and request is not resource, throw HttpNotFoundException
     */
    @Override
    public ActionMethod findAction(HttpSoletRequest request) throws HttpNotFoundException {
        this.currentRequest = request;
        ActionMethod actionMethod = this.findActionMethod();

        if (actionMethod == null) {
            if (!this.currentRequest.isResource()) {
                throw new HttpNotFoundException(this.currentRequest.getRequestURL());
            }
        }

        return actionMethod;
    }

    /**
     * Extracts path variable from actionMethod, invokes action method
     * and returns new ActionInvokeResult.
     */
    @Override
    public ActionInvokeResult invokeMethod(ActionMethod actionMethod) {
        Map<String, Object> pathVariables = this.getPathVariables(actionMethod);
        Object methodResult = this.invokeAction(actionMethod, pathVariables);

        this.currentRequest = null;
        return new ActionInvokeResult(actionMethod, methodResult, actionMethod.getContentType());
    }

    /**
     * Adds exception with all parents to the dependencyContainer, then searches for exception listeners.
     * If listener is found, invoke the method, else return null.
     */
    @Override
    public ActionInvokeResult invokeMethod(Exception ex) {
        this.currentRequest = this.dependencyContainer.getObject(HttpSoletRequest.class);
        List<Throwable> exceptionStack = this.getExceptionStack(ex);
        exceptionStack.forEach(this.dependencyContainer::addPlatformBean);

        ActionMethod actionMethod = this.findActionMethod(exceptionStack);
        if (actionMethod == null) {
            return null;
        }

        Object methodResult = this.invokeAction(actionMethod, new HashMap<>());
        this.currentRequest = null;

        return new ActionInvokeResult(actionMethod, methodResult, actionMethod.getContentType());
    }

    /**
     * Get the controller for the actionMethod, reload if lifeSpan is REQUEST.
     * Collect method parameters.
     * Invoke method and return result, or throw ActionInvocationException if
     * errors occur.
     */
    private Object invokeAction(ActionMethod actionMethod, Map<String, Object> pathVariables) {
        Object controller = this.controllers.entrySet().stream()
                .filter((kvp) -> actionMethod.getControllerClass().isAssignableFrom(kvp.getKey()))
                .findFirst().orElse(null).getValue(); //never null

        if (controller.getClass().getAnnotation(Controller.class).lifeSpan() == ServiceLifeSpan.REQUEST) {
            controller = this.dependencyContainer.reloadComponent(controller);
        }

        Object[] methodParams = this.getMethodParameters(actionMethod, pathVariables);
        try {
            actionMethod.getMethod().setAccessible(true);
            return actionMethod.getMethod().invoke(controller, methodParams);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new ActionInvocationException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Finds action method requested params by looking in the platform beans.
     * If @RequestParam is present, it looks for query/body parameter.
     * If @PathVariable is present, it looks in the pathVariables.
     * If the object is not found, then it is considered to be a bindingModel.
     * If the object is bindingModel, it is populated and validated if needed.
     */
    private Object[] getMethodParameters(ActionMethod actionMethod, Map<String, Object> pathVariables) {
        Parameter[] parameters = actionMethod.getMethod().getParameters();
        Object[] parameterInstances = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (parameter.isAnnotationPresent(RequestParam.class)) {
                parameterInstances[i] = this.handleRequestParam(parameter, parameter.getAnnotation(RequestParam.class));
                continue;
            }

            if (parameter.isAnnotationPresent(PathVariable.class)) {
                parameterInstances[i] = pathVariables.get(parameter.getName());
                continue;
            }

            for (Object platformBean : this.dependencyContainer.getPlatformBeans()) {
                if (parameter.getType().isAssignableFrom(platformBean.getClass())) {
                    parameterInstances[i] = platformBean;
                    break;
                }
            }

            if (parameterInstances[i] != null) continue;

            try {
                Object instanceOfBindingModel = parameter.getType().getConstructor().newInstance();
                this.bindingService.populateBindingModel(instanceOfBindingModel);
                if (parameter.isAnnotationPresent(Valid.class)) {
                    this.validationService.validateBindingModel(instanceOfBindingModel, this.dependencyContainer.getObject(BindingResult.class));
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
        String paramName = requestParam.value();

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
                DataAdapter dataAdapter = this.dataAdapters.getDataAdapter(parameter.getAnnotation(ConvertedBy.class).value());

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
                return null;
            }
        }

        return resultValue;
    }

    /**
     * Get matcher for the given action method.
     * Find all parameters with @PathVariable annotation and get the value from
     * the matcher where the group name is the PathVariable.value()
     * Then resolve that value with dataResolver to any primitive type
     * or use a custom data adapter if @ConvertedBy annotation is present.
     *
     * @throws UnsatisfiedPathVariableParamException if the there is no value for a given @PathVariable
     *                                               and that value is required.
     */
    private Map<String, Object> getPathVariables(ActionMethod actionMethod) {
        Map<String, Object> pathVariables = new HashMap<>();
        Pattern routePattern = Pattern.compile(actionMethod.getPattern());
        Matcher routeMatcher = routePattern.matcher(this.currentRequest.getRelativeRequestURL());
        routeMatcher.find(); //always true

        Arrays.stream(actionMethod.getMethod().getParameters()).forEach(p -> {
            if (p.isAnnotationPresent(PathVariable.class)) {
                PathVariable pathVariable = p.getAnnotation(PathVariable.class);

                String paramName = pathVariable.value();
                String paramValue = routeMatcher.group(paramName);

                Object pathVariableValue = null;
                if (p.isAnnotationPresent(ConvertedBy.class)) {
                    DataAdapter dataAdapter = this.dataAdapters.getDataAdapter(p.getAnnotation(ConvertedBy.class).value());

                    if (dataAdapter != null) {
                        //add to request so that custom data adapter can pick it up.
                        this.currentRequest.addBodyParameter(paramName, paramValue);
                        pathVariableValue = dataAdapter.resolve(paramName, this.currentRequest);
                    }

                } else {
                    pathVariableValue = this.dataResolver.resolve(p.getType(), paramValue);
                }

                if (pathVariableValue == null && pathVariable.required()) {
                    throw new UnsatisfiedPathVariableParamException(actionMethod, pathVariable);
                }

                pathVariables.put(p.getName(), pathVariableValue);
            }
        });

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
        List<Throwable> thList = new ArrayList<>();
        if (ex == null) {
            return thList;
        }

        thList.add(ex);
        thList.addAll(this.getExceptionStack(ex.getCause()));

        return thList;
    }
}
