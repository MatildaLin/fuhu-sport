package com.fuhu.gdx.sprite;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.GearJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.utils.Array;
import com.fuhu.gdx.sport.BicycleScene;

/**
 * Created by sabrinakuo on 2015/10/1.
 */
public class Bicycle {
    private float WHEEL_RADIUS = 0.5f;
    private float CHAIN_WHEEL_RADIUS = 0.5f;
    private Body groundBody, rearWheelBody, frontWheelBody, chainWheelBody, skeletonBody;
    private RevoluteJoint revoluteJoint3;
    private World mWorld;
    private SpriteBatch batch;
    private OrthographicCamera mCamera;
    private BicycleScene mScene;
    private Array<Body> bodies = new Array<Body>();

    public Bicycle(BicycleScene scene, World world) {

        this.mWorld = world;
        this.mScene = scene;
        this.mCamera = (OrthographicCamera) scene.getCamera();
        mCamera.position.set(mCamera.viewportWidth / 2, mCamera.viewportHeight / 2, 0);
        mCamera.update();
        batch = new SpriteBatch();

        float sceneCenter = scene.getCamera().viewportWidth / 2;
        float groundHight = scene.getGROUND_Y_HEIGHT();

        /** ground */
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);
        groundBody = world.createBody(bodyDef);

        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(new Vector2(0, scene.changeToUnits(groundHight)), new Vector2(scene.changeToUnits(sceneCenter * 2), scene.changeToUnits(groundHight)));
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = edgeShape;
        groundBody.createFixture(fixtureDef);

        edgeShape.dispose();

        /** rear wheel */
        BodyDef WheelBodyDef = new BodyDef();
        WheelBodyDef.type = BodyDef.BodyType.DynamicBody;
        WheelBodyDef.position.set(scene.changeToUnits(sceneCenter - 230), scene.changeToUnits(groundHight * 2));
        rearWheelBody = world.createBody(WheelBodyDef);

        Sprite rearWheel = new Sprite(scene.getWheelTexture());
        rearWheel.setSize(200, 200);
        rearWheelBody.setUserData(rearWheel);

        CircleShape wheelShape = new CircleShape();
        wheelShape.setRadius(WHEEL_RADIUS);
        FixtureDef wheelFixtureDef = new FixtureDef();
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.density = 3.0f;
        rearWheelBody.createFixture(wheelFixtureDef);

        /** front wheel */
        WheelBodyDef.position.set(scene.changeToUnits(sceneCenter + 270), scene.changeToUnits(groundHight * 2));
        frontWheelBody = world.createBody(WheelBodyDef);

        Sprite frontWheel = new Sprite(scene.getWheelTexture());
        frontWheel.setSize(200, 200);
        frontWheelBody.setUserData(frontWheel);
        frontWheelBody.createFixture(wheelFixtureDef);

        /** chain wheel */
        WheelBodyDef.position.set(scene.changeToUnits(sceneCenter), scene.changeToUnits(groundHight * 2));
        chainWheelBody = world.createBody(WheelBodyDef);

        wheelShape.setRadius(CHAIN_WHEEL_RADIUS);
        chainWheelBody.createFixture(wheelFixtureDef);

        wheelShape.dispose();

        /** bicycle skeleton */
        BodyDef bicycleMainDef = new BodyDef();
        bicycleMainDef.type = BodyDef.BodyType.StaticBody;
        bicycleMainDef.position.set(scene.changeToUnits(sceneCenter), scene.changeToUnits(300 + groundHight));
        skeletonBody = world.createBody(bicycleMainDef);

        PolygonShape mainShape = new PolygonShape();
        mainShape.setAsBox(WHEEL_RADIUS * 4, WHEEL_RADIUS * 4);
        Sprite skeleton = new Sprite(scene.getSkeletonTexture());
        skeleton.setSize(skeleton.getWidth() * 2, skeleton.getHeight() * 2);
        skeletonBody.setUserData(skeleton);

        FixtureDef bicycleFixtureDef = new FixtureDef();
        bicycleFixtureDef.shape = mainShape;
        bicycleFixtureDef.density = 3.0f;
        skeletonBody.createFixture(bicycleFixtureDef);

        mainShape.dispose();

        createJoint(world);
    }

    private void createJoint(World world) {
        // create revolute joint
        RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
        revoluteJointDef.initialize(groundBody, rearWheelBody, rearWheelBody.getWorldCenter());
        RevoluteJoint revoluteJoint1 = (RevoluteJoint) world.createJoint(revoluteJointDef);

        // create second revolute joint
        RevoluteJointDef revoluteJointDef2 = new RevoluteJointDef();
        revoluteJointDef2.initialize(groundBody, frontWheelBody, frontWheelBody.getWorldCenter());
        RevoluteJoint revoluteJoint2 = (RevoluteJoint) world.createJoint(revoluteJointDef2);

        // create third revolute joint
        RevoluteJointDef revoluteJointDef3 = new RevoluteJointDef();
        revoluteJointDef3.initialize(groundBody, chainWheelBody, chainWheelBody.getWorldCenter());
        revoluteJointDef3.enableMotor = true;
        revoluteJoint3 = (RevoluteJoint) world.createJoint(revoluteJointDef3);

        // create gear joint
        GearJointDef gearJointDef = new GearJointDef();
        gearJointDef.bodyA = chainWheelBody;
        gearJointDef.bodyB = rearWheelBody;
        gearJointDef.joint1 = revoluteJoint3;
        gearJointDef.joint2 = revoluteJoint1;
        gearJointDef.ratio = 2f;
        world.createJoint(gearJointDef);

        // create gear joint to active mCircle1 and mCircle2
        GearJointDef gearJointDef2 = new GearJointDef();
        gearJointDef2.bodyA = rearWheelBody;
        gearJointDef2.bodyB = frontWheelBody;
        gearJointDef2.joint1 = revoluteJoint3;
        gearJointDef2.joint2 = revoluteJoint2;
        gearJointDef2.ratio = 2f;
        world.createJoint(gearJointDef2);
    }

    public RevoluteJoint getRevoluteJoint() {
        return revoluteJoint3;
    }

    public void update() {

        batch.setProjectionMatrix(mCamera.combined);
        batch.begin();
        mWorld.getBodies(bodies);
        for (Body b : bodies) {
            // get body's user data
            Sprite sprite = (Sprite) b.getUserData();

            if (sprite != null) {
                if (!b.equals(skeletonBody)) {
                    sprite.setOrigin(100, 100);
                    sprite.setRotation(MathUtils.radiansToDegrees * b.getAngle());
                }
                sprite.setPosition(mScene.changeToPixels(b.getPosition().x) - sprite.getWidth() / 2, mScene.changeToPixels(b.getPosition().y) - sprite.getHeight() / 2);
                sprite.draw(batch);
            }
        }
        batch.end();
    }
}
