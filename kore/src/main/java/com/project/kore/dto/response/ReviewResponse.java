package com.project.kore.dto.response;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Una recensione lasciata a un professionista, così come mostrata sul suo profilo.
 */
public class ReviewResponse {

    private String authorName;
    private int rating;
    private String comment;
    private LocalDateTime date;


    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public LocalDateTime getDate() { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviewResponse that = (ReviewResponse) o;
        return rating == that.rating &&
               Objects.equals(authorName, that.authorName) &&
               Objects.equals(comment, that.comment) &&
               Objects.equals(date, that.date);
    }

    @Override
    public int hashCode() {
        return Objects.hash(authorName, rating, comment, date);
    }

    @Override
    public String toString() {
        return "ReviewResponse{authorName='" + authorName + "', rating=" + rating + ", date=" + date + "}";
    }

    public static class Builder {
        private String authorName;
        private int rating;
        private String comment;
        private LocalDateTime date;

        public Builder authorName(String authorName) { this.authorName = authorName; return this; }
        public Builder rating(int rating) { this.rating = rating; return this; }
        public Builder comment(String comment) { this.comment = comment; return this; }
        public Builder date(LocalDateTime date) { this.date = date; return this; }

        public ReviewResponse build() {
            ReviewResponse obj = new ReviewResponse();
            obj.authorName = this.authorName;
            obj.rating = this.rating;
            obj.comment = this.comment;
            obj.date = this.date;
            return obj;
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
