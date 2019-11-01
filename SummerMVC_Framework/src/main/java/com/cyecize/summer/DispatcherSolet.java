package com.cyecize.summer;

import com.cyecize.http.HttpStatus;
import com.cyecize.solet.*;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.*;
import com.cyecize.summer.areas.scanning.models.ScannedObjects;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.scanning.services.SessionScopeManager;
import com.cyecize.summer.areas.scanning.services.SessionScopeManagerImpl;
import com.cyecize.summer.areas.security.models.Principal;
import com.cyecize.summer.areas.template.services.TemplateRenderingTwigService;
import com.cyecize.summer.areas.validation.interfaces.BindingResult;
import com.cyecize.summer.areas.validation.services.DataAdapterStorageService;
import com.cyecize.summer.areas.validation.services.DataAdapterStorageServiceImpl;
import com.cyecize.summer.areas.validation.services.ObjectBindingServiceImpl;
import com.cyecize.summer.areas.validation.services.ObjectValidationServiceImpl;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.models.*;
import com.cyecize.summer.constants.RoutingConstants;
import com.cyecize.summer.constants.SecurityConstants;

import static com.cyecize.summer.constants.IocConstants.*;

@WebSolet("/*")
public abstract class DispatcherSolet extends BaseHttpSolet {

    private final SessionScopeManager sessionScopeManager;

    private ActionMethodInvokingService methodInvokingService;

    private ActionMethodResultHandler methodResultHandler;

    private InterceptorInvokerService interceptorService;

    private ScannedObjects scannedObjects;

    protected DependencyContainer dependencyContainer;

    protected String workingDir;

    protected DispatcherSolet() {
        super();
        this.sessionScopeManager = new SessionScopeManagerImpl();
    }

    /**
     * Finds an action method.
     * Runs interceptors' preHandle.
     * Invokes method.
     * Handles invocation result.
     * Runs interceptors' postHandle.
     */
    private void processRequest(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        final ActionMethod method = this.methodInvokingService.findAction(request);

        if (method == null) {
            this.setHasIntercepted(false);
            return;
        }

        if (!this.interceptorService.preHandle(request, response, method)) {
            return;
        }

        final ActionInvokeResult result = this.methodInvokingService.invokeMethod(method);

        this.methodResultHandler.handleActionResult(result);
        this.interceptorService.postHandle(request, response, result, this.dependencyContainer.getService(Model.class));
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
    public synchronized final void service(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        super.setHasIntercepted(true);
        this.updatePlatformBeans(request, response);
        this.dependencyContainer.clearFlashServices();
        this.dependencyContainer.reloadServices(ServiceLifeSpan.REQUEST);
        this.sessionScopeManager.setSessionScopedServices(request);

        try {
            if (this.interceptorService.preHandle(request, response, dependencyContainer)) {
                super.service(request, response);
            }
        } catch (Exception ex) {
            try {
                this.processException(ex);
            } catch (Exception exception) {
                this.whitePageException(response, exception);
            }
        }

        request.getSession().addAttribute(SecurityConstants.SESSION_USER_DETAILS_KEY, dependencyContainer.getService(Principal.class).getUser());
        request.getSession().addAttribute(RoutingConstants.REDIRECT_ATTRIBUTES_SESSION_ID, dependencyContainer.getService(RedirectAttributes.class).getAttributes());
        request.getSession().addAttribute(RoutingConstants.BINDING_ERRORS_SESSION_ID, dependencyContainer.getService(BindingResult.class).getErrors());
    }

    /**
     * Looks for exception listeners and calls whitePageError, if no listener is found.
     * If a listener is found, proceeds to handle the actionResult.
     */
    private void processException(Exception ex) {
        final ActionInvokeResult exResult = this.methodInvokingService.invokeMethod(ex);
        if (exResult == null) {
            this.whitePageException(this.dependencyContainer.getService(HttpSoletResponse.class), ex);
            return;
        }

        this.methodResultHandler.handleActionResult(exResult);
    }

    /**
     * Updates request beans on every request.
     */
    private void updatePlatformBeans(HttpSoletRequest request, HttpSoletResponse response) {
        this.dependencyContainer.update(request);
        this.dependencyContainer.update(response);
        this.dependencyContainer.update(request.getSession());
    }

    /**
     * Called when no exceptionListener has been found for a given exception.
     */
    private void whitePageException(HttpSoletResponse response, Throwable ex) {
        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        response.setContent(String.format("Uncaught exception \"%s\". Check the console.", ex.getMessage()));
        ex.printStackTrace();
    }

    /**
     * Adds scanned objects to the solet config attributes.
     * Calls @PostConstruct annotated methods for services/components/controllers.
     *
     * @param soletConfig config received from broccolina
     */
    @Override
    public final void init(SoletConfig soletConfig) {
        super.init(soletConfig);
        dependencyContainer.update(soletConfig);

        this.getSoletConfig().setAttribute(SOLET_CFG_SCANNED_OBJECTS, this.scannedObjects);

        this.onApplicationLoaded();
    }

    /**
     * Creates main services for the framework to function.
     *
     * @param scannedObjects objects received from the scanning process
     */
    final void initSummerBoot(ScannedObjects scannedObjects) {
        this.dependencyContainer = SummerBootApplication.dependencyContainer;
        this.scannedObjects = scannedObjects;
        this.workingDir = scannedObjects.getWorkingDir();
        this.sessionScopeManager.initialize(dependencyContainer);

        final DataAdapterStorageService dataAdapterStorageService = new DataAdapterStorageServiceImpl(this.dependencyContainer);

        this.methodInvokingService = new ActionMethodInvokingServiceImpl(
                this.dependencyContainer,
                new ObjectBindingServiceImpl(this.dependencyContainer, dataAdapterStorageService),
                new ObjectValidationServiceImpl(this.dependencyContainer),
                dataAdapterStorageService,
                scannedObjects.getActionsByMethod(),
                scannedObjects.getLoadedControllers());

        final TemplateRenderingTwigService renderingService = new TemplateRenderingTwigService(this.workingDir, this.dependencyContainer);
        this.methodResultHandler = new ActionMethodResultHandlerImpl(this.dependencyContainer, renderingService);

        this.interceptorService = new InterceptorInvokerServiceImpl(this.dependencyContainer);
    }

    /**
     * This method will be called once the application has fully been loaded.
     * It can be overridden to achieve event-like effect.
     */
    protected void onApplicationLoaded() {

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
