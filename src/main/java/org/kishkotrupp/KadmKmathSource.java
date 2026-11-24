package org.kishkotrupp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KadmKmathSource implements LectureSource {
    private static final String URL = "https://kadm.kmath.ru/pages.php?id=vernikov_kurses_alggeom";

    @Override
    public String getSourceName() {
        return "Алгем (kadm.kmath.ru)";
    }

    @Override
    public List<LectureMeta> fetchLectures() {
        List<LectureMeta> lectures = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL).timeout(10000).get();
            Elements links = doc.select("a[href^=files/]");

            for (Element link : links) {
                String title = link.text();
                String absoluteUrl = "https://kadm.kmath.ru/" + link.attr("href");
                lectures.add(new LectureMeta(title, absoluteUrl));
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки kadm.kmath.ru: " + e.getMessage());
        }
        return lectures;
    }
}