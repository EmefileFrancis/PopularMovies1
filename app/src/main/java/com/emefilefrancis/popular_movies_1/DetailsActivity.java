package com.emefilefrancis.popular_movies_1;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.emefilefrancis.popular_movies_1.Database.AppDatabase;
import com.emefilefrancis.popular_movies_1.Database.AppExecutors;
import com.emefilefrancis.popular_movies_1.Models.Movie;
import com.emefilefrancis.popular_movies_1.ViewModels.MovieViewModel;
import com.emefilefrancis.popular_movies_1.ViewModels.MovieViewModelFactory;
import com.squareup.picasso.Picasso;

public class DetailsActivity extends AppCompatActivity {

    private static final String MOVIE_ID_AS_EXTRA = "movie_id";

    private ImageView mBackDropImage;
    private ImageView mPosterImage;
    private TextView mMovieTitle;
    private TextView mMovieReleaseDate;
    private TextView mMovieRating;
    private TextView mMovieOverview;
    private CheckBox mFavorite;

    private Movie mMovie;

    private AppDatabase mDB;

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
        mFavorite = findViewById(R.id.favorite_cb);

        mDB = AppDatabase.getInstance(getApplicationContext());

        Intent intent = getIntent();
        if(intent.hasExtra("TheSelectedMovie")){
            mMovie = intent.getParcelableExtra("TheSelectedMovie");

            final int movieId = mMovie.getId();

            /*
            * Check the CheckBox if the movie is found in the database
            *
            * */
            MovieViewModelFactory movieViewModelFactory = new MovieViewModelFactory(mDB, movieId);
            MovieViewModel movieViewModel = ViewModelProviders.of(this, movieViewModelFactory).get(MovieViewModel.class);
            movieViewModel.getMovie().observe(this, new Observer<Movie>() {
                @Override
                public void onChanged(@Nullable Movie movie) {
                    if(movie != null){
                        if(movie.isFavorite())
                            mFavorite.setChecked(true);
                    }
                }
            });

            loadUIWithData(mMovie);
        }

        addListenerOnFavoriteCheckBox();

    }

    public void addListenerOnFavoriteCheckBox() {

        mFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                int movieID = mMovie.getId();
                MovieViewModelFactory movieViewModelFactory = new MovieViewModelFactory(mDB, movieID);
                MovieViewModel movieViewModel = ViewModelProviders.of(DetailsActivity.this, movieViewModelFactory).get(MovieViewModel.class);
                movieViewModel.getMovie().observe(DetailsActivity.this, new Observer<Movie>() {
                    @Override
                    public void onChanged(@Nullable Movie movie) {
                        if(((CheckBox) v).isChecked()){
                            Toast.makeText(DetailsActivity.this, "Added to Favorites", Toast.LENGTH_LONG).show();
                            //Movie is not inside the database, then insert it
                            if(movie == null){
                                mMovie.setFavorite(true);
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDB.movieDao().insertMovie(mMovie);
                                    }
                                });
                            }
                        }else{
                            Toast.makeText(DetailsActivity.this, "Removed from Favorites", Toast.LENGTH_LONG).show();
                            if(movie != null){
                                mMovie.setFavorite(false);
                                final Movie theMovie = movie;
                                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                                    @Override
                                    public void run() {
                                        mDB.movieDao().deleteMovie(theMovie);
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
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
        intent.putExtra(MOVIE_ID_AS_EXTRA, String.valueOf(mMovie.getId()));
        startActivity(intent);
    }
}
