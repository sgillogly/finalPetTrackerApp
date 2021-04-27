package com.example.pettrackerapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.ftdi.j2xx.D2xxManager;
import com.ftdi.j2xx.FT_Device;
import com.google.android.gms.common.api.Api;
import com.google.android.gms.common.api.GoogleApi;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.HashMap;
import java.util.Iterator;

import static com.ftdi.j2xx.D2xxManager.FT_PURGE_RX;
import static java.lang.Thread.sleep;

public class LocationStringActivity extends AppCompatActivity implements OnMapReadyCallback {

    String name;
    String type;
    int id;
    double homeLat, homeLong;
    LatLng homeLocation;

    GoogleMap map;
    Marker petMarker;
    Marker phoneMarker;
    Marker homeMarker;
    LocationListener locationListener;
    LocationManager mLocationManager;

    PendingIntent permissionIntent;
    UsbDevice device;
    FT_Device ftDev = null;
    D2xxManager ftD2xx;
    Context serial_context;
    UsbManager manager;
    UsbDeviceConnection connection;

    Handler locationHandler;
    LocationHandlerThread locationHandlerThread;
    public com.google.android.gms.location.FusedLocationProviderClient fusedLocationClient;

    Toast toast;
    int iavailable = 0;
    byte[] readData;
    char[] readDataToText;
    public static final int readLength = 23;

    private static final String ACTION_USB_PERMISSION = "com.example.prototype1.USB_PERMISSION";
    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {

                            try {
                                ftD2xx = D2xxManager.getInstance(serial_context);
                                ftD2xx.createDeviceInfoList(serial_context);

                                ftDev = ftD2xx.openByIndex(serial_context, 0);

                                toast = Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_LONG);

                                toast.show();

                                ftDev.setBaudRate(9600);
                                ftDev.close();
                                ftD2xx = D2xxManager.getInstance(serial_context);
                                ftD2xx.createDeviceInfoList(serial_context);

                                ftDev = ftD2xx.openByIndex(serial_context, 0);

                                toast = Toast.makeText(getApplicationContext(), "connected", Toast.LENGTH_LONG);

                                toast.show();

                                ftDev.setBaudRate(9600);
                                locationHandlerThread = new LocationHandlerThread("name");
                                locationHandlerThread.start();

                            } catch (D2xxManager.D2xxException e) {
                                e.printStackTrace();
                                toast = Toast.makeText(getApplicationContext(), "cannot connect", Toast.LENGTH_SHORT);
                                toast.show();
                            }
                        }
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        setContentView(R.layout.activity_location_string);
        Intent intent = getIntent();
        id = intent.getIntExtra("_id", 0);
        name = intent.getStringExtra("name");
        type = intent.getStringExtra("type");

        PetDatabaseHelper petDatabaseHelper = new PetDatabaseHelper(getApplicationContext());
        SQLiteDatabase sqLiteDatabase = petDatabaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteDatabase.query("pets", new String[]{"_id", "name", "type", "drawable", "homeLat", "homeLong", "petLat", "petLong"}, null, null, null, null, null);
        int position = intent.getIntExtra("pos", 0);
        cursor.moveToPosition(position);
        int column = cursor.getColumnIndex("homeLat");
        homeLat = Double.parseDouble(cursor.getString(column));
        column = cursor.getColumnIndex("homeLong");
        homeLong = Double.parseDouble(cursor.getString(column));
        if(homeLat != 0.0){
            homeLocation = new LatLng(homeLat, homeLong);
        } else{
            homeLocation = null;
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                LatLng phoneLocation = new LatLng(location.getLatitude(), location.getLongitude());
                phoneMarker.setVisible(true);
                phoneMarker.setPosition(phoneLocation);
            }
        };

        serial_context = getApplicationContext();

        manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);

        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            device = deviceIterator.next();

        }
        if (device != null) {
            manager.requestPermission(device, permissionIntent);

            connection = manager.openDevice(device);
        }

        locationHandler = new Handler() {
            @Override
            public void handleMessage(@NonNull Message msg) {
                Bundle bundle = msg.getData();
                Double latitude = bundle.getDouble("lat");
                Double longitude = bundle.getDouble("long");
                LatLng location = new LatLng(latitude, longitude);
                if (latitude != 10000 && longitude != 10000) {
                    petMarker.setVisible(true);
                    petMarker.setPosition(location);
                }
            }
        };

        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, locationListener);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng gunnison = new LatLng(38.5, -106.5);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(gunnison, 1));
        int drawable = 0;
        if(type.equals("cat") || type.equals("CAT") || type.equals("Cat")){
            drawable = R.drawable.cat;
        } else if(type.equals("dog") || type.equals("DOG") || type.equals("Dog")){
            drawable = R.drawable.dog;
        } else{
            drawable = R.drawable.pet;
        }

        petMarker = map.addMarker(new MarkerOptions().position(new LatLng(13,0)).title("pet")
                .icon(BitmapDescriptorFactory.fromResource(drawable)));
        phoneMarker = map.addMarker(new MarkerOptions().position(gunnison).title("phone")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.phone)));
        if(homeLocation != null){
            homeMarker = map.addMarker(new MarkerOptions().position(homeLocation).title("home")
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.home)));
        }
        phoneMarker.setVisible(false);
        petMarker.setVisible(false);
    }

    public class LocationHandlerThread extends HandlerThread {

        public LocationHandlerThread(String name) {
            super(name);
        }

        @Override
        public void run() {
            while (true) {
                iavailable = ftDev.getQueueStatus();
                if (iavailable > 0) {
                    if (iavailable > readLength) {
                        iavailable = readLength;
                    }
                }
                readData = new byte[readLength];
                readDataToText = new char[readLength];
                ftDev.purge(FT_PURGE_RX);
                ftDev.read(readData, iavailable);
                for (int i = 0; i < iavailable; i++) {
                    readDataToText[i] = (char) readData[i];
                }
                String locationString = new String(readDataToText);

                String[] petLocationArray = locationString.split("/");
                double petLat = 10000;
                double petLong = 10000;
                if (petLocationArray.length != 2) {
                    petLat = 10000;
                    petLong = 10000;
                }
                else if(petLocationArray.length == 2) {
                    try {
                        petLat = Double.parseDouble(petLocationArray[0]);
                        petLong = Double.parseDouble(petLocationArray[1]);
                    }
                    catch(NumberFormatException e){
                        petLat = 10000;
                        petLong = 10000;
                    }
                }
                if(petLat < -90 || petLat > 90 || petLong <-180 || petLong > 180){
                    petLat = 10000;
                    petLong = 10000;
                }
                Message message = new Message();
                Bundle bundle = new Bundle();
                bundle.putDouble("lat", petLat);
                bundle.putDouble("long", petLong);
                message.setData(bundle);
                locationHandler.sendMessage(message);
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage(this.getPackageName());
        ComponentName componentName = intent.getComponent();
        Intent mainIntent = Intent.makeRestartActivityTask(componentName);
        this.startActivity(mainIntent);
        Runtime.getRuntime().exit(0);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        locationHandlerThread.quit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_layout, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menuAddPet:
                showAddDialog();
                return true;
            case R.id.menuHome:
                PackageManager packageManager = this.getPackageManager();
                Intent intent = packageManager.getLaunchIntentForPackage(this.getPackageName());
                ComponentName componentName = intent.getComponent();
                Intent mainIntent = Intent.makeRestartActivityTask(componentName);
                this.startActivity(mainIntent);
                Runtime.getRuntime().exit(0);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddDialog(){
        FragmentManager fm = getSupportFragmentManager();
        AddPetDialogFragment addPetDialogFragment = AddPetDialogFragment.newInstance("Add New Pet");
        addPetDialogFragment.show(fm, "fragment_add_pet");
    }
}