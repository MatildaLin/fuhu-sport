package com.fuhu.gdx.sprite;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.*;
import com.badlogic.gdx.utils.Array;
import com.fuhu.gdx.sport.BicycleScene;

/**
 * Created by sabrinakuo on 2015/10/1.
 */
public class Bicycle {
    private float WHEEL_RADIUS = 150f;
    private float CHAIN_WHEEL_RADIUS = 45f;
    private Body groundBody, rearWheelBody, frontWheelBody, chainWheelBody, frameBody, axialBody, crankBody, pedalBody;
    private RevoluteJoint axialJoint;
    private World mWorld;
    private SpriteBatch batch;
    private OrthographicCamera mCamera;
    private BicycleScene mScene;
    private Array<Body> bodies = new Array<Body>();

//    private Vector2 crankAnchor;

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
        WheelBodyDef.position.set(scene.changeToUnits(sceneCenter - 250), scene.changeToUnits(groundHight + WHEEL_RADIUS));
        rearWheelBody = world.createBody(WheelBodyDef);

        Sprite rearWheel = new Sprite(scene.getWheelTexture());
        rearWheel.setSize(WHEEL_RADIUS*2, WHEEL_RADIUS*2);
        rearWheelBody.setUserData(rearWheel);

        CircleShape wheelShape = new CircleShape();
        wheelShape.setRadius(scene.changeToUnits(WHEEL_RADIUS));
        FixtureDef wheelFixtureDef = new FixtureDef();
        wheelFixtureDef.shape = wheelShape;
        wheelFixtureDef.density = 3.0f;
        rearWheelBody.createFixture(wheelFixtureDef);

        /** front wheel */
        WheelBodyDef.position.set(scene.changeToUnits(sceneCenter + 250), scene.changeToUnits(groundHight + WHEEL_RADIUS));
        frontWheelBody = world.createBody(WheelBodyDef);

        Sprite frontWheel = new Sprite(scene.getWheelTexture());
        frontWheel.setSize(WHEEL_RADIUS * 2, WHEEL_RADIUS * 2);
        frontWheelBody.setUserData(frontWheel);
        frontWheelBody.createFixture(wheelFixtureDef);

        /** bicycle frame */
        BodyDef bicycleMainDef = new BodyDef();
        bicycleMainDef.type = BodyDef.BodyType.StaticBody;

        bicycleMainDef.position.set(scene.changeToUnits(sceneCenter), scene.changeToUnits(groundHight + 331));
        frameBody = world.createBody(bicycleMainDef);

        PolygonShape mainShape = new PolygonShape();
        mainShape.setAsBox(scene.changeToUnits(WHEEL_RADIUS*1.7f), scene.changeToUnits(WHEEL_RADIUS*1.4f));
        Sprite frame = new Sprite(scene.getFrameTexture());
        frame.setSize(frame.getWidth()/5, frame.getHeight()/5);
        frameBody.setUserData(frame);

        FixtureDef bicycleFixtureDef = new FixtureDef();
        bicycleFixtureDef.shape = mainShape;
        bicycleFixtureDef.density = 3.0f;
        frameBody.createFixture(bicycleFixtureDef);

        mainShape.dispose();

        /** chain wheel */
        WheelBodyDef.position.set(scene.changeToUnits(sceneCenter - 38), scene.changeToUnits(groundHight + WHEEL_RADIUS));
        chainWheelBody = world.createBody(WheelBodyDef);

        Sprite chainWheel = new Sprite(scene.getChainwheelTexture());
        chainWheel.setSize(CHAIN_WHEEL_RADIUS*2, CHAIN_WHEEL_RADIUS*2);
        chainWheelBody.setUserData(chainWheel);

        wheelShape.setRadius(scene.changeToUnits(CHAIN_WHEEL_RADIUS));
        chainWheelBody.createFixture(wheelFixtureDef);

        wheelShape.dispose();

        /** Axial */
        BodyDef axialDef = new BodyDef();
        axialDef.type = BodyDef.BodyType.DynamicBody;
        axialDef.position.set(scene.changeToUnits(sceneCenter - 38), scene.changeToUnits(groundHight + WHEEL_RADIUS));
        axialBody = world.createBody(axialDef);

        CircleShape axialShape = new CircleShape();
        axialShape.setRadius(scene.changeToUnits(CHAIN_WHEEL_RADIUS));

        FixtureDef axialFixtureDef = new FixtureDef();
        axialFixtureDef.shape = axialShape;
        axialFixtureDef.density = 3.0f;

        axialBody.createFixture(axialFixtureDef);

        axialShape.dispose();

        /** crank */
        BodyDef crankDef = new BodyDef();
        crankDef.type = BodyDef.BodyType.KinematicBody;

        Sprite crank = new Sprite(scene.getCrankTexture());
        crank.setSize(crank.getWidth() / 5, crank.getHeight() / 5);

        float crank_x = chainWheelBody.getPosition().x;
        float crank_y = chainWheelBody.getPosition().y - scene.changeToUnits(crank.getHeight()/2);

        crankDef.position.set(crank_x, crank_y);
        crankBody = world.createBody(crankDef);
        crankBody.setUserData(crank);
        crankBody.setGravityScale(0f);

        PolygonShape crankShape = new PolygonShape();
        float crank_width = scene.changeToUnits(crank.getWidth()/2);
        float crank_height = scene.changeToUnits(crank.getHeight() / 2);
        crankShape.setAsBox(crank_width, crank_height);

        FixtureDef crankFixtureDef = new FixtureDef();
        crankFixtureDef.shape = crankShape;
        crankFixtureDef.density = 3.0f;

        crankBody.createFixture(crankFixtureDef);
//        crankAnchor = new Vector2(crankBody.getWorldCenter().x, crankBody.getWorldCenter().y + scene.changeToUnits(50));

        crankShape.dispose();

        /** pedal */
        BodyDef pedalDef = new BodyDef();
        pedalDef.type = BodyDef.BodyType.KinematicBody;

        Sprite pedal = new Sprite(scene.getPedalTexture());
        pedal.setSize(pedal.getWidth() / 5, pedal.getHeight() / 5);

        pedalDef.position.set(crank_x + crank_width, crank_y - crank_height);
        pedalBody = world.createBody(pedalDef);
        pedalBody.setUserData(pedal);

        PolygonShape pedalShape = new PolygonShape();
        pedalShape.setAsBox(scene.changeToUnits(pedal.getWidth() / 2), scene.changeToUnits(pedal.getHeight() / 2));

        FixtureDef pedalFixtureDef = new FixtureDef();
        pedalFixtureDef.shape = pedalShape;
        pedalFixtureDef.density =3.0f;
        pedalFixtureDef.filter.categoryBits = 2;
        pedalFixtureDef.filter.maskBits = 1;
        pedalBody.createFixture(pedalFixtureDef);

        pedalShape.dispose();

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
        RevoluteJoint revoluteJoint3 = (RevoluteJoint) world.createJoint(revoluteJointDef3);

        // create axial revolute joint
        RevoluteJointDef axialRevoluteDef = new RevoluteJointDef();
        axialRevoluteDef.initialize(groundBody, axialBody, axialBody.getWorldCenter());
        axialRevoluteDef.enableMotor = true;
        axialJoint = (RevoluteJoint) world.createJoint(axialRevoluteDef);

//        // create crank revolute joint
//        RevoluteJointDef crankrevoluteDef = new RevoluteJointDef();
//        crankrevoluteDef.initialize(chainWheelBody, crankBody, crankAnchor);
//        crankrevoluteDef.enableMotor =true;
//        RevoluteJoint crankJoint = (RevoluteJoint) world.createJoint(crankrevoluteDef);

//        // create pedal revolute joint
//        RevoluteJointDef pedalrevoluteDef = new RevoluteJointDef();
//        pedalrevoluteDef.initialize(chainWheelBody, pedalBody, pedalBody.getWorldCenter());
//        pedalrevoluteDef.enableMotor = true;
//        RevoluteJoint pedalJoint = (RevoluteJoint) world.createJoint(pedalrevoluteDef);

        // create gear joint
        GearJointDef gearJointDef = new GearJointDef();
        gearJointDef.bodyA = chainWheelBody;
        gearJointDef.bodyB = rearWheelBody;
        gearJointDef.joint1 = axialJoint;
        gearJointDef.joint2 = revoluteJoint1;
        gearJointDef.ratio = 3f;
        world.createJoint(gearJointDef);

        // create gear joint to active mCircle1 and mCircle2
        GearJointDef gearJointDef2 = new GearJointDef();
        gearJointDef2.bodyA = chainWheelBody;
        gearJointDef2.bodyB = frontWheelBody;
        gearJointDef2.joint1 = axialJoint;
        gearJointDef2.joint2 = revoluteJoint2;
        gearJointDef2.ratio = 3f;
        world.createJoint(gearJointDef2);

        // create gear joint to active mCircle1 and mCircle2
        GearJointDef gearJointDef3 = new GearJointDef();
        gearJointDef3.bodyA = axialBody;
        gearJointDef3.bodyB = chainWheelBody;
        gearJointDef3.joint1 = axialJoint;
        gearJointDef3.joint2 = revoluteJoint3;
        gearJointDef3.ratio = 2f;
        world.createJoint(gearJointDef3);

    }

    public RevoluteJoint getRevoluteJoint() {
        return axialJoint;
    }

    public void update() {

        batch.setProjectionMatrix(mCamera.combined);
        batch.begin();
        batch.draw(mScene.getBgTextrue(), 0, 0, mCamera.viewportWidth, mCamera.viewportHeight);
        mWorld.getBodies(bodies);
        for (Body b : bodies) {
            // get body's user data
            Sprite sprite = (Sprite) b.getUserData();

            if (sprite != null) {
                if (!b.equals(frameBody)) {
                    if (b.equals(chainWheelBody)){
                        sprite.setOrigin(CHAIN_WHEEL_RADIUS, CHAIN_WHEEL_RADIUS);
                    }else if (b.equals(rearWheelBody) || b.equals(frontWheelBody)){
                        sprite.setOrigin(WHEEL_RADIUS, WHEEL_RADIUS);
                    }else if (b.equals(crankBody)){
                        sprite.setOrigin(b.getWorldCenter().x, b.getWorldCenter().y);
                    }else if (b.equals(pedalBody)){
                        sprite.setOrigin(b.getWorldCenter().x, b.getWorldCenter().y);
                    }

                    sprite.setRotation(MathUtils.radiansToDegrees * b.getAngle());
                }
                sprite.setPosition(mScene.changeToPixels(b.getPosition().x) - sprite.getWidth() / 2, mScene.changeToPixels(b.getPosition().y) - sprite.getHeight() / 2);
                sprite.draw(batch);
            }
        }
        batch.end();
    }
}
