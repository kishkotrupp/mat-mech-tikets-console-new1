package org.kishkotrupp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class FileTicketStorageTest {
    @TempDir
    Path tempDir;

    FileTicketStorage storage;

    @BeforeEach
    void setUp() {
        // Инициализируем хранилище во временной папке
        storage = new FileTicketStorage(tempDir.toString());
    }

    private void createTestFiles() throws IOException {
        Files.createDirectory(tempDir.resolve("math"));
        Files.createDirectory(tempDir.resolve("physics"));

        Files.writeString(tempDir.resolve("math/Abby.txt"), "Geometry text", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("math/Andrey.pdf"), "PDF content", StandardCharsets.UTF_8);
        Files.writeString(tempDir.resolve("physics/Bibob.docx"), "DOCX content", StandardCharsets.UTF_8);

        Files.writeString(tempDir.resolve("readme.md"), "you must dont know wtf is there");
    }

    @Test
    void listSubjectsReturnOnlyDirectories() throws IOException {
        createTestFiles();
        List<String> subjects = storage.listSubjects();

        assertEquals(2, subjects.size());
        assertTrue(subjects.contains("math"));
        assertTrue(subjects.contains("physics"));
        assertFalse(subjects.contains("readme.md"));
    }

    @Test
    void listAuthorsReturnAllFilesWithExtensions() throws IOException {
        createTestFiles();
        List<String> authors = storage.listAuthors("math");

        assertEquals(2, authors.size());
        assertTrue(authors.contains("Abby.txt"));
        assertTrue(authors.contains("Andrey.pdf"));
    }
    // создаем путь которого не существует
    @Test
    void listAuthorsReturnEmptyForNonExistentSubject() {
        assertTrue(storage.listAuthors("doesnt_exist").isEmpty());
    }

    @Test
    void getFileReturnsFileIfExists() throws IOException {
        createTestFiles();
        File file = storage.getFile("math", "Abby.txt");

        assertNotNull(file);
        assertTrue(file.exists());
        assertEquals("Abby.txt", file.getName());
    }

    @Test
    void getFileReturnsNullForNonExistentFile() throws IOException {
        createTestFiles();
        assertNull(storage.getFile("math", "no_such_file.txt"));
        assertNull(storage.getFile("no_such_subject", "Abby.txt"));
    }

    // Проверка безопасности: нельзя выйти за пределы папки subjects
    @Test
    void getFileBlocksPathTraversalAttack() {
        assertNull(storage.getFile("math", "../secret.txt"));
        assertNull(storage.getFile("..", "secret.txt"));
    }

    @Test
    void readFileReturnsContent() throws IOException {
        createTestFiles();
        String content = storage.readFile("math", "Abby.txt");

        assertEquals("Geometry text", content);
    }

    @Test
    void listAuthorsIgnoreNoTxtFormat() throws IOException {
        createTestFiles();
        Files.writeString(tempDir.resolve("math/VolanDeMort.md"), "не txt");
        List<String> authors = storage.listAuthors("math");
        assertFalse(authors.contains("VolanDeMort"));
    }

    @Test
    void readFileReturnsNullForMissingFile() {
        assertNull(storage.readFile("math", "doesnt_exist.txt"));
    }

    @Test
    void saveFileCreatesNewFile() throws IOException {
        Files.createDirectory(tempDir.resolve("math"));

        String testData = "New file data from Telegram";
        InputStream stream = new ByteArrayInputStream(testData.getBytes(StandardCharsets.UTF_8));

        storage.saveFile("math", "NewFile.txt", stream);

        File savedFile = storage.getFile("math", "NewFile.txt");
        assertNotNull(savedFile);
        assertTrue(savedFile.exists());

        assertEquals(testData, storage.readFile("math", "NewFile.txt"));
    }

}
