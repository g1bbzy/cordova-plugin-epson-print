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

        //make sure it is not looking already
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
        mCallbackContext.success("Stopped searching");
    }

    private DiscoveryListener mDiscoveryListener = new DiscoveryListener() {
        @Override
        public void onDiscovery(final DeviceInfo deviceInfo) {
            
            new Thread(new Runnable() {

                @Override
                public void run() {

                    boolean new_printer = true;

                    for (int i = 0; i < mfoundPrinters.length(); i++) {

                        try{

                            JSONObject current_printer = mfoundPrinters.getJSONObject(i);
                            if(current_printer.getString("target").equals(deviceInfo.getTarget())) {
                                new_printer = false;
                                break;
                            }
                        } catch (JSONException e) {
                            mCallbackContext.error("Invalid JSON");
                        }
                    }

                    if(new_printer == true) {
                        try{
                            JSONObject obj = new JSONObject();
                            obj.put("printer_name", deviceInfo.getDeviceName());
                            obj.put("target", deviceInfo.getTarget());
                            obj.put("mac", deviceInfo.getMacAddress());
                            obj.put("brand", "Epson");
                            mCallbackContext.success(obj);
                        } catch (JSONException e) {
                            mCallbackContext.error("Error building JSON packet");
                        }
                        try {
                            Discovery.stop();
                        }
                        catch (Epos2Exception e) {
                            
                        }
                    }
                }
            }).start();
        }
    };
}