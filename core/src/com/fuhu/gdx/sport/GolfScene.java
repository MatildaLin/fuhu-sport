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
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.utils.Array;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.esotericsoftware.spine.AnimationState.AnimationStateListener;
import com.fuhu.gdx.game.LoadingScene;
import com.fuhu.gdx.game.MatchScene;
import com.fuhu.gdx.physic.PhysicScene;
import com.fuhu.gdx.scene.Scene;
import com.fuhu.gdx.scene.transition.NoTransition;

public class GolfScene extends PhysicScene {

    private static final float PIXEL_PER_METER = 40;
    private final float WORLD_WIDTH = 1920;  
    private final float WORLD_HEIGHT = 1128;
    private final float BACKGROUND_WIDTH = 2280;  
    private final float BACKGROUND_HEIGHT = 1440;

    public final float target = 1680;
	public final float targetRadius = 36;
    public final float targetY = 470;
    public final float angle = 21;
	
	private BitmapFont font;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private World world;
	
	EdgeShape tempEdgeShape;
	Body tempBodyGround;
	FixtureDef tempFixtureDef;

	private Body ball;
	private Array<Body> arrBody = new Array<Body>();
	private Sprite spriteBall;
	private Texture textureGolf;
	private Texture textureGolfHole;
	private Texture textureGolfHoleHover;
	private Texture textureGolfField;
	private String pathGolf = "images/sport/golf/golfball.png";
	private String pathHole = "images/sport/golf/golf_hole.png";
	private String pathHoleHover = "images/sport/golf/golf_hole_hover.png";
	private String pathGolfField = "images/sport/golf/golf.jpeg";

	private Shape shape;
	private float bodyX;
	
	private int step = -1;

	private boolean readyToStart;
	private int powerLevel;
	private float defaultPower = 5f;
	
	private int blendSrcFunc;
	private int blendDstFunc;
    
	private boolean isBrassie;
	private boolean hitBall;
	private boolean ballIn;
	
	private boolean impulseRight, impulseUp;
	private float impulseX, impulseY;

    public GolfScene() {
        super(PIXEL_PER_METER);

        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void loadResources(AssetManager assetManager) {
        assetManager.load(pathGolf, Texture.class);
        assetManager.load(pathHole, Texture.class);
        assetManager.load(pathHoleHover, Texture.class);
        assetManager.load(pathGolfField, Texture.class);
    }

    @Override
    public void loadResourcesComplete(AssetManager assetManager) {
    	textureGolf = assetManager.get(pathGolf, Texture.class);
    	textureGolfHole = assetManager.get(pathHole, Texture.class);
    	textureGolfHoleHover = assetManager.get(pathHoleHover, Texture.class);
    	textureGolfField = assetManager.get(pathGolfField, Texture.class);
        
		batch = new SpriteBatch();
		
		world = getWorld();
		
		setDebugBox2d(false);
		createCamera();
		createFont();
		createWorldEdge();
        createBall();
		createSpineAnimation();
        
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void unloadResources(AssetManager assetManager) {
    	assetManager.unload(pathGolf);
    	assetManager.unload(pathHole);
    	assetManager.unload(pathHoleHover);
        assetManager.unload(pathGolfField);
    }

    @Override
    public void render(float elapsedSeconds) {
        super.render(elapsedSeconds);
    	
		state.update(Gdx.graphics.getDeltaTime());
		state.apply(skeleton);
		skeleton.updateWorldTransform();

		camera.update();

		batch.setProjectionMatrix(this.camera.combined);
		skeletonDebugRenderer.getShapeRenderer().setProjectionMatrix(camera.combined);
		
		batch.begin();
		batch.draw(textureGolfField, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
		batch.draw(textureGolfHole, setX(target - targetRadius), setY(targetY), targetRadius*2, targetRadius);
		
		updateBodyAndSprite();

		batch.draw(textureGolfHoleHover, setX(target - targetRadius), setY(targetY), targetRadius*2, targetRadius);

		if ( !hitBall ) {
			blendSrcFunc = batch.getBlendSrcFunc();
			blendDstFunc = batch.getBlendDstFunc();
			renderer.draw(batch, skeleton);
			batch.setBlendFunction(blendSrcFunc, blendDstFunc);
		}
		
		font.draw(batch, getStatus(), setX(PIXEL_PER_METER), setY(WORLD_HEIGHT - PIXEL_PER_METER));
		
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
	
	private void createWorldEdge() {
		BodyDef bodyGroundDef = new BodyDef();
		bodyGroundDef.type = BodyType.StaticBody;
		bodyGroundDef.position.set( 0, 0 );
		Body bodyGround = world.createBody( bodyGroundDef );
		tempBodyGround = world.createBody( bodyGroundDef );
		
		tempEdgeShape = new EdgeShape();
		
        EdgeShape edgeShape = new EdgeShape();  
        FixtureDef fixtureDef = new FixtureDef();  
        fixtureDef.shape = edgeShape;  
        edgeShape.set(new Vector2(toUnits(setX(0f)), toUnits(setY(WORLD_HEIGHT*1.5f))), new Vector2(toUnits(setX(0f)), toUnits(setY(PIXEL_PER_METER*2))));  
        bodyGround.createFixture(fixtureDef);
        edgeShape.set(new Vector2(toUnits(setX(0f)), toUnits(setY(PIXEL_PER_METER*2))), new Vector2(toUnits(setX(400f)), toUnits(setY(PIXEL_PER_METER*2))));  
        bodyGround.createFixture(fixtureDef);

        edgeShape.set(new Vector2(toUnits(setX(400f)), toUnits(setY(PIXEL_PER_METER*2))), new Vector2(toUnits(setX(target - targetRadius)), toUnits(setY((target - targetRadius - 400)*(float)Math.tan(Math.toRadians(angle))))));  
        bodyGround.createFixture(fixtureDef);
        edgeShape.set(new Vector2(toUnits(setX(target - targetRadius)), toUnits(setY((target - targetRadius - 400)*(float)Math.tan(Math.toRadians(angle))))), new Vector2(toUnits(setX(target - targetRadius)), toUnits(setY(400))));  
        bodyGround.createFixture(fixtureDef);
        edgeShape.set(new Vector2(toUnits(setX(target + targetRadius)), toUnits(setY((target + targetRadius - 400)*(float)Math.tan(Math.toRadians(angle))))), new Vector2(toUnits(setX(target + targetRadius)), toUnits(setY(400))));  
        bodyGround.createFixture(fixtureDef);
        edgeShape.set(new Vector2(toUnits(setX(target + targetRadius)), toUnits(setY((target + targetRadius - 400)*(float)Math.tan(Math.toRadians(angle))))), new Vector2(toUnits(setX(WORLD_WIDTH)), toUnits(setY((WORLD_WIDTH - 400)*(float)Math.tan(Math.toRadians(angle))))));  
        bodyGround.createFixture(fixtureDef);
        
	}
	
	public void createTempEdge(float h) {
		if (tempBodyGround.getFixtureList().size > 0) {
			return;
		}
		tempFixtureDef = new FixtureDef();
		tempFixtureDef.shape = tempEdgeShape;
		tempEdgeShape.set(new Vector2(0f, h), new Vector2(WORLD_WIDTH, h));
		tempBodyGround.createFixture(tempFixtureDef);
	}
	
	public void removeTempEdge() {
		if (tempBodyGround.getFixtureList().size > 0) {
			for (Fixture fixture : tempBodyGround.getFixtureList()) {
				tempBodyGround.destroyFixture(fixture);
			}
		}
	}
	
	private void createBall() {
		BodyDef bodyBoxDef = new BodyDef();
		bodyBoxDef.type = BodyType.DynamicBody;
		bodyBoxDef.position.set( toUnits(setX(200)), toUnits(setY(WORLD_HEIGHT/2)) );
		ball = world.createBody( bodyBoxDef );
		CircleShape shapeBox = new CircleShape();
		shapeBox.setRadius(toUnits(PIXEL_PER_METER));
		FixtureDef fixtureDefBox = new FixtureDef();
//		fixtureDefBox.density = 1;
		fixtureDefBox.friction = 1f;
		fixtureDefBox.shape = shapeBox;
		fixtureDefBox.restitution = 0.2f;
		ball.createFixture( fixtureDefBox );
		shapeBox.dispose();
			
			world.setContactListener( new ContactListener(){
				@Override
				public void beginContact(Contact contact) {
					// TODO Auto-generated method stub
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
			
		// sprite
		spriteBall = new Sprite(textureGolf);
		spriteBall.setSize(4, 4);
		ball.setUserData(spriteBall);
	}
	
	private void resetBall() {
		ball.setLinearVelocity(0f,0f);
		ball.setTransform(toUnits(setX(200)), toUnits(setY(WORLD_HEIGHT/2)), 0);
		setBrassie(false);
		step = 0;
		hitBall = false;
		ballIn = false;
	}
	
	SkeletonRenderer renderer;
	SkeletonRendererDebug skeletonDebugRenderer;
	TextureAtlas atlas;
	Skeleton skeleton;
	AnimationState state;
	private void createSpineAnimation() {
		renderer = new SkeletonRenderer();
		renderer.setPremultipliedAlpha(true); // PMA results in correct blending without outlines.
		skeletonDebugRenderer = new SkeletonRendererDebug();
		skeletonDebugRenderer.setBoundingBoxes(false);
		skeletonDebugRenderer.setRegionAttachments(false);
		
		atlas = new TextureAtlas(Gdx.files.internal("images/sport/golf/skeleton/skeleton.atlas"));
		SkeletonJson json = new SkeletonJson(atlas); // This loads skeleton JSON data, which is stateless.
		json.setScale(0.5f); // Load the skeleton at 60% the size it was in Spine.
		SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal("images/sport/golf/skeleton/skeleton.json"));

		skeleton = new Skeleton(skeletonData); // Skeleton holds skeleton state (bone positions, slot attachments, etc).
		skeleton.setPosition(10, 10);

		AnimationStateData stateData = new AnimationStateData(skeletonData); // Defines mixing (crossfading) between animations.

		state = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
		state.setTimeScale(0.2f); // Slow all animations down to 50% speed.
		state.addListener(new AnimationStateListener() {
			public void event (int trackIndex, Event event) {
				if (event.getData().getName().equals("power")) {
					powerLevel = event.getInt();
				}
				if (event.getData().getName().equals("hit")) {
					runImpulse();
				}
			}

			public void complete (int trackIndex, int loopCount) {
				System.out.println(trackIndex + " complete: " + state.getCurrent(trackIndex) + ", " + loopCount);
			}

			public void start (int trackIndex) {
				System.out.println(trackIndex + " start: " + state.getCurrent(trackIndex));
			}

			public void end (int trackIndex) {
				System.out.println(trackIndex + " end: " + state.getCurrent(trackIndex));
			}
		});

		// Queue animations on track 0.
		state.setAnimation(0, "animation", true);
	}
	
	private void runImpulse() {
		System.out.print("isBrassie: "+isBrassie);
		if (isBrassie) {
			hitBall = true;
			if (impulseRight) {
				right(impulseX);
			}
			else {
				left(impulseX);
			}
			
			if (impulseUp) {
				up(impulseY);
			}
			else {
				down(impulseY);
			}
			
			removeTempEdge();
			setBrassie(false);
		}
	}
	
	private void setBrassie(boolean b) {
		isBrassie = b;
	}
	
	private int getPowerLevel() {
		return powerLevel;
	}
	
	private boolean getBrassie() {
		return isBrassie;
	}
	
	private String getStatus() {
		if (getStep() == -1) {
			return "READY";
		}
		else if (getStep() == 0) {
			return "GO!";
		}
		else {
			return "Gross: "+getStep();
		}
	}
	
	private int getStep() {
		return step;
	}
	
	private void updateBodyAndSprite() {
		bodyX = ball.getPosition().x;

		shape = ball.getFixtureList().get(0).getShape();
		float radius = 1/((bodyX+toUnits(WORLD_WIDTH))/toUnits(WORLD_WIDTH));
		shape.setRadius(radius);
		
		if (Math.abs(ball.getLinearVelocity().x) < 0.25 && Math.abs(ball.getLinearVelocity().y) < 0.25) {
			hitBall = false;
		}
		
		world.getBodies(arrBody);
		for (Body body : arrBody) {
			
			Sprite sprite0 = (Sprite) body.getUserData();
			if (sprite0 != null) {
				
				if ( (body.getPosition().x > toUnits(setX(target - targetRadius))) && (body.getPosition().x < toUnits(setX(target + targetRadius))) ){
					if (body.getPosition().y < toUnits(setY(targetY + targetRadius/2))) {
						ballIn = true;
					}
				}
				
				if (body.getPosition().y < 0) {
					resetBall();
				}
				
				if (!ballIn) {
					skeleton.setPosition(toPixels(body.getPosition().x), toPixels(body.getPosition().y) + 330);
					sprite0.setSize(toPixels(radius*2), toPixels(radius*2));
					sprite0.setPosition(toPixels(body.getPosition().x) - sprite0.getWidth()/2, toPixels(body.getPosition().y) - sprite0.getHeight()/2);
					sprite0.setRotation(MathUtils.radiansToDegrees * body.getAngle());
					sprite0.draw(batch);
				}
				
			}
		}
	}
	
    public void left(float f) {
		ball.applyLinearImpulse(f, 0, ball.getPosition().x, ball.getPosition().y, true);
    }
    
    public void right(float f) {
		ball.applyLinearImpulse(f, 0, ball.getPosition().x, ball.getPosition().y, true);
    }
    
    public void up(float f) {
    	ball.applyLinearImpulse(0, -f, ball.getPosition().x, ball.getPosition().y, true);
    }
    
    public void down(float f) {
    	ball.applyLinearImpulse(0, -f, ball.getPosition().x, ball.getPosition().y, true);
    }
    
    private void readyToStart() {
		step++;
	}
	
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		
		if (getBrassie() || hitBall) {
			return false;
		}
		
		readyToStart();
		
		if (!readyToStart) {
			readyToStart = true;
			return false;
		}
		
		setBrassie(true);
		
		float fY = -((WORLD_HEIGHT-screenY)-500)/32f;

		powerLevel = getPowerLevel();
		
		System.out.println("xuan powerLevel: "+powerLevel);
		
		
		if (toPixels(ball.getPosition().x) < setX(target)) {
			if (fY < 0) {
				setLinearImpulse(true, true, powerLevel*defaultPower, -powerLevel*defaultPower);
			}
			else if (fY > 0) {
				setLinearImpulse(true, false, powerLevel*defaultPower, -powerLevel*defaultPower);
			}
		}
		else {
			if (fY < 0) {
				setLinearImpulse(false, true, -powerLevel*defaultPower*0.5f, -powerLevel*defaultPower*0.1f);
			}
			else if (fY > 0) {
				setLinearImpulse(false, false, -powerLevel*defaultPower*0.5f, -powerLevel*defaultPower*0.1f);
			}
		}
		
		return false;
	}
	
	private void setLinearImpulse(boolean right, boolean up, float x, float y) {
		impulseRight = right;
		impulseUp = up;
		impulseX = x;
		impulseY = y;
	}
	
	private float setX(float x) {
		return getWorldX() + x;
	}
	
	private float setY(float y) {
		return getWorldY() + y;
	}

    @Override
    public boolean keyDown(int keycode) {
        if ((keycode == Input.Keys.ESCAPE) || (keycode == Input.Keys.BACK)) {
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
