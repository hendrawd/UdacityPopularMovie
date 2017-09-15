package ganteng.hendrawd.popularmovies.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.ShareCompat;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.Loader;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.GsonRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ganteng.hendrawd.popularmovies.R;
import ganteng.hendrawd.popularmovies.network.UrlComposer;
import ganteng.hendrawd.popularmovies.network.VolleySingleton;
import ganteng.hendrawd.popularmovies.network.model.GenreMapper;
import ganteng.hendrawd.popularmovies.network.model.Movie;
import ganteng.hendrawd.popularmovies.network.model.Review;
import ganteng.hendrawd.popularmovies.network.model.Video;
import ganteng.hendrawd.popularmovies.network.response.GetReviewList;
import ganteng.hendrawd.popularmovies.network.response.GetVideoList;
import ganteng.hendrawd.popularmovies.util.NetworkChecker;
import ganteng.hendrawd.popularmovies.util.TextToSpeechHelper;
import ganteng.hendrawd.popularmovies.util.Util;
import ganteng.hendrawd.popularmovies.view.AutoFitImageView;
import ganteng.hendrawd.popularmovies.view.ContinuedLineView;
import ganteng.hendrawd.popularmovies.view.CustomToast;
import me.zhanghai.android.materialratingbar.MaterialRatingBar;

import static ganteng.hendrawd.popularmovies.data.FavoriteMovieContract.FavoriteMovieEntry.COLUMN_MOVIE_ID;
import static ganteng.hendrawd.popularmovies.data.FavoriteMovieContract.FavoriteMovieEntry.CONTENT_URI;
import static ganteng.hendrawd.popularmovies.data.FavoriteMovieContract.FavoriteMovieEntry.MOVIE_PROJECTION;
import static ganteng.hendrawd.popularmovies.util.Util.getMovieValues;

public class DetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Movie> {

    private static final String TAG = "DetailActivity";
    public static final String KEY_MOVIE_DATA = "movie data";
    private static final int FAVORITE_MOVIE_LOADER_ID = 212;
    private static final String BUNDLE_VIDEO_LIST = "video_list";
    private static final String BUNDLE_REVIEW_LIST = "review_list";
    private static final String BUNDLE_SCROLL_POSITION = "scroll_position";
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;
    @BindView(R.id.appbar)
    AppBarLayout appbar;
    @BindView(R.id.iv_backdrop_image)
    ImageView ivBackdropImage;
    @BindView(R.id.tv_overview)
    TextView tvOverview;
    @BindView(R.id.tv_release_date)
    TextView tvReleaseDate;
    @BindView(R.id.main_content)
    CoordinatorLayout mainContent;
    @BindView(R.id.rating_bar)
    MaterialRatingBar ratingBar;
    @BindView(R.id.tv_rating)
    TextView tvRating;
    @BindView(R.id.ll_video_container)
    LinearLayout llVideoContainer;
    @BindView(R.id.tv_related_videos)
    TextView tvRelatedVideos;
    @BindView(R.id.tv_genre)
    TextView tvGenre;
    @BindView(R.id.iv_speaker)
    ImageView ivSpeaker;
    @BindView(R.id.pb_text_to_speech)
    ProgressBar pbTextToSpeech;
    @BindView(R.id.tv_original_title)
    TextView tvOriginalTitle;
    @BindView(R.id.tv_latest_reviews)
    TextView tvLatestReviews;
    @BindView(R.id.ll_review_container)
    LinearLayout llReviewContainer;
    @BindView(R.id.iv_poster)
    ImageView ivPosterImageThumbnail;
    @BindView(R.id.nested_scroll_view)
    NestedScrollView nestedScrollView;

    private TextToSpeechHelper textToSpeechHelper;
    private MenuItem menuItemFavorite;
    private boolean isFavoriteMovie = false;
    private ArrayList<Video> mVideoList;
    private ArrayList<Review> mReviewList;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!getIntent().hasExtra(KEY_MOVIE_DATA)) {
            CustomToast.show(this, getString(R.string.no_movie_data));
            finish();
            return;
        }
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        // workaround for shared element transition stuck
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            postponeEnterTransition();
        }

        //change icon color to white
        ivSpeaker.setColorFilter(Color.WHITE);

        initActionBar();

        final Movie movieData = getMovie();
        showMovieData(movieData);
        final String movieId = movieData.getId();

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_VIDEO_LIST)) {
            mVideoList = savedInstanceState.getParcelableArrayList(BUNDLE_VIDEO_LIST);
            createVideoListView();
            tvRelatedVideos.setVisibility(View.VISIBLE);
        } else {
            if (NetworkChecker.isNetworkAvailable(this)) {
                getVideoData(movieId);
            } else {
                CustomToast.show(DetailActivity.this, getString(R.string.no_internet_connection));
            }
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_REVIEW_LIST)) {
            mReviewList = savedInstanceState.getParcelableArrayList(BUNDLE_REVIEW_LIST);
            createReviewListView();
        } else {
            if (NetworkChecker.isNetworkAvailable(this)) {
                getReviewData(movieId);
            } else {
                CustomToast.show(DetailActivity.this, getString(R.string.no_internet_connection));
            }
        }

        if (savedInstanceState != null && savedInstanceState.containsKey(BUNDLE_SCROLL_POSITION)) {
            final int[] scrollPosition = savedInstanceState.getIntArray(BUNDLE_SCROLL_POSITION);
            if (scrollPosition != null && scrollPosition.length == 2) {
                nestedScrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        nestedScrollView.scrollTo(scrollPosition[0], scrollPosition[1]);
                    }
                });
            }
        }
    }

    private void initActionBar() {
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        if (actionbar != null) {
            actionbar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (textToSpeechHelper != null) {
            textToSpeechHelper.stop();
        }
        VolleySingleton.getInstance(this).cancelPendingRequests(DetailActivity.class);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mVideoList != null && !mVideoList.isEmpty()) {
            outState.putParcelableArrayList(BUNDLE_VIDEO_LIST, mVideoList);
        }
        if (mReviewList != null && !mReviewList.isEmpty()) {
            outState.putParcelableArrayList(BUNDLE_REVIEW_LIST, mReviewList);
        }
        outState.putIntArray(BUNDLE_SCROLL_POSITION, new int[]{nestedScrollView.getScrollX(), nestedScrollView.getScrollY()});
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        menuItemFavorite = menu.findItem(R.id.action_favorite);
        getSupportLoaderManager().restartLoader(FAVORITE_MOVIE_LOADER_ID, null, this);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!isFavoriteMovie) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("UNFAVORITE_ID", getMovie().getId());
            setResult(Activity.RESULT_OK, returnIntent);
        } else {
            setResult(Activity.RESULT_CANCELED);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: {
                // NavUtils.navigateUpFromSameTask(this);//this will messed up shared element transition
                // please suggest me if you have solution
                onBackPressed();
                return true;
            }
            case R.id.action_share: {
                if (mVideoList != null && !mVideoList.isEmpty()) {
                    ShareCompat.IntentBuilder shareIntentBuilder = ShareCompat.IntentBuilder.from(DetailActivity.this);
                    shareIntentBuilder
                            .setType("text/plain")
                            .setText(
                                    getString(
                                            R.string.share_first_trailer,
                                            "https://www.youtube.com/watch?v=" + mVideoList.get(0).getKey()
                                    )
                            );
                    startActivity(shareIntentBuilder.getIntent());
                } else {
                    CustomToast.show(this, getString(R.string.no_trailer));
                }
                return true;
            }
            case R.id.action_favorite: {
                isFavoriteMovie = !isFavoriteMovie;
                if (isFavoriteMovie) {
                    CustomToast.show(this, getString(R.string.added_to_favorite));
                    saveMovieToDb();
                } else {
                    CustomToast.show(this, getString(R.string.removed_from_favorite));
                    deleteMovieFromDb();
                }
                toggleFavoriteIcon();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick(R.id.iv_speaker)
    public void onClick() {
        pbTextToSpeech.setVisibility(View.VISIBLE);
        String text = tvOverview.getText().toString();
        if (!TextUtils.isEmpty(text)) {
            if (textToSpeechHelper == null) {
                textToSpeechHelper = new TextToSpeechHelper();
                textToSpeechHelper.setCallback(new TextToSpeechHelper.SpeakCallback() {
                    @Override
                    public void onSpeak() {
                        pbTextToSpeech.setVisibility(View.GONE);
                    }
                });
            }
            textToSpeechHelper.speak(this, text);
        }
    }

    private void toggleFavoriteIcon() {
        menuItemFavorite.setIcon(
                ResourcesCompat.getDrawable(
                        getResources(),
                        isFavoriteMovie ? R.drawable.ic_favorite : R.drawable.ic_favorite_border,
                        null
                )
        );
    }

    private Movie getMovie() {
        return getIntent().getParcelableExtra(KEY_MOVIE_DATA);
    }

    /**
     * workaround for shared element transition stuck
     * http://www.androiddesignpatterns.com/2015/03/activity-postponed-shared-element-transitions-part3b.html
     *
     * @param sharedElement View that has the shared element with the previous activity
     */
    private void scheduleStartPostponedTransition(final View sharedElement) {
        sharedElement.getViewTreeObserver().addOnPreDrawListener(
                new ViewTreeObserver.OnPreDrawListener() {
                    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                    @Override
                    public boolean onPreDraw() {
                        sharedElement.getViewTreeObserver().removeOnPreDrawListener(this);
                        startPostponedEnterTransition();
                        return true;
                    }
                });
    }

    private void showMovieData(Movie movieData) {
        collapsingToolbar.setTitle(movieData.getTitle());
        collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(this, R.color.colorAccent));
        collapsingToolbar.setContentScrimColor(ContextCompat.getColor(this, R.color.scrim));
        appbar.setExpanded(false);

        // set backdrop image
        String backdropPath = movieData.getBackdropPath();
        if (TextUtils.isEmpty(backdropPath)) {
            ivBackdropImage.setImageResource(R.drawable.error_landscape);
        } else {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .error(R.drawable.error_landscape)
                    .placeholder(R.drawable.placeholder_landscape);
            Glide.with(ivBackdropImage.getContext())
                    .load(UrlComposer.getBackdropUrl(backdropPath))
                    .apply(options)
                    .into(ivBackdropImage);
        }

        //set poster image
        String posterPath = movieData.getPosterPath();
        if (TextUtils.isEmpty(posterPath)) {
            ivPosterImageThumbnail.setImageResource(R.drawable.error_landscape);
        } else {
            RequestOptions options = new RequestOptions()
                    .error(R.drawable.error_portrait)
                    .placeholder(R.drawable.placeholder_portrait);
            Glide.with(ivPosterImageThumbnail.getContext())
                    .load(UrlComposer.getPosterUrl(posterPath))
                    .apply(options)
                    .into(new SimpleTarget<Drawable>() {
                        @Override
                        public void onResourceReady(Drawable resource, Transition<? super Drawable> transition) {
                            ivPosterImageThumbnail.setImageDrawable(resource);
                            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                                scheduleStartPostponedTransition(ivPosterImageThumbnail);
                            }
                        }
                    });
        }

        tvOriginalTitle.setText(movieData.getOriginalTitle());
        tvOverview.setText(movieData.getOverview());
        tvReleaseDate.setText(movieData.getReleaseDate());
        float rating = movieData.getVoteAverage() / 2;
        tvRating.setText(getString(R.string.rating_number, String.format(Locale.US, "%.2f", rating)));
        ratingBar.setRating(rating);

        //set genres text
        if (GenreMapper.isInitialized()) {
            List<Integer> genreIds = movieData.getGenreIds();
            if (genreIds != null && genreIds.size() > 0) {
                int genreIdsLength = genreIds.size();
                StringBuilder genreBuilder = new StringBuilder();
                for (int i = 0; i < genreIdsLength; i++) {
                    genreBuilder.append(GenreMapper.get(genreIds.get(i)));
                    if (i != genreIdsLength - 1) {
                        genreBuilder.append(", ");
                    }
                }
                tvGenre.setText(genreBuilder.toString());
            } else {
                tvGenre.setText(R.string.no_information);
            }
        } else {
            tvGenre.setText(R.string.no_information);
        }
    }

    private void getVideoData(String movieId) {
        if (!TextUtils.isEmpty(movieId)) {
            final GsonRequest<GetVideoList> gsonRequest = new GsonRequest<>(
                    UrlComposer.getVideoUrl(movieId),
                    GetVideoList.class,
                    null,
                    new Response.Listener<GetVideoList>() {
                        @Override
                        public void onResponse(GetVideoList videoResponse) {
                            mVideoList = new ArrayList<>(videoResponse.getResults());
                            createVideoListView();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            if (volleyError != null) {
                                CustomToast.show(DetailActivity.this, volleyError.getMessage());
                            }
                        }
                    });
            VolleySingleton.getInstance(this).addToRequestQueue(gsonRequest, DetailActivity.class);
        }
    }

    private void createVideoListView() {
        if (!mVideoList.isEmpty()) {
            tvRelatedVideos.setVisibility(View.VISIBLE);
            llVideoContainer.removeAllViews();
            int videosLength = mVideoList.size();
            for (int i = 0; i < videosLength; i++) {
                Video video = mVideoList.get(i);
                generateVideoView(video, i == (videosLength - 1));
            }
        } else {
            tvRelatedVideos.setVisibility(View.GONE);
        }
    }

    private void getReviewData(String movieId) {
        if (!TextUtils.isEmpty(movieId)) {
            final GsonRequest<GetReviewList> gsonRequest = new GsonRequest<>(
                    UrlComposer.getReviewUrl(movieId, 1),
                    GetReviewList.class,
                    null,
                    new Response.Listener<GetReviewList>() {
                        @Override
                        public void onResponse(GetReviewList reviewResponse) {
                            mReviewList = new ArrayList<>(reviewResponse.getResults());
                            createReviewListView();
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError volleyError) {
                            if (volleyError != null) {
                                CustomToast.show(DetailActivity.this, volleyError.getMessage());
                            }
                        }
                    });
            VolleySingleton.getInstance(this).addToRequestQueue(gsonRequest, DetailActivity.class);
        }
    }

    private void createReviewListView() {
        if (!mReviewList.isEmpty()) {
            tvLatestReviews.setVisibility(View.VISIBLE);
            llReviewContainer.removeAllViews();
            int reviewsLength = mReviewList.size();
            for (int i = 0; i < reviewsLength; i++) {
                Review review = mReviewList.get(i);
                generateReviewView(review, i == (reviewsLength - 1));
            }
        } else {
            tvLatestReviews.setVisibility(View.GONE);
        }
    }

    private void generateVideoView(final Video video, boolean lastItem) {
        final ViewGroup nullParent = null;
        View row = LayoutInflater.from(this).inflate(R.layout.row_video, nullParent);
        AutoFitImageView ivThumbnail = (AutoFitImageView) row.findViewById(R.id.iv_thumbnail);
        TextView tvName = (TextView) row.findViewById(R.id.tv_name);
        TextView tvType = (TextView) row.findViewById(R.id.tv_type);

        //load image glide
        String videoKey = video.getKey();
        if (TextUtils.isEmpty(videoKey)) {
            ivThumbnail.setImageResource(R.drawable.error_landscape);
        } else {
            RequestOptions options = new RequestOptions().error(R.drawable.error_landscape);
            Glide.with(ivThumbnail.getContext())
                    .load(UrlComposer.getYoutubeThumbnail(video.getKey()))
                    .apply(options)
                    .into(ivThumbnail);
        }

        //set info texts
        tvName.setText(video.getName());
        tvType.setText(getString(R.string.video_type, video.getType()));

        //set click action
        row.findViewById(R.id.main_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.playYoutubeVideo(DetailActivity.this, video.getKey());
            }
        });

        llVideoContainer.addView(row);

        if (!lastItem) {
            llVideoContainer.addView(getLineView());
        }
    }

    private void generateReviewView(final Review review, boolean lastItem) {
        final ViewGroup nullParent = null;
        LayoutInflater inflater = LayoutInflater.from(this);
        View row = inflater.inflate(R.layout.row_review, nullParent);
        TextView tvAuthor = (TextView) row.findViewById(R.id.tv_author);
        TextView tvContent = (TextView) row.findViewById(R.id.tv_content);

        //set info texts
        tvAuthor.setText(getString(R.string.review_author, review.getAuthor()));
        tvContent.setText(review.getContent());

        //set click action
        row.findViewById(R.id.main_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Util.openUrl(review.getUrl(), DetailActivity.this);
            }
        });

        llReviewContainer.addView(row);

        if (!lastItem) {
            llReviewContainer.addView(getLineView());
        }
    }

    private View getLineView() {
        ContinuedLineView continuedLineView = new ContinuedLineView(this);
        int lineHeight = getResources().getDimensionPixelSize(R.dimen.line_height);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, lineHeight);
        continuedLineView.setLayoutParams(params);
        continuedLineView.setLineColor(ContextCompat.getColor(this, R.color.colorAccent));
        return continuedLineView;
    }

    public Movie getMovieFromDb() {
        Movie movieData = getMovie();

        final String where = String.format("%s=?", COLUMN_MOVIE_ID);
        final String[] args = new String[]{String.valueOf(movieData.getId())};
        final Cursor cursor = getContentResolver().query(CONTENT_URI, MOVIE_PROJECTION, where, args, null);
        if (cursor != null && cursor.getCount() >= 1) {
            Log.d(TAG, "getMovie: " + movieData.getTitle());
            Log.d(TAG, "getMovie: cursor count " + cursor.getCount());
            cursor.moveToFirst();
            return Movie.fromCursor(cursor);
        } else {
            return null;
        }
    }

    public void saveMovieToDb() {
        Movie movieData = getMovie();

        final ContentValues movieValues = getMovieValues(movieData);
        getContentResolver().insert(CONTENT_URI, movieValues);
    }

    public void deleteMovieFromDb() {
        Movie movieData = getMovie();

        final String where = String.format("%s=?", COLUMN_MOVIE_ID);
        final String[] args = new String[]{String.valueOf(movieData.getId())};
        getContentResolver().delete(CONTENT_URI, where, args);
    }

    @Override
    public Loader<Movie> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<Movie>(this) {

            @Override
            protected void onStartLoading() {
                forceLoad();
            }

            @Override
            public Movie loadInBackground() {
                return getMovieFromDb();
            }

            @Override
            public void deliverResult(Movie data) {
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Movie> loader, Movie data) {
        isFavoriteMovie = data != null;
        toggleFavoriteIcon();
    }

    @Override
    public void onLoaderReset(Loader<Movie> loader) {
    }
}
