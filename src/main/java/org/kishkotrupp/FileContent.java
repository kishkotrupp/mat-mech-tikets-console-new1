package org.kishkotrupp;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class FileContent implements Content{

    private final Path baseDir;

    public FileContent() {
        this(ensureSubjectsExist());
    }

    public FileContent(Path baseDir) {
        this.baseDir = baseDir;
    }
// ==================== Автораспаковка ====================

    private static Path ensureSubjectsExist() {
        try {
            Path jarDir = resolveJarDir();
            Path subjectsDir = jarDir.resolve("subjects");

            if (!Files.exists(subjectsDir)) {
                Files.createDirectories(subjectsDir);
                unpackSubjects(subjectsDir);
                System.out.println("Билеты распакованы в: " + subjectsDir);
            }

            return subjectsDir;
        } catch (Exception e) {
            throw new RuntimeException("Не удалось подготовить папку subjects", e);
        }
    }

    // Определяем папку, где лежит jar (или корень проекта в IDE)
    private static Path resolveJarDir() throws Exception {
        URL url = FileContent.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();

        Path codePath = Paths.get(url.toURI());

        // В jar: codePath = .../app.jar → берём родителя
        if (Files.isRegularFile(codePath)) {
            return codePath.getParent();
        }

        // В IDE: codePath = .../out/production/classes → поднимаемся до корня проекта
        // (обычно на 3 уровня вверх: classes → production → out → корень)
        Path p = codePath;
        for (int i = 0; i < 3 && p.getParent() != null; i++) {
            p = p.getParent();
        }
        return p;
    }

    // Распаковываем subjects из ресурсов в целевую папку
    private static void unpackSubjects(Path target) throws Exception {
        URL url = FileContent.class
                .getProtectionDomain()
                .getCodeSource()
                .getLocation();

        Path codePath = Paths.get(url.toURI());

        if (Files.isRegularFile(codePath)) {
            unpackFromJar(codePath, target);
        } else {
            unpackFromDir(codePath.resolve("subjects"), target);
        }
    }

    // Распаковка из jar
    private static void unpackFromJar(Path jarPath, Path target) throws IOException {
        try (JarFile jar = new JarFile(jarPath.toFile())) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();

                if (!name.startsWith("subjects/") || entry.isDirectory()) {
                    continue;
                }

                // subjects/Матан/Иванов.txt → target/Матан/Иванов.txt
                String relative = name.substring("subjects/".length());
                Path out = target.resolve(relative);

                Files.createDirectories(out.getParent());
                try (InputStream is = jar.getInputStream(entry)) {
                    Files.copy(is, out, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    // Распаковка из папки (когда запускаем в IDE)
    private static void unpackFromDir(Path source, Path target) throws IOException {
        if (!Files.exists(source)) return;

        try (var stream = Files.walk(source)) {
            stream.forEach(src -> {
                try {
                    Path relative = source.relativize(src);
                    Path dst = target.resolve(relative);

                    if (Files.isDirectory(src)) {
                        Files.createDirectories(dst);
                    } else {
                        Files.createDirectories(dst.getParent());
                        Files.copy(src, dst, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    // ==================== Content ====================
    @Override
    public List<String> listSubjects() {
        List<String> result = new ArrayList<>();
        File base = baseDir.toFile();
        if (!base.isDirectory()) return result;

        File[] items = base.listFiles();
        if (items == null) return result;

        for (File item : items) {
            if (item.isDirectory()) {
                result.add(item.getName());
            }
        }
        return result;
    }

    @Override
    public List<String> listAuthors(String subject) {
        List<String> result = new ArrayList<>();
        File base = baseDir.resolve(subject).toFile();
        if (!base.isDirectory()) return result;

        File[] items = base.listFiles();
        if (items == null) return result;

        for (File item : items) {
            String name = item.getName();
            if (name.endsWith(".txt")) {
                result.add(name.substring(0, name.length() - 4));
            }
        }
        return result;
    }

    @Override
    public String readContent(String subject, String author) {
        Path file = baseDir.resolve(subject).resolve(author + ".txt");
        try {
            return Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Не удалось прочитать " + file, e);
        }
    }
}
