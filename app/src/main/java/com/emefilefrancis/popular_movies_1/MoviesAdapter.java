package com.emefilefrancis.popular_movies_1;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.emefilefrancis.popular_movies_1.Models.Movie;

import java.util.List;

/**
 * Created by SP on 8/26/2018.
 */

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MoviesViewHolder> {

    private final Context mContext;
    private List<Movie> mMovies;
    private final MoviesItemClickHandler mClickHandler;

    public MoviesAdapter(Context mContext, MoviesItemClickHandler clickHandler) {
        this.mContext = mContext;
        this.mClickHandler = clickHandler;
    }

    public interface MoviesItemClickHandler {
        void movieOnClickHandler(Movie movie);
    }

    @Override
    public MoviesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layout = R.layout.movie_item;
        View view = LayoutInflater.from(mContext).inflate(layout, parent, false);
        MoviesViewHolder viewHolder = new MoviesViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(MoviesViewHolder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        if(mMovies == null) return 0;
        return mMovies.size();
    }

    public void setmMovies(List<Movie> movies){
        this.mMovies = movies;
        notifyDataSetChanged();
    }

    class MoviesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final RelativeLayout mRelativeLayout;
        final ImageView mImageView;
        final TextView mMovieTitle;
        final TextView mMovieRating;
        final TextView mReleaseDate;

        public MoviesViewHolder(View itemView) {
            super(itemView);
            mRelativeLayout = itemView.findViewById(R.id.rl_layout);
            mImageView = itemView.findViewById(R.id.poster_iv);
            mMovieTitle = itemView.findViewById(R.id.title_tv);
            mMovieRating = itemView.findViewById(R.id.rating_tv);
            mReleaseDate = itemView.findViewById(R.id.release_date_tv);
            itemView.setOnClickListener(this);
        }

        public void bindData(int position) {
            Movie movie = mMovies.get(position);

            // Using Glide to load the PosterPath Image
            Glide.with(mContext)
                    .load(movie.getPosterPath())
                    .placeholder(R.drawable.ic_launcher_background)
                    .into(mImageView);

            mMovieTitle.setText(movie.getTitle());
            mMovieRating.setText(String.valueOf(movie.getRating()));
            mReleaseDate.setText(movie.getReleaseDate());
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Movie clickedMovie = mMovies.get(position);
            mClickHandler.movieOnClickHandler(clickedMovie);
        }
    }
}
