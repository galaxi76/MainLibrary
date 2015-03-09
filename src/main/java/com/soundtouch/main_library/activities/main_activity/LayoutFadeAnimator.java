package com.soundtouch.main_library.activities.main_activity;

import android.os.Handler;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;

public class LayoutFadeAnimator {

	protected AlphaAnimation fadeOut;
	protected AlphaAnimation fadeIn;
	protected ViewGroup animatedLayout;
	protected static final int DEFUALT_DURATION  = 200;
	protected int fadeInDuration = DEFUALT_DURATION;
	protected int fadeOutDuration= DEFUALT_DURATION;
	protected static final float THRESHOLD = 0.6f;
	protected IFadeAnimationStarter animationStarter ;
	protected Handler handler;
	public LayoutFadeAnimator(ViewGroup animatedLayout){
		this.animatedLayout = animatedLayout;
		initializeAnimations();
	}
	
	protected void initializeAnimations(){
		handler = new Handler();
		fadeOut = new AlphaAnimation(1 , THRESHOLD);
		fadeIn  = new AlphaAnimation(THRESHOLD,1);
		fadeOut.setDuration(fadeOutDuration);
		fadeIn.setDuration(fadeInDuration);
		fadeOut.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				if(animationStarter !=null){
					animationStarter.onStart();
				}
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				animatedLayout.startAnimation(fadeIn);
				
			}
		});
	}
	
	/** Set what to do right at the start of the animation**/
	public void setOnAnimationStartRunnable(IFadeAnimationStarter fas){
		animationStarter = fas;
	}
	
	public void startAnimation(){
		animatedLayout.startAnimation(fadeOut);
	}
}
