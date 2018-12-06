package com.cyecize.summer.areas.routing.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.areas.routing.exceptions.ActionInvocationException;
import com.cyecize.summer.areas.routing.exceptions.HttpNotFoundException;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.utils.PrimitiveTypeDataResolver;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.validation.annotations.Valid;
import com.cyecize.summer.areas.validation.exceptions.ValidationException;
import com.cyecize.summer.areas.validation.interfaces.BindingResult;
import com.cyecize.summer.areas.validation.services.ObjectBindingService;
import com.cyecize.summer.areas.validation.services.ObjectValidationService;
import com.cyecize.summer.common.annotations.Controller;
import com.cyecize.summer.common.annotations.routing.ExceptionListener;
import com.cyecize.summer.common.annotations.routing.PathVariable;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import org.eclipse.xtext.xbase.lib.Exceptions;

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

    private Map<String, Set<ActionMethod>> actionMethods;

    private Map<Class<?>, Object> controllers;

    private PrimitiveTypeDataResolver dataResolver;

    private HttpSoletRequest currentRequest;

    public ActionMethodInvokingServiceImpl(DependencyContainer dependencyContainer, ObjectBindingService bindingService, ObjectValidationService validationService, Map<String, Set<ActionMethod>> actionMethods, Map<Class<?>, Object> controllers) {
        this.dependencyContainer = dependencyContainer;
        this.bindingService = bindingService;
        this.validationService = validationService;
        this.actionMethods = actionMethods;
        this.controllers = controllers;
        this.dataResolver = new PrimitiveTypeDataResolver();
    }

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

    @Override
    public ActionInvokeResult invokeMethod(ActionMethod actionMethod) {
        Map<String, Object> pathVariables = this.getPathVariables(actionMethod);
        Object methodResult = this.invokeAction(actionMethod, pathVariables);

        this.currentRequest = null;
        return new ActionInvokeResult(methodResult, actionMethod.getContentType());
    }

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
        return new ActionInvokeResult(methodResult, actionMethod.getContentType());
    }

    private Object invokeAction(ActionMethod actionMethod, Map<String, Object> pathVariables) {
        Object controller = this.controllers.entrySet().stream()
                .filter((kvp) -> actionMethod.getControllerClass().isAssignableFrom(kvp.getKey()))
                .findFirst().orElse(null).getValue();
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
     * If @PathVariable is present, it looks in the pathVariables instead.
     * If the object is not found, then it is considered to be a bindingModel.
     * If the object is bindingModel, it is populated and validated if needed.
     */
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

    private Map<String, Object> getPathVariables(ActionMethod actionMethod) {
        Map<String, Object> pathVariables = new HashMap<>();
        Pattern routePattern = Pattern.compile(actionMethod.getPattern());
        Matcher routeMatcher = routePattern.matcher(this.currentRequest.getRelativeRequestURL());
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
