package ganteng.hendrawd.popularmovies.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;

import com.google.android.youtube.player.YouTubeStandalonePlayer;
import com.google.gson.Gson;

import ganteng.hendrawd.popularmovies.BuildConfig;
import ganteng.hendrawd.popularmovies.R;
import ganteng.hendrawd.popularmovies.data.FavoriteMovieContract;
import ganteng.hendrawd.popularmovies.network.model.Movie;
import ganteng.hendrawd.popularmovies.view.CustomToast;

/**
 * created on 4/21/16
 *
 * @author hendrawd
 */
public class Util {

    private static int screenW = -1;
    private static int screenH = -1;

    /**
     * Converts dp unit to equivalent pixels, depending on device density.
     *
     * @param context Context to get resources and device specific display metrics
     * @param dp      A value in dp (density independent pixels) unit. Which we need to convert into pixels
     * @return A float value to represent px equivalent to dp depending on device density
     */
    public static float dp2px(Context context, float dp) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi / 160f);
    }

    /**
     * Helper method to get display width of screen
     *
     * @param c context
     * @return screen width
     */
    public static int getDisplayWidth(Context c) {
        if (screenW == -1) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            screenW = dm.widthPixels;
        }
        return screenW;
    }

    /**
     * Helper method to get display height of screen
     *
     * @param c context
     * @return screen height
     */
    public static int getDisplayHeight(Context c) {
        if (screenH == -1) {
            WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
            DisplayMetrics dm = new DisplayMetrics();
            wm.getDefaultDisplay().getMetrics(dm);
            screenH = dm.heightPixels;
        }
        return screenH;
    }

    public static String getMimeType(Context context, Uri uri) {
        String mimeType;
        if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
            ContentResolver cr = context.getApplicationContext().getContentResolver();
            mimeType = cr.getType(uri);
        } else {
            String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri
                    .toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                    fileExtension.toLowerCase());
        }
        return mimeType;
    }

    public static void hideKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(
                    Context.INPUT_METHOD_SERVICE
            );
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static void openUrl(String url, Context ctx) {
        try {
            if (!url.startsWith("http://") && !url.startsWith("https://"))
                url = "http://" + url;

            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            ctx.startActivity(browserIntent);
        } catch (Exception e) {
            CustomToast.show(ctx, "Can't open url!");
        }
    }

    /**
     * Share text and/or url to external application
     * I created this helper method long time ago, and don't remember why i create this helper method,
     * but looks like this is superior than original share intent and easy to use.
     * But in this project, actually i use ShareCompat.IntentBuilder instead
     *
     * @param context      Context for starting share intent
     * @param text         String to share
     * @param extraSubject String to share
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @SuppressWarnings("deprecation")
    public static void shareTextUrl(Context context, String text, String extraSubject) {
        Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        } else {
            shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT);
        }

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, extraSubject);
        shareIntent.putExtra(Intent.EXTRA_TEXT, text);

        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_with)));
    }

    public static void playYoutubeVideo(Activity context, String videoId) {
        if (Util.isPackageInstalled("com.google.android.youtube", context.getPackageManager())) {
            // if have youtube application
            Intent intent = YouTubeStandalonePlayer.createVideoIntent(context, BuildConfig.YOUTUBE_API, videoId);
            context.startActivity(intent);
        } else {
            // if don't have youtube application
            String youtubeUrl = "https://www.youtube.com/watch?v=" + videoId;
            Util.openUrl(youtubeUrl, context);
        }
    }

    public static boolean isPackageInstalled(String packageName, PackageManager packageManager) {
        try {
            packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Start activity with view shared element transition if the version greater than lollipop
     * or no shared element transition if the version below lollipop
     * Don't forget to add
     * android:transitionName="yourPreferredTransitionName"
     * to the shared elements
     *
     * @param context activity
     * @param intent  intent to start
     * @param view    shared element view
     */
    public static void startActivityWithSharedElementTransitionIfPossible(Context context, Intent intent, View view) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            context.startActivity(intent);
        } else {
            context.startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(
                    (Activity) context, view, view.getTransitionName()).toBundle());
        }
    }

    /**
     * Return a {@link ContentValues} item with the values from a {@link Movie}.
     */
    public static ContentValues getMovieValues(Movie movie) {
        final ContentValues values = new ContentValues();
        values.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID, movie.getId());
        values.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_POSTER_PATH, movie.getPosterPath());
        values.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_ORIGINAL_TITLE, movie.getOriginalTitle());
        values.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_TITLE, movie.getTitle());
        values.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_OVERVIEW, movie.getOverview());
        values.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_RELEASE_DATE, movie.getReleaseDate());
        values.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_BACKDROP_PATH, movie.getBackdropPath());
        values.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_VOTE_AVERAGE, movie.getVoteAverage());
        values.put(FavoriteMovieContract.FavoriteMovieEntry.COLUMN_GENRE_IDS, new Gson().toJson(movie.getGenreIds()));
        return values;
    }
}
