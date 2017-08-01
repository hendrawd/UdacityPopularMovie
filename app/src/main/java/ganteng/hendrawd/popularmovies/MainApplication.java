package ganteng.hendrawd.popularmovies;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.misc.AsyncTask;
import com.android.volley.request.GsonRequest;

import java.util.List;

import ganteng.hendrawd.popularmovies.network.UrlComposer;
import ganteng.hendrawd.popularmovies.network.VolleySingleton;
import ganteng.hendrawd.popularmovies.network.model.Genre;
import ganteng.hendrawd.popularmovies.network.model.GenreMapper;
import ganteng.hendrawd.popularmovies.network.response.GetGenreList;
import ganteng.hendrawd.popularmovies.util.NetworkChecker;

/**
 * @author hendrawd on 7/4/16
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        if (TextUtils.isEmpty(BuildConfig.THE_MOVIE_DB_API) || TextUtils.isEmpty(BuildConfig.YOUTUBE_API)) {
            throw new RuntimeException("Please Provide API Keys in app's build.gradle");
        }
        super.onCreate();
        new RequestGenreTask().execute();
        // Stetho.initializeWithDefaults(this);
    }

    /**
     * Send request to get genre list
     * Will periodically send request to get genre list every 5 seconds if the request failed
     */
    private class RequestGenreTask extends AsyncTask<Void, Integer, Boolean> {

        private static final long REQUEST_EVERY = 5_000L;//millis

        @Override
        protected Boolean doInBackground(Void... params) {
            do {
                if (NetworkChecker.isNetworkAvailable(MainApplication.this)) {
                    final GsonRequest<GetGenreList> gsonRequest = new GsonRequest<>(
                            UrlComposer.getGenreListUrl(),
                            GetGenreList.class,
                            null,
                            new Response.Listener<GetGenreList>() {
                                @Override
                                public void onResponse(GetGenreList getGenreList) {
                                    List<Genre> genreList = getGenreList.genres;
                                    for (Genre genre : genreList) {
                                        GenreMapper.put(genre.getId(), genre.getName());
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError volleyError) {
                                    if (volleyError != null) {
                                        Log.e("GetGenreList", volleyError.getMessage());
                                    }
                                }
                            });
                    VolleySingleton.getInstance(MainApplication.this).addToRequestQueue(gsonRequest);
                }

                try {
                    Thread.sleep(REQUEST_EVERY);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } while (!GenreMapper.isInitialized());
            return true;
        }
    }
}
