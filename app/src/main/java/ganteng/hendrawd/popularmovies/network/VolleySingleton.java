package ganteng.hendrawd.popularmovies.network;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RequestTickle;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.VolleyTickle;

public class VolleySingleton {
    private static VolleySingleton mInstance = null;
    private RequestQueue mRequestQueue;
    private RequestTickle mRequestTickle;
    private OkHttp3Stack mOkHttp3Stack;
    private static final String TAG = VolleySingleton.class.getSimpleName();

    private VolleySingleton(Context context) {
        mOkHttp3Stack = new OkHttp3Stack();
        mRequestQueue = Volley.newRequestQueue(context, mOkHttp3Stack);
        mRequestTickle = VolleyTickle.newRequestTickle(context, mOkHttp3Stack);
//        mRequestQueue = Volley.newRequestQueue(context);
    }

    public static synchronized VolleySingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new VolleySingleton(context);
        }
        return mInstance;
    }

    private RequestQueue getRequestQueue() {
        return this.mRequestQueue;
    }

    private RequestTickle getRequestTickle() {
        return this.mRequestTickle;
    }

    /**
     * Helper classes
     */
    public <T> void addToRequestQueue(Request<T> req, Object tag) {
        // set the default tag if tag is empty
        req.setTag(tag == null ? TAG : tag);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestQueue(Request<T> req) {
        req.setTag(TAG);
        getRequestQueue().add(req);
    }

    public <T> void addToRequestTickle(Request<T> req, Object tag) {
        // set the default tag if tag is empty
        req.setTag(tag == null ? TAG : tag);
        getRequestTickle().add(req);
    }

    public <T> void addToRequestTickle(Request<T> req) {
        req.setTag(TAG);
        getRequestTickle().add(req);
    }

    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
            if (mOkHttp3Stack != null)
                mOkHttp3Stack.cancelRequestOnGoing(tag);
        }
    }

    public void cancelPendingRequestsNoTag() {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(TAG);
            if (mOkHttp3Stack != null)
                mOkHttp3Stack.cancelRequestOnGoing(TAG);
        }
    }

    public void clearVolleyCache() {
        if (mRequestQueue != null) {
            mRequestQueue.getCache().clear();
        }
    }
    //End helper classes
}