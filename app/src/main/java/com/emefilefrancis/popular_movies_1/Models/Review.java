package com.emefilefrancis.popular_movies_1.Models;

/**
 * Created by SP on 10/8/2018.
 */

public class Review {
    private String author;
    private String review;

    public Review(String author, String review) {
        this.author = author;
        this.review = review;
    }

    public String getAuthor() {
        return author;
    }

    public String getReview() {
        return review;
    }
}
