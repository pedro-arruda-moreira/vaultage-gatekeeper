package com.github.pedroarrudamoreira.vaultage.util.listener;

import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import lombok.SneakyThrows;
import org.apache.commons.lang3.SystemUtils;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;
import java.io.File;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class SpringLoader extends ContextLoaderListener {

    private static boolean initialized = false;

    private static final List<String> PRELOAD_PROPS = Collections.singletonList("security.implementation");

    @Override
    @SneakyThrows
    public void contextInitialized(ServletContextEvent sce) {
        checkDebugDelay(sce);
        synchronized (SpringLoader.class) {
            if (!initialized) {
                initialized = true;
                loadConfigPath();
                loadPreloadProperties();
            }
        }
        super.contextInitialized(sce);
    }

    @SneakyThrows
    private static void loadPreloadProperties() {
        Properties loadedProperties = ObjectFactory.build(Properties.class);
        File configFile = ObjectFactory.build(File.class, System.getProperty("gatekeeper.config.dir"), "vaultage-security-facade/config.properties");
        try (InputStream is = ObjectFactory.buildFileInputStream(configFile)) {
            loadedProperties.load(is);
        }
        for (String currProp : PRELOAD_PROPS) {
            String value = loadedProperties.getProperty(currProp);
            System.setProperty(currProp, value);
        }
    }

    private static void loadConfigPath() {
        File userHomeFile = ObjectFactory.invokeStatic(SystemUtils.class, "getUserHome");
        String configDir = System.getProperty("gatekeeper.properties.dir");
        if (configDir == null) {
            System.setProperty("gatekeeper.config.dir", userHomeFile.getAbsolutePath());
            return;
        }
        System.setProperty("gatekeeper.config.dir", configDir);
    }

    private static void checkDebugDelay(ServletContextEvent sce) throws InterruptedException {
        if (Boolean.parseBoolean(System.getenv("LOCAL_EXEC"))) {
            List<String> vmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
            for (String arg : vmArgs) {
                String argLower = arg.toLowerCase();
                if (argLower.contains("xdebug") || argLower.contains("jdwp")) {
                    sce.getServletContext().log("Waiting 20 seconds for debugger...");
                    Thread.sleep(20000);
                    return;
                }
            }
        }
    }
}
