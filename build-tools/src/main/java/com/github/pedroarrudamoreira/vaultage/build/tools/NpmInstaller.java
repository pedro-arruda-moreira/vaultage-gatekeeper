package com.github.pedroarrudamoreira.vaultage.build.tools;

import java.io.IOException;

import com.github.pedroarrudamoreira.vaultage.build.tools.utils.FileOperations;
import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawner;
import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawnerOptions;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

public class NpmInstaller {
    public static final EventLoop loop = new EventLoop();

    public static void main(String[] args) {
        try {
            try {
                ProcessSpawner.executeProcessAndWait(
                        ProcessSpawnerOptions.builder()
                                .loop(loop)
                                .command(new String[]{"npm", "-version"})
                                .build());
            } catch (IOException e) {
                throw new RuntimeException("NPM is required for this program to be compiled.");
            }
            String npmPackage = args[0];
            String locationToInstall = ObjectFactory.normalizePath(args[1]);
            String subFolderWithContents = ObjectFactory.normalizePath(
                    locationToInstall + '/' + args[2]);
            ProcessSpawner.executeProcessAndWait(
                    ProcessSpawnerOptions.builder()
                            .loop(loop)
                            .command(new String[]{
                                    "npm",
                                    "i",
                                    npmPackage,
                                    "--prefix",
                                    locationToInstall
                            })
                            .build());
            FileOperations.copyFiles(subFolderWithContents, locationToInstall);
            if (!executeCleanup(locationToInstall)) {
                throw new RuntimeException("Could not execute cleanup.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        } finally {
            loop.shutdown();
        }
    }

    private static boolean executeCleanup(String locationToInstall) {
        return FileOperations.destroy(locationToInstall + "/node_modules") &&
                FileOperations.destroy(locationToInstall + "/package.json") &&
                FileOperations.destroy(locationToInstall + "/package-lock.json");
    }

}
