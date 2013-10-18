package us.nm.state.emnrd.livewallpaper;

/**
 * Listen for changes to PreferenceStore
 *
 */
public interface PreferenceStoreListener {
	
	 void onPreferenceStoreUpdate(String preference);
	
}
