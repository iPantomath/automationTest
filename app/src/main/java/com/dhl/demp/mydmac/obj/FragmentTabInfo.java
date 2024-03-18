package com.dhl.demp.mydmac.obj;

import androidx.fragment.app.Fragment;

/**
 * Created by frogggias on 07.04.15.
 */
public class FragmentTabInfo {
    public final Class<? extends Fragment> cls;
    public final int tabNameId;

    public FragmentTabInfo(Class<? extends Fragment> cls, int tabNameId) {
        this.cls = cls;
        this.tabNameId = tabNameId;
    }
}
