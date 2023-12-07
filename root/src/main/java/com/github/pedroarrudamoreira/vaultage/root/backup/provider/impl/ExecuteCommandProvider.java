package com.github.pedroarrudamoreira.vaultage.root.backup.provider.impl;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawner;
import com.github.pedroarrudamoreira.vaultage.process.ProcessSpawnerOptions;
import com.github.pedroarrudamoreira.vaultage.root.backup.provider.BackupProvider;
import com.github.pedroarrudamoreira.vaultage.root.backup.provider.util.BackupProviderUtils;
import com.github.pedroarrudamoreira.vaultage.root.security.model.User;
import com.github.pedroarrudamoreira.vaultage.root.util.RootObjectFactory;
import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import com.github.pedroarrudamoreira.vaultage.util.IOUtils;
import com.github.pedroarrudamoreira.vaultage.util.ObjectFactory;

import lombok.Cleanup;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

@RequiredArgsConstructor
public class ExecuteCommandProvider implements BackupProvider {

    private final EventLoop loop;

    @Override
    @SneakyThrows
    public void doBackup(User user, InputStream database, Object params) {
        File tempFolder = null;
        File tempFile = null;
        try {
            tempFile = RootObjectFactory.buildTempFile("backup", ".zip");
            tempFolder = tempFile.getParentFile();
            final File theFile = ObjectFactory.buildFile(tempFolder, BackupProviderUtils.createFileName(user));
            {
                @Cleanup OutputStream out = ObjectFactory.buildFileOutputStream(theFile);
                IOUtils.copy(database, out);
            }
            List<String> paramsList = BackupProviderUtils.getParamsAsStringList(params);
            for (int i = 0; i < paramsList.size(); i++) {
                paramsList.set(i, String.format(paramsList.get(i), theFile.getAbsolutePath()));
            }
            final Process process = ProcessSpawner.executeProcess(ProcessSpawnerOptions.builder()
                    .command(paramsList.toArray(new String[paramsList.size()]))
                    .loop(loop)
                    .build());
            loop.repeatTask(() -> {
                final boolean alive = process.isAlive();
                if (!alive) {
                    if (theFile != null) {
                        theFile.delete();
                    }
                }
                return alive;
            }, 2, TimeUnit.SECONDS);
        } finally {
            if (tempFile != null) {
                tempFile.delete();
            }
        }
    }

}
