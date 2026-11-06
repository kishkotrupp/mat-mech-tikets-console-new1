package org.kishkotrupp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TgDialogRatingTest {

    TgDialog bot;
    TicketManager mockTicketManager;

    @BeforeEach
    void setUp() throws Exception {
        mockTicketManager = mock(TicketManager.class);
        bot = spy(new TgDialog("token", "test_bot", mockTicketManager));
        doReturn(null).when(bot).execute(any(AnswerCallbackQuery.class));
    }

    @Test
    void handleRateCallback() throws Exception {
        // --- СБОРКА ФЕЙКОВОГО НАЖАТИЯ НА КНОПКУ "8" ---
        CallbackQuery callback = new CallbackQuery();
        callback.setId("12345");
        callback.setData("rate:алгем:a.pdf:8"); // Имитируем клик по 8-ке

        // Создаем фейковый чат
        org.telegram.telegrambots.meta.api.objects.Chat chat = new org.telegram.telegrambots.meta.api.objects.Chat();
        chat.setId(1L);

        // Создаем сообщение и кладем в него чат
        Message message = new Message();
        message.setChat(chat);


        callback.setMessage(message);

        // Устанавливаем ID пользователя, который нажал
        org.telegram.telegrambots.meta.api.objects.User user = new org.telegram.telegrambots.meta.api.objects.User();
        user.setId(999L);
        callback.setFrom(user);

        Update update = new Update();
        update.setCallbackQuery(callback);

        // --- ВЫЗОВ МЕТОДА ---
        // Передаём фейковый Update в бота
        bot.onUpdateReceived(update);

        // --- ПРОВЕРКИ ---
        // 1. Убеждаемся, что бот передал правильные данные в Collection
        verify(mockTicketManager, times(1)).saveRating("алгем", "a.pdf", 999L, 8);

        // 2. Убеждаемся, что бот попытался отправить всплывающее окно
        verify(bot, times(1)).execute(any(AnswerCallbackQuery.class));
    }
}