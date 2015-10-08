package com.fuhu.gdx.game.android;

import android.os.Bundle;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.fuhu.gdx.game.SportGame;
import com.fuhu.gdx.manager.ApplicationManager;

public class AndroidLauncher extends AndroidApplication {
	
	ApplicationManager applicationManager = ApplicationManager.getInstance();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		applicationManager.setGDXCommand(new AndroidGDXCommand(this));

		AndroidApplicationConfiguration config = new AndroidApplicationConfiguration();
		initialize(new SportGame(), config);
	}
}
