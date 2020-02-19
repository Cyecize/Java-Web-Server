package com.cyecize.summer.areas.template.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.summer.areas.routing.exceptions.ViewNotFoundException;
import com.cyecize.summer.areas.security.models.Principal;
import com.cyecize.summer.areas.template.annotations.TemplateService;
import com.cyecize.summer.areas.template.functions.JTwigFieldErrorsFunction;
import com.cyecize.summer.areas.template.functions.JTwigHasRoleFunction;
import com.cyecize.summer.areas.template.functions.JTwigPathFunction;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.template.functions.JTwigUrlFunction;
import com.cyecize.summer.common.models.Model;
import com.cyecize.summer.constants.RoutingConstants;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;
import org.jtwig.exceptions.JtwigException;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class TemplateRenderingTwigService implements TemplateRenderingService {

    private static final String GLOBAL_VAR_USER = "user";

    private static final String GLOBAL_VAR_REQUEST = "request";

    private static final String TEMPLATE_NOT_FOUND_FORMAT = "Template \"%s\" not found under resources/templates/%s";

    private final DependencyContainer dependencyContainer;

    private Map<String, Object> templateServices;

    private EnvironmentConfiguration twigEnvironmentConfig;

    private String templatesDir;

    public TemplateRenderingTwigService(String appRootDir, DependencyContainer dependencyContainer) {
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
            final JtwigTemplate template = JtwigTemplate.fileTemplate(
                    this.templatesDir + view,
                    this.twigEnvironmentConfig
            );
            return template.render(model);
        } catch (JtwigException ex) {
            throw new ViewNotFoundException(String.format(TEMPLATE_NOT_FOUND_FORMAT, view, view), ex);
        }
    }

    /**
     * Add global variables that will be accessible across all views.
     * Add Template services, specified by the user (annotated with {@link TemplateService}).
     */
    private void addGlobalVars(Model model) {
        model.addAttribute(GLOBAL_VAR_USER, this.dependencyContainer.getService(Principal.class).getUser());
        model.addAttribute(GLOBAL_VAR_REQUEST, this.dependencyContainer.getService(HttpSoletRequest.class));

        for (Map.Entry<String, Object> serviceEntry : this.templateServices.entrySet()) {
            model.addAttribute(serviceEntry.getKey(), serviceEntry.getValue());
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
                    .add(new JTwigPathFunction(this.dependencyContainer))
                    .add(new JTwigHasRoleFunction(this.dependencyContainer))
                    .add(new JTwigFieldErrorsFunction(this.dependencyContainer))
                    .add(new JTwigUrlFunction(this.dependencyContainer))
                .and()
                .build();
    }

    /**
     * Get all services that are {@link TemplateService} annotated.
     * Iterate and add them to a map of String and Object where the string is the service name in twig.
     */
    private void initTemplateServices() {
        this.templateServices = new HashMap<>();
        this.dependencyContainer.getServicesByAnnotation(TemplateService.class).forEach(sd -> {
            final String serviceNameInTemplate = ((TemplateService)sd.getAnnotation()).serviceNameInTemplate();

            this.templateServices.put(serviceNameInTemplate, sd.getInstance());
        });
    }
}
