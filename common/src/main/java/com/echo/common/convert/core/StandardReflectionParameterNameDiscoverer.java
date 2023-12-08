package com.echo.common.convert.core;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * {@link ParameterNameDiscoverer} implementation which uses JDK 8's reflection facilities
 * for introspecting parameter names (based on the "-parameters" compiler flag).
 *
 * @see java.lang.reflect.Method#getParameters()
 * @see java.lang.reflect.Parameter#getName()
 */
public class StandardReflectionParameterNameDiscoverer implements ParameterNameDiscoverer {

    @Override
    public String[] getParameterNames(Method method) {
        return getParameterNames(method.getParameters());
    }

    @Override
    public String[] getParameterNames(Constructor<?> ctor) {
        return getParameterNames(ctor.getParameters());
    }

    private String[] getParameterNames(Parameter[] parameters) {
        String[] parameterNames = new String[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            if (!param.isNamePresent()) {
                return null;
            }
            parameterNames[i] = param.getName();
        }
        return parameterNames;
    }

}