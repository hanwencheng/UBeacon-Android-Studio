package mci.uni.stuttgart.bilget;

import android.animation.ValueAnimator;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BeaconsViewHolder extends RecyclerView.ViewHolder implements View.OnTouchListener, View.OnFocusChangeListener{
	
	protected TextView vName;
	protected TextView vRSSI;
	protected TextView vLabel;
	protected TextView vMACaddress;
    protected TextView vCategory;
    protected TextView vDescription;
    protected LinearLayout vExpandArea;

    private static String TAG = "BeaconViewHolder";
    private int mOriginalHeight = 0;
    private boolean mIsViewExpanded = false;

	public BeaconsViewHolder(View view) {
		super(view);
		
		vName = (TextView) view.findViewById(R.id.beacon_item_name);
		vRSSI = (TextView) view.findViewById(R.id.beacon_item_RSSI);
		vLabel = (TextView) view.findViewById(R.id.beacon_item_label);
//        vCategory = (TextView) view.findViewById(R.id.beacon_item_category);
        vDescription = (TextView) view.findViewById(R.id.beacon_item_description);
//		vMACaddress = (TextView) view.findViewById(R.id.beacon_item_MACaddress);
        vExpandArea = (LinearLayout)view.findViewById(R.id.expandArea);

	}

    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        if(hasFocus) {
            Log.i(TAG, "get click event on the view");
            if (mOriginalHeight == 0) {
                mOriginalHeight = view.getHeight();
            }
            ValueAnimator valueAnimator;
            if (!mIsViewExpanded) {
                mIsViewExpanded = true;
                valueAnimator = ValueAnimator.ofInt(mOriginalHeight, mOriginalHeight + (int) (mOriginalHeight * 1.5));
            } else {
                mIsViewExpanded = false;
                valueAnimator = ValueAnimator.ofInt(mOriginalHeight + (int) (mOriginalHeight * 1.5), mOriginalHeight);
            }
            valueAnimator.setDuration(300);
            valueAnimator.setInterpolator(new LinearInterpolator());
            final View theView = view;
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    theView.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                    theView.requestLayout();
                }
            });
            valueAnimator.start();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if(MotionEvent.ACTION_UP == event.getAction()){
            Log.i(TAG, "get click event on the view");
            if (mOriginalHeight == 0) {
                mOriginalHeight = view.getHeight();
            }
            ValueAnimator valueAnimator;
            if (!mIsViewExpanded) {
                mIsViewExpanded = true;
                valueAnimator = ValueAnimator.ofInt(mOriginalHeight, mOriginalHeight + (int) (mOriginalHeight * 1.5));
            } else {
                mIsViewExpanded = false;
                valueAnimator = ValueAnimator.ofInt(mOriginalHeight + (int) (mOriginalHeight * 1.5), mOriginalHeight);
            }
            valueAnimator.setDuration(300);
            valueAnimator.setInterpolator(new LinearInterpolator());
            final View theView = view;
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    theView.getLayoutParams().height = (Integer) animation.getAnimatedValue();
                    theView.requestLayout();
                }
            });
            valueAnimator.start();
        }
        return false;
    }
}
