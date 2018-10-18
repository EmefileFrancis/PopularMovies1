package com.emefilefrancis.popular_movies_1;

import android.app.ActionBar;
import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.emefilefrancis.popular_movies_1.Database.AppDatabase;
import com.emefilefrancis.popular_movies_1.Database.AppExecutors;
import com.emefilefrancis.popular_movies_1.Models.Movie;
import com.emefilefrancis.popular_movies_1.Models.Trailer;
import com.emefilefrancis.popular_movies_1.Utilities.JsonUtils;
import com.emefilefrancis.popular_movies_1.Utilities.NetworkUtils;
import com.emefilefrancis.popular_movies_1.ViewModels.MovieViewModel;
import com.emefilefrancis.popular_movies_1.ViewModels.MovieViewModelFactory;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DetailsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Trailer>> {

    private static final int TRAILERS_LOADER_ID = 24;
    private static final String MOVIE_ID_AS_EXTRA = "movie_id";

    // Using Jake Wharton's ButterKnife
    @BindView(R.id.backdrop_iv) ImageView mBackDropImage;
    @BindView(R.id.poster_iv) ImageView mPosterImage;
    @BindView(R.id.movie_title_tv) TextView mMovieTitle;
    @BindView(R.id.release_date_tv) TextView mMovieReleaseDate;
    @BindView(R.id.rating_tv) TextView mMovieRating;
    @BindView(R.id.overview_tv) TextView mMovieOverview;
    @BindView(R.id.favorite_cb) CheckBox mFavorite;
    @BindView(R.id.trailers_layout) LinearLayout mLinearLayout;
    @BindView(R.id.no_trailers_tv) TextView mNoTrailersMessage;

    private Movie mMovie;

    private AppDatabase mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        ButterKnife.bind(this);
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

        loadMovieTrailers();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("ChildCount: ", String.valueOf(mLinearLayout.getChildCount()));
        if(mLinearLayout.getChildCount() > 1)
            mLinearLayout.removeAllViews();
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
                            Toast.makeText(DetailsActivity.this, "Added to Favorites", Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(DetailsActivity.this, "Removed from Favorites", Toast.LENGTH_SHORT).show();
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
        Glide.with(this)
                .load(movie.getBackdropPath())
                .placeholder(R.drawable.ic_launcher_background)
                .into(mBackDropImage);
        Glide.with(this)
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

    @NonNull
    @Override
    public Loader<List<Trailer>> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<List<Trailer>>(this) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                forceLoad();
            }

            @Nullable
            @Override
            public List<Trailer> loadInBackground() {
                String movieId = args.getString(MOVIE_ID_AS_EXTRA);
                if(movieId == null){
                    return null;
                }else{
                    try {
                        URL url = NetworkUtils.buildUrl(movieId, true);
                        String trailersInJson = NetworkUtils.getResponseFromApiCall(url);
                        List<Trailer> trailers = JsonUtils.getTrailersFromJsonResponse(trailersInJson);
                        return trailers;
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                return null;
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Trailer>> loader, List<Trailer> data) {
        if(data != null){
            updateUIWithTrailers(data);
        }else{
            showNoTrailersMessage();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Trailer>> loader) {

    }

    private void showNoTrailersMessage(){
        mNoTrailersMessage.setVisibility(View.VISIBLE);
    }

    private void loadMovieTrailers(){
        Bundle bundle = new Bundle();
        bundle.putString(MOVIE_ID_AS_EXTRA, String.valueOf(mMovie.getId()));
        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<Trailer> loader = loaderManager.getLoader(TRAILERS_LOADER_ID);

        if(loader == null){
            loaderManager.initLoader(TRAILERS_LOADER_ID, bundle, this);
        }else{
            loaderManager.restartLoader(TRAILERS_LOADER_ID, bundle, this);
        }
    }

    private void updateUIWithTrailers(List<Trailer> trailers){

        for(int i = 0; i < trailers.size(); i++){
            Trailer trailer = trailers.get(i);

            String trailerName = trailer.getName();
            final String trailerKey = trailer.getKey();

            Button button = new Button(this);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ActionBar.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 8, 0, 8);
            button.setLayoutParams(layoutParams);
            button.setTypeface(Typeface.create("sans-serif_light", Typeface.NORMAL));
            button.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));

            button.setText(trailerName);
            button.setOnClickListener(new View.OnClickListener() {
                String youtubeBaseUrl = "https://www.youtube.com/watch?v=";
                @Override
                public void onClick(View v) {
                    youtubeBaseUrl = youtubeBaseUrl + trailerKey;
                    Uri trailerUri = Uri.parse(youtubeBaseUrl);
                    Intent intent = new Intent(Intent.ACTION_VIEW, trailerUri);
                    if(intent.resolveActivity(getPackageManager()) != null){
                        startActivity(intent);
                    }
                }
            });
            mLinearLayout.addView(button);
        }
    }
}
