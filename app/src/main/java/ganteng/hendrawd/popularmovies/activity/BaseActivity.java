package ganteng.hendrawd.popularmovies.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import ganteng.hendrawd.popularmovies.R;

/**
 * @author hendrawd on 6/23/17
 */

public abstract class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("Low Memory", "---Low on memory---");
    }

    public void showProgressDialog() {
        showProgressDialog(
                getString(R.string.please_wait),
                getString(R.string.processing)
        );
    }

    public void showProgressDialog(final String dialogTitle, final String dialogMessage) {
        //make sure to run on ui thread to avoid force closed
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProgressDialog = ProgressDialog.show(
                        BaseActivity.this,
                        dialogTitle,
                        dialogMessage,
                        true
                );
            }
        });
    }

    public void hideProgressDialog() {
        //make sure to run on ui thread to avoid force closed
        runOnUiThread(
                new Runnable() {
                    @Override
                    public void run() {
                        mProgressDialog.dismiss();
                    }
                }
        );
    }

    /**
     * see http://stackoverflow.com/questions/7469082/getting-exception-illegalstateexception-can-not-perform-this-action-after-onsa/10261438#10261438
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("WORKAROUND_FOR_BUG_19917_KEY", "WORKAROUND_FOR_BUG_19917_VALUE");
        super.onSaveInstanceState(outState);
    }
}
