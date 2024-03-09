package com.github.pedroarrudamoreira.vaultage.util.listener;

import lombok.SneakyThrows;
import org.springframework.web.context.ContextLoaderListener;

import javax.servlet.ServletContextEvent;
import java.lang.management.ManagementFactory;
import java.util.List;

public class SpringLoader extends ContextLoaderListener {

    @Override
    @SneakyThrows
    public void contextInitialized(ServletContextEvent sce) {
        checkDebugDelay(sce);
        super.contextInitialized(sce);
    }

    private static void checkDebugDelay(ServletContextEvent sce) throws InterruptedException {
        if(Boolean.parseBoolean(System.getenv("LOCAL_EXEC"))) {
            List<String> vmArgs = ManagementFactory.getRuntimeMXBean().getInputArguments();
            for (String arg : vmArgs) {
                String argLower = arg.toLowerCase();
                if(argLower.contains("xdebug") || argLower.contains("jdwp")) {
                    sce.getServletContext().log("Waiting 20 seconds for debugger...");
                    Thread.sleep(20000);
                    return;
                }
            }
        }
    }
}
