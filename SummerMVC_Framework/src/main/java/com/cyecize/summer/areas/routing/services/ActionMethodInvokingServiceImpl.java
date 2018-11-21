package com.cyecize.summer.areas.routing.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.utils.PrimitiveTypeDataResolver;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.common.annotations.Controller;
import com.cyecize.summer.common.annotations.routing.PathVariable;
import com.cyecize.summer.common.enums.ServiceLifeSpan;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionMethodInvokingServiceImpl implements ActionMethodInvokingService {

    private static final String CANNOT_INSTANTIATE_CLASS_FORMAT = "Cannot create an instance of class \"%s\" because it relies on dependencies.";

    private final DependencyContainer dependencyContainer;

    private Map<String, Set<ActionMethod>> actionMethods;

    private Map<Class<?>, Object> controllers;

    private PrimitiveTypeDataResolver dataResolver;

    private HttpSoletRequest currentRequest;

    public ActionMethodInvokingServiceImpl(DependencyContainer dependencyContainer, Map<String, Set<ActionMethod>> actionMethods, Map<Class<?>, Object> controllers) {
        this.dependencyContainer = dependencyContainer;
        this.actionMethods = actionMethods;
        this.controllers = controllers;
        this.dataResolver = new PrimitiveTypeDataResolver();
    }

    @Override
    public ActionInvokeResult invokeMethod() {
        this.currentRequest = this.dependencyContainer.getObject(HttpSoletRequest.class);
        System.out.println("path is " + this.currentRequest.getRelativeRequestURL());
        ActionMethod actionMethod = this.findActionMethod();
        if (actionMethod == null) {
            return null;
        }
        Map<String, Object> pathVariables = this.getPathVariables(actionMethod);
        Object methodResult = this.invokeAction(actionMethod, pathVariables);

        this.currentRequest = null;
        return new ActionInvokeResult(methodResult, actionMethod.getContentType());
    }

    private Object invokeAction(ActionMethod actionMethod, Map<String, Object> pathVariables) {
        Object controller = this.controllers.entrySet().stream()
                .filter((kvp) -> actionMethod.getControllerClass().isAssignableFrom(kvp.getKey()))
                .findFirst().orElse(null).getValue();
        if (controller.getClass().getAnnotation(Controller.class).lifeSpan() == ServiceLifeSpan.REQUEST) {
            controller = this.dependencyContainer.reloadController(controller);
        }

        Object[] methodParams = this.getMethodParameters(actionMethod, pathVariables);
        try {
            actionMethod.getMethod().setAccessible(true);
            return actionMethod.getMethod().invoke(controller, methodParams);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Object[] getMethodParameters(ActionMethod actionMethod, Map<String, Object> pathVariables) {
        Parameter[] parameters = actionMethod.getMethod().getParameters();
        Object[] parameterInstances = new Object[parameters.length];

        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
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
                this.populateBindingModel(instanceOfBindingModel);
                parameterInstances[i] = instanceOfBindingModel;
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException cause) {
                throw new RuntimeException(String.format(CANNOT_INSTANTIATE_CLASS_FORMAT, parameter.getType().getName()), cause);
            }
        }
        return parameterInstances;
    }

    private void populateBindingModel(Object bindingModel) {
        HttpSoletRequest request = this.currentRequest;
        if (request.getBodyParameters() == null || request.getBodyParameters().size() < 1) {
            return;
        }
        Arrays.stream(bindingModel.getClass().getDeclaredFields()).forEach(f -> {
            f.setAccessible(true);
            if (!request.getBodyParameters().containsKey(f.getName())) {
                return;
            }
            Object parsedVal = this.dataResolver.resolve(f.getType(), request.getBodyParameters().get(f.getName()));
            try {
                f.set(bindingModel, parsedVal);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        });
    }

    private Map<String, Object> getPathVariables(ActionMethod actionMethod) {
        Map<String, Object> pathVariables = new HashMap<>();
        Pattern routePattern = Pattern.compile(actionMethod.getPattern());
        Matcher routeMatcher = routePattern.matcher(this.currentRequest.getRequestURL());
        routeMatcher.find(); //always true

        Arrays.stream(actionMethod.getMethod().getParameters()).forEach(p -> {
            if (p.isAnnotationPresent(PathVariable.class)) {
                String paramName = p.getAnnotation(PathVariable.class).value();
                pathVariables.put(p.getName(), this.dataResolver.resolve(p.getType(), routeMatcher.group(paramName)));
            }
        });

        return pathVariables;
    }

    private ActionMethod findActionMethod() {
        if (!this.actionMethods.containsKey(this.currentRequest.getMethod().toUpperCase())) {
            return null;
        }
        return this.actionMethods.get(this.currentRequest.getMethod().toUpperCase()).stream()
                .filter(action -> Pattern.matches(action.getPattern(), this.currentRequest.getRelativeRequestURL()))
                .findFirst().orElse(null);
    }
}
