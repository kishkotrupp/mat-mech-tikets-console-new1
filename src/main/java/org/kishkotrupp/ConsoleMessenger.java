package org.kishkotrupp;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConsoleMessenger implements Messenger{
    private final IO io;

    private final Map<String, String> callbackData = new HashMap<>();

    public ConsoleMessenger(IO io) {
        this.io = io;
    }

    @Override
    public void sendText(String text) {
        io.println(text);
    }

    @Override
    public void sendMenu(String text, List<String> labels, List<String> commands, int columns) {
        io.println(text);
        for (int i = 0; i < labels.size(); i++) {
            String label = labels.get(i);
            String command = commands.get(i);
            callbackData.put(label, command);
            // В консоли мы просто показываем, какую команду надо ввести
            io.println("  -> " + label);
        }
        io.print("> ");
    }

    @Override
    public void sendFile(String caption, File file) {
        io.println("---- " + caption + " ----");
            try {
                io.println(Files.readString(file.toPath(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                io.println("Ошибка чтения файла: " + e.getMessage());
            }
        io.println("------------------------------------");
    }

    @Override
    public boolean supportsFile(String fileName) {
        if (fileName.toLowerCase().endsWith(".txt")){
            return true;
        }
        return false;
    }

    public String getCallbackData(String input) {
        if (callbackData.containsKey(input)) {
            return callbackData.get(input);
        }

        for (Map.Entry<String, String> entry : callbackData.entrySet()) {
            if (entry.getKey().trim().startsWith(input)) {
                return entry.getValue();
            }
        }

        return input;
    }

    @Override
    public void sendLectures(List<LectureStorage> lectures) {
        io.println("Доступные лекции:");
        for (LectureStorage lecture : lectures) {
            io.println("  - " + lecture.getTitle());
            io.println("    " + lecture.getUrl());
        }
    }
}
