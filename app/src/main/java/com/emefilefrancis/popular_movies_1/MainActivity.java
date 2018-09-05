package com.emefilefrancis.popular_movies_1;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.emefilefrancis.popular_movies_1.Models.Movie;
import com.emefilefrancis.popular_movies_1.Utilities.JsonUtils;
import com.emefilefrancis.popular_movies_1.Utilities.NetworkUtils;

import org.json.JSONException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MoviesAdapter.MoviesItemClickHandler{

    private static final String TOP_RATED_QUERY_PARAM = "top_rated";
    private static final String POPULARITY_QUERY_PARAM = "popular";

    private RecyclerView mRecyclerView;
    private TextView mErrorMessage;
    private ProgressBar mLoadingIndicator;

    private String mSortByQueryParam = POPULARITY_QUERY_PARAM;

    private MoviesAdapter mMoviesAdapter;
    private GridLayoutManager mLayoutManager;

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

        if(id == R.id.popularity){ mSortByQueryParam = POPULARITY_QUERY_PARAM; }

        if(id == R.id.top_rated){ mSortByQueryParam = TOP_RATED_QUERY_PARAM; }
        loadMoviesData();
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

    class FetchMoviesTask extends AsyncTask <String, Void, List<Movie>> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingIndicator.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Movie> doInBackground(String... strings) {
            if(strings.length == 0) {
                return null;
            }

            String queryParam = strings[0];
            try {
                URL url = NetworkUtils.buildUrl(queryParam);
                String apiCallResponse = NetworkUtils.getResponseFromApiCall(url);
                List<Movie> movies = JsonUtils.getArrayFromJsonResponse(apiCallResponse);
                return movies;
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Movie> movies) {
            super.onPostExecute(movies);
            mLoadingIndicator.setVisibility(View.INVISIBLE);
            if(movies != null){
                mMoviesAdapter.setmMovies(movies);
            }else{
                showErrorMessage();
            }
        }
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
        new FetchMoviesTask().execute(mSortByQueryParam);
    }
}
