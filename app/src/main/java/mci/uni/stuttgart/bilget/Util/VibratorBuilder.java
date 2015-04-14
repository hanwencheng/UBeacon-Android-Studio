package mci.uni.stuttgart.bilget.Util;

import android.content.Context;
import android.media.AudioAttributes;
import android.os.Build;
import android.os.Vibrator;
import android.widget.Toast;

/**
 * Created by hanwencheng on 4/5/15.
 */
public class VibratorBuilder {

    private static VibratorBuilder instance = null;
    private Vibrator vibrator;
    private AudioAttributes audioAttributes;

    public static int LONG1PATTERN = 0;
    public static int SHORT1PATTERN = 1;
    public static int SHORT2PATTERN = 2;

    private static long[] long1Patter = {0, 500};
    private static long[] short1Pattern = {0, 200};
    private static long[] short2Pattern = {0, 200, 0, 200};

    private static long[][] patternArray = {long1Patter, short1Pattern, short2Pattern};

    public static VibratorBuilder getInstance(Context context){
        if(instance == null){
            instance = new VibratorBuilder(context);
        }
        return instance;
    }

    public VibratorBuilder (Context context){
        vibrator = (Vibrator) context.getApplicationContext().getSystemService(Context.VIBRATOR_SERVICE);
        if (Build.VERSION.SDK_INT >= 21) {
            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();
        }
        if(!vibrator.hasVibrator()){
            Toast.makeText(context.getApplicationContext(), "vibration function is disabled.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * the vibrate with certain pattern
     * @param patternCode <ul>
     *                    <li>0 for long once pattern</li>
     *                    <li>1 for short once pattern</li>
     *                    <li>2 for short twice pattern</li>
     *                    </ul>
     */
    public void vibrate(int patternCode){
        vibrateFunc(patternArray[patternCode]);
    }

    private void vibrateFunc(long[] pattern){
        if(vibrator.hasVibrator()){
            if (Build.VERSION.SDK_INT >= 21) {
                vibrator.vibrate(pattern, -1, audioAttributes);
            }else{
                vibrator.vibrate(pattern, -1);
            }
        }
    }
}

