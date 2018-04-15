package com.manali.wimta;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import android.support.v7.widget.Toolbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;
    private Button btnCheckIn;
    public ArrayList<BluetoothDevice> mBTDevices = new ArrayList<>();
    private Button btnCheckOut;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothDevice remoteDevice;
    int DISCOVERY_REQUEST = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnCheckIn = findViewById(R.id.btnCheckIn);
        btnCheckOut = findViewById(R.id.btnCheckOut);
        btnCheckOut.setVisibility(View.GONE);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter.isEnabled()) {
            btnCheckOut.setVisibility(View.VISIBLE);
            btnCheckIn.setVisibility(View.GONE);
        }
        assert btnCheckIn != null;
        btnCheckIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (!mBluetoothAdapter.isEnabled()) {
                    String scanModeChanged = BluetoothAdapter.ACTION_SCAN_MODE_CHANGED;
                    String beDiscoverable = BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE;
                    IntentFilter filter = new IntentFilter(scanModeChanged);
                    registerReceiver(mReceiver, filter);
                    startActivityForResult(new Intent(beDiscoverable), DISCOVERY_REQUEST);
                }
            }
        });

        assert btnCheckOut != null;
        btnCheckOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBluetoothAdapter.cancelDiscovery();
                mBluetoothAdapter.disable();
                btnCheckIn.setVisibility(View.VISIBLE);
                btnCheckOut.setVisibility(View.GONE);
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                assert currentUser != null;
                mDatabaseReference = FirebaseDatabase.getInstance().getReference()
                        .child("CheckedIn")
                        .child(currentUser.getUid());
                mDatabaseReference.setValue(null).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()) {
                            final Snackbar snackBar =  Snackbar.make(findViewById(R.id.coordinatorLayout), "Checked Out Successfully", Snackbar.LENGTH_LONG);

                            snackBar.setAction("Close", new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snackBar.dismiss();
                                }
                            })
                            .setActionTextColor(Color.RED)
                            .show();
                        }
                    }
                });

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.logout){
            FirebaseAuth.getInstance().signOut();
            Intent signOut = new Intent(MainActivity.this, ActivityWelcome.class);
            startActivity(signOut);
            finish();
        }

        return true;
    }

    // Create a BroadcastReceiver for ACTION_FOUND.
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Discovery has found a device. Get the BluetoothDevice
                // object and its info from the Intent.
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                String deviceName = device.getName();
                if(deviceName.equals("raspberrypi")) {
                    Toast.makeText(MainActivity.this, "woofooo", Toast.LENGTH_SHORT).show();
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == DISCOVERY_REQUEST) {
            Toast.makeText(MainActivity.this, "Discovery In Progress", Toast.LENGTH_SHORT).show();
            btnCheckOut.setVisibility(View.VISIBLE);
            btnCheckIn.setVisibility(View.GONE);
            findDevices();
        }
    }

    private void findDevices() {
        if(remoteDevice == null ) {
            Toast.makeText(MainActivity.this, "Starting Discovery for remote devices..", Toast.LENGTH_SHORT).show();
            if(mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
                checkBTPermissions();
                mBluetoothAdapter.startDiscovery();
                Toast.makeText(MainActivity.this, "Finding Raspberry Pi", Toast.LENGTH_SHORT).show();
                registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
            if(!mBluetoothAdapter.isDiscovering()){

                //check BT permissions in manifest
                checkBTPermissions();

                mBluetoothAdapter.startDiscovery();
                Toast.makeText(MainActivity.this, "Finding Raspberry Pi", Toast.LENGTH_SHORT).show();
                registerReceiver(discoveryResult, new IntentFilter(BluetoothDevice.ACTION_FOUND));
            }
        }
    }

    BroadcastReceiver discoveryResult = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, "onReceive: ACTION FOUND.");

            assert action != null;
            if (action.equals(BluetoothDevice.ACTION_FOUND)){
                BluetoothDevice device = intent.getParcelableExtra (BluetoothDevice.EXTRA_DEVICE);
                mBTDevices.add(device);
                Log.d(TAG, "onReceive: " + device.getName() + ": " + device.getAddress());
                if(device.getAddress().equals("B8:27:EB:6E:6D:06")){
                    mAuth = FirebaseAuth.getInstance();
                    final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                    assert currentUser != null;
                    mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("CheckedIn").child(currentUser.getUid());
                    HashMap<String,Object> empty = new HashMap<>();
                    empty.put("user_id", currentUser.getUid());
                    mDatabaseReference.updateChildren(empty).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                final Snackbar snackBar =  Snackbar.make(findViewById(R.id.coordinatorLayout), "Checked In Successfully", Snackbar.LENGTH_LONG);

                                snackBar.setAction("Close", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        snackBar.dismiss();
                                    }
                                })
                                .setActionTextColor(Color.RED)
                                .show();
                            }
                            else {
                                btnCheckOut.setVisibility(View.GONE);
                                btnCheckIn.setVisibility(View.VISIBLE);
                            }
                        }
                    });
                }
            }
        }
    };

    private void checkBTPermissions() {
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {

            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }
    }
}