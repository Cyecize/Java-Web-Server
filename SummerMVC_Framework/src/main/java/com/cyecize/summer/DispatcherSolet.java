package com.cyecize.summer;

import com.cyecize.solet.*;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.scanning.services.DependencyContainerImpl;
import com.cyecize.summer.common.annotations.routing.PathVariable;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.models.Model;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.cyecize.summer.constants.IocConstants.*;

@WebSolet("/*")
public abstract class DispatcherSolet extends BaseHttpSolet {

    private Map<String, Set<ActionMethod>> actionMethods;

    private Map<Class<?>, Object> controllers;

    protected DependencyContainer dependencyContainer;

    protected DispatcherSolet() {
        super();
        this.actionMethods = new HashMap<>();
        this.controllers = new HashMap<>();
        this.dependencyContainer = new DependencyContainerImpl();
    }

    private ActionMethod findActionMethod(HttpSoletRequest request) {
        if (!this.actionMethods.containsKey(request.getMethod().toUpperCase())) {
            return null;
        }
        return this.actionMethods.get(request.getMethod().toUpperCase()).stream()
                .filter(action -> Pattern.matches(action.getPattern(), request.getRequestURL()))
                .findFirst().orElse(null);
    }

    private Object resolvePathVariable(Class<?> pathVariableClass, String matcherGroup) {
        Class<?>[] classes = new Class<?>[]{int.class, Integer.class};
        if (pathVariableClass == int.class || pathVariableClass == Integer.class) {
            try {
                return Integer.valueOf(matcherGroup);
            } catch (NumberFormatException ignored) {
            }
            return Integer.MIN_VALUE;
        }
        //TODO check for other primitives
        return matcherGroup;
    }

    private Map<String, Object> getPathVariables(ActionMethod actionMethod, HttpSoletRequest request) {
        Map<String, Object> pathVariables = new HashMap<>();
        Pattern routePattern = Pattern.compile(actionMethod.getPattern());
        Matcher routeMatcher = routePattern.matcher(request.getRequestURL());
        routeMatcher.find(); //always true

        Arrays.stream(actionMethod.getMethod().getParameters()).forEach(p -> {
            if (p.isAnnotationPresent(PathVariable.class)) {
                String paramName = p.getAnnotation(PathVariable.class).value();
                pathVariables.put(p.getName(), this.resolvePathVariable(p.getType(), routeMatcher.group(paramName)));
            }
        });

        return pathVariables;
    }

    private Object invokeAction(ActionMethod actionMethod, Map<String, Object> pathVariables) {
        //todo re-init controller if lifespan is REQUEST
        Object controllerInstance = this.controllers.entrySet().stream().filter((kvp) -> actionMethod.getControllerClass().isAssignableFrom(kvp.getKey()))
                .findFirst().orElse(null).getValue();
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
            //todo if null check for binding models
        }
        try {
            return actionMethod.getMethod().invoke(controllerInstance, parameterInstances);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void processRequest(HttpSoletRequest request, HttpSoletResponse response) {
        this.dependencyContainer.reloadServices(ServiceLifeSpan.REQUEST);
        ActionMethod actionMethod = this.findActionMethod(request);
        if (actionMethod == null) {
            return;
        }
        Map<String, Object> pathVariables = this.getPathVariables(actionMethod, request);


        Object result = this.invokeAction(actionMethod, pathVariables);
        System.out.println("Method was  invoked!");
        System.out.println(result);
    }


    @Override
    public final void init(SoletConfig soletConfig) {
        if (soletConfig.getAttribute(SOLET_CFG_LOADED_SERVICES_AND_BEANS) != null) {
            this.dependencyContainer.addServices((Set<Object>) soletConfig.getAttribute(SOLET_CFG_LOADED_SERVICES_AND_BEANS));
        }
        if (soletConfig.getAttribute(SOLET_CFG_LOADED_ACTIONS) != null) {
            this.actionMethods = (Map<String, Set<ActionMethod>>) soletConfig.getAttribute(SOLET_CFG_LOADED_ACTIONS);
        }
        if (soletConfig.getAttribute(SOLET_CFG_LOADED_CONTROLLERS) != null) {
            this.controllers = (Map<Class<?>, Object>) soletConfig.getAttribute(SOLET_CFG_LOADED_CONTROLLERS);
        }
        super.init(soletConfig);
    }

    @Override
    public final void service(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        dependencyContainer.addPlatformBean(request);
        dependencyContainer.addPlatformBean(response);
        dependencyContainer.addPlatformBean(new Model());

        if (request.getSession() != null) {
            dependencyContainer.addPlatformBean(request.getSession());
        }

        //TODO try catch this then search for exception listeners
        super.service(request, response);

        dependencyContainer.evictPlatformBeans();
    }

    //override these methods and make them private to restrict the final app from overriding and breaking the code.
    @Override
    protected final void doGet(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.processRequest(request, response);
    }

    @Override
    protected final void doPost(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.processRequest(request, response);
    }

    @Override
    protected final void doPut(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        super.doPut(request, response);
    }

    @Override
    protected final void doDelete(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        super.doDelete(request, response);
    }

    @Override
    public final void setAppNamePrefix(String appName) {
        super.setAppNamePrefix(appName);
    }

    @Override
    public final boolean isInitialized() {
        return super.isInitialized();
    }

    @Override
    protected final String createRoute(String route) {
        return super.createRoute(route);
    }

    @Override
    public final SoletConfig getSoletConfig() {
        return super.getSoletConfig();
    }
}
