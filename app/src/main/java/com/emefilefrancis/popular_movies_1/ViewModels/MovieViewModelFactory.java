package com.emefilefrancis.popular_movies_1.ViewModels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.emefilefrancis.popular_movies_1.Database.AppDatabase;

/**
 * Created by SP on 10/10/2018.
 */

public class MovieViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private AppDatabase mDB;
    private int movieID;

    public MovieViewModelFactory(AppDatabase mDB, int movieID) {
        this.mDB = mDB;
        this.movieID = movieID;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new MovieViewModel(mDB, movieID);
    }
}
