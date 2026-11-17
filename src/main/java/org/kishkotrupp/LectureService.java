package org.kishkotrupp;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class LectureService {
    private static final String LECTURES_URL = "https://kadm.kmath.ru/pages.php?id=vernikov_kurses_alggeom";

    public List<LectureStorage> fetchLectures() {
        List<LectureStorage> lectures = new ArrayList<>();
        try {
            // 1. Скачиваем страницу (с таймаутом, чтобы не висеть вечно)
            Document doc = Jsoup.connect(LECTURES_URL).timeout(10000).get();

            Elements links = doc.select("a[href^=files/]");

            for (Element link : links) {
                String title = link.text(); // Текст ссылки, например "1. Множества и отображения"
                String relativeUrl = link.attr("href"); // Относительная ссылка "files/alggeom01.pdf"

                // 3. Превращаем относительную ссылку в абсолютную
                String absoluteUrl = "https://kadm.kmath.ru/" + relativeUrl;

                lectures.add(new LectureStorage(title, absoluteUrl));
            }
        } catch (IOException e) {
            System.err.println("Не удалось загрузить лекции: " + e.getMessage());
        }
        return lectures;
    }
}
