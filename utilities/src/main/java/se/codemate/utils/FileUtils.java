package se.codemate.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class FileUtils {

    public static void copyFile(File source, File target) throws IOException {
        FileChannel sourceChannel = new FileInputStream(source).getChannel();
        FileChannel targetChannel = new FileOutputStream(target).getChannel();
        try {
            sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
        } finally {
            if (sourceChannel != null) sourceChannel.close();
            if (targetChannel != null) targetChannel.close();
        }
    }

    public static boolean deleteDirectory(String directory) {
        return deleteDirectory(new File(directory));
    }

    public static boolean deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            for (File file : directory.listFiles()) {
                deleteDirectory(file);
            }
        }
        return directory.delete();
    }

}
