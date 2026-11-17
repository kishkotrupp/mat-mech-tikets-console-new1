package org.kishkotrupp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DataBaseRating implements RatingStorage {

    private final Path filePath;
    private static final String DB_FILE = "ratings.txt";

    public DataBaseRating() {
        this(DB_FILE);
    }

    public DataBaseRating(String filePath) {
        this.filePath = Paths.get(filePath);
        try {
            if (!Files.exists(this.filePath)) {
                Files.createFile(this.filePath);
            }
        } catch (IOException e) {
            throw new RuntimeException("Не удалось создать файл для рейтинга", e);
        }
    }


    @Override
    public void saveRating(String subject, String author, long userId, int score) {
        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            List<String> newLines = new ArrayList<>();

            String prefix = subject + ";" + author + ";" + userId + ";";
            boolean finded = false;

            for (String line : lines) {
                if (line.startsWith(prefix)) {
                    newLines.add(prefix + score);
                    finded = true;
                } else {
                    newLines.add(line);
                }
            }
            if (!finded){
                newLines.add(prefix + score);
            }
            Files.write(filePath, newLines, StandardCharsets.UTF_8);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public double getMiddleRating(String subject, String author) {
        int count = 0;
        int sum = 0;

        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split(";");
                if (parts.length == 4 && parts[0].equals(subject) && parts[1].equals(author)) {
                    sum += Integer.parseInt(parts[3]);
                    count++;
                }
            }
        } catch (IOException | NumberFormatException e) {
            e.printStackTrace();
        }

        return count == 0 ? 0.0 : (double) sum / count;
    }

    @Override
    public int getRatingCountPeople(String subject, String author) {
        int count = 0;

        try {
            List<String> lines = Files.readAllLines(filePath, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split(";");
                if (parts.length == 4 && parts[0].equals(subject) && parts[1].equals(author)) {
                    count++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return count;
    }
}
