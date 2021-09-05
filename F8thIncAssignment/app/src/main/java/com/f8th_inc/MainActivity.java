package com.f8th_inc;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;

import static com.f8th_inc.Utils.isVolumeDownPressedContinously;

public class MainActivity extends AppCompatActivity implements VolumeService.IOnToggleStateListener {

    public static final String TAG = "F8TH_INC";
    public static final String ACTION_PUT_IN_BG_FG = "ACTION_PUT_IN_BG_FG";
    public static final String EXTRA_PUT_IN_FG = "EXTRA_PUT_IN_FG";
    private boolean mIsBound;
    private VolumeService mVolumeService;
    private ArrayList<Integer> mVolumeList = new ArrayList<>();
    private int mCounter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        Log.i(TAG, "onCreate() intent:" + intent);
        if (null != intent) {
            String action = intent.getAction();
            boolean putInFG = intent.getBooleanExtra(EXTRA_PUT_IN_FG, false);
            Log.i(TAG, "onCreate(),action:" + action + ",putInFG:" + putInFG);
            if (!putInFG) {
                moveToBackground();
            }
        }
        doBindService();
        registerReceiver();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        super.onKeyDown(keyCode, event);
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mVolumeList.add(mCounter++);
            Log.i(MainActivity.TAG, "onKeyDown(),KEYCODE_VOLUME_DOWN,mVolumeList:" + mVolumeList);
            if (mVolumeList.size() == 3) {
                if (isVolumeDownPressedContinously(mVolumeList)) {
                    moveToBackground();
                }
                mVolumeList.clear();
            }
            return true;
        }
        return false;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            mVolumeService = ((VolumeService.LocalBinder) service).getService();
            mVolumeService.setIOnToggleStateListener(MainActivity.this);
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            mVolumeService = null;
        }
    };

    void doBindService() {
        if (!mIsBound) {
            bindService(new Intent(this, VolumeService.class), mConnection, Context.BIND_AUTO_CREATE);
            mIsBound = true;
        }
    }

    void doUnbindService() {
        if (mIsBound) {
            // Detach our existing connection.
            unbindService(mConnection);
            mIsBound = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        doUnbindService();
        unregisterReceiver();
    }

    @Override
    public void onToggleState() {
        Log.i(TAG, "onToggleState()");
        moveToBackground();
    }

    private void moveToBackground() {
        boolean sentAppToBackground = moveTaskToBack(true);
        Log.i(TAG, "sentAppToBackground:" + sentAppToBackground);
        if (!sentAppToBackground) {
            Intent i = new Intent();
            i.setAction(Intent.ACTION_MAIN);
            i.addCategory(Intent.CATEGORY_HOME);
            this.startActivity(i);
        }
    }

    // handler for received data from service
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(MainActivity.ACTION_PUT_IN_BG_FG)) {
                Log.i(MainActivity.TAG, "Calling startActivity(intent);");
                Intent intent2 = new Intent(MainActivity.this, MainActivity.class);
                intent2.setAction(MainActivity.ACTION_PUT_IN_BG_FG);
                boolean putInFG = intent.getBooleanExtra(MainActivity.EXTRA_PUT_IN_FG, false);
                intent2.putExtra(EXTRA_PUT_IN_FG, putInFG);
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                MainActivity.this.startActivity(intent2);
            }
        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_PUT_IN_BG_FG);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.registerReceiver(mBroadcastReceiver, filter);
    }


    private void unregisterReceiver() {
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }
}