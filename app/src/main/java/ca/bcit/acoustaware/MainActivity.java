package ca.bcit.acoustaware;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;

import android.widget.CompoundButton;
import android.widget.SeekBar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;


public class MainActivity extends AppCompatActivity implements OnMapReadyCallback{
    private static final String TAG="MainActivity";
    private final int radiusInMeter = 50;
    private final int scalar = 25;
    private GoogleMap mMap;
    private LocationManager locationManager;
    private DrawerLayout mDrawerLayout;
    private Vibrator vibrator;
    private int defaultVolume;
    private int changedVolume=0;
    private boolean isVibrateOn=false;
    private boolean isAlertMsgOn =false;
    private int minCrashNum=10;
    private Location currentLocation;
    private boolean isInCrashSite= false;
    private AudioManager audioManager;
    private CrashSite nearbyCrashSite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        CrashSite.setContext(this);
        CrashSite.init();

        setContentView(R.layout.activity_main);

        //test bg thread

//        class bgThread extends Thread{
//            @Override
//            public void run() {
//                for (int i =0;i<=20;i++){
//                    Log.d(TAG, "run: "+i);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//
//        bgThread bgThread1 = new bgThread();
//        bgThread1.run();


//        for (int i =0;i<=20;i++){
//            Log.d(TAG, "run: "+i);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }}




        //menu
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);

        // drawer
        mDrawerLayout = findViewById(R.id.drawer_layout);

        SwitchCompat vibrateSwitch = (SwitchCompat) findViewById(R.id.vibrate_setting);
        SwitchCompat alertMsgSwitch = (SwitchCompat) findViewById(R.id.alert_msg);
        vibrateSwitch.setOnCheckedChangeListener(onCheckedChanged());
        alertMsgSwitch.setOnCheckedChangeListener(onCheckedChanged());

        locationManager = (LocationManager)
                getSystemService(Context.LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
        }
//        LocationListener locationListener = new MyLocationListener();
//        locationManager.requestLocationUpdates(
//                LocationManager.GPS_PROVIDER, 5000, 10, locationListener);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Setting seekbar to set the minimum number of crashes to show markers
        SeekBar crashNumBar = findViewById(R.id.crash_num_seek_bar);
        crashNumBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int crashNum, boolean b) {
                minCrashNum = crashNum;
                mMap.clear();
                addMarkers();
                Log.i("CrashChanged: ", ""+crashNum);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });


        // AudioManager for volume settings that
       audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
       defaultVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        SeekBar volumeBar = findViewById(R.id.seekbar);
        volumeBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int newVolume, boolean b) {
                changedVolume = newVolume;

                Log.i("VolumeChange: ", ""+newVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        //Vibrate settings
//        check ringer_mode,
//                if mode_normal, can vibrate and play music
//                if mode_vibrate , can vibrate and not play music
//                if mode_silent , can't vibrate or play'
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

//        FLAG_VIBRATE // for vibrate
//        MODE_CURRENT // current audio mode
        //getRingerMode(), setRingerMode() RINGER_MODE_VIBRATE, RINGER_MODE_NORMAL for vibrate
        // playSoundEffect() for push notification
        // isMusicActive () // checks whether any music is active
        // isBluetoothScoOn() //check whether communications use Bluetooth SCO
        // getStreamVolumne, getStreamMaxVolume, setStreamVolume, getStreamMinVolume
        // adjustVolume
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            return super.onKeyDown(keyCode, event);
//        }
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if(defaultVolume >0) {
                    defaultVolume--;
                }
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume, 0);
                defaultVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                return true;
            case KeyEvent.KEYCODE_VOLUME_UP:
                if(defaultVolume < audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)) {
                    defaultVolume++;
                }
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume, 0);
                defaultVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * @return the last know best location
     */
    private Location getLastBestLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
        }
        Location locationGPS = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Location locationNet = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        long GPSLocationTime = 0;
        if (null != locationGPS) {
            GPSLocationTime = locationGPS.getTime();
        }

        long NetLocationTime = 0;

        if (null != locationNet) {
            NetLocationTime = locationNet.getTime();
        }

        if (0 < GPSLocationTime - NetLocationTime) {
            return locationGPS;
        } else {
            return locationNet;
        }
    }

    private class MyLocationListener implements LocationListener {

        @Override
        public void onLocationChanged(Location loc) {
        }

        @Override
        public void onProviderDisabled(String provider) {
        }

        @Override
        public void onProviderEnabled(String provider) {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION},177);
        }
        mMap.setMyLocationEnabled(true);
        addMarkers();

        //TODO:We should update this marker to be the current location; currently hardcoded to new west
        LatLng marker = new LatLng(49.2057179, -122.910956);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        moveToCurrentLocation(marker);

        //Draw circle for current location
        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            Circle circle = mMap.addCircle(new CircleOptions()
                    .center(new LatLng(0, 0))
                    .radius(radiusInMeter/scalar)
                    .strokeColor(Color.argb(50,0,0,255))
                    .fillColor(Color.argb(50,0,0,255)));
            @Override
            public void onMyLocationChange(Location location) {
                currentLocation = location;
                circle.setCenter(new LatLng(location.getLatitude(),location.getLongitude()));
                Log.d(TAG, "onMyLocationChange: "+location.getLatitude()+ " " + location.getLongitude());
                locationChecker();
            }
        });
//        circleAroundUserLocationThread();
    }

//    public void circleAroundUserLocationThread(){
//        CircleLocationThread circleLocationThread = new CircleLocationThread();
//        circleLocationThread.start();
//    }
//
//    class CircleLocationThread extends Thread{
//        @Override
//        public void run() {
//            for(int i =0;i<10;i++){
//                try {
//                    Log.d(TAG, "run: "+i);
//                    Thread.sleep(1000);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//
//            }
//        }
//    }

    public void addMarkers(){
        for(int i=0;i<CrashSite.crashSites.size();i++){
            if(CrashSite.crashSites.get(i).getCrashCount()>minCrashNum)
                mMap.addCircle(new CircleOptions()
                    .center(new LatLng(CrashSite.crashSites.get(i).getLatitude(),
                            CrashSite.crashSites.get(i).getLongtitude()))
                    .radius(radiusInMeter*CrashSite.crashSites.get(i).getCrashCount()/scalar)
                    .strokeColor(Color.argb(50,255,0,0))
                    .fillColor(Color.argb(50,255,0,0)));
            CrashSite.crashSites.get(i).setRadius(radiusInMeter*CrashSite.crashSites.get(i).getCrashCount()/scalar);
        }
    }



    private void moveToCurrentLocation(LatLng currentLocation)
    {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation,15));
        // Zoom in, animating the camera.
        mMap.animateCamera(CameraUpdateFactory.zoomIn());
        // Zoom out to zoom level 10, animating with a duration of 2 seconds.
        mMap.animateCamera(CameraUpdateFactory.zoomTo(15), 2000, null);
    }

    private void vibrateOn() {
        // first value = wait before turning vibrator ON
        // Next value = milliseconds to keep the vibrator on before turning off,
        // subsequent values alternate On and Off
        long pattern[] = {0, 100, 3000};

        vibrator.vibrate(pattern, -1); // pass -1 for repeat to turn off repeats, or the index of pattern to indicate starting position
    }

    private void stopVibrate() {
        vibrator.cancel();
    }


    private CompoundButton.OnCheckedChangeListener onCheckedChanged() {
        return new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                switch(buttonView.getId()) {
                    case R.id.vibrate_setting: {
                        isVibrateOn= !isVibrateOn;
                        break;
                    }
                    case R.id.alert_msg: {
                        isAlertMsgOn = !isAlertMsgOn;
                        break;
                    }
                }
            }
        };
    }

    private void locationChecker() {

        if (isInCrashSite) {
            if(!checkIfNearCrashSite()) {
                checkIfAwayFromCrashSite();
            }
        } else {
            if(checkIfNearCrashSite()) {
                triggerNearCrashSite();
            }
        }
    }

    private void checkIfAwayFromCrashSite() {
        float[] results = new float[1];
        Location.distanceBetween(currentLocation.getLatitude(),
                currentLocation.getLongitude(),
                nearbyCrashSite.getLatitude(),
                nearbyCrashSite.getLongtitude(),
                results);
        if(results[0] > nearbyCrashSite.getRadius()){
            triggerAwayFromCrashSite();
        }
    }

    private boolean checkIfNearCrashSite() {
        if(currentLocation !=null) {
            for (int i = 0; i < CrashSite.crashSites.size(); i++) {

                float[] results = new float[1];
                Location.distanceBetween(currentLocation.getLatitude(),
                        currentLocation.getLongitude(),
                        CrashSite.crashSites.get(i).getLatitude(),
                        CrashSite.crashSites.get(i).getLongtitude(),
                        results);
                if (CrashSite.crashSites.get(i).getRadius() > 0
                        && results[0] < CrashSite.crashSites.get(i).getRadius() && CrashSite.crashSites.get(i).getCrashCount() >minCrashNum) {
                    nearbyCrashSite = CrashSite.crashSites.get(i);
                    return true;
                }
            }
        }
        return false;
    }
    private void triggerNearCrashSite() {
        isInCrashSite = true;
        if(isVibrateOn){
            vibrateOn();
        }
        if(isAlertMsgOn) {
            sendAlertMsg();
        }
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, changedVolume, 0);
    }

    private void triggerAwayFromCrashSite() {
        isInCrashSite = false;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume, 0);
    }

    private void sendAlertMsg() {
        // Alert notification manager
        NotificationManager alertMsg = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        String title="Be careful! You're near a crash site";
        Notification notify = new Notification.Builder
                (getApplicationContext()).setSmallIcon(R.drawable.ic_stat_name).setContentTitle(title).setContentText("Be aware of your surroundings")
                .setContentTitle("You're near a high crash area!").build();
        notify.flags |= Notification.FLAG_AUTO_CANCEL;
        alertMsg.notify(0, notify);
    }
    @Override
    public void onResume(){
        super.onResume();
        defaultVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    @Override
    public void onStop(){
        super.onStop();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume, 0);
        isInCrashSite=false;
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, defaultVolume, 0);
        isInCrashSite=false;
    }
}
