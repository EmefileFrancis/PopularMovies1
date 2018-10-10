package com.emefilefrancis.popular_movies_1.Database;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import com.emefilefrancis.popular_movies_1.Models.Movie;

import java.util.List;

/**
 * Created by SP on 10/9/2018.
 */

@Dao
public interface MovieDao {

    @Query("SELECT * FROM my_movies ORDER BY id")
    LiveData<List<Movie>> loadAllMovies();

    @Query("SELECT * FROM my_movies WHERE id=:id")
    LiveData<Movie> loadMovieById(int id);

    @Insert
    void insertMovie(Movie movie);

    @Delete
    void deleteMovie(Movie movie);
}
