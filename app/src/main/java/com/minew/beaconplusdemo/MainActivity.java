package com.minew.beaconplusdemo;

import static com.minew.beaconplus.sdk.enums.FrameType.FrameiBeacon;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.minew.beaconplus.sdk.MTCentralManager;
import com.minew.beaconplus.sdk.MTFrameHandler;
import com.minew.beaconplus.sdk.MTPeripheral;
import com.minew.beaconplus.sdk.Utils.LogUtils;
import com.minew.beaconplus.sdk.enums.BluetoothState;
import com.minew.beaconplus.sdk.enums.ConnectionStatus;
import com.minew.beaconplus.sdk.exception.MTException;
import com.minew.beaconplus.sdk.frames.IBeaconFrame;
import com.minew.beaconplus.sdk.frames.MinewFrame;
import com.minew.beaconplus.sdk.interfaces.ConnectionStatueListener;
import com.minew.beaconplus.sdk.interfaces.GetPasswordListener;
import com.minew.beaconplus.sdk.interfaces.MTCentralManagerListener;
import com.minew.beaconplus.sdk.interfaces.OnBluetoothStateChangedListener;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_ENABLE_BT          = 3;
    private static final int PERMISSION_COARSE_LOCATION = 2;
    @BindView(R.id.recycle)
    RecyclerView mRecycle;

    private MTCentralManager mMtCentralManager;
    private RecycleAdapter   mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        if (!ensureBleExists())
            finish();
        if (!isBLEEnabled()) {
            showBLEDialog();
        }
        initView();
        initManager();
        getRequiredPermissions();
        initListener();
    }

    private boolean ensureBleExists() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Phone does not support BLE", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    protected boolean isBLEEnabled() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        assert bluetoothManager != null;
        final BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    private void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                initData();
            } else {
                finish();
            }
        }
    }

    private void initView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        mRecycle.setLayoutManager(layoutManager);
        mAdapter = new RecycleAdapter();
        mRecycle.setAdapter(mAdapter);
        mRecycle.addItemDecoration(new RecycleViewDivider(this, LinearLayoutManager
                .HORIZONTAL));
    }


    private void getBeaconMetaData(MTPeripheral mtPeripheral){
        MTFrameHandler mtFrameHandler = mtPeripheral.mMTFrameHandler;
        String mac = mtFrameHandler.getMac(); 		//mac address of device
        String name = mtFrameHandler.getName();		// name of device
        int battery = mtFrameHandler.getBattery();	//battery
        int rssi = mtFrameHandler.getRssi();		//rssi

        Log.v("BeaconPlusDemo", "MAC: " +mac );
        Log.v("BeaconPlusDemo", "NAME: " +name );
        Log.v("BeaconPlusDemo", "BATTERY: " +battery );
        Log.v("BeaconPlusDemo", "RSSI: " +rssi );

        // all data frames of device（such as:iBeacon，UID，URL...）
        ArrayList<MinewFrame> advFrames = mtFrameHandler.getAdvFrames();
        for (MinewFrame minewFrame : advFrames) {
            if(minewFrame.getFrameType()==FrameiBeacon){
                IBeaconFrame iBeaconFrame = (IBeaconFrame) minewFrame;
                String uuid = iBeaconFrame.getUuid();
                int major = iBeaconFrame.getMajor();
                int minor =  iBeaconFrame.getMinor();

                Log.v("BeaconPlusDemo", "UUID: " +uuid );
                Log.v("BeaconPlusDemo", "MAJOR: " +major );
                Log.v("BeaconPlusDemo", "MINOR: " +minor );
            }
        }
    }

    private void initListener() {
        mMtCentralManager.setMTCentralManagerListener(new MTCentralManagerListener() {
            @Override
            public void onScanedPeripheral(final List<MTPeripheral> peripherals) {
                Log.e("BeaconPlusDemo", "Number of beacons detected: " + peripherals.size());
                for (MTPeripheral mtPeripheral : peripherals) {
                    getBeaconMetaData(mtPeripheral);
                }
                mAdapter.setData(peripherals);

            }
        });
    }



    @Override
    public void onRequestPermissionsResult(int code, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (code == PERMISSION_COARSE_LOCATION) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initData();
            } else {
                finish();
            }
        }
    }


    private void initManager() {
        mMtCentralManager = MTCentralManager.getInstance(this);
        mMtCentralManager.startService();
        BluetoothState bluetoothState = mMtCentralManager.getBluetoothState(this);
        switch (bluetoothState) {
            case BluetoothStateNotSupported:
                Log.e("tag", "BluetoothStateNotSupported");
                break;
            case BluetoothStatePowerOff:
                Log.e("tag", "BluetoothStatePowerOff");
                break;
            case BluetoothStatePowerOn:
                Log.e("tag", "BluetoothStatePowerOn");
                break;
        }
        mMtCentralManager.setBluetoothChangedListener(new OnBluetoothStateChangedListener() {
            @Override
            public void onStateChanged(BluetoothState state) {
                switch (state) {
                    case BluetoothStateNotSupported:
                        Log.e("tag", "BluetoothStateNotSupported");
                        break;
                    case BluetoothStatePowerOff:
                        Log.e("tag", "BluetoothStatePowerOff");
                        break;
                    case BluetoothStatePowerOn:
                        Log.e("tag", "BluetoothStatePowerOn");
                        break;
                }
            }
        });
    }

    private void getRequiredPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_COARSE_LOCATION);
        } else {
            initData();
        }
    }

    private void initData() {
        mMtCentralManager.startScan();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMtCentralManager.stopScan();
        mMtCentralManager.stopService();
    }
}
