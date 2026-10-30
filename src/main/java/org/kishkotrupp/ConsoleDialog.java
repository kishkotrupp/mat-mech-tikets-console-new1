package org.kishkotrupp;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;


public class ConsoleDialog implements Messenger{
    private static final long CONSOLE_USER_ID = 1;


    private final IO io;
    private final DialogService dialog;

    private String menuText = null;
    private List<String> menuLabels = new ArrayList<>();
    private List<String> menuCommands = new ArrayList<>();
    private boolean menuIsRating = false;

    public ConsoleDialog(TicketManager ticketManager, IO io){
        this.io = io;
        this.dialog = new DialogService(ticketManager, this);
    }


    public void run() {
        dialog.handle(CONSOLE_USER_ID, DialogService.START);

        while (true) {
            io.print(" ");
            String input = io.readLine();
            if (input == null) return;
            input = input.trim();

            if (input.isEmpty()) continue;
            if (input.equals("\\help") || input.equalsIgnoreCase("/help")) {
                dialog.handle(CONSOLE_USER_ID, DialogService.HELP);
                repeatMenu();
                continue;
            }
            String command = findCommand(input);
            if (command != null) {
                dialog.handle(CONSOLE_USER_ID, command);
            } else if (input.startsWith("/")) {
                dialog.handle(CONSOLE_USER_ID, input);
            } else {
                io.println("Некорректная команда. Введи название из списка или \\help для справки.");
                repeatMenu();
            }

        }
    }
    private String findCommand(String input) {
        for (int i = 0; i < menuLabels.size(); i++) {
            // у авторов в названии есть рейтинг " ★4.5 (2)" — для сравнения его отбрасываем
            String name = menuLabels.get(i).replaceAll(" ★.*$", "");
            if (name.equalsIgnoreCase(input)) {
                return menuCommands.get(i);
            }
        }
        if (input.equalsIgnoreCase("назад") && menuCommands.contains(DialogService.BACK)) {
            return DialogService.BACK;
        }
        return null;
    }

    private void printMenu() {
        io.println(menuText);
        if (menuIsRating) {
            io.println("(введи число или «Пропустить»)");
        } else {
            for (String label : menuLabels) {
                io.println("  - " + label);
            }
        }
    }

    private void repeatMenu() {
        if (menuText != null) {
            printMenu();
        }
    }
    @Override
    public void sendText(String text) {
        io.println(text);
    }

    @Override
    public void sendMenu(String text, List<String> labels, List<String> commands, int columns) {
        this.menuText = text;
        this.menuLabels = new ArrayList<>(labels);
        this.menuCommands = new ArrayList<>(commands);
        this.menuIsRating = columns > 1;
        printMenu();
    }

    @Override
    public void sendFile(String caption, File file) {
        io.println("---- " + caption + " ----");
        if (file.getName().toLowerCase().endsWith(".txt")) {
            try {
                io.println(Files.readString(file.toPath(), StandardCharsets.UTF_8));
            } catch (IOException e) {
                io.println("Ошибка чтения файла: " + e.getMessage());
            }
        } else {
            io.println("Файл лежит здесь: " + file.getPath() + " (в консоли показываются только .txt)");
        }
        io.println("------------------------------------");
    }
}