package com.github.pedroarrudamoreira.vaultage.build.tools.utils;

import java.io.File;

import com.github.pedroarrudamoreira.vaultage.build.tools.NpmInstaller;
import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawnerOptions;
import org.apache.commons.lang3.SystemUtils;

import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawner;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

public class FileOperations {

    private FileOperations() {
        super();
    }

    public static void copyFiles(String origin, String destination) throws Exception {
        boolean isWindows = SystemUtils.IS_OS_WINDOWS;
        if (isWindows) {
            ProcessSpawner.executeProcessAndWait(
                    ProcessSpawnerOptions.builder()
                            .failureCodeHandler(retVal -> retVal <= 7)
                            .command(new String[]{
                                    "robocopy",
                                    ObjectFactory.normalizePath(origin),
                                    ObjectFactory.normalizePath(destination),
                                    "/E"
                            })
                            .loop(NpmInstaller.loop)
                            .build());
        } else {
            ProcessSpawner.executeProcessAndWait(
                    ProcessSpawnerOptions.builder()
                            .command(new String[]{
                                    "cp",
                                    "-r",
                                    ObjectFactory.normalizePath(origin),
                                    ObjectFactory.normalizePath(destination)
                            })
                            .loop(NpmInstaller.loop)
                            .build());

        }
    }

    public static boolean destroy(String location) {
        return destroyImpl(new File(location));
    }

    private static boolean destroyImpl(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                if (!destroyImpl(child)) {
                    return false;
                }
            }
        }
        return file.delete();
    }

}
