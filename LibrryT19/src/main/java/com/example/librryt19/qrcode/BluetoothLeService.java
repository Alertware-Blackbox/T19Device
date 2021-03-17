/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.librryt19.qrcode;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.example.librryt19.R;

import org.json.JSONObject;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    private final int HIDE_MSB_8BITS_OUT_OF_32BITS = 0x00FFFFFF;
    private final int HIDE_MSB_8BITS_OUT_OF_16BITS = 0x00FF;
    private final int SHIFT_LEFT_8BITS = 8;
    private final int SHIFT_LEFT_16BITS = 16;
    private final int GET_BIT24 = 0x00400000;
    private static final int FIRST_BIT_MASK = 0x01;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    public final static UUID UUID_SERVICE_ENVIRONMENT =
            UUID.fromString(SampleGattAttributes.UUID_SERVICE_ENVIRONMENT_SENSING);

    ///////////////////////
    public int counter = 0;
  //  private Temperature temperatureObject;
    private JSONObject jsonObject;
    private Activity mActivity;
   // List<Body> userInfo;
    //////////////////////

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                Log.i(TAG, "Connected to GATT server123.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else if("00002a19-0000-1000-8000-00805f9b34fb".equals(characteristic.getUuid().toString())) {
        //} else if("00002a05-0000-1000-8000-00805f9b34fb".equals(characteristic.getUuid().toString())) {

            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }else if (!SampleGattAttributes.UUID_SERVICE_ENVIRONMENT_SENSING.equals(characteristic.getUuid().toString())) {
            intent.putExtra(EXTRA_DATA, new String(characteristic.getStringValue(0)));
        }else if (!SampleGattAttributes.UUID_SERVICE_ENVIRONMENT_SENSING.equals(characteristic.getUuid().toString())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_SINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_SINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 0);
            try {
                decodeTemperature(characteristic.getValue());
            }catch (Exception e){

            }
            updateHumidityValues(characteristic);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        }  else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
            }
        }
        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        public BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    private void updateHumidityValues(BluetoothGattCharacteristic characteristic) {
        int lsb = characteristic.getValue()[0] & 0xff;
    }
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     * @return Return true if the connection is initiated successfully. The connection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled        If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
//        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
//            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
//                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
//
//        if (UUID_SERVICE_ENVIRONMENT.equals(characteristic.getUuid())) {
//            BluetoothGattService service = mBluetoothGatt.getService(fromString("f598dbc5-2f00-4ec5-9936-b3d1aa4f957f"));
//            if (service == null) {
//            }
//            BluetoothGattCharacteristic characteristic1 = service.getCharacteristic(
//                    fromString("f598dbc5-2f01-4ec5-9936-b3d1aa4f957f"));
//            BluetoothGattDescriptor descriptor = characteristic1.getDescriptor(
//                   UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            mBluetoothGatt.writeDescriptor(descriptor);
//        }
//
//        try {
//            setCharacteristicNotification(mBluetoothGatt,
//                    ThunderBoardUuids.UUID_SERVICE_AUTOMATION_IO,
//                    ThunderBoardUuids.UUID_CHARACTERISTIC_DIGITAL,
//                    ThunderBoardUuids
//                            .UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION,
//                    true);
//        }catch (Exception e){
//
//        }
        /////*****************************
//        try {
//        setCharacteristicNotification(
//                mBluetoothGatt,
//                ThunderBoardUuids.UUID_SERVICE_ACCELERATION_ORIENTATION,
//                ThunderBoardUuids.UUID_CHARACTERISTIC_ORIENTATION,
//                ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, true);
//    }catch (Exception e){
//
//    }
//        try {
//        setCharacteristicNotification(
//                mBluetoothGatt,
//                ThunderBoardUuids.UUID_SERVICE_CSC,
//                ThunderBoardUuids.UUID_CHARACTERISTIC_CSC_MEASUREMENT,
//                ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, true);
//}catch (Exception e){
//
//        }
//        try {
//        setCharacteristicNotification(
//                mBluetoothGatt,
//                ThunderBoardUuids.UUID_SERVICE_HALL_EFFECT,
//                ThunderBoardUuids.UUID_CHARACTERISTIC_HALL_STATE,
//                ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, true);
//        }catch (Exception e){
//
//        }
        /////*****************************
//        try {
//        setCharacteristicNotification(mBluetoothGatt,
//                ThunderBoardUuids.UUID_SERVICE_BATTERY,
//                ThunderBoardUuids.UUID_CHARACTERISTIC_BATTERY_LEVEL,
//                ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, true);
//        }catch (Exception e){
//
//        }
//        try {
//        setCharacteristicNotification(mBluetoothGatt,
//                ThunderBoardUuids.UUID_SERVICE_POWER_MANAGEMENT,
//                ThunderBoardUuids.UUID_CHARACTERISTIC_POWER_SOURCE,
//                ThunderBoardUuids.UUID_DESCRIPTOR_CLIENT_CHARACTERISTIC_CONFIGURATION, true);
//        }catch (Exception e){
//
//        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    public static boolean setCharacteristicNotification(BluetoothGatt gatt, UUID serviceUuid, UUID characteristicUuid, UUID descriptorUuid, boolean enable) {
        if (gatt == null) {
            return false;
        }
        BluetoothGattService service = gatt.getService(serviceUuid);
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(characteristicUuid);

        if (characteristic == null) {
            //System.out.println("could not get characteristic: %s for service: %s", characteristicUuid.toString(), serviceUuid.toString());
            return false;
        }

        if (!gatt.setCharacteristicNotification(characteristic, true)) {
           // Timber.d("was not able to setCharacteristicNotification");
            return false;
        }

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(descriptorUuid);
        if (descriptor == null) {
           // Timber.d("was not able to getDescriptor");
            return false;
        }

        if (enable) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        } else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        return gatt.writeDescriptor(descriptor);
    }

    private double decodeTemperature(byte[] data) throws Exception {
        double temperatureValue = 0.0;
        byte flag = data[0];
        byte exponential = data[4];
        short firstOctet = convertNegativeByteToPositiveShort(data[1]);
        short secondOctet = convertNegativeByteToPositiveShort(data[2]);
        short thirdOctet = convertNegativeByteToPositiveShort(data[3]);
        int mantissa = ((thirdOctet << SHIFT_LEFT_16BITS) | (secondOctet << SHIFT_LEFT_8BITS) | (firstOctet)) & HIDE_MSB_8BITS_OUT_OF_32BITS;
        mantissa = getTwosComplimentOfNegativeMantissa(mantissa);
        temperatureValue = (mantissa * Math.pow(10, exponential));
        /*
         * Conversion of temperature unit from Fahrenheit to Celsius if unit is in Fahrenheit
         * Celsius = (98.6*Fahrenheit -32) 5/9
         */
        if ((flag & FIRST_BIT_MASK) != 0) {
            temperatureValue = (float) ((98.6 * temperatureValue - 32) * (5 / 9.0));
        }
        return temperatureValue;
    }

    private short convertNegativeByteToPositiveShort(byte octet) {
        if (octet < 0) {
            return (short) (octet & HIDE_MSB_8BITS_OUT_OF_16BITS);
        } else {
            return octet;
        }
    }

    private int getTwosComplimentOfNegativeMantissa(int mantissa) {
        if ((mantissa & GET_BIT24) != 0) {
            return ((((~mantissa) & HIDE_MSB_8BITS_OUT_OF_32BITS) + 1) * (-1));
        } else {
            return mantissa;
        }
    }

    ///////////////////////////////////
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_STICKY;
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime = 0;

    public void startTimer() {
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        //timer.schedule(timerTask, 10000, 1000*60*30); //
        timer.schedule(timerTask, 10000, 10000); //

    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "ble in timer ++++  " + (counter++));
                getSupportedGattServices();
                //startDataInsert();
            }
        };
    }

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

//    private void startDataInsert() {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//
//                    userInfo = WristbandApplication.getAppDb()
//                            .getBodyDAO().getUserInfo();
//
//                } catch (Exception e) {
//                }
//
//                temperatureObject = new Temperature();
//                try {
//                    jsonObject = new JSONObject();
//                    String temp = "99.45";
//                    if (userInfo != null
//                            && userInfo.size() > 0
//                            && userInfo.get(0) != null) {
//                        try {
//                            if (Integer.parseInt(WristbandUtils.getTimeStamp().substring(WristbandUtils.getTimeStamp().length() - 3)) % 3 == 0) {
//                                jsonObject.put("temperature", "98.56");
//                                temp = "98.56";
//                            } else if (Integer.parseInt(WristbandUtils.getTimeStamp().substring(WristbandUtils.getTimeStamp().length() - 3)) % 3 == 1) {
//                                jsonObject.put("temperature", "99.45");
//                                temp = "99.45";
//                            } else {
//                                jsonObject.put("temperature", "100.67");
//                                temp = "100.67";
//                            }
//                        } catch (Exception e) {
//                            jsonObject.put("temperature", "98.27");
//                            temp = "98.27";
//                        }
//                        //getSupportedGattServices();
//                        temperatureObject.setTemperature(temp);
//                        if (userInfo != null
//                                && userInfo.get(0) != null
//                                && userInfo.get(0).getUserId() != null) {
//                            jsonObject.put("userid", userInfo.get(0).getUserId());
//                            temperatureObject.setUserid(userInfo.get(0).getUserId());
//                        } else {
//                            jsonObject.put("userid", "0");
//                            temperatureObject.setUserid(0);
//                        }
//
//                        jsonObject.put("resulttime", WristbandUtils.getDateTime());
//                        temperatureObject.setResulttime(WristbandUtils.getDateTime());
//                        temperatureObject.setInserted_date(new Date());
//
//                        if (userInfo != null
//                                && userInfo.get(0) != null
//                                && userInfo.get(0).getOrganizationId() != null) {
//                            jsonObject.put("organization_id", userInfo.get(0).getOrganizationId());
//                            temperatureObject.setOrganization_id(userInfo.get(0).getOrganizationId());
//                        } else {
//                            jsonObject.put("organization_id", 0);
//                            temperatureObject.setOrganization_id(0);
//                        }
//                        GetDataService service = RetrofitClientInstance.getRetrofitInstanceForMasterData().create(GetDataService.class);
//                        RequestBody temperature = RequestBody.create(MediaType.parse("multipart/form-data"),
//                                temp);
//                        RequestBody userid = RequestBody.create(MediaType.parse("multipart/form-data"),
//                                String.valueOf(userInfo.get(0).getUserId()));
//                        RequestBody resulttime = RequestBody.create(MediaType.parse("multipart/form-data"),
//                                WristbandUtils.getDateTime());
//                        RequestBody organization_id = RequestBody.create(MediaType.parse("multipart/form-data"),
//                                String.valueOf(userInfo.get(0).getOrganizationId()));
//
//                        Call<ResponseBody> call = service.sendDataTemp(temperature, userid, resulttime, organization_id);
//                        call.enqueue(new Callback<ResponseBody>() {
//                            @Override
//                            public void onResponse(Call<ResponseBody> call,
//                                                   final Response<ResponseBody> response) {
//                                if (response != null && response.body() != null) {
//                                    saveLoginInfo(temperatureObject);
//                                }
//                            }
//
//
//                            @Override
//                            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                            }
//
//                        });
//                        //saveLoginInfo(temperature);
//                    } else {
//                        stopSelf();
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void result) {
//
//            }
//        }.execute();
//
//    }
//    private void saveLoginInfo(final Temperature temperature) {
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//                    if (temperature != null) {
//                        WristbandApplication.getAppDb()
//                                .getTemperatureDAO().insert(temperature);
//                    }
//                } catch (Exception e) {
//                }
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void result) {
////                if (temperature == null) {
////                    OrsacUtils.toast(mActivity, "Problem occurred!");
////                }
//
//            }
//        }.execute();
//    }


    @Override
    public void onCreate() {
        super.onCreate();
        startServiceOreoCondition();
    }

    private void startServiceOreoCondition() {
        if (Build.VERSION.SDK_INT >= 26) {
            String CHANNEL_ID = "my_service";
            String CHANNEL_NAME = "My Background Service";

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE);
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).createNotificationChannel(channel);

            Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setCategory(Notification.CATEGORY_SERVICE).setSmallIcon(R.drawable.logo).setPriority(PRIORITY_MIN).build();

            startForeground(101, notification);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        //Intent broadcastIntent = new Intent(this, SensorRestarterBroadcastReceiver.class);
        //sendBroadcast(broadcastIntent);
        stoptimertask();
    }
}
