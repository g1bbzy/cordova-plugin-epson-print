import java.util.TimeZone;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

public class EpsonPrinter implements ReceiveListener {

    private Context mContext = null;
    private Printer  mPrinter = null;
    private String  printer_ip_address = null;
    private String  image_to_print = null;
    private String  printer_series = null;

    public EpsonPrinter(Context pContext) {
        mContext = pContext;
    }

    public boolean runPrintReceiptSequence(String ip, String image, String series) {

        printer_ip_address = ip;
        image_to_print = image;
        printer_series = series;
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

            int series;
            switch (printer_series) {
                case "TM-m10" : series = Printer.TM_M10;
                    break;
                case "TM-m30" : series = Printer.TM_M30;
                    break;
                case "TM-P20" : series = Printer.TM_P20;
                    break;
                case "TM-P60" : series = Printer.TM_P60;
                    break;
                case "TM-P60II" : series = Printer.TM_P60II;
                    break;
                case "TM-P80" : series = Printer.TM_P80;
                    break;
                case "TM-T20" : series = Printer.TM_T20;
                    break;
                case "TM-T60" : series = Printer.TM_T60;
                    break;
                case "TM-T70" : series = Printer.TM_T70;
                    break;
                case "TM-T81" : series = Printer.TM_T81;
                    break;
                case "TM-T82" : series = Printer.TM_T82;
                    break;
                case "TM-T83" : series = Printer.TM_T83;
                    break;
                case "TM-T88" : series = Printer.TM_T88;
                    break;
                case "TM-T90" : series = Printer.TM_T90;
                    break;
                case "TM-T90KP" : series = Printer.TM_U220;
                    break;
                case "TM-U220" : series = Printer.TM_U330;
                    break;
                case "TM-L90" : series = Printer.TM_L90;
                    break;
                case "TM-H6000" : series = Printer.TM_H6000;
                    break;
                default: series = Printer.TM_M10;
                    break;
            }
            
            mPrinter = new Printer(series,Printer.MODEL_ANK,mContext);

        }
        catch (Exception e) {
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
            return false;
        }

        try {
            mPrinter.beginTransaction();
            isBeginTransaction = true;
        }
        catch (Exception e) {
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
        }

        try {
            mPrinter.disconnect();
        }
        catch (final Exception e) {
        }

        finalizeObject();
    }
    @Override
    public void onPtrReceive(final Printer printerObj, final int code, final PrinterStatusInfo status, final String printJobId) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                disconnectPrinter();
            }
        }).start();
            
    }
}