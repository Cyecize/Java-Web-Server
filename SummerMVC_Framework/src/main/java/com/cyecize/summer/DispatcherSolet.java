package com.cyecize.summer;

import com.cyecize.http.HttpStatus;
import com.cyecize.solet.*;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.*;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.scanning.services.DependencyContainerImpl;
import com.cyecize.summer.areas.security.interfaces.UserDetails;
import com.cyecize.summer.areas.security.models.Principal;
import com.cyecize.summer.areas.template.services.TemplateRenderingTwigService;
import com.cyecize.summer.areas.validation.interfaces.BindingResult;
import com.cyecize.summer.areas.validation.models.BindingResultImpl;
import com.cyecize.summer.areas.validation.models.FieldError;
import com.cyecize.summer.areas.validation.models.RedirectedBindingResult;
import com.cyecize.summer.areas.validation.services.ObjectBindingServiceImpl;
import com.cyecize.summer.areas.validation.services.ObjectValidationServiceImpl;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.models.Model;
import com.cyecize.summer.common.models.ModelAndView;
import com.cyecize.summer.common.models.RedirectAttributes;
import com.cyecize.summer.constants.RoutingConstants;
import com.cyecize.summer.constants.SecurityConstants;

import java.lang.reflect.Field;
import java.util.*;

import static com.cyecize.summer.constants.IocConstants.*;

@WebSolet("/*")
public abstract class DispatcherSolet extends BaseHttpSolet {

    private ActionMethodInvokingService methodInvokingService;

    private ActionMethodResultHandler methodResultHandler;

    private InterceptorInvokerService interceptorService;

    private TemplateRenderingTwigService renderingService;

    protected DependencyContainer dependencyContainer;

    protected String workingDir;

    protected DispatcherSolet() {
        super();
        this.dependencyContainer = new DependencyContainerImpl();
        this.dependencyContainer.addPlatformBean(new Principal());
        SummerBootApplication.dependencyContainer = this.dependencyContainer;
    }

    /**
     * Finds an action method.
     * Runs interceptors' preHandle.
     * Invokes method.
     * Handles invocation result.
     * Runs interceptors' postHandle.
     */
    private void processRequest(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        ActionMethod method = this.methodInvokingService.findAction(request);
        if (method == null) {
            this.setHasIntercepted(false);
            return;
        }

        if (!this.interceptorService.preHandle(request, response, method, false)) {
            return;
        }

        ActionInvokeResult result = this.methodInvokingService.invokeMethod(method);
        this.methodResultHandler.handleActionResult(result);
        this.interceptorService.postHandle(request, response, result, this.dependencyContainer.getObject(Model.class));
    }

    /**
     * Called on every request.
     * Adds platform beans.
     * Reloads services with REQUEST life span.
     * Runs interceptors' preHandle
     * Proceeds with the  BaseHttpSolet's service logic.
     * If Exception is thrown, processException method is called.
     * Finally sets state to the session and evicts platform beans.
     */
    @Override
    public final void service(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        super.setHasIntercepted(true);
        this.addPlatformBeans(request, response);
        dependencyContainer.reloadServices(ServiceLifeSpan.REQUEST);
        try {
            if (this.interceptorService.preHandle(request, response, dependencyContainer, true)) {
                super.service(request, response);
            }
        } catch (Exception ex) {
            try {
                this.processException(ex);
            } catch (Exception exception) {
                this.whitePageException(response, exception);
            }
        }

        request.getSession().addAttribute(SecurityConstants.SESSION_USER_DETAILS_KEY, dependencyContainer.getObject(Principal.class).getUser());
        request.getSession().addAttribute(RoutingConstants.REDIRECT_ATTRIBUTES_SESSION_ID, dependencyContainer.getObject(RedirectAttributes.class).getAttributes());
        request.getSession().addAttribute(RoutingConstants.BINDING_ERRORS_SESSION_ID, dependencyContainer.getObject(BindingResult.class).getErrors());
        dependencyContainer.evictPlatformBeans();
    }

    /**
     * Looks for exception listeners and calls whitePageError, if no listener is found.
     * If a listener is found, proceeds to handle the actionResult.
     */
    private void processException(Exception ex) {
        ActionInvokeResult exResult = this.methodInvokingService.invokeMethod(ex);
        if (exResult == null) {
            this.whitePageException(this.dependencyContainer.getObject(HttpSoletResponse.class), ex);
            return;
        }
        this.methodResultHandler.handleActionResult(exResult);
    }

    /**
     * Adds platform beans on every request, including the dependency container itself.
     */
    @SuppressWarnings("unchecked")
    private void addPlatformBeans(HttpSoletRequest request, HttpSoletResponse response) {
        this.addPlatformBean(this.dependencyContainer);
        this.addPlatformBean(request);
        this.addPlatformBean(response);
        this.addPlatformBean(this.getSoletConfig());
        this.addPlatformBean(request.getSession());
        this.addPlatformBean(this.renderingService);
        this.addPlatformBean(new Model((Map<String, Object>) request.getSession().getAttribute(RoutingConstants.REDIRECT_ATTRIBUTES_SESSION_ID)));
        this.addPlatformBean(new ModelAndView());
        this.addPlatformBean(new RedirectAttributes());
        this.addPlatformBean(new Principal((UserDetails) request.getSession().getAttribute(SecurityConstants.SESSION_USER_DETAILS_KEY)));
        this.addPlatformBean(new BindingResultImpl());
        this.addPlatformBean(new RedirectedBindingResult((List<FieldError>) request.getSession().getAttribute(RoutingConstants.BINDING_ERRORS_SESSION_ID)));
    }

    /**
     * Add platform bean
     */
    private void addPlatformBean(Object bean) {
        this.dependencyContainer.addPlatformBean(bean);
    }

    /**
     * Called when no exceptionListener has been found for a given exception.
     */
    private void whitePageException(HttpSoletResponse response, Throwable ex) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.setContent(String.format("Uncaught exception \"%s\". Check the console.", ex.getMessage()));
        ex.printStackTrace();
    }

    @SuppressWarnings("unchecked")
    private void initApplication(SoletConfig soletConfig) {
        this.workingDir = (String) soletConfig.getAttribute(SOLET_CFG_WORKING_DIR);
        Map<String, Set<Object>> components = (Map<String, Set<Object>>) soletConfig.getAttribute(SOLET_CFG_COMPONENTS);

        this.dependencyContainer.addServices((Set<Object>) soletConfig.getAttribute(SOLET_CFG_LOADED_SERVICES_AND_BEANS));
        this.dependencyContainer.addServices(Set.of(soletConfig));

        this.methodInvokingService = new ActionMethodInvokingServiceImpl(
                this.dependencyContainer,
                new ObjectBindingServiceImpl(this.dependencyContainer, components.get(COMPONENT_MAP_DATA_ADAPTERS)),
                new ObjectValidationServiceImpl(components.get(COMPONENT_MAP_VALIDATORS), dependencyContainer),
                (Map<String, Set<ActionMethod>>) soletConfig.getAttribute(SOLET_CFG_LOADED_ACTIONS),
                (Map<Class<?>, Object>) soletConfig.getAttribute(SOLET_CFG_LOADED_CONTROLLERS));

        this.renderingService = new TemplateRenderingTwigService(this.workingDir, this.dependencyContainer);
        this.methodResultHandler = new ActionMethodResultHandlerImpl(this.dependencyContainer, renderingService);

        this.interceptorService = new InterceptorInvokerServiceImpl(components.get(COMPONENT_MAP_INTERCEPTORS), this.dependencyContainer);
    }

    //override these methods and make them final to prevent the app from overriding and breaking the code.
    @Override
    public final void init(SoletConfig soletConfig) {
        if (soletConfig.getAttribute(SOLET_CFG_WORKING_DIR) != null) {
            this.initApplication(soletConfig);
            super.init(soletConfig);

            //set the field to false so broccolina can init the app with additional parameters
            try {
                Field isInitialized = BaseHttpSolet.class.getDeclaredField("isInitialized");
                isInitialized.setAccessible(true);
                isInitialized.set(this, false);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }

            return;
        }

        if (this.getSoletConfig() != null) {
            soletConfig.getAllAttributes().forEach(super.getSoletConfig()::setAttribute);
        } else {
            super.init(soletConfig);
        }
    }

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

    @Override
    public void setAssetsFolder(String dir) {
        super.setAssetsFolder(dir);
        this.getSoletConfig().setAttribute(SOLET_CFG_ASSETS_DIR, dir);
    }
}
