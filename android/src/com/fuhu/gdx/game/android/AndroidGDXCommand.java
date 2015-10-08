package com.fuhu.gdx.game.android;

import com.fuhu.gdx.game.GDXCommand;

import android.content.pm.ActivityInfo;

public class AndroidGDXCommand implements GDXCommand {

	private AndroidLauncher mAndroidLauncher;
	
	public AndroidGDXCommand(AndroidLauncher androidLauncher) {
		this.mAndroidLauncher = androidLauncher;
	}

	@Override
	public void command(int type) {
		// TODO Auto-generated method stub

	}

	public void setRequestedOrientationLandscape() {
		mAndroidLauncher.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
	}

	public void setRequestedOrientationPortait() {
		mAndroidLauncher.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
	}

}
