package com.f8th_inc;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media.VolumeProviderCompat;

import java.util.ArrayList;

import static com.f8th_inc.MainActivity.EXTRA_PUT_IN_FG;
import static com.f8th_inc.Utils.isVolumeDownPressedContinously;

public class VolumeService extends Service {

    private IOnToggleStateListener mIOnToggleStateListener;
    private ArrayList<Integer> mVolumeList = new ArrayList<>();

    public interface IOnToggleStateListener {
        public void onToggleState();
    }

    public void setIOnToggleStateListener(IOnToggleStateListener iOnToggleStateListener) {
        mIOnToggleStateListener = iOnToggleStateListener;
    }

    private MediaSessionCompat mediaSession;
    // This is the object that receives interactions from clients.
    private final IBinder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        VolumeService getService() {
            return VolumeService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mediaSession = new MediaSessionCompat(this, "PlayerService");
        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PLAYING, 0, 0) //you simulate a player which plays something.
                .build());

        //this will only work on Lollipop and up, see https://code.google.com/p/android/issues/detail?id=224134
        VolumeProviderCompat myVolumeProvider =
                new VolumeProviderCompat(VolumeProviderCompat.VOLUME_CONTROL_RELATIVE, 100,
                        10) {
                    @Override
                    public void onAdjustVolume(int direction) {
                        mVolumeList.add(direction);
                        Log.i(MainActivity.TAG, "onAdjustVolume()," + mVolumeList);
                        if (mVolumeList.size() == 6) {
                            if (isVolumeDownPressedContinously(mVolumeList)) {
                                if (F8thApplication.getF8thApplication().isApplicationInBackground()) {
                                    broadcastAction(true);
                                } else {
                                    if (null != mIOnToggleStateListener)
                                        mIOnToggleStateListener.onToggleState();
                                }
                            }
                            mVolumeList.clear();
                        }
                    }
                };

        mediaSession.setPlaybackToRemote(myVolumeProvider);
        mediaSession.setActive(true);
        mVolumeList.clear();
    }


    // called to send data to Activity
    public void broadcastAction(boolean putInFG) {
        Intent intent = new Intent(MainActivity.ACTION_PUT_IN_BG_FG);
        intent.putExtra(EXTRA_PUT_IN_FG, putInFG);
        LocalBroadcastManager bm = LocalBroadcastManager.getInstance(this);
        bm.sendBroadcast(intent);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mediaSession.release();
    }
}
