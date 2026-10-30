package org.kishkotrupp;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class TgDialog extends TelegramLongPollingBot {

    private static final String BOT_USERNAME = "MathMechTicketsBot";
    private final String token;
    private final TicketManager ticketManager;

    public TgDialog(String token, TicketManager ticketManager) {
        this.token = token;
        this.ticketManager = ticketManager;
    }

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                long chatId = update.getMessage().getChatId();
                long userId = update.getMessage().getFrom().getId();
                DialogService dialog = new DialogService(ticketManager, new TgMessenger(chatId));
                dialog.handle(userId, update.getMessage().getText());
            } else if ((update.hasMessage() && update.getMessage().hasDocument())) {
                handleDocument(update);
            } else if (update.hasCallbackQuery()) {
                long chatId = update.getCallbackQuery().getMessage().getChatId();
                long userId = update.getCallbackQuery().getFrom().getId();
                String data = update.getCallbackQuery().getData();

                // без этого тг ругается
                execute(new AnswerCallbackQuery(update.getCallbackQuery().getId()));

                if (data != null) {
                    DialogService dialog = new DialogService(ticketManager, new TgMessenger(chatId));
                    dialog.handle(userId, data);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class TgMessenger implements Messenger {
    private final long chatId;

    TgMessenger(long chatId) {
        this.chatId = chatId;
    }

    @Override
    public void sendMenu(String text, List<String> labels, List<String> commands, int columns) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        for (int i = 0; i < labels.size(); i++) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(labels.get(i));
            btn.setCallbackData(commands.get(i));
            row.add(btn);

            if (row.size() == columns) {
                rows.add(row);
                row = new ArrayList<>();
            }
        }
        if (!row.isEmpty()) {
            rows.add(row);
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);

        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText(text);
        msg.setReplyMarkup(keyboard);
        send(msg);
    }

    @Override
    public void sendFile(String caption, File file) {
        if (file.length() > 20L * 1024 * 1024) {
            sendText("Файл больше 20 МБ Telegram не пропустит.");
            return;
        }

        SendDocument doc = new SendDocument();
        doc.setChatId(String.valueOf(chatId));
        doc.setDocument(new InputFile(file));
        doc.setCaption(caption);
        try {
            execute(doc);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendText(String text) {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText(text);
        send(msg);
    }

    private void send(SendMessage msg) {
        try {
            execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }
}

    private void handleDocument(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        Document doc = update.getMessage().getDocument();
        String subject = update.getMessage().getCaption();
        String name = doc.getFileName();
        Long size = doc.getFileSize() == null ? 0 : doc.getFileSize().longValue();

        TgMessenger messenger = new TgMessenger(chatId);
        DialogService dialog = new DialogService(ticketManager, messenger);

        if (!dialog.checkUpload(subject, name, size)) {
            return;
        }

        try {
            org.telegram.telegrambots.meta.api.objects.File tgFile = execute(new GetFile(doc.getFileId()));
            try (InputStream in = downloadFileAsStream(tgFile.getFilePath())) {
                dialog.saveUpload(subject, name, in);
            }

        } catch (IOException e) {
            e.printStackTrace();
            messenger.sendText("Не получилось сохранить файл.");
        }
    }


}
