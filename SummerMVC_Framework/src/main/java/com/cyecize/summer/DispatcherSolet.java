package com.cyecize.summer;

import com.cyecize.solet.BaseHttpSolet;
import com.cyecize.solet.HttpSoletRequest;
import com.cyecize.solet.HttpSoletResponse;
import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConstants;
import com.cyecize.solet.WebSolet;
import com.cyecize.summer.areas.routing.services.ActionMethodInvokingServiceImpl;
import com.cyecize.summer.areas.routing.services.ActionMethodResultHandlerImpl;
import com.cyecize.summer.areas.routing.services.InterceptorInvokerServiceImpl;
import com.cyecize.summer.areas.routing.services.RequestProcessor;
import com.cyecize.summer.areas.routing.services.RequestProcessorImpl;
import com.cyecize.summer.areas.startup.models.ScannedObjects;
import com.cyecize.summer.areas.startup.models.SummerAppContext;
import com.cyecize.summer.areas.startup.resolvers.ConfigurationDependencyResolver;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.startup.util.JavacheConfigServiceExtractor;
import com.cyecize.summer.areas.template.services.TemplateRenderingTwigService;
import com.cyecize.summer.areas.validation.services.DataAdapterStorageService;
import com.cyecize.summer.areas.validation.services.DataAdapterStorageServiceImpl;
import com.cyecize.summer.areas.validation.services.ObjectBindingServiceImpl;
import com.cyecize.summer.areas.validation.services.ObjectValidationServiceImpl;

import java.util.Map;

@WebSolet("/*")
public abstract class DispatcherSolet extends BaseHttpSolet {

    private RequestProcessor requestProcessor;

    protected DependencyContainer dependencyContainer;

    protected DispatcherSolet() {
        super();
    }

    /**
     * Called on every request.
     */
    @Override
    public synchronized final void service(HttpSoletRequest request, HttpSoletResponse response) {
        super.setHasIntercepted(this.requestProcessor.processRequest(request, response));
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
        final Map<String, Object> javahceConfig = JavacheConfigServiceExtractor.getConfigParams(soletConfig);

        final SummerAppContext summerAppContext = SummerAppRunner.run(
                this.getClass(),
                new ConfigurationDependencyResolver(soletConfig, javahceConfig)
        );

        this.dependencyContainer = summerAppContext.getDependencyContainer();
        this.dependencyContainer.update(soletConfig);

        this.initSummerBoot(summerAppContext.getScannedObjects());

        this.onApplicationLoaded();
    }

    /**
     * Creates main services for the framework to function.
     *
     * @param scannedObjects objects received from the scanning process
     */
    private void initSummerBoot(ScannedObjects scannedObjects) {
        final DataAdapterStorageService dataAdapterStorageService =
                new DataAdapterStorageServiceImpl(this.dependencyContainer);

        this.requestProcessor = new RequestProcessorImpl(
                new ActionMethodInvokingServiceImpl(
                        this.dependencyContainer,
                        new ObjectBindingServiceImpl(
                                this.dependencyContainer,
                                dataAdapterStorageService
                        ),
                        new ObjectValidationServiceImpl(this.dependencyContainer),
                        dataAdapterStorageService,
                        scannedObjects.getActionsByMethod(),
                        scannedObjects.getLoadedControllers()
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
