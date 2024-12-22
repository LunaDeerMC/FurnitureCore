package cn.lunadeer.furnitureCore.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    public static void compressFolderContentToZip(File folder, File zipFile) throws IOException {
        compressFolderContentToZip(folder.getAbsolutePath(), zipFile.getAbsolutePath());
    }

    public static void compressFolderContentToZip(String folderPath, String zipFilePath) throws IOException {
        File folder = new File(folderPath);
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            compressFolderContent(folder, zos, "");
        }
    }

    private static void compressFolderContent(File folder, ZipOutputStream zos, String parentPath) throws IOException {
        File[] files = folder.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (file.isDirectory()) {
                compressFolderContent(file, zos, parentPath + file.getName() + "/");
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    zos.putNextEntry(new ZipEntry(parentPath + file.getName()));
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = fis.read(buffer)) >= 0) {
                        zos.write(buffer, 0, length);
                    }
                    zos.closeEntry();
                }
            }
        }
    }

    public static void compressToZip(File sourceFile, File zipFile) throws IOException {
        compressToZip(sourceFile.getAbsolutePath(), zipFile.getAbsolutePath());
    }

    public static void compressToZip(String sourcePath, String zipFilePath) throws IOException {
        File sourceFile = new File(sourcePath);
        try (FileOutputStream fos = new FileOutputStream(zipFilePath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            compressFile(sourceFile, sourceFile.getName(), zos);
        }
    }

    private static void compressFile(File file, String fileName, ZipOutputStream zos) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files == null || files.length == 0) {
                zos.putNextEntry(new ZipEntry(fileName + "/"));
                zos.closeEntry();
            } else {
                for (File childFile : files) {
                    compressFile(childFile, fileName + "/" + childFile.getName(), zos);
                }
            }
        } else {
            try (FileInputStream fis = new FileInputStream(file)) {
                zos.putNextEntry(new ZipEntry(fileName));
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) >= 0) {
                    zos.write(buffer, 0, length);
                }
                zos.closeEntry();
            }
        }
    }

    public static void decompressFromZip(File zipFile, File destDir) throws IOException {
        decompressFromZip(zipFile.getAbsolutePath(), destDir.getAbsolutePath());
    }

    public static void decompressFromZip(String zipFilePath, String destDirPath) throws IOException {
        File destDir = new File(destDirPath);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        try (FileInputStream fis = new FileInputStream(zipFilePath);
             ZipInputStream zis = new ZipInputStream(fis)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File newFile = new File(destDir, entry.getName());
                if (entry.isDirectory()) {
                    newFile.mkdirs();
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.exists()) {
                        parent.mkdirs();
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                    }
                }
                zis.closeEntry();
            }
        }
    }

}
