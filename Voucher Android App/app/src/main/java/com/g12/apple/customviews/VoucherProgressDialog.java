package com.g12.apple.customviews;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.g12.apple.R;


public class VoucherProgressDialog {
    private static final String TAG = "VoucherProgressDialog";
    private static Dialog mDialog;

    public static void show(Context context) {
        show(context, true);
    }

    public static void show(Context context, boolean cancelable) {
        try {
            if (mDialog == null) {
                mDialog = new Dialog(context);
            } else if (mDialog.isShowing()) {
                mDialog.dismiss();
            }

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = inflater.inflate(R.layout.custom_progres, null);

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
            mDialog.addContentView(v, params);
            mDialog.setCancelable(cancelable);
            mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
            mDialog.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void dismissDialog() {
        try {
            if (mDialog != null) {
                mDialog.dismiss();
                mDialog = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
