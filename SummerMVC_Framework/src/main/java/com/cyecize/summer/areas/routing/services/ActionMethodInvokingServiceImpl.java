package com.cyecize.summer.areas.routing.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.MemoryFile;
import com.cyecize.solet.SoletConfig;
import com.cyecize.summer.areas.routing.exceptions.ActionInvocationException;
import com.cyecize.summer.areas.routing.exceptions.HttpNotFoundException;
import com.cyecize.summer.areas.routing.interfaces.MultipartFile;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.models.MultipartFileImpl;
import com.cyecize.summer.areas.routing.utils.PrimitiveTypeDataResolver;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.common.annotations.Controller;
import com.cyecize.summer.common.annotations.routing.ExceptionListener;
import com.cyecize.summer.common.annotations.routing.PathVariable;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.constants.IocConstants;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ActionMethodInvokingServiceImpl implements ActionMethodInvokingService {

    private static final String CANNOT_INSTANTIATE_CLASS_FORMAT = "Cannot create an instance of class \"%s\" because it relies on dependencies.";

    private static final String EXCEPTION = "EXCEPTION";

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
    public ActionMethod findAction() throws HttpNotFoundException {
        this.currentRequest = this.dependencyContainer.getObject(HttpSoletRequest.class);
        ActionMethod actionMethod = this.findActionMethod();
        if (actionMethod == null) {
            throw new HttpNotFoundException(this.currentRequest.getRequestURL());
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
            Object parsedVal = null;
            if (f.getType() == MultipartFile.class) {
                MemoryFile memoryFile = request.getUploadedFiles().get(f.getName());
                if (memoryFile != null) {
                    parsedVal = new MultipartFileImpl(dependencyContainer.getObject(SoletConfig.class).getAttribute(IocConstants.SOLET_CFG_ASSETS_DIR) + "", memoryFile);
                }
            } else {
                parsedVal = this.dataResolver.resolve(f.getType(), request.getBodyParameters().get(f.getName()));
            }

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
