package com.cyecize.summer.areas.routing.services;

import com.cyecize.summer.areas.routing.exceptions.ViewNotFoundException;
import com.cyecize.summer.common.models.Model;
import com.cyecize.summer.constants.RoutingConstants;
import org.jtwig.JtwigTemplate;
import org.jtwig.environment.EnvironmentConfiguration;
import org.jtwig.environment.EnvironmentConfigurationBuilder;
import org.jtwig.exceptions.JtwigException;
import org.jtwig.parser.ParseException;

import java.io.File;
import java.nio.charset.StandardCharsets;

public class TemplateRenderingTwigService implements TemplateRenderingService {

    private static final String TEMPLATE_NOT_FOUND_FORMAT = "Template \"%s\" not found under resources/templates/%s";

    private final String appRootDir;

    private EnvironmentConfiguration twigEnvironmentConfig;

    private String templatesDir;

    public TemplateRenderingTwigService(String appRootDir) {
        this.appRootDir = appRootDir;
        this.initTwigEnvironment();
        this.templatesDir = appRootDir + RoutingConstants.TEMPLATES_DIRECTORY + File.separator;
    }

    @Override
    public String render(String view, Model model) throws ViewNotFoundException {
        if (view.startsWith("/")) {
            view = view.substring(1);
        }
        try {
            JtwigTemplate template = JtwigTemplate.fileTemplate(this.templatesDir + view, this.twigEnvironmentConfig);
            return template.render(model);
        } catch (JtwigException | ParseException ex) {
            throw new ViewNotFoundException(String.format(TEMPLATE_NOT_FOUND_FORMAT, view, view), ex);
        }
    }

    private void initTwigEnvironment() {
        this.twigEnvironmentConfig = EnvironmentConfigurationBuilder
                .configuration()
                .resources()
                .withDefaultInputCharset(StandardCharsets.UTF_8)
                .and()
                .render()
                .withOutputCharset(StandardCharsets.UTF_8)
                .and()
                .build();
    }

}
