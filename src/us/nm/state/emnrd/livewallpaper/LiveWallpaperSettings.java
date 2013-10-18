package us.nm.state.emnrd.livewallpaper;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.util.Log;

public class LiveWallpaperSettings extends PreferenceActivity
implements  OnPreferenceClickListener, OnSharedPreferenceChangeListener 
{	

	//	@SuppressWarnings("deprecation")
	// called when "Settings" is clicked
	// we should get stored preference values here, or maybe not since
	// Stored preferences are obtained on LiveWallpaperService.OnCreateScene
	@Override
	public void onCreate(Bundle bundle)
	{
		Log.d("LiveWallpaperSettings" , "onCreate()");
		
		super.onCreate(bundle);

		//PreferenceManager preferenceActivity = this.getPreferenceManager();
		addPreferencesFromResource(R.xml.livewallpaper_settings);

		return;
	}

	// also called when clicking "Settings" (after OnCreate())
	@Override
	public void onResume()
	{
        Log.d("LiveWallpaperSettings.onResume()", "onResume()");
		super.onResume();

		PreferenceManager preferenceActivity = this.getPreferenceManager();
		SharedPreferences sharedPreferences = preferenceActivity.getSharedPreferences();
		sharedPreferences.registerOnSharedPreferenceChangeListener(this);		
		
	}

	@Override
	public void onPause() {
        Log.d("LiveWallpaperSettings.onPause()", "onPause()");
		super.onPause();

		PreferenceManager preferenceActivity = this.getPreferenceManager();
		SharedPreferences sharedPreferences = preferenceActivity.getSharedPreferences();
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	// called when leaving Settings and going back to the wallpaper
	// could store preferences here (or do them on at a time in onSharedPreferenceChanged() or PreferenceStore.toggleValue())
	@Override
	public void onBackPressed()	{
        Log.d("LiveWallpaperSettings.onBackPressed()", "onBackPressed()");

		super.onBackPressed();
		savePreferences();
	}

	/**
	 * 
	 */
	private void savePreferences() {

	    SharedPreferences settings = getSharedPreferences(Constants.PREFS_NAME, 0);
	    SharedPreferences.Editor editor = settings.edit();
		//SharedPreferences.Editor editor = getPreferences(MODE_PRIVATE).edit();

		PreferenceStore preferenceStore = PreferenceStore.getInstance();

		// persist the preferences
		Log.d("LiveWallpaperSetting.savePreferences()", "One: " + String.valueOf(preferenceStore.getValue(getResources().getString(R.string.preferenceKeyLogoOne)))); 
		Log.d("LiveWallpaperSetting.savePreferences()", "Two: " + String.valueOf(preferenceStore.getValue(getResources().getString(R.string.preferenceKeyLogoTwo)))); 

		// persist the preferences
		editor.putBoolean(getResources().getString(R.string.preferenceKeyLogoOne), preferenceStore.getValue(getResources().getString(R.string.preferenceKeyLogoOne))); 
		editor.putBoolean(getResources().getString(R.string.preferenceKeyLogoTwo), preferenceStore.getValue(getResources().getString(R.string.preferenceKeyLogoTwo))); 

		editor.commit();

		// Notify listeners in case any checkbox was toggled
		preferenceStore.notifyAfterSetValueDoNotNotify();

	}

	// called when preference check box is clicked
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key){
		PreferenceStore preferenceStore = PreferenceStore.getInstance();
		Log.d("LiveWallpaperSettings", "onSharedPreferenceChanged(): " + key);
		preferenceStore.toggleValue(key);

		// store to disk
		
	}

	@Override
	public boolean onPreferenceClick(Preference arg0) {
        Log.d("LiveWallpaperSettings.onPreferenceClick()", "arg0.getKey(): " + arg0.getKey());

		return false;
	}
	
	private Intent getFacebookIntent() {
		try {
			this.getPackageManager().getPackageInfo("com.facebook.katana", 0);
			return new Intent(Intent.ACTION_VIEW, Uri.parse("fb://profile/EMNRD"));
		} 
		catch (final Exception ex) {
			return new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/pages/EMNRD/160086370820551"));
		}
	}

	private Intent getGooglePlayStoreIntent() {
		return new Intent(Intent.ACTION_VIEW, Uri.parse("market://search?q=EMNRD"));
	}
	
	// called when clicking on a top-level menu selection, Women, Men, ...
	// or when clicking on a checkbox
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference)
    {
        Log.d("LiveWallpaperSettings.onPreferenceTreeClick()", preference.toString());
        
    	super.onPreferenceTreeClick(preferenceScreen, preference);
    	if (preference!=null)
	    	if (preference instanceof PreferenceScreen)
	        	if (((PreferenceScreen)preference).getDialog()!=null) {
	        		((PreferenceScreen)preference).getDialog().getWindow().getDecorView().setBackgroundDrawable(
	        				this.getWindow().getDecorView().getBackground().getConstantState().newDrawable());
	        	}
    	return false;
    }

}
