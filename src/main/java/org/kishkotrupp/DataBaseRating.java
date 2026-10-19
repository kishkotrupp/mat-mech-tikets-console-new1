package org.kishkotrupp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DataBaseRating implements RatingStorage {

    private final String dbUrl;

    public DataBaseRating(String dbPath) {
        // JDBC (Java Database Connectivity) требует, чтобы путь начинался с префикса.
        this.dbUrl = "jdbc:sqlite:" + dbPath;
        initDatabase();
    }

    private void initDatabase() {
        String sql = """
        CREATE TABLE IF NOT EXISTS ratings (
            subject TEXT NOT NULL,
            author TEXT NOT NULL,
            user_id INTEGER NOT NULL,
            score INTEGER NOT NULL CHECK(score BETWEEN 1 AND 10),
            PRIMARY KEY (subject, author, user_id)
        );
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             // создаём инструмент для отправки простого запроса
             Statement stmt = conn.createStatement()) {
            // выполняем наш SQL (создание таблицы)
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка инициализации БД", e);
        }
    }

    @Override
    public void saveRating(String subject, String author, long userId, int score) {
        String sql = """
        // вставить новую строку
        INSERT INTO ratings (subject, author, user_id, score)
        VALUES (?, ?, ?, ?)
        // если такой ключ уже есть обнови score на то что мы пытались вставить в score
        ON CONFLICT(subject, author, user_id) DO UPDATE SET score = excluded.score
        """;

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            // подставляем под вопросы
            pstmt.setString(1, subject);
            pstmt.setString(2, author);
            pstmt.setLong(3, userId);
            pstmt.setInt(4, score);
            pstmt.executeUpdate(); // отправить в базу обновить
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getMiddleRating(String subject, String author) {
        // посчитай среднее арифметическое для колонки score
        String sql = "SELECT AVG(score) FROM ratings WHERE subject = ? AND author = ?";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, subject);
            pstmt.setString(2, author);
            try (ResultSet rs = pstmt.executeQuery()) { // Прочитай и покажи. ResultSet - таблица с данными результатов
                // на строку вниз и есть че то есть дай значение из колонки 1
                if (rs.next()) return rs.getDouble(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0.0;
    }

    @Override
    public int getRatingCountPeople(String subject, String author) {
        return 0;
    }
}
