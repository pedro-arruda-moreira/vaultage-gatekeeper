package com.github.pedroarrudamoreira.vaultage.root.util.zip;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class EasyZip {

    private final List<String> fileList = new ArrayList<>();
    
    private final File folderLocation;
    
    private final String folderPath;

    public EasyZip(File folderLocation) {
        this.folderLocation = folderLocation;
        this.folderPath = this.folderLocation.getAbsolutePath();
        generateFileList(folderLocation);
    }

    public void zipIt(OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        String source = folderLocation.getName();
        ZipOutputStream zos = null;
        try {
            zos = new ZipOutputStream(out);

            for (String file : this.fileList) {
            	FileInputStream in = null;
                ZipEntry ze = new ZipEntry(normalize(source + "/" + file));
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(new File(folderLocation, file));
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    in.close();
                }
            }

            zos.closeEntry();

        } catch (IOException ex) {
            throw ex;
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                // ignored
            }
        }
    }

    private String normalize(String string) {
		return string.replace('\\', '/');
	}

	private void generateFileList(File file) {

        if (file.isDirectory()) {
            File[] children = file.listFiles();
            for (File child: children) {
                generateFileList(child);
            }
        } else {
            fileList.add(generateFilePath(file.toString()));
        }
    }

    private String generateFilePath(String file) {
        return file.substring(folderPath.length() + 1);
    }
}