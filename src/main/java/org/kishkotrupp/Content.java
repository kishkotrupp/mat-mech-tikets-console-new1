package org.kishkotrupp;

import java.io.File;
import java.util.List;

public interface Content {
    List<String> listSubjects();
    List<String> listAuthors(String subject);
    File getFile(String subject, String author);
}