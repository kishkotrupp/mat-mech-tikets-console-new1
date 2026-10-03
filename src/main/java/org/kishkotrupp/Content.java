package org.kishkotrupp;

import java.util.List;

public interface Content {
    List<String> listSubjects();
    List<String> listAuthors(String subject);
    String readContent(String subject, String author);
}