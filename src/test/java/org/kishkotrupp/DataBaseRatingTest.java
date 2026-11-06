package org.kishkotrupp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DataBaseRatingTest {

    @TempDir
    Path tempDir;

    DataBaseRating ratingStorage;

    @BeforeEach
    void setUp() {
        // Создаём базу во временной папке, чтобы не мусорить в проекте
        String dbPath = tempDir.resolve("test_ratings.db").toString();
        ratingStorage = new DataBaseRating(dbPath);
    }

    @Test
    void saveAndGetRating() {
        // Пользователь с ID 1 ставит оценку 8 файлу a.pdf по предмету алгем
        ratingStorage.saveRating("алгем", "a.pdf", 1L, 8);

        // Проверяем среднее (должно быть 8.0) и количество (1)
        assertEquals(8.0, ratingStorage.getMiddleRating("алгем", "a.pdf"));
        assertEquals(1, ratingStorage.getRatingCountPeople("алгем", "a.pdf"));
    }

    @Test
    void averageRatingOfMultipleUsers() {
        // Три разных пользователя ставят оценки
        ratingStorage.saveRating("алгем", "a.pdf", 1L, 8);
        ratingStorage.saveRating("алгем", "a.pdf", 2L, 10);
        ratingStorage.saveRating("алгем", "a.pdf", 3L, 6);

        // (8 + 10 + 6) / 3 = 8.0
        assertEquals(8.0, ratingStorage.getMiddleRating("алгем", "a.pdf"));
        assertEquals(3, ratingStorage.getRatingCountPeople("алгем", "a.pdf"));
    }

    @Test
    void userCanChangeRating() {
        // Пользователь сначала поставил 5, потом передумал и поставил 10
        ratingStorage.saveRating("алгем", "a.pdf", 1L, 5);
        ratingStorage.saveRating("алгем", "a.pdf", 1L, 10);

        // Оценка должна обновиться (среднее = 10, количество всё ещё 1)
        assertEquals(10.0, ratingStorage.getMiddleRating("алгем", "a.pdf"));
        assertEquals(1, ratingStorage.getRatingCountPeople("алгем", "a.pdf"));
    }

    @Test
    void emptyRating() {
        // Если никто не голосовал, среднее = 0.0, количество = 0
        assertEquals(0.0, ratingStorage.getMiddleRating("нет такого", "файла.pdf"));
        assertEquals(0, ratingStorage.getRatingCountPeople("нет такого", "файла.pdf"));
    }
}
