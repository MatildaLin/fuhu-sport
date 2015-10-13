package com.fuhu.gdx.sport;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
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
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.esotericsoftware.spine.AnimationState;
import com.esotericsoftware.spine.AnimationStateData;
import com.esotericsoftware.spine.Event;
import com.esotericsoftware.spine.Skeleton;
import com.esotericsoftware.spine.SkeletonBounds;
import com.esotericsoftware.spine.SkeletonData;
import com.esotericsoftware.spine.SkeletonJson;
import com.esotericsoftware.spine.SkeletonRenderer;
import com.esotericsoftware.spine.SkeletonRendererDebug;
import com.esotericsoftware.spine.Skin;
import com.esotericsoftware.spine.AnimationState.AnimationStateListener;
import com.esotericsoftware.spine.attachments.AtlasAttachmentLoader;
import com.esotericsoftware.spine.attachments.RegionAttachment;
import com.fuhu.gdx.game.Game;
import com.fuhu.gdx.game.LoadingScene;
import com.fuhu.gdx.game.MatchScene;
import com.fuhu.gdx.physic.PhysicScene;
import com.fuhu.gdx.physic.PhysicalActorWrapper;
import com.fuhu.gdx.scene.Scene;
import com.fuhu.gdx.scene.SceneLayer;
import com.fuhu.gdx.scene.transition.NoTransition;
import com.fuhu.gdx.ui.SpineActor;
import com.fuhu.gdx.viewport.SafeZoneViewport;

public class SkateboardScene extends PhysicScene implements ContactListener, InputProcessor {
	private static final float METER_TO_PIXEL = 100;
	int scoreNumber;
	SpriteBatch batch;
	float WIDTH = getWorldWidth();
	float HEIGHT = getWorldHeight();
	private World world;
	private Body groundBody;
	private MouseJoint mouseJoint;
	private Body skateBoardBody;
	private Texture backgroundTexture;
	Label promptLabel;
	Label scoreLabel;
	Label textLabel;
	private Body rightWheelBody;
	private Body leftWheelBody;
	SceneLayer textLayer;
	private float deltaTime;
	private boolean showingToast = false;
	private OrthographicCamera camera;
	private float deltaY;
	private boolean contactFlag;
	private float contactPosition;
	private boolean scoreFlag;
	private Sprite skateBoardSprite;
	private Sprite rightWheelSprite;
	private Sprite leftWheelSprite;
	private SkeletonRendererDebug skeletonRendererDebug;
	private SkeletonRenderer skeletonRenderer;
	private Skeleton skeleton;
	private AnimationState animationState;
	private Body roadblockBody;
	private Sprite roadblockSprite;
	private List<Texture> blockTextureList;

	public SkateboardScene() {
			super(METER_TO_PIXEL);
			setDebugBox2d(false);
			
			world = getWorld();
			world.setContactListener(this);
			Gdx.input.setInputProcessor(this);
			
			createRoom();
			
			/** Prompt text */
            promptLabel = createLabel(""+scoreNumber, manage(createBitmapFont("fonts/comicstripposter-regular.ttf",180,Color.GREEN)));
            scoreLabel = createLabel("score:", manage(createBitmapFont("fonts/comicstripposter-regular.ttf",50,Color.GRAY)));
            scoreLabel.setX(-scoreLabel.getWidth()*1.1f);
            scoreLabel.setY(promptLabel.getHeight()/4);
            SceneLayer promptLayer = new SceneLayer();
            promptLayer.setSize(promptLabel.getPrefWidth(), promptLabel.getPrefHeight());
            promptLayer.setPosition(getWorldWidth() - promptLayer.getWidth() - 150, getWorldHeight()-promptLayer.getHeight() - 10);
//            promptLayer.setAlign(Align.topRight);
            promptLayer.addActor(promptLabel);
            promptLayer.addActor(scoreLabel);
            addLayer(promptLayer);
            
            textLabel = createLabel("Score!!", manage(createBitmapFont("fonts/KOMIKAX_AXIS.ttf",100,Color.WHITE)));
            textLayer = new SceneLayer();
            textLayer.setSize(textLabel.getPrefWidth(), textLabel.getPrefHeight());
            textLayer.setAlign(Align.center);
            textLayer.addActor(textLabel);
		}

	private void updateScore(float delta) {
		deltaTime = deltaTime + 1f;
	}

	private void update(float delta) {
		deltaY = deltaY - 8f;
	}

	@Override
	public void render(float elapsedSeconds) {

		if (showingToast)
			updateScore(elapsedSeconds);
		update(Gdx.graphics.getDeltaTime());

		skateBoardSprite.setPosition(toPixels(skateBoardBody.getPosition().x) - skateBoardSprite.getWidth() / 2,
				toPixels(skateBoardBody.getPosition().y) - skateBoardSprite.getHeight() / 2);
		rightWheelSprite.setPosition(toPixels(rightWheelBody.getPosition().x) - rightWheelSprite.getWidth() / 2,
				toPixels(rightWheelBody.getPosition().y) - rightWheelSprite.getHeight() / 2);
		leftWheelSprite.setPosition(toPixels(leftWheelBody.getPosition().x) - leftWheelSprite.getWidth() / 2,
				toPixels(leftWheelBody.getPosition().y) - leftWheelSprite.getHeight() / 2);
		roadblockSprite.setPosition(toPixels(roadblockBody.getPosition().x) - roadblockSprite.getWidth() / 2,
				toPixels(roadblockBody.getPosition().y) - roadblockSprite.getHeight() / 2);

		skeletonRendererDebug.getShapeRenderer().setProjectionMatrix(camera.combined);
		animationState.update(Gdx.graphics.getDeltaTime());
		animationState.apply(skeleton);
		skeleton.updateWorldTransform();

		batch.setProjectionMatrix(camera.combined);

		batch.begin();

		// background moving
		batch.draw(backgroundTexture, getWorldX() + deltaY % WIDTH, getWorldY(), WIDTH, HEIGHT, 0, 1, 1, 0);
		batch.draw(backgroundTexture, getWorldX() + WIDTH + deltaY % WIDTH, getWorldY(), WIDTH, HEIGHT, 0, 1, 1, 0);

		skeletonRenderer.draw(batch, skeleton);
		skeleton.setPosition(skateBoardSprite.getX() + 140f, skateBoardSprite.getY() + 20f);

		batch.draw(skateBoardSprite, skateBoardSprite.getX(), skateBoardSprite.getY());
		batch.draw(rightWheelSprite, rightWheelSprite.getX(), rightWheelSprite.getY());
		batch.draw(leftWheelSprite, leftWheelSprite.getX(), leftWheelSprite.getY());
		batch.draw(roadblockSprite, roadblockSprite.getX(), roadblockSprite.getY());

		if (roadblockSprite.getX() < skateBoardSprite.getX() && !scoreFlag) {
			scoreNumber++;
			promptLabel.setText("" + scoreNumber);
			textLabel.setText("Score!!!");
			textLabel.setColor(Color.BLUE);
			deltaTime = 0;
			scoreFlag = true;
			addLayer(textLayer);
			showingToast = true;
		}

		if (roadblockSprite.getX() < getWorldX() - 100) {
			roadblockSprite = new Sprite(blockTextureList.get((int) (Math.random() * 5)));
			roadblockBody.setTransform(toUnits(getWorldX() + getWorldWidth()), toUnits(getWorldY() + 200), 0);
			roadblockBody.setLinearVelocity(-5f, 0);
			scoreFlag = false;
			contactFlag = false;
		}
		
		if (contactFlag) {
			// System.out.println("in render contact");
			if (roadblockBody.getPosition().x > skateBoardBody.getPosition().x
					+ toUnits(skateBoardSprite.getWidth() + 200)) {
				roadblockBody.setLinearVelocity(-5f, 0);
			}
		}
		
		if (deltaTime > 50f) {
			removeLayer(textLayer);
			showingToast = false;
		}

		batch.end();
		super.render(elapsedSeconds);
	}

	private void createRoom() {

		// ground body
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(0, 0);
		groundBody = world.createBody(bodyDef);
		EdgeShape edge = new EdgeShape();
		FixtureDef groundShapeDef = new FixtureDef();
		groundShapeDef.shape = edge;
		// down edge
		edge.set(new Vector2(0f, toUnits(getWorldY() + 100)),
				new Vector2(toUnits(getWorldX() + getWorldWidth()), toUnits(getWorldY() + 100)));
		groundBody.createFixture(groundShapeDef).setUserData("ground");

	}

	private Body createBox(float x, float y, float width, float height, BodyDef.BodyType bodyType) {
		PolygonShape shape = new PolygonShape();
		shape.setAsBox(toUnits(width) / 2, toUnits(height) / 2);
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(toUnits(x) + toUnits(width / 2), toUnits(y) + toUnits(height / 2));
		bodyDef.type = bodyType;
		Body body = world.createBody(bodyDef);
		FixtureDef boxShapeDef = new FixtureDef();
		boxShapeDef.shape = shape;
		boxShapeDef.density = 1f;
		boxShapeDef.friction = 0f;
		boxShapeDef.restitution = 0f;
		body.createFixture(boxShapeDef);

		return body;
	}

	private Label createLabel(String text, BitmapFont font) {
		Label.LabelStyle style = new Label.LabelStyle(font, Color.WHITE);
		return new Label(text, style);
	}

	private BitmapFont createBitmapFont(String fontName, int size, Color color) {
		FreeTypeFontGenerator fontGenerator = new FreeTypeFontGenerator(Gdx.files.internal(fontName));
		FreeTypeFontGenerator.FreeTypeFontParameter param = new FreeTypeFontGenerator.FreeTypeFontParameter();
		param.size = size;
		param.color = color;
		BitmapFont font = fontGenerator.generateFont(param);
		for (TextureRegion region : font.getRegions()) {
			region.getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		}
		fontGenerator.dispose();
		return font;
	}

	@Override
	public void beginContact(Contact contact) {
		// TODO Auto-generated method stub
		// startGame=false;
		Fixture fa = contact.getFixtureA();
		Fixture fb = contact.getFixtureB();
		System.out.println("fa.userdata=" + contact.getFixtureA().getUserData());
		System.out.println("fb.userdata=" + contact.getFixtureB().getUserData());
		
		if ((fa.getUserData() != null && fb.getUserData() != null && fa.getUserData().equals("ground")
				&& fb.getUserData().equals("leftWheel"))
				|| (fa.getUserData() != null && fb.getUserData() != null && fa.getUserData().equals("leftWheel")
						&& fb.getUserData().equals("ground"))) {
			boxIsJumpingFlag = false;
		} else if ((fa.getUserData() == null && fb.getUserData() != null && fb.getUserData().equals("roadblock"))
				|| (fa.getUserData() != null && fb.getUserData() == null && fa.getUserData().equals("roadblock"))) {
			System.out.println("box and roadblock contact!!");
			contactFlag = true;
			contactPosition = roadblockSprite.getX();
			if (roadblockSprite.getX() > skateBoardSprite.getX() + skateBoardSprite.getWidth() / 2)
				roadblockBody.setLinearVelocity(3f, 0f);
		} else if ((fa.getUserData() != null && fb.getUserData() != null && fa.getUserData().equals("rightWheel")
				&& fb.getUserData().equals("roadblock"))
				|| (fa.getUserData() != null && fb.getUserData() != null && fa.getUserData().equals("roadblock")
						&& fb.getUserData().equals("rightWheel"))) {
			// System.out.println("contact!!");
			contactFlag = true;
			contactPosition = roadblockSprite.getX();
			if (roadblockSprite.getX() > skateBoardSprite.getX() + skateBoardSprite.getWidth() / 2)
				roadblockBody.setLinearVelocity(3f, 0f);
		} else if ((fa.getUserData() != null && fb.getUserData() != null && fa.getUserData().equals("leftWheel")
				&& fb.getUserData().equals("roadblock"))
				|| (fa.getUserData() != null && fb.getUserData() != null && fa.getUserData().equals("roadblock")
						&& fb.getUserData().equals("leftWheel"))) {
			// System.out.println("contact!!");
			contactFlag = true;
			contactPosition = roadblockSprite.getX();
			if (roadblockSprite.getX() > skateBoardSprite.getX() + skateBoardSprite.getWidth() / 2)
				roadblockBody.setLinearVelocity(3f, 0f);
		}
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

	private boolean boxIsJumpingFlag;

	@Override
	public boolean touchDown(int x, int y, int pointer, int button) {
		if (!boxIsJumpingFlag && skateBoardBody != null) {
			boxIsJumpingFlag = true;
			skateBoardBody.setLinearVelocity(0, toUnits(2000));
			animationState.setAnimation(0, "jump", false); // Set animation on track 0 to jump.
			animationState.addAnimation(0, "idle", true, 0);
		}
		return false;
	}

	@Override
	public boolean touchDragged(int x, int y, int p) {
		return false;
	}

	@Override
	public boolean touchUp(int x, int y, int pointer, int button) {
		return false;
	}

	@Override
	public void loadResources(AssetManager assetManager) {
		assetManager.load("images/sport/skateboard/back.png", Texture.class);
		assetManager.load("images/sport/skateboard/teddy_bear_toy_1.png", Texture.class);
		assetManager.load("images/sport/skateboard/teddy_bear_toy_2.png", Texture.class);
		assetManager.load("images/sport/skateboard/teddy_bear_toy_3.png", Texture.class);
		assetManager.load("images/sport/skateboard/teddy_bear_toy_4.png", Texture.class);
		assetManager.load("images/sport/skateboard/teddy_bear_toy_5.png", Texture.class);
		assetManager.load("images/sport/skateboard/teddy_bear_toy_6.png", Texture.class);
		assetManager.load("images/sport/skateboard/board.png", Texture.class);
		assetManager.load("images/sport/skateboard/wheel.png", Texture.class);
		assetManager.load("images/sport/skateboard/spineboy/spineboy.atlas", TextureAtlas.class);
	}

	private void createCamera() {
		camera = (OrthographicCamera) getViewport().getCamera();
		camera.update();
	}

	@Override
	public void loadResourcesComplete(AssetManager assetManager) {
		// TODO Auto-generated method stub
		super.loadResourcesComplete(assetManager);
		batch = new SpriteBatch();
		createCamera();
		backgroundTexture = new Texture(Gdx.files.internal("images/sport/skateboard/back.png"));
		skateBoardSprite = new Sprite(new Texture("images/sport/skateboard/board.png"));
		rightWheelSprite = new Sprite(new Texture("images/sport/skateboard/wheel.png"));
		leftWheelSprite = new Sprite(new Texture("images/sport/skateboard/wheel.png"));
		
		// Create skateboard body and jointdef
		skateBoardBody = createBox(150 + getWorldX(), 300 + getWorldY(), skateBoardSprite.getWidth(),
				skateBoardSprite.getHeight(), BodyType.DynamicBody);
		skateBoardBody.setUserData("skateboard");

		PrismaticJointDef prismaticJointDef = new PrismaticJointDef();
		Vector2 axis = new Vector2(0f, 1f);
		prismaticJointDef.collideConnected = true;
		prismaticJointDef.initialize(groundBody, skateBoardBody, skateBoardBody.getWorldCenter(), axis);
		world.createJoint(prismaticJointDef);

		// Create right wheel body and shape
		BodyDef ballBodyDef = new BodyDef();
		ballBodyDef.type = BodyType.DynamicBody;
		ballBodyDef.position.set(toUnits(350 + getWorldX()), toUnits(100 + getWorldY()));
		rightWheelBody = world.createBody(ballBodyDef);
		CircleShape circle = new CircleShape();
		circle.setRadius(toUnits(rightWheelSprite.getWidth() / 2));
		FixtureDef ballShapeDef = new FixtureDef();
		ballShapeDef.shape = circle;
		ballShapeDef.density = 1.0f;
		ballShapeDef.friction = 0f;
		ballShapeDef.restitution = 0.3f;
		rightWheelBody.createFixture(ballShapeDef).setUserData("rightWheel");

		RevoluteJointDef revoluteJointDef1 = new RevoluteJointDef();
		revoluteJointDef1.bodyA = skateBoardBody;
		revoluteJointDef1.bodyB = rightWheelBody;
		revoluteJointDef1.localAnchorA.set(0.7f, -0.3f);
		revoluteJointDef1.localAnchorB.set(0, 0);
		world.createJoint(revoluteJointDef1);

		// Create left wheel body and shape
		ballBodyDef.position.set(toUnits(250 + getWorldX()), toUnits(100 + getWorldY()));
		leftWheelBody = world.createBody(ballBodyDef);
		leftWheelBody.createFixture(ballShapeDef).setUserData("leftWheel");

		RevoluteJointDef revoluteJointDef2 = new RevoluteJointDef();
		revoluteJointDef2.bodyA = skateBoardBody;
		revoluteJointDef2.bodyB = leftWheelBody;
		revoluteJointDef2.localAnchorA.set(-0.7f, -0.3f);
		revoluteJointDef2.localAnchorB.set(0, 0);
		world.createJoint(revoluteJointDef2);

		final TextureAtlas atlas = getGame().getAssetManager().get("images/sport/skateboard/spineboy/spineboy.atlas",
				TextureAtlas.class);
		SkeletonJson json = new SkeletonJson(atlas);
		json.setScale(0.5f);
		SkeletonData playerSkeletonData = json
				.readSkeletonData(Gdx.files.internal("images/sport/skateboard/spineboy/spineboy.json"));
		AnimationStateData playerAnimationData = new AnimationStateData(playerSkeletonData);
		skeletonRendererDebug = new SkeletonRendererDebug();

		skeletonRenderer = new SkeletonRenderer();
		skeleton = new Skeleton(playerSkeletonData);

		skeleton.setAttachment("head-bb", "head");
		animationState = new AnimationState(playerAnimationData);
		animationState.setAnimation(0, "idle", true);
		animationState.addListener(new AnimationStateListener() {
			public void event(int trackIndex, Event event) {
				// System.out.println(trackIndex + " event: " +
				// animationState.getCurrent(trackIndex) + ", " +
				// event.getData().getName() + ", "
				// + event.getInt());
			}

			public void complete(int trackIndex, int loopCount) {
				// System.out.println(trackIndex + " complete: " +
				// animationState.getCurrent(trackIndex) + ", " + loopCount);
				// if(animationState.getCurrent(trackIndex).isComplete()){
				animationState.setAnimation(0, "idle", true);
				// }
			}

			public void start(int trackIndex) {
				// System.out.println(trackIndex + " start: " +
				// animationState.getCurrent(trackIndex));
			}

			public void end(int trackIndex) {
				// System.out.println(trackIndex + " end: " +
				// animationState.getCurrent(trackIndex));
			}

		});

		// block body
		blockTextureList = new ArrayList<Texture>();
		blockTextureList.add(new Texture("images/sport/skateboard/teddy_bear_toy_1.png"));
		blockTextureList.add(new Texture("images/sport/skateboard/teddy_bear_toy_2.png"));
		blockTextureList.add(new Texture("images/sport/skateboard/teddy_bear_toy_3.png"));
		blockTextureList.add(new Texture("images/sport/skateboard/teddy_bear_toy_4.png"));
		blockTextureList.add(new Texture("images/sport/skateboard/teddy_bear_toy_5.png"));
		blockTextureList.add(new Texture("images/sport/skateboard/teddy_bear_toy_6.png"));
		roadblockSprite = new Sprite(blockTextureList.get((int) (Math.random() * 5)));

		BodyDef roadblockBodyDef = new BodyDef();
		roadblockBodyDef.type = BodyType.DynamicBody;
		roadblockBodyDef.position.set(toUnits(getWorldX() + getWorldWidth()), toUnits(getWorldY() + 200));
		roadblockBody = world.createBody(roadblockBodyDef);
		PolygonShape roadblockShape = new PolygonShape();
		roadblockShape.setAsBox(toUnits(roadblockSprite.getWidth() / 2 - 40), toUnits(roadblockSprite.getHeight() / 2));
		FixtureDef roadblockShapeDef = new FixtureDef();
		roadblockShapeDef.shape = roadblockShape;
		roadblockShapeDef.density = 0.2f;
		roadblockShapeDef.friction = 0.0f;
		roadblockShapeDef.restitution = 0.5f;
		roadblockBody.createFixture(roadblockShapeDef).setUserData("roadblock");
		roadblockBody.setLinearVelocity(-5f, 0f);
	}

	@Override
	public void unloadResources(AssetManager assetManager) {
		// TODO Auto-generated method stub
		super.unloadResources(assetManager);
		assetManager.unload("images/sport/skateboard/back.png");
		assetManager.unload("images/sport/skateboard/teddy_bear_toy_1.png");
		assetManager.unload("images/sport/skateboard/teddy_bear_toy_2.png");
		assetManager.unload("images/sport/skateboard/teddy_bear_toy_3.png");
		assetManager.unload("images/sport/skateboard/teddy_bear_toy_4.png");
		assetManager.unload("images/sport/skateboard/teddy_bear_toy_5.png");
		assetManager.unload("images/sport/skateboard/teddy_bear_toy_6.png");
		assetManager.unload("images/sport/skateboard/board.png");
		assetManager.unload("images/sport/skateboard/wheel.png");
		assetManager.unload("images/sport/skateboard/spineboy/spineboy.atlas");
	}
	
	@Override
	public boolean keyDown(int keyCode) {
		if ((keyCode == Input.Keys.ESCAPE) || (keyCode == Input.Keys.BACK)) {
			Scene matchScene = new MatchScene();
			Scene loadingScene = new LoadingScene(matchScene);
			matchScene.setInTransition(new NoTransition(loadingScene));
			loadingScene.setInTransition(new NoTransition(this));
			getGame().setScene(loadingScene);
			// getGame().setScene(new LoadingScene(new MatchScene()));
		}
		return false;
	}
}
