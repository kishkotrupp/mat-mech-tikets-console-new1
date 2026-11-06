package org.kishkotrupp;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
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
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;

public class TgDialog extends TelegramLongPollingBot {

    private final String token;
    private final String username;
    private final TicketManager ticketManager;
    private static final String BOT_USERNAME = "MathMechTicketsBot";

    public TgDialog(String token, String username, TicketManager ticketManager) {
        this.token = token;
        this.username = username;
        this.ticketManager = ticketManager;
    }

    public TgDialog(String token, TicketManager ticketManager) {
        this(token, BOT_USERNAME, ticketManager);
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage() && update.getMessage().hasText()) {
                handleTextMessage(update);
            } else if ((update.hasMessage() && update.getMessage().hasDocument())) {
                handleDocument(update);
            } else if (update.hasCallbackQuery()) {
                handleCallbackQuery(update);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleTextMessage(Update update) throws TelegramApiException {
        long chatId = update.getMessage().getChatId();
        String text = update.getMessage().getText().trim();

        Messenger messenger = new TGMessenger(this, chatId);
        DialogService dialog = new DialogService(ticketManager, messenger);
        dialog.handle(chatId, update.getMessage().getText());
    }

    private void handleCallbackQuery(Update update) throws TelegramApiException {
        String data = update.getCallbackQuery().getData();
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        // callbackId — это строка вида "4382910756893210472". Она уникальна для каждого нажатия.
        String callbackId = update.getCallbackQuery().getId();
        // получаем ID пользователя
        long userId = update.getCallbackQuery().getFrom().getId();
        // execute(...) — отправляем в Telegram, создаем объект «ответить на callback с ID X» (нажатие на кнопочку) она от этого пиликает и через 10 сек ругается если мы ничего не отправим
        //execute(new AnswerCallbackQuery(callbackId));


        if (data == null) return;

        Messenger messenger = new TGMessenger(this, chatId);
        DialogService dialog = new DialogService(ticketManager, messenger);
        dialog.handle(userId, data);
    }

    private void handleDocument(Update update) {
        long chatId = update.getMessage().getChatId();
        Document doc = update.getMessage().getDocument();
        String subject = update.getMessage().getCaption();
        String name = doc.getFileName();
        long size = doc.getFileSize() != null ? doc.getFileSize() : 0;

        Messenger messenger = new TGMessenger(this, chatId);
        DialogService dialog = new DialogService(ticketManager, messenger);

        // Проверяем файл через DialogService (он сам напишет ошибку, если что-то не так)
        if (!dialog.checkUpload(subject, name, size)) {
            return;
        }

        try {
            // Скачиваем файл из Telegram
            org.telegram.telegrambots.meta.api.objects.File tgFile = execute(new GetFile(doc.getFileId()));
            try (InputStream in = downloadFileAsStream(tgFile.getFilePath())) {
                // Отдаем файл в DialogService для сохранения
                dialog.saveUpload(subject, name, in);
            }
        } catch (Exception e) {
            e.printStackTrace();
            messenger.sendText("Не получилось скачать файл из Telegram.");
        }
    }

}