package org.lamorim.huxflooderapp.utility;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

import org.lamorim.huxflooderapp.R;

/**
 * Created by lucas on 22/11/2016.
 */

public class ProgressHelper {
    public static void showCustomDialog(Activity context, String msg, String title, boolean isErrDialog) {
        AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(context);
        dlgAlert.setMessage(msg);
        if (!title.equals(""))
            dlgAlert.setTitle(title);
        if (isErrDialog)
            dlgAlert.setIcon(R.mipmap.ic_warning);
        else
        dlgAlert.setIcon(R.mipmap.ic_info);
        dlgAlert.setCancelable(false);
        dlgAlert.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        dlgAlert.create().show();
    }
}
