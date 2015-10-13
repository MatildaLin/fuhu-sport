package com.fuhu.gdx.sport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.fuhu.gdx.game.LoadingScene;
import com.fuhu.gdx.game.MatchScene;
import com.fuhu.gdx.physic.PhysicScene;
import com.fuhu.gdx.scene.Scene;
import com.fuhu.gdx.scene.transition.NoTransition;
import com.fuhu.gdx.sprite.Bicycle;

/**
 * Created by sabrinakuo on 2015/9/25.
 */
public class BicycleScene extends PhysicScene {

    private static int pixelsPerUnit = 192;
    private final int GROUND_Y_HEIGHT = 100;
    private World mWorld;
    private OrthographicCamera mCamera;
    private SpriteBatch batch;
    private Bicycle mBicycle;
    private RevoluteJoint revoluteJoint;
    private Vector2 position;
    private Vector2 groundPos1, groundPos2, groundPos3;
    private float MOVEMENT = 400;

    private Texture chainwheelTexture, crankTexture, frameTexture, pedalTexture, wheelTexture;
    private Texture bgTextrue, groundTexture;

    public BicycleScene() {
        super(pixelsPerUnit);

        Gdx.input.setCatchBackKey(true);

        mWorld = this.getWorld();
        batch = new SpriteBatch();
        mCamera = new OrthographicCamera(this.getCamera().viewportWidth, this.getCamera().viewportHeight);
        mCamera.position.set(mCamera.viewportWidth / 2, mCamera.viewportHeight / 2, 0);
        mCamera.update();

        position = new Vector2(mCamera.viewportWidth / 2, mCamera.viewportHeight / 2);

        this.setDebugBox2d(false);
    }

    private void createWorldBody() {
        groundPos1 = new Vector2(0, 0);
        groundPos2 = new Vector2(this.getWorldWidth(), 0);
        groundPos3 = new Vector2(this.getWorldWidth() * 2, 0);


        mBicycle = new Bicycle(this, mWorld);
        revoluteJoint = mBicycle.getRevoluteJoint();
    }

    private void updateGround() {
        if (mCamera.position.x - (mCamera.viewportWidth / 2) > groundPos1.x + this.getWorldWidth())
            groundPos1.add(this.getWorldWidth() * 3, 0);
        if (mCamera.position.x - (mCamera.viewportWidth / 2) > groundPos2.x + this.getWorldWidth())
            groundPos2.add(this.getWorldWidth() * 3, 0);
        if (mCamera.position.x - (mCamera.viewportWidth / 2) > groundPos3.x + this.getWorldWidth())
            groundPos3.add(this.getWorldWidth() * 3, 0);

    }

    public void update(float dt) {
        updateGround();

        if (revoluteJoint.getMotorSpeed() != 0f)
            position.add(MOVEMENT * dt, 0);

        mCamera.position.x = position.x;
        mCamera.update();

    }

    @Override
    public void render(float elapsedSeconds) {

        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(elapsedSeconds);

        mBicycle.update();

        batch.setProjectionMatrix(mCamera.combined);
        batch.begin();
        batch.draw(groundTexture, groundPos1.x, groundPos1.y, this.getWorldWidth(), GROUND_Y_HEIGHT);
        batch.draw(groundTexture, groundPos2.x, groundPos2.y, this.getWorldWidth(), GROUND_Y_HEIGHT);
        batch.draw(groundTexture, groundPos3.x, groundPos3.y, this.getWorldWidth(), GROUND_Y_HEIGHT);
        batch.end();
        super.render(elapsedSeconds);
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        revoluteJoint.setMotorSpeed(10f);
        revoluteJoint.setMaxMotorTorque(10f);
        return true;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        revoluteJoint.setMotorSpeed(0f);
        revoluteJoint.setMaxMotorTorque(100f);
        return true;
    }

    public int getGROUND_Y_HEIGHT() {
        return GROUND_Y_HEIGHT;
    }

    public Texture getChainwheelTexture() {
        return chainwheelTexture;
    }

    public Texture getCrankTexture() {
        return crankTexture;
    }

    public Texture getPedalTexture() {
        return pedalTexture;
    }

    public Texture getWheelTexture() {
        return wheelTexture;
    }

    public Texture getFrameTexture() {
        return frameTexture;
    }

    public Texture getBgTextrue() {
        return bgTextrue;
    }

    public float changeToUnits(float pixels) {
        return toUnits(pixels);
    }

    public float changeToPixels(float units) {
        return toPixels(units);
    }

    @Override
    public void loadResources(AssetManager assetManager) {
        assetManager.load("images/sport/bicycle/chainwheel.png", Texture.class);
        assetManager.load("images/sport/bicycle/crank.png", Texture.class);
        assetManager.load("images/sport/bicycle/frame.png", Texture.class);
        assetManager.load("images/sport/bicycle/pedal.png", Texture.class);
        assetManager.load("images/sport/bicycle/wheel.png", Texture.class);
        assetManager.load("images/sport/bicycle/background.jpg", Texture.class);
        assetManager.load("images/sport/ground.png", Texture.class);
    }

    @Override
    public void loadResourcesComplete(AssetManager assetManager) {
        chainwheelTexture = assetManager.get("images/sport/bicycle/chainwheel.png");
        crankTexture = assetManager.get("images/sport/bicycle/crank.png");
        frameTexture = assetManager.get("images/sport/bicycle/frame.png");
        pedalTexture = assetManager.get("images/sport/bicycle/pedal.png");
        wheelTexture = assetManager.get("images/sport/bicycle/wheel.png");
        bgTextrue = assetManager.get("images/sport/bicycle/background.jpg");
        groundTexture = assetManager.get("images/sport/ground.png");
        createWorldBody();
    }

    @Override
    public void unloadResources(AssetManager assetManager) {
        assetManager.unload("images/sport/bicycle/chainwheel.png");
        assetManager.unload("images/sport/bicycle/crank.png");
        assetManager.unload("images/sport/bicycle/frame.png");
        assetManager.unload("images/sport/bicycle/pedal.png");
        assetManager.unload("images/sport/bicycle/wheel.png");
        assetManager.unload("images/sport/bicycle/background.jpg");
        assetManager.unload("images/sport/ground.png");
    }

    @Override
    public boolean keyDown(int keyCode) {
        if ((keyCode == Input.Keys.ESCAPE) || (keyCode == Input.Keys.BACK)) {
            Scene matchScene = new MatchScene();
            Scene loadingScene = new LoadingScene(matchScene);
            matchScene.setInTransition(new NoTransition(loadingScene));
            loadingScene.setInTransition(new NoTransition(this));
            getGame().setScene(loadingScene);
        }
        return false;
    }
}
