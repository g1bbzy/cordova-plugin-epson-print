
package com.epson.epos2_printer;

import java.util.TimeZone;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Log;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;

public class EpsonPrint extends CordovaPlugin {

	private static Context mContext;

	// set static actions
	public static final String SHOWALERT = "ShowAlert";

	public EpsonPrint() {
	}

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
	}

	public boolean execute(String action, JSONArray arguments,
            CallbackContext callbackContext) throws JSONException {
		mContext = this.cordova.getActivity();

		try {
			if (SHOWALERT.equals(action)) {
				Context context = this.cordova.getActivity();
				ShowAlert("success!!", "we have a working plugin!");
				callbackContext.success();
				return true;
			}
			callbackContext.error("Invalid action");
			return false;
		} catch (Exception e) {
			System.err.println("Exception: " + e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		}
	}

	// --------------------------------------------------------------------------
	// LOCAL METHODS
	// --------------------------------------------------------------------------

	private static void ShowAlert(String Title, String Message) {
		Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setNegativeButton("Ok", null);
		AlertDialog alert = dialog.create();
		alert.setTitle(Title);
		alert.setMessage(Message);
		alert.setCancelable(false);
		alert.show();

	}

}