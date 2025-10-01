package caret.agent.gemini.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import caret.agent.gemini.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_GEMINI_KEY,"");
		store.setDefault(PreferenceConstants.P_GEMINI_URL,"");
		store.setDefault(PreferenceConstants.P_GEMINI_MODEL,"");
		store.setDefault(PreferenceConstants.P_GEMINI_TOP_P,0.8f);
		store.setDefault(PreferenceConstants.P_GEMINI_TOP_K,40);
	}

}
