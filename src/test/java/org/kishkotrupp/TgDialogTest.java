package org.kishkotrupp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.*;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TgDialogTest {
    @TempDir
    Path tempDir; //временная папка на диске

    TgDialog bot;
    TicketManager mockTicketManager;
    LectureService mockLectureService;

    @BeforeEach
    void setUp() throws Exception {
        mockTicketManager = mock(TicketManager.class);
        mockLectureService = mock(LectureService.class);

        // можем перехватывать execute()
        bot = spy(new TgDialog("fake_token", "test_bot", mockTicketManager, mockLectureService));

        doReturn(null).when(bot).execute(any(SendMessage.class));
        doReturn(null).when(bot).execute(any(AnswerCallbackQuery.class));
        doReturn(null).when(bot).execute(any(SendDocument.class));
    }

    @Test
    void handleTextMessage_Start_ShouldSendWelcomeAndMenu() throws Exception {
        Message msg = new Message();
        msg.setText("/start");
        Chat chat = new Chat();
        chat.setId(123L);
        msg.setChat(chat);

        Update update = new Update();
        update.setMessage(msg);

        // Update — это «конверт», в который Telegram упаковывает все события
        bot.onUpdateReceived(update);

        // sendText и sendMenu, минимум 2 вызова execute
        verify(bot, atLeast(2)).execute(any(SendMessage.class));
    }

    @Test
    void handleCallbackQuery_ShouldAnswerAndProcessCommand() throws Exception {
        // нажатие на кнопку "Билеты" (menu:tickets)
        CallbackQuery callback = new CallbackQuery();
        callback.setId("call_123");
        callback.setData("menu:tickets");

        Message msg = new Message();
        Chat chat = new Chat();
        chat.setId(123L);
        msg.setChat(chat);
        callback.setMessage(msg);

        User user = new User();
        user.setId(999L);
        callback.setFrom(user);

        Update update = new Update();
        update.setCallbackQuery(callback);

        bot.onUpdateReceived(update);

        // ответить на callback, чтобы перестала мигать кнопка
        verify(bot, times(1)).execute(any(AnswerCallbackQuery.class));
        verify(mockTicketManager, times(1)).listSubjects();
    }

    @Test
    void handleDocument_ValidFile_ShouldDownloadAndSave() throws Exception {
        Document doc = new Document();
        doc.setFileName("test.pdf");
        doc.setFileId("file_123");
        doc.setFileSize(1024L);

        Message msg = new Message();
        Chat chat = new Chat();
        chat.setId(123L);
        msg.setChat(chat);
        msg.setDocument(doc);
        msg.setCaption("math");

        Update update = new Update();
        update.setMessage(msg);

        // Настраиваем моки для скачивания файла
        // файл лежит на сервере Telegram по адресу path/to/file
        org.telegram.telegrambots.meta.api.objects.File mockTgFile = new org.telegram.telegrambots.meta.api.objects.File();
        mockTgFile.setFilePath("path/to/file");

        // Когда попросишь файл из телеграма, верни эту фейковую инфу
        doReturn(mockTgFile).when(bot).execute(any(org.telegram.telegrambots.meta.api.methods.GetFile.class));

        // Когда начнешь скачивать, верни пустой поток
        // InputStream.nullInputStream() выглядит для программы как настоящий поток данных
        doReturn(InputStream.nullInputStream()).when(bot).downloadFileAsStream(anyString());

        when(mockTicketManager.listSubjects()).thenReturn(Arrays.asList("math", "physics"));

        bot.onUpdateReceived(update);

        // сохранение файла
        verify(mockTicketManager, times(1)).saveFile(eq("math"), eq("test.pdf"), any(InputStream.class));
    }
}