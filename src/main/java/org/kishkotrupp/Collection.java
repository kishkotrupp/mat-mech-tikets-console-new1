package org.kishkotrupp;

import java.io.File;
import java.util.List;

public class Collection {

    private final Content content;

    public Collection(Content content) {
        this.content = content;
    }

    public List<String> listSubjects() {
        return content.listSubjects();
    }

    public List<String> listAuthors(String subject) {
        return content.listAuthors(subject);
    }

    public File getFile(String subject, String author) {
        return content.getFile(subject, author);
    }
}