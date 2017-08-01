package ganteng.hendrawd.popularmovies.data;

import android.net.Uri;
import android.provider.BaseColumns;


public class FavoriteMovieContract {

    public static final String AUTHORITY = "ganteng.hendrawd.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AUTHORITY);
    public static final String PATH_FAVORITE_MOVIES = "favorite_movies";

    public static final class FavoriteMovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_FAVORITE_MOVIES).build();
        public static final String TABLE_NAME = "favorite_movies";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_VOTE_AVERAGE = "vote_average";
        public static final String COLUMN_GENRE_IDS = "genre_ids";

        public static final String[] MOVIE_PROJECTION = new String[]{
                COLUMN_MOVIE_ID,
                COLUMN_POSTER_PATH,
                COLUMN_ORIGINAL_TITLE,
                COLUMN_TITLE,
                COLUMN_OVERVIEW,
                COLUMN_RELEASE_DATE,
                COLUMN_BACKDROP_PATH,
                COLUMN_VOTE_AVERAGE,
                COLUMN_GENRE_IDS,
        };

        static Uri buildMovieUri(String movieId) {
            return CONTENT_URI.buildUpon().appendPath(movieId).build();
        }
    }
}
