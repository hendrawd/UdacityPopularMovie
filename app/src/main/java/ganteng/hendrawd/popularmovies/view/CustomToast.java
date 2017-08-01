package ganteng.hendrawd.popularmovies.view;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * An implementation of a Toast that close right away and replaced with a new one when the
 * method is called
 * @author hendrawd on 6/6/16
 */
public class CustomToast {
    private static Toast toast;

    public static void show(Context context, final String message) {
        final Context applicationContext = context.getApplicationContext();//make sure that there is no activity leak
        try {
            //make sure that the code will run on ui thread using handler post
            new Handler().post(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (toast != null) {
                                toast.cancel();
                            }
                            toast = Toast.makeText(applicationContext, message, Toast.LENGTH_LONG);
                            toast.show();
                        }
                    }
            );
        } catch (Exception e) {
            //in case context is not present, etc
        }
    }
}
