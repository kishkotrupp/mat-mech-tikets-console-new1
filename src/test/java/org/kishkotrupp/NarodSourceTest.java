package org.kishkotrupp;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class NarodSourceTest {

    private NarodSource narodSource;
    private PdfParser mockPdfParser;
    private TitleCacheStorage mockCache;

    @BeforeEach
    void setUp() {
        mockPdfParser = mock(PdfParser.class);
        mockCache = mock(TitleCacheStorage.class);
        narodSource = new NarodSource(mockPdfParser, mockCache);
    }

    // Вспомогательный метод для создания фейкового HTML
    private void mockJsoupResponse() throws Exception {
        // Создаем фейковую ссылку
        Element mockElement = mock(Element.class);
        when(mockElement.text()).thenReturn("Слайды лекции");
        when(mockElement.attr("href")).thenReturn("IMKN/DM_KN_1.pdf");

        Elements mockElements = new Elements(mockElement); // Кладем в список
        Document mockDocument = mock(Document.class);
        when(mockDocument.select(anyString())).thenReturn(mockElements);

        Connection mockConnection = mock(Connection.class);
        when(mockConnection.timeout(anyInt())).thenReturn(mockConnection);
        when(mockConnection.get()).thenReturn(mockDocument);

        // Перехватываем статический метод Jsoup.connect
        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);

            List<LectureMeta> lectures = narodSource.fetchLectures();

            assertEquals(1, lectures.size());
        }
    }

    @Test
    void fetchLectures_UsesCache_WhenTitleIsCached() throws Exception {
        when(mockCache.getTitle("https://univer2005-73.narod.ru/IMKN/DM_KN_1.pdf"))
                .thenReturn("Кэшированное название");

        mockJsoupResponse();

        // Проверяем, что парсер PDF даже не вызывался
        verify(mockPdfParser, never()).extractTitle(anyString());
        verify(mockCache, never()).saveTitle(anyString(), anyString());
    }

    @Test
    void fetchLectures_ParsesPdfInBackground_WhenCacheMiss() throws Exception {
        // Кэш пуст
        when(mockCache.getTitle(anyString())).thenReturn(null);

        // Говорим парсеру вернуть красивое название
        when(mockPdfParser.extractTitle(anyString())).thenReturn("Прямое произведение");

        mockJsoupResponse();

        // Так как скачивание идет в фоновом потоке, нам нужно подождать,
        // пока фоновая задача выполнится. Даем ей 2 секунды.
        verify(mockCache, timeout(2000)).saveTitle(
                eq("https://univer2005-73.narod.ru/IMKN/DM_KN_1.pdf"),
                eq("Прямое произведение")
        );
    }
}