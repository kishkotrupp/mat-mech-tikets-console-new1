package org.kishkotrupp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.File;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TgDialogTest {
    @TempDir
    Path tempDir; //временная папка на диске

    TgDialog bot;
    File subjectDir;

    @BeforeEach
    void setUp() throws Exception {
        subjectDir = new File(tempDir.toFile(), "алгем");
        subjectDir.mkdirs();

        RatingStorage mockRating = mock(RatingStorage.class);

        Collection collection = new Collection(new StringContent(tempDir.toString()), mockRating);
        bot = spy(new TgDialog("token", "test_bot", collection));

        doReturn(null).when(bot).execute(any(SendMessage.class));
    }

    private void send(String caption, String fileName, int size) throws Exception {
        Document doc = mock(Document.class);
        when(doc.getFileId()).thenReturn("id");
        when(doc.getFileName()).thenReturn(fileName);
        when(doc.getFileSize()).thenReturn((long) size);

        Chat chat = mock(Chat.class);
        when(chat.getId()).thenReturn(1L);

        Message msg = mock(Message.class);
        when(msg.getChatId()).thenReturn(1L);
        when(msg.getChat()).thenReturn(chat);
        when(msg.getDocument()).thenReturn(doc);
        when(msg.getCaption()).thenReturn(caption);

        Update update = mock(Update.class);
        when(update.getMessage()).thenReturn(msg);


        bot.onUpdateReceived(update);
    }

    private void assertNothingSaved() {
        assertEquals(0, subjectDir.list().length);
    }

    @Test
    void noCaption() throws Exception {
        send(null, "a.pdf", 100);
        assertNothingSaved();
    }

    @Test
    void unknownSubject() throws Exception {
        send("нет такого", "a.pdf", 100);
        assertNothingSaved();
    }

    @Test
    void nameTooLong() throws Exception {
        send("алгем", "a".repeat(21) + ".pdf", 100);
        assertNothingSaved();
    }

    @Test
    void badExtension() throws Exception {
        send("алгем", "virus.exe", 100);
        assertNothingSaved();
    }

    @Test
    void fileTooBig() throws Exception {
        send("алгем", "big.pdf", 20 * 1024 * 1024 + 1);
        assertNothingSaved();
    }
}