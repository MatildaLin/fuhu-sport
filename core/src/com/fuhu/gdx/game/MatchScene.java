package com.fuhu.gdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.actions.RepeatAction;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.fuhu.gdx.manager.ApplicationManager;
import com.fuhu.gdx.scene.Scene;
import com.fuhu.gdx.scene.SceneLayer;
import com.fuhu.gdx.scene.transition.NoTransition;
import com.fuhu.gdx.sport.*;
import com.fuhu.gdx.utils.MaskedButton;
import com.fuhu.gdx.viewport.SafeZoneViewport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by matildalin on 2015/10/1.
 */
public class MatchScene extends Scene {
	
    private final String TAG = MatchScene.class.getSimpleName();

    private final String MATCH_FOLDER = "images/match/";
    private final String MATCH_SPORT_FOLDER = "images/match/sport/";

    private Image mWhiteboardImage;
    private Image mSportBoxImage;
    private Image mAccessoryBoxImage;
    private Image mSportQuestImage;
    private Image mAccessoryQuestImage;
    private Image mBingoImage;
    private MaskedButton mSportLeftButton;
    private MaskedButton mSportRightButton;
    private MaskedButton mAccessoryLeftButton;
    private MaskedButton mAccessoryRightButton;
    private Label mNameLabel;

    private ArrayList<Image> mSportImageArray = new ArrayList<Image>();
    private ArrayList<Image> mAccessoryImageArray = new ArrayList<Image>();

    private SceneLayer mBingoLayer;

    private int mSportIndex = -1;
    private int mAccessoryIndex = -1;

    protected enum SportType {
        BASEBALL, SOCCER, TENNIS, BOXING, BICYCLE, BASKETBALL, HOCKEY, SKATEBOARD, FOOTBALL, GOLF
    }

    private final int mSportTypeCount = SportType.values().length;
    private int mSportTypeIndex = -1;

    public MatchScene() {
    }

    @Override
    public void loadResources(AssetManager assetManager) {
        Gdx.app.log(TAG, "loadResources");
        assetManager.load(MATCH_FOLDER + "background.png", Texture.class);
        assetManager.load(MATCH_FOLDER + "wood_board.png", Texture.class);
        assetManager.load(MATCH_FOLDER + "box.png", Texture.class);
        assetManager.load(MATCH_FOLDER + "choose_left_button.png", Texture.class);
        assetManager.load(MATCH_FOLDER + "choose_right_button.png", Texture.class);
        assetManager.load(MATCH_FOLDER + "question_mark.png", Texture.class);
        assetManager.load(MATCH_FOLDER + "bingo.png", Texture.class);

        for (SportType st : SportType.values()) {
            assetManager.load(MATCH_SPORT_FOLDER + st.name().toLowerCase() + "_0.png", Texture.class);
            assetManager.load(MATCH_SPORT_FOLDER + st.name().toLowerCase() + "_1.png", Texture.class);
        }
    }

    @Override
    public void loadResourcesComplete(AssetManager assetManager) {
        Gdx.app.log(TAG, "loadResourcesComplete");
        Image bgImage = new Image(getGame().getAssetManager().get(MATCH_FOLDER + "background.png", Texture.class));
        bgImage.setSize(this.getWorldWidth(), this.getWorldHeight());
        bgImage.setPosition(0, 0);

        mWhiteboardImage = new Image(getGame().getAssetManager().get(MATCH_FOLDER + "wood_board.png", Texture.class));
        mWhiteboardImage.setPosition((this.getWidth() - mWhiteboardImage.getWidth()) / 2, 200);
        mWhiteboardImage.addListener(mClickListener);

        mSportBoxImage = new Image(getGame().getAssetManager().get(MATCH_FOLDER + "box.png", Texture.class));
        mSportBoxImage.setPosition(160, 70);

        mAccessoryBoxImage = new Image(getGame().getAssetManager().get(MATCH_FOLDER + "box.png", Texture.class));
        mAccessoryBoxImage.setPosition(this.getWidth() - mSportBoxImage.getX() - mAccessoryBoxImage.getWidth(), 70);

        mSportQuestImage = new Image(getGame().getAssetManager().get(MATCH_FOLDER + "question_mark.png", Texture.class));
        mSportQuestImage.setPosition(mSportBoxImage.getX() + (mSportBoxImage.getWidth() - mSportQuestImage.getWidth()) / 2, 340);
        //setPopping(mSportQuestImage, true);

        mAccessoryQuestImage = new Image(getGame().getAssetManager().get(MATCH_FOLDER + "question_mark.png", Texture.class));
        mAccessoryQuestImage.setPosition(this.getWidth() - mSportQuestImage.getX() - mAccessoryQuestImage.getWidth(), 340);
        //setPopping(mAccessoryQuestImage, true);

        mSportLeftButton = new MaskedButton(getGame().getAssetManager().get(MATCH_FOLDER + "choose_left_button.png", Texture.class));
        mSportLeftButton.setPosition(45 + mSportBoxImage.getX(), 65 + mSportBoxImage.getY());
        mSportLeftButton.addListener(mClickListener);
        setPopping(mSportLeftButton, true);

        mSportRightButton = new MaskedButton(getGame().getAssetManager().get(MATCH_FOLDER + "choose_right_button.png", Texture.class));
        mSportRightButton.setPosition(mSportBoxImage.getX() + mSportBoxImage.getWidth() - 45 - mSportRightButton.getWidth(), mSportLeftButton.getY());
        mSportRightButton.addListener(mClickListener);
        setPopping(mSportRightButton, true);

        mAccessoryLeftButton = new MaskedButton(getGame().getAssetManager().get(MATCH_FOLDER + "choose_left_button.png", Texture.class));
        mAccessoryLeftButton.setPosition(45 + mAccessoryBoxImage.getX(), 65 + mAccessoryBoxImage.getY());
        mAccessoryLeftButton.addListener(mClickListener);
        setPopping(mAccessoryLeftButton, true);

        mAccessoryRightButton = new MaskedButton(getGame().getAssetManager().get(MATCH_FOLDER + "choose_right_button.png", Texture.class));
        mAccessoryRightButton.setPosition(mAccessoryBoxImage.getX() + mAccessoryBoxImage.getWidth() - 45 - mAccessoryRightButton.getWidth()
                , mAccessoryLeftButton.getY());
        mAccessoryRightButton.addListener(mClickListener);
        setPopping(mAccessoryRightButton, true);

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 80;
        param.color = Color.BLACK;
        BitmapFont font = manage(fontGenerator.generateFont(param));
        for (TextureRegion region : font.getRegions()) {
            region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        }
        fontGenerator.dispose();

        Label.LabelStyle style = new Label.LabelStyle(font, Color.BLACK);
        mNameLabel = new Label("  Choose...", style);
        mNameLabel.setPosition(mWhiteboardImage.getX() + (mWhiteboardImage.getWidth() - mNameLabel.getPrefWidth()) / 2 + 10,
                mWhiteboardImage.getY() + (mWhiteboardImage.getHeight() - mNameLabel.getPrefHeight()) / 2 + 120);
        mNameLabel.addListener(mClickListener);
        setPopping(mNameLabel, true);

        SceneLayer layer = new SceneLayer();
        layer.addActor(bgImage);
        layer.addActor(mWhiteboardImage);
        layer.addActor(mNameLabel);

        ArrayList<SportType> randomTypeArray = new ArrayList<SportType>();
        ArrayList<SportType> randomSample = new ArrayList<SportType>(Arrays.asList(SportType.values()));
        Random r = new Random();
        int sampleLen = mSportTypeCount;
        while (sampleLen > 0) {
            int random = r.nextInt(sampleLen--);
            randomTypeArray.add(randomSample.get(random));
            Gdx.app.log(TAG, "random= " + randomSample.get(random).name());
            randomSample.remove(random);
        }

        SceneLayer sportLayer = new SceneLayer();
        SceneLayer accessoryLayer = new SceneLayer();

        for (SportType st : randomTypeArray) {
            Image sportImage = new Image(getGame().getAssetManager().get(MATCH_SPORT_FOLDER + st.name().toLowerCase() + "_0.png", Texture.class));
            sportImage.setName(st.name());
            sportImage.addAction(Actions.alpha(0f));
            sportImage.setPosition(mSportBoxImage.getX() + (mSportBoxImage.getWidth() - sportImage.getWidth()) / 2, mSportBoxImage.getY());
            mSportImageArray.add(sportImage);
            sportLayer.addActor(sportImage);

            Image accessoryImage = new Image(getGame().getAssetManager().get(MATCH_SPORT_FOLDER + st.name().toLowerCase() + "_1.png", Texture.class));
            accessoryImage.setName(st.name());
            accessoryImage.addAction(Actions.alpha(0f));
            accessoryImage.setPosition(mAccessoryBoxImage.getX() + (mAccessoryBoxImage.getWidth() - accessoryImage.getWidth()) / 2, mAccessoryBoxImage.getY());
            mAccessoryImageArray.add(0, accessoryImage);
            accessoryLayer.addActor(accessoryImage);
        }

        sportLayer.addActor(mSportQuestImage);
        sportLayer.addActor(mSportBoxImage);
        sportLayer.addActor(mSportLeftButton);
        sportLayer.addActor(mSportRightButton);

        accessoryLayer.addActor(mAccessoryQuestImage);
        accessoryLayer.addActor(mAccessoryBoxImage);
        accessoryLayer.addActor(mAccessoryLeftButton);
        accessoryLayer.addActor(mAccessoryRightButton);

        Pixmap bgPix = new Pixmap((int) getWidth(), (int) getHeight(), Pixmap.Format.RGBA8888);
        bgPix.setColor(Color.WHITE);
        bgPix.fillRectangle(0, 0, bgPix.getWidth(), bgPix.getHeight());
        Image bingoBgImage = new Image(manage(new Texture(bgPix)));
        bingoBgImage.addAction(Actions.alpha(0.3f));
        bgPix.dispose();

        mBingoImage = new Image(getGame().getAssetManager().get(MATCH_FOLDER + "bingo.png", Texture.class));
        //mBingoImage.setPosition((this.getWidth() - mBingoImage.getWidth()) / 2, (this.getHeight() - mBingoImage.getHeight()) / 2);
        //mBingoImage.addAction(Actions.alpha(0f));

        mBingoLayer = new SceneLayer();
        mBingoLayer.addActor(bingoBgImage);
        mBingoLayer.addActor(mBingoImage);


        addLayer(layer);
        addLayer(sportLayer);
        addLayer(accessoryLayer);
    }

    @Override
    public void unloadResources(AssetManager assetManager) {
        assetManager.unload(MATCH_FOLDER + "background.png");
        assetManager.unload(MATCH_FOLDER + "wood_board.png");
        assetManager.unload(MATCH_FOLDER + "box.png");
        assetManager.unload(MATCH_FOLDER + "choose_left_button.png");
        assetManager.unload(MATCH_FOLDER + "choose_right_button.png");
        assetManager.unload(MATCH_FOLDER + "question_mark.png");

        for (SportType st : SportType.values()) {
            assetManager.unload(MATCH_SPORT_FOLDER + st.name().toLowerCase() + "_0.png");
            assetManager.unload(MATCH_SPORT_FOLDER + st.name().toLowerCase() + "_1.png");
        }
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

    public void flipSportItem(boolean nextPage) {
        mSportLeftButton.setTouchable(Touchable.disabled);
        mSportRightButton.setTouchable(Touchable.disabled);

        if (mSportIndex < 0) {
            mSportQuestImage.setVisible(false);

            setPopping(mSportLeftButton, false);
            setPopping(mSportRightButton, false);
            mSportIndex = 0;

        } else {
            mSportImageArray.get(mSportIndex).addAction(Actions.parallel(
                    Actions.fadeOut(0.5f), Actions.moveTo(mSportBoxImage.getX() + (mSportBoxImage.getWidth() - mSportImageArray.get(mSportIndex).getWidth()) / 2,
                            mSportBoxImage.getY(), 0.5f)));

            if (!nextPage) {
                mSportIndex = mSportIndex - 1 < 0 ? mSportTypeCount - 1 : mSportIndex - 1;
            } else {
                mSportIndex = mSportIndex + 1 >= mSportTypeCount ? 0 : mSportIndex + 1;
            }
        }

        mSportImageArray.get(mSportIndex).addAction(Actions.sequence(Actions.parallel(
                        Actions.fadeIn(0.5f), Actions.moveTo(mSportBoxImage.getX() + (mSportBoxImage.getWidth() - mSportImageArray.get(mSportIndex).getWidth()) / 2,
                                mSportBoxImage.getY() + 300, 0.5f)),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        mSportLeftButton.setTouchable(Touchable.enabled);
                        mSportRightButton.setTouchable(Touchable.enabled);
                        checkMatching();
                    }
                })));
    }

    public void flipAccessoryItem(boolean nextPage) {
        mAccessoryLeftButton.setTouchable(Touchable.disabled);
        mAccessoryRightButton.setTouchable(Touchable.disabled);

        if (mAccessoryIndex < 0) {
            mAccessoryQuestImage.setVisible(false);

            setPopping(mAccessoryLeftButton, false);
            setPopping(mAccessoryRightButton, false);
            mAccessoryIndex = 0;

        } else {
            mAccessoryImageArray.get(mAccessoryIndex).addAction(Actions.parallel(
                    Actions.fadeOut(0.5f), Actions.moveTo(mAccessoryBoxImage.getX() +
                                    (mAccessoryBoxImage.getWidth() - mAccessoryImageArray.get(mAccessoryIndex).getWidth()) / 2,
                            mAccessoryBoxImage.getY(), 0.5f)));

            if (!nextPage) {
                mAccessoryIndex = mAccessoryIndex - 1 < 0 ? mSportTypeCount - 1 : mAccessoryIndex - 1;
            } else {
                mAccessoryIndex = mAccessoryIndex + 1 >= mSportTypeCount ? 0 : mAccessoryIndex + 1;
            }
        }

        mAccessoryImageArray.get(mAccessoryIndex).addAction(Actions.sequence(Actions.parallel(
                        Actions.fadeIn(0.5f), Actions.moveTo(mAccessoryBoxImage.getX() +
                                        (mAccessoryBoxImage.getWidth() - mAccessoryImageArray.get(mAccessoryIndex).getWidth()) / 2,
                                mAccessoryBoxImage.getY() + 300, 0.5f)),
                Actions.run(new Runnable() {
                    @Override
                    public void run() {
                        mAccessoryLeftButton.setTouchable(Touchable.enabled);
                        mAccessoryRightButton.setTouchable(Touchable.enabled);
                        checkMatching();
                    }
                })));
    }

    public void checkMatching() {
        if (mSportIndex < 0 || mAccessoryIndex < 0 || mSportTypeIndex < 0) return;
        if (mSportImageArray.get(mSportIndex).getName().equalsIgnoreCase(mNameLabel.getText().toString()) &&
                mAccessoryImageArray.get(mAccessoryIndex).getName().equalsIgnoreCase(mNameLabel.getText().toString())) {
            Gdx.app.log(TAG, "Match!!");

            final Scene newScene;
            switch (SportType.valueOf(mNameLabel.getText().toString())) {
                case BASEBALL:
                    newScene = new BaseballScene();
                    break;
                case BASKETBALL:
                    newScene = new BasketballScene();
                    break;
                case BICYCLE:
                    newScene = new BicycleScene(192);
                    break;
                case BOXING:
                    newScene = new BoxingScene();
                    break;
                case FOOTBALL:
                	// set portrait viewport
                    getGame().setViewport(new SafeZoneViewport(
                            new Vector2(2280, 4320),
                            new Vector2(1920, 1128),
                            new OrthographicCamera()));
                    
                    newScene = new FootballScene();
                    break;
                case GOLF:
                    newScene = new GolfScene();
                    break;
                case SOCCER:
                    newScene = new SoccerScene();
                    break;
                case TENNIS:
                    newScene = new TennisScene(192);
                    break;
                case HOCKEY:
                	newScene = new HockeyScene();
                	break;
                case SKATEBOARD:
                	newScene = new SkateboardScene();
                	break;
                default:
                    newScene = new MainScene();
            }

            addLayer(mBingoLayer);
            mBingoImage.addAction(Actions.sequence(
                    Actions.parallel(Actions.sizeTo(1f, 1f), Actions.moveTo(this.getWidth() / 2, this.getHeight() / 2)),
                    Actions.parallel(Actions.sizeTo(mBingoImage.getWidth(), mBingoImage.getHeight(), 0.5f),
                            Actions.moveTo((this.getWidth() - mBingoImage.getWidth()) / 2, (this.getHeight() - mBingoImage.getHeight()) / 2, 0.5f)),
                    Actions.delay(0.3f),
                    Actions.run(new Runnable() {
                        @Override
                        public void run() {
                            newScene.setInTransition(new NoTransition(MatchScene.this));
                            getGame().setScene(newScene);
                        }
                    })));

            //newScene.setInTransition(new BottomInTransition(this, 1f));
            //setOutTransition(new TopOutTransition(this, 1f));
            //Scene bingoScene = new BingoScene(newScene);
            //newScene.setInTransition(new NoTransition(bingoScene));
            //bingoScene.setInTransition(new NoTransition(MatchScene.this));

//            getGame().pushScene(newScene);
//            getGame().pushScene(new MainScene());
        }

    }

    public ClickListener mClickListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            if (event.getListenerActor() == mWhiteboardImage || event.getListenerActor() == mNameLabel) {
                mWhiteboardImage.setTouchable(Touchable.disabled);
                if (mSportTypeIndex < 0) {
                    setPopping(mNameLabel, false);
                    mSportTypeIndex = 0;
                }

                mNameLabel.addAction(Actions.sequence(
                        Actions.fadeOut(0.15f),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                mNameLabel.setText(SportType.values()[mSportTypeIndex].name());
                                mNameLabel.setX(mWhiteboardImage.getX() + (mWhiteboardImage.getWidth() - mNameLabel.getPrefWidth()) / 2);

                                mSportTypeIndex = mSportTypeIndex + 1 >= mSportTypeCount ? 0 : mSportTypeIndex + 1;
                            }
                        }),
                        Actions.fadeIn(0.15f),
                        Actions.run(new Runnable() {
                            @Override
                            public void run() {
                                mWhiteboardImage.setTouchable(Touchable.enabled);
                                checkMatching();
                            }
                        })));

            } else if (event.getListenerActor() == mSportLeftButton) {
                flipSportItem(false);

            } else if (event.getListenerActor() == mSportRightButton) {
                flipSportItem(true);

            } else if (event.getListenerActor() == mAccessoryLeftButton) {
                flipAccessoryItem(false);

            } else if (event.getListenerActor() == mAccessoryRightButton) {
                flipAccessoryItem(true);
            }

            super.clicked(event, x, y);
        }
    };
}
