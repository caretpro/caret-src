package caret.agent.deepseek.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import caret.agent.deepseek.Activator;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		store.setDefault(PreferenceConstants.P_DEEPSEEK_KEY, "");
		store.setDefault(PreferenceConstants.P_DEEPSEEK_URL, "https://api.deepseek.com/chat/completions");
		store.setDefault(PreferenceConstants.P_DEEPSEEK_MODEL, "deepseek-chat");
	}

}
