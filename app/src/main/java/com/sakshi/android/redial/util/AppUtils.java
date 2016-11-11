package com.sakshi.android.redial.util;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;

/**
 * Created by sakshiagarwal on 27/10/16.
 */

public class AppUtils {

    private static AppUtils instance;
    public static int NUMBER_OF_CALLS = 0;

    public static AppUtils getInstance(){
        if(instance == null){
            instance = new AppUtils();
        }
        return instance;
    }

    public void showAlertDialogBox(Context context, String message)
    {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setMessage(message);
        alertDialog.setTitle("Alert");
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        alertDialog.show();
    }
}
