package com.cyecize.summer.areas.routing.services;

import com.cyecize.http.HttpRequest;
import com.cyecize.http.HttpStatus;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.solet.SoletLogger;
import com.cyecize.summer.areas.routing.exceptions.ActionInvocationException;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.security.models.Principal;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.startup.services.SessionScopeManager;
import com.cyecize.summer.areas.startup.services.SessionScopeManagerImpl;
import com.cyecize.summer.areas.validation.interfaces.BindingResult;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.models.Model;
import com.cyecize.summer.common.models.RedirectAttributes;
import com.cyecize.summer.constants.RoutingConstants;
import com.cyecize.summer.constants.SecurityConstants;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.regex.Pattern;

public class RequestProcessorImpl implements RequestProcessor {

    private final SoletLogger soletLogger;

    private final SessionScopeManager sessionScopeManager;

    private final ActionMethodInvokingService methodInvokingService;

    private final ActionMethodResultHandler methodResultHandler;

    private final InterceptorInvokerService interceptorService;

    private final DependencyContainer dependencyContainer;

    public RequestProcessorImpl(SoletLogger soletLogger, ActionMethodInvokingService methodInvokingService,
                                ActionMethodResultHandler methodResultHandler,
                                InterceptorInvokerService interceptorService,
                                DependencyContainer dependencyContainer) {
        this.soletLogger = soletLogger;
        this.sessionScopeManager = new SessionScopeManagerImpl();
        this.sessionScopeManager.initialize(dependencyContainer);

        this.methodInvokingService = methodInvokingService;
        this.methodResultHandler = methodResultHandler;
        this.interceptorService = interceptorService;
        this.dependencyContainer = dependencyContainer;
    }

    @Override
    public boolean processRequest(HttpSoletRequest request, HttpSoletResponse response) {
        this.updatePlatformBeans(request, response);
        this.dependencyContainer.clearFlashServices();
        this.dependencyContainer.reloadServices(ServiceLifeSpan.REQUEST);

        this.sessionScopeManager.setSessionScopedServices(request);

        try {
            if (this.interceptorService.preHandle(request, response, dependencyContainer)) {
                if (!this.executeActionMethod(request, response)) {
                    return false;
                }
            }
        } catch (Exception ex) {
            try {
                this.processException(ex);
            } catch (Exception exception) {
                this.whitePageException(response, exception);
            }
        }

        this.setSessionAttributes(request);
        return true;
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
     * Finds an action method.
     * Runs interceptors' preHandle.
     * Invokes method.
     * Handles invocation result.
     * Runs interceptors' postHandle.
     */
    private boolean executeActionMethod(HttpSoletRequest request, HttpSoletResponse response) throws Exception {
        final ActionMethod method = this.methodInvokingService.findAction(request);

        if (method == null) {
            return false;
        }

        if (!this.interceptorService.preHandle(request, response, method)) {
            return true;
        }

        final ActionInvokeResult result = this.methodInvokingService.invokeMethod(method);

        this.methodResultHandler.handleActionResult(result);
        this.interceptorService.postHandle(request, response, result, this.dependencyContainer.getService(Model.class));
        return true;
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
     * Called when no exceptionListener has been found for a given exception.
     */
    private void whitePageException(HttpSoletResponse response, Throwable ex) {
        if (ex instanceof ActionInvocationException) {
            ex = ex.getCause();
        }

        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final PrintWriter printWriter = new PrintWriter(outputStream);
        ex.printStackTrace(printWriter);
        printWriter.flush();
        printWriter.close();

        response.setContent(String.format(
                "<h1>Uncaught exception \"%s\".</h1> <br> %s",
                ex.getMessage(),
                new String(outputStream.toByteArray())
                        .replaceAll(Pattern.quote(System.lineSeparator()), "<br>")
        ));

        this.soletLogger.printStackTrace(ex);
    }

    private void setSessionAttributes(HttpRequest request) {
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
}
