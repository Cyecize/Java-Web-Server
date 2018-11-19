package com.cyecize.summer;

import com.cyecize.solet.*;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.scanning.services.DependencyContainerImpl;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.constants.IocConstants;

import java.util.Set;

@WebSolet("/*")
public abstract class DispatcherSolet extends BaseHttpSolet {

    protected DependencyContainer dependencyContainer;

    protected DispatcherSolet() {
        super();
        this.dependencyContainer = new DependencyContainerImpl();
    }

    private void reloadServicesWithRequestLifeSpan() {
        this.dependencyContainer.reloadServices(ServiceLifeSpan.REQUEST);
    }

    @Override
    protected final void doGet(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.reloadServicesWithRequestLifeSpan();
        //TODO Find action method and invoke
        response.setContent("Hello, this is default get!");
    }

    @Override
    protected final void doPost(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        this.reloadServicesWithRequestLifeSpan();
        //TODO Find action method and invoke
        response.setContent("Hello, this is default post!");
    }

    @Override
    public final void init(SoletConfig soletConfig) {
        if (soletConfig.getAttribute(IocConstants.SOLET_CFG_LOADED_SERVICES_AND_BEANS_NAME) != null) {
            this.dependencyContainer.addServices((Set<Object>) soletConfig.getAttribute(IocConstants.SOLET_CFG_LOADED_SERVICES_AND_BEANS_NAME));
        }
        super.init(soletConfig);
    }

    //override these methods and make them private to restrict the final app from overriding and breaking the code.
    @Override
    protected final void doPut(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        super.doPut(request, response);
    }

    @Override
    protected final void doDelete(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        super.doDelete(request, response);
    }

    @Override
    public final void service(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        super.service(request, response);
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
