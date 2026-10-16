package org.kishkotrupp;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class MatMechBot {

    private static final String BOT_USERNAME = "MathMechTicketsBot";
    private static final String SUBJECTS_DIR = "subjects";

    public static void main(String[] args) throws TelegramApiException {
        String botToken = System.getenv("TG_BOT_TOKEN");
        if (botToken == null || botToken.isBlank()) {
            System.err.println("Не задан TG_BOT_TOKEN в переменных окружения.");
            System.exit(1);
        }

        // Собираем зависимости: StringContent -> Collection -> TgDialog
        Content content = new StringContent(SUBJECTS_DIR);
        Collection collection = new Collection(content);
        TgDialog bot = new TgDialog(botToken, BOT_USERNAME, collection);

        // Регистрируем бота в Telegram API — с этого момента он принимает updates
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(bot);

        System.out.println("Бот запущен: @" + BOT_USERNAME);
    }
}