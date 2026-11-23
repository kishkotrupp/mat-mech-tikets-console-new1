package org.kishkotrupp;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;


public class ConsoleDialog {
    private final TicketManager ticketManager;
    private final LectureService lectureService;
    private final LectureServiceDM lectureServiceDM;
    private final IO io;

    public ConsoleDialog(TicketManager ticketManager, IO io, LectureService lectureService, LectureServiceDM lectureServiceDM){
        this.ticketManager = ticketManager;
        this.io = io;
        this.lectureService = lectureService;
        this.lectureServiceDM = lectureServiceDM;
    }

    public void run() {
        ConsoleMessenger messenger = new ConsoleMessenger(io);
        DialogService dialog = new DialogService(ticketManager, messenger, lectureService, lectureServiceDM);

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