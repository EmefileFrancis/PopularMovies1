package com.emefilefrancis.popular_movies_1;

import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.Intent;
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

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesItemClickHandler,
        LoaderManager.LoaderCallbacks<List<Movie>>{

    private static final int FETCH_MOVIES_LOADER_ID = 22;
    private static final String SORT_QUERY_URL_EXTRA = "sort_extra";
    private static final String TOP_RATED_QUERY_PARAM = "top_rated";
    private static final String POPULARITY_QUERY_PARAM = "popular";
    private static final String TAG = MainActivity.class.getSimpleName();

    private RecyclerView mRecyclerView;
    private TextView mErrorMessage;
    private ProgressBar mLoadingIndicator;

    private String mSortByQueryParam = POPULARITY_QUERY_PARAM;

    private MoviesAdapter mMoviesAdapter;
    private GridLayoutManager mLayoutManager;

    private AppDatabase mDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = findViewById(R.id.main_rv);
        mLoadingIndicator = findViewById(R.id.loading_indicator_pb);
        mErrorMessage = findViewById(R.id.error_message_tv);

        mMoviesAdapter = new MoviesAdapter(this, this);
        mLayoutManager = new GridLayoutManager(this, 2, GridLayoutManager.VERTICAL, false);

        mRecyclerView.setAdapter(mMoviesAdapter);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mDB = AppDatabase.getInstance(getApplicationContext());
        loadMoviesData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings, menu);
        return super.onCreateOptionsMenu(menu);
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
        startActivity(intent);
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
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Movie>> loader) {

    }

    private void retrieveMovies(){
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
        mErrorMessage.setVisibility(View.VISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
    }

    private void showRecyclerView(){
        mErrorMessage.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void loadMoviesData() {
        showRecyclerView();
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
