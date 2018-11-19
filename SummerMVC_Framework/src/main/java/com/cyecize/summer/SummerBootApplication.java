package com.cyecize.summer;

import com.cyecize.summer.areas.scanning.exceptions.BeanLoadException;
import com.cyecize.summer.areas.scanning.exceptions.FileScanException;
import com.cyecize.summer.areas.scanning.exceptions.ServiceLoadException;
import com.cyecize.summer.areas.scanning.services.*;

import java.util.Set;

public class SummerBootApplication {

    public static <T extends DispatcherSolet> void run(T startupSolet) {
        FileScanService fileScanService = new FileScanServiceImpl(startupSolet.getClass());
        BeanLoadingService beanLoadingService = new BeanLoadingServiceImpl();
        ServiceLoadingService serviceLoadingService = new ServiceLoadingServiceImpl();

        try {
            Set<Class<?>> loadedClasses = fileScanService.scanFiles();
            Set<Object> loadedBeans = beanLoadingService.loadBeans(loadedClasses);
            System.out.println("loaded Object - " + loadedBeans.size());
            loadedBeans.forEach(b -> {
                System.out.println(b.getClass().getName());
            });
            Set<Object> loadedServices = serviceLoadingService.loadServices(loadedBeans, loadedClasses);
            System.out.println("loaded " + loadedServices.size() + " services");
            loadedServices.forEach((s) -> {
                System.out.println(s.getClass().getName());
            });
        } catch (FileScanException | ServiceLoadException | BeanLoadException ex) {
            ex.printStackTrace();
        }
    }
}
