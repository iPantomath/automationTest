package com.dhl.demp.mydmac.dialog;

import android.app.ProgressDialog;
import android.content.Context;

import mydmac.R;

/**
 * Created by robielok on 8/28/2017.
 */

public class RequestWaitDialog extends ProgressDialog {
    public RequestWaitDialog(Context context) {
        super(context);

        init(context);
    }

    public RequestWaitDialog(Context context, int theme) {
        super(context, theme);

        init(context);
    }

    private void init(Context context) {
        setIndeterminate(true);
        setMessage(context.getString(R.string.wait_request));
        setCancelable(false);
    }
}
