package org.kishkotrupp;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.List;

public class DialogService {
    public static final String START = "/start";
    public static final String LIST = "/list";
    public static final String HELP = "/help";
    public static final String BACK = "back";
    public static final String SUBJ = "subj:";
    public static final String FILE = "file:";
    public static final String RATE = "rate:";

    public static final int MAX_NAME_LEN = 20;
    public static final long MAX_UPLOAD_BYTES = 20L * 1024 * 1024;
    public static final List<String> ALLOWED_EXT = List.of("pdf", "doc", "docx", "txt");

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

    public DialogService(TicketManager ticketManager, Messenger messenger) {
        this.ticketManager = ticketManager;
        this.messenger = messenger;
    }

    public void handle(long userId, String command) {
        command = command.trim();

        if (command.equals(START) || command.equals(LIST)) {
            messenger.sendText(WELCOME);
            showSubjects();
        } else if (command.equals(HELP)) {
            messenger.sendText(HELP_TEXT);
        } else if (command.equals(BACK)) {
            showSubjects();
        } else if (command.startsWith(SUBJ)) {
            showAuthors(command.substring(SUBJ.length()));
        } else if (command.startsWith(FILE)) {
            openFile(command.substring(FILE.length()));
        } else if (command.startsWith(RATE)) {
            rate(userId, command.substring(RATE.length()));
        } else {
            messenger.sendText("Некорректная команда. Напиши /start, чтобы начать, или /help для справки.");
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
            commands.add(SUBJ + subject);
        }
        messenger.sendMenu("Выбери предмет:", labels, commands, 1);
    }

    private void showAuthors(String subject) {
        List<String> authors = ticketManager.listAuthors(subject);

        List<String> labels = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        for (String author : authors) {
            int count = ticketManager.getRatingCountPeople(subject, author);
            String label = author;
            if (count > 0) {
                label += String.format(" ★%.1f (%d)", ticketManager.getMiddleRating(subject, author), count);
            }
            labels.add(label);
            commands.add(FILE + subject + ":" + author);
        }
        labels.add("⬅️ Назад");
        commands.add(BACK);

        String text = authors.isEmpty()
                ? "В предмете " + subject + " нет файлов."
                : "Авторы по предмету " + subject + ":";
        messenger.sendMenu(text, labels, commands, 1);
    }

    private void openFile(String payload) {
        int sep = payload.indexOf(':');
        if (sep < 0) {
            messenger.sendText("Некорректная команда.");
            return;
        }
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

    private void showRatingMenu(String subject, String author) {
        List<String> labels = new ArrayList<>();
        List<String> commands = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            labels.add(String.valueOf(i));
            commands.add(RATE + subject + ":" + author + ":" + i);
        }
        labels.add("Пропустить");
        commands.add(BACK);

        messenger.sendMenu("Оцени автора " + author + " от 1 до 10:", labels, commands, 5);
    }

    private void rate(long userId, String payload) {
        // payload = "предмет:автор:оценка"
        int first = payload.indexOf(':');
        int last = payload.lastIndexOf(':');
        if (first < 0 || last <= first) {
            messenger.sendText("Некорректная команда");
            return;
        }
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

