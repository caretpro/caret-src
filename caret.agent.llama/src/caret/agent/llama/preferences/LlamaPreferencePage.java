package caret.agent.llama.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import caret.agent.llama.Activator;

public class LlamaPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public LlamaPreferencePage() {
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Llama settings:");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new StringFieldEditor(PreferenceConstants.P_LLAMA_KEY, "Key:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_LLAMA_URL, "URL:", getFieldEditorParent()));
		addField(new StringFieldEditor(PreferenceConstants.P_LLAMA_MODEL, "Model:", getFieldEditorParent()));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		
	}

}