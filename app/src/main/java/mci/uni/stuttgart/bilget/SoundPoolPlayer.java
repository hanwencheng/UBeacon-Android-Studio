package mci.uni.stuttgart.bilget;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;

import java.util.HashMap;

/**
 * a singleton Class
 * Created by Hanwen Cheng on 2/26/15.
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
            SoundPool.Builder builder = new SoundPool.Builder();
            builder.setMaxStreams(1);
            this.mShortPlayer = builder.build();
        }else{
            this.mShortPlayer = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 0);
        }
        mSoundsMap.put(R.raw.arcade_action_04, this.mShortPlayer.load(context, R.raw.arcade_action_04, 1));
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