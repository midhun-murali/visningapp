package com.dev.macx.visningsappen.Utils;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import java.util.List;

/**
 * This class is used for..
 *
 * @author Midhun.
 */

public class DialogFactory {

    private static final String DEFULT_POSITIVE_BTN_TEXT = "OK";

    private static Dialog sDialog = null;


    public static void showDialog(AppCompatActivity appCompatActivity, String title, String message,
                                  String positiveBtn, DialogInterface.OnClickListener positiveBtnListener,
                                  String negativeBtn, DialogInterface.OnClickListener negativeBtnListener,
                                  String neutralBtn, DialogInterface.OnClickListener neutralBtnListener,
                                  boolean isDismissable) {

        if (appCompatActivity.isFinishing()) {
            return;
        }

        if (null != sDialog && sDialog.isShowing()) {
            sDialog.dismiss();
            sDialog = null;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(appCompatActivity);
        builder.setTitle(title)
                .setMessage(message)
                .setCancelable(isDismissable);

        if (null != positiveBtn) {
            builder.setPositiveButton(positiveBtn, positiveBtnListener);
        }

        if (null != negativeBtn) {
            builder.setNegativeButton(negativeBtn, negativeBtnListener);
        }

        if (null != neutralBtn) {
            builder.setNeutralButton(neutralBtn, neutralBtnListener);
        }

        sDialog = builder.create();
        sDialog.show();
    }

    public static void showDialog(AppCompatActivity appCompatActivity, int title, String message, int positiveBtn, DialogInterface.OnClickListener positiveBtnListener,
                                  int negativeBtn, DialogInterface.OnClickListener negativeBtnListener, boolean isDismissable) {
        showDialog(appCompatActivity, appCompatActivity.getString(title), message, appCompatActivity.getString(positiveBtn), positiveBtnListener, appCompatActivity.getString(negativeBtn), negativeBtnListener, null, null, isDismissable);
    }

}