package com.cyecize.summer;

import com.cyecize.solet.*;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.ActionMethodInvokingService;
import com.cyecize.summer.areas.routing.services.ActionMethodInvokingServiceImpl;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.scanning.services.DependencyContainerImpl;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.models.JsonResponse;
import com.cyecize.summer.common.models.Model;
import com.google.gson.Gson;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import static com.cyecize.summer.constants.IocConstants.*;

@WebSolet("/*")
public abstract class DispatcherSolet extends BaseHttpSolet {

    private ActionMethodInvokingService methodInvokingService;

    protected DependencyContainer dependencyContainer;

    protected String workingDir;

    protected DispatcherSolet() {
        super();
        this.dependencyContainer = new DependencyContainerImpl();
    }

    private void processRequest(HttpSoletRequest request, HttpSoletResponse response) {
        this.dependencyContainer.reloadServices(ServiceLifeSpan.REQUEST);
        ActionInvokeResult result = this.methodInvokingService.invokeMethod();
        if (result == null) {
            System.out.println("Not Found!");
            return;
        }
        response.addHeader("Content-Type", result.getContentType());
        Object invokedMethodResult = result.getInvocationResult();
        if (invokedMethodResult instanceof String) {
            String stringResult = (String) invokedMethodResult;
            String[] resultTokens = stringResult.split(":");
            if (resultTokens.length == 2) {
                switch (resultTokens[0].trim()) {
                    case "template":
                        String pathToFile = this.workingDir + "templates/" + resultTokens[1].trim();
                        try {
                            var conf  = EnvironmentConfigurationBuilder
                                    .configuration()
                                    .resources()
                                    .withDefaultInputCharset(StandardCharsets.UTF_8)
                                    .and()
                                    .render()
                                    .withOutputCharset(StandardCharsets.UTF_8)
                                    .and()
                                    .build();
                            JtwigTemplate template = JtwigTemplate.fileTemplate(pathToFile, conf);

                            String content = template.render(this.dependencyContainer.getObject(Model.class));
                            response.setContent(content);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case "redirect":
                        response.sendRedirect(super.createRoute(resultTokens[1]));
                        break;
                }
            } else {
                response.setContent(stringResult);
            }
        } else if (invokedMethodResult instanceof Model) {

        } else if (invokedMethodResult instanceof JsonResponse) {
            response.addHeader("Content-Type", "application/json");
            response.setContent(new Gson().toJson(invokedMethodResult));
        } else {
            response.setContent(new Gson().toJson(invokedMethodResult));
        }
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
