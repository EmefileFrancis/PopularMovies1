package com.emefilefrancis.popular_movies_1;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.emefilefrancis.popular_movies_1.Database.AppDatabase;
import com.emefilefrancis.popular_movies_1.Models.Movie;
import com.emefilefrancis.popular_movies_1.Utilities.JsonUtils;
import com.emefilefrancis.popular_movies_1.Utilities.NetworkUtils;
import com.emefilefrancis.popular_movies_1.ViewModels.AllMoviesViewModelFactory;
import com.emefilefrancis.popular_movies_1.ViewModels.AllMoviesViewModel;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesItemClickHandler,
        LoaderManager.LoaderCallbacks<List<Movie>>{

    private static final int FETCH_MOVIES_LOADER_ID = 22;

    private static final String SORT_QUERY_URL_EXTRA = "sort_extra";
    private static final String TOP_RATED_QUERY_PARAM = "top_rated";
    private static final String POPULARITY_QUERY_PARAM = "popular";
    private static final String FAVORITE_PARAM = "favorite";
    private static final String LAST_VISIBLE_POSITION_EXTRA = "visible_position_extra";
    private static final String MOVIES_SHARED_PREF = "popular_movies_app_shared_pref";

    private static final String TAG = MainActivity.class.getSimpleName();

    //Using Jake Wharton's ButterKnife
    @BindView(R.id.main_rv) RecyclerView mRecyclerView;
    @BindView(R.id.error_message_tv) TextView mErrorMessage;
    @BindView(R.id.loading_indicator_pb) ProgressBar mLoadingIndicator;
    @BindView(R.id.movie_category_label) TextView mMovieCategoryLabel;

    private String mSortByQueryParam = POPULARITY_QUERY_PARAM;
    private SharedPreferences sharedPreferences;

    private MoviesAdapter mMoviesAdapter;
    private GridLayoutManager mLayoutManager;
    private Parcelable mSavedRecyclerLayoutState;

    private AppDatabase mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ButterKnife.bind(this);

        mMoviesAdapter = new MoviesAdapter(this, this);
        mLayoutManager = new GridLayoutManager(this, calculateNoOfColumns(this), GridLayoutManager.VERTICAL, false);

        mRecyclerView.setAdapter(mMoviesAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDB = AppDatabase.getInstance(getApplicationContext());

        displayMovies(savedInstanceState);

    }

    private void displayMovies(Bundle savedInstanceState) {
        if(savedInstanceState != null && savedInstanceState.getString(SORT_QUERY_URL_EXTRA) != null)
            mSortByQueryParam = savedInstanceState.getString(SORT_QUERY_URL_EXTRA);

        if(savedInstanceState == null) {
            sharedPreferences = getSharedPreferences(MOVIES_SHARED_PREF, MODE_PRIVATE);
            String restoredSortParam = sharedPreferences.getString(SORT_QUERY_URL_EXTRA, null);
            if(restoredSortParam != null)
                mSortByQueryParam = restoredSortParam;
        }

        if(mSortByQueryParam == FAVORITE_PARAM){
            retrieveMovies();
        }else{
            loadMoviesData();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public static int calculateNoOfColumns(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int scalingFactor = 200;
        int noOfColumns = (int) (dpWidth/scalingFactor);
        if(noOfColumns < 2)
            noOfColumns = 2;
        return noOfColumns;
    }

    // Retrieve the RecyclerView's initial scroll position
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if(savedInstanceState != null && savedInstanceState.getParcelable(LAST_VISIBLE_POSITION_EXTRA) != null){
            mSavedRecyclerLayoutState = savedInstanceState.getParcelable(LAST_VISIBLE_POSITION_EXTRA);
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mSavedRecyclerLayoutState);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);

        // Save the RecyclerView's Scroll Position
        mSavedRecyclerLayoutState = mRecyclerView.getLayoutManager().onSaveInstanceState();
        state.putString(SORT_QUERY_URL_EXTRA, mSortByQueryParam);
        state.putParcelable(LAST_VISIBLE_POSITION_EXTRA, mSavedRecyclerLayoutState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.popularity){
            mSortByQueryParam = POPULARITY_QUERY_PARAM;
            loadMoviesData();
        }

        if(id == R.id.top_rated){
            mSortByQueryParam = TOP_RATED_QUERY_PARAM;
            loadMoviesData();
        }

        if(id == R.id.favorite) {
            mSortByQueryParam = FAVORITE_PARAM;
            retrieveMovies();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void movieOnClickHandler(Movie movie) {
        Class detailsActivity = DetailsActivity.class;
        Context context = MainActivity.this;
        Intent intent = new Intent(context, detailsActivity);
        intent.putExtra("TheSelectedMovie", movie);
        // Store the sort param in SharedPreference so as  to retrieve it later when coming back to this activity
        SharedPreferences.Editor editor = getSharedPreferences(MOVIES_SHARED_PREF, MODE_PRIVATE).edit();
        editor.putString(SORT_QUERY_URL_EXTRA, mSortByQueryParam);
        editor.apply();
        startActivity(intent);
    }

    private void setMoviesCategoryLabel() {
        if(mSortByQueryParam == POPULARITY_QUERY_PARAM){
            mMovieCategoryLabel.setText(R.string.by_popularity_label);
        }else if(mSortByQueryParam == TOP_RATED_QUERY_PARAM){
            mMovieCategoryLabel.setText(R.string.top_rated_label);
        }else if(mSortByQueryParam == FAVORITE_PARAM){
            mMovieCategoryLabel.setText(R.string.favorites_label);
        }
    }

    @NonNull
    @Override
    public Loader<List<Movie>> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<List<Movie>>(this) {

            List<Movie> cachedMovies;

            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                mLoadingIndicator.setVisibility(View.VISIBLE);
                if(cachedMovies != null){
                    deliverResult(cachedMovies);
                }else{
                    forceLoad();
                }
            }

            @Nullable
            @Override
            public List<Movie> loadInBackground() {
                String path = args.getString(SORT_QUERY_URL_EXTRA);
                if(path == null || TextUtils.isEmpty(path)){
                    return null;
                }
                try {
                    URL url = NetworkUtils.buildUrl(path, false);
                    String apiCallResponse = NetworkUtils.getResponseFromApiCall(url);
                    List<Movie> movies = JsonUtils.getMoviesFromJsonResponse(apiCallResponse);
                    return movies;
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            public void deliverResult(@Nullable List<Movie> data) {
                cachedMovies = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Movie>> loader, List<Movie> data) {
        mLoadingIndicator.setVisibility(View.INVISIBLE);
        if(data == null) {
            showErrorMessage();
        }
        mMoviesAdapter.setmMovies(data);

        //Restore the RecycleView State
        if(mSavedRecyclerLayoutState != null)
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mSavedRecyclerLayoutState);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Movie>> loader) {

    }

    private void retrieveMovies(){
        setMoviesCategoryLabel();

        if(mSavedRecyclerLayoutState != null)
            mRecyclerView.getLayoutManager().onRestoreInstanceState(mSavedRecyclerLayoutState);

        AllMoviesViewModelFactory mainViewModelFactory = new AllMoviesViewModelFactory(mDB);
        AllMoviesViewModel mainViewModel = ViewModelProviders.of(this, mainViewModelFactory).get(AllMoviesViewModel.class);
        mainViewModel.getMovies().observe(this, new Observer<List<Movie>>() {
            @Override
            public void onChanged(@Nullable List<Movie> movies) {
                Log.d(TAG, "Receiving database update from LiveData");
                mMoviesAdapter.setmMovies(movies);
            }
        });
    }

    private void showErrorMessage(){
        mMovieCategoryLabel.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showRecyclerView(){
        mErrorMessage.setVisibility(View.INVISIBLE);
        mMovieCategoryLabel.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void loadMoviesData() {
        showRecyclerView();
        setMoviesCategoryLabel();
        Bundle sortQueryBundle = new Bundle();
        sortQueryBundle.putString(SORT_QUERY_URL_EXTRA, mSortByQueryParam);

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<List<Movie>> moviesLoader = loaderManager.getLoader(FETCH_MOVIES_LOADER_ID);

        if(moviesLoader == null){
            loaderManager.initLoader(FETCH_MOVIES_LOADER_ID, sortQueryBundle, this);
        }else{
            loaderManager.restartLoader(FETCH_MOVIES_LOADER_ID, sortQueryBundle, this);
        }
    }
}
