package com.fuhu.gdx.sport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.utils.Array;
import com.fuhu.gdx.game.LoadingScene;
import com.fuhu.gdx.game.MatchScene;
import com.fuhu.gdx.physic.PhysicScene;
import com.fuhu.gdx.scene.Scene;
import com.fuhu.gdx.scene.transition.NoTransition;

public class BaseballScene extends PhysicScene {

    private static final float PIXEL_PER_METER = 40;
    public final float WORLD_WIDTH = 1920;
    public final float WORLD_HEIGHT = 1128;
    public final float BACKGROUND_WIDTH = 2280;
    public final float BACKGROUND_HEIGHT = 1440;

    private BitmapFont font;
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private World world;
    private MouseJointDef jointDef;
    private MouseJoint mouseJoint;

    private Body bodyGround;
    private Body paddle;
    private Body ball;
    private BodyDef bodyBoxDef;
    private Fixture fixPaddle;
    private Array<Body> arrBody = new Array<Body>();
    private Sprite spriteGloves;
    private Sprite spriteBall;
    private Texture textureGloves;
    private Texture textureField;
    private Texture textureBall;
	private String pathGloves = "images/sport/baseball/gloves.png";
	private String pathBaseballField = "images/sport/baseball/baseball_field.jpg";
	private String pathBaseball = "images/sport/baseball/baseball.png";

    private boolean resetBall;
    private int catchBalls;
    private int balls;

    public BaseballScene() {
        super(PIXEL_PER_METER);

        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void loadResources(AssetManager assetManager) {
        assetManager.load(pathGloves, Texture.class);
        assetManager.load(pathBaseballField, Texture.class);
        assetManager.load(pathBaseball, Texture.class);
    }

    @Override
    public void loadResourcesComplete(AssetManager assetManager) {
        textureGloves = assetManager.get(pathGloves, Texture.class);
        textureField = assetManager.get(pathBaseballField, Texture.class);
        textureBall = assetManager.get(pathBaseball, Texture.class);

        batch = new SpriteBatch();

        world = getWorld();
        setDebugBox2d(true);
        createCamera();
        createFont();
        createWorldEdge();
        createGloves();
        createBall();
        createJoint();
        setContactListener();

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void unloadResources(AssetManager assetManager) {
        assetManager.unload(pathGloves);
        assetManager.unload(pathBaseballField);
        assetManager.unload(pathBaseball);
    }

    @Override
    public void render(float elapsedSeconds) {
        super.render(elapsedSeconds);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(textureField, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        checkBallOutSide();
        updateBodyAndSprite();

        font.draw(batch, catchBalls + " / " + balls + "\n" + (balls != 0 ? ((float) catchBalls / balls) * 100 : 0) + " %",
                setX(PIXEL_PER_METER), setY(WORLD_HEIGHT - PIXEL_PER_METER));

        batch.end();

    }

    private void createCamera() {
        camera = (OrthographicCamera) getViewport().getCamera();
        camera.update();
    }

    private void createFont() {
        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/Roboto-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param
                = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 80;
        param.color = Color.BLUE;

        font = fontGenerator.generateFont(param);

        for (TextureRegion region : font.getRegions()) {
            region.getTexture().setFilter(Texture.TextureFilter.Linear,
                    Texture.TextureFilter.Linear);
        }
        fontGenerator.dispose();
    }

    private void checkBallOutSide() {
        if (ball.getPosition().y < 0) {
            resetBall = true;
        }
    }

    private void updateBodyAndSprite() {
        world.getBodies(arrBody);
        for (Body body : arrBody) {
            Sprite sprite = (Sprite) body.getUserData();
            if (sprite != null) {
                if (sprite == spriteGloves) {
                    sprite.setPosition(toPixels(body.getPosition().x) - sprite.getWidth() / 2, toPixels(body.getPosition().y) - sprite.getHeight() / 2);
                    sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
                    sprite.draw(batch);
                }

                if (sprite == spriteBall) {
                    if (!resetBall) {
                        sprite.setPosition(toPixels(body.getPosition().x) - sprite.getWidth() / 2, toPixels(body.getPosition().y) - sprite.getHeight() / 2);
                        sprite.setOrigin(sprite.getWidth() / 2, sprite.getHeight() / 2);
                        sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
                        sprite.draw(batch);
                    }
                    else {
                        resetPosition();
                    }

                }
            }
        }
    }

    private Vector3 tmp = new Vector3();
    private Vector2 tmp2 = new Vector2();

    private QueryCallback queryCallback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if (fixPaddle.testPoint(tmp.x, tmp.y)) {
                Body body = fixPaddle.getBody();
                jointDef.bodyB = fixPaddle.getBody();
                jointDef.target.set(body.getWorldCenter().x, body.getWorldCenter().y);
                mouseJoint = (MouseJoint) world.createJoint(jointDef);
            }

            return true;
        }
    };

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        camera.unproject(tmp.set(screenX, screenY, 0));
        tmp.x = toUnits(tmp.x);
        tmp.y = toUnits(tmp.y);
        world.QueryAABB(queryCallback, tmp.x, tmp.y, tmp.x, tmp.y);
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (mouseJoint != null) {
            world.destroyJoint(mouseJoint);
            mouseJoint = null;
        }

        paddle.setLinearVelocity(0, 0);

        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (mouseJoint != null) {
            camera.unproject(tmp.set(screenX, screenY, 0));
            tmp.x = toUnits(tmp.x);
            tmp.y = toUnits(tmp.y);
            mouseJoint.setTarget(tmp2.set(tmp.x, tmp.y));
        }

        return true;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        // TODO Auto-generated method stub
        return false;
    }

    private void setContactListener() {
        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                // TODO Auto-generated method stub
                resetBall = true;
                catchBalls++;
            }

            @Override
            public void endContact(Contact contact) {
                // TODO Auto-generated method stub
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                // TODO Auto-generated method stub
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
                // TODO Auto-generated method stub
            }
        });
    }

    private void createWorldEdge() {
        BodyDef bodyGroundDef = new BodyDef();
        bodyGroundDef.type = BodyType.StaticBody;
        bodyGroundDef.position.set(toUnits(setX(0)), -toUnits(PIXEL_PER_METER));
        bodyGround = world.createBody(bodyGroundDef);
    }

    private void createGloves() {
        // body
        BodyDef paddleBodyDef = new BodyDef();
        paddleBodyDef.type = BodyType.DynamicBody;
        paddleBodyDef.position.set(toUnits(setX(WORLD_WIDTH / 2)), toUnits(setY(WORLD_HEIGHT / 5)));
        paddle = world.createBody(paddleBodyDef);

        PolygonShape paddleShape = new PolygonShape();
        paddleShape.setAsBox(toUnits(WORLD_WIDTH / 20), toUnits(WORLD_WIDTH / 20));

        FixtureDef paddleShapeDef = new FixtureDef();
        paddleShapeDef.shape = paddleShape;
        paddleShapeDef.density = 10000f;
        paddleShapeDef.friction = 0f;
        paddleShapeDef.restitution = 0f;
        fixPaddle = paddle.createFixture(paddleShapeDef);

        paddle.setGravityScale(0f);
        paddle.setAngularVelocity(0);
        paddle.setFixedRotation(true);
        paddle.setBullet(false);

        // sprite
        spriteGloves = new Sprite(textureGloves);
        spriteGloves.setSize(WORLD_WIDTH / 10, WORLD_WIDTH / 10);
        paddle.setUserData(spriteGloves);
    }

    private void createJoint() {
        // mouse joint
        jointDef = new MouseJointDef();
        jointDef.bodyA = bodyGround;
        jointDef.collideConnected = true;
        jointDef.maxForce = 1000.0f * paddle.getMass();

        PrismaticJointDef prismaticJointDef = new PrismaticJointDef();
        Vector2 axis = new Vector2(1f, 0f);
        prismaticJointDef.collideConnected = true;
        prismaticJointDef.initialize(bodyGround, paddle,
                paddle.getWorldCenter(), axis);
        world.createJoint(prismaticJointDef);
    }

    private void createBall() {
        // body
        ball = world.createBody(setBodyDef());
        CircleShape shapeBox = new CircleShape();
        shapeBox.setRadius(toUnits(WORLD_WIDTH / 80));
        FixtureDef fixtureDefBox = new FixtureDef();
//		fixtureDefBox.density = 1;
        fixtureDefBox.friction = 0.2f;
        fixtureDefBox.shape = shapeBox;
        fixtureDefBox.restitution = 0.2f;
        ball.createFixture(fixtureDefBox);
        shapeBox.dispose();

        // sprite
        spriteBall = new Sprite(textureBall);
        spriteBall.setSize(WORLD_WIDTH / 40, WORLD_WIDTH / 40);
        ball.setUserData(spriteBall);
        ball.setAngularVelocity(-10);

    }

    private BodyDef setBodyDef() {
        bodyBoxDef = new BodyDef();
        bodyBoxDef.type = BodyType.DynamicBody;
        bodyBoxDef.position.set((float) (Math.random() * toUnits(WORLD_WIDTH)) + toUnits(setX(0f)), toUnits(setY(WORLD_HEIGHT)));

        return bodyBoxDef;
    }

    private void resetPosition() {
        if (resetBall) {
            ball.setLinearVelocity(0, 0);
            ball.setTransform((float) (Math.random() * toUnits(WORLD_WIDTH)) + toUnits(setX(0)), toUnits(setY(WORLD_HEIGHT)), 0);

            balls++;
            resetBall = false;
        }
    }

    private float setX(float x) {
        return getWorldX() + x;
    }

    private float setY(float y) {
        return getWorldY() + y;
    }

    @Override
    public boolean keyDown(int keyCode) {
        if ((keyCode == Input.Keys.ESCAPE) || (keyCode == Input.Keys.BACK)) {
            Scene matchScene = new MatchScene();
            Scene loadingScene = new LoadingScene(matchScene);
            matchScene.setInTransition(new NoTransition(loadingScene));
            loadingScene.setInTransition(new NoTransition(this));
            getGame().setScene(loadingScene);
//            getGame().setScene(new LoadingScene(new MatchScene()));
        }
        return false;
    }
}
