import android.content.Context;

import java.util.ArrayList;
import java.util.HashMap;

import com.epson.epos2.discovery.Discovery;
import com.epson.epos2.discovery.DiscoveryListener;
import com.epson.epos2.discovery.FilterOption;
import com.epson.epos2.discovery.DeviceInfo;
import com.epson.epos2.Epos2Exception;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import org.apache.cordova.CallbackContext;

public class PrinterSearch {

    private Context mContext = null;
    private CallbackContext mCallbackContext = null;
    private ArrayList<HashMap<String, String>> mPrinterList = null;
    private FilterOption mFilterOption = null;
    private JSONArray mfoundPrinters = null;

    public PrinterSearch(Context pContext, CallbackContext pCallbackContext,JSONArray pfoundPrinters) {
        mContext = pContext;
        mCallbackContext = pCallbackContext;
        mfoundPrinters = pfoundPrinters;
    }


    public void search() {

        mPrinterList = new ArrayList<HashMap<String, String>>();
        mFilterOption = new FilterOption();
        mFilterOption.setDeviceType(Discovery.TYPE_PRINTER);
        mFilterOption.setEpsonFilter(Discovery.FILTER_NAME);
        try {
            Discovery.stop();
        }
        catch (Exception e) {
        }

        try {
            Discovery.start(mContext, mFilterOption, mDiscoveryListener);
        }
        catch (Exception e) {
            
        }
    }

    public void stop(){
        try {
            Discovery.stop();
        }
        catch (Exception e) {
        }
        mCallbackContext.success("");
    }

    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {

                    boolean lcNewPrinter = true;

                    for (int i = 0; i < mfoundPrinters.length(); i++) {
                        try{
                        JSONObject lcPrinter = mfoundPrinters.getJSONObject(i);
                        if(lcPrinter.getString("target").equals(deviceInfo.getTarget())) {
                            lcNewPrinter = false;
                            break;
                        }
                        } catch (JSONException e) {

                        }
                    }

                    if(lcNewPrinter == true) {
                        try{
                            JSONObject obj = new JSONObject();
                            obj.put("printer_name", deviceInfo.getDeviceName());
                            obj.put("target", deviceInfo.getTarget());
                            obj.put("mac", deviceInfo.getMacAddress());
                            obj.put("brand", "Epson");
                            mCallbackContext.success(obj);
                        } catch (JSONException e) {
                            
                        }
                        try {
                            Discovery.stop();
                        }
                        catch (Epos2Exception e) {
                            
                        }
                    }
                    else {

                    }
             
        }
    };
}