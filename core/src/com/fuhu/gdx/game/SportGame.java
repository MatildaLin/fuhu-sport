package com.fuhu.gdx.game;

import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.fuhu.gdx.viewport.SafeZoneViewport;

public class SportGame extends Game {

    @Override
    protected void onCreate() {
        setScene(new SplashScene());
    }

    @Override
    protected void onResume() {

    }

    @Override
    protected void onResize() {

    }

    @Override
    protected void onPause() {

    }

    @Override
    protected void onDispose() {

    }

    @Override
    protected Viewport createViewport() {
        return new SafeZoneViewport(
                new Vector2(2280, 1440),
                new Vector2(1920, 1128),
                new OrthographicCamera()
        );
    }

    @Override
    protected FileHandleResolver createFileHandleResolver() {
        return null;
    }
}
