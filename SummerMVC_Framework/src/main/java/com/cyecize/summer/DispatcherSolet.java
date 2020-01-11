package com.cyecize.summer;

import static com.cyecize.summer.constants.IocConstants.SOLET_CFG_SCANNED_OBJECTS;

import com.cyecize.http.HttpStatus;
import com.cyecize.solet.BaseHttpSolet;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.WebSolet;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.ActionMethodInvokingService;
import com.cyecize.summer.areas.routing.services.ActionMethodInvokingServiceImpl;
import com.cyecize.summer.areas.routing.services.ActionMethodResultHandler;
import com.cyecize.summer.areas.routing.services.ActionMethodResultHandlerImpl;
import com.cyecize.summer.areas.routing.services.InterceptorInvokerService;
import com.cyecize.summer.areas.routing.services.InterceptorInvokerServiceImpl;
import com.cyecize.summer.areas.scanning.models.ScannedObjects;
import com.cyecize.summer.areas.scanning.models.SummerAppContext;
import com.cyecize.summer.areas.scanning.resolvers.ConfigurationDependencyResolver;
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
import com.cyecize.summer.common.models.Model;
import com.cyecize.summer.common.models.RedirectAttributes;
import com.cyecize.summer.constants.IocConstants;
import com.cyecize.summer.constants.RoutingConstants;
import com.cyecize.summer.constants.SecurityConstants;

@WebSolet("/*")
public abstract class DispatcherSolet extends BaseHttpSolet {

    private final SessionScopeManager sessionScopeManager;

    private ActionMethodInvokingService methodInvokingService;

    private ActionMethodResultHandler methodResultHandler;

    private InterceptorInvokerService interceptorService;

    protected DependencyContainer dependencyContainer;

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
     * Proceeds with the processRequest logic.
     * If Exception is thrown, processException method is called.
     * Finally sets state to the session and evicts platform beans.
     */
    @Override
    public synchronized final void service(HttpSoletRequest request, HttpSoletResponse response) {
        super.setHasIntercepted(true);
        this.updatePlatformBeans(request, response);
        this.dependencyContainer.clearFlashServices();
        this.dependencyContainer.reloadServices(ServiceLifeSpan.REQUEST);
        this.sessionScopeManager.setSessionScopedServices(request);

        try {
            if (this.interceptorService.preHandle(request, response, dependencyContainer)) {
                this.processRequest(request, response);
            }
        } catch (Exception ex) {
            try {
                this.processException(ex);
            } catch (Exception exception) {
                this.whitePageException(response, exception);
            }
        }

        request.getSession().addAttribute(
                SecurityConstants.SESSION_USER_DETAILS_KEY,
                this.dependencyContainer.getService(Principal.class).getUser()
        );
        request.getSession().addAttribute(
                RoutingConstants.REDIRECT_ATTRIBUTES_SESSION_ID,
                this.dependencyContainer.getService(RedirectAttributes.class).getAttributes()
        );
        request.getSession().addAttribute(
                RoutingConstants.BINDING_ERRORS_SESSION_ID,
                dependencyContainer.getService(BindingResult.class).getErrors()
        );
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
     * Entry point of the application.
     * Here is where Summer MVC will be initialized.
     *
     * @param soletConfig - provided config from broccolina.
     */
    @Override
    public final void init(SoletConfig soletConfig) {
        super.init(soletConfig);
        soletConfig.setAttribute(IocConstants.SOLET_CFG_WORKING_DIR, this.getWorkingDir());

        final SummerAppContext summerAppContext = SummerAppRunner.run(
                this.getClass(),
                new ConfigurationDependencyResolver(soletConfig)
        );

        this.dependencyContainer = summerAppContext.getDependencyContainer();
        this.initSummerBoot(summerAppContext.getScannedObjects());
        this.dependencyContainer.update(soletConfig);

        this.onApplicationLoaded();
    }

    /**
     * Creates main services for the framework to function.
     *
     * @param scannedObjects objects received from the scanning process
     */
    private void initSummerBoot(ScannedObjects scannedObjects) {
        this.getSoletConfig().setAttribute(SOLET_CFG_SCANNED_OBJECTS, scannedObjects);
        this.sessionScopeManager.initialize(this.dependencyContainer);

        final DataAdapterStorageService dataAdapterStorageService = new DataAdapterStorageServiceImpl(this.dependencyContainer);

        this.methodInvokingService = new ActionMethodInvokingServiceImpl(
                this.dependencyContainer,
                new ObjectBindingServiceImpl(this.dependencyContainer, dataAdapterStorageService),
                new ObjectValidationServiceImpl(this.dependencyContainer),
                dataAdapterStorageService,
                scannedObjects.getActionsByMethod(),
                scannedObjects.getLoadedControllers()
        );

        this.methodResultHandler = new ActionMethodResultHandlerImpl(
                this.dependencyContainer,
                new TemplateRenderingTwigService(
                        this.getWorkingDir(),
                        this.dependencyContainer
                )
        );

        this.interceptorService = new InterceptorInvokerServiceImpl(this.dependencyContainer);
    }

    /**
     * This method will be called once the application has fully been loaded.
     * It can be overridden to achieve event-like effect.
     */
    protected void onApplicationLoaded() {

    }

    private String getWorkingDir() {
        return this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().substring(1);
    }
}
