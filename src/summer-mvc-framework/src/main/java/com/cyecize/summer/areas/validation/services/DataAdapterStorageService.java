package com.cyecize.summer.areas.validation.services;

import com.cyecize.summer.areas.validation.interfaces.DataAdapter;

import java.util.List;

public interface DataAdapterStorageService {

    boolean hasDataAdapter(String genericType);

    <T extends DataAdapter<?>> DataAdapter<?> getDataAdapter(Class<T> dataAdapterType);

    <T extends DataAdapter<?>> DataAdapter<?> getDataAdapter(String fieldGenericType, Class<T> dataAdapterType);

    DataAdapter<?> getDataAdapter(String genericType);
}
