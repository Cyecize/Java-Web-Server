package com.cyecize.summer.areas.template.services;

import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConstants;
import com.cyecize.summer.areas.routing.exceptions.ViewNotFoundException;
import com.cyecize.summer.areas.security.models.Principal;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.template.annotations.TemplateService;
import com.cyecize.summer.areas.template.functions.JTwigFieldErrorsFunction;
import com.cyecize.summer.areas.template.functions.JTwigHasRoleFunction;
import com.cyecize.summer.areas.template.functions.JTwigPathFunction;
import com.cyecize.summer.areas.template.functions.JTwigUrlFunction;
import com.cyecize.summer.areas.validation.models.RedirectedBindingResult;
import com.cyecize.summer.common.annotations.Service;
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

@Service
public class TemplateRenderingTwigService implements TemplateRenderingService {

    private static final String GLOBAL_VAR_USER = "user";

    private static final String GLOBAL_VAR_REQUEST = "request";

    private static final String TEMPLATE_NOT_FOUND_FORMAT = "Template \"%s\" not found under resources/templates/%s";

    private final SoletConfig soletConfig;

    private final HttpSoletRequest soletRequest;

    private final Principal principal;

    private final RedirectedBindingResult redirectedBindingResult;

    private Map<String, Object> templateServices;

    private EnvironmentConfiguration twigEnvironmentConfig;

    private String templatesDir;

    public TemplateRenderingTwigService(SoletConfig soletConfig,
                                        HttpSoletRequest soletRequest,
                                        Principal principal,
                                        RedirectedBindingResult redirectedBindingResult) {
        this.soletConfig = soletConfig;
        this.soletRequest = soletRequest;
        this.principal = principal;
        this.redirectedBindingResult = redirectedBindingResult;
    }

    public void initialize(DependencyContainer dependencyContainer) {
        this.initTwigEnvironment();
        this.templatesDir = this.soletConfig.getAttribute(SoletConstants.SOLET_CFG_WORKING_DIR).toString()
                + RoutingConstants.TEMPLATES_DIRECTORY + File.separator;
        this.initTemplateServices(dependencyContainer);
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
        model.addAttribute(GLOBAL_VAR_USER, this.principal.getUser());
        model.addAttribute(GLOBAL_VAR_REQUEST, this.soletRequest);

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
                    .add(new JTwigPathFunction(this.soletRequest))
                    .add(new JTwigHasRoleFunction(this.principal))
                    .add(new JTwigFieldErrorsFunction(this.redirectedBindingResult))
                    .add(new JTwigUrlFunction(this.soletRequest))
                .and()
                .build();
    }

    /**
     * Get all services that are {@link TemplateService} annotated.
     * Iterate and add them to a map of String and Object where the string is the service name in twig.
     */
    private void initTemplateServices(DependencyContainer dependencyContainer) {
        this.templateServices = new HashMap<>();
        dependencyContainer.getServicesByAnnotation(TemplateService.class).forEach(sd -> {
            final String serviceNameInTemplate = ((TemplateService) sd.getAnnotation()).serviceNameInTemplate();

            this.templateServices.put(serviceNameInTemplate, sd.getInstance());
        });
    }
}
