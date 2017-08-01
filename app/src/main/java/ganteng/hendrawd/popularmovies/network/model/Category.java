package ganteng.hendrawd.popularmovies.network.model;

import android.content.Context;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import ganteng.hendrawd.popularmovies.R;

/**
 * @author hendrawd on 7/4/17
 */

public abstract class Category {

    @Retention(RetentionPolicy.SOURCE)
    @StringDef({
            LATEST,
            NOW_PLAYING,
            POPULAR,
            TOP_RATED,
            UPCOMING,
            FAVORITE
    })
    public @interface MovieCategory {
    }

    public static String getCategoryName(Context context, String category) {
        switch (category) {
            case LATEST:
                return context.getString(R.string.category_latest);
            case NOW_PLAYING:
                return context.getString(R.string.category_now_playing);
            case POPULAR:
                return context.getString(R.string.category_popular);
            case TOP_RATED:
                return context.getString(R.string.category_top_rated);
            case UPCOMING:
                return context.getString(R.string.category_upcoming);
            case FAVORITE:
                return context.getString(R.string.category_favorite);
            default:
                return "";
        }
    }

    //the response is not list, but just 1 last movie object
    public static final String LATEST = "latest";
    //not support regional id, so it will not used
    public static final String NOW_PLAYING = "now_playing";
    public static final String POPULAR = "popular";
    public static final String TOP_RATED = "top_rated";
    public static final String UPCOMING = "upcoming";
    public static final String FAVORITE = "favorite";
}
