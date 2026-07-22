package com.university.lms.config;

import java.lang.reflect.Constructor;

import javafx.util.Callback;

/**
 * {@link FXMLLoader} controller factory that performs constructor-based dependency injection:
 * a controller is instantiated via a constructor that accepts an {@link AppContext} if one
 * exists, otherwise via its no-arg constructor. This keeps controllers testable (they can be
 * constructed directly in unit tests) without pulling in a full DI framework.
 */
public final class FxControllerFactory implements Callback<Class<?>, Object> {

    private final AppContext appContext;

    public FxControllerFactory(AppContext appContext) {
        this.appContext = appContext;
    }

    @Override
    public Object call(Class<?> controllerClass) {
        try {
            for (Constructor<?> constructor : controllerClass.getConstructors()) {
                Class<?>[] paramTypes = constructor.getParameterTypes();
                if (paramTypes.length == 1 && paramTypes[0] == AppContext.class) {
                    return constructor.newInstance(appContext);
                }
            }
            return controllerClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Unable to instantiate controller: " + controllerClass, e);
        }
    }
}
