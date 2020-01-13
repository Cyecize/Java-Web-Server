package com.cyecize.summer.areas.validation.services;

import com.cyecize.ioc.models.ServiceDetails;
import com.cyecize.summer.areas.scanning.services.DependencyContainer;
import com.cyecize.summer.areas.validation.interfaces.DataAdapter;

import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataAdapterStorageServiceImpl implements DataAdapterStorageService {

    private static final String NO_GENERIC_TYPE_FOUND_FOR_CLS_FORMAT = "No generic type found for data adapter \"%s\".";

    private final Map<String, List<DataAdapter>> dataAdapters;

    public DataAdapterStorageServiceImpl(DependencyContainer dependencyContainer) {
        this.dataAdapters = new HashMap<>();
        this.setDataAdapters(dependencyContainer.getImplementations(DataAdapter.class));
    }

    @Override
    public boolean hasDataAdapter(String genericType) {
        return this.getAdaptersForGenericType(genericType) != null;
    }

    /**
     * Iterates through all data adapters since generic type is not provided.
     * Looks for an adapter that is assignable from the given type.
     *
     * @param dataAdapterType the required adapter type.
     * @return the required adapter of null if nothing found.
     */
    @Override
    public <T extends DataAdapter> DataAdapter getDataAdapter(Class<T> dataAdapterType) {
        for (Map.Entry<String, List<DataAdapter>> entry : this.dataAdapters.entrySet()) {
            for (DataAdapter adapter : entry.getValue()) {
                if (dataAdapterType.isAssignableFrom(adapter.getClass())) {
                    return adapter;
                }
            }
        }

        return null;
    }

    /**
     * Gets a specific data adapter.
     * First gets the list of adapters by the given field type and
     * then looks for an adapter that is assignable from the given data adapter type.
     *
     * @param fieldGenericType field type
     * @param dataAdapterType  the type of the data adapter.
     * @return the first data adapter.
     */
    @Override
    public <T extends DataAdapter> DataAdapter getDataAdapter(String fieldGenericType, Class<T> dataAdapterType) {
        final List<DataAdapter> dataAdapters = this.getAdaptersForGenericType(fieldGenericType);

        if (dataAdapters == null) {
            return null;
        }

        return dataAdapters.stream()
                .filter(da -> dataAdapterType.isAssignableFrom(da.getClass()))
                .findFirst().orElse(null);
    }


    /**
     * Gets the first data adapter for a given field type.
     *
     * @param genericType field type
     * @return the first data adapter.
     */
    @Override
    public DataAdapter getDataAdapter(String genericType) {
        final List<DataAdapter> dataAdapters = this.getAdaptersForGenericType(genericType);

        if (dataAdapters != null) {
            return dataAdapters.get(0);
        }

        return null;
    }

    /**
     * Gets data adapters for a given field type.
     *
     * @param genericType the target type of the data adapter.
     * @return adapters for that type.
     */
    private List<DataAdapter> getAdaptersForGenericType(String genericType) {
        final List<DataAdapter> dataAdapters = this.dataAdapters.get(genericType);

        if (dataAdapters == null || dataAdapters.size() < 1) {
            return null;
        }

        return dataAdapters;
    }

    /**
     * Iterates the set of data adapters and for each object gets its
     * generic type and adds the adapter to a map of data adapters where the key is the
     * generic type and the value is a list of data adapters for that type.
     */
    private void setDataAdapters(Collection<ServiceDetails> adapters) {

        for (ServiceDetails serviceDetails : adapters) {
            final DataAdapter adapter = (DataAdapter) serviceDetails.getActualInstance();

            try {
                final String genericType = ((ParameterizedType) adapter.getClass()
                        .getGenericInterfaces()[0]).getActualTypeArguments()[0].getTypeName();

                if (!this.dataAdapters.containsKey(genericType)) {
                    this.dataAdapters.put(genericType, new ArrayList<>());
                }

                this.dataAdapters.get(genericType).add((DataAdapter) serviceDetails.getInstance());
            } catch (Throwable e) {
                throw new RuntimeException(NO_GENERIC_TYPE_FOUND_FOR_CLS_FORMAT, e);
            }
        }
    }
}
