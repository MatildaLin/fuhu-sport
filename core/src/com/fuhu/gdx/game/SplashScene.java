package com.fuhu.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.fuhu.gdx.scene.Scene;
import com.fuhu.gdx.scene.SceneLayer;
import com.fuhu.gdx.scene.transition.NoTransition;

/**
 * Created by matildalin on 2015/10/1.
 */
public class SplashScene extends Scene {

    public SplashScene() {
        Pixmap bgPix = new Pixmap((int) getWidth(), (int) getHeight(), Pixmap.Format.RGBA8888);
        bgPix.setColor(Color.WHITE);
        bgPix.fillRectangle(0, 0, bgPix.getWidth(), bgPix.getHeight());
        Image bgImage = new Image(manage(new Texture(bgPix)));
        bgPix.dispose();

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/house_a_rama.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 160;
        param.color = Color.BLACK;
        BitmapFont font = manage(fontGenerator.generateFont(param));
        for (TextureRegion region : font.getRegions()) {
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }

        fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Regular.ttf"));
        param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 40;
        param.color = Color.GRAY;
        BitmapFont subFont = manage(fontGenerator.generateFont(param));
        for (TextureRegion region : subFont.getRegions()) {
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        fontGenerator.dispose();

        Label.LabelStyle style = new Label.LabelStyle(font, Color.BLACK);
        final Label label = new Label("", style);
        label.setY((getHeight() - label.getPrefHeight()) / 2 + 100);

        label.addAction(Actions.sequence(
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        label.setText("S");
                        label.setX((getWidth() - label.getPrefWidth()) / 2);
                    }
                }), Actions.delay(0.5f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        label.setText("SP");
                        label.setX((getWidth() - label.getPrefWidth()) / 2);
                    }
                }), Actions.delay(0.5f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        label.setText("SPO");
                        label.setX((getWidth() - label.getPrefWidth()) / 2);
                    }
                }), Actions.delay(0.5f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        label.setText("SPOR");
                        label.setX((getWidth() - label.getPrefWidth()) / 2);
                    }
                }), Actions.delay(0.5f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        label.setText("SPORT");
                        label.setX((getWidth() - label.getPrefWidth()) / 2);
                    }
                }), Actions.delay(1f)
        ));

        SceneLayer mainLayer = new SceneLayer();
        mainLayer.setSize(getWidth(), getHeight());
        mainLayer.addActor(bgImage);
        mainLayer.addActor(label);


        Label.LabelStyle subStyle = new Label.LabelStyle(subFont, Color.GRAY);
        final Label subLabel = new Label("Fuhu @ 2015  ", subStyle);

        SceneLayer companyLayer = new SceneLayer();
        companyLayer.setSize(subLabel.getPrefWidth(), subLabel.getPrefHeight());
        companyLayer.setAlign(Align.bottomRight);
        companyLayer.addActor(subLabel);

        addLayer(mainLayer);
        addLayer(companyLayer);
    }

    @Override
    public void enter() {
        getRootLayer().addAction(Actions.sequence(
                Actions.delay(2f),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        Scene matchScene = new MatchScene();
                        Scene loadingScene = new LoadingScene(matchScene);
                        matchScene.setInTransition(new NoTransition(loadingScene));
                        loadingScene.setInTransition(new NoTransition(SplashScene.this));
                        getGame().setScene(loadingScene);

                        //getGame().setScene(new LoadingScene(new MatchScene()));
                    }
                })));
        super.enter();
    }
}
