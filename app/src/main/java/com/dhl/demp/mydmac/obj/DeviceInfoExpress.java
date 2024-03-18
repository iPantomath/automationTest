package com.dhl.demp.mydmac.obj;

import android.content.Context;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Method;

/**
 * Created by jahofman on 3/7/2016.
 */
public class DeviceInfoExpress extends DeviceInfo {
    private static final String DEFAULT_HOSTNAME = "N/A";
    @SerializedName("hostname")
    @Expose
    public String hostName;
    @SerializedName("serial_num")
    @Expose
    public String serialNum;
    @SerializedName("imei")
    @Expose
    public String IMEI;

    public DeviceInfoExpress(Context context, String sn, String imei) {
        super(context);

        //hostname
        try{
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            hostName = (String) get.invoke(c, "net.hostname", DEFAULT_HOSTNAME);
            if (hostName.equals(DEFAULT_HOSTNAME)) {
                hostName = (String) get.invoke(c, "net.hostname", DEFAULT_HOSTNAME);
            }
        }catch(Exception e){
            hostName = DEFAULT_HOSTNAME;
        }

        serialNum = sn;
        IMEI = imei;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        DeviceInfoExpress that = (DeviceInfoExpress) o;

        if (hostName != null ? !hostName.equals(that.hostName) : that.hostName != null)
            return false;
        if (serialNum != null ? !serialNum.equals(that.serialNum) : that.serialNum != null)
            return false;
        return IMEI != null ? IMEI.equals(that.IMEI) : that.IMEI == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (hostName != null ? hostName.hashCode() : 0);
        result = 31 * result + (serialNum != null ? serialNum.hashCode() : 0);
        result = 31 * result + (IMEI != null ? IMEI.hashCode() : 0);
        return result;
    }
}
