package com.cyecize.summer.areas.validation.interfaces;

import com.cyecize.solet.HttpSoletRequest;

import java.lang.reflect.Field;

public interface DataAdapter<T> {

    T resolveField(Field field, HttpSoletRequest request);
}
