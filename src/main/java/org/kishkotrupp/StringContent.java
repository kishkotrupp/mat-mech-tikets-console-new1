package org.kishkotrupp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringContent implements Content {
    private final Map<String, Map<String, String>> data = new HashMap<>();

    public void add(String subject, String author, String content) {
        data.computeIfAbsent(subject, k -> new HashMap<>()).put(author, content);
    }

    @Override
    public List<String> listSubjects() {
        return List.of();
    }

    @Override
    public List<String> listAuthors(String subject) {
        return List.of();
    }

    @Override
    public String readContent(String subject, String author) {
        return "";
    }


}
