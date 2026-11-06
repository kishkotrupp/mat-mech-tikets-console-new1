package org.kishkotrupp;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.List;

public interface Messenger {
    void sendText(String text);
    void sendMenu(String text, List<String> labels, List<String> commands, int columns);
    void sendFile(String caption, File file);
    boolean supportsFile(String fileName);

    void sendLectures(List<LectureStorage> lectures);
}
