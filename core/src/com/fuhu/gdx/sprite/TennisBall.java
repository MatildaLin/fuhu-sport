package com.fuhu.gdx.sprite;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.fuhu.gdx.sport.TennisScene;

/**
 * Created by Sabrina uo on 2015/9/25.
 */
public class TennisBall {
    private Body ballBody;
    private OrthographicCamera camera;
    private float Radius = 30f;

    public TennisBall(TennisScene scene, World world) {

        camera = (OrthographicCamera) scene.getCamera();
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;

        float y = scene.changeToUnits(camera.viewportHeight / 2) * (1 + (float) Math.random());
        bodyDef.position.set(scene.changeToUnits(5), y);

        Sprite ballSprite = new Sprite(scene.getBallTexture());
        ballSprite.setSize(Radius * 2, Radius * 2);

        ballBody = world.createBody(bodyDef);
        ballBody.setUserData(ballSprite);

        // Create a circle shape and set its radius to 6
        CircleShape circle = new CircleShape();
        circle.setRadius(scene.changeToUnits(Radius));


        // Create a fixture definition to apply our shape to
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circle;
        fixtureDef.density = 1f;
        fixtureDef.friction = .2f;
        fixtureDef.restitution = .8f; // Make it bounce a little bit

        // Create our fixture and attach it to the body
        ballBody.createFixture(fixtureDef);

        Vector2 force = new Vector2(scene.changeToUnits(70), scene.changeToUnits(-30));

        ballBody.applyLinearImpulse(force, new Vector2(bodyDef.position.x, bodyDef.position.y), true);

        circle.dispose();
    }

    public float getRadius() {
        return Radius;
    }

    public Body getBallBody() {
        return ballBody;
    }

}
