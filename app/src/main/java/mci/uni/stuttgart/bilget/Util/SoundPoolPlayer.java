package mci.uni.stuttgart.bilget.Util;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.util.HashMap;

import mci.uni.stuttgart.bilget.R;

/**
 * a singleton Class
 * Created by Hanwen Cheng on 2/26/15.
 */

/**
 * Sound Play API
 * include three functions
 *
 * load: load resource to sounds library
 *
 * play: play the sound now
 *
 * release: delete resource in sounds library
 */
public class SoundPoolPlayer {

    private static SoundPoolPlayer instance = null;
    private SoundPool mShortPlayer= null;
    private HashMap mSoundsMap = new HashMap();
    private Context context;

    public static SoundPoolPlayer getInstance(Context context){
        if(instance == null){
            instance = new SoundPoolPlayer(context);
        }
        return instance;
    }

    public SoundPoolPlayer(Context context)
    {
        this.context = context;
        // setup Soundpool
        if (Build.VERSION.SDK_INT >= 21) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
            mShortPlayer = new SoundPool.Builder()
                    .setAudioAttributes(audioAttributes)
                    .setMaxStreams(1)
                    .build();
        }else{
            this.mShortPlayer = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }
//        mShortPlayer.setVolume(1,1,1); TODO
        //intitial loading
        mSoundsMap.put(R.raw.scanning, this.mShortPlayer.load(context, R.raw.scanning, 1));
        mSoundsMap.put(R.raw.new_direction, this.mShortPlayer.load(context, R.raw.scanning, 2));
    }

    public void load(int resourceID){
        mSoundsMap.put(resourceID , this.mShortPlayer.load(context, resourceID, 1));
    }

    public void play(int piResource) {
        //get the sound ID returned by the load function
        int iSoundId = (Integer) mSoundsMap.get(piResource);
        this.mShortPlayer.play(iSoundId, 0.99f, 0.99f, 0, 0, 1);
    }

    // Cleanup
    public void release() {
        // Cleanup
        this.mShortPlayer.release();
        this.mShortPlayer = null;
    }
}