package com.cyecize.summer;

import com.cyecize.summer.areas.scanning.exceptions.BeanLoadException;
import com.cyecize.summer.areas.scanning.exceptions.ControllerLoadException;
import com.cyecize.summer.areas.scanning.exceptions.FileScanException;
import com.cyecize.summer.areas.scanning.exceptions.ServiceLoadException;
import com.cyecize.summer.areas.scanning.services.*;

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

            loadedControllers.forEach(c -> {
                System.out.println(c.getClass().getName());
            });
        } catch (FileScanException | ServiceLoadException | BeanLoadException | ControllerLoadException ex) {
            ex.printStackTrace();
        }
    }
}
