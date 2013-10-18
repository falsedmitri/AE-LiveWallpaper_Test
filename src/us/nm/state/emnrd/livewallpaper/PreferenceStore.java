package us.nm.state.emnrd.livewallpaper;

import java.util.ArrayList;
import java.util.List;

import android.content.ContextWrapper;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Singleton
 */

public class PreferenceStore {

	private List<PreferenceStoreListener> listenerArray = new ArrayList<PreferenceStoreListener>() ;

	private String preferenceString; // not really used

	private static boolean mLogoOneCheck; 
	private static boolean mLogoTwoCheck; 

	public static PreferenceStore instance;

	// need this to access getResources()
	private static ContextWrapper contextWrapper;

	private PreferenceStore() {

	}

	/**
	 * 
	 * @param sharedPreferences contains value only the instantiation, otherwise null
	 * @return
	 */
	public static PreferenceStore getInstance() {

		if (instance == null) {
			instance = new PreferenceStore();

		}
		return instance;
	}

	// this method should be called only once
	public static void initialize(ContextWrapper contextWrapper2) {

		Log.d("PreferenceStore.initialize", contextWrapper2.toString()); 

		contextWrapper = contextWrapper2;

		SharedPreferences sharedPreferences2 = contextWrapper.getSharedPreferences(Constants.PREFS_NAME, ContextWrapper.MODE_PRIVATE);

		mLogoOneCheck = sharedPreferences2.getBoolean(contextWrapper.getResources().getString(R.string.preferenceKeyLogoOne), true);
		mLogoTwoCheck = sharedPreferences2.getBoolean(contextWrapper.getResources().getString(R.string.preferenceKeyLogoTwo), false);

	}

	public Object getValue() { 
		return preferenceString; 
	}

	public boolean getValue(String preference) { 

		if (preference.equalsIgnoreCase(contextWrapper.getResources().getString(R.string.preferenceKeyLogoOne))) {
			return mLogoOneCheck; 
		}
		else if (preference.equalsIgnoreCase(contextWrapper.getResources().getString(R.string.preferenceKeyLogoTwo))) {
			return mLogoTwoCheck; 
		}

		return false;
	}

	/**
	 * Do not notify listeners when setting value
	 * Used when setting a bunch of preferences consecutively and you don't want to generate many notifies
	 * Must then explicitly notify
	 */
	public void setValueDoNotNotify(String preference, boolean displayThing) { 

		Log.d("setValueDoNotNotify", preference); 

		preferenceString = preference;

		if (preference.equalsIgnoreCase(contextWrapper.getResources().getString(R.string.preferenceKeyLogoOne))) {
			mLogoOneCheck = displayThing; 
		}
		else if (preference.equalsIgnoreCase(contextWrapper.getResources().getString(R.string.preferenceKeyLogoTwo))) {
			mLogoTwoCheck = displayThing; 
		}

	}

	/**
	 * Notify listeners when setting value
	 */
	public void setValueNotify(String preference, boolean displayThing) { 

		Log.d("setValueNotify", preference); 

		preferenceString = preference;

		if (preference.equalsIgnoreCase(contextWrapper.getResources().getString(R.string.preferenceKeyLogoOne))) {
			mLogoOneCheck = displayThing; 
		}
		else if (preference.equalsIgnoreCase(contextWrapper.getResources().getString(R.string.preferenceKeyLogoTwo))) {
			mLogoTwoCheck = displayThing; 
		}

		notifyListeners(); 
	}

	// called when a preference check box is clicked
	// could store here
	public void toggleValue(String preferenceKey) { 
		Log.d("PreferenceStore.toggleValue()" , preferenceKey);

		preferenceString = preferenceKey;

		if (preferenceKey.equalsIgnoreCase(contextWrapper.getResources().getString(R.string.preferenceKeyLogoOne))) {
			mLogoOneCheck = ! mLogoOneCheck; 
		}
		else if (preferenceKey.equalsIgnoreCase(contextWrapper.getResources().getString(R.string.preferenceKeyLogoTwo))) {
			mLogoTwoCheck = ! mLogoTwoCheck; 
		}

	}

	public void addListener(PreferenceStoreListener listener) {
		listenerArray.add(listener); 
	}

	public boolean removeListener(PreferenceStoreListener listener) { 
		return listenerArray.remove(listener); 
	}

	public void notifyAfterSetValueDoNotNotify() {
		notifyListeners();
	}

	private void notifyListeners() {
		Log.d("PreferenceStore.notifyListeners()" , "array size: " + listenerArray.size());
		for (PreferenceStoreListener listener : listenerArray) {
			try {
				listener.onPreferenceStoreUpdate(preferenceString) ;
			} 
			catch (Exception ex) { 
				Log.e("PreferenceStore.notifyListeners", ex.toString());
			}
		}
	}

	private void storeAllPreferences() {	

		Log.d("storeAllPreferences", "storeAllPreferences"); 

		storeBooleanPreference(contextWrapper.getResources().getString(R.string.preferenceKeyLogoOne),  mLogoOneCheck); 
		storeBooleanPreference(contextWrapper.getResources().getString(R.string.preferenceKeyLogoTwo), mLogoTwoCheck); 
	}

	private void storeBooleanPreference(String key, boolean value) {

		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(contextWrapper);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putBoolean(key, value);
		editor.commit();	

	}
}