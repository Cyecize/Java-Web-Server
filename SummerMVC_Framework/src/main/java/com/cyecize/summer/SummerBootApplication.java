package com.cyecize.summer;

import com.cyecize.http.HttpSessionImpl;
import com.cyecize.ioc.MagicInjector;
import com.cyecize.ioc.config.MagicConfiguration;
import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.solet.SoletConfigImpl;
import com.cyecize.summer.areas.routing.models.ActionMethod;
import com.cyecize.summer.areas.routing.services.ActionMethodScanningService;
import com.cyecize.summer.areas.routing.services.ActionMethodScanningServiceImpl;
import com.cyecize.summer.areas.routing.utils.PathFormatter;
import com.cyecize.summer.areas.scanning.callbacks.ComponentScopeHandler;
import com.cyecize.summer.areas.scanning.models.ScannedObjects;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.scanning.services.DependencyContainerImpl;
import com.cyecize.summer.areas.scanning.util.SoletRequestAndResponseBean;
import com.cyecize.summer.areas.security.interceptors.SecurityInterceptor;
import com.cyecize.summer.areas.security.models.Principal;
import com.cyecize.summer.areas.validation.constraints.FieldMatchConstraint;
import com.cyecize.summer.areas.validation.constraints.MaxConstraint;
import com.cyecize.summer.areas.validation.constraints.MaxLengthConstraint;
import com.cyecize.summer.areas.validation.constraints.MediaTypeConstraint;
import com.cyecize.summer.areas.validation.constraints.MinConstraint;
import com.cyecize.summer.areas.validation.constraints.MinLengthConstraint;
import com.cyecize.summer.areas.validation.constraints.NotEmptyConstraint;
import com.cyecize.summer.areas.validation.constraints.NotNullConstraint;
import com.cyecize.summer.areas.validation.constraints.RegExConstraint;
import com.cyecize.summer.areas.validation.models.BindingResultImpl;
import com.cyecize.summer.areas.validation.models.RedirectedBindingResult;
import com.cyecize.summer.common.annotations.BeanConfig;
import com.cyecize.summer.common.annotations.Component;
import com.cyecize.summer.common.annotations.Controller;
import com.cyecize.summer.common.models.Model;
import com.cyecize.summer.common.models.ModelAndView;
import com.cyecize.summer.common.models.RedirectAttributes;
import com.cyecize.summer.constants.IocConstants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class SummerBootApplication {

    public static DependencyContainer dependencyContainer;

    public static <T extends DispatcherSolet> void run(T startupSolet) {
        final ActionMethodScanningService methodScanningService = new ActionMethodScanningServiceImpl(new PathFormatter());

        final MagicConfiguration configuration = new MagicConfiguration()
                .scanning()
                .addCustomServiceAnnotations(IocConstants.SERVICE_ANNOTATIONS)
                .addAdditionalClassesForScanning(new HashMap<>() {{

                    //platform
                    put(SoletRequestAndResponseBean.class, BeanConfig.class);
                    put(SoletConfigImpl.class, null);
                    put(HttpSessionImpl.class, null);
                    put(Model.class, Component.class);
                    put(ModelAndView.class, Component.class);
                    put(RedirectAttributes.class, Component.class);
                    put(Principal.class, Component.class);
                    put(BindingResultImpl.class, Component.class);
                    put(RedirectedBindingResult.class, Component.class);

                    //constraints
                    put(FieldMatchConstraint.class, Component.class);
                    put(MaxConstraint.class, Component.class);
                    put(MaxLengthConstraint.class, Component.class);
                    put(MediaTypeConstraint.class, Component.class);
                    put(MinConstraint.class, Component.class);
                    put(MinLengthConstraint.class, Component.class);
                    put(NotEmptyConstraint.class, Component.class);
                    put(NotNullConstraint.class, Component.class);
                    put(RegExConstraint.class, Component.class);

                    //interceptors
                    put(SecurityInterceptor.class, Component.class);
                }})
                .setClassLoader(startupSolet.getClass().getClassLoader())
                .addServiceDetailsCreatedCallback(new ComponentScopeHandler())
                .and()
                .build();
        
        dependencyContainer = new DependencyContainerImpl(MagicInjector.run(startupSolet.getClass(), configuration));

        //TODO add service for those
        final Map<Class<?>, Object> loadedControllers = dependencyContainer.getServicesByAnnotation(Controller.class)
                .stream()
                .collect(Collectors.toMap(ServiceDetails::getServiceType, ServiceDetails::getActualInstance));

        final Map<String, Set<ActionMethod>> actionsByMethod = methodScanningService.findActionMethods(loadedControllers);

        startupSolet.initSummerBoot(new ScannedObjects(
                loadedControllers,
                actionsByMethod,
                startupSolet.getClass().getProtectionDomain().getCodeSource().getLocation().getFile().substring(1)
        ));
    }
}
