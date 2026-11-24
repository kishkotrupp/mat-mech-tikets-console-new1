package org.kishkotrupp;

import java.util.List;

public class LectureService {
    private final List<LectureSource> sources;

    public LectureService() {
        // Создаем общие компоненты
        PdfParser pdfParser = new PdfParser();
        TitleCacheStorage cache = new TitleCacheStorage("titles_cache.txt");

        // Передаем их в источники
        this(List.of(
                new KadmKmathSource(),
                new NarodSource(pdfParser, cache)
        ));
    }

    public LectureService(List<LectureSource> sources) {
        this.sources = sources;
    }

    public List<LectureSource> getAvailableSources() {
        return sources;
    }

    public List<LectureMeta> fetchLecturesForSource(String sourceName) {
        for (LectureSource source : sources) {
            if (source.getSourceName().equals(sourceName)) {
                return source.fetchLectures();
            }
        }
        return List.of();
    }
}
