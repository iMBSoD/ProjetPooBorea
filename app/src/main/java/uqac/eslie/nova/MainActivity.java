package uqac.eslie.nova;


import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.Manifest;

import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;



import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import com.orm.SugarContext;

import java.net.MalformedURLException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import uqac.eslie.nova.BDD.CarPooling;
import uqac.eslie.nova.BDD.DataBaseHelper;
import uqac.eslie.nova.BDD.Marker;
import uqac.eslie.nova.Dialog.MarkerDialog;
import uqac.eslie.nova.Fragments.AccountFragment;
import uqac.eslie.nova.Fragments.CarFragment;
import uqac.eslie.nova.Fragments.CarPoolingDetailFragment;
import uqac.eslie.nova.Fragments.ChartFragment;
import uqac.eslie.nova.Fragments.HomeFragment;

import uqac.eslie.nova.Fragments.ImageFragment;
import uqac.eslie.nova.Fragments.MapFragment;
import uqac.eslie.nova.Fragments.WeatherFragment;
import uqac.eslie.nova.Helper.GPSTracker;
import uqac.eslie.nova.Helper.Helper_NavigationBottomBar;

public class MainActivity extends AppCompatActivity
    implements    HomeFragment.clickAddCarpooling,
        CarFragment.CarFragmentListener,
        HomeFragment.clickFindCarpooling,
        CarFragment.CarFragmentListenerFloatingButton,
        MarkerDialog.MarkerListener

{

    Fragment fragment = null;
    Fragment home;
    Fragment weather;
    Fragment car;
    Fragment map;
    Fragment account;
    Fragment chart;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();

            switch (item.getItemId()) {
                case R.id.navigation_home:
                    transaction.replace(R.id.content, home).commit();
                    transaction.addToBackStack("home");
                    return true;
                case R.id.navigation_weather:
                    transaction.replace(R.id.content, weather).commit();
                    transaction.addToBackStack("weather");
                   // transaction.replace(R.id.content, new WeatherFragment()).commit();
                    return true;
                case R.id.navigation_find_a_car:
                    transaction.replace(R.id.content, new CarFragment()).commit();
                    transaction.addToBackStack("car");
                    return true;
                case R.id.navigation_place:
                    transaction.replace(R.id.content, map).commit();
                    transaction.addToBackStack("map");
                    return true;
                case R.id.navigation_account:
                    transaction.replace(R.id.content, account).commit();
                    transaction.addToBackStack("account");
                    return true;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment, "TAG").commit();
            return true;
        }

    };

    public void switchFragment(Fragment f){
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.content, f).commit();
        transaction.addToBackStack("");

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        home = new HomeFragment();
        try {
            weather= new WeatherFragment();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        car= new CarFragment();
        map = new MapFragment();
        account = new AccountFragment();
        chart = new ChartFragment();
        transaction.replace(R.id.content, new HomeFragment()).commit();
        transaction.disallowAddToBackStack();
        Helper_NavigationBottomBar helper = new Helper_NavigationBottomBar();


        helper.disableShiftMode(navigation);
        calculateHashKey("uqac.eslie.nova");


        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED)
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.INTERNET}, 1);

        GPSTracker gps = new GPSTracker(this);
        if(!gps.isGPSEnabled)
        {
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));

        }
        SugarContext.terminate();

        SugarContext.init(getApplicationContext());

    }


    @Override
    protected void onPause(){
        super.onPause();
        //Sauvegarder les données

    }
    @Override
    public void onCarPoolingClick() {
        startActivity(new Intent(MainActivity.this, addCarPooling.class));
    }
    @Override
    public void addMarker(Marker marker){
        //Add marker to firebase
        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference mReference = mDatabase.getReference("Marker");
        String ID = mReference.push().getKey();
        mReference.child(ID).setValue(marker);
    }

    @Override
    public void onButtonClick(){
        startActivity(new Intent(MainActivity.this, addCarPooling.class));
    }


    @Override
    public void onAddPhoto() {
        startActivity(new Intent(MainActivity.this, addImage.class));
    }

    private void calculateHashKey(String yourPackageName) {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    yourPackageName,
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:",
                        Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onItemClick(CarPooling carPooling) {
        DataBaseHelper.setCurrentCarPooling(carPooling);
        switchFragment(new CarPoolingDetailFragment());

    }
}
