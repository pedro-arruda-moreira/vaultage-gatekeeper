package com.github.pedroarrudamoreira.vaultage.build.tools.utils;

import com.github.pedroarrudamoreira.vaultage.util.EventLoop;
import lombok.RequiredArgsConstructor;
import lombok.extern.apachecommons.CommonsLog;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

@CommonsLog
@RequiredArgsConstructor
public class FileOperations {

    public void copyFiles(String origin, String destination) throws Exception {
        Path source = Paths.get(origin);
        Path target = Paths.get(destination);
        Files.walkFileTree(source, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                Path targetDir = target.resolve(source.relativize(dir));
                log.info(String.format(
                        "    directory %s -> %s",
                        dir.toFile().getAbsolutePath(),
                        targetDir.toFile().getAbsolutePath()
                ));
                try {
                    Files.createDirectory(targetDir);
                } catch (FileAlreadyExistsException e) {
                    // Ignore if directory already exists
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path targetFile = target.resolve(source.relativize(file));
                log.info(String.format(
                        "        file %s -> %s",
                        file.toFile().getAbsolutePath(),
                        targetFile.toFile().getAbsolutePath()
                ));
                Files.copy(file, targetFile, StandardCopyOption.REPLACE_EXISTING);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public boolean destroy(String location) {
        return destroyImpl(new File(location));
    }

    private boolean destroyImpl(File file) {
        if (file.isDirectory()) {
            log.info(String.format("    del dir %s", file.getAbsolutePath()));
            for (File child : file.listFiles()) {
                if (!destroyImpl(child)) {
                    return false;
                }
            }
        } else {
            log.info(String.format("        del file %s", file.getAbsolutePath()));
        }
        return file.delete();
    }

}
