package com.cyecize.summer;

import com.cyecize.http.HttpStatus;
import com.cyecize.solet.*;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.*;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.scanning.services.DependencyContainerImpl;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.models.Model;
import com.cyecize.summer.common.models.ModelAndView;

import java.util.*;

import static com.cyecize.summer.constants.IocConstants.*;

@WebSolet("/*")
public abstract class DispatcherSolet extends BaseHttpSolet {

    private ActionMethodInvokingService methodInvokingService;

    private ActionMethodResultHandler methodResultHandler;

    private TemplateRenderingService renderingService;

    private InterceptorInvokerService interceptorService;

    protected DependencyContainer dependencyContainer;

    protected String workingDir;

    protected DispatcherSolet() {
        super();
        this.dependencyContainer = new DependencyContainerImpl();
    }

    private void processRequest(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.dependencyContainer.reloadServices(ServiceLifeSpan.REQUEST);
        ActionMethod method = this.methodInvokingService.findAction();
        if (!this.interceptorService.preHandle(request, response, method)) {
            return;
        }
        ActionInvokeResult result = this.methodInvokingService.invokeMethod(method);
        this.methodResultHandler.handleActionResult(result);
        this.interceptorService.postHandle(request, response, result, this.dependencyContainer.getObject(Model.class));
    }

    private void processException(Exception ex) {
        ActionInvokeResult exResult = this.methodInvokingService.invokeMethod(ex);
        if (exResult == null) {
            this.whitePageException(this.dependencyContainer.getObject(HttpSoletResponse.class), ex);
            return;
        }
        this.methodResultHandler.handleActionResult(exResult);
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void init(SoletConfig soletConfig) {
        if (soletConfig.getAttribute(SOLET_CFG_WORKING_DIR) != null) {
            this.workingDir = (String) soletConfig.getAttribute(SOLET_CFG_WORKING_DIR);
            this.dependencyContainer.addServices((Set<Object>) soletConfig.getAttribute(SOLET_CFG_LOADED_SERVICES_AND_BEANS));
            this.methodInvokingService = new ActionMethodInvokingServiceImpl(
                    this.dependencyContainer,
                    (Map<String, Set<ActionMethod>>) soletConfig.getAttribute(SOLET_CFG_LOADED_ACTIONS),
                    (Map<Class<?>, Object>) soletConfig.getAttribute(SOLET_CFG_LOADED_CONTROLLERS));

            this.renderingService = new TemplateRenderingTwigService(this.workingDir);
            this.methodResultHandler = new ActionMethodResultHandlerImpl(this.dependencyContainer, renderingService);

            Map<String, Set<Object>> components = (Map<String, Set<Object>>) soletConfig.getAttribute(SOLET_CFG_COMPONENTS);

            this.interceptorService = new InterceptorInvokerServiceImpl(components.get(COMPONENT_MAP_INTERCEPTORS), this.dependencyContainer);
        }
        super.init(soletConfig);
    }

    @Override
    public final void service(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        dependencyContainer.addPlatformBean(request);
        dependencyContainer.addPlatformBean(response);
        dependencyContainer.addPlatformBean(new Model());
        dependencyContainer.addPlatformBean(new ModelAndView());

        if (request.getSession() != null) {
            dependencyContainer.addPlatformBean(request.getSession());
        }

        //TODO... add interceptors here
        try {
            super.service(request, response);
        } catch (Exception ex) {
            try {
                this.processException(ex);
            } catch (Exception exception) {
                this.whitePageException(response, exception);
            }
        }
        dependencyContainer.evictPlatformBeans();
    }

    private void whitePageException(HttpSoletResponse response, Exception ex) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.setContent(String.format("Uncaught exception \"%s\". Check the console.", ex.getMessage()));
        ex.printStackTrace();
    }

    //override these methods and make them final to prevent the app from overriding and breaking the code.
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
