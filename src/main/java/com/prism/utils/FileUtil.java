package com.prism.utils;

import java.io.File;
import java.nio.file.Path;
import java.text.DecimalFormat;

public class FileUtil {
    public static String getRelativePath(File directory, File file) {
        Path dirPath = directory.toPath().toAbsolutePath().normalize();
        Path filePath = file.toPath().toAbsolutePath().normalize();

        return dirPath.relativize(filePath).toString();
    }

    public static String getFileExtension(File file) {
        if (file == null) {
            return "";
        }

        String name = file.getName();

        int lastDot = name.lastIndexOf('.');

        if (lastDot == -1 || lastDot == name.length() - 1) {
            return "";
        }

        return name.substring(lastDot + 1).toLowerCase();
    }

    public static boolean isViewableImage(File file) {
        String extension = getFileExtension(file);

        switch (extension) {
            case "png":
            case "jpeg":
            case "jpg":
                return true;
            default:
                return false;
        }
    }

    public static String formatFileSize(long bytes) {
        if (bytes <= 0) {
            return "0 bytes";
        }

        final String[] units = new String[]{"bytes", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));

        return new DecimalFormat("#,##0.#").format(bytes / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }
}
