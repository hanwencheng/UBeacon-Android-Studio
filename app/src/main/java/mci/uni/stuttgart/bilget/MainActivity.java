package mci.uni.stuttgart.bilget;

import android.media.AudioManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;

import mci.uni.stuttgart.bilget.Util.UBeaconPageAdapter;

public class MainActivity extends FragmentActivity {

    private static String TAG = "main Activity";
    UBeaconPageAdapter pagerAdapter;
    FragmentManager fragmentManager;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//        test();
		setContentView(R.layout.activity_main);
        fragmentManager = getSupportFragmentManager();
        pagerAdapter = new UBeaconPageAdapter(fragmentManager);
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setCurrentItem(0);

//		if (savedInstanceState == null) {
//			getSupportFragmentManager().beginTransaction()
//					.add(R.id.container, new ScanListFragment()).commit();
//		}

        //add apps audio volume control.
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
//		if (id == R.id.action_settings) {
//			return true;
//		}
		return super.onOptionsItemSelected(item);
	}


//	========================================Test Module=======================================
//	==========================================================================================
    private void test(){
        int[] initArray = new int[3];
        initArray[0] = 20;
        for (int i = 0; i < 3 ; i++){
            Log.d(TAG, "our array" + i + "is" + initArray[i]);
        }

    }


}
