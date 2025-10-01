package caret.agent.gpt.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import caret.agent.gpt.Activator;

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
		store.setDefault(PreferenceConstants.P_GPT_KEY, "");
		store.setDefault(PreferenceConstants.P_GPT_URL, "https://api.openai.com/v1/chat/completions");
		store.setDefault(PreferenceConstants.P_GPT_MODEL, "gpt-4.1-mini");
	}

}
