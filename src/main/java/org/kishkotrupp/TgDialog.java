package org.kishkotrupp;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class TgDialog extends TelegramLongPollingBot {

    private static final String BACK_LABEL = "⬅️ Назад";
    private static final String BACK_DATA = "back";
    private static final String SUBJ_PREFIX = "subj:";
    private static final String FILE_PREFIX = "file:";
    private static final String RATE_PREFIX = "rate:";

    private final String token;
    private final String username;
    private final Collection collection;

    public TgDialog(String token, String username, Collection collection) {
        this.token = token;
        this.username = username;
        this.collection = collection;
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

        switch (text) {
            case "/start":
            case "/list":
                sendWelcome(chatId);
                showSubjects(chatId);
                break;
            case "/help":
                showHelp(chatId);
                break;
            default:
                sendText(chatId, "Напиши /start, чтобы начать, или /help для справки.");
        }
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

        if (data.equals(BACK_DATA)) {
            execute(new AnswerCallbackQuery(callbackId));
            showSubjects(chatId);
        } else if (data.startsWith(SUBJ_PREFIX)) {
            execute(new AnswerCallbackQuery(callbackId));
            String subject = data.substring(SUBJ_PREFIX.length());
            showAuthors(chatId, subject);
        } else if (data.startsWith(FILE_PREFIX)) {
            execute(new AnswerCallbackQuery(callbackId));
            String payload = data.substring(FILE_PREFIX.length());
            int sep = payload.indexOf(':');
            if (sep < 0) {
                sendText(chatId, "Некорректные данные кнопки.");
                return;
            }
            String subject = payload.substring(0, sep);
            String author = payload.substring(sep + 1);

            sendFile(chatId, subject, author);
            sendRatingKeyboard(chatId, subject, author);
        } else if (data.startsWith(RATE_PREFIX)) {
            String payload = data.substring(RATE_PREFIX.length());
            String[] parts = payload.split(":");
            if (parts.length == 3) {
                String subject = parts[0];
                String author = parts[1];
                int score = Integer.parseInt(parts[2]);

                collection.saveRating(subject, author, userId, score);

                AnswerCallbackQuery answer = new AnswerCallbackQuery(callbackId);
                answer.setText("Оценка сохранена!");
                execute(answer);
                showSubjects(chatId);
            }
        }
    }


    private void showSubjects(long chatId) throws TelegramApiException {
        List<String> subjects = collection.listSubjects();
        if (subjects.isEmpty()) {
            sendText(chatId, "Папка subjects/ пуста — предметов нет.");
            return;
        }

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (String subject : subjects) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(subject);
            btn.setCallbackData(SUBJ_PREFIX + subject);
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(btn);
            rows.add(row);
        }

        //Создаём объект разметки клавиатуры.
        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        //кладем суда список списков кнопок
        keyboard.setKeyboard(rows);

        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Выбери предмет:");
        // Прикрепляем клавиатуру к сообщению
        msg.setReplyMarkup(keyboard);
        execute(msg);
    }

    private void showAuthors(long chatId, String subject) throws TelegramApiException {
        List<String> authors = collection.listAuthors(subject);
        if (authors.isEmpty()) {
            sendText(chatId, "В предмете " + subject + " нет файлов.");
            return;
        }

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (String author : authors) {
            InlineKeyboardButton btn = new InlineKeyboardButton();

            double middleRating = collection.getMiddleRating(subject, author);
            int countPeople = collection.getRatingCountPeople(subject, author);
            String ratingStr = (countPeople > 0) ? String.format(" ★%.1f (%d)", middleRating, countPeople) : "  ";

            btn.setText(author + ratingStr);
            btn.setCallbackData(FILE_PREFIX + subject + ":" + author);
            List<InlineKeyboardButton> row = new ArrayList<>();
            row.add(btn);
            rows.add(row);
        }

        InlineKeyboardButton back = new InlineKeyboardButton();
        back.setText(BACK_LABEL);
        back.setCallbackData(BACK_DATA);
        List<InlineKeyboardButton> backRow = new ArrayList<>();
        backRow.add(back);
        rows.add(backRow);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);

        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Авторы по предмету " + subject + ":");
        msg.setReplyMarkup(keyboard);
        execute(msg);
    }

    private void sendRatingKeyboard(long chatId, String subject, String author) throws TelegramApiException {
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row1 = new ArrayList<>();
        List<InlineKeyboardButton> row2 = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            InlineKeyboardButton btn = new InlineKeyboardButton();
            btn.setText(String.valueOf(i));
            btn.setCallbackData(RATE_PREFIX + subject + ":" + author + ":" + i);

            if (i <= 5) row1.add(btn);
            else row2.add(btn);
        }

        rows.add(row1);
        rows.add(row2);

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(rows);

        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText("Оцени автора " + author + " от 1 до 10:");
        msg.setReplyMarkup(keyboard);
        execute(msg);
    }


    private void sendFile(long chatId, String subject, String author) throws TelegramApiException {
        File file = collection.getFile(subject, author);
        if (file == null) {
            sendText(chatId, "Файл не найден: " + author);
            return;
        }
        if (file.length() > 50L * 1024 * 1024) {
            sendText(chatId, "Файл больше 50 МБ — Telegram не пропустит.");
            return;
        }

        SendDocument doc = new SendDocument();
        doc.setChatId(String.valueOf(chatId));
        doc.setDocument(new InputFile(file));
        doc.setCaption(subject + " / " + author);
        execute(doc);
    }

    private void sendWelcome(long chatId) throws TelegramApiException {
        sendText(chatId,
                "Привет! Я бот для подготовки к экзаменам на матмехе.\n" +
                        "Выбери предмет, потом файл — я пришлю его тебе.\n" +
                        "Нажми /help для справки.");
    }

    private void showHelp(long chatId) throws TelegramApiException {
        sendText(chatId,
                "=== СПРАВКА ===\n" +
                        "1) /start — показать список предметов.\n" +
                        "2) Нажми на предмет — увидишь файлы.\n" +
                        "3) Нажми на файл — я его пришлю.\n" +
                        "4) Кнопка «Назад» возвращает к списку предметов.");
    }

    private void sendText(long chatId, String text) throws TelegramApiException {
        SendMessage msg = new SendMessage();
        msg.setChatId(String.valueOf(chatId));
        msg.setText(text);
        execute(msg);
    }
}