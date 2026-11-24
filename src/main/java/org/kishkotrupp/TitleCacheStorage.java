package org.kishkotrupp;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TitleCacheStorage {
    private final Path cacheFile;
    // Потокобезопасная мапа для кэша
    private final Map<String, String> cache = new ConcurrentHashMap<>();

    public TitleCacheStorage(String filePath) {
        this.cacheFile = Paths.get(filePath);
        loadCache();
    }

    private void loadCache() {
        if (!Files.exists(cacheFile)) return;
        try {
            List<String> lines = Files.readAllLines(cacheFile, StandardCharsets.UTF_8);
            for (String line : lines) {
                String[] parts = line.split(";", 2);
                if (parts.length == 2) {
                    cache.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getTitle(String url) {
        return cache.get(url);
    }

    // Синхронизированный метод, чтобы потоки не перезаписывали файл одновременно
    public synchronized void saveTitle(String url, String title) {
        cache.put(url, title);
        try {
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, String> entry : cache.entrySet()) {
                sb.append(entry.getKey()).append(";").append(entry.getValue()).append("\n");
            }
            Files.writeString(cacheFile, sb.toString(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}