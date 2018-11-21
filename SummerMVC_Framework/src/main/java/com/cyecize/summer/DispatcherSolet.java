package com.cyecize.summer;

import com.cyecize.solet.*;
import com.cyecize.summer.areas.routing.exceptions.HttpNotFoundException;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.*;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.scanning.services.DependencyContainerImpl;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.models.Model;

import java.util.*;

import static com.cyecize.summer.constants.IocConstants.*;

@WebSolet("/*")
public abstract class DispatcherSolet extends BaseHttpSolet {

    private ActionMethodInvokingService methodInvokingService;

    private ActionMethodResultHandler methodResultHandler;

    private TemplateRenderingService renderingService;

    protected DependencyContainer dependencyContainer;

    protected String workingDir;

    protected DispatcherSolet() {
        super();
        this.dependencyContainer = new DependencyContainerImpl();
    }

    private void processRequest() throws HttpNotFoundException {
        this.dependencyContainer.reloadServices(ServiceLifeSpan.REQUEST);
        ActionInvokeResult result = this.methodInvokingService.invokeMethod();
        if (result == null) {
            System.out.println("Not Found!");
            return;
        }
        this.methodResultHandler.handleActionResult(result);
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
        //Also TODO... add interceptors here
        try {
            super.service(request, response);
        }catch (Exception ex) {
            ex.printStackTrace();
        }

        dependencyContainer.evictPlatformBeans();
    }

    //override these methods and make them final to prevent the app from overriding and breaking the code.
    @Override
    protected final void doGet(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.processRequest();
    }

    @Override
    protected final void doPost(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.processRequest();
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
