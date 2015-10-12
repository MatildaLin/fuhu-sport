package com.fuhu.gdx.sport;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
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
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.Align;
import com.fuhu.gdx.game.LoadingScene;
import com.fuhu.gdx.game.MatchScene;
import com.fuhu.gdx.physic.PhysicScene;
import com.fuhu.gdx.scene.Scene;
import com.fuhu.gdx.scene.SceneLayer;
import com.fuhu.gdx.scene.transition.NoTransition;

public class HockeyScene extends PhysicScene implements InputProcessor {
	private static final float METER_TO_PIXEL = 100;
	private World world;
	private Body groundBody;
	private Body goalBody;
	private boolean shot;
	private Body boxBody;
	private Body ballBody;
	private Texture backgroundTexture;
	Label promptLabel;
	Label scoreLabel;
	Label textLabel;
	Sprite boxSprite;
	Sprite ballSprite;
	Sprite goalSprite;
	Sprite hammerSprite;
	SceneLayer textLayer;
	float[] X_POSITION_ARRAY = { toUnits(getWorldWidth() / 5 + getWorldX()), toUnits(getWorldWidth() / 3 + getWorldX()),
			toUnits(getWorldWidth() / 2 + getWorldX()), toUnits(getWorldWidth() / 3 * 2 + getWorldX()),
			toUnits(getWorldWidth() / 5 * 4 + getWorldX()) };
	float previous_x_position;
	float x_position;
	private float deltaTime;
	private boolean showingToast = false;
	private OrthographicCamera camera;
	private int number;
	SpriteBatch batch;
	private boolean isPrepared = false;

	public HockeyScene() {
		super(METER_TO_PIXEL);
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setInputProcessor(this);
		
		setDebugBox2d(false);

		world = getWorld();
		world.setGravity(new Vector2(0, 0));
		
		createRoom();

	}

	private void update(float delta) {
		deltaTime = deltaTime + 1f;
	}

	private void updateXPosition() {
		do {
			x_position = X_POSITION_ARRAY[(int) (Math.random() * X_POSITION_ARRAY.length)];
		} while (previous_x_position == x_position);
		previous_x_position = x_position;
	}

	@Override
	public void render(float elapsedSeconds) {

		if (!isPrepared) {
			isPrepared = true;
		}
		
		if (showingToast)
			update(elapsedSeconds);

		// guard rule
		if (boxBody.getPosition().x < toUnits(getWorldWidth() / 2 - goalSprite.getWidth() / 2 + getWorldX()))
			boxBody.setLinearVelocity(toUnits(200f), 0f);
		else if (boxBody.getPosition().x > toUnits(getWorldWidth() / 2 + goalSprite.getWidth() / 2 + getWorldX()))
			boxBody.setLinearVelocity(toUnits(-200f), 0f);

		boxSprite.setPosition(toPixels(boxBody.getPosition().x) - boxSprite.getWidth() / 2,
				toPixels(boxBody.getPosition().y) - boxSprite.getHeight() / 2);
		ballSprite.setPosition(toPixels(ballBody.getPosition().x) - ballSprite.getWidth() / 2,
				toPixels(ballBody.getPosition().y) - ballSprite.getHeight() / 2);

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		batch.draw(backgroundTexture, getWorldX(), getWorldY(), getWorldWidth(), getWorldHeight());
		batch.draw(goalSprite, getWorldX() + getWorldWidth() / 2 - goalSprite.getWidth() / 2,
				getWorldY() + getWorldHeight() / 2 - goalSprite.getHeight() / 2 + 300);
		batch.draw(boxSprite, boxSprite.getX(), boxSprite.getY());
		batch.draw(ballSprite, ballSprite.getX(), ballSprite.getY());

		// Score rule
		if (ballBody.getPosition().x > toUnits(getWorldWidth() / 2 - goalSprite.getWidth() / 2 + getWorldX())
				&& ballBody.getPosition().x < toUnits(getWorldWidth() / 2 + goalSprite.getWidth() / 2 + getWorldX())
				&& ballBody.getPosition().y > toUnits(
						getWorldHeight() / 2 - goalSprite.getHeight() / 2 + 300 + getWorldY() + 50)
				&& ballBody.getPosition().y < toUnits(
						getWorldHeight() / 2 + goalSprite.getHeight() / 2 + 300 + getWorldY())) {
			number++;
			ballBody.setTransform(x_position, toUnits(getWorldHeight() / 2 + getWorldY() - 400), 0);
			ballBody.setLinearVelocity(0, 0);
			promptLabel.setText("" + number);
			shot = false;
			textLabel.setText("Score!!!");
			textLabel.setColor(Color.BLUE);
			addLayer(textLayer);
			showingToast = true;
			deltaTime = 0;
			updateXPosition();
		}
		// OutSide rule
		else if (ballBody.getPosition().y > toUnits(getWorldHeight() + getWorldY() - 200)
				|| ballBody.getPosition().y < toUnits(getWorldY() + 100)
				|| ballBody.getPosition().x < toUnits(getWorldX() + 100)
				|| ballBody.getPosition().x > toUnits(getWorldX() + getWorldWidth() - 100)) {
			ballBody.setTransform(x_position, toUnits(getWorldHeight() / 2 + getWorldY() - 400), 0);
			ballBody.setLinearVelocity(0, 0);
			shot = false;
			textLabel.setText("OUT!!!");
			textLabel.setColor(Color.RED);
			addLayer(textLayer);
			showingToast = true;
			deltaTime = 0;
			updateXPosition();
		}
		if (deltaTime > 50f) {
			removeLayer(textLayer);
			showingToast = false;
		}

		batch.end();
		super.render(elapsedSeconds);
	}

	private void createRoom() {

		createBox(getWorldX(), getWorldY(), 0, getViewport().getWorldHeight(), BodyDef.BodyType.StaticBody)
				.setUserData("ground");

		createBox(getWorldX() + getViewport().getWorldWidth(), getWorldY(), 0, getViewport().getWorldHeight(),
				BodyDef.BodyType.StaticBody).setUserData("ground");

		createBox(getWorldX(), getWorldY() + getViewport().getWorldHeight(), getViewport().getWorldWidth(), 0,
				BodyDef.BodyType.StaticBody).setUserData("ground");

		groundBody = createBox(getWorldX(), getWorldY(), getViewport().getWorldWidth(), 0, BodyDef.BodyType.StaticBody);
		groundBody.setUserData("ground");

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
		boxShapeDef.density = 0.1f;
		boxShapeDef.friction = 0f;
		boxShapeDef.restitution = 1.0f;
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
	public boolean touchDown(int x, int y, int pointer, int button) {
		if (isPrepared && shot == false) {
			Vector2 direction = new Vector2(toUnits(getWorldWidth() / 2 + getWorldX()),
					toUnits(getWorldHeight() / 2 + goalSprite.getHeight() / 2 + 300));
			// System.out.println("x,y = "+x+","+y);
			// System.out.println("ballBody.x,ballBody.y =
			// "+toPixels(ballBody.getPosition().x)+","+toPixels(ballBody.getPosition().y));
			direction.sub(ballBody.getPosition());
			direction.nor();

			float speed = 10;
			ballBody.setLinearVelocity(direction.scl(speed));
			// ballBody.setLinearVelocity(0, 10f);
			shot = true;
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
		assetManager.load("images/sport/hockey/background.jpg", Texture.class);
		assetManager.load("images/sport/hockey/guard.png", Texture.class);
		assetManager.load("images/sport/hockey/ball.png", Texture.class);
		assetManager.load("images/sport/hockey/goal.png", Texture.class);
		assetManager.load("images/sport/hockey/hammer.png", Texture.class);
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
		backgroundTexture = assetManager.get("images/sport/hockey/background.jpg", Texture.class);
		boxSprite = new Sprite(assetManager.get("images/sport/hockey/guard.png", Texture.class));
		ballSprite = new Sprite(assetManager.get("images/sport/hockey/ball.png", Texture.class));
		goalSprite = new Sprite(assetManager.get("images/sport/hockey/goal.png", Texture.class));
		hammerSprite = new Sprite(assetManager.get("images/sport/hockey/hammer.png", Texture.class));

		/** Prompt text */
		promptLabel = createLabel("" + number,
				manage(createBitmapFont("fonts/comicstripposter-regular.ttf", 180, Color.GREEN)));
		scoreLabel = createLabel("score:",
				manage(createBitmapFont("fonts/comicstripposter-regular.ttf", 50, Color.GRAY)));
		scoreLabel.setX(-scoreLabel.getWidth() * 1.1f);
		scoreLabel.setY(promptLabel.getHeight() / 4);
		SceneLayer promptLayer = new SceneLayer();
		promptLayer.setSize(promptLabel.getPrefWidth(), promptLabel.getPrefHeight());
		promptLayer.setPosition(getWorldWidth() - promptLayer.getWidth() - 150,
				getWorldHeight() - promptLayer.getHeight() - 10);
		// promptLayer.setAlign(Align.topRight);
		promptLayer.addActor(promptLabel);
		promptLayer.addActor(scoreLabel);
		addLayer(promptLayer);

		textLabel = createLabel("Score!!", manage(createBitmapFont("fonts/KOMIKAX_AXIS.ttf", 100, Color.WHITE)));
		textLayer = new SceneLayer();
		textLayer.setSize(textLabel.getPrefWidth(), textLabel.getPrefHeight());
		textLayer.setAlign(Align.center);
		textLayer.addActor(textLabel);

		boxBody = createBox(getWorldWidth() / 2 + getWorldX(), getWorldHeight() / 2 + getWorldY(), boxSprite.getWidth(),
				boxSprite.getHeight(), BodyType.KinematicBody);
		boxBody.setLinearVelocity(toUnits(-200f), 0f);
		boxBody.setUserData("guard");

		CircleShape circle = new CircleShape();
		circle.setRadius(toUnits(ballSprite.getWidth() / 2));
		BodyDef ballBodyDef = new BodyDef();
		ballBodyDef.type = BodyType.DynamicBody;
		x_position = X_POSITION_ARRAY[(int) (Math.random() * X_POSITION_ARRAY.length)];
		ballBodyDef.position.set(x_position, toUnits(getWorldHeight() / 2 + getWorldY() - 400));
		ballBody = world.createBody(ballBodyDef);
		ballBody.setUserData("ball");
		ballBody.createFixture(circle, 1);

		EdgeShape edge = new EdgeShape();
		FixtureDef groundShapeDef = new FixtureDef();
		groundShapeDef.shape = edge;
		groundShapeDef.restitution = 1;
		BodyDef bodyDef = new BodyDef();
		bodyDef.position.set(toUnits(getWorldX()), toUnits(getWorldY()));
		goalBody = world.createBody(bodyDef);
		// wall left
		edge.set(
				new Vector2(toUnits(getWorldWidth() / 2 - goalSprite.getWidth() / 2),
						toUnits(getWorldHeight() / 2 + goalSprite.getHeight() / 2 + 300)),
				new Vector2(toUnits(getWorldWidth() / 2 - goalSprite.getWidth() / 2),
						toUnits(getWorldHeight() / 2 - goalSprite.getHeight() / 2 + 300)));
		goalBody.createFixture(groundShapeDef);
		// wall right
		edge.set(
				new Vector2(toUnits(getWorldWidth() / 2 + goalSprite.getWidth() / 2),
						toUnits(getWorldHeight() / 2 + goalSprite.getHeight() / 2 + 300)),
				new Vector2(toUnits(getWorldWidth() / 2 + goalSprite.getWidth() / 2),
						toUnits(getWorldHeight() / 2 - goalSprite.getHeight() / 2 + 300)));
		goalBody.createFixture(groundShapeDef);
		// wall up
		edge.set(
				new Vector2(toUnits(getWorldWidth() / 2 - goalSprite.getWidth() / 2),
						toUnits(getWorldHeight() / 2 + goalSprite.getHeight() / 2 + 300)),
				new Vector2(toUnits(getWorldWidth() / 2 + goalSprite.getWidth() / 2),
						toUnits(getWorldHeight() / 2 + goalSprite.getHeight() / 2 + 300)));
		goalBody.createFixture(groundShapeDef);
	}

	@Override
	public void unloadResources(AssetManager assetManager) {
		// TODO Auto-generated method stub
		super.unloadResources(assetManager);
		assetManager.unload("images/sport/hockey/background.jpg");
		assetManager.unload("images/sport/hockey/guard.png");
		assetManager.unload("images/sport/hockey/ball.png");
		assetManager.unload("images/sport/hockey/goal.png");
		assetManager.unload("images/sport/hockey/hammer.png");
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
