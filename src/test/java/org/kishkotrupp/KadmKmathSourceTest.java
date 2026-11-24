package org.kishkotrupp;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class KadmKmathSourceTest {

    @Test
    void fetchLectures_ShouldParseLinksFromFakeWebsite() throws Exception {
        //фейковые объекты, которые имитируют Jsoup
        Connection mockConnection = mock(Connection.class);
        Document mockDocument = mock(Document.class);
        Elements mockElements = new Elements();
        Element mockElement = mock(Element.class);

        // 2. Настраиваем фейковый элемент <a>
        when(mockElement.text()).thenReturn("1. Множества и отображения");
        when(mockElement.attr("href")).thenReturn("files/alggeom01.pdf");
        mockElements.add(mockElement); // Кладем элемент в список

        // 3. Настраиваем фейковый документ
        when(mockDocument.select("a[href^=files/]")).thenReturn(mockElements);

        // 4. Настраиваем цепочку вызовов Jsoup.connect().timeout().get()
        when(mockConnection.timeout(anyInt())).thenReturn(mockConnection);
        when(mockConnection.get()).thenReturn(mockDocument);

        // 5. ПЕРЕХВАТЫВАЕМ СТАТИЧЕСКИЙ МЕТОД Jsoup.connect()
        try (MockedStatic<Jsoup> mockedJsoup = mockStatic(Jsoup.class)) {
            // когда кто-то вызовет Jsoup.connect(любая строка), верни mockConnection
            mockedJsoup.when(() -> Jsoup.connect(anyString())).thenReturn(mockConnection);


            KadmKmathSource service = new KadmKmathSource();
            List<LectureMeta> result = service.fetchLectures();


            // вернул ровно 1 лекцию
            assertEquals(1, result.size());

            // Проверяем, что название и ссылка правильно распарсились
            assertEquals("1. Множества и отображения", result.get(0).getTitle());
            assertEquals("https://kadm.kmath.ru/files/alggeom01.pdf", result.get(0).getUrl());
        }
    }
}