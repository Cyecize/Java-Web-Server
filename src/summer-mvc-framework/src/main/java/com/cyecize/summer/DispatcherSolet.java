package com.cyecize.summer;

import com.cyecize.solet.HttpSolet;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConstants;
import com.cyecize.solet.SoletLogger;
import com.cyecize.solet.WebSolet;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.ActionMethodInvokingServiceImpl;
import com.cyecize.summer.areas.routing.services.ActionMethodResultHandlerImpl;
import com.cyecize.summer.areas.routing.services.InterceptorInvokerServiceImpl;
import com.cyecize.summer.areas.routing.services.RequestProcessor;
import com.cyecize.summer.areas.routing.services.RequestProcessorImpl;
import com.cyecize.summer.areas.startup.models.SummerAppContext;
import com.cyecize.summer.areas.startup.resolvers.ConfigurationDependencyResolver;
import com.cyecize.summer.areas.startup.resolvers.SoletLoggerDependencyResolver;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.startup.services.UserConfigService;
import com.cyecize.summer.areas.startup.services.UserConfigServiceImpl;
import com.cyecize.summer.areas.startup.util.JavacheConfigServiceUtils;
import com.cyecize.summer.areas.template.services.TemplateRenderingTwigService;
import com.cyecize.summer.areas.validation.services.DataAdapterStorageService;
import com.cyecize.summer.areas.validation.services.DataAdapterStorageServiceImpl;
import com.cyecize.summer.areas.validation.services.ObjectBindingServiceImpl;
import com.cyecize.summer.areas.validation.services.ObjectValidationServiceImpl;

import java.util.Map;
import java.util.Set;

@WebSolet("/*")
public abstract class DispatcherSolet implements HttpSolet {

    private RequestProcessor requestProcessor;

    private boolean hasIntercepted;

    private boolean hasInitialized;

    protected SoletConfig soletConfig;

    protected DependencyContainer dependencyContainer;

    protected DispatcherSolet() {
    }

    /**
     * Called on every request.
     */
    @Override
    public synchronized final void service(HttpSoletRequest request, HttpSoletResponse response) {
        this.hasIntercepted = this.requestProcessor.processRequest(request, response);
    }

    @Override
    public final boolean isInitialized() {
        return this.hasInitialized;
    }

    @Override
    public final boolean hasIntercepted() {
        return this.hasIntercepted;
    }

    @Override
    public final SoletConfig getSoletConfig() {
        return this.soletConfig;
    }

    /**
     * Entry point of the application.
     * Here is where Summer MVC will be initialized.
     *
     * @param soletConfig - provided config from broccolina.
     */
    @Override
    public final void init(SoletConfig soletConfig) {
        this.soletConfig = soletConfig;
        this.hasInitialized = true;

        final Map<String, Object> javacheConfig = JavacheConfigServiceUtils.getConfigParams(soletConfig);
        final UserConfigService userConfigService = new UserConfigServiceImpl(soletConfig, javacheConfig);

        final SummerAppContext summerAppContext = SummerAppRunner.run(
                this.getClass(),
                new ConfigurationDependencyResolver(soletConfig, javacheConfig, userConfigService.getUserProvidedConfig()),
                new SoletLoggerDependencyResolver(soletConfig)
        );

        this.dependencyContainer = summerAppContext.getDependencyContainer();
        this.dependencyContainer.update(soletConfig);

        this.initRequestProcessor(summerAppContext.getActionsByMethod());

        this.onApplicationLoaded();
    }

    private void initRequestProcessor(Map<String, Set<ActionMethod>> actionMethods) {
        final DataAdapterStorageService dataAdapterStorageService =
                new DataAdapterStorageServiceImpl(this.dependencyContainer);

        this.requestProcessor = new RequestProcessorImpl(
                (SoletLogger) this.getSoletConfig().getAttribute(SoletConstants.SOLET_CONFIG_LOGGER),
                new ActionMethodInvokingServiceImpl(
                        this.dependencyContainer,
                        new ObjectBindingServiceImpl(
                                this.dependencyContainer,
                                dataAdapterStorageService
                        ),
                        new ObjectValidationServiceImpl(this.dependencyContainer),
                        dataAdapterStorageService,
                        actionMethods
                ),
                new ActionMethodResultHandlerImpl(
                        this.dependencyContainer,
                        new TemplateRenderingTwigService(
                                this.getSoletConfig().getAttribute(SoletConstants.SOLET_CFG_WORKING_DIR).toString(),
                                this.dependencyContainer
                        )
                ),
                new InterceptorInvokerServiceImpl(
                        this.dependencyContainer
                ),
                this.dependencyContainer
        );
    }


    /**
     * This method will be called once the application has fully been loaded.
     * It can be overridden to achieve event-like effect.
     */
    protected void onApplicationLoaded() {

    }
}
