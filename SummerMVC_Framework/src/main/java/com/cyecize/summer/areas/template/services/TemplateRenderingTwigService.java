package com.cyecize.summer.areas.template.services;

import com.cyecize.summer.areas.routing.exceptions.ViewNotFoundException;
import com.cyecize.summer.areas.security.models.Principal;
import com.cyecize.summer.areas.template.annotations.TemplateService;
import com.cyecize.summer.areas.template.functions.JTwigFieldErrorsFunction;
import com.cyecize.summer.areas.template.functions.JTwigHasRoleFunction;
import com.cyecize.summer.areas.template.functions.JTwigPathFunction;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.common.annotations.Service;
import com.cyecize.summer.common.enums.ServiceLifeSpan;
import com.cyecize.summer.common.models.Model;
import com.cyecize.summer.constants.RoutingConstants;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;
import org.jtwig.exceptions.JtwigException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateRenderingTwigService implements TemplateRenderingService {

    private static final String GLOBAL_VAR_USER = "user";

    private static final String TEMPLATE_NOT_FOUND_FORMAT = "Template \"%s\" not found under resources/templates/%s";

    private final String appRootDir;

    private final DependencyContainer dependencyContainer;

    private Map<String, Object> templateServices;

    private List<String> templateServicesWithRequestLifeSpan;

    private EnvironmentConfiguration twigEnvironmentConfig;

    private String templatesDir;

    public TemplateRenderingTwigService(String appRootDir, DependencyContainer dependencyContainer) {
        this.appRootDir = appRootDir;
        this.dependencyContainer = dependencyContainer;
        this.initTwigEnvironment();
        this.templatesDir = appRootDir + RoutingConstants.TEMPLATES_DIRECTORY + File.separator;
        this.initTemplateServices();
    }

    @Override
    public String render(String view, Model model) throws ViewNotFoundException {
        if (view.startsWith("/")) {
            view = view.substring(1);
        }
        this.addGlobalVars(model);
        try {
            JtwigTemplate template = JtwigTemplate.fileTemplate(this.templatesDir + view, this.twigEnvironmentConfig);
            return template.render(model);
        } catch (JtwigException ex) {
            throw new ViewNotFoundException(String.format(TEMPLATE_NOT_FOUND_FORMAT, view, view), ex);
        }
    }

    /**
     * Add global variables that will be accessible across all views.
     * Add Template services, specified by the user (annotated with @TemplateService).
     */
    private void addGlobalVars(Model model) {
        this.reloadTemplateServices();

        model.addAttribute(GLOBAL_VAR_USER, this.dependencyContainer.getObject(Principal.class).getUser());

        for (Map.Entry<String, Object> serviceEntry : this.templateServices.entrySet()) {
            model.addAttribute(serviceEntry.getKey(), serviceEntry.getValue());
        }
    }

    /**
     * Reload services with REQUEST life span.
     */
    private void reloadTemplateServices() {
        for (String serviceName : this.templateServicesWithRequestLifeSpan) {
            this.templateServices.put(serviceName, this.dependencyContainer.reloadComponent(this.templateServices.get(serviceName)));
        }
    }

    /**
     * Create Twig configuration and add global functions.
     */
    private void initTwigEnvironment() {
        this.twigEnvironmentConfig = EnvironmentConfigurationBuilder
                .configuration()
                .resources()
                    .withDefaultInputCharset(StandardCharsets.UTF_8)
                .and()
                .render()
                    .withOutputCharset(StandardCharsets.UTF_8)
                .and()
                .functions()
                    .add(new JTwigPathFunction(this.appRootDir))
                    .add(new JTwigHasRoleFunction(this.dependencyContainer))
                    .add(new JTwigFieldErrorsFunction(this.dependencyContainer))
                .and()
                .build();
    }

    /**
     * Get all services that are @TwigTemplate annotated.
     * Iterate and add them to a map of String and Object where the string is the service name in twig.
     * If the service is with REQUEST life span, add it to a list of services to be reloaded.
     */
    private void initTemplateServices() {
        this.templateServices = new HashMap<>();
        this.templateServicesWithRequestLifeSpan = new ArrayList<>();
        this.dependencyContainer.getServicesByAnnotation(TemplateService.class).forEach(ts -> {
            String serviceNameInTemplate = ts.getClass().getAnnotation(TemplateService.class).serviceNameInTemplate();

            this.templateServices.put(serviceNameInTemplate, ts);

            if (ts.getClass().getAnnotation(Service.class).lifespan() == ServiceLifeSpan.REQUEST) {
                this.templateServicesWithRequestLifeSpan.add(serviceNameInTemplate);
            }
        });
    }
}
