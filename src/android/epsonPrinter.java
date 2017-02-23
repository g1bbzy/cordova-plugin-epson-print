import java.util.TimeZone;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.provider.Settings;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.printer.Printer;
import com.epson.epos2.printer.PrinterStatusInfo;
import com.epson.epos2.printer.ReceiveListener;
import com.epson.epos2.Epos2CallbackCode;
import com.epson.epos2.Log;

public class EpsonPrinter extends Activity implements ReceiveListener {

    private Context mContext = null;
    private Printer  mPrinter = null;
    private String  printer_ip_address = null;
    private String  image_to_print = null;

    public EpsonPrinter(Context pContext) {
        mContext = pContext;
    }

    public void ShowAlert(String Title, String Message) {
        Builder dialog = new AlertDialog.Builder(mContext);
        dialog.setNegativeButton("Ok", null);
        AlertDialog alert = dialog.create();
        alert.setTitle(Title);
        alert.setMessage(Message);
        alert.setCancelable(false);
        alert.show();

    }

    public boolean runPrintReceiptSequence(String ip, String image) {

        printer_ip_address = ip;
        image_to_print = image;
        if (!initializeObject()) {
            return false;
        }

        if (!createReceiptData()) {
            finalizeObject();
            return false;
        }

        if (!printData()) {
            finalizeObject();
            return false;
        }

        return true;
    }

    private boolean initializeObject() {
        try {

            mPrinter = new Printer(Printer.TM_T88,Printer.MODEL_ANK,mContext);


        }
        catch (Exception e) {
            ShowAlert("Printer", "");
            return false;
        }

        mPrinter.setReceiveEventListener(this);

        return true;
    }

    private boolean connectPrinter() {
        boolean isBeginTransaction = false;

        if (mPrinter == null) {
            return false;
        }

        try {
            mPrinter.connect(printer_ip_address, Printer.PARAM_DEFAULT);

        }
        catch (Exception e) {
            ShowAlert("connect", e.getMessage());
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
            ShowAlert("Begin transaction", "");
        }

        if (isBeginTransaction == false) {
            try {
                mPrinter.disconnect();
            }
            catch (Epos2Exception e) {
                // Do nothing
                return false;
            }
        }

        return true;
    }



    private boolean createReceiptData() {

        String method = "image";

        byte[] decodedString = MyBase64.decode(image_to_print);
        Bitmap image = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);



        if (mPrinter == null) {
            return false;
        }

        try {
            method = "addTextAlign";
            mPrinter.addTextAlign(Printer.ALIGN_CENTER);

			method = "addImage";
			mPrinter.addImage(image, 0, 0,
                    image.getWidth(),
                    image.getHeight(),
					Printer.COLOR_1,
                    Printer.MODE_GRAY16,
					Printer.HALFTONE_DITHER,
					Printer.PARAM_DEFAULT,
					Printer.COMPRESS_NONE);

            method = "addFeedLine";
            mPrinter.addFeedLine(1);
            method = "addCut";
            mPrinter.addCut(Printer.CUT_FEED);
        }
        catch (Exception e) {
            return false;
        }

        return true;
    }

    private void finalizeObject() {
        if (mPrinter == null) {
            return;
        }

        mPrinter.clearCommandBuffer();

        mPrinter.setReceiveEventListener(null);

        mPrinter = null;
    }

    private boolean printData() {
        if (mPrinter == null) {
            return false;
        }

        if (!connectPrinter()) {
            return false;
        }

        PrinterStatusInfo status = mPrinter.getStatus();

        if (!isPrintable(status)) {
            ShowAlert("isprintable", "");
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        try {
            mPrinter.sendData(Printer.PARAM_DEFAULT);
        }
        catch (Exception e) {
            ShowAlert("senddata", "");
            try {
                mPrinter.disconnect();
            }
            catch (Exception ex) {
                // Do nothing
            }
            return false;
        }

        return true;
    }

    private boolean isPrintable(PrinterStatusInfo status) {
        if (status == null) {
            return false;
        }

        if (status.getConnection() == Printer.FALSE) {
            return false;
        }
        else if (status.getOnline() == Printer.FALSE) {
            return false;
        }
        else {
            ;//print available
        }

        return true;
    }
    private void disconnectPrinter() {
        if (mPrinter == null) {
            return;
        }

        try {
            mPrinter.endTransaction();
        }
        catch (final Exception e) {
            ShowAlert("endtransaction", "");
        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {
            ShowAlert("disconnectPrinter", "");
        }

        finalizeObject();
    }
    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        disconnectPrinter();
                    }
                }).start();
            }
        });
    }
}