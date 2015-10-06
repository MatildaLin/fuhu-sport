package com.fuhu.gdx.game;

import com.badlogic.gdx.*;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.fuhu.gdx.scene.Scene;
import com.fuhu.gdx.ui.SpineActor;

/**
 * Created by matildalin on 2015/9/30.
 */
public class MainScene extends Scene {

    private SpineActor mSpineActor;

    public MainScene() {
        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void loadResources(AssetManager assetManager) {
        assetManager.load("images/test/logo.atlas", TextureAtlas.class);
    }

    @Override
    public void loadResourcesComplete(AssetManager assetManager) {
        final TextureAtlas loadingAtlas = getGame().getAssetManager().get("images/test/logo.atlas", TextureAtlas.class);
        mSpineActor = new SpineActor(loadingAtlas, Gdx.files.internal("images/test/logo.json"));
        mSpineActor.setPremultipliedAlpha(true);
        mSpineActor.setDebug(false);
        mSpineActor.getAnimationState().setAnimation(0, "open_set", true);
        mSpineActor.setPosition((this.getWorldWidth() - mSpineActor.getWidth()) / 2,
                (this.getWorldHeight() - mSpineActor.getHeight()) / 2);
        mSpineActor.addListener(mClickListener);
        getRootLayer().addActor(mSpineActor);

    }

    @Override
    public void unloadResources(AssetManager assetManager) {
        super.unloadResources(assetManager);
    }

    public ClickListener mClickListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            //getGame().popScene();
            getGame().setScene(new LoadingScene(new MatchScene()));
        }
    };

    @Override
    public boolean keyDown(int keyCode) {
        if ((keyCode == Input.Keys.ESCAPE) || (keyCode == Input.Keys.BACK)) {
            Gdx.app.log("MainScene", "back key");
            getGame().setScene(new LoadingScene(new MatchScene()));
//            getGame().popScene(new LoadingScene());
        }
        return false;
    }

}
