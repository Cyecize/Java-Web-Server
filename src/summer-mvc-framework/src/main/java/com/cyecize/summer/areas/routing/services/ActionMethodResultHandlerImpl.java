package com.cyecize.summer.areas.routing.services;

import com.cyecize.http.HttpStatus;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.summer.areas.routing.exceptions.ActionInvocationException;
import com.cyecize.summer.areas.routing.exceptions.ViewNotFoundException;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.template.exceptions.EmptyViewException;
import com.cyecize.summer.areas.template.services.TemplateRenderingService;
import com.cyecize.summer.common.models.JsonResponse;
import com.cyecize.summer.common.models.Model;
import com.cyecize.summer.common.models.ModelAndView;
import com.cyecize.summer.constants.ContentTypes;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.cyecize.summer.constants.RoutingConstants.ACTION_REDIRECT_ABSOLUTE_ROUTE_STARTING_CHAR;
import static com.cyecize.summer.constants.RoutingConstants.ACTION_RETURN_DELIMITER;
import static com.cyecize.summer.constants.RoutingConstants.ACTION_RETURN_REDIRECT;
import static com.cyecize.summer.constants.RoutingConstants.ACTION_RETURN_TEMPLATE;
import static com.cyecize.summer.constants.RoutingConstants.MODEL_VIEW_NAME_KEY;

public class ActionMethodResultHandlerImpl implements ActionMethodResultHandler {

    private static final String VIEW_EMPTY_MSG = "There is no specified view";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private final ObjectMapper objectMapper;

    private final DependencyContainer dependencyContainer;

    private final TemplateRenderingService renderingService;

    private HttpSoletResponse response;

    private ActionInvokeResult actionInvokeResult;

    public ActionMethodResultHandlerImpl(DependencyContainer dependencyContainer,
                                         TemplateRenderingService renderingService) {
        this.dependencyContainer = dependencyContainer;
        this.renderingService = renderingService;
        this.objectMapper = dependencyContainer.getService(ObjectMapper.class);
    }

    /**
     * Sets Content-Type header from the action result.
     * Looks for suitable method.
     */
    @Override
    public void handleActionResult(ActionInvokeResult result) {
        this.response = this.dependencyContainer.getService(HttpSoletResponse.class);
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
        final Object methodInvokeResult = result.getInvocationResult();
        if (methodInvokeResult == null) {
            this.handleNullResponse();
            return;
        }

        final Class<?> actionResultType = methodInvokeResult.getClass();
        final Method suitableMethod = Arrays.stream(ActionMethodResultHandlerImpl.class.getDeclaredMethods())
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
        try {
            this.response.setContent(this.objectMapper.writeValueAsString(result.getInvocationResult()));
        } catch (JsonProcessingException e) {
            throw new ActionInvocationException("Error while converting response to JSON.", e);
        }
    }

    /**
     * In case of {@link JsonResponse}, Stringify the result and set the Content-Type to application/json
     */
    private void handleJsonResponse(JsonResponse result) {
        if (result.getStatusCode() != null) {
            this.response.setStatusCode(result.getStatusCode());
        }

        this.response.addHeader(CONTENT_TYPE_HEADER, ContentTypes.APPLICATION_JSON);
        try {
            this.response.setContent(this.objectMapper.writeValueAsString(result));
        } catch (JsonProcessingException e) {
            throw new ActionInvocationException("Error while converting response to JSON.", e);
        }
    }

    /**
     * In case of {@link ModelAndView}, Transfer all added parameters to the model,
     * set the status code from the modelAndView if response is missing one,
     * adds the view parameter to the model and proceeds to call
     * handleModelResponse.
     */
    private void handleModelAndViewResponse(ModelAndView result) throws EmptyViewException, ViewNotFoundException {
        final Model model = this.dependencyContainer.getService(Model.class);
        if (result.getStatus() != null) {
            this.response.setStatusCode(result.getStatus());
        }

        result.getAttributes().forEach(model::addAttribute);
        model.addAttribute(MODEL_VIEW_NAME_KEY, result.getView());
        this.handleModelResponse(model);
    }

    /**
     * In case of {@link Model} response, check if the view is empty and throw an exception if it is.
     * If the view contains the redirect value, proceed to handle redirect response
     * otherwise proceed to handle view response.
     */
    private void handleModelResponse(Model result) throws EmptyViewException, ViewNotFoundException {
        final Object viewName = result.getAttribute(MODEL_VIEW_NAME_KEY);
        if (viewName == null) {
            throw new EmptyViewException(VIEW_EMPTY_MSG);
        }

        if (viewName.toString().startsWith(ACTION_RETURN_REDIRECT + ACTION_RETURN_DELIMITER)) {
            this.handleRedirectResponse(
                    viewName.toString().split(ACTION_RETURN_DELIMITER)[1].trim(),
                    this.dependencyContainer.getService(HttpSoletRequest.class));
            return;
        }

        this.handleViewResponse(viewName.toString(), result);
    }

    /**
     * Checks if response contains template or redirect keywords and handles the actions accordingly.
     * If there are no matching keywords, sets the response content directly.
     */
    private void handleStringResponse(String result) throws ViewNotFoundException {
        final String delimiter = ACTION_RETURN_DELIMITER;

        if (result.startsWith(ACTION_RETURN_TEMPLATE + delimiter)) {
            this.handleViewResponse(
                    result.substring((ACTION_RETURN_TEMPLATE + delimiter).length()).trim(),
                    this.dependencyContainer.getService(Model.class)
            );

        } else if (result.startsWith(ACTION_RETURN_REDIRECT + delimiter)) {
            this.handleRedirectResponse(
                    result.substring((ACTION_RETURN_REDIRECT + delimiter).length()).trim(),
                    this.dependencyContainer.getService(HttpSoletRequest.class)
            );

        } else {
            this.response.setContent(result);
        }
    }

    private void handleNullResponse() {
        if (this.response.getContent() == null) {
            this.response.setContent("");
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
