package com.emefilefrancis.popular_movies_1.ViewModels;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import com.emefilefrancis.popular_movies_1.Database.AppDatabase;

/**
 * Created by SP on 10/9/2018.
 */

public class AllMoviesViewModelFactory extends ViewModelProvider.NewInstanceFactory {
    private AppDatabase mDb;

    public AllMoviesViewModelFactory(AppDatabase mDb) {
        this.mDb = mDb;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new AllMoviesViewModel(mDb);
    }
}
