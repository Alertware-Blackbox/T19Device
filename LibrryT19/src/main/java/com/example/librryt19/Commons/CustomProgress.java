package com.example.librryt19.Commons;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.example.librryt19.R;


public class CustomProgress {

    private static CustomProgress mCustomProgress;
    private Dialog mDialog;

    private CustomProgress() {

    }

    public static CustomProgress getInstance() {
        if (mCustomProgress == null) {
            mCustomProgress = new CustomProgress();
        }
        return mCustomProgress;
    }

    public void showProgress(Context mContext, String msg) {
        mDialog = new Dialog(mContext);
        mDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDialog.setContentView(R.layout.custom_progress_layout);
        mDialog.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);
        mDialog.findViewById(R.id.progress_message).setVisibility(View.VISIBLE);
        TextView mMsg = mDialog.findViewById(R.id.progress_message);
        mMsg.setText(msg);
        //noinspection ConstantConditions
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        mDialog.setCancelable(false);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.show();
    }

    public void hideProgress() {
        if (mDialog!= null) {
            mDialog.dismiss();
            mDialog= null;
        }
    }
}