package com.dhl.demp.mydmac.ui;

import android.text.Editable;
import android.text.TextWatcher;

/**
 * Created by robielok on 10/24/2017.
 */

public abstract class SimpleTextWatcher implements TextWatcher {
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        //let subclass override this method if needed
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        //let subclass override this method if needed
    }

    @Override
    public void afterTextChanged(Editable s) {
        //let subclass override this method if needed
    }
}
