package ganteng.hendrawd.popularmovies.activity;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v4.app.SharedElementCallback;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Response;
import com.android.volley.error.VolleyError;
import com.android.volley.request.GsonRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import ganteng.hendrawd.popularmovies.R;
import ganteng.hendrawd.popularmovies.adapter.MovieAdapter;
import ganteng.hendrawd.popularmovies.adapter.base.EmptyAdapter;
import ganteng.hendrawd.popularmovies.network.UrlComposer;
import ganteng.hendrawd.popularmovies.network.VolleySingleton;
import ganteng.hendrawd.popularmovies.network.model.Category;
import ganteng.hendrawd.popularmovies.network.model.Movie;
import ganteng.hendrawd.popularmovies.network.response.GetMovieList;
import ganteng.hendrawd.popularmovies.util.NetworkChecker;
import ganteng.hendrawd.popularmovies.view.CustomToast;
import ganteng.hendrawd.popularmovies.view.EndlessRecyclerViewScrollListener;
import ganteng.hendrawd.popularmovies.view.GridSpacingItemDecoration;

import static ganteng.hendrawd.popularmovies.data.FavoriteMovieContract.FavoriteMovieEntry.CONTENT_URI;
import static ganteng.hendrawd.popularmovies.data.FavoriteMovieContract.FavoriteMovieEntry.MOVIE_PROJECTION;

public class MainActivity extends BaseActivity
        implements Response.Listener<GetMovieList>, Response.ErrorListener, MovieAdapter.ClickListener {

    private static final String BUNDLE_RECYCLER_STATE = "recycler_state";
    private static final String BUNDLE_NETWORK_DATA = "network_data";
    private static final String BUNDLE_ADAPTER_DATA = "adapter_data";
    private static final String BUNDLE_LAST_CATEGORY = "last_category";
    private static final String BUNDLE_ITEM_VIEW_POSITION = "item_view_position";
    private static final int RC_FAV_CHANGE = 123;
    @BindView(R.id.recycler_view)
    RecyclerView mRecyclerView;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;
    @BindView(R.id.bottom_loading)
    ProgressBar mBottomLoading;

    private EndlessRecyclerViewScrollListener mEndlessRecyclerViewScrollListener;

    private GetMovieList mGetMovieListResponse;
    private boolean mIsRequestingData = false;
    private String mCategory;
    private int mViewPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        init();

        if (savedInstanceState != null) {
            mCategory = savedInstanceState.getString(BUNDLE_LAST_CATEGORY);
            mGetMovieListResponse = savedInstanceState.getParcelable(BUNDLE_NETWORK_DATA);

            if (savedInstanceState.containsKey(BUNDLE_ADAPTER_DATA)) {
                ArrayList<Movie> lastData = savedInstanceState.getParcelableArrayList(BUNDLE_ADAPTER_DATA);
                if (lastData != null) {
                    mRecyclerView.setAdapter(new MovieAdapter(lastData));

                    Parcelable savedRecyclerLayoutState = savedInstanceState.getParcelable(BUNDLE_RECYCLER_STATE);
                    mRecyclerView.getLayoutManager().onRestoreInstanceState(savedRecyclerLayoutState);

                    mViewPosition = savedInstanceState.getInt(BUNDLE_ITEM_VIEW_POSITION);
                    persistExitSharedElement();

                    addEndlessScrollListener();
                } else {
                    getContent(1);
                }
            } else {
                mRecyclerView.setAdapter(new EmptyAdapter());
            }
        } else {
            mCategory = Category.POPULAR;
            getContent(1);
        }
    }

    /**
     * Workaround for orientation change issue that will eliminate shared element exit transition,
     * but still has bug, shared element will not triggered if the item is not visible on the screen
     * https://stackoverflow.com/a/42658635/3940133
     */
    private void persistExitSharedElement() {
        setExitSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                super.onMapSharedElements(names, sharedElements);
                if (sharedElements.isEmpty()) {
                    View view = mRecyclerView.getLayoutManager().findViewByPosition(mViewPosition);
                    if (view != null) {
                        sharedElements.put(names.get(0), view);
                    }
                }
            }
        });
    }

    private void init() {
        int columnNumber = getResources().getInteger(R.integer.column_number);
        mRecyclerView.setLayoutManager(new GridLayoutManager(this, columnNumber));
        int gridSpacing = getResources().getDimensionPixelSize(R.dimen.grid_spacing);
        mRecyclerView.addItemDecoration(new GridSpacingItemDecoration(columnNumber, gridSpacing, true));
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (mCategory.equals(Category.FAVORITE)) {
                    loadFavoriteMoviesFromDb();
                } else {
                    getContent(1);
                }
            }
        });
    }

    private void loadFavoriteMoviesFromDb() {
        new Thread() {
            @Override
            public void run() {
                final ArrayList<Movie> movieArrayList = new ArrayList<>();
                final Cursor query = getContentResolver().query(CONTENT_URI, MOVIE_PROJECTION, null, null, null);
                if (query != null) {
                    if (query.moveToFirst()) {
                        do {
                            movieArrayList.add(Movie.fromCursor(query));
                        } while (query.moveToNext());
                    }
                }

                if (!MainActivity.this.isFinishing() && !MainActivity.this.isDestroyed()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GetMovieList response = new GetMovieList();
                            response.setPage(1);
                            response.setTotal_pages(1);
                            response.setTotal_results(1);
                            response.setResults(movieArrayList);

                            onResponse(response);
                        }
                    });
                }
            }
        }.start();
    }

    @Override
    protected void onStop() {
        VolleySingleton.getInstance(this).cancelPendingRequests(MainActivity.class);
        super.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(BUNDLE_NETWORK_DATA, mGetMovieListResponse);
        outState.putString(BUNDLE_LAST_CATEGORY, mCategory);

        RecyclerView.Adapter currentAdapter = mRecyclerView.getAdapter();
        if (currentAdapter instanceof MovieAdapter) {
            outState.putParcelableArrayList(BUNDLE_ADAPTER_DATA, ((MovieAdapter) mRecyclerView.getAdapter()).getData());
            outState.putParcelable(BUNDLE_RECYCLER_STATE, mRecyclerView.getLayoutManager().onSaveInstanceState());
            outState.putInt(BUNDLE_ITEM_VIEW_POSITION, mViewPosition);
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_FAV_CHANGE && resultCode == Activity.RESULT_OK) {
            if (mCategory.equals(Category.FAVORITE)) {
                if (data.hasExtra("UNFAVORITE_ID")) {
                    String movieId = data.getStringExtra("UNFAVORITE_ID");
                    RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
                    if (adapter instanceof MovieAdapter) {
                        ((MovieAdapter) adapter).removeItemWithMovieId(movieId);
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.category_most_popular:
                changeCategory(Category.POPULAR);
                return true;
            case R.id.category_top_rated:
                changeCategory(Category.TOP_RATED);
                return true;
            case R.id.category_favorite:
                changeCategory(Category.FAVORITE);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Get content from API server
     *
     * @param page
     */
    private void getContent(int page) {
        if (NetworkChecker.isNetworkAvailable(this)) {
            if (!mIsRequestingData) {
                if (!TextUtils.isEmpty(mCategory)) {
                    mIsRequestingData = true;
                    if (page > 1) {//load more
                        mBottomLoading.setVisibility(View.VISIBLE);
                    }
                    final GsonRequest<GetMovieList> gsonRequest =
                            new GsonRequest<>(
                                    UrlComposer.getMovieUrl(mCategory, page),
                                    GetMovieList.class,
                                    null,
                                    this,
                                    this
                            );
                    VolleySingleton.getInstance(MainActivity.this)
                            .addToRequestQueue(gsonRequest, MainActivity.class);
                }
            }
        } else {
            CustomToast.show(MainActivity.this, getString(R.string.no_internet_connection));
            mRecyclerView.setAdapter(new EmptyAdapter());

            mSwipeRefresh.setRefreshing(false);
            mBottomLoading.setVisibility(View.GONE);
            mIsRequestingData = false;
        }
    }

    /**
     * Volley callback when request error
     *
     * @param volleyError
     */
    @Override
    public void onErrorResponse(VolleyError volleyError) {
        if (volleyError != null) {
            CustomToast.show(MainActivity.this, volleyError.getMessage());
            Log.e("GetMovieList", volleyError.getMessage());
        }

        mSwipeRefresh.setRefreshing(false);
        mBottomLoading.setVisibility(View.GONE);
        mIsRequestingData = false;
    }

    /**
     * Volley callback when request success
     *
     * @param currentResponse
     */
    @Override
    public void onResponse(@NonNull GetMovieList currentResponse) {
        if (currentResponse.getPage() == 1) {
            if (mSwipeRefresh.isRefreshing()) {
                //action refresh
                RecyclerView.Adapter currentAdapter = mRecyclerView.getAdapter();
                if (currentAdapter == null || currentAdapter instanceof EmptyAdapter) {
                    mRecyclerView.setAdapter(new MovieAdapter(currentResponse.getResults()));
                } else {
                    if (mGetMovieListResponse != null && !hasNewMovie(currentResponse)) {
                        CustomToast.show(
                                MainActivity.this,
                                getString(
                                        R.string.no_new_movies,
                                        Category.getCategoryName(MainActivity.this, mCategory)
                                )
                        );
                    } else {
                        ((MovieAdapter) currentAdapter).set(currentResponse.getResults());
                    }
                }
            } else {
                // action change category
                RecyclerView.Adapter currentAdapter = mRecyclerView.getAdapter();
                if (currentAdapter != null && currentAdapter instanceof MovieAdapter) {
                    ((MovieAdapter) currentAdapter).set(currentResponse.getResults());
                } else {
                    mRecyclerView.setAdapter(new MovieAdapter(currentResponse.getResults()));
                }
            }
        } else {
            RecyclerView.Adapter currentAdapter = mRecyclerView.getAdapter();
            if (currentAdapter instanceof MovieAdapter) {
                ((MovieAdapter) currentAdapter).append(currentResponse.getResults());
            } else {
                mRecyclerView.setAdapter(new MovieAdapter(currentResponse.getResults()));
            }
        }

        mGetMovieListResponse = currentResponse;

        addEndlessScrollListener();

        mSwipeRefresh.setRefreshing(false);
        mBottomLoading.setVisibility(View.GONE);
        mIsRequestingData = false;
    }

    /**
     * This method call is intentionally after the data exist, since endless scroll listener need the data
     */
    private void addEndlessScrollListener() {
        if (mEndlessRecyclerViewScrollListener == null) {
            mRecyclerView.clearOnScrollListeners();
            GridLayoutManager gridLayoutManager = (GridLayoutManager) mRecyclerView.getLayoutManager();
            mEndlessRecyclerViewScrollListener = new EndlessRecyclerViewScrollListener(gridLayoutManager) {
                @Override
                public void onLoadMore(int page, int totalItemsCount, RecyclerView view) {
                    if (mGetMovieListResponse.getPage() < mGetMovieListResponse.getTotal_pages()) {
                        getContent(mGetMovieListResponse.getPage() + 1);
                    }
                }
            };
            mRecyclerView.addOnScrollListener(mEndlessRecyclerViewScrollListener);
        }
    }

    /**
     * Check if there is new movies from the same category
     *
     * @param newResponse
     * @return boolean indicating there is new movies or not
     */
    private boolean hasNewMovie(GetMovieList newResponse) {
        return !((MovieAdapter) mRecyclerView.getAdapter()).getData().get(0).getId()
                .equals(newResponse.getResults().get(0).getId());
    }

    private void changeCategory(String category) {
        if (!mCategory.equals(category)) {
            mCategory = category;
            CustomToast.show(this,
                    getString(
                            R.string.changing_category,
                            Category.getCategoryName(MainActivity.this, mCategory)
                    )
            );

            if (category.equals(Category.FAVORITE)) {
                loadFavoriteMoviesFromDb();
            } else {
                getContent(1);
            }
        } else {
            CustomToast.show(this, getString(R.string.same_category));
        }
    }

    @Override
    public void onItemClick(int position, Movie movieData, View view) {
        mViewPosition = position;
        openDetailActivity(movieData, view);
    }

    private void openDetailActivity(Movie movieData, View view) {
        Intent openDetailIntent = new Intent(MainActivity.this, DetailActivity.class);
        openDetailIntent.putExtra(DetailActivity.KEY_MOVIE_DATA, movieData);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            startActivityForResult(openDetailIntent, RC_FAV_CHANGE);
        } else {
            startActivityForResult(openDetailIntent, RC_FAV_CHANGE,
                    ActivityOptions.makeSceneTransitionAnimation(
                            MainActivity.this,
                            view,
                            view.getTransitionName()
                    ).toBundle());
        }
    }
}
