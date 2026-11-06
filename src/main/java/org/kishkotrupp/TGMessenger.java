package org.kishkotrupp;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TGMessenger implements Messenger {
    private final TelegramLongPollingBot bot;
    private final long chatId;

    public TGMessenger(TelegramLongPollingBot bot, long chatId) {
        this.bot = bot;
        this.chatId = chatId;
    }

    @Override
    public void sendText(String text) {
        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        try {
            bot.execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendMenu(String text, List<String> labels, List<String> commands, int columns) {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> currentRow = new ArrayList<>();

        for (int i = 0; i < labels.size(); i++) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(labels.get(i));
            btn.setCallbackData(commands.get(i));
            currentRow.add(btn);

            // Если набрали нужное количество колонок или это последняя кнопка
            if (currentRow.size() == columns || i == labels.size() - 1) {
                rows.add(currentRow);
                currentRow = new ArrayList<>();
            }
        }
        //Создаём объект разметки клавиатуры.
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        //кладем суда список списков кнопок
        keyboard.setKeyboard(rows);

        SendMessage msg = new SendMessage(String.valueOf(chatId), text);
        msg.setReplyMarkup(keyboard);
        try {
            bot.execute(msg);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendFile(String caption, File file) {
        SendDocument doc = new SendDocument();
        doc.setChatId(String.valueOf(chatId));
        doc.setDocument(new InputFile(file));
        doc.setCaption(caption);
        try {
            bot.execute(doc);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean supportsFile(String fileName) {
        return true;
    }
}