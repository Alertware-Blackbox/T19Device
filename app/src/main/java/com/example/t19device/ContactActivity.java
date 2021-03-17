package com.example.t19device;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.librryt19.Commons.Commons;
import com.example.librryt19.Conn;
import com.example.librryt19.Model.Attributes;
import com.example.librryt19.Utils.WristbandUtils;
import com.example.librryt19.ble.BleWrapperUiCallbacks;
import com.example.librryt19.ble.Bluetooth;
import com.example.librryt19.qrcode.BluetoothLeService;
import com.example.librryt19.qrcode.SampleGattAttributes;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ContactActivity extends AppCompatActivity implements BleWrapperUiCallbacks {
    Conn conn;
    private Bluetooth bluetooth=null;
    LinearLayout llDynamicContent;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    Button cntct=null;
    int j=0;
    private BluetoothLeService mBluetoothLeService;
    private ExpandableListView mGattServicesList;
    Attributes attributes=new Attributes();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<>();
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cntct);
        cntct=findViewById(R.id.cntct);
        mGattServicesList=new ExpandableListView(this);
        llDynamicContent = (LinearLayout) findViewById(R.id.llDynamicContent);
        bluetooth = Bluetooth.getInstance(this,
                this);
        conn=new Conn(this,"BC:33:AC:4A:AC:AF",bluetooth,attributes);
        Timer timer = new Timer(); // creating timer
        TimerTask task = new MyTask(); // creating timer task
        // scheduling the task for repeated fixed-delay execution, beginning after the specified delay
        timer.schedule(task, 800, 1000);
    }
    int i = 4;
    class MyTask extends TimerTask {
        public void run() {
            if (mGattCharacteristics != null) {
                if (mGattCharacteristics.size() > 3) {
                    if (i == 78) i = 4;
                    BluetoothGattCharacteristic characteristic;
                    characteristic =
                            mGattCharacteristics.get(6).get(i);
                    i = i + 1;
                    final int charaProp = characteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        if (mNotifyCharacteristic != null
                                && mBluetoothLeService != null) {
                            mBluetoothLeService.setCharacteristicNotification(
                                    mNotifyCharacteristic, false);
                            mNotifyCharacteristic = null;
                        }
                        if (mBluetoothLeService != null) {
                            mBluetoothLeService.readCharacteristic(characteristic);
                        }
                    }
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                        mNotifyCharacteristic = characteristic;
                        if (mBluetoothLeService != null) {
                            mBluetoothLeService.setCharacteristicNotification(
                                    characteristic, true);
                        }
                    }
                }
            }
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(Commons.mDeviceAddress);
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
        if(bluetooth != null) {
            bluetooth.disconnect();
        }
    }
    //**************************
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(Commons.mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    @Override
    public void uiDeviceFound(BluetoothDevice device, int rssi, byte[] record) {

    }

    @Override
    public void uiDeviceConnected(BluetoothGatt gatt, BluetoothDevice device) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WristbandUtils.toast(ContactActivity.this,
                        "Device connected!"
                );
            }
        });
    }

    @Override
    public void uiDeviceDisconnected(BluetoothGatt gatt, BluetoothDevice device) {

    }

    @Override
    public void uiAvailableServices(BluetoothGatt gatt, BluetoothDevice device, List<BluetoothGattService> services) {

    }

    @Override
    public void uiCharacteristicForService(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, List<BluetoothGattCharacteristic> chars) {

    }

    @Override
    public void uiCharacteristicsDetails(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void uiNewValueForCharacteristic(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String strValue, int intValue, byte[] rawValue, String timestamp) {

    }

    @Override
    public void uiGotNotification(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic characteristic) {

    }

    @Override
    public void uiSuccessfulWrite(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String description) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WristbandUtils.toast(ContactActivity.this,
                        "Device connected successfuly!"
                );
            }
        });
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    public void uiFailedWrite(BluetoothGatt gatt, BluetoothDevice device, BluetoothGattService service, BluetoothGattCharacteristic ch, String description) {

    }

    @Override
    public void uiNewRssiAvailable(BluetoothGatt gatt, BluetoothDevice device, int rssi) {

    }

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                updateConnectionState(getString(R.string.con));
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                updateConnectionState(getString(R.string.disconnected));
                Commons.hasCon=false;
                invalidateOptionsMenu();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                conn.display_cntct(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                System.out.println("XXXXXXX " + attributes.getAlrt_wr_mac_id()+" --->> "+attributes.getCntct_dt());
                if (!attributes.getAlrt_wr_mac_id().matches("") && attributes.getAlrt_wr_mac_id().length() > 4) {
                    loadLayout(attributes.getAlrt_wr_mac_id(), attributes.getCntct_dt());
                }
            }
        }
    };

    private void loadLayout(String mac_id,String dte) {
            LinearLayout layout1 = new LinearLayout(this);
            layout1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layout1.setOrientation(LinearLayout.VERTICAL);

            TextView tv4 = new TextView(this);
            tv4.setText("-----------------------------------------------------------");
            tv4.setTextColor(getResources().getColor(R.color.colorPrimary));
            tv4.setTypeface(null, Typeface.BOLD);

            LinearLayout.LayoutParams params4 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            tv4.setLayoutParams(params4);

            layout1.addView(tv4);//Add parent view

            LinearLayout layout2 = new LinearLayout(this);

            layout2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            layout2.setOrientation(LinearLayout.HORIZONTAL);
            layout2.setWeightSum(2);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            params.weight = 1.0f;
            //tv1.setLayoutParams(params);

            TextView tv2 = new TextView(this);
            tv2.setTextColor(getResources().getColor(R.color.black));

            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            //tv1.getLayoutParams();
            params1.weight = 1.0f;
            tv2.setLayoutParams(params1);

            DecimalFormat df = new DecimalFormat("#.00");
            df.format(0.912385);

            TextView tv3 = new TextView(this);
            tv3.setText(dte);
            tv3.setTextColor(getResources().getColor(R.color.black));

            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            tv3.getLayoutParams();
            params2.weight = 1.0f;
            tv3.setLayoutParams(params2);

            tv2.setText(WristbandUtils.makeMacAddress(mac_id.toUpperCase()));

            layout2.addView(tv3);
            //layout2.addView(tv1);
            layout2.addView(tv2);

            layout1.addView(layout2);//Add parent view

            llDynamicContent.addView(layout1);


    }

    private void updateConnectionState(final String resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WristbandUtils.toast(ContactActivity.this,
                        resourceId
                );
            }
        });
    }
    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        SimpleExpandableListAdapter gattServiceAdapter = new SimpleExpandableListAdapter(
                this,
                gattServiceData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2},
                gattCharacteristicData,
                android.R.layout.simple_expandable_list_item_2,
                new String[]{LIST_NAME, LIST_UUID},
                new int[]{android.R.id.text1, android.R.id.text2}
        );
        mGattServicesList.setAdapter(gattServiceAdapter);
    }
    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}