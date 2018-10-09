package com.emefilefrancis.popular_movies_1;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.emefilefrancis.popular_movies_1.Models.Review;

import java.util.List;

/**
 * Created by SP on 10/8/2018.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewsViewHolder> {

    private Context mContext;
    private List<Review> mReviews;

    public ReviewsAdapter(Context context){ mContext = context; }

    @NonNull
    @Override
    public ReviewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layout = R.layout.review_item;
        View view = LayoutInflater.from(mContext).inflate(layout, parent, false);
        ReviewsViewHolder reviewsViewHolder = new ReviewsViewHolder(view);
        return reviewsViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewsViewHolder holder, int position) {
        holder.bindData(position);
    }

    @Override
    public int getItemCount() {
        if(mReviews == null) return 0;
        return mReviews.size();
    }

    public void setmReviews(List<Review> reviews){
        mReviews = reviews;
        notifyDataSetChanged();
    }

    public class ReviewsViewHolder extends RecyclerView.ViewHolder{

        TextView mAuthor;
        TextView mReview;

        public ReviewsViewHolder(View itemView) {
            super(itemView);
            mAuthor = itemView.findViewById(R.id.author_tv);
            mReview = itemView.findViewById(R.id.review_tv);
        }

        public void bindData(int position){
            Review review = mReviews.get(position);
            mAuthor.setText(review.getAuthor());
            mReview.setText(review.getReview());
        }
    }
}
