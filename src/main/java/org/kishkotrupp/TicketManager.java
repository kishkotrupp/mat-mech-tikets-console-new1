package org.kishkotrupp;

import java.io.File;
import java.util.List;

public class TicketManager {

    private final TicketStorage ticketStorage;
    private final RatingStorage ratingStorage;

    public TicketManager(TicketStorage ticketStorage, RatingStorage ratingStorage) {
        this.ticketStorage = ticketStorage;
        this.ratingStorage = ratingStorage;
    }

    //=====================================

    public List<String> listSubjects() {
        return ticketStorage.listSubjects();
    }

    public List<String> listAuthors(String subject) {
        return ticketStorage.listAuthors(subject);
    }

    public File getFile(String subject, String author) {
        return ticketStorage.getFile(subject, author);
    }

    public String readContent(String subject, String author) {
        return ticketStorage.readFile(subject, author);
    }

    public void saveFile(String subject, String fileName, java.io.InputStream data) throws java.io.IOException {
        ticketStorage.saveFile(subject, fileName, data);
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