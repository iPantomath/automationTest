package com.dhl.demp.mydmac.sso;

import android.content.Context;
import android.os.Bundle;

/**
 * Created by robielok on 11/13/2017.
 */

public interface SSOActionHandler {
    void onAction(Context context, String appId, String state, String callback, Bundle params);
}
