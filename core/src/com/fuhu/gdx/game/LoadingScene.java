package com.fuhu.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Disposable;
import com.fuhu.gdx.scene.Scene;
import com.fuhu.gdx.ui.SpineActor;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matildalin on 2015/9/30.
 */
public class LoadingScene extends com.fuhu.gdx.scene.loading.LoadingScene {

    private List<Disposable> mDisposable = new ArrayList<Disposable>();

    public LoadingScene() {
        super();
    }

    public LoadingScene(Scene targetScene) {
        super(targetScene);

        Pixmap bgPix = new Pixmap((int) getWidth(), (int) getHeight(), Pixmap.Format.RGBA8888);
        bgPix.setColor(Color.GRAY);
        bgPix.fillRectangle(0, 0, bgPix.getWidth(), bgPix.getHeight());
        Image bgImage = new Image(manage(new Texture(bgPix)));
        bgPix.dispose();
        getRootLayer().addActor(bgImage);

        final TextureAtlas loadingAtlas = new TextureAtlas(Gdx.files.internal("images/loading/spineboy.atlas"));
        mDisposable.add(loadingAtlas);
        SpineActor loadingActor = new SpineActor(loadingAtlas, Gdx.files.internal("images/loading/spineboy.json"));
        loadingActor.setPremultipliedAlpha(true);
        loadingActor.setDebug(false);
        loadingActor.getAnimationState().setAnimation(0, "walk", true);
        loadingActor.setPosition((this.getWorldWidth() - loadingActor.getWidth()) / 2,
                (this.getWorldHeight() - loadingActor.getHeight()) / 2);
        getRootLayer().addActor(loadingActor);

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 40;
        param.color = Color.BLACK;
        BitmapFont font = manage(fontGenerator.generateFont(param));
        for (TextureRegion region : font.getRegions()) {
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        fontGenerator.dispose();

        Label.LabelStyle style = new Label.LabelStyle(font, Color.BLACK);
        Label label = new com.badlogic.gdx.scenes.scene2d.ui.Label("Loading...", style);
        label.setPosition((this.getWorldWidth() - label.getWidth()) / 2, loadingActor.getY() - 60);
        getRootLayer().addActor(label);
    }

    @Override
    public void dispose() {
        for (Disposable d : mDisposable) {
            d.dispose();
        }
        super.dispose();
    }
}
