package org.kishkotrupp;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


public class ConsoleDialog {
    private final TicketManager ticketManager;
    private final LectureService lectureService;
    private final IO io;

    public ConsoleDialog(TicketManager ticketManager, IO io, LectureService lectureService){
        this.ticketManager = ticketManager;
        this.io = io;
        this.lectureService = lectureService;
    }

    public void run() {
        ConsoleMessenger messenger = new ConsoleMessenger(io);
        LectureService lectureService = new LectureService();
        DialogService dialog = new DialogService(ticketManager, messenger, lectureService);

        io.println("Привет! Я бот. Введи /start для начала или /help для справки.");

        while (true) {
            String input = io.readLine();
            if (input == null) return; // Конец ввода

            input = input.trim();
            if (input.isEmpty()) continue;

            String commandToSend = messenger.getCallbackData(input);

            dialog.handle(1L, commandToSend);
        }
    }
}