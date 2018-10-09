package com.emefilefrancis.popular_movies_1;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.emefilefrancis.popular_movies_1.Models.Movie;
import com.squareup.picasso.Picasso;

public class DetailsActivity extends AppCompatActivity {

    private static final String MOVIE_ID_AS_EXTRA = "movie_id";

    private ImageView mBackDropImage;
    private ImageView mPosterImage;
    private TextView mMovieTitle;
    private TextView mMovieReleaseDate;
    private TextView mMovieRating;
    private TextView mMovieOverview;

    private Movie movie;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        mBackDropImage = findViewById(R.id.backdrop_iv);
        mPosterImage = findViewById(R.id.poster_iv);
        mMovieTitle = findViewById(R.id.movie_title_tv);
        mMovieReleaseDate = findViewById(R.id.release_date_tv);
        mMovieRating = findViewById(R.id.rating_tv);
        mMovieOverview = findViewById(R.id.overview_tv);

        Intent intent = getIntent();
        if(intent.hasExtra("TheSelectedMovie")){
            movie = intent.getParcelableExtra("TheSelectedMovie");
            loadUIWithData(movie);
        }
    }

    private void loadUIWithData(Movie movie){
        Picasso.get()
                .load(movie.getBackdropPath())
                .placeholder(R.drawable.ic_launcher_background)
                .into(mBackDropImage);
        Picasso.get()
                .load(movie.getPosterPath())
                .placeholder(R.drawable.ic_launcher_background)
                .into(mPosterImage);
        mMovieTitle.setText(movie.getTitle());
        mMovieReleaseDate.setText(movie.getReleaseDate());
        mMovieRating.setText(String.valueOf(movie.getRating()));
        mMovieOverview.setText(String.valueOf(movie.getOverview()));
    }

    public void loadReviews(View view){
        Activity originatingActivity = DetailsActivity.this;
        Class destinationActivity = ReviewsActivity.class;
        Intent intent = new Intent(originatingActivity, destinationActivity);
        intent.putExtra(MOVIE_ID_AS_EXTRA, String.valueOf(movie.getId()));
        startActivity(intent);
    }
}
