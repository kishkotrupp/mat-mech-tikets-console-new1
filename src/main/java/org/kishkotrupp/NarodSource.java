package org.kishkotrupp;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NarodSource implements LectureSource {
    private static final String URL = "https://univer2005-73.narod.ru/";
    private final PdfParser pdfParser;
    private final TitleCacheStorage cache;

    // Очередь из одного потока для фонового скачивания (защита от бана)
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public NarodSource(PdfParser pdfParser, TitleCacheStorage cache) {
        this.pdfParser = pdfParser;
        this.cache = cache;
    }

    @Override
    public String getSourceName() {
        return "Дискретная математика (narod.ru)";
    }

    @Override
    public List<LectureMeta> fetchLectures() {
        List<LectureMeta> lectures = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(URL).timeout(10000).get();

            // Ищем только нужные ссылки (как мы обсуждали ранее)
            Elements links = doc.select("a[href*=DM_KN_], a[href*=Zadachnik_DM_MO2020], a[href*=dzdm_ft2020], a[href*=Zamyatin-graphs]");

            for (Element link : links) {
                String originalText = link.text().replace("\n", " ").trim();
                if (originalText.isEmpty()) {
                    originalText = link.attr("href");
                }
                String absoluteUrl = "https://univer2005-73.narod.ru/" + link.attr("href");

                // 1. Проверяем кэш
                String smartTitle = cache.getTitle(absoluteUrl);

                if (smartTitle == null) {
                    // 2. Если нет в кэше — показываем оригинальный текст
                    smartTitle = originalText;

                    // 3. И запускаем фоновую задачу для скачивания и парсинга
                    final String finalUrl = absoluteUrl;
                    executor.submit(() -> {
                        String parsedTitle = pdfParser.extractTitle(finalUrl);
                        if (parsedTitle != null && !parsedTitle.isEmpty()) {
                            cache.saveTitle(finalUrl, parsedTitle);
                            System.out.println("Закэшировано: " + finalUrl + " -> " + parsedTitle);
                        }
                    });
                }

                lectures.add(new LectureMeta(smartTitle, absoluteUrl));
            }
        } catch (IOException e) {
            System.err.println("Ошибка загрузки narod.ru: " + e.getMessage());
        }
        return lectures;
    }
}
