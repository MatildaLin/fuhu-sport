package com.fuhu.gdx.sport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;
import com.fuhu.gdx.game.LoadingScene;
import com.fuhu.gdx.game.MatchScene;
import com.fuhu.gdx.physic.PhysicScene;
import com.fuhu.gdx.scene.Scene;
import com.fuhu.gdx.scene.transition.NoTransition;
import com.fuhu.gdx.sprite.TennisBall;
import com.fuhu.gdx.sprite.TennisRacket;

/**
 * Created by sabrinakuo on 2015/9/25.
 */
public class TennisScene extends PhysicScene {
    private final int GROUND_Y_HEIGHT = 100;
    private World mWorld;
    private TennisBall mTennisBall;
    private TennisRacket mTennisRacket;

    private MouseJointDef jointDef;
    private MouseJoint mouseJoint;
    private Body worldBounds;
    private Fixture fixracket;

    private Texture groundTexture, ballTexture, racketTexture;
    private Vector2 groundPos1;
    private BitmapFont bitmapFont;

    private boolean mb_BodyDead = false;
    private int mScore = 0;

    private OrthographicCamera mCamera;
    private SpriteBatch batch;
    private Array<Body> bodies = new Array<Body>();

    public TennisScene(float pixelsPerUnit) {
        super(pixelsPerUnit);

        Gdx.input.setCatchBackKey(true);

        mWorld = this.getWorld();
        mCamera = (OrthographicCamera) this.getCamera();
        mCamera.position.set(mCamera.viewportWidth / 2, mCamera.viewportHeight / 2, 0);
        mCamera.update();
        bitmapFont = new BitmapFont();

        batch = new SpriteBatch();
        groundPos1 = new Vector2(mCamera.position.x - mCamera.viewportWidth / 2, 0);

        // create font
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("fonts/Roboto-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 58;
        parameter.color = Color.BLACK;

        bitmapFont = generator.generateFont(parameter);
        generator.dispose();

        Gdx.input.setInputProcessor(this);

        this.setDebugBox2d(false);
    }

    private void createWorldBody() {
        mTennisBall = new TennisBall(this, mWorld);
        mTennisRacket = new TennisRacket(this, mWorld);
        fixracket = mTennisRacket.getFixracket();
        createWorldEdge();
        // mouse joint
        jointDef = new MouseJointDef();
        jointDef.bodyA = worldBounds;
        jointDef.collideConnected = true;
        jointDef.maxForce = 1000f * mTennisRacket.getRacketBody().getMass();
        jointDef.dampingRatio = 1;
    }

    private void createWorldEdge() {
        // body definition
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);
        worldBounds = mWorld.createBody(bodyDef);

        // world edges
        EdgeShape edgeShape = new EdgeShape();
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = edgeShape;

        // edges
        // bottom
        edgeShape.set(new Vector2(0f, toUnits(GROUND_Y_HEIGHT)), new Vector2(toUnits(mCamera.viewportWidth), toUnits(GROUND_Y_HEIGHT)));
        worldBounds.createFixture(fixtureDef);

        edgeShape.dispose();
    }

    @Override
    public void render(float elapsedSeconds) {
        super.render(elapsedSeconds);
        Gdx.gl.glClearColor(1f, 1f, 1f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        Body ballBody = mTennisBall.getBallBody();
        if (!mb_BodyDead && ballBody.getPosition().x < 0) {
            mScore++;
            mb_BodyDead = true;
        }

        batch.setProjectionMatrix(mCamera.combined);
        batch.begin();
        // fill the array with all bodies
        mWorld.getBodies(bodies);

        for (Body b : bodies) {
            // get body's user data
            Sprite sprite = (Sprite) b.getUserData();

            if (sprite != null) {
                sprite.setPosition(toPixels(b.getPosition().x) - sprite.getWidth() / 2, toPixels(b.getPosition().y) - sprite.getHeight() / 2);
                if (b.equals(ballBody)) {
                    sprite.setOrigin(mTennisBall.getRadius(), mTennisBall.getRadius());
                    sprite.setRotation(MathUtils.radiansToDegrees * b.getAngle());
                }
                sprite.draw(batch);
            }
        }
        batch.draw(groundTexture, groundPos1.x, groundPos1.y, this.getWorldWidth(), GROUND_Y_HEIGHT);
        bitmapFont.draw(batch, "Score : " + mScore, 0 + 10f, mCamera.viewportHeight - 10f);
        batch.end();

        if (toPixels(ballBody.getPosition().x) < -1000f || toPixels(ballBody.getPosition().x) > mCamera.viewportWidth + 1000f) {
            mWorld.destroyBody(ballBody);
            new TennisBall(this, mWorld);
            mb_BodyDead = false;
        }
    }

    private Vector3 tmp = new Vector3();
    private Vector2 tmp2 = new Vector2();

    private QueryCallback queryCallback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if (fixracket.testPoint(toUnits(tmp.x), toUnits(tmp.y))) {
                Body body = fixracket.getBody();
                jointDef.bodyB = body;
                jointDef.target.set(toUnits(tmp.x), toUnits(tmp.y));
                mouseJoint = (MouseJoint) mWorld.createJoint(jointDef);
            }

            return true;
        }
    };

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (mouseJoint != null) {
            mWorld.destroyJoint(mouseJoint);
            mouseJoint = null;
        }
        mCamera.unproject(tmp.set(screenX, screenY, 0));
        mWorld.QueryAABB(queryCallback, toUnits(tmp.x), toUnits(tmp.y), toUnits(tmp.x), toUnits(tmp.y));
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (mouseJoint != null) {
            mCamera.unproject(tmp.set(screenX, screenY, 0));
            mouseJoint.setTarget(tmp2.set(toUnits(tmp.x), toUnits(tmp.y)));
        }
        return true;
    }

    public Texture getBallTexture() {
        return ballTexture;
    }

    public Texture getRacketTexture() {
        return racketTexture;
    }

    public float changeToUnits(float pixels) {
        return toUnits(pixels);
    }

    @Override
    public void loadResources(AssetManager assetManager) {
        assetManager.load("images/sport/tennis/ball.png", Texture.class);
        assetManager.load("images/sport/tennis/racket.png", Texture.class);
        assetManager.load("images/sport/ground.png", Texture.class);
    }

    @Override
    public void loadResourcesComplete(AssetManager assetManager) {
        groundTexture = assetManager.get("images/sport/ground.png");
        ballTexture = assetManager.get("images/sport/tennis/ball.png");
        racketTexture = assetManager.get("images/sport/tennis/racket.png");
        createWorldBody();
    }

    @Override
    public void unloadResources(AssetManager assetManager) {
        assetManager.unload("images/sport/ground.png");
        assetManager.unload("images/sport/tennis/ball.png");
        assetManager.unload("images/sport/tennis/racket.png");
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    @Override
    public boolean keyDown(int keyCode) {
        if ((keyCode == Input.Keys.ESCAPE) || (keyCode == Input.Keys.BACK)) {
            Scene matchScene = new MatchScene();
            Scene loadingScene = new LoadingScene(matchScene);
            matchScene.setInTransition(new NoTransition(loadingScene));
            loadingScene.setInTransition(new NoTransition(this));
            getGame().setScene(loadingScene);
            //getGame().setScene(new LoadingScene(new MatchScene()));
        }
        return false;
    }
}
