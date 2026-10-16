package org.kishkotrupp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class StringContent implements Content {

    private final File baseDir;

    public StringContent(String dirPath) {
        this.baseDir = new File(dirPath);
        if (!baseDir.exists()) {
            baseDir.mkdirs();
        }
        if (!baseDir.isDirectory()) {
            throw new IllegalStateException("Не директория: " + dirPath);
        }
    }

    @Override
    public List<String> listSubjects() {
        List<String> result = new ArrayList<>();
        File[] items = baseDir.listFiles(File::isDirectory);
        if (items == null) return result;
        for (File item : items) {
            result.add(item.getName());
        }
        result.sort(String::compareToIgnoreCase);
        return result;
    }

    @Override
    public List<String> listAuthors(String subject) {
        List<String> result = new ArrayList<>();
        if (!isSafe(subject)) return result;

        File dir = new File(baseDir, subject);
        if (!dir.isDirectory()) return result;

        File[] items = dir.listFiles(File::isFile);
        if (items == null) return result;
        for (File item : items) {
            result.add(item.getName());
        }
        result.sort(String::compareToIgnoreCase);
        return result;
    }

    @Override
    public File getFile(String subject, String author) {
        if (!isSafe(subject) || !isSafe(author)) return null;

        File file = new File(new File(baseDir, subject), author);
        return file.isFile() ? file : null;
    }

    private boolean isSafe(String name) {
        if (name == null || name.isEmpty()) return false;
        if (name.contains("/") || name.contains("\\")) return false;
        if (name.contains("..")) return false;
        return true;
    }
}