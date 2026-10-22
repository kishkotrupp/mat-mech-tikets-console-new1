package org.kishkotrupp;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

    private static final String BOT_USERNAME = "MathMechTicketsBot";
    private static final String SUBJECTS_DIR = "subjects";

    public static void main(String[] args) throws TelegramApiException {
        //russ out
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        //-------


        TicketStorage ticketStorage = new FileTicketStorage(SUBJECTS_DIR);
        RatingStorage ratingStorage = new DataBaseRating();
        TicketManager ticketManager = new TicketManager(ticketStorage, ratingStorage);

        if (args.length == 0){
            startConsole(ticketManager);
        }else if (args[0].equalsIgnoreCase("TG")){
            startTgBot(ticketManager);
            keepAlive(); // Блокируем поток, чтобы бот работал
        }else if (args[0].equalsIgnoreCase("BOTH")){
            startConsole(ticketManager);
            startTgBot(ticketManager);
        }


    }

    private static void startTgBot(TicketManager ticketManager) throws TelegramApiException {
        String botToken = System.getenv("TG_BOT_TOKEN");
        if (botToken == null || botToken.isBlank()) {
            System.err.println("Не задан TG_BOT_TOKEN в переменных окружения.");
            System.exit(1);
        }

        TgDialog bot = new TgDialog(botToken, BOT_USERNAME, ticketManager);

        // Регистрируем бота в Telegram API — с этого момента он принимает updates
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);

        System.out.println("Бот запущен: @" + BOT_USERNAME);
    }

    private static void startConsole(TicketManager ticketManager) {
        IO io = new ConsoleIO();
        new ConsoleDialog(ticketManager, io).run();
    }

    private static void keepAlive() {
        // Не даем программе завершиться, пока работает бот
        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
