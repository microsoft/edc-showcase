package org.eclipse.dataspaceconnector.test;

import org.eclipse.dataspaceconnector.spi.monitor.Monitor;
import org.eclipse.dataspaceconnector.spi.types.TypeManager;
import org.eclipse.dataspaceconnector.system.DefaultServiceExtensionContext;
import org.eclipse.dataspaceconnector.system.ServiceLocator;

import java.util.HashMap;
import java.util.Map;

public class TestServiceExtensionContext extends DefaultServiceExtensionContext {
    private final Map<String, String> overriddenSettings;

    public TestServiceExtensionContext(TypeManager typeManager, Monitor monitor, ServiceLocator serviceLocator) {
        super(typeManager, monitor, serviceLocator);
        this.overriddenSettings = new HashMap<>();
    }

    @Override
    public String getSetting(String key, String defaultValue) {
        if (overriddenSettings.containsKey(key)) {
            return overriddenSettings.get(key);
        }

        return super.getSetting(key, defaultValue);
    }

    void overrideSetting(String key, String value) {
        overriddenSettings.put(key, value);
    }
}
