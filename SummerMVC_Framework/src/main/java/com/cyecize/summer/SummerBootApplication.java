package com.cyecize.summer;

import com.cyecize.solet.SoletConfig;
import com.cyecize.solet.SoletConfigImpl;
import com.cyecize.summer.areas.scanning.exceptions.BeanLoadException;
import com.cyecize.summer.areas.scanning.exceptions.ControllerLoadException;
import com.cyecize.summer.areas.scanning.exceptions.FileScanException;
import com.cyecize.summer.areas.scanning.exceptions.ServiceLoadException;
import com.cyecize.summer.areas.scanning.services.*;
import com.cyecize.summer.constants.IocConstants;

import java.util.Set;

public class SummerBootApplication {

    public static <T extends DispatcherSolet> void run(T startupSolet) {
        FileScanService fileScanService = new FileScanServiceImpl(startupSolet.getClass());
        BeanLoadingService beanLoadingService = new BeanLoadingServiceImpl();
        ServiceLoadingService serviceLoadingService = new ServiceLoadingServiceImpl();
        ControllerLoadingService controllerLoadingService = new ControllerLoadingServiceImpl();
        try {
            Set<Class<?>> loadedClasses = fileScanService.scanFiles();
            Set<Object> loadedBeans = beanLoadingService.loadBeans(loadedClasses);
            Set<Object> loadedServicesAndBeans = serviceLoadingService.loadServices(loadedBeans, loadedClasses);
            Set<Object> loadedControllers = controllerLoadingService.loadControllers(loadedClasses, loadedServicesAndBeans);

            SoletConfig soletConfig = new SoletConfigImpl();
            soletConfig.setAttribute(IocConstants.SOLET_CFG_LOADED_SERVICES_AND_BEANS_NAME, loadedServicesAndBeans);
            soletConfig.setAttribute(IocConstants.SOLET_CFG_LOADED_CONTROLLERS_NAME, loadedControllers);
            startupSolet.init(soletConfig);
            loadedClasses = null;
            loadedBeans = null;
        } catch (FileScanException | ServiceLoadException | BeanLoadException | ControllerLoadException ex) {
            ex.printStackTrace();
        } finally {
            fileScanService = null;
            beanLoadingService = null;
            serviceLoadingService = null;
            controllerLoadingService = null;
            System.gc();
        }
    }
}
