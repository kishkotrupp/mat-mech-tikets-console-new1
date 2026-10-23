package org.kishkotrupp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class FileTicketStorage implements TicketStorage {

    private final File baseDir;

    public FileTicketStorage(String dirPath) {
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

    @Override
    public void saveFile(String subject, String fileName, java.io.InputStream data) throws java.io.IOException {
        File dir = new File (baseDir, subject);
        if(!dir.isDirectory()){
            throw new java.io.IOException("Нет такого предмета");
        }
        java.nio.file.Files.copy(data, new File(dir, fileName).toPath());
    }

    @Override
    public String readFile(String subject, String author) {
        if (!isSafe(subject) || !isSafe(author)) {
            return null;
        }

        File file = new File(new File(baseDir, subject), author);
        if (!file.isFile()) {
            return null; // Файла нет — возвращаем null
        }

        try {
            return Files.readString(file.toPath(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return "Ошибка чтения файла: " + e.getMessage();
        }
    }

}