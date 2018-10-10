package com.emefilefrancis.popular_movies_1.ViewModels;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.emefilefrancis.popular_movies_1.Database.AppDatabase;
import com.emefilefrancis.popular_movies_1.Models.Movie;

/**
 * Created by SP on 10/10/2018.
 */

public class MovieViewModel extends ViewModel {
    private static String TAG = MovieViewModel.class.getSimpleName();
    private AppDatabase mDB;
    private LiveData<Movie> movie;

    public MovieViewModel(AppDatabase mDB, int movieID) {
        this.mDB = mDB;
        movie = mDB.movieDao().loadMovieById(movieID);
    }

    public LiveData<Movie> getMovie() {
        return movie;
    }
}
