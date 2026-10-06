package org.kishkotrupp;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileContentTest {

    @TempDir
    Path tempDir;

    private FileContent content;
    // подготовка виртуальной директории
    private void createVDir() throws IOException {
        Files.createDirectory(tempDir.resolve("math"));
        Files.createDirectory(tempDir.resolve("physics"));

        Files.writeString(tempDir.resolve("math/Abby.txt"),
                "Abby tolk about geometry", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("math/Andrey.txt"),
                "Andrey tolk about algebra", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("physics/Bibob.txt"),
                "Bibob tolk about newton", StandardCharsets.UTF_8);
        // файл в корне — не должен попасть в список предметов
        Files.writeString(tempDir.resolve("readme.md"), "you must dont know wtf is there");

        content = new FileContent(tempDir);
    }

    @Test
    void listSubjectsReturnOnlyDirectories() throws IOException {
        createVDir();
        List<String> subjects = content.listSubjects();

        assertEquals(2, subjects.size());
        assertTrue(subjects.contains("math"));
        assertTrue(subjects.contains("physics"));
        assertFalse(subjects.contains("readme.md"));
    }
    // создаем путь которого не существует
    @Test
    void listSubjectsReturnEmptyWhenDirDoesntExist() {
        FileContent c = new FileContent(tempDir.resolve("doesnt_exist"));
        assertTrue(c.listSubjects().isEmpty());
    }
    //создаем пустую папку
    @Test
    void listSubjectsReturnEmptyWhenDirIsEmpty() throws IOException {
        Files.createDirectory(tempDir.resolve("empty_dir"));
        FileContent c = new FileContent(tempDir.resolve("empty_dir"));
        assertTrue(c.listSubjects().isEmpty());
    }
    //проверяем есть ли все авторы и то что они выводятся без txt
    @Test
    void listAuthorsReturnAllAuthors() throws IOException {
        createVDir();
        List<String> authors = content.listAuthors("math");

        assertEquals(2, authors.size());
        assertTrue(authors.contains("Abby"));
        assertTrue(authors.contains("Andrey"));
        assertFalse(authors.contains("Abby.txt"));
    }
    //авторов не должно быть по несуществующей папке
    @Test
    void listAuthorsReturnEmptyWhenDirDoesntExist() throws IOException {
        createVDir();
        assertTrue(content.listAuthors("doesnt_exist").isEmpty());
    }
    //другие форматы кроме txt должны игнорироваться
    @Test
    void listAuthorsIgnoreNoTxtFormat() throws IOException {
        createVDir();
        Files.writeString(tempDir.resolve("math/VolanDeMort.md"), "не txt");
        List<String> authors = content.listAuthors("math");
        assertFalse(authors.contains("VolanDeMort"));
    }

    @Test
    void readContentReturnsFileContent() throws IOException {
        createVDir();
        String text = content.readContent("math", "Abby");
        assertEquals("Abby tolk about geometry", text);
    }
}
