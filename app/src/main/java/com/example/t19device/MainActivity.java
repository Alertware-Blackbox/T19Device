package com.example.t19device;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleExpandableListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.librryt19.Commons.Commons;
import com.example.librryt19.Conn;
import com.example.librryt19.Model.Attributes;
import com.example.librryt19.Utils.WristbandUtils;
import com.example.librryt19.becon.BeaconService;
import com.example.librryt19.ble.BleWrapperUiCallbacks;
import com.example.librryt19.ble.Bluetooth;
import com.example.librryt19.qrcode.BluetoothLeService;
import com.example.librryt19.qrcode.SampleGattAttributes;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.MonitorNotifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements BleWrapperUiCallbacks, BeaconConsumer {
    Button button,cntct,btn_led,btn_buzz;
    Conn conn;
    TextView dte=null;
    private Bluetooth bluetooth=null;
    Timer timer = new Timer();
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private BluetoothLeService mBluetoothLeService;
    private ExpandableListView mGattServicesList;
    Attributes attributes=new Attributes();
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<>();
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    //Beacon Part...
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;
    //Relative Layout
    RelativeLayout rl;
    //Recycler View
    private RecyclerView rv;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.Adapter adapter;
    //Beacon Manager
    private BeaconManager beaconManager;
    // Progress bar
    private ProgressBar pb;
    BeaconService beaconService;
    //Beacon Part...
    @Override
    public void onRequestPermissionsResult(int requestCode,
          String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_COARSE_LOCATION: {

                // If Permission is Granted than its ok
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }

                // If not Granted then alert the user by the message
                else {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setTitle("Functionality limited");
                    builder.setMessage("Since location access has not been granted, this app will not be able to discover beacons when in the background.");
                    builder.setPositiveButton(android.R.string.ok, null);
                    builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            checkPermission();
                        }
                    });
                    builder.show();
                }
                return;
            }
        }
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle(R.string.alert);
                builder.setMessage("Sure to Disconnect?");
                builder.setCancelable(false);
                builder.setPositiveButton(
                        R.string.yes,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                try {
                                    unbindService(mServiceConnection);
                                    mBluetoothLeService = null;
                                    if(bluetooth != null) {
                                        bluetooth.disconnect();
                                    }
                                    Toast.makeText(getApplicationContext(),"Device is disconnected successfully!",Toast.LENGTH_SHORT).show();
                                }
                                catch(IllegalArgumentException ex){
                                    Toast.makeText(getApplicationContext(),"Device is already disconnected!",Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                builder.setNegativeButton(
                        R.string.no,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGattServicesList=new ExpandableListView(this);
        bluetooth = Bluetooth.getInstance(this,
                this);
        Commons.bluetooth=bluetooth;
        conn=new Conn(this,"BC:33:AC:4A:AC:AF",bluetooth,attributes);
        button=findViewById(R.id.btn);
        cntct=findViewById(R.id.cntct);
        btn_led=findViewById(R.id.btn_led);
        btn_buzz=findViewById(R.id.btn_buzz);
        dte=findViewById(R.id.dte);
        cntct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    unbindService(mServiceConnection);
                }
                catch(IllegalArgumentException ex){ex.printStackTrace();}
                finally{
                    Intent i =new Intent(MainActivity.this,ContactActivity.class);
                    startActivity(i);
                }
            }
        });
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Commons.hasCon){
                    timer = new Timer(); // creating timer
                    TimerTask task = new MyTask(); // creating timer task
                    // scheduling the task for repeated fixed-delay execution, beginning after the specified delay
                    timer.schedule(task, 800, 1000);
                }
                else{
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            WristbandUtils.toast(MainActivity.this,
                                    "Device connection failure!"
                            );
                        }
                    });
                }

            }
        });
        btn_led.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                conn.led_Diago();
            }
        });
        btn_buzz.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                conn.buzzer_Diago();
            }
        });
        rl = findViewById(R.id.Relative_One);

        // Recycler View
        rv = findViewById(R.id.search_recycler);

        //Progress Bar
        pb = findViewById(R.id.pb);
        //For Becon...
        beaconService=new BeaconService(getApplicationContext());
        //getting beaconManager instance (object) for Main Activity class
        beaconManager = BeaconManager.getInstanceForApplication(getApplicationContext());

        // To detect proprietary beacons, you must add a line like below corresponding to your beacon
        // type.  Do a web search for "setBeaconLayout" to get the proper expression.
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25"));

        //Binding MainActivity to the BeaconService.
        beaconManager.bind(this);
        //For Becon...
        checkPermission();
    }
    int i = 1;

    @Override
    public void onBeaconServiceConnect() {


        //Constructing a new Region object to be used for Ranging or Monitoring
        final Region region = new Region("myBeaons",null, null, null);

        //Specifies a class that should be called each time the BeaconService sees or stops seeing a Region of beacons.
        beaconManager.addMonitorNotifier(new MonitorNotifier() {

            /*
                This override method is runned when some beacon will come under the range of device.
            */
            @Override
            public void didEnterRegion(Region region) {
                System.out.println("ENTER ------------------->");
                try {

                    //Tells the BeaconService to start looking for beacons that match the passed Region object
                    // , and providing updates on the estimated mDistance every seconds while beacons in the Region
                    // are visible.
                    beaconManager.startRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

            /*
                 This override method is runned when beacon that comes in the range of device
                 ,now been exited from the range of device.
             */
            @Override
            public void didExitRegion(Region region) {
                System.out.println("EXIT----------------------->");
                try {

                    //Tells the BeaconService to stop looking for beacons
                    // that match the passed Region object and providing mDistance
                    // information for them.
                    beaconManager.stopRangingBeaconsInRegion(region);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }


            /*
                 This override method will Determine the state for the device , whether device is in range
               of beacon or not , if yes then i = 1 and if no then i = 0
            */
            @Override
            public void didDetermineStateForRegion(int state, Region region) {
                System.out.println( "I have just switched from seeing/not seeing beacons: "+state);
            }
        });



        //Specifies a class that should be called each time the BeaconService gets ranging data,
        // which is nominally once per second when beacons are detected.
        beaconManager.addRangeNotifier(new RangeNotifier() {

            /*
               This Override method tells us all the collections of beacons and their details that
               are detected within the range by device
             */
            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                System.out.println( "AAAAAAAA11111: "+beacons);

                // Checking if the Beacon inside the collection (ex. list) is there or not

                // if Beacon is detected then size of collection is > 0
                if (beacons.size() > 0) {
                    try{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                // Make ProgressBar Invisible
                                pb.setVisibility(View.INVISIBLE);

                                // Make Relative Layout to be Gone
                                rl.setVisibility(View.GONE);

                                //Make RecyclerView to be visible
                                rv.setVisibility(View.VISIBLE);

                                // Setting up the layout manager to be linear
                                layoutManager = new LinearLayoutManager(MainActivity.this);
                                rv.setLayoutManager(layoutManager);
                            }
                        });
                    }
                    catch(Exception e){

                    }
                    final ArrayList<ArrayList<String>> arrayList = new ArrayList<>();

                    // Iterating through all Beacons from Collection of Beacons
                    for (Beacon b:beacons){

                        //UUID
                        String uuid = String.valueOf(b.getId1());

                        //Major
                        long major = Long.parseLong(String.valueOf(b.getId2()));

                        long unsignedTemp = Long.parseLong(String.valueOf(major>>8));
                        // long unsignedTemp = Long.parseLong(major);
                        double temperature = unsignedTemp > 128 ?
                                unsignedTemp - 256 :
                                unsignedTemp +(Long.parseLong(String.valueOf(unsignedTemp & 0xff)))/25;
                        //Minor
                        String minor = String.valueOf(b.getId3());

                        //Distance
                        double distance1 =b.getDistance();
                        String distance = String.valueOf(Math.round(distance1*100.0)/100.0);

                        ArrayList<String> arr = new ArrayList<String>();
                        arr.add(uuid);
                        arr.add(String.valueOf(temperature));
                        arr.add(minor);
                        arr.add(distance + " meters");
                        arrayList.add(arr);
                    }
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                // Setting Up the Adapter for Recycler View
                                adapter = new RecyclerAdapter(arrayList);
                                rv.setAdapter(adapter);
                                adapter.notifyDataSetChanged();
                            }
                        });
                    }catch(Exception e){

                    }
                }


                // if Beacon is not detected then size of collection is = 0
                else if (beacons.size()==0) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                // Setting Progress Bar InVisible
                                pb.setVisibility(View.INVISIBLE);

                                // Setting RelativeLayout to be Visible
                                rl.setVisibility(View.VISIBLE);

                                // Setting RecyclerView to be Gone
                                rv.setVisibility(View.GONE);
                            }
                        });
                    } catch (Exception e) {

                    }
                }
            }
        });
        try {
            //Tells the BeaconService to start looking for beacons that match the passed Region object.
            beaconManager.startMonitoringBeaconsInRegion(region);
        } catch (RemoteException e) {
            System.out.println("Error is---> "+e);
        }
    }

    class MyTask extends TimerTask {
        public void run() {
            if (mGattCharacteristics != null) {
                if (mGattCharacteristics.size() > 3) {
                    // for(int i=1;i<4;i++) {
                    if (i == 5) i = 1;
                    BluetoothGattCharacteristic characteristic;
                    if (i == 1) {
                        characteristic =
                                mGattCharacteristics.get(3).get(0);
                    } else {
                        characteristic =
                                mGattCharacteristics.get(6).get(i-1);
                    }
                    Log.e("count", i + "");
                    i = i + 1;
                    final int charaProp = characteristic.getProperties();
                    if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                        // If there is an active notification on a characteristic, clear
                        // it first so it doesn't update the data field on the user interface.
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
                WristbandUtils.toast(MainActivity.this,
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
                WristbandUtils.toast(MainActivity.this,
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
                conn.display(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                System.out.println("Data1 "+ attributes.getDataString());
                dte.setText("Temparature in Centigrade: "+attributes.getTemp_cen()
                +"\n"+"Temparature in Farenhite: "+attributes.getTemp_frn()
                +"\n"+"Battery Status: "+attributes.getBattery()
                +"\n"+"Last Accessed: "+attributes.getDate());
            }
        }
    };
    private void updateConnectionState(final String resourceId) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                WristbandUtils.toast(MainActivity.this,
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
    public void checkPermission(){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                AlertDialog.Builder builder=new AlertDialog.Builder(this);
                builder.setTitle("Location Permission");
                builder.setPositiveButton("OK",null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @TargetApi(Build.VERSION_CODES.M)
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},PERMISSION_REQUEST_COARSE_LOCATION);
                    }
                });
                builder.show();
            }
        }
    }
}