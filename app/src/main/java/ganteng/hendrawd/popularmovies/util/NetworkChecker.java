package ganteng.hendrawd.popularmovies.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * created on 1/20/16
 *
 * @author hendrawd
 */
public class NetworkChecker {

    /**
     * Check if network / internet is available or not
     *
     * @param context activity or application
     * @return state of the network(available or not)
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
