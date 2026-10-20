package org.kishkotrupp;

public interface RatingStorage {
    void saveRating(String subject, String author, long userId, int score);
    double getMiddleRating(String subject, String author);
    int getRatingCountPeople(String subject, String author);
}