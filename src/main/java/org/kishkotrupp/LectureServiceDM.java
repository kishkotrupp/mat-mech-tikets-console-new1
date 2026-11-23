package org.kishkotrupp;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
public class LectureServiceDM {
    private static final String LECTURES_URL = "https://univer2005-73.narod.ru";

    public List<LectureStorage> fetchLectures() {
        List<LectureStorage> lectures = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(LECTURES_URL)
                    .userAgent("Mozilla/5.0")
                    .timeout(10000)
                    .get();

            Elements links = doc.select("a[href]");

            for (Element link : links) {
                String title = link.text();
                String relativeUrl = link.attr("href");

                if (title.isEmpty() || relativeUrl.startsWith("http") || relativeUrl.startsWith("mailto:")) {
                    continue;
                }

                String absoluteUrl = LECTURES_URL + relativeUrl;

                lectures.add(new LectureStorage(title, absoluteUrl));
            }
        }
        catch (IOException e) {
            System.err.println("Не удалось загрузить лекции: " + e.getMessage());
        }
        return lectures;
    }
}
