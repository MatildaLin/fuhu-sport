package com.fuhu.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.utils.Disposable;
import com.fuhu.gdx.scene.Scene;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matildalin on 2015/10/6.
 */
public class BingoScene extends com.fuhu.gdx.scene.loading.LoadingScene {

    private List<Disposable> mDisposable = new ArrayList<Disposable>();

    public BingoScene(Scene targetScene) {
        super(targetScene);

        Pixmap bgPix = new Pixmap((int) getWidth(), (int) getHeight(), Pixmap.Format.RGBA8888);
        bgPix.setColor(Color.WHITE);
        bgPix.fillRectangle(0, 0, bgPix.getWidth(), bgPix.getHeight());
        Image bgImage = new Image(manage(new Texture(bgPix)));
        bgImage.addAction(Actions.alpha(0.5f));
        bgPix.dispose();
        getRootLayer().addActor(bgImage);

        Texture bingoTexture = new Texture(Gdx.files.internal("images/match/bingo.png"));
        mDisposable.add(bingoTexture);
        Image bingoImage = new Image(bingoTexture);
        bingoImage.setPosition((this.getWidth() - bingoImage.getWidth()) / 2, (this.getHeight() - bingoImage.getHeight()) / 2);
        //setPopping(bingoImage, true);
        getRootLayer().addActor(bingoImage);
    }

    private void setPopping(Actor actor, boolean isAdding) {
        if (isAdding) {
            float width = actor.getWidth();
            float height = actor.getHeight();
            float scale = 1.15f;
            float scale_offset = scale - 1;
            float duration = 0.3f;
            ParallelAction poppingAction = Actions.sequence(Actions.parallel(
                    Actions.moveTo(actor.getX() - actor.getWidth() * scale_offset / 2, actor.getY() - actor.getHeight() * scale_offset / 2, duration),
                    Actions.sizeTo(width * scale, height * scale, duration)), Actions.parallel(Actions.moveTo(actor.getX(), actor.getY(), duration),
                    Actions.sizeTo(width, height, duration)));
            actor.addAction(Actions.repeat(RepeatAction.FOREVER, poppingAction));
        } else {
            if (actor.getActions().size == 0) return;
            for (Action action : actor.getActions()) {
                actor.removeAction(action);
            }
        }
    }

    @Override
    public void dispose() {
        for (Disposable d : mDisposable) {
            d.dispose();
        }
        super.dispose();
    }
}
