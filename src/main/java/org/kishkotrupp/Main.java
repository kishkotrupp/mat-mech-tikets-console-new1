package org.kishkotrupp;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {

    public static void main(String[] args) throws TelegramApiException {
        //russ out
        System.setOut(new PrintStream(System.out, true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(System.err, true, StandardCharsets.UTF_8));
        //-------


        TicketStorage ticketStorage = new FileTicketStorage();
        RatingStorage ratingStorage = new DataBaseRating();
        TicketManager ticketManager = new TicketManager(ticketStorage, ratingStorage);
        LectureService lectureService = new LectureService();

        if (args.length == 0){
            startConsole(ticketManager, lectureService);
        }else if (args[0].equalsIgnoreCase("TG")){
            startTgBot(ticketManager, lectureService);
            keepAlive(); // Блокируем поток, чтобы бот работал
        }else if (args[0].equalsIgnoreCase("BOTH")){
            startTgBot(ticketManager, lectureService);
            startConsole(ticketManager, lectureService);
        }


    }

    private static void startTgBot(TicketManager ticketManager, LectureService lectureService) throws TelegramApiException {
        String botToken = System.getenv("TG_BOT_TOKEN");
        if (botToken == null || botToken.isBlank()) {
            System.err.println("Не задан TG_BOT_TOKEN в переменных окружения.");
            System.exit(1);
        }

        TgDialog bot = new TgDialog(botToken, ticketManager, lectureService);

        // Регистрируем бота в Telegram API — с этого момента он принимает updates
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);

        System.out.println("Бот запущен: @" + bot.getBotUsername());
    }

    private static void startConsole(TicketManager ticketManager, LectureService lectureService) {
        IO io = new ConsoleIO();
        new ConsoleDialog(ticketManager, io, lectureService).run();
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
