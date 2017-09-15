package ganteng.hendrawd.popularmovies.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import ganteng.hendrawd.popularmovies.R;
import ganteng.hendrawd.popularmovies.network.UrlComposer;
import ganteng.hendrawd.popularmovies.network.model.Movie;
import ganteng.hendrawd.popularmovies.view.AutoFitImageView;

import static ganteng.hendrawd.popularmovies.util.Util.scanForActivity;

/**
 * @author hendrawd on 6/23/16
 */

public class MovieAdapter extends RecyclerView.Adapter<MovieAdapter.ViewHolder> {

    public interface ClickListener {
        void onItemClick(int position, Movie movieData, View view);
    }

    private ArrayList<Movie> mData;
    private final Object mLock = new Object();

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final AutoFitImageView mImageView;
        final TextView mTextView;

        ViewHolder(View view) {
            super(view);
            mImageView = (AutoFitImageView) view.findViewById(R.id.image_view);
            mTextView = (TextView) view.findViewById(R.id.text_view);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final Context context = scanForActivity(view.getContext());
            final int clickPosition = getAdapterPosition();
            ((ClickListener) context).onItemClick(clickPosition, getValueAt(clickPosition), mImageView);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mTextView.getText();
        }
    }

    public Movie getValueAt(int position) {
        return mData.get(position);
    }

    public MovieAdapter(ArrayList<Movie> movieList) {
        mData = movieList;
    }

    public ArrayList<Movie> getData() {
        return this.mData;
    }

    public void set(ArrayList<Movie> dataToSet) {
        synchronized (mLock) {
            mData = dataToSet;
            notifyDataSetChanged();
        }
    }

    public void append(List<Movie> dataToAppend) {
        synchronized (mLock) {
            int firstPosition = mData.size();
            mData.addAll(dataToAppend);
            notifyItemRangeChanged(firstPosition, dataToAppend.size());
        }
    }

    public void removeItemWithMovieId(String movieId) {
        synchronized (mLock) {
            ListIterator<Movie> iterator = mData.listIterator();
            int index = 0;
            while (iterator.hasNext()) {
                if (iterator.next().getId().equals(movieId)) {
                    iterator.remove();
                    notifyItemRemoved(index);
                    break;
                }
                index++;
            }
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_movie, parent, false);
        ViewHolder holder = new ViewHolder(view);
        holder.mTextView.setSelected(true);
        return holder;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Movie movie = getValueAt(position);

        holder.mTextView.setText(movie.getTitle());

        String path = movie.getPosterPath();
        if (TextUtils.isEmpty(path)) {
            holder.mImageView.setImageResource(R.drawable.error_portrait);
        } else {
            RequestOptions options = new RequestOptions()
                    .error(R.drawable.error_portrait)
                    .placeholder(R.drawable.placeholder_portrait);
            Glide.with(holder.mImageView.getContext())
                    .load(UrlComposer.getPosterUrl(path))
                    .apply(options)
                    .into(holder.mImageView);
        }
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }
}