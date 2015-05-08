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

    public static int SHORT_ONCE        = 0;
    public static int LONG_ONCE         = 1;
    public static int SHORT_SHORT       = 2;
    public static int LONG_LONG         = 3;
    public static int LONG_SHORT        = 4;
    public static int SHORT_LONG_SHORT  = 5;
    public static int SHORT_LONG        = 6;

    private static long[] short_once = {0, 150};
    private static long[] long_once = {0, 300};
    private static long[] short_short = {0, 150, 0, 150};
    private static long[] long_long = {0, 300, 0, 300};
    private static long[] long_short = {0, 300, 0, 150};
    private static long[] short_long_short = {0, 150, 0, 150};
    private static long[] short_long = {0, 150, 0, 300};

    private static long[][] patternArray =
            {short_once, long_once, short_short, long_long, long_short, short_long_short, short_long};

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

