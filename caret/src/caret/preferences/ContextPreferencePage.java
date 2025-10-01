package caret.preferences;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.*;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;

import caret.Activator;
import caret.ChatView;
import caret.agent.AgentInterface;
import caret.agent.Response;
import caret.preferences.MoveListEditor;
import caret.preferences.PreferenceConstants;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class ContextPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	
	public ContextPreferencePage() {
		
		super(GRID);
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
		setDescription("Settings of the input data context to sent to the agent:");
	}
	
	/**
	 * Creates the field editors. Field editors are abstractions of
	 * the common GUI blocks needed to manipulate various types
	 * of preferences. Each field editor knows how to save and
	 * restore itself.
	 */
	public void createFieldEditors() {
		addField(new BooleanFieldEditor(PreferenceConstants.P_CONTEXT_EXTENDED_CLASS, "Extended class", getFieldEditorParent()));
		addField(new BooleanFieldEditor(PreferenceConstants.P_CONTEXT_IMPLEMENTED_INTERFACE, "Implemented class", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_CONTEXT_ATTRIBUTE, "Attribute", getFieldEditorParent()));	
        addField(new BooleanFieldEditor(PreferenceConstants.P_CONTEXT_METHOD_PARAMETERS, "Method parameters", getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_CONTEXT_METHOD_VARIABLES, "Method variables", getFieldEditorParent()));	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		
	}
	
	@Override
	protected void performApply() {
		System.out.println("PERFORM APPLY");
	}

	@Override
	public boolean performOk() {
		System.out.println("PERFORM OK");
		IPreferenceStore store = Activator.getDefault().getPreferenceStore();
		System.out.println("AGENT STORE: "+store.getString(PreferenceConstants.P_AGENT));
		return super.performOk();
	}
}