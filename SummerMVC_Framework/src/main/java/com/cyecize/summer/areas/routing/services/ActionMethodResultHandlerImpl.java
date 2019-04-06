package com.cyecize.summer.areas.routing.services;

import com.cyecize.http.HttpStatus;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.summer.areas.routing.exceptions.ActionInvocationException;
import com.cyecize.summer.areas.template.exceptions.EmptyViewException;
import com.cyecize.summer.areas.routing.exceptions.ViewNotFoundException;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.template.services.TemplateRenderingService;
import com.cyecize.summer.common.models.JsonResponse;
import com.cyecize.summer.common.models.Model;
import com.cyecize.summer.common.models.ModelAndView;
import com.cyecize.summer.constants.ContentTypes;
import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.cyecize.summer.constants.RoutingConstants.*;

public class ActionMethodResultHandlerImpl implements ActionMethodResultHandler {

    private static final String VIEW_EMPTY_MSG = "There is no specified view";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private final Gson gson;

    private final DependencyContainer dependencyContainer;

    private final TemplateRenderingService renderingService;

    private HttpSoletResponse response;

    private ActionInvokeResult actionInvokeResult;

    public ActionMethodResultHandlerImpl(DependencyContainer dependencyContainer, TemplateRenderingService renderingService) {
        this.dependencyContainer = dependencyContainer;
        this.renderingService = renderingService;
        this.gson = new Gson();
    }

    /**
     * Sets Content-Type header from the action result.
     * Looks for suitable method.
     */
    @Override
    public void handleActionResult(ActionInvokeResult result) {
        this.response = this.dependencyContainer.getObject(HttpSoletResponse.class);
        this.actionInvokeResult = result;

        if (!this.response.getHeaders().containsKey(CONTENT_TYPE_HEADER)) {
            this.response.addHeader(CONTENT_TYPE_HEADER, result.getContentType());
        }

        this.executeSuitableMethod(result);
        if (this.response.getStatusCode() == null) {
            this.response.setStatusCode(HttpStatus.OK);
        }

        this.actionInvokeResult = null;
    }

    /**
     * Scans the current class for methods who have 1 parameter and that parameter is
     * assignable from the action result.
     * Then proceeds to execute that method.
     * If no suitable method is found, the method handleOtherResponse is called.
     */
    private void executeSuitableMethod(ActionInvokeResult result) {
        Object methodInvokeResult = result.getInvocationResult();
        Class<?> actionResultType = methodInvokeResult.getClass();
        Method suitableMethod = Arrays.stream(ActionMethodResultHandlerImpl.class.getDeclaredMethods())
                .filter(m -> m.getParameterCount() == 1 && actionResultType.isAssignableFrom(m.getParameterTypes()[0]))
                .findFirst().orElse(null);
        if (suitableMethod != null) {
            suitableMethod.setAccessible(true);
            try {
                suitableMethod.invoke(this, methodInvokeResult);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ActionInvocationException(e.getCause().getMessage(), e.getCause());
            }
        } else {
            this.handleOtherResponse(result);
        }
    }

    /**
     * Sets the response as a Json representation of the return type.
     */
    private void handleOtherResponse(ActionInvokeResult result) {
        this.response.setContent(this.gson.toJson(result.getInvocationResult()));
    }

    /**
     * In case of JsonResponse, Stringify the result and set the Content-Type to application/json
     */
    private void handleJsonResponse(JsonResponse result) {
        if (result.getStatusCode() != null) {
            this.response.setStatusCode(result.getStatusCode());
        }

        this.response.addHeader(CONTENT_TYPE_HEADER, ContentTypes.APPLICATION_JSON);
        this.response.setContent(this.gson.toJson(result));
    }

    /**
     * In case of ModelAndView, Transfer all added parameters to the model,
     * set the status code from the modelAndView if response is missing one,
     * adds the view parameter to the model and proceeds to call
     * handleModelResponse.
     */
    private void handleModelAndViewResponse(ModelAndView result) throws EmptyViewException, ViewNotFoundException {
        Model model = this.dependencyContainer.getObject(Model.class);
        if (result.getStatus() != null) {
            this.response.setStatusCode(result.getStatus());
        }

        result.getAttributes().forEach(model::addAttribute);
        model.addAttribute(MODEL_VIEW_NAME_KEY, result.getView());
        this.handleModelResponse(model);
    }

    /**
     * In case of Model response, check if the view is empty and throw an exception if it is.
     * If the view contains the redirect value, proceed to handle redirect response
     * otherwise proceed to handle view response.
     */
    private void handleModelResponse(Model result) throws EmptyViewException, ViewNotFoundException {
        Object viewName = result.getAttribute(MODEL_VIEW_NAME_KEY);
        if (viewName == null) {
            throw new EmptyViewException(VIEW_EMPTY_MSG);
        }

        if (viewName.toString().startsWith(ACTION_RETURN_REDIRECT + ACTION_RETURN_DELIMITER)) {
            this.handleRedirectResponse(
                    viewName.toString().split(ACTION_RETURN_DELIMITER)[1].trim(),
                    this.dependencyContainer.getObject(HttpSoletRequest.class));
            return;
        }
        this.handleViewResponse(viewName.toString(), result);
    }

    /**
     * Checks if response contains template or redirect keywords and handles the actions accordingly.
     * If there are no matching keywords, sets the response content directly.
     */
    private void handleStringResponse(String result) throws ViewNotFoundException {
        String[] resultTokens = (result + "").split(ACTION_RETURN_DELIMITER);
        if (resultTokens.length == 2) {
            switch (resultTokens[0].trim().toLowerCase()) {
                case ACTION_RETURN_TEMPLATE:
                    this.handleViewResponse(resultTokens[1].trim(), this.dependencyContainer.getObject(Model.class));
                    break;
                case ACTION_RETURN_REDIRECT:
                    this.handleRedirectResponse(resultTokens[1].trim(), this.dependencyContainer.getObject(HttpSoletRequest.class));
                    break;
                default:
                    this.response.setContent(result);
            }
        } else {
            this.response.setContent(result);
        }
    }

    /**
     * Calls twig service to handle the view.
     */
    private void handleViewResponse(String view, Model model) throws ViewNotFoundException {
        this.response.setContent(this.renderingService.render(view, model));
    }

    /**
     * Sends redirect by adding the app Name as a prefix.
     */
    private void handleRedirectResponse(String location, HttpSoletRequest request) {
        if (!location.startsWith(ACTION_REDIRECT_ABSOLUTE_ROUTE_STARTING_CHAR)) {
            location = this.actionInvokeResult.getActionMethod().getBaseRoute() + ACTION_REDIRECT_ABSOLUTE_ROUTE_STARTING_CHAR + location;
        }

        this.response.sendRedirect(request.getContextPath() + location);
    }
}
