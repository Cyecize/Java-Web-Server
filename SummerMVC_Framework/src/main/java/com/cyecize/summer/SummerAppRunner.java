package com.cyecize.summer;

import com.cyecize.ioc.MagicInjector;
import com.cyecize.ioc.config.MagicConfiguration;
import com.cyecize.ioc.handlers.DependencyResolver;
import com.cyecize.solet.HttpSolet;
import com.cyecize.summer.areas.startup.callbacks.ComponentScopeHandler;
import com.cyecize.summer.areas.startup.models.SummerAppContext;
import com.cyecize.summer.areas.startup.services.DependencyContainer;
import com.cyecize.summer.areas.startup.services.DependencyContainerImpl;
import com.cyecize.summer.areas.startup.util.MagicConfigurationProducer;
import com.cyecize.summer.areas.startup.util.SummerAppContextProducer;

import java.util.List;

public class SummerAppRunner {

    public static SummerAppContext run(Class<? extends HttpSolet> startupSolet, DependencyResolver... dependencyResolvers) {

        final MagicConfiguration magicConfiguration = MagicConfigurationProducer.getConfiguration(
                startupSolet,
                List.of(dependencyResolvers),
                List.of(new ComponentScopeHandler())
        );

        final DependencyContainer dependencyContainer = new DependencyContainerImpl(
                MagicInjector.run(startupSolet, magicConfiguration)
        );

        return SummerAppContextProducer.createAppContext(dependencyContainer);
    }
}
