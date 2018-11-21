package com.cyecize.summer.areas.routing.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.summer.areas.routing.exceptions.ActionInvocationException;
import com.cyecize.summer.areas.routing.exceptions.EmptyViewException;
import com.cyecize.summer.areas.routing.exceptions.ViewNotFoundException;
import com.cyecize.summer.areas.routing.models.ActionInvokeResult;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.common.models.JsonResponse;
import com.cyecize.summer.common.models.Model;
import com.cyecize.summer.common.models.ModelAndView;
import com.google.gson.Gson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import static com.cyecize.summer.constants.RoutingConstants.*;

public class ActionMethodResultHandlerImpl implements ActionMethodResultHandler {

    private static final String VIEW_EMPTY_MSG = "There is no specified view";

    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private final DependencyContainer dependencyContainer;

    private final TemplateRenderingService renderingService;

    private HttpSoletResponse response;

    private Gson gson;

    public ActionMethodResultHandlerImpl(DependencyContainer dependencyContainer, TemplateRenderingService renderingService) {
        this.dependencyContainer = dependencyContainer;
        this.renderingService = renderingService;
        this.gson = new Gson();
    }

    @Override
    public void handleActionResult(ActionInvokeResult result) {
        this.response = this.dependencyContainer.getObject(HttpSoletResponse.class);
        this.response.addHeader(CONTENT_TYPE_HEADER, result.getContentType());
        this.executeSuitableMethod(result);
    }

    private void executeSuitableMethod(ActionInvokeResult result) {
        Object methodInvokeResult = result.getInvocationResult();
        Method suitableMethod = Arrays.stream(this.getClass().getDeclaredMethods())
                .filter(m -> m.getParameterCount() == 1 && methodInvokeResult.getClass().isAssignableFrom(m.getParameterTypes()[0]))
                .findFirst().orElse(null);
        if (suitableMethod != null) {
            suitableMethod.setAccessible(true);
            try {
                suitableMethod.invoke(this, methodInvokeResult);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new ActionInvocationException(e.getCause().getCause().getMessage(), e.getCause());
            }
        } else {
            this.handleOtherResponse(result);
        }
    }

    private void handleOtherResponse(ActionInvokeResult result) {
        this.response.setContent(this.gson.toJson(result.getInvocationResult()));
    }

    private void handleJsonResponse(JsonResponse result) {
        this.response.addHeader(CONTENT_TYPE_HEADER, "application/json");
        this.response.setContent(this.gson.toJson(result));
    }

    private void handleModelAndViewResponse(ModelAndView result) throws EmptyViewException, ViewNotFoundException {
        Model model = new Model();
        this.response.setStatusCode(result.getStatus());
        result.getAttributes().forEach(model::addAttribute);
        model.addAttribute(MODEL_VIEW_NAME_KEY, result.getView());
        this.handleModelResponse(model);
    }

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

    private void handleViewResponse(String view, Model model) throws ViewNotFoundException {
        this.response.setContent(this.renderingService.render(view, model));
    }

    private void handleRedirectResponse(String location, HttpSoletRequest request) {
        this.response.sendRedirect(request.getContextPath() + location);
    }
}
