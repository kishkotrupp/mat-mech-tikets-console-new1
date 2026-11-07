package org.kishkotrupp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;

public class DialogServiceTest {
    TicketManager mockTicketManager;
    Messenger mockMessenger;
    LectureService mockLectureService;
    DialogService dialogService;

    @BeforeEach
    void setUp() {
        mockTicketManager = mock(TicketManager.class);
        mockMessenger = mock(Messenger.class);
        mockLectureService = mock(LectureService.class);

        dialogService = new DialogService(mockTicketManager, mockMessenger, mockLectureService);
    }

    @Test
    void startCommandShowsMainMenu() {
        dialogService.handle(1L, "/start");

        // verify(...) — это команда-детектив из Mockito. Она говорит: «Проверь, было ли такое действие».
        //times(1) — это проверка количества. «Убедись, что метод был вызван ровно 1 раз»
        verify(mockMessenger, times(1)).sendText(contains("Привет!"));

        verify(mockMessenger, times(1)).sendMenu(
                anyString(), //приветсвие
                argThat(labels -> labels.size() == 2), // Проверяем, что кнопок 2
                anyList(), //команды
                eq(1) //колонки
        );
    }

    @Test
    void menuTicketsShowsSubjects() {
        when(mockTicketManager.listSubjects()).thenReturn(Arrays.asList("math", "physics"));

        dialogService.handle(1L, "menu:tickets");

        verify(mockTicketManager, times(1)).listSubjects();
        verify(mockMessenger, times(1)).sendMenu(
                contains("предмет"),
                argThat(labels -> labels.contains("math") && labels.contains("physics")),
                anyList(),
                eq(1)
        );
    }

    @Test
    void selectingSubjectShowsAuthorsWithRating() {
        when(mockTicketManager.listAuthors("math")).thenReturn(Collections.singletonList("Abby.txt"));
        when(mockTicketManager.getMiddleRating("math", "Abby.txt")).thenReturn(8.5);
        when(mockTicketManager.getRatingCountPeople("math", "Abby.txt")).thenReturn(2);
        when(mockMessenger.supportsFile("Abby.txt")).thenReturn(true);

        dialogService.handle(1L, "subj:math");

        verify(mockMessenger, times(1)).sendMenu(
                contains("Авторы"),
                argThat(labels -> labels.contains("Abby.txt ★8.5 (2)")),
                anyList(),
                eq(1)
        );
    }

    @Test
    void rateCommandSavesRating() {
        dialogService.handle(999L, "rate:math:Abby.txt:8");

        verify(mockTicketManager, times(1)).saveRating("math", "Abby.txt", 999L, 8);
        verify(mockMessenger, times(1)).sendText(
                contains("Оценка сохранена!")
        );
    }

    @Test
    void rateCommandRejectsInvalidScore() {
        dialogService.handle(1L, "rate:math:Abby.txt:15");

        // Бот НЕ должен сохранять эту оценку
        verify(mockTicketManager, never()).saveRating(anyString(), anyString(), anyLong(), anyInt());
        verify(mockMessenger, times(1)).sendText(contains("Оценка должна быть от 1 до 10!"));
    }

    @Test
    void unknownCommandReturnsError() {
        dialogService.handle(1L, "smirno");

        verify(mockMessenger, times(1)).sendText(contains("Неизвестная команда"));
    }
}
