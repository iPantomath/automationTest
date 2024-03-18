package com.dhl.demp.mydmac.utils;

import com.dhl.demp.mydmac.obj.Apka;
import com.dhl.demp.mydmac.obj.BBFiedApproveState;

/**
 * Created by robielok on 10/25/2017.
 */

public class BBFiedUtils {
    public static boolean needRequestApprove(Apka apka) {
        return BBFiedApproveState.VISIBLE.equals(apka.approveState);
    }

    public static boolean canInstallApp(Apka apka) {
        return (!apka.bbFied) || BBFiedApproveState.INSTALL_ALLOWED.equals(apka.approveState);
    }
}
