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

import java.util.ArrayList;

public class BoxingScene extends PhysicScene {

    private static final float PIXEL_PER_METER = 40;
    //	public final float WORLD_WIDTH = 40;
//	public final float WORLD_HEIGHT = 24;
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
    private Body punchingBag1, punchingBag2, punchingBag3;
    private Fixture fixPaddle1, fixPaddle2, fixPaddle3;
    ;
    private Array<Body> arrBody = new Array<Body>();
    private Sprite spritePunchingBag1, spritePunchingBag2, spritePunchingBag3;
    private Texture texturePunchingBoxRed;
    private Texture texturePunchingBoxYellow;
    private Texture texturePunchingBoxGreen;
    private Texture texturePunchingBoxBlue;
    private Texture texturePunchingBoxPurple;
    private Texture textureField;

    private int punchingBag1isMovingStatus; // 1:Down  0:reset status  -1:Up
    private int punchingBag2isMovingStatus; // 1:Down  0:reset status  -1:Up
    private int punchingBag3isMovingStatus; // 1:Down  0:reset status  -1:Up
    private int correct;

    private int colorAns = -1;
    private int punchingBag1color;
    private int punchingBag2color;
    private int punchingBag3color;
    private ArrayList<String> colors;
    private ArrayList<Texture> punchingBoxs;
    private boolean readyAns;

    private final float movingSpeedY = WORLD_HEIGHT / 50;

    public BoxingScene() {
        super(PIXEL_PER_METER);

        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void loadResources(AssetManager assetManager) {
        assetManager.load("images/sport/boxing/punching_box_red.png", Texture.class);
        assetManager.load("images/sport/boxing/punching_box_yellow.png", Texture.class);
        assetManager.load("images/sport/boxing/punching_box_green.png", Texture.class);
        assetManager.load("images/sport/boxing/punching_box_blue.png", Texture.class);
        assetManager.load("images/sport/boxing/punching_box_purple.png", Texture.class);
        assetManager.load("images/sport/boxing/ring.png", Texture.class);
    }

    @Override
    public void loadResourcesComplete(AssetManager assetManager) {
        texturePunchingBoxRed = assetManager.get("images/sport/boxing/punching_box_red.png", Texture.class);
        texturePunchingBoxYellow = assetManager.get("images/sport/boxing/punching_box_yellow.png", Texture.class);
        texturePunchingBoxGreen = assetManager.get("images/sport/boxing/punching_box_green.png", Texture.class);
        texturePunchingBoxBlue = assetManager.get("images/sport/boxing/punching_box_blue.png", Texture.class);
        texturePunchingBoxPurple = assetManager.get("images/sport/boxing/punching_box_purple.png", Texture.class);
        textureField = assetManager.get("images/sport/boxing/ring.png", Texture.class);

        batch = new SpriteBatch();

        world = getWorld();
        setDebugBox2d(true);
        createCamera();
        createFont();
        createWorldEdge();
        setColorsAndPunchingBoxs();
        createPunchingBox();
        createJoint();

        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void unloadResources(AssetManager assetManager) {
        assetManager.unload("images/sport/boxing/punching_box_red.png");
        assetManager.unload("images/sport/boxing/punching_box_yellow.png");
        assetManager.unload("images/sport/boxing/punching_box_green.png");
        assetManager.unload("images/sport/boxing/punching_box_blue.png");
        assetManager.unload("images/sport/boxing/punching_box_purple.png");
        assetManager.unload("images/sport/boxing/ring.png");
    }

    @Override
    public void render(float elapsedSeconds) {
        super.render(elapsedSeconds);

        //Gdx.gl.glClearColor(0, 0, 0, 1);
        //Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        batch.draw(textureField, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);

        updateBodyAndSprite();

        font.draw(batch, Integer.toString(correct), setX(PIXEL_PER_METER), setY(WORLD_HEIGHT - PIXEL_PER_METER));
        font.draw(batch, (colorAns == -1) ? "" : colors.get(colorAns), setX(WORLD_WIDTH - 8 * PIXEL_PER_METER), setY(WORLD_HEIGHT - PIXEL_PER_METER));

        batch.end();

    }

    private void updateBodyAndSprite() {
        world.getBodies(arrBody);
        for (Body body : arrBody) {
            Sprite sprite = (Sprite) body.getUserData();
            if (sprite != null) {
                if (sprite == spritePunchingBag1) {
                    if (punchingBag1isMovingStatus == 1) {
                        if (body.getPosition().y < toUnits(setY(WORLD_HEIGHT / 2))) {
                            body.setLinearVelocity(0, 0);
                            setColor();
                            punchingBag1isMovingStatus = 0;
                        }
                    } else if (punchingBag1isMovingStatus == -1) {
                        if (body.getPosition().y > toUnits(setY(WORLD_HEIGHT * 2))) {
                            body.setLinearVelocity(0, 0);
                            clearColor();
                            setPunchingBag1Sprite();
                            punchingBag1isMovingStatus = 0;
                            showPunchingBox1();
                        }
                    }

                    sprite.setPosition(toPixels(body.getPosition().x) - sprite.getWidth() / 2, toPixels(body.getPosition().y) - sprite.getHeight() / 2);
                    sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
                    sprite.draw(batch);
                } else if (sprite == spritePunchingBag2) {
                    if (punchingBag2isMovingStatus == 1) {
                        if (body.getPosition().y < toUnits(setY(WORLD_HEIGHT / 2) - PIXEL_PER_METER)) {
                            body.setLinearVelocity(0, 0);
                            setColor();
                            punchingBag2isMovingStatus = 0;
                        }
                    } else if (punchingBag2isMovingStatus == -1) {
                        if (body.getPosition().y > toUnits(setY(WORLD_HEIGHT * 2))) {
                            body.setLinearVelocity(0, 0);
                            clearColor();
                            setPunchingBag2Sprite();
                            punchingBag2isMovingStatus = 0;
                            showPunchingBox2();
                        }
                    }

                    sprite.setPosition(toPixels(body.getPosition().x) - sprite.getWidth() / 2, toPixels(body.getPosition().y) - sprite.getHeight() / 2);
                    sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
                    sprite.draw(batch);
                } else if (sprite == spritePunchingBag3) {
                    if (punchingBag3isMovingStatus == 1) {
                        if (body.getPosition().y < toUnits(setY(WORLD_HEIGHT / 2))) {
                            body.setLinearVelocity(0, 0);
                            setColor();
                            punchingBag3isMovingStatus = 0;
                        }
                    } else if (punchingBag3isMovingStatus == -1) {
                        if (body.getPosition().y > toUnits(setY(WORLD_HEIGHT * 2))) {
                            body.setLinearVelocity(0, 0);
                            clearColor();
                            setPunchingBag3Sprite();
                            punchingBag3isMovingStatus = 0;
                            showPunchingBox3();
                        }
                    }

                    sprite.setPosition(toPixels(body.getPosition().x) - sprite.getWidth() / 2, toPixels(body.getPosition().y) - sprite.getHeight() / 2);
                    sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
                    sprite.draw(batch);
                }
            }
        }
    }

    private Vector3 tmp = new Vector3();

    private QueryCallback queryCallback = new QueryCallback() {
        @Override
        public boolean reportFixture(Fixture fixture) {
            if (fixPaddle1.testPoint(tmp.x, tmp.y)) {
                if (punchingBag1color == colorAns) {
                    correct++;
                }
                resetPunchingBox1();
            }
            if (fixPaddle2.testPoint(tmp.x, tmp.y)) {
                if (punchingBag2color == colorAns) {
                    correct++;
                }
                resetPunchingBox2();
            }
            if (fixPaddle3.testPoint(tmp.x, tmp.y)) {
                if (punchingBag3color == colorAns) {
                    correct++;
                }
                resetPunchingBox3();
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
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
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

    private void createCamera() {
//		camera = new OrthographicCamera(toUnits(WORLD_WIDTH), toUnits(WORLD_HEIGHT));
        camera = (OrthographicCamera) getViewport().getCamera();
//		camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);
        camera.update();
    }

    private void createFont() {

//		getRootLayer().addActor(new Label());

        FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(
                Gdx.files.internal("fonts/Roboto-Bold.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter param
                = new FreeTypeFontGenerator.FreeTypeFontParameter();
        param.size = 80;
        param.color = Color.YELLOW;

        font = fontGenerator.generateFont(param);

        for (TextureRegion region : font.getRegions()) {
            region.getTexture().setFilter(Texture.TextureFilter.Linear,
                    Texture.TextureFilter.Linear);
        }
        fontGenerator.dispose();
//		font = new BitmapFont(Gdx.files.internal("data/font/MyFont.fnt"), Gdx.files.internal("data/font/MyFont.png"), false);
//		font.getData().setScale(2f);
//		font.setColor(Color.YELLOW);
    }

    private void createWorldEdge() {
        BodyDef bodyGroundDef = new BodyDef();
        bodyGroundDef.type = BodyType.StaticBody;
        bodyGroundDef.position.set(toUnits(setX(0f)), toUnits(setY(0f)));
        bodyGround = world.createBody(bodyGroundDef);

        EdgeShape edgeShape = new EdgeShape();
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = edgeShape;
        edgeShape.set(new Vector2(toUnits(0f), toUnits(0f)), new Vector2(toUnits(0f), toUnits(WORLD_HEIGHT)));
        bodyGround.createFixture(fixtureDef);
        edgeShape.set(new Vector2(toUnits(WORLD_WIDTH), toUnits(0f)), new Vector2(toUnits(WORLD_WIDTH), toUnits(WORLD_HEIGHT)));
        bodyGround.createFixture(fixtureDef);
    }

    private void createPunchingBox() {
        // body
        BodyDef paddleBodyDef1 = new BodyDef();
        paddleBodyDef1.type = BodyType.DynamicBody;
        paddleBodyDef1.position.set(toUnits(setX(WORLD_WIDTH * 1 / 4)), toUnits(setY(WORLD_HEIGHT)));
        punchingBag1 = world.createBody(paddleBodyDef1);
        BodyDef paddleBodyDef2 = new BodyDef();
        paddleBodyDef2.type = BodyType.DynamicBody;
        paddleBodyDef2.position.set(toUnits(setX(WORLD_WIDTH * 2 / 4)), toUnits(setY(WORLD_HEIGHT)));
        punchingBag2 = world.createBody(paddleBodyDef2);
        BodyDef paddleBodyDef3 = new BodyDef();
        paddleBodyDef3.type = BodyType.DynamicBody;
        paddleBodyDef3.position.set(toUnits(setX(WORLD_WIDTH * 3 / 4)), toUnits(setY(WORLD_HEIGHT)));
        punchingBag3 = world.createBody(paddleBodyDef3);

        PolygonShape paddleShape = new PolygonShape();
        paddleShape.setAsBox(toUnits(WORLD_WIDTH / 20), toUnits(WORLD_WIDTH / 5));

        FixtureDef paddleShapeDef = new FixtureDef();
        paddleShapeDef.shape = paddleShape;
        paddleShapeDef.density = 1f;
        paddleShapeDef.friction = 0f;
        paddleShapeDef.restitution = 0.5f;
        fixPaddle1 = punchingBag1.createFixture(paddleShapeDef);
        fixPaddle2 = punchingBag2.createFixture(paddleShapeDef);
        fixPaddle3 = punchingBag3.createFixture(paddleShapeDef);

        punchingBag1.setGravityScale(0f);
        punchingBag1.setAngularVelocity(0);
        punchingBag1.setFixedRotation(true);
        punchingBag1.setBullet(false);

        punchingBag2.setGravityScale(0f);
        punchingBag2.setAngularVelocity(0);
        punchingBag2.setFixedRotation(true);
        punchingBag2.setBullet(false);

        punchingBag3.setGravityScale(0f);
        punchingBag3.setAngularVelocity(0);
        punchingBag3.setFixedRotation(true);
        punchingBag3.setBullet(false);

        // sprite
        setPunchingBag1Sprite();
        setPunchingBag2Sprite();
        setPunchingBag3Sprite();

        showPunchingBox1();
        showPunchingBox2();
        showPunchingBox3();
    }

    private void setPunchingBag1Sprite() {
        punchingBag1color = (int) (Math.random() * punchingBoxs.size());
        spritePunchingBag1 = new Sprite(punchingBoxs.get(punchingBag1color));
        spritePunchingBag1.setSize(WORLD_WIDTH / 10, WORLD_WIDTH / 2.5f);
        punchingBag1.setUserData(spritePunchingBag1);
    }

    private void setPunchingBag2Sprite() {
        punchingBag2color = (int) (Math.random() * punchingBoxs.size());
        spritePunchingBag2 = new Sprite(punchingBoxs.get(punchingBag2color));
        spritePunchingBag2.setSize(WORLD_WIDTH / 10, WORLD_WIDTH / 2.5f);
        punchingBag2.setUserData(spritePunchingBag2);
    }

    private void setPunchingBag3Sprite() {
        punchingBag3color = (int) (Math.random() * punchingBoxs.size());
        spritePunchingBag3 = new Sprite(punchingBoxs.get(punchingBag3color));
        spritePunchingBag3.setSize(WORLD_WIDTH / 10, WORLD_WIDTH / 2.5f);
        punchingBag3.setUserData(spritePunchingBag3);
    }

    private void createJoint() {
        // mouse joint
        jointDef = new MouseJointDef();
        jointDef.bodyA = bodyGround;
        jointDef.collideConnected = true;
        jointDef.maxForce = 1000.0f * punchingBag1.getMass();

        PrismaticJointDef prismaticJointDef = new PrismaticJointDef();
        Vector2 axis = new Vector2(toUnits(0f), toUnits(1f));
        prismaticJointDef.collideConnected = true;
        prismaticJointDef.initialize(bodyGround, punchingBag1,
                punchingBag1.getWorldCenter(), axis);
        world.createJoint(prismaticJointDef);

        prismaticJointDef.initialize(bodyGround, punchingBag2,
                punchingBag2.getWorldCenter(), axis);
        world.createJoint(prismaticJointDef);

        prismaticJointDef.initialize(bodyGround, punchingBag3,
                punchingBag3.getWorldCenter(), axis);
        world.createJoint(prismaticJointDef);
    }

    private void showPunchingBox1() {
        punchingBag1isMovingStatus = 1;
        punchingBag1.setLinearVelocity(0, -movingSpeedY);
    }

    private void showPunchingBox2() {
        punchingBag2isMovingStatus = 1;
        punchingBag2.setLinearVelocity(0, -movingSpeedY);
    }

    private void showPunchingBox3() {
        punchingBag3isMovingStatus = 1;
        punchingBag3.setLinearVelocity(0, -movingSpeedY);
    }

    private void resetPunchingBox1() {
        punchingBag1isMovingStatus = -1;
        punchingBag1.setLinearVelocity(0, movingSpeedY * 2);
    }

    private void resetPunchingBox2() {
        punchingBag2isMovingStatus = -1;
        punchingBag2.setLinearVelocity(0, movingSpeedY * 2);
    }

    private void resetPunchingBox3() {
        punchingBag3isMovingStatus = -1;
        punchingBag3.setLinearVelocity(0, movingSpeedY * 2);
    }

    private void setColorsAndPunchingBoxs() {
        colors = new ArrayList<String>();
        colors.add("RED");
        colors.add("YELLOW");
        colors.add("GREEN");
        colors.add("BLUE");
        colors.add("PURPLE");

        punchingBoxs = new ArrayList<Texture>();
        punchingBoxs.add(texturePunchingBoxRed);
        punchingBoxs.add(texturePunchingBoxYellow);
        punchingBoxs.add(texturePunchingBoxGreen);
        punchingBoxs.add(texturePunchingBoxBlue);
        punchingBoxs.add(texturePunchingBoxPurple);
    }

    private void setColor() {
        if (!readyAns) {
            colorAns = (int) (Math.random() * colors.size());
            checkAnsColor();
        }
    }

    private void checkAnsColor() {
        if ((colorAns != punchingBag1color) &&
                (colorAns != punchingBag2color) &&
                (colorAns != punchingBag3color)) {
            setColor();
        } else {
            readyAns = true;
        }
    }

    private void clearColor() {
        if (readyAns) {
            colorAns = -1;
            readyAns = false;
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
            //getGame().setScene(new LoadingScene(new MatchScene()));
        }
        return false;
    }
}
