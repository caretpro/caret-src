package caret.agent.codex.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import caret.agent.codex.Activator;

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
		store.setDefault(PreferenceConstants.P_CODEX_KEY, "");
		store.setDefault(PreferenceConstants.P_CODEX_URL, "https://api.openai.com/v1/responses");
		store.setDefault(PreferenceConstants.P_CODEX_MODEL, "codex-mini-latest");
	}

}
