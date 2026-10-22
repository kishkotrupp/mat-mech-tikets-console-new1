package org.kishkotrupp;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


public class ConsoleDialog {
    private final TicketManager ticketManager;
    private final IO io;

    public ConsoleDialog(TicketManager ticketManager, IO io){
        this.ticketManager = ticketManager;
        this.io = io;
    }

    private void printHelp() {
        io.println("=== СПРАВКА ===\n1) Введи название нужного предмета.\n" +
                "2) Выбери нужного для тебя автора.\n" +
                "3) Выучи билет и сдай экзамен на отлично!");
    }

    private boolean isHelp(String input) {
        if (input.equalsIgnoreCase("\\help")) {
            printHelp();
            return true;
        }
        return false;
    }
    public void run() {
        io.println("Привет! Я - бот, созданный для упрощения подготовки к экзаменам на великом матмехе!\n" +
                "Я помогу найти расписанные билеты!\n" +
                "Введи '\\help' для справки!");

        String subject = null;
        List<String> authors = null;

        while (true) {
            if (subject == null) {
                List<String> subjects = ticketManager.listSubjects();
                io.print("Какой предмет тебя интересует? ");
                String input = io.readLine();
                if (input == null) return; //ввод закончился
                input = input.trim();

                if (isHelp(input)) continue;

                if (!subjects.contains(input)) {
                    io.println("Такого предмета нет.");
                    continue;
                }
                subject = input;
                authors = ticketManager.listAuthors(subject);
                io.println("Авторы: ");
                for (String author : authors) {
                    double middleRating = ticketManager.getMiddleRating(subject, author);
                    int countPeople = ticketManager.getRatingCountPeople(subject, author);
                    String ratingStr = (countPeople > 0) ? String.format(" ★%.1f (%d)", middleRating, countPeople) : "  ";

                    io.println(author + ratingStr);
                }
                continue;
            }
            io.print("Выбери автора: ");
            String author = io.readLine();
            if (author == null) return;
            author = author.trim();

            if (isHelp(author)) continue;

            if (!authors.contains(author)) {
                io.println("Такого автора нет.");
                continue;
            }

            String content = ticketManager.readContent(subject, author);
            io.println("---- " + subject + " / " + author + " ----");
            io.println(content);
            io.println("------------------------------------");

            long userId = 1;
            io.print("оцени автора от 1 до 10: ");
            String ratingInput = io.readLine();
            int score = Integer.parseInt(ratingInput);
            if (score >= 1 && score <= 10) {
                ticketManager.saveRating(subject, author, userId, score);
            } else {
                io.println("Ошибка: оценка должна быть от 1 до 10!");
            }

            subject = null;
        }
    }
}