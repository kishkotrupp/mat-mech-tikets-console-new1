package org.kishkotrupp;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DialogService {


    public enum Command {
        START("/start"), LIST("/list"), HELP("/help"), BACK("back"),
        MENU_TICKETS("menu:tickets"), MENU_LECTURES("menu:lectures"),
        SUBJ("subj:"), FILE("file:"), RATE("rate:");

        private final String value;

        Command(String value) {
            this.value = value;
        }
        public String getValue() {
            return value;
        }

        // Метод для получения команды из строки
        public static Command fromString(String text) {
            for (Command cmd : values()) {
                if (text.startsWith(cmd.value)) return cmd;
            }
            return null; // Или бросаем исключение
        }
    }

    private static final int MAX_NAME_LEN = 20;
    private static final long MAX_UPLOAD_BYTES = 20L * 1024* 1024;
    private static final Set<String> ALLOWED_EXT = Set.of("pdf", "doc", "docx", "txt");

    private static final String WELCOME =
            "Привет! Я бот для подготовки к экзаменам на матмехе!\n" +
                    "Выбери предмет, потом автора — я покажу билеты.\n" +
                    "Напиши /help для справки.";
    private static final String HELP_TEXT =
            "=== СПРАВКА ===\n" +
                    "1) /start — показать список предметов.\n" +
                    "2) Выбери предмет — увидишь авторов.\n" +
                    "3) Выбери автора — получишь файл и сможешь поставить оценку.\n" +
                    "4) «Назад» возвращает к списку предметов.\n" +
                    "5) В Telegram можно добавить файл: отправь его как документ, " +
                    "в подписи напиши название предмета. " +
                    "Макс. 20 МБ, имя до " + MAX_NAME_LEN + " символов, форматы: pdf, doc, docx, txt";

    private final TicketManager ticketManager;
    private final Messenger messenger;
    private final LectureService lectureService;

    public DialogService(TicketManager ticketManager, Messenger messenger, LectureService lectureService) {
        this.ticketManager = ticketManager;
        this.messenger = messenger;
        this.lectureService = lectureService;
    }


    public void handle(long userId, String command) {
        command = command.trim();
        Command cmd = Command.fromString(command);

        if (cmd == null) {
            messenger.sendText("Неизвестная команда: «" + command + "». Напиши /start для выхода в меню или /help для справки.");
            return;
        }

        // Извлекаем "полезную нагрузку" (например, "Иван" из "subj:Иван")
        String payload = command.substring(cmd.getValue().length());

        switch (cmd) {
            case START, LIST -> {
                messenger.sendText(WELCOME);
                showMainMenu();
            }
            case HELP -> messenger.sendText(HELP_TEXT);
            case BACK -> showMainMenu();
            case MENU_TICKETS -> showSubjects();
            case MENU_LECTURES -> showLectures();
            case SUBJ -> showAuthors(payload);
            case FILE -> openFile(payload);
            case RATE -> rate(userId, payload);
        }
    }

    private void showMainMenu() {
        List<String> labels = List.of("Просмотр билетов", "Просмотр лекций");
        List<String> commands = List.of(Command.MENU_TICKETS.getValue(), Command.MENU_LECTURES.getValue());
        messenger.sendMenu("Что хочешь посмотреть?", labels, commands, 1);
    }

    private void showLectures() {
        List<LectureStorage> lectures = lectureService.fetchLectures();
        if (lectures.isEmpty()) {
            messenger.sendText("Не удалось загрузить лекции. Попробуй позже.");
        } else {
            messenger.sendLectures(lectures);
        }
    }

    private void showSubjects() {
        List<String> subjects = ticketManager.listSubjects();

        if (subjects.isEmpty()) {
            messenger.sendText("Папка subjects/ пуста — предметов нет.");
            return;
        }

        List<String> labels = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        for (String subject : subjects) {
            labels.add(subject);
            commands.add(Command.SUBJ.getValue() + subject);
        }
        messenger.sendMenu("Выбери предмет:", labels, commands, 1);
    }

    private void showAuthors(String subject){
        List<String> authors = ticketManager.listAuthors(subject);
        if (authors.isEmpty()) {
            messenger.sendText("В предмете " + subject + " нет файлов.");
            return;
        }

        List<String> labels = new ArrayList<>();
        List<String> commands = new ArrayList<>();

        for (String author : authors) {
            if (messenger.supportsFile(author)) {
                double middleRating = ticketManager.getMiddleRating(subject, author);
                int countPeople = ticketManager.getRatingCountPeople(subject, author);
                String ratingStr = (countPeople > 0) ? String.format(Locale.US," ★%.1f (%d)", middleRating, countPeople) : "  ";

                String label = author + ratingStr;
                labels.add(label);
                commands.add(Command.FILE.getValue() + subject + ":" + author);
            }
        }

        labels.add("⬅️ Назад");
        commands.add(Command.BACK.getValue());


        messenger.sendMenu("Авторы по предмету ", labels, commands, 1);
    }

    private void showRatingMenu(String subject, String author) {
        List<String> labels = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            labels.add(String.valueOf(i));
            commands.add(Command.RATE.getValue() + subject + ":" + author + ":" + i);
        }
        labels.add("Пропустить");
        commands.add(Command.BACK.getValue());

        messenger.sendMenu("Оцени автора " + author + " от 1 до 10:", labels, commands, 5);
    }

    private void rate(long userId, String payload) {
        // payload = "предмет:автор:оценка"
        int first = payload.indexOf(':');
        int last = payload.lastIndexOf(':');

        String subject = payload.substring(0, first);
        String author = payload.substring(first + 1, last);

        int score;
        try {
            score = Integer.parseInt(payload.substring(last + 1));
        } catch (NumberFormatException e) {
            messenger.sendText("Оценка должна быть числом от 1 до 10!");
            return;
        }
        if (score < 1 || score > 10) {
            messenger.sendText("Оценка должна быть от 1 до 10!");
            return;
        }

        ticketManager.saveRating(subject, author, userId, score);
        messenger.sendText("Оценка сохранена!");
        showSubjects();
    }

    private void openFile(String payload){
        int sep = payload.indexOf(':');
        String subject = payload.substring(0, sep);
        String author = payload.substring(sep + 1);

        File file = ticketManager.getFile(subject, author);
        if (file == null) {
            messenger.sendText("Файл не найден: " + author);
            return;
        }
        messenger.sendFile(subject + " / " + author, file);
        showRatingMenu(subject, author);
    }

    public boolean checkUpload(String subject, String fileName, long size) {
        if (subject == null || subject.isBlank()) {
            messenger.sendText("Напиши название предмета в подписи файла");
            return false;
        }
        subject = subject.trim();

        if (!ticketManager.listSubjects().contains(subject)) {
            messenger.sendText("Предмета «" + subject + "» нет. Доступные: "
                    + String.join(", ", ticketManager.listSubjects()));
            return false;
        }
        if (fileName == null || fileName.length() > MAX_NAME_LEN) {
            messenger.sendText("Имя файла должно быть не длиннее " + MAX_NAME_LEN + " символов.");
            return false;
        }
        int dot = fileName.lastIndexOf('.');
        String ext = dot < 0 ? "" : fileName.substring(dot + 1).toLowerCase();
        if (!ALLOWED_EXT.contains(ext)) {
            messenger.sendText("Формат не подходит. Разрешены: " + String.join(", ", ALLOWED_EXT));
            return false;
        }
        if (size > MAX_UPLOAD_BYTES) {
            messenger.sendText("Файл больше 20 МБ.");
            return false;
        }
        return true;
    }

    public void saveUpload(String subject, String fileName, InputStream data) {
        subject = subject.trim();
        try {
            ticketManager.saveFile(subject, fileName, data);
        } catch (FileAlreadyExistsException e) {
            messenger.sendText("Файл с таким именем уже есть. Переименуй и отправь снова.");
            return;
        } catch (IOException e) {
            e.printStackTrace();
            messenger.sendText("Не получилось сохранить файл.");
            return;
        }
        messenger.sendText("Готово! Файл «" + fileName + "» добавлен в «" + subject + "».");
        showSubjects();
    }
}
