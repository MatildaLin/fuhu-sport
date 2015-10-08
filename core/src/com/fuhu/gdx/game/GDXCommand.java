package com.fuhu.gdx.game;

public interface GDXCommand {

	public void command(int type);
	
	public void setRequestedOrientationLandscape();
	public void setRequestedOrientationPortait();
}
