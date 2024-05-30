package com.github.pedroarrudamoreira.vaultage.build.tools;

import java.io.IOException;

import com.github.pedroarrudamoreira.vaultage.build.tools.utils.FileOperations;
import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawner;
import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawnerOptions;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;
import lombok.extern.apachecommons.CommonsLog;

@CommonsLog
public class NpmInstaller {
    private final EventLoop loop = new EventLoop();

    private final FileOperations fileOperations = new FileOperations();

    private void execute(String[] args) {
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
            log.info(String.format(
                    "\n  package: %s\n  location to install: %s\n  sub folder: %s",
                    npmPackage,
                    locationToInstall,
                    subFolderWithContents
            ));
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
            new FileOperations().copyFiles(subFolderWithContents, locationToInstall);
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

    public static void main(String[] args) {
        new NpmInstaller().execute(args);
    }

    private boolean executeCleanup(String locationToInstall) {
        return fileOperations.destroy(locationToInstall + "/node_modules") &&
                fileOperations.destroy(locationToInstall + "/package.json") &&
                fileOperations.destroy(locationToInstall + "/package-lock.json");
    }

}
