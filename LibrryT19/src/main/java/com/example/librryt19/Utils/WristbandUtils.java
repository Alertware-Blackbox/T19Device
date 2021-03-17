package com.example.librryt19.Utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.Uri;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


import com.example.librryt19.R;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import javax.crypto.Cipher;


public class WristbandUtils {
    public static ProgressDialog progressDoalog;
    @SuppressLint("MissingPermission")
    public static String getIMEI(Activity activity) {
        TelephonyManager telephonyManager = (TelephonyManager) activity
                .getSystemService(Context.TELEPHONY_SERVICE);
        return telephonyManager.getDeviceId();
    }

    public static String getUNIQUEID(Activity activity) {
        return Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void hideKeyboardFrom(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        View view = activity.getCurrentFocus();
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static void toast(Activity activity, String textValue) {
        LayoutInflater inflater = activity.getLayoutInflater();
        View layout = inflater.inflate(R.layout.toast,
                (ViewGroup) activity.findViewById(R.id.toast_layout_root));

        TextView text = (TextView) layout.findViewById(R.id.text);
        text.setText(textValue);

        Toast toast = new Toast(activity);
        toast.setGravity(Gravity.CENTER_HORIZONTAL| Gravity.BOTTOM, 0, 50);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
    }

    public static String getCurrentDate(DatePicker picker) {
        //String selectedDate = picker.getDayOfMonth() + "-" + ((picker.getMonth() + 1)) + "-" + picker.getYear();
        StringBuilder builder = new StringBuilder();
        builder.append(picker.getDayOfMonth() + "-");
        builder.append((picker.getMonth() + 1) + "-");
        builder.append(picker.getYear());
        return builder.toString();
    }
    public static void alertBack(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Confirm!")
                .setMessage("Are you sure you want to exit?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        activity.finish();
                        activity.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    public static void alertSubmit(final Activity activity) {
        new AlertDialog.Builder(activity)
                .setTitle("Confirm!")
                .setMessage("Are you want to save the artisan data?")
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        activity.finish();
                        activity.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);

                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                })
                .setNegativeButton(android.R.string.no, null).show();
    }

    public static void checkGPSStatus(final Activity activity) {
        LocationManager locationManager = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (locationManager == null) {
            locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);
        }
        try {
            gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {
        }
        try {
            network_enabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (!gps_enabled && !network_enabled) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
            dialog.setMessage("GPS not enabled");
            dialog.setCancelable(false);
            dialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //this will navigate user to the device location settings screen
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    activity.startActivity(intent);
                }
            });
            AlertDialog alert = dialog.create();
            alert.show();
        }
    }

    public static String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }



    public static String getDateTimeYYYYMMDD() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyyMMdd", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static boolean isLegalDate(String s) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date strDate = sdf.parse(s);
        if (new Date().after(strDate)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getTimeStamp() {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return String.valueOf(timestamp.getTime());
    }

    public static String getAndroidId(Activity activity){
        String android_id = Settings.Secure.getString(activity.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        return android_id;
    }

    public static String IMAGE_TYPE_ID_PROOF = "road_image";
    public static int IMAGE_TYPE_ID_PROOF_CODE = 103;

    public static String TRIP_STATUS_START = "0";
    public static String TRIP_STATUS_END = "1";


    public static String TRIP_STATUS_START_INTERMEDIATE = "0";
    public static String TRIP_STATUS_PAUSE_INTERMEDIATE = "1";
    public static String TRIP_STATUS_PAUSE_END = "2";

    public static String NO_STATUS = "-1";

    public static String getLocalDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        return dateFormat.format(cal.getTime());
    }

    public static String getLocalTime() {
        DateFormat dateFormat1 = new SimpleDateFormat("HH:mm:ss");
        Calendar cal1 = Calendar.getInstance(TimeZone.getTimeZone("GMT+5:30"));
        return dateFormat1.format(cal1.getTime());
    }

    public static String getStringValue(String strValue) {
        return strValue == null ? "" : strValue;
    }

    public static String getStringValue(String strValue, String opValue) {
        return strValue == null ? opValue : strValue;
    }

    public static void hideSoftKeyboard(EditText input, Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }

    public static String getFileExtension(File file) {
        String fileName = file.getName();
        if (fileName.lastIndexOf(".") != -1 && fileName.lastIndexOf(".") != 0)
            return fileName.substring(fileName.lastIndexOf(".") + 1);
        else return "";
    }

    public static boolean isValidEmail(String email) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    public static void hideAppbar(AppCompatActivity activity) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
    }

    public static void showAppbar(AppCompatActivity activity) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.show();
        }
    }


    public static int[] Graphcolors = new int[]{
            Color.rgb(255, 0, 0),
            Color.rgb(76, 153, 60),
            Color.rgb(0, 0, 204),
            Color.rgb(255, 0, 127),
            Color.rgb(175, 0, 42),
            Color.rgb(59, 122, 87),
            Color.rgb(196, 98, 16),
            Color.rgb(175, 0, 42),
            Color.rgb(255, 191, 0),
            Color.rgb(124, 185, 232),
            Color.rgb(178, 132, 190),
            Color.rgb(171, 39, 79),
            Color.rgb(255, 165, 0),
            Color.rgb(128, 128, 0),
            Color.rgb(0, 112, 204),
            Color.rgb(255, 0, 0),
            Color.rgb(76, 153, 60),
            Color.rgb(0, 0, 204),
            Color.rgb(255, 0, 127),
            Color.rgb(175, 0, 42),
            Color.rgb(59, 122, 87),
            Color.rgb(196, 98, 16),
            Color.rgb(175, 0, 42),
            Color.rgb(255, 191, 0),
            Color.rgb(124, 185, 232),
            Color.rgb(178, 132, 190),
            Color.rgb(171, 39, 79),
            Color.rgb(255, 165, 0),
            Color.rgb(128, 128, 0),
            Color.rgb(0, 112, 204)
    };

    public static String PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtPRBURPWAFr/cL7nXQoH" +
            "mQpInltgdwPgug/M2KrqDmbeNXo3mvrNPvDolfOQOK9CAzmNolujN46GeMFaw5Oo" +
            "Dr7oOtrNEzRn1WBdxBTukiXS8LUDn/2+3ioVZgPJUpSorRtnjo56hg2S06zEJUhQ" +
            "3Xi62YbqqCEJi/L+FDb69UjklA+GQOrR86xtzNUZAFeVG3NbVX9haj7DS2l6MSGb" +
            "pmUc1sp2NZ1+T4ARfJTAFN72dDnBMSmbfTLtSgNUnSoB+kKByT6GjHP95z7pz5CH" +
            "CTWHm3UVutbCHyfX0GgraNM/3Vo7IYO0y3yO0jpFfwFn6W4dS1GjVa18wMXW3ZJH" +
            "MQIDAQAB";

    public static String enccriptData(String txt) {
        String encoded = "";
        byte[] encrypted = null;
        try {
            byte[] publicBytes = Base64.decode(PUBLIC_KEY, Base64.DEFAULT);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey pubKey = keyFactory.generatePublic(keySpec);
            Cipher cipher = Cipher.getInstance("RSA/NONE/OAEPWithSHA1AndMGF1Padding", "BC"); //or try with "RSA"
            cipher.init(Cipher.ENCRYPT_MODE, pubKey);
            encrypted = cipher.doFinal(txt.getBytes());
            encoded = Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encoded;
    }

    // Get Device Current Time
    public static String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String createDateHex(){
        String dateTime[] = getCurrentTime().split("-");
        String year = ((Integer.toHexString(Integer.parseInt(dateTime[0])).length() == 3)
                ? "0"+ Integer.toHexString(Integer.parseInt(dateTime[0])) : Integer.toHexString(Integer.parseInt(dateTime[0])));
        String finalString= lastTwoCharecter(year)+  // YY - Year MSB
                firstTwoCharecte(year);  // YY - Year LSB
        // MM (Month),  DD (Day), HH (Hour), MM (Minute), SS (Secs)
        for(int i=1;i<dateTime.length;i++){
            finalString += ((Integer.toHexString(Integer.parseInt(dateTime[i])).length() == 1)
                    ? "0"+ Integer.toHexString(Integer.parseInt(dateTime[i])) : Integer.toHexString(Integer.parseInt(dateTime[i])));
        }
        Calendar c = Calendar.getInstance();
        System.out.println(c.get(Calendar.DAY_OF_WEEK));
        finalString += "0"+(c.getTime().getDay());  //WEEKDAYNAME Week name (Monday=01, Tuesday=02 etc.)
        finalString += "00"; //FRACTIONS (millsecs)- 00 –FF
        return finalString.toUpperCase()+ "01"; //Adjust - 01
    }


    public static String firstTwoCharecte(String str) {

        if(str.length()<2){
            return str;
        }
        else{
            return str.substring(0,2);
        }
    }

    public static String lastTwoCharecter(String value){
        String lastTwo = null;
        if (value != null && value.length() >= 2) {
            lastTwo = value.substring(value.length() - 2);
        }
        return lastTwo;
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static void openActivity(Activity activity, Class destination, boolean isFinish){
        Intent i = new Intent(activity, destination);
        activity.startActivity(i);
        if(isFinish) {
            activity.finish();
        }
        activity.overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    public static void closeActivity(Activity activity){
        activity.finish();
        activity.overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }

    public static String convertHexFromDecimal(String selectedVal){
        return "00"+ Integer.toHexString(Integer.parseInt(selectedVal));
    }

    public static String makeMacAddress(String str) {
        return str.substring(0, 2)
                + ":"
                + str.substring(2, 4)
                + ":"
                + str.substring(4, 6)
                + ":"
                + str.substring(6, 8)
                + ":"
                + str.substring(8, 10)
                + ":"
                +str.substring(10, 12);
    }

    public static int getDateTimeOnedayBack() {

        DateFormat dateFormat = new SimpleDateFormat("yyMMddHHmm");

        Date currentDate = new Date();

        // convert date to calendar
        Calendar c = Calendar.getInstance();
        c.setTime(currentDate);

        // manipulate date
        //c.add(Calendar.YEAR, 1);
        //c.add(Calendar.MONTH, 1);
        c.add(Calendar.DATE, -1); //same with c.add(Calendar.DAY_OF_MONTH, 1);
        //c.add(Calendar.HOUR, 1);
        //c.add(Calendar.MINUTE, 1);
        //c.add(Calendar.SECOND, 1);

        // convert calendar to date
        Date currentDatePlusOne = c.getTime();
        return Integer.parseInt(dateFormat.format(currentDatePlusOne));
    }

//    public void prepareOtaFile(Uri uri, OtaFileType type, String filename,Activity activity) {
//
//        try {
//            InputStream is = activity.getContentResolver().openInputStream(uri);
//
//            if (is == null) {
//                Toast.makeText(activity, activity.getResources().getString(R.string.project_id),
//                        Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            File file = new File(activity.getCacheDir(), filename);
//
//            OutputStream output = new FileOutputStream(file);
//            byte[] buffer = new byte[4 * 1024];
//            int read;
//
//            while ((read = is.read(buffer)) != -1) {
//                output.write(buffer, 0, read);
//            }
//
//            if (type.equals(OtaFileType.APPLICATION)) {
//                appPath = file.getAbsolutePath();
//                appFileButton.setText(filename);
//            } else {
//                stackPath = file.getAbsolutePath();
//                appLoaderFileButton.setText(filename);
//            }
//
//            output.flush();
//        } catch (IOException e) {
//            e.printStackTrace();
//            //Toast.makeText(DeviceServicesActivity.this, getResources().getString(R.string.Incorrect_file), Toast.LENGTH_SHORT).show();
//        }
//
//    }

    public static byte[] DFUMode(Activity activity) {
        //switch (step) {
        /**SET THE FILES TO BE UPLOADED TO OTA_DATA CHARACTERISTIC*/
                Log.d("OTAUPLOAD", "Called");
                //File sdcard = Environment.getExternalStorageDirectory();
                File futureStudioIconFile = new File(activity.getExternalFilesDir(null) + File.separator +"t19"
                        + File.separator+ "t19_update.gbl");
                //File futureStudioIconFile = new File(activity.getExternalFilesDir(null) , "t19");

        /**Check Services*/
                //BluetoothGattService mBluetoothGattService = bluetoothGatt.getService(ota_service);
                //if (mBluetoothGattService != null) {
                    //BluetoothGattCharacteristic charac = bluetoothGatt.getService(ota_service).getCharacteristic(ota_data);
                    //if (charac != null) {
                        //charac.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
                        //Log.d("Instance ID", "" + charac.getInstanceId());
                        /**Check Files*/
                        byte[] ebl = null;
                        try {
                           // Log.d("stackPath", "" + stackPath);
                           // Log.d("appPath", "" + appPath);
                            File file;
                            // if (!stackPath.equals("") && doubleStepUpload) {
                            //    file = new File(stackPath);
                            //    boolFullOTA = true;
                            //} else {
                                //file = new File(sdcard,appPath);
                             //   boolFullOTA = false;
                            //}
                            FileInputStream fileInputStream = new FileInputStream(futureStudioIconFile);

                            int size = fileInputStream.available();
                            Log.d("size", "" + size);
                            byte[] temp = new byte[size];
                            fileInputStream.read(temp);
                            fileInputStream.close();
                            ebl = temp;
                            return temp;
                        } catch (Exception e) {
                            Log.e("InputStream", "Couldn't open file" + e);
                            return null;
                        }
                   // }
                //}
      //  }
    }
    public static void turnGPSOn(Activity activity){
        String provider = Settings.Secure.getString(activity.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);

        if(!provider.contains("gps")){ //if gps is disabled
            final Intent poke = new Intent();
            poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
            poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
            poke.setData(Uri.parse("3"));
            activity.sendBroadcast(poke);
        }
    }
}
