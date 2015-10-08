package com.fuhu.gdx.manager;

import com.badlogic.gdx.utils.Disposable;
import com.fuhu.gdx.game.GDXCommand;

public class ApplicationManager implements Disposable {

	private static ApplicationManager sInstance = new ApplicationManager();
	private GDXCommand mCommand;

	public synchronized static ApplicationManager getInstance() {
		if (sInstance == null) {
			sInstance = new ApplicationManager();
		}
		return sInstance;
	}

	public void setGDXCommand(GDXCommand mCommand) {
		this.mCommand = mCommand;
	}

	public GDXCommand getGDXCommand() {
		return mCommand;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
