package us.nm.state.emnrd.livewallpaper;

import java.util.Random;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.modifier.AlphaModifier;
import org.andengine.entity.particle.SpriteParticleSystem;
import org.andengine.entity.particle.emitter.RectangleParticleEmitter;
import org.andengine.entity.particle.initializer.AccelerationParticleInitializer;
import org.andengine.entity.particle.modifier.AlphaParticleModifier;
import org.andengine.entity.particle.modifier.ExpireParticleInitializer;
import org.andengine.entity.particle.modifier.RotationParticleModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.Background;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.ui.livewallpaper.BaseLiveWallpaperService;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;

import android.content.res.Configuration;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

public class LiveWallpaperService extends BaseLiveWallpaperService implements PreferenceStoreListener {

	//================================================================================
	//                                  Fields
	//================================================================================

	//	private Toast toast;

	private static int MODE = 0;

	private int cameraWidth = Constants.CAMERA_WIDTH;
	private int cameraHeight = Constants.CAMERA_HEIGHT;

	final int START_X = cameraWidth/2;
	final int START_Y = cameraHeight; // start offscreen

	int startX = 0;

	final float SLOW_EXPIRATION_TIME = 25f;
	final float FAST_EXPIRATION_TIME = 20f;
	float expirationTime = FAST_EXPIRATION_TIME;

	private Camera mCamera;
	private Scene mScene;

	private ITextureRegion mLogoOneTextureRegion; 
	private ITextureRegion mLogoTwoTextureRegion; 

	boolean setOptionsTumble;
	boolean setOptionsFast;

	private Random random;

	@Override
	public void onCreate() {
		super.onCreate();

		//android.os.Debug.waitForDebugger(); // remove for device/publish
	}

	@Override
	public EngineOptions onCreateEngineOptions() {

		Log.d("LiveWallpaperService.onCreateEngineOptions()", this.toString());		

		addPreferenceUpdateListener(this);

		final DisplayMetrics displayMetrics = new DisplayMetrics();
		WindowManager wm = (WindowManager)getSystemService(WINDOW_SERVICE);
		wm.getDefaultDisplay().getMetrics(displayMetrics);
		int width = displayMetrics.widthPixels;
		int height = displayMetrics.heightPixels;

		if (width > height) {			
			cameraWidth = Constants.CAMERA_HEIGHT;
			cameraHeight = Constants.CAMERA_WIDTH;
			MODE = 1; // mode 1 = landscape mode
			startX = cameraWidth/2;
			this.mCamera = new Camera(0, 0, cameraWidth, cameraHeight);
			return new EngineOptions(true, ScreenOrientation.LANDSCAPE_SENSOR, new RatioResolutionPolicy(cameraWidth, cameraHeight), mCamera);
		}
		else {
			cameraWidth = Constants.CAMERA_WIDTH;
			cameraHeight = Constants.CAMERA_HEIGHT;			
			MODE = 0; // mode 0 = portrait mode
			startX = cameraWidth/2;
			this.mCamera = new Camera(0, 0, cameraWidth, cameraHeight);
			return new EngineOptions(true, ScreenOrientation.PORTRAIT_SENSOR, new RatioResolutionPolicy(cameraWidth, cameraHeight), mCamera);

		}
	}

	@Override
	public void onCreateResources(OnCreateResourcesCallback createResourcesCallback) {

		random = new Random(System.currentTimeMillis());

		createResourceLogoOne("gfx/logo/one.png");
		createResourceLogoTwo("gfx/logo/two.png");

		createResourcesCallback.onCreateResourcesFinished();
	}


	/**
	 * 
	 */
	private void createResourceLogoOne(String pathAndFilename) {
		try {
			BitmapTextureAtlas bitmapTextureAtlas = new BitmapTextureAtlas
					(this.getTextureManager(),
							147,
							142, 
							TextureOptions.BILINEAR_PREMULTIPLYALPHA);

			mLogoOneTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset  (bitmapTextureAtlas, this, pathAndFilename, 0, 0);      

			this.getEngine().getTextureManager().loadTexture(bitmapTextureAtlas);
		} 
		catch (Exception e) {
			Log.e("onCreateResources", "Error setting atlas or region - " + pathAndFilename);
			e.printStackTrace();
		}
	}

	/**
	 * 
	 */
	private void createResourceLogoTwo(String pathAndFilename) {
		try {
			BitmapTextureAtlas bitmapTextureAtlas = new BitmapTextureAtlas
					(this.getTextureManager(),
							161,
							105, 
							TextureOptions.BILINEAR_PREMULTIPLYALPHA);

			mLogoTwoTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset  (bitmapTextureAtlas, this, pathAndFilename, 0, 0);      

			this.getEngine().getTextureManager().loadTexture(bitmapTextureAtlas);
		} 
		catch (Exception e) {
			Log.e("onCreateResources", "Error setting atlas or region - " + pathAndFilename);
			e.printStackTrace();
		}
	}

	///////////////////////////////////////////////////////////////////////////

	@Override
	public void onCreateScene(OnCreateSceneCallback createSceneCallback) {  

		//Log.d("LiveWallpaperService" , "onCreateScene()");
		
		mScene= new Scene();

		// set color of background of the mScene 
		mScene.setBackground(new Background(0.0f, 0.0f, 0.0f)); 

		getStoredPreferences();
		
		createSceneCallback.onCreateSceneFinished(mScene);

	}

	@Override
	public void onPopulateScene(Scene arg0, OnPopulateSceneCallback populateSceneCallback) {
		populateSceneCallback.onPopulateSceneFinished();
	}

	/**
	 * Get preferences stored from last time
	 * Set these values in PreferenceStore
	 * Recreate the scene 
	 */
	private void getStoredPreferences() {

		//Log.w("LiveWallpaperService.setStoredPreferences()", "(rain: " + displayRain + ", leaf: " + displayLeaf + ", " + ", snow: " + displaySnow + ")");

		// this is first call to getInstance(), so need to init
		//PreferenceStore preferenceStore = PreferenceStore.getInstance();
		PreferenceStore.initialize(this);
		
		onRecreateScene();
	}

	/**
	 * Recreate the scene by:
	 *    getting display color preference from PreferenceStore
	 *    detach all children
	 *    if color is set to display then created a particle system and attach it to scene
	 */
	public void onRecreateScene(){

		//Log.d("LiveWallpaperService" , "onRecreateScene()");
		
		PreferenceStore preferenceStore = PreferenceStore.getInstance();

		boolean displayLogoOne = preferenceStore.getValue(getResources().getString(R.string.preferenceKeyLogoOne));
		boolean displayLogoTwo = preferenceStore.getValue(getResources().getString(R.string.preferenceKeyLogoTwo));
		
		mScene.detachChildren();

		if(displayLogoOne) {
			SpriteParticleSystem particleSystem = generateLogoOneParticleEmitter();
			this.mScene.attachChild(particleSystem);	// attach particle system to scene
		}		
		if(displayLogoTwo) {
			SpriteParticleSystem particleSystem = generateLogoTwoParticleEmitter();
			this.mScene.attachChild(particleSystem);	// attach particle system to scene
		}
		
	}

	private SpriteParticleSystem generateLogoOneParticleEmitter() {

		// set the x y values of where the particles fall from
		final int mParticleX = startX;
		final int mParticleY = START_Y; // start offscreen

		// set the min and max rates that particles are generated per second
		final int mParticleMinRate = 1;
		final int mParticleMaxRate = 2;
		// set a variable for the max particles in the system.
		final int mParticleMax = 25;

		// Create Particle System.  
		RectangleParticleEmitter rectangleParticleEmmiter = new RectangleParticleEmitter(mParticleX, mParticleY, cameraWidth, 0 );

		final SpriteParticleSystem particleSystem = new SpriteParticleSystem(
				rectangleParticleEmmiter, 
				mParticleMinRate, 
				mParticleMaxRate, 
				mParticleMax,
				this.mLogoOneTextureRegion, 
				this.getVertexBufferObjectManager());

		particleSystem.registerEntityModifier(new AlphaModifier(2, 0, 255));
		// particleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		// set initial velocity  
		//		this.mVelocityParticleInitializer = new VelocityParticleInitializer<Sprite>(-100, 100, 100, 190);
		//		particleSystem.addParticleInitializer(this.mVelocityParticleInitializer);

		// add gravity so the particles fall downward
		//particleSystem.addParticleInitializer(new GravityParticleInitializer<Sprite>());

		// add acceleration so particles float 
		particleSystem.addParticleInitializer(new AccelerationParticleInitializer<Sprite>(0, getRandomAcceleration()));

		// add a rotation to particles
		//particleSystem.addParticleInitializer(new RotationParticleInitializer<Sprite>(0.0f, 90.0f));

		// have particles expire 
		particleSystem.addParticleInitializer(new ExpireParticleInitializer<Sprite>(expirationTime));

		particleSystem.addParticleModifier(getRotationParticleModifier());

		return particleSystem;
	}

	private SpriteParticleSystem generateLogoTwoParticleEmitter() {

		// set the x y values of where the particles fall from
		final int mParticleX = startX;
		final int mParticleY = START_Y; // start offscreen

		// set the min and max rates that particles are generated per second
		final int mParticleMinRate = 1;
		final int mParticleMaxRate = 2;
		// set a variable for the max particles in the system.
		final int mParticleMax = 25;

		// Create Particle System.  
		RectangleParticleEmitter rectangleParticleEmmiter = new RectangleParticleEmitter(mParticleX, mParticleY, cameraWidth, 0 );

		final SpriteParticleSystem particleSystem = new SpriteParticleSystem(
				rectangleParticleEmmiter, 
				mParticleMinRate, 
				mParticleMaxRate, 
				mParticleMax,
				this.mLogoTwoTextureRegion, 
				this.getVertexBufferObjectManager());

		particleSystem.registerEntityModifier(new AlphaModifier(2, 0, 255));
		// particleSystem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		// set initial velocity  
		//		this.mVelocityParticleInitializer = new VelocityParticleInitializer<Sprite>(-100, 100, 100, 190);
		//		particleSystem.addParticleInitializer(this.mVelocityParticleInitializer);

		// add gravity so the particles fall downward
		//particleSystem.addParticleInitializer(new GravityParticleInitializer<Sprite>());

		// add acceleration so particles float 
		// divide leaf acceleration by 2 so the fall slower
		particleSystem.addParticleInitializer(new AccelerationParticleInitializer<Sprite>(0, getRandomAcceleration()/2));

		// add a rotation to particles
		//particleSystem.addParticleInitializer(new RotationParticleInitializer<Sprite>(0.0f, 90.0f));

		// have particles expire 
		particleSystem.addParticleInitializer(new ExpireParticleInitializer<Sprite>(expirationTime));

		particleSystem.addParticleModifier(getRotationParticleModifier());

		return particleSystem;
	}

	@Override 
	public void onConfigurationChanged (Configuration newConfig) { 

		if(MODE == 0) {  
			if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) { 
				mScene.setScale(1); 
				mScene.setPosition(0, 0); 
			} 
			else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) 
			{ 
				mScene.setScaleY(1.6f); 
				mScene.setScaleX(0.6f);  

				mScene.setPosition(120, -240); 
			} 
		} 
		else if (MODE == 1) { 
			if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) { 
				mScene.setScaleY(0.6f); 
				mScene.setScaleX(1.6f);

				mScene.setPosition(-240, 120); 
			} 
			else if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) { 
				mScene.setScale(1); 
				mScene.setPosition(0, 0); 
			} 
		} 
	}

	// add listener to PreferenceStore
	private void addPreferenceUpdateListener (PreferenceStoreListener listener) 
	{
		//Log.d("LWService.addPreferenceUpdateListener", listener.toString());
		PreferenceStore preferenceStore = PreferenceStore.getInstance();
		while (preferenceStore.removeListener(listener)) {
			// do nothing, just remove all existing listeners
		}
		preferenceStore.addListener(listener);
	}

	@Override
	public void onPreferenceStoreUpdate(String preference) {

		//Log.d("LiveWallpaperService.onPreferenceUpdate", preference);
		onRecreateScene();

	} 

	/**
	 * 
	 * @return return clockwise or counter-clockwise  rotationParticleModifier
	 */
	private RotationParticleModifier<Sprite> getRotationParticleModifier() {

			int rotationDirection = 1; // may get flipped to -1

			int fromRotation = 0;
			int toRotation = 0 ;

			// vary the fromRotation so as to vary starting point
			if (setOptionsTumble) { // if tumble 

				if (random.nextBoolean()) {
					rotationDirection = -1; // go the other direction half the time
				}

				int low = -90;  
				int high = 90;  
				int i = random.nextInt(high-low);  
				fromRotation = i + low;

				// vary the toRotation so as to vary rotation speed
				low = 360;  
				high = 720;  
				i = random.nextInt(high-low);  
				toRotation = i + low;
			}

			RotationParticleModifier<Sprite> rotationParticleModifier = new RotationParticleModifier<Sprite>(
					0.0f, 
					expirationTime,
					fromRotation, 
					toRotation * rotationDirection);

			return rotationParticleModifier;
	}

	/**
	 * return random acceleration between a low and high value
	 * @return acceleration
	 */
	private int getRandomAcceleration() {

		// default to fast
		int low = 30;
		int high = 60;
		if ( ! setOptionsFast) { // if slow
			low = 5;
			high = 15;
			expirationTime = SLOW_EXPIRATION_TIME;
		}

		int i = random.nextInt(high-low);  
		int acceleration = i + low;
		acceleration *= -1; // -1 for GLES2
		//Log.w("LiveWallpaperService", "getRandomAcceleration(): " + acceleration);
		return acceleration;
	}

	/**
	 * 
	 * @return return alphaParticleModifier
	 */
	private AlphaParticleModifier<Sprite> getAlphaParticleModifier() {

//			int rotationDirection = 1; // may get flipped to -1

//			int fromAlpha = 0;
//			int toAlpha = 0 ;

//				if (random.nextBoolean()) {
//					rotationDirection = -1; // go the other direction half the time
//				}

//				int low = -90;  
//				int high = 90;  
//				int i = random.nextInt(high-low);  
//				fromAlpha = i + low;
//               fromAlpha = 0;
                
				// vary the toRotation so as to vary rotation speed
//				low = 360;  
//				high = 720;  
//				i = random.nextInt(high-low);  
//				toAlpha = i + low;
//				toAlpha = 255;

//			AlphaParticleModifier<Sprite> alphaParticleModifier = new AlphaParticleModifier<Sprite>(
//					0.0f, 
//					expirationTime,
//					fromAlpha, 
//					toAlpha * rotationDirection);
			
			AlphaParticleModifier<Sprite> alphaParticleModifier = new AlphaParticleModifier<Sprite>(
					1.0f, 
					0,
					0, 
					3);
			 
			return alphaParticleModifier;
	}
	
}
