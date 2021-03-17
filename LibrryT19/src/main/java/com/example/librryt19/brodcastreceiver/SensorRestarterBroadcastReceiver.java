package com.example.librryt19.brodcastreceiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.example.librryt19.qrcode.BluetoothLeService;


public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {

    //List<Body> userInfo;
    @Override
    public void onReceive(Context context, Intent intent) {
        startDataInsert(context,intent);
    }

    private void startDataInsert(final Context context, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, BluetoothLeService.class));
        } else {
            context.startService(new Intent(context, BluetoothLeService.class));
        }
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//
//                        userInfo= WristbandApplication.getAppDb()
//                                .getBodyDAO().getUserInfo();
//
//                } catch (Exception e) {
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void result) {
//                if(userInfo != null
//                        && userInfo.size()>0
//                        && userInfo.get(0) != null) {
//                    Log.i(SensorRestarterBroadcastReceiver.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
//                    //Add by sauraav lall
////                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
////                        context.startForegroundService(new Intent(context, SensorService.class));
////                    } else {
////                        context.startService(new Intent(context, SensorService.class));
////                    }
//                   // context.startService(new Intent(context, SensorService.class));;
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                        context.startForegroundService(new Intent(context, BluetoothLeService.class));
//                    } else {
//                        context.startService(new Intent(context, BluetoothLeService.class));
//                    }
//                }
//            }
//        }.execute();

    }
}
