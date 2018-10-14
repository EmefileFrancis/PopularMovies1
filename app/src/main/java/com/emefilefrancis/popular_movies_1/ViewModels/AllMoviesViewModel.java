package com.emefilefrancis.popular_movies_1.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.emefilefrancis.popular_movies_1.Database.AppDatabase;
import com.emefilefrancis.popular_movies_1.Models.Movie;

import java.util.List;

/**
 * Created by SP on 10/9/2018.
 */

public class AllMoviesViewModel extends ViewModel {
    private final String TAG = AllMoviesViewModel.class.getSimpleName();
    private LiveData<List<Movie>> movies;

    public AllMoviesViewModel(AppDatabase mDb) {
        Log.d(TAG, "Actively querying the database for favorite movies.");
        movies = mDb.movieDao().loadAllMovies();
    }

    public LiveData<List<Movie>> getMovies() {
        return movies;
    }
}
