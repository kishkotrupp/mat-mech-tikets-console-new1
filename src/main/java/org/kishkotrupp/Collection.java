package org.kishkotrupp;

import java.io.File;
import java.util.List;

public class Collection {

    private final Content content;
    private final RatingStorage ratingStorage;

    public Collection(Content content, RatingStorage ratingStorage) {
        this.content = content;
        this.ratingStorage = ratingStorage;
    }

    //=====================================

    public List<String> listSubjects() {
        return content.listSubjects();
    }

    public List<String> listAuthors(String subject) {

        return content.listAuthors(subject);
    }

    public File getFile(String subject, String author) {
        return content.getFile(subject, author);
    }

    //=========================================

    public void saveRating(String subject, String author, long userId, int score) {
        ratingStorage.saveRating(subject, author, userId, score);
    }

    public double getMiddleRating(String subject, String author) {
        return ratingStorage.getMiddleRating(subject, author);
    }

    public int getRatingCountPeople(String subject, String author) {
        return ratingStorage.getRatingCountPeople(subject, author);
    }
}