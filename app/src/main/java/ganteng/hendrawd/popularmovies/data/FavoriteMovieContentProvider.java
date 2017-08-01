package ganteng.hendrawd.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static ganteng.hendrawd.popularmovies.data.FavoriteMovieContract.AUTHORITY;
import static ganteng.hendrawd.popularmovies.data.FavoriteMovieContract.BASE_CONTENT_URI;
import static ganteng.hendrawd.popularmovies.data.FavoriteMovieContract.FavoriteMovieEntry.TABLE_NAME;

public class FavoriteMovieContentProvider extends ContentProvider {

    private static final int FAVORITE_MOVIES = 100;
    private static final int FAVORITE_MOVIES_ID = 101;
    private static final String FAVORITE_MOVIES_PATH = "favorite_movies";
    private static final String FAVORITE_MOVIES_ID_PATH = "favorite_movies/*";
    private static final UriMatcher URI_MATCHER = buildUriMatcher();

    private FavoriteMovieDbHelper dbHelper;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        matcher.addURI(AUTHORITY, FAVORITE_MOVIES_PATH, FAVORITE_MOVIES);
        matcher.addURI(AUTHORITY, FAVORITE_MOVIES_ID_PATH, FAVORITE_MOVIES_ID);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        dbHelper = new FavoriteMovieDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = dbHelper.getReadableDatabase();
        final int match = URI_MATCHER.match(uri);
        Cursor cursor;
        switch (match) {
            case FAVORITE_MOVIES: {
                cursor = db.query(TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown query uri: " + uri);
            }
        }
        final Context context = getContext();
        if (context != null) cursor.setNotificationUri(context.getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        Uri insertUri;
        switch (match) {
            case FAVORITE_MOVIES: {
                long id = db.insertOrThrow(TABLE_NAME, null, values);
                if (id > 0) {
                    insertUri = FavoriteMovieContract.FavoriteMovieEntry.buildMovieUri(String.valueOf(id));
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown insert uri: " + uri);
            }
        }
        notifyResolver(uri);
        return insertUri;
    }

    @Override
    public int delete(@NonNull Uri uri, String selection, String[] selectionArgs) {
        if (uri.equals(BASE_CONTENT_URI)) {
            dbHelper.close();
            dbHelper.deleteDatabase();
            dbHelper = new FavoriteMovieDbHelper(getContext());
            return 1;
        }
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        int deletedValue;
        switch (match) {
            case FAVORITE_MOVIES: {
                deletedValue = db.delete(TABLE_NAME,
                        selection,
                        selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown delete uri: " + uri);
            }
        }
        if (deletedValue != 0) notifyResolver(uri);
        return deletedValue;
    }

    @Override
    public int update(@NonNull Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = dbHelper.getWritableDatabase();
        final int match = URI_MATCHER.match(uri);
        int retrieveValue;
        switch (match) {
            case FAVORITE_MOVIES: {
                retrieveValue = db.update(TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            }
            default: {
                throw new UnsupportedOperationException("Unknown update uri: " + uri);
            }
        }
        notifyResolver(uri);
        return retrieveValue;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * Helper method to notify resolver
     *
     * @param uri to notify
     */
    private void notifyResolver(Uri uri) {
        Context context = getContext();
        if (context != null) context.getContentResolver().notifyChange(uri, null);
    }
}
