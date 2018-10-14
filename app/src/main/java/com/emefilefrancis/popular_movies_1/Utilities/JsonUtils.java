package com.emefilefrancis.popular_movies_1.Utilities;

import com.emefilefrancis.popular_movies_1.Models.Movie;
import com.emefilefrancis.popular_movies_1.Models.Review;
import com.emefilefrancis.popular_movies_1.Models.Trailer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by SP on 8/26/2018.
 */

public class JsonUtils {
    private static final String RESULT_KEY = "results";
    private static final String ID_KEY = "id";
    private static final String TITLE_KEY = "original_title";
    private static final String POSTER_PATH_KEY = "poster_path";
    private static final String BACKDROP_PATH_KEY = "backdrop_path";
    private static final String OVERVIEW_KEY = "overview";
    private static final String VOTE_AVERAGE_KEY = "vote_average";
    private static final String RELEASE_DATE_KEY = "release_date";

    private static final String AUTHOR_KEY = "author";
    private static final String CONTENT_KEY = "content";

    private static final String NAME_KEY = "name";
    private static final String KEY_KEY = "key";

    private static final String POSTER_PATH_BASE_URL = "http://image.tmdb.org/t/p/";
    private static final String PHONES_SIZE_FOR_PATH = "w185";

    public static List<Movie> getMoviesFromJsonResponse(String jsonResponse) throws JSONException {
        List<Movie> movies = new ArrayList<>();
        JSONObject responseObject = new JSONObject(jsonResponse);
        JSONArray allMoviesArray = responseObject.getJSONArray(RESULT_KEY);
        for(int i = 0; i < allMoviesArray.length(); i++){
            JSONObject movie = allMoviesArray.getJSONObject(i);

            int movieID = movie.getInt(ID_KEY);
            String movieTitle = movie.getString(TITLE_KEY);
            String moviePosterPath = POSTER_PATH_BASE_URL + PHONES_SIZE_FOR_PATH + movie.getString(POSTER_PATH_KEY);
            String movieBackDropPath = POSTER_PATH_BASE_URL + PHONES_SIZE_FOR_PATH + movie.getString(BACKDROP_PATH_KEY);
            String movieOverview = movie.getString(OVERVIEW_KEY);
            double movieVoteAverage = movie.getDouble(VOTE_AVERAGE_KEY);
            String movieReleaseDate = movie.getString(RELEASE_DATE_KEY);
            Movie thisMovie = new Movie(movieID, movieTitle, moviePosterPath, movieBackDropPath, movieOverview, movieVoteAverage, movieReleaseDate);
            movies.add(thisMovie);
        }
        return movies;
    }

    public static List<Review> getReviewsFromJsonResponse(String jsonResponse) throws JSONException {
        List<Review> reviews = new ArrayList<>();
        JSONObject responseObject = new JSONObject(jsonResponse);
        JSONArray allReviews = responseObject.getJSONArray(RESULT_KEY);
        for(int i = 0; i < allReviews.length(); i++){
            JSONObject review = allReviews.getJSONObject(i);

            String author = review.getString(AUTHOR_KEY);
            String content = review.getString(CONTENT_KEY);

            Review thisReview = new Review(author, content);
            reviews.add(thisReview);
        }
        return reviews;
    }

    public static List<Trailer> getTrailersFromJsonResponse(String jsonResponse) throws JSONException {
        List<Trailer> trailers = new ArrayList<>();
        JSONObject responseObject = new JSONObject(jsonResponse);
        JSONArray allTrailers = responseObject.getJSONArray(RESULT_KEY);
        for(int i = 0; i < allTrailers.length(); i++){
            JSONObject trailer = allTrailers.getJSONObject(i);

            String id = trailer.getString(ID_KEY);
            String key = trailer.getString(KEY_KEY);
            String name = trailer.getString(NAME_KEY);

            Trailer thisTrailer = new Trailer(id, key, name);
            trailers.add(thisTrailer);
        }
        return trailers;
    }
}
