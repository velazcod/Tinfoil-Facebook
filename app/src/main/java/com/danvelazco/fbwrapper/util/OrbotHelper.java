package com.danvelazco.fbwrapper.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import com.danvelazco.fbwrapper.R;

/**
 * Modified utility class from OnionKit library
 */
public class OrbotHelper {

    // Constants
    public final static String URI_ORBOT = "org.torproject.android";
    public final static String TOR_BIN_PATH = "/data/data/org.torproject.android/app_bin/tor";
    public final static String ACTION_START_TOR = "org.torproject.android.START_TOR";
    public final static int REQUEST_CODE_START_ORBOT = 80010;

    // Members
    private Context mContext = null;

    /**
     * Constructor
     *
     * @param context {@link Context}
     */
    public OrbotHelper(Context context) {
        mContext = context;
    }

    /**
     * Check whether or not Orbot is running.
     *
     * @return {@link boolean}
     */
    public boolean isOrbotRunning() {
        int procId = TorServiceUtils.findProcessId(TOR_BIN_PATH);
        return (procId != -1);
    }

    /**
     * Check whether or not Orbot is installed.
     *
     * @return {@link boolean}
     */
    public boolean isOrbotInstalled() {
        return isAppInstalled(URI_ORBOT);
    }

    private boolean isAppInstalled(String uri) {
        PackageManager pm = mContext.getPackageManager();
        boolean installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            installed = false;
        }
        return installed;
    }

    /**
     * Request Orbot to start and connect
     *
     * @param activity {@link Activity}
     */
    public void requestOrbotStart(final Activity activity) {
        AlertDialog.Builder downloadDialog = new AlertDialog.Builder(activity);
        downloadDialog.setTitle(R.string.start_orbot_);
        downloadDialog.setMessage(R.string.orbot_not_running_start_it_question);
        downloadDialog.setPositiveButton(R.string.lbl_yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(URI_ORBOT);
                intent.setAction(ACTION_START_TOR);
                activity.startActivityForResult(intent, REQUEST_CODE_START_ORBOT);
            }
        });
        downloadDialog.setNegativeButton(R.string.lbl_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        downloadDialog.show();

    }

}
