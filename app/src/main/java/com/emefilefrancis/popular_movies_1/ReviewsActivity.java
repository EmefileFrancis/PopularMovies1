package com.emefilefrancis.popular_movies_1;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.emefilefrancis.popular_movies_1.Models.Review;
import com.emefilefrancis.popular_movies_1.Utilities.JsonUtils;
import com.emefilefrancis.popular_movies_1.Utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class ReviewsActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Review>> {

    private static final String MOVIE_ID_AS_EXTRA = "movie_id";
    private static final String MOVIE_ID_AS_KEY = "movie_id";
    private static final String REVIEWS_URL_PART = "/reviews";
    private static final int REVIEWS_LOADER_ID = 23;
    RecyclerView mRecyclerView;
    ReviewsAdapter mAdapter;
    LinearLayoutManager mLayoutManager;
    TextView mNoReviewsMessage;
    TextView mErrorMessage;
    ProgressBar mLoadingReviewsPB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviews);

        mRecyclerView = findViewById(R.id.reviews_rv);
        mAdapter = new ReviewsAdapter(this);
        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mNoReviewsMessage = findViewById(R.id.no_reviews_message_tv);
        mErrorMessage = findViewById(R.id.reviews_error_message_tv);
        mLoadingReviewsPB = findViewById(R.id.reviews_loading_indicator_pb);

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mAdapter);

        loadReviewsFromNetwork();
    }

    @NonNull
    @Override
    public Loader<List<Review>> onCreateLoader(int id, @Nullable final Bundle args) {
        return new AsyncTaskLoader<List<Review>>(this) {
            @Override
            protected void onStartLoading() {
                super.onStartLoading();
                mLoadingReviewsPB.setVisibility(View.VISIBLE);
                forceLoad();
            }

            @Nullable
            @Override
            public List<Review> loadInBackground() {
                List<Review> reviews = null;
                String movieId = args.getString(MOVIE_ID_AS_KEY);

                if(movieId == null || TextUtils.isEmpty(movieId))
                    return null;

                String extraUrlPath = movieId + REVIEWS_URL_PART;
                try {
                    URL builtUrl = NetworkUtils.buildUrl(extraUrlPath, false);
                    String reviewJsonResponse = NetworkUtils.getResponseFromApiCall(builtUrl);
                    reviews = JsonUtils.getReviewsFromJsonResponse(reviewJsonResponse);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return reviews;
            }
        };
    }

    @Override
    public void onLoadFinished(@NonNull Loader<List<Review>> loader, List<Review> data) {
        if(data != null && data.size() > 0){
            showReviews();
            mAdapter.setmReviews(data);
        }else if(data != null && data.size() == 0){
            showNoReviewMessage();
        }else if(data == null){
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(@NonNull Loader<List<Review>> loader) {

    }

    public void showReviews(){
        mLoadingReviewsPB.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    public void showErrorMessage(){
        mLoadingReviewsPB.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    public void showNoReviewMessage() {
        mLoadingReviewsPB.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.INVISIBLE);
        mNoReviewsMessage.setVisibility(View.VISIBLE);
    }

    public void loadReviewsFromNetwork(){
        String movieId = null;
        Intent originIntent = getIntent();
        if(originIntent != null){
           movieId = originIntent.getStringExtra(MOVIE_ID_AS_EXTRA);
        }
        Bundle movieIdBundle = new Bundle();
        if(movieId != null){
            movieIdBundle.putString(MOVIE_ID_AS_KEY, movieId);
        }else{
            movieIdBundle.putString(MOVIE_ID_AS_KEY, "0");
        }

        LoaderManager loaderManager = getSupportLoaderManager();
        Loader<List<Review>> loader = loaderManager.getLoader(REVIEWS_LOADER_ID);

        if(loader == null){
            loaderManager.initLoader(REVIEWS_LOADER_ID, movieIdBundle, this);
        }else{
            loaderManager.restartLoader(REVIEWS_LOADER_ID, movieIdBundle, this);
        }
    }
}
