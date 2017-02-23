import java.util.TimeZone;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaInterface;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.Context;

public class EpsonController extends CordovaPlugin  {

	private Context mContext = null;

	// set static actions
	private static final String SHOWALERT = "ShowAlert";
	private static final String PRINTRECEIPT = "printReceipt";
	private static final String FINDPRINTERS = "findPrinters";
	private static final String STOPSEARCH = "stopSearch";

	private EpsonPrinter mPrinter = null;
	private PrinterSearch mPrinterSearch = null;

	public EpsonController() {
	}

	public void initialize(CordovaInterface cordova, CordovaWebView webView) {
		super.initialize(cordova, webView);
	}

	public boolean execute(String action, JSONArray arguments,
		CallbackContext callbackContext) throws JSONException {
		mContext = this.cordova.getActivity();

		try {
			if (PRINTRECEIPT.equals(action)) {

				String ip_address = (arguments.get(0).toString());
				String base64_image_str = (arguments.get(1).toString());

				mPrinter = new EpsonPrinter(mContext);
				if (mPrinter.runPrintReceiptSequence(ip_address , base64_image_str )) {
					callbackContext.success("Print success");
					return true;
				}
				else{
					callbackContext.error("error");
					return false;
				}
			}
			else if(FINDPRINTERS.equals(action)) {
				JSONArray found_printers = new JSONArray(arguments.get(0).toString());
				mPrinterSearch = new PrinterSearch(mContext, callbackContext, found_printers);
				mPrinterSearch.search();
				return true;
			}
			else if(STOPSEARCH.equals(action)) {
				JSONArray found_printers = new JSONArray();
				mPrinterSearch = new PrinterSearch(mContext, callbackContext, found_printers);
				mPrinterSearch.stop();
				return true;
			}

			callbackContext.error("Invalid action: " + action);
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

}