package com.fuhu.gdx.sprite;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.physics.box2d.*;
import com.fuhu.gdx.sport.TennisScene;

/**
 * Created by sabrinakuo on 2015/9/30.
 */
public class TennisRacket {
    private Sprite racketSprite;
    private Fixture fixracket;
    private Body racketBody;
    private OrthographicCamera camera;
    private float racketWidth = 80f;
    private float racketHeight = 180f;

    public TennisRacket(TennisScene scene, World world) {

        camera = (OrthographicCamera) scene.getCamera();
        // racket body definition
        BodyDef racketBodyDef = new BodyDef();
        racketBodyDef.type = BodyDef.BodyType.DynamicBody;
        racketBodyDef.position.set(scene.changeToUnits(camera.viewportWidth / 2), scene.changeToUnits(camera.viewportHeight / 2));

        racketSprite = new Sprite(scene.getRacketTexture());
        racketSprite.setSize(racketSprite.getWidth() * 2.5f, racketSprite.getHeight() * 2.5f);
        racketBody = world.createBody(racketBodyDef);
        racketBody.setUserData(racketSprite);

        // body shape
        PolygonShape racketShape = new PolygonShape();
        racketShape.setAsBox(scene.changeToUnits(racketWidth), scene.changeToUnits(racketHeight));

        // fixture definition
        FixtureDef racketShapeDef = new FixtureDef();
        racketShapeDef.shape = racketShape;
        racketShapeDef.density = 10f;
        racketShapeDef.friction = 1f;
        racketShapeDef.restitution = 0f;

        fixracket = racketBody.createFixture(racketShapeDef);

        // set body gravity
        racketBody.setGravityScale(0f);
        racketBody.setAngularVelocity(0);
        racketBody.setFixedRotation(true);
        racketBody.setBullet(false);

        racketShape.dispose();
    }

    public Body getRacketBody() {
        return racketBody;
    }

    public Fixture getFixracket() {
        return fixracket;
    }
}
