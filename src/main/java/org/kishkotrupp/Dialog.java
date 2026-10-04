package org.kishkotrupp;
import java.util.List;


public class Dialog {
    private final Collection collection;
    private final IO io;

    public Dialog(Collection collection, IO io){
        this.collection = collection;
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
                List<String> subjects = collection.listSubjects();
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
                authors = collection.listAuthors(subject);
                io.println("Авторы: " + authors);
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

            String content = collection.readContent(subject, author);
            io.println("---- " + subject + " / " + author + " ----");
            io.println(content);
            io.println("------------------------------------");

            subject = null;
        }
    }
}
