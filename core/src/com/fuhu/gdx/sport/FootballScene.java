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
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;
import com.fuhu.gdx.game.GDXCommand;
import com.fuhu.gdx.game.LoadingScene;
import com.fuhu.gdx.game.MatchScene;
import com.fuhu.gdx.manager.ApplicationManager;
import com.fuhu.gdx.physic.PhysicScene;
import com.fuhu.gdx.scene.Scene;
import com.fuhu.gdx.scene.transition.NoTransition;
import com.fuhu.gdx.viewport.SafeZoneViewport;

public class FootballScene extends PhysicScene {

    private static final float PIXEL_PER_METER = 40;
	public final float WORLD_WIDTH = 1200;  
	public final float WORLD_HEIGHT = 1848;
	public final float BACKGROUND_WIDTH = 1440;  
	public final float BACKGROUND_HEIGHT = 2280;

	private World world;
	private BitmapFont font;
	private SpriteBatch batch;
	private OrthographicCamera camera;
	private MouseJointDef jointDef;
	private MouseJoint mouseJoint;

	private Body bodyGround;
	private Body football;
	private Body guard;
	private BodyDef bodyBoxDef;
	private Fixture fixPaddle;
	private Array<Body> arrBody = new Array<Body>();
	private Sprite spriteFootBall;
	private Sprite spriteGuard;
	private Texture textureBall;
	private Texture textureField;
	private Texture textureGuard;
	private String pathFootball = "images/sport/football/football.png";
	private String pathFootballField = "images/sport/football/football_field.jpg";
	private String pathGuard = "images/sport/football/guard.png";
	
	private boolean resetBall;
	private boolean isInterception;
	private int balls;
	private int pn;
	
    private float deltaY;
    private float enemySpeedX= PIXEL_PER_METER/2;
    private float enemySpeedY = -PIXEL_PER_METER/8;

    public FootballScene() {
        super(PIXEL_PER_METER);
        
        Gdx.input.setCatchBackKey(true);
    }

    @Override
    public void loadResources(AssetManager assetManager) {
        assetManager.load(pathFootball, Texture.class);
        assetManager.load(pathFootballField, Texture.class);
        assetManager.load(pathGuard, Texture.class);
    }

    @Override
    public void loadResourcesComplete(AssetManager assetManager) {
    	textureBall = assetManager.get(pathFootball, Texture.class);
    	textureField = assetManager.get(pathFootballField, Texture.class);
    	textureGuard = assetManager.get(pathGuard, Texture.class);
        
		batch = new SpriteBatch();
		
		world = getWorld();
		world.setGravity(new Vector2(0f, 0f));
		setDebugBox2d(false);
		
		createCamera();
		createFont();
		createWorldEdge();
        createFootBall();
        createGuard();
        createJoint();
        setContactListener();
        
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(this);
        Gdx.input.setInputProcessor(multiplexer);
    }

    @Override
    public void unloadResources(AssetManager assetManager) {
    	assetManager.unload(pathFootball);
        assetManager.unload(pathFootballField);
        assetManager.unload(pathGuard);
    }

    @Override
    public void render(float elapsedSeconds) {
        super.render(elapsedSeconds);
		
		deltaY -= PIXEL_PER_METER/2;
	    
	    batch.setProjectionMatrix(camera.combined);
		batch.begin();
		
		batch.draw(textureField, 0, 0, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
		batch.draw(textureField, 
				0, setY(deltaY % WORLD_HEIGHT),
				BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 0, 1, 1, 0);// u v u2 v2
		batch.draw(textureField, 
				0, setY(WORLD_HEIGHT + deltaY % WORLD_HEIGHT),
				BACKGROUND_WIDTH, BACKGROUND_HEIGHT, 0, 1, 1, 0);// u v u2 v2
		
		checkOutSide();
		checkOver();
		updateBodyAndSprite();
		
		font.draw(batch, Integer.toString(balls), setX(PIXEL_PER_METER), setY(WORLD_HEIGHT - PIXEL_PER_METER));
		
		batch.end();

    }
    
    private void checkOutSide() {
		if (toPixels(guard.getPosition().y) < setY(0f)) {
			resetBall = true;
		}
	}
	
	private void checkOver() {
		if (football.getPosition().y > guard.getPosition().y) {
			resetBall = true;
		}
	}
	
	private void updateBodyAndSprite() {
		world.getBodies(arrBody);
		for (Body body : arrBody) {
			Sprite sprite = (Sprite) body.getUserData();
			if (sprite != null) {
				if (sprite == spriteFootBall){
					if (toPixels(body.getPosition().y) < setY(sprite.getHeight()/2)) {
						body.setTransform(body.getPosition().x, toUnits(setY(sprite.getHeight()/2)), 0);
					}
					
					sprite.setPosition(toPixels(body.getPosition().x) - sprite.getWidth()/2, toPixels(body.getPosition().y) - sprite.getHeight()/2);
					sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
					sprite.draw(batch);
				}
				
				if (sprite == spriteGuard) {
					if (!resetBall) {
						
						if (isInterception) {
							body.setLinearVelocity(0, 0);
							body.setTransform(guard.getPosition().x, guard.getPosition().y + 1f, 0);
							
							showEnemy();							
						}
						
						if (body.getPosition().x <= toUnits(setX(sprite.getWidth()))) {
							guard.setLinearVelocity(enemySpeedX, enemySpeedY);
						}
						else if (body.getPosition().x >= toUnits(setX(WORLD_WIDTH - sprite.getWidth()))) {
							guard.setLinearVelocity(-enemySpeedX, enemySpeedY);
						}
						
						sprite.setPosition(toPixels(body.getPosition().x) - sprite.getWidth()/2, toPixels(body.getPosition().y) - sprite.getHeight()/2);
						// rotation center default sprite's left bottom
						sprite.setOrigin(sprite.getWidth()/2,sprite.getHeight()/2);
						sprite.setRotation(MathUtils.radiansToDegrees * body.getAngle());
						sprite.draw(batch);
					}
					else {
						resetPosotion();
					}
					
				}
			}
		}
	}
    
	@Override
	public void dispose() {
		batch.dispose();
		font.dispose();
		world.dispose();
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
		if (mouseJoint != null){
			world.destroyJoint(mouseJoint);
			mouseJoint = null;
		}
		
		football.setLinearVelocity(0, 0);

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
		world.setContactListener( new ContactListener(){
			@Override
			public void beginContact(Contact contact) {
				// TODO Auto-generated method stub
					isInterception = true;
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
        param.color = Color.YELLOW;

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
		bodyGroundDef.position.set( toUnits(setX(0f)), toUnits(setY(0f)) );
		bodyGround = world.createBody( bodyGroundDef );
		
		EdgeShape edgeShape = new EdgeShape();
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = edgeShape;
		edgeShape.set(new Vector2(toUnits(0f), toUnits(0f)), new Vector2(toUnits(0f), toUnits(WORLD_HEIGHT)));  
        bodyGround.createFixture(fixtureDef);
        edgeShape.set(new Vector2(toUnits(WORLD_WIDTH), toUnits(0f)), new Vector2(toUnits(WORLD_WIDTH), toUnits(WORLD_HEIGHT)));  
        bodyGround.createFixture(fixtureDef);
	}
	
	private void createFootBall() {
		// body
		BodyDef paddleBodyDef = new BodyDef();
        paddleBodyDef.type = BodyType.DynamicBody;
        paddleBodyDef.position.set(toUnits(setX(WORLD_WIDTH/2)), toUnits(setY(WORLD_HEIGHT/5)));
        football = world.createBody(paddleBodyDef);
        
        PolygonShape paddleShape = new PolygonShape();
        paddleShape.setAsBox(toUnits(WORLD_WIDTH/20), toUnits(WORLD_WIDTH/8));
        
        FixtureDef paddleShapeDef = new FixtureDef();
        paddleShapeDef.shape = paddleShape;
        paddleShapeDef.density = 1f;
        paddleShapeDef.friction = 0f;
        paddleShapeDef.restitution = 0.5f;
		fixPaddle = football.createFixture(paddleShapeDef);

		football.setGravityScale(0f);
		football.setAngularVelocity(0);
		football.setFixedRotation(true);
		football.setBullet(false);
		
		// sprite
		spriteFootBall = new Sprite(textureBall);
		spriteFootBall.setSize(WORLD_WIDTH/8, WORLD_WIDTH/4);
		football.setUserData(spriteFootBall);
	}
	
	private void createJoint() {
		// mouse joint
        jointDef = new MouseJointDef();
        jointDef.bodyA = bodyGround;
        jointDef.collideConnected = true;
        jointDef.maxForce = 1000.0f * football.getMass();
        
	}
	
	private void createGuard() {
    	// body
    	guard = world.createBody( setBodyDef() );
    	
    	CircleShape shapeBox = new CircleShape();
		shapeBox.setRadius(toUnits(WORLD_WIDTH/12));
    	
		FixtureDef fixtureDefBox = new FixtureDef();
		fixtureDefBox.density = 10000;
		fixtureDefBox.friction = 0.2f;
		fixtureDefBox.shape = shapeBox;
		fixtureDefBox.restitution = 0f;
		guard.createFixture( fixtureDefBox );
		shapeBox.dispose();
		
		guard.setFixedRotation(true);
		
		// sprite
		spriteGuard = new Sprite(textureGuard);
		spriteGuard.setSize(WORLD_WIDTH/4, WORLD_WIDTH/6);
		spriteGuard.setRotation(0);
		guard.setUserData(spriteGuard);

		showEnemy();
    }
	
	private BodyDef setBodyDef() {
		bodyBoxDef = new BodyDef();
		bodyBoxDef.type = BodyType.DynamicBody;
		bodyBoxDef.position.set( toUnits(setX(PIXEL_PER_METER / 2)) + (float)Math.random()*toUnits(WORLD_WIDTH - PIXEL_PER_METER), toUnits(setY(WORLD_HEIGHT + WORLD_WIDTH/6 + PIXEL_PER_METER)));
		
		return bodyBoxDef;
	}

	private void resetPosotion() {
		if (resetBall) {
			guard.setLinearVelocity(0, 0);
			guard.setTransform( toUnits(setX(PIXEL_PER_METER / 2)) + (float)Math.random()*toUnits(WORLD_WIDTH - PIXEL_PER_METER), toUnits(setY(WORLD_HEIGHT + WORLD_WIDTH/6 + PIXEL_PER_METER)), 0);
			
			balls++;
			resetBall = false;
			
			showEnemy();
		}
	}
	
	private void showEnemy() {
		isInterception = false;
		pn = (Math.random()*2 > 1 ? 1 : -1);
		
		guard.setLinearVelocity(pn * enemySpeedX, enemySpeedY);
		
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
        	ApplicationManager.getInstance().getGDXCommand().setRequestedOrientationLandscape();
        	getGame().setViewport(new SafeZoneViewport(
        			new Vector2(2280, 1440), 
        			new Vector2(1920, 1128),
                    new OrthographicCamera()));
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
