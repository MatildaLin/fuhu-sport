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

public class BasketballScene extends PhysicScene {

    private static final float PIXEL_PER_METER = 40;
    private final float WORLD_WIDTH = 1920;  
    private final float WORLD_HEIGHT = 1128;
    private final float BACKGROUND_WIDTH = 2280;  
    private final float BACKGROUND_HEIGHT = 1440;
    private final float BASKET_X = 1160;  
    private final float BASKET_Y = 440;

	private static final float GRAVITY = -9.81f;

    public final float target = 1270;
	public final float targetRadius = 115;
	
	private BitmapFont font;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private World world;
	
	EdgeShape tempEdgeShape;
	Body tempBodyGround;
	FixtureDef tempFixtureDef;

	private Body basketball;
	private Array<Body> arrBody = new Array<Body>();
	private Sprite spriteBall;
	private Texture textureBasketUp;
	private Texture textureBasketDown;
	private Texture textureBasketBallField;
	private Texture textureBasketBall;
	private String pathBasketUp = "images/sport/basketball/basket_up.png";
	private String pathBasketDown = "images/sport/basketball/basket_down.png";
	private String pathBasketBallField = "images/sport/basketball/basketball_field.jpg";
	private String pathBasketBall = "images/sport/basketball/basketball.png";

	private Shape shape;
	private float bodyX, bodyY;
	
    private int shots;
	private int shotIn;
	
	private int blendSrcFunc;
	private int blendDstFunc;
    
    public boolean waitingForResult;
    public boolean waitingForReset;

    public BasketballScene() {
        super(PIXEL_PER_METER);

        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void loadResources(AssetManager assetManager) {
        assetManager.load(pathBasketBallField, Texture.class);
        assetManager.load(pathBasketBall, Texture.class);
        assetManager.load(pathBasketUp, Texture.class);
        assetManager.load(pathBasketDown, Texture.class);
    }

    @Override
    public void loadResourcesComplete(AssetManager assetManager) {
    	textureBasketUp = assetManager.get(pathBasketUp, Texture.class);
    	textureBasketDown = assetManager.get(pathBasketDown, Texture.class);
    	textureBasketBallField = assetManager.get(pathBasketBallField, Texture.class);
    	textureBasketBall = assetManager.get(pathBasketBall, Texture.class);
        
		batch = new SpriteBatch();
		
		world = getWorld();
		world.setGravity(new Vector2(0f, GRAVITY*7));
		
		setDebugBox2d(false);
		createCamera();
		createFont();
		createWorldEdgeAndBasket();
        createBall();
		createSpineAnimation();
        
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void unloadResources(AssetManager assetManager) {
//    	assetManager.unload(pathBasket);
    	assetManager.unload(pathBasketUp);
    	assetManager.unload(pathBasketDown);
        assetManager.unload(pathBasketBallField);
        assetManager.unload(pathBasketBall);
    }

    @Override
    public void render(float elapsedSeconds) {
        super.render(elapsedSeconds);
	    
		if (!waitingForResult) {
			state.update(Gdx.graphics.getDeltaTime());
		}
		
		state.apply(skeleton);
		skeleton.updateWorldTransform();

		camera.update();

		batch.setProjectionMatrix(this.camera.combined);
		skeletonDebugRenderer.getShapeRenderer().setProjectionMatrix(camera.combined);
		
		batch.begin();
		batch.draw(textureBasketBallField, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
		batch.draw(textureBasketDown, setX(BASKET_X), setY(BASKET_Y - WORLD_HEIGHT/50), WORLD_WIDTH/8, WORLD_HEIGHT/5);
		
		updateBodyAndSprite();

		batch.draw(textureBasketUp, setX(BASKET_X), setY(BASKET_Y + WORLD_HEIGHT/5 - WORLD_HEIGHT/50), WORLD_WIDTH/8, WORLD_HEIGHT/50);
//		batch.draw(textureBasket, setX(BASKET_X), setY(BASKET_Y), WORLD_WIDTH/8, WORLD_HEIGHT/5);

		skeleton.setPosition(setX(target), setY(BASKET_Y*1.6f));

		blendSrcFunc = batch.getBlendSrcFunc();
		blendDstFunc = batch.getBlendDstFunc();
		renderer.draw(batch, skeleton);
		batch.setBlendFunction(blendSrcFunc, blendDstFunc);
		
		System.out.println("shots: "+shots);
		font.draw(batch, shotIn + " / " + shots + "\n" + (shots != 0 ? (shotIn*1f/shots)*100 : 0) + " %", setX(PIXEL_PER_METER), setY(WORLD_HEIGHT - PIXEL_PER_METER));
		
		batch.end();

    }
    
    private void updateBodyAndSprite() {
    	bodyX = basketball.getPosition().x;
		bodyY = basketball.getPosition().y;
		
		if (bodyX < toUnits(setX(0)) | bodyX > toUnits(setX(WORLD_WIDTH))) {
			reset();
		}
		if (waitingForResult || waitingForReset) {
			if (Math.abs(basketball.getLinearVelocity().x) < 0.25 && Math.abs(basketball.getLinearVelocity().y) < 0.5) {
				reset();
			}
		}
		else {
			if (Math.abs(basketball.getLinearVelocity().y) < 0.5) {
				basketball.setLinearVelocity(0, 0);
			}
		}
		
		if (toUnits(setX(target - targetRadius)) <= bodyX && bodyX <= toUnits(setX(target + targetRadius*1.5f))) {
			if (toUnits(setY(BASKET_Y - PIXEL_PER_METER*2f)) <= bodyY && bodyY <= toUnits(setY(BASKET_Y + PIXEL_PER_METER))) {
				if (waitingForResult) {
					shotIn ++;
					waitingForResult = false;
					waitingForReset = true;
				}
			}
		}

		shape = basketball.getFixtureList().get(0).getShape();
		float radius = 3/((bodyX+toUnits(WORLD_WIDTH))/toUnits(WORLD_WIDTH));
		shape.setRadius(radius);
		
		world.getBodies(arrBody);
		for (int i = 0; i < arrBody.size; ++i) {
			Body body = arrBody.get(i);
			Sprite sprite = (Sprite) body.getUserData();
			if (sprite != null) {
				
				if (waitingForResult && body.getLinearVelocity().y < 0) {
					createTempEdge(toUnits(setY(100)));
				}
				
				sprite.setSize(toPixels(radius*2), toPixels(radius*2));
				sprite.setPosition(toPixels(body.getPosition().x) - sprite.getWidth()/2, toPixels(body.getPosition().y) - sprite.getHeight()/2);
				sprite.setOrigin(sprite.getWidth()/2,sprite.getHeight()/2);
				sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
				sprite.draw(batch);
			}
		}
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
	
	private void createWorldEdgeAndBasket() {
		BodyDef bodyGroundDef = new BodyDef();
		bodyGroundDef.type = BodyType.StaticBody;
		bodyGroundDef.position.set( 0, toUnits(setY(PIXEL_PER_METER)) );
		Body bodyGround = world.createBody( bodyGroundDef );
		tempBodyGround = world.createBody( bodyGroundDef );
		
		tempEdgeShape = new EdgeShape();
		
        EdgeShape edgeShape = new EdgeShape();  
        FixtureDef fixtureDef = new FixtureDef();  
        fixtureDef.shape = edgeShape;  

        edgeShape.set(new Vector2(WORLD_WIDTH, 0f), new Vector2(0f, 0f));  
        bodyGround.createFixture(fixtureDef);
        
        edgeShape.set(new Vector2(toUnits(setX(target - targetRadius)), toUnits(setY(BASKET_Y))), new Vector2(toUnits(setX(target - targetRadius + PIXEL_PER_METER/4)), toUnits(setY(BASKET_Y + PIXEL_PER_METER/3))));  
        bodyGround.createFixture(fixtureDef);
        edgeShape.set(new Vector2(toUnits(setX(target - targetRadius + PIXEL_PER_METER/4)), toUnits(setY(BASKET_Y + PIXEL_PER_METER/3))), new Vector2(toUnits(setX(target - targetRadius + PIXEL_PER_METER/2)), toUnits(setY(BASKET_Y))));  
        bodyGround.createFixture(fixtureDef);
        edgeShape.set(new Vector2(toUnits(setX(target - targetRadius + PIXEL_PER_METER/2)), toUnits(setY(BASKET_Y))), new Vector2(toUnits(setX(target - targetRadius)), toUnits(setY(BASKET_Y))));  
        bodyGround.createFixture(fixtureDef);
        
        edgeShape.set(new Vector2(toUnits(setX(target + targetRadius)), toUnits(setY(BASKET_Y))), new Vector2(toUnits(setX(target + targetRadius + PIXEL_PER_METER/4)), toUnits(setY(BASKET_Y + PIXEL_PER_METER/3))));  
        bodyGround.createFixture(fixtureDef);
        edgeShape.set(new Vector2(toUnits(setX(target + targetRadius + PIXEL_PER_METER/4)), toUnits(setY(BASKET_Y + PIXEL_PER_METER/3))), new Vector2(toUnits(setX(target + targetRadius + PIXEL_PER_METER/2)), toUnits(setY(BASKET_Y))));  
        bodyGround.createFixture(fixtureDef);
        edgeShape.set(new Vector2(toUnits(setX(target + targetRadius + PIXEL_PER_METER/2)), toUnits(setY(BASKET_Y))), new Vector2(toUnits(setX(target + targetRadius)), toUnits(setY(BASKET_Y))));  
        bodyGround.createFixture(fixtureDef);
	}
	
	public void createTempEdge(float h) {
		if (tempBodyGround.getFixtureList().size > 0) {
			return;
		}
		tempFixtureDef = new FixtureDef();
		tempFixtureDef.shape = tempEdgeShape;
		tempEdgeShape.set(new Vector2(toUnits(setX(0)), h), new Vector2(toUnits(setX(WORLD_WIDTH)), h));
		tempBodyGround.createFixture(tempFixtureDef);
	}
	
	public void removeTempEdge() {
		if (tempBodyGround.getFixtureList().size > 0) {
			for (Fixture fixture : tempBodyGround.getFixtureList()) {
				tempBodyGround.destroyFixture(fixture);
			}
		}
	}
	
	// spine
		SkeletonRenderer renderer;
		SkeletonRendererDebug skeletonDebugRenderer;
		TextureAtlas atlas;
		Skeleton skeleton;
		AnimationState state;
		private int shot;
		
		private void createSpineAnimation() {
			renderer = new SkeletonRenderer();
			renderer.setPremultipliedAlpha(true); // PMA results in correct blending without outlines.
			skeletonDebugRenderer = new SkeletonRendererDebug();
			skeletonDebugRenderer.setBoundingBoxes(false);
			skeletonDebugRenderer.setRegionAttachments(false);
			
			atlas = new TextureAtlas(Gdx.files.internal("images/sport/basketball/skeleton/skeleton.atlas"));
			SkeletonJson json = new SkeletonJson(atlas); // This loads skeleton JSON data, which is stateless.
			json.setScale(1.3f);
			SkeletonData skeletonData = json.readSkeletonData(Gdx.files.internal("images/sport/basketball/skeleton/skeleton.json"));

			skeleton = new Skeleton(skeletonData); // Skeleton holds skeleton state (bone positions, slot attachments, etc).
			skeleton.setPosition(10, 10);

			AnimationStateData stateData = new AnimationStateData(skeletonData); // Defines mixing (crossfading) between animations.

			state = new AnimationState(stateData); // Holds the animation state for a skeleton (current animation, time, etc).
			state.setTimeScale(0.5f); // Slow all animations down to 50% speed.
			state.addListener(new AnimationStateListener() {
				public void event (int trackIndex, Event event) {
					if (event.getData().getName().equals("shot")) {
						shot = event.getInt();
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
	
	private void createBall() {
		// body
		BodyDef bodyBoxDef = new BodyDef();
		bodyBoxDef.type = BodyType.DynamicBody;
		bodyBoxDef.position.set( toUnits(setX(WORLD_WIDTH/5)), toUnits(setY(WORLD_HEIGHT/2)) );
		basketball = world.createBody( bodyBoxDef );
		CircleShape shapeBox = new CircleShape();
		shapeBox.setRadius(2);
		FixtureDef fixtureDefBox = new FixtureDef();
		// 密度
//		fixtureDefBox.density = 1;
		// 磨擦力
		fixtureDefBox.friction = 0.1f;
		fixtureDefBox.shape = shapeBox;
		// 彈力0-1,1為完全彈性碰撞 
		fixtureDefBox.restitution = 0.7f;
		basketball.createFixture( fixtureDefBox );
		shapeBox.dispose();
			
		world.setContactListener( new ContactListener(){
			@Override
			public void beginContact(Contact contact) {
				// TODO Auto-generated method stub
				basketball.setAngularVelocity(0);
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
		spriteBall = new Sprite(textureBasketBall);
		spriteBall.setSize(4, 4);
		basketball.setUserData(spriteBall);
	}
	
	private void reset() {
		basketball.setLinearVelocity(0, 0);
		basketball.setTransform(toUnits(setX(WORLD_WIDTH/5)), toUnits(setY(WORLD_HEIGHT/2)), 0);
		removeTempEdge();
		waitingForResult = false;
		waitingForReset = false;
	}
	
	public void shot(float f) {
		basketball.applyLinearImpulse(f, -world.getGravity().y*0.8f, basketball.getPosition().x, basketball.getPosition().y, true);
		basketball.setAngularVelocity(10f);
    }
    
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if (waitingForResult || waitingForReset) {
			return false;
		}
		waitingForResult = true;
		shots ++;
		
		/**
		 * GRAVITY = -9.81f * 7;
		 */
		shot(17.56f + shot*0.7f);
		
		/**
		 * GRAVITY = -10;
		 */
//		shot(6.87f + shot*0.4f);
		
		removeTempEdge();
		
		return false;
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
